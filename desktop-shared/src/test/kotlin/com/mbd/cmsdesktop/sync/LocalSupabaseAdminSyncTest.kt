package com.mbd.cmsdesktop.sync

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Drives the admin app's REAL refresh (AdminDataBootstrapper.refreshAllReport over the real Dagger graph, real Room
 * database, real Supabase client) against the local scratch Postgres via supabase/tests/local/postgrest-shim.mjs.
 * For every synced table it checks: a first refresh pulls the seeded rows, an idle refresh pulls (almost) nothing,
 * and after one insert + one update + one soft delete the next refresh brings exactly those into the local cache.
 * Skipped unless the shim is running.
 */
class LocalSupabaseAdminSyncTest {

    private val shim = "http://localhost:54330"
    private val http = HttpClient.newHttpClient()

    private fun call(method: String, path: String, body: String? = null): String {
        val b = HttpRequest.newBuilder(URI.create(shim + path)).header("content-type", "application/json")
        val req = if (body != null) b.method(method, HttpRequest.BodyPublishers.ofString(body)).build() else b.method(method, HttpRequest.BodyPublishers.noBody()).build()
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body()
    }

    private fun sql(statement: String) {
        val out = call("POST", "/__sql", buildJsonObject { put("sql", statement) }.toString())
        check(!out.contains("\"message\"")) { "SQL failed: $statement -> $out" }
    }

    private fun shimUp() = runCatching { call("GET", "/__log"); true }.getOrDefault(false)

    /** table -> (rows from the checkpointed "updated_at >= since" queries, rows from other lookups such as merge links). */
    private fun fetchedByTable(): Map<String, Pair<Int, Int>> {
        val log = Json.parseToJsonElement(call("GET", "/__log")).jsonArray.map { it.jsonObject }
        call("POST", "/__log/clear")
        return log.filter { it["method"]?.jsonPrimitive?.content == "GET" }
            .groupBy { it["table"]!!.jsonPrimitive.content }
            .mapValues { (_, v) ->
                val (inc, other) = v.partition { it["query"]!!.jsonPrimitive.content.contains("updated_at=gt") }
                inc.sumOf { it["returned"]!!.jsonPrimitive.int } to other.sumOf { it["returned"]!!.jsonPrimitive.int }
            }
    }

    private fun fmt(p: Pair<Int, Int>?): String = if (p == null) "0" else if (p.second == 0) "${p.first}" else "${p.first}+${p.second}lk"

    private lateinit var dbFile: File
    private var report7 = ""

    private val truncateAll = "truncate period_sessions, timetable_periods, session_attendance, session_marks, student_semester_gpa, session_fee_heads, session_fees, fines, exam_paper_submissions, datesheet_slots, datesheets, mark_edit_requests, student_link_requests, notifications, calendar_events, semester_terms, session_subjects, session_students, academic_sessions, teachers, rooms, buildings, departments, profiles, auth.users cascade"

    /** A brand-new app instance (own Room file, own Dagger graph) pointed at the local shim. */
    private fun startApp(): com.mbd.cmscommon.data.sync.AdminDataBootstrapper {
        val appId = "synctest-${System.nanoTime()}"
        System.setProperty("cms.desktop.appId", appId)
        System.setProperty("cms.supabase.url", shim)
        System.setProperty("cms.supabase.anonKey", "anon")
        dbFile = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "CMSDesktop/$appId/cms.db")
        return DesktopAppComponent.create().adminDataBootstrapper()
    }

    private fun <T> local(block: (SQLiteConnection) -> T): T {
        val c = BundledSQLiteDriver().open(dbFile.absolutePath)
        return try { block(c) } finally { c.close() }
    }

    private fun scalar(query: String): Int = local { c ->
        c.prepare(query).use { st -> if (st.step()) st.getLong(0).toInt() else -1 }
    }

    private fun activeCount(table: String): Int = runCatching { scalar("select count(*) from $table where isDeleted = 0") }.getOrElse { scalar("select count(*) from $table") }

    private class Case(
        val remote: String,
        val local: String,
        val seed: List<String>,
        /** Rows expected in the local table after the first refresh. */
        val expectSeed: Int,
        val insert: String,
        /** Sets a column to a marker value on one existing row; [probe] then counts local rows carrying the marker. */
        val update: String,
        val probe: String,
        val delete: String,
        /** Local-side filter for counting (e.g. only a period's own rows). */
        val countWhere: String = "isDeleted = 0",
        /** Independent checkpoint scopes the app keeps for this table (notifications: one per role). */
        val scopes: Int = 1,
    )

    private val cases = listOf(
        Case("profiles", "administrator_accounts",
            listOf(
                "do $$ begin insert into auth.users(id,email) values (gen_random_uuid(),'a1@x.pk'); update profiles set role='ADMIN' where email='a1@x.pk'; end $$",
                "do $$ begin insert into auth.users(id,email) values (gen_random_uuid(),'a2@x.pk'); update profiles set role='ADMIN' where email='a2@x.pk'; end $$",
                "do $$ begin insert into auth.users(id,email) values (gen_random_uuid(),'avic@x.pk'); update profiles set role='ADMIN' where email='avic@x.pk'; end $$",
                "insert into auth.users(id,email) values (gen_random_uuid(),'student@x.pk')",
            ), 3,
            "do $$ begin insert into auth.users(id,email) values (gen_random_uuid(),'a3@x.pk'); update profiles set role='ADMIN' where email='a3@x.pk'; end $$",
            "update profiles set status='DISABLED' where email='a1@x.pk'",
            "select count(*) from administrator_accounts where status='DISABLED'",
            "update profiles set is_deleted=true where email='avic@x.pk'"),
        Case("departments", "departments",
            listOf("insert into departments(dept_id,name,code) values ('ch','Chemistry','CH')", "insert into departments(dept_id,name,code) values ('ur','Urdu','UR')", "insert into departments(dept_id,name,code) values ('vic','Victim','VIC')"), 3,
            "insert into departments(dept_id,name,code) values ('ph','Physics','PH')",
            "update departments set name='UPD' where dept_id='ch'",
            "select count(*) from departments where name='UPD'",
            "update departments set is_deleted=true where dept_id='vic'"),
        Case("buildings", "buildings",
            listOf("insert into buildings(building_id,name,code) values ('b1','Main','MB')", "insert into buildings(building_id,name,code) values ('bvic','Victim','VB')"), 2,
            "insert into buildings(building_id,name,code) values ('b2','Annex','AX')",
            "update buildings set name='UPD' where building_id='b1'",
            "select count(*) from buildings where name='UPD'",
            "update buildings set is_deleted=true where building_id='bvic'"),
        Case("rooms", "rooms",
            listOf("insert into rooms(room_id,building_id,room_no,name) values ('r1','b1','R14','Room 14')", "insert into rooms(room_id,building_id,room_no,name) values ('r2','b1','R15','Room 15')", "insert into rooms(room_id,building_id,room_no,name) values ('rvic','b1','R99','Victim')"), 3,
            "insert into rooms(room_id,building_id,room_no,name) values ('r3','b1','R16','Room 16')",
            "update rooms set name='UPD' where room_id='r1'",
            "select count(*) from rooms where name='UPD'",
            "update rooms set is_deleted=true where room_id='rvic'"),
        Case("teachers", "teachers",
            listOf("insert into teachers(email,name,dept_id) values ('t1@x.pk','Teacher One','ch')", "insert into teachers(email,name,dept_id) values ('t2@x.pk','Teacher Two','ur')", "insert into teachers(email,name,dept_id) values ('tvic@x.pk','Victim','ch')"), 3,
            "insert into teachers(email,name,dept_id) values ('t3@x.pk','Teacher Three','ch')",
            "update teachers set name='UPD' where email='t1@x.pk'",
            "select count(*) from teachers where name='UPD'",
            "update teachers set is_deleted=true where email='tvic@x.pk'"),
        Case("academic_sessions", "academic_sessions",
            listOf(
                "insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ch_2023','ch',2023,2027,50,3,'MORNING','BS')",
                "insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ur_2023','ur',2023,2027,50,3,'MORNING','BS')",
                "insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ch_2024','ch',2024,2028,50,1,'MORNING','BS')",
            ), 3,
            "insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ur_2024','ur',2024,2028,50,1,'MORNING','BS')",
            "update academic_sessions set program_name='UPD' where session_id='ch_2023'",
            "select count(*) from academic_sessions where programName='UPD'",
            "update academic_sessions set is_deleted=true where session_id='ch_2024'"),
        Case("session_students", "session_students",
            listOf(
                "insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-01','Ali','MORNING')",
                "insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-02','Sara','MORNING')",
                "insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-03','Victim','MORNING')",
                "insert into session_students(session_id,roll_number,name,shift) values ('ur_2023','R-01','Omar','MORNING')",
            ), 4,
            "insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-04','Newbie','MORNING')",
            "update session_students set name='UPD' where session_id='ch_2023' and roll_number='R-01'",
            "select count(*) from session_students where name='UPD'",
            "update session_students set is_deleted=true where session_id='ch_2023' and roll_number='R-03'"),
        Case("session_subjects", "semester_subjects",
            listOf(
                "insert into session_subjects(session_id,semester,course_code,name,credit_hours) values ('ch_2023',3,'GE-101','English',3)",
                "insert into session_subjects(session_id,semester,course_code,name,credit_hours) values ('ch_2023',3,'GE-102','Maths',3)",
                "insert into session_subjects(session_id,semester,course_code,name,credit_hours) values ('ch_2023',3,'GE-103','Victim',3)",
            ), 3,
            "insert into session_subjects(session_id,semester,course_code,name,credit_hours) values ('ch_2023',3,'GE-104','Physics',3)",
            "update session_subjects set name='UPD' where course_code='GE-101'",
            "select count(*) from semester_subjects where name='UPD'",
            "update session_subjects set is_deleted=true where course_code='GE-103'"),
        Case("semester_terms", "semester_terms",
            listOf(
                "insert into semester_terms(session_id,semester,start_date,end_date) values ('ch_2023',3,'2026-01-01','2026-05-01')",
                "insert into semester_terms(session_id,semester,start_date,end_date) values ('ch_2023',4,'2026-06-01','2026-10-01')",
                "insert into semester_terms(session_id,semester,start_date,end_date) values ('ur_2023',3,'2026-01-01','2026-05-01')",
            ), 3,
            "insert into semester_terms(session_id,semester,start_date,end_date) values ('ur_2023',4,'2026-06-01','2026-10-01')",
            "update semester_terms set start_date='2026-02-02' where session_id='ch_2023' and semester=3",
            "select count(*) from semester_terms where startDate='2026-02-02'",
            "update semester_terms set is_deleted=true where session_id='ur_2023' and semester=3"),
        Case("timetable_periods", "session_periods",
            listOf(
                "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('ch_2023','MONDAY','09:00','10:00','GE-101','English','t1@x.pk','R1','MORNING')",
                "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('ch_2023','TUESDAY','09:00','10:00','GE-102','Maths','t1@x.pk','R1','MORNING')",
                "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('ch_2023','WEDNESDAY','09:00','10:00','GE-103','Victim','t1@x.pk','R1','MORNING')",
                "insert into period_sessions(period_id,session_id) select id,'ur_2023' from timetable_periods where day='MONDAY'",
            ), 3,
            "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('ch_2023','FRIDAY','09:00','10:00','GE-104','Physics','t1@x.pk','R1','MORNING')",
            "update timetable_periods set room_no='UPD' where day='TUESDAY'",
            "select count(*) from session_periods where roomNo='UPD'",
            "update timetable_periods set is_deleted=true where day='WEDNESDAY'",
            countWhere = "isDeleted = 0 and id = remotePeriodId"),
        Case("calendar_events", "calendar_events",
            listOf(
                "insert into calendar_events(title,event_type,start_date) values ('Sports Day','EVENT','2026-10-01')",
                "insert into calendar_events(title,event_type,start_date) values ('Eid Break','HOLIDAY','2026-10-05')",
                "insert into calendar_events(title,event_type,start_date) values ('Victim','EVENT','2026-10-09')",
            ), 3,
            "insert into calendar_events(title,event_type,start_date) values ('Convocation','EVENT','2026-11-01')",
            "update calendar_events set title='UPD' where title='Sports Day'",
            "select count(*) from calendar_events where title='UPD'",
            "update calendar_events set is_deleted=true where title='Victim'"),
        Case("datesheets", "datesheets",
            listOf(
                "insert into datesheets(id,session_id,semester,shift) values ('11111111-1111-1111-1111-111111111111','ch_2023',3,'MORNING')",
                "insert into datesheets(id,session_id,semester,shift) values ('22222222-2222-2222-2222-222222222222','ur_2023',3,'MORNING')",
                "insert into datesheets(id,session_id,semester,shift) values ('33333333-3333-3333-3333-333333333333','ch_2024',1,'MORNING')",
            ), 3,
            "insert into datesheets(id,session_id,semester,shift) values ('44444444-4444-4444-4444-444444444444','ur_2024',1,'MORNING')",
            "update datesheets set instructions='UPD' where id='11111111-1111-1111-1111-111111111111'",
            "select count(*) from datesheets where instructions='UPD'",
            "update datesheets set is_deleted=true where id='33333333-3333-3333-3333-333333333333'"),
        Case("datesheet_slots", "datesheet_slots",
            listOf(
                "insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name) values ('11111111-1111-1111-1111-111111111111','2026-12-01','09:00','12:00','GE-101','English')",
                "insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name) values ('11111111-1111-1111-1111-111111111111','2026-12-02','09:00','12:00','GE-102','Maths')",
                "insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name) values ('22222222-2222-2222-2222-222222222222','2026-12-03','09:00','12:00','UR-201','Victim')",
            ), 3,
            "insert into datesheet_slots(datesheet_id,exam_date,start_time,end_time,course_code,subject_name) values ('22222222-2222-2222-2222-222222222222','2026-12-04','09:00','12:00','UR-202','Poetry')",
            "update datesheet_slots set subject_name='UPD' where course_code='GE-101'",
            "select count(*) from datesheet_slots where subjectName='UPD'",
            "update datesheet_slots set is_deleted=true where course_code='UR-201'"),
        Case("mark_edit_requests", "mark_edit_requests",
            listOf(
                "insert into mark_edit_requests(session_id,semester,course_code,exam_type,roll_number,current_score,requested_score,requested_by) values ('ch_2023',3,'GE-101','MIDTERM','R-01',10,12,'t1@x.pk')",
                "insert into mark_edit_requests(session_id,semester,course_code,exam_type,roll_number,current_score,requested_score,requested_by) values ('ch_2023',3,'GE-101','MIDTERM','R-02',11,13,'t1@x.pk')",
                "insert into mark_edit_requests(session_id,semester,course_code,exam_type,roll_number,current_score,requested_score,requested_by) values ('ch_2023',3,'GE-102','MIDTERM','R-01',9,14,'t1@x.pk')",
            ), 3,
            "insert into mark_edit_requests(session_id,semester,course_code,exam_type,roll_number,current_score,requested_score,requested_by) values ('ch_2023',3,'GE-102','MIDTERM','R-02',8,15,'t1@x.pk')",
            "update mark_edit_requests set requested_score=24 where course_code='GE-101' and roll_number='R-01'",
            "select count(*) from mark_edit_requests where requestedScore=24",
            "update mark_edit_requests set is_deleted=true where course_code='GE-102' and roll_number='R-01'"),
        Case("session_attendance", "session_attendance_rows",
            listOf(
                "insert into session_attendance(session_id,semester,course_code,date,roll_number,status) values ('ch_2023',3,'GE-101','2026-09-01','R-01','PRESENT')",
                "insert into session_attendance(session_id,semester,course_code,date,roll_number,status) values ('ch_2023',3,'GE-101','2026-09-01','R-02','ABSENT')",
                "insert into session_attendance(session_id,semester,course_code,date,roll_number,status) values ('ch_2023',3,'GE-101','2026-09-01','R-03','PRESENT')",
            ), 3,
            "insert into session_attendance(session_id,semester,course_code,date,roll_number,status) values ('ch_2023',3,'GE-101','2026-09-02','R-01','PRESENT')",
            "update session_attendance set remark='UPD' where roll_number='R-01' and date='2026-09-01'",
            "select count(*) from session_attendance_rows where remark='UPD'",
            "update session_attendance set is_deleted=true where roll_number='R-03'"),
        Case("session_marks", "session_marks",
            listOf(
                "insert into session_marks(session_id,semester,course_code,exam_type,roll_number,score,max_marks) values ('ch_2023',3,'GE-101','MIDTERM','R-01',10,25)",
                "insert into session_marks(session_id,semester,course_code,exam_type,roll_number,score,max_marks) values ('ch_2023',3,'GE-101','MIDTERM','R-02',11,25)",
                "insert into session_marks(session_id,semester,course_code,exam_type,roll_number,score,max_marks) values ('ch_2023',3,'GE-101','MIDTERM','R-03',12,25)",
            ), 3,
            "insert into session_marks(session_id,semester,course_code,exam_type,roll_number,score,max_marks) values ('ch_2023',3,'GE-102','MIDTERM','R-01',13,25)",
            "update session_marks set score=24 where roll_number='R-01' and course_code='GE-101'",
            "select count(*) from session_marks where score=24",
            "update session_marks set is_deleted=true where roll_number='R-03' and course_code='GE-101'"),
        Case("student_semester_gpa", "student_semester_gpa",
            listOf(
                "insert into student_semester_gpa(session_id,roll_number,semester,gpa,cgpa) values ('ch_2023','R-01',1,3.1,3.1)",
                "insert into student_semester_gpa(session_id,roll_number,semester,gpa,cgpa) values ('ch_2023','R-02',1,3.2,3.2)",
                "insert into student_semester_gpa(session_id,roll_number,semester,gpa,cgpa) values ('ch_2023','R-03',1,3.3,3.3)",
            ), 3,
            "insert into student_semester_gpa(session_id,roll_number,semester,gpa,cgpa) values ('ch_2023','R-01',2,3.4,3.25)",
            "update student_semester_gpa set gpa=3.9 where roll_number='R-01' and semester=1",
            "select count(*) from student_semester_gpa where gpa=3.9",
            "update student_semester_gpa set is_deleted=true where roll_number='R-03'"),
        Case("session_fees", "session_fees",
            listOf(
                "insert into session_fees(session_id,shift,cadence) values ('ch_2023','MORNING','SEMESTER')",
                "insert into session_fees(session_id,shift,cadence) values ('ur_2023','MORNING','SEMESTER')",
                "insert into session_fees(session_id,shift,cadence) values ('ch_2024','MORNING','SEMESTER')",
            ), 3,
            "insert into session_fees(session_id,shift,cadence) values ('ur_2024','MORNING','SEMESTER')",
            "update session_fees set payment_note='UPD' where session_id='ch_2023'",
            "select count(*) from session_fees where paymentNote='UPD'",
            "update session_fees set is_deleted=true where session_id='ch_2024'"),
        Case("session_fee_heads", "session_fee_heads",
            listOf(
                "insert into session_fee_heads(session_id,shift,label,amount,position) values ('ch_2023','MORNING','Tuition',10000,0)",
                "insert into session_fee_heads(session_id,shift,label,amount,position) values ('ch_2023','MORNING','Library',500,1)",
                "insert into session_fee_heads(session_id,shift,label,amount,position) values ('ch_2023','MORNING','Victim',1,2)",
            ), 3,
            "insert into session_fee_heads(session_id,shift,label,amount,position) values ('ch_2023','MORNING','Lab',800,3)",
            "update session_fee_heads set amount=777 where label='Tuition'",
            "select count(*) from session_fee_heads where amount=777",
            "update session_fee_heads set is_deleted=true where label='Victim'"),
        Case("fines", "fines",
            listOf(
                "insert into fines(session_id,roll_number,category,amount,reason) values ('ch_2023','R-01','LIBRARY',100,'late book')",
                "insert into fines(session_id,roll_number,category,amount,reason) values ('ch_2023','R-02','LIBRARY',200,'lost book')",
                "insert into fines(session_id,roll_number,category,amount,reason) values ('ch_2023','R-03','LIBRARY',300,'victim')",
            ), 3,
            "insert into fines(session_id,roll_number,category,amount,reason) values ('ch_2023','R-04','LIBRARY',400,'new')",
            "update fines set amount=999 where roll_number='R-01'",
            "select count(*) from fines where amount=999",
            "update fines set is_deleted=true where roll_number='R-03'"),
        Case("exam_paper_submissions", "exam_paper_submissions",
            listOf(
                "insert into exam_paper_submissions(session_id,semester,course_code,teacher_email,storage_path,file_name) values ('ch_2023',3,'GE-101','t1@x.pk','p/1.pdf','one.pdf')",
                "insert into exam_paper_submissions(session_id,semester,course_code,teacher_email,storage_path,file_name) values ('ch_2023',3,'GE-102','t1@x.pk','p/2.pdf','two.pdf')",
                "insert into exam_paper_submissions(session_id,semester,course_code,teacher_email,storage_path,file_name) values ('ch_2023',3,'GE-103','t1@x.pk','p/3.pdf','victim.pdf')",
            ), 3,
            "insert into exam_paper_submissions(session_id,semester,course_code,teacher_email,storage_path,file_name) values ('ch_2023',3,'GE-104','t1@x.pk','p/4.pdf','four.pdf')",
            "update exam_paper_submissions set file_name='UPD.pdf' where course_code='GE-101'",
            "select count(*) from exam_paper_submissions where fileName='UPD.pdf'",
            "update exam_paper_submissions set is_deleted=true where course_code='GE-103'"),
        Case("student_link_requests", "student_link_requests",
            listOf(
                "insert into student_link_requests(requested_by_email,roll_number_claimed,session_id) values ('s1@x.pk','R-01','ch_2023')",
                "insert into student_link_requests(requested_by_email,roll_number_claimed,session_id) values ('s2@x.pk','R-02','ch_2023')",
                "insert into student_link_requests(requested_by_email,roll_number_claimed,session_id) values ('s3@x.pk','R-03','ch_2023')",
            ), 3,
            "insert into student_link_requests(requested_by_email,roll_number_claimed,session_id) values ('s4@x.pk','R-04','ch_2023')",
            "update student_link_requests set message='UPD' where roll_number_claimed='R-01'",
            "select count(*) from student_link_requests where message='UPD'",
            "update student_link_requests set is_deleted=true where roll_number_claimed='R-03'"),
        Case("notifications", "notifications",
            listOf(
                "insert into notifications(title,body,target_role,created_by_email) values ('Hello','Body one','ALL','a1@x.pk')",
                "insert into notifications(title,body,target_role,created_by_email) values ('Exam','Body two','STUDENT','a1@x.pk')",
                "insert into notifications(title,body,target_role,created_by_email) values ('Victim','Body three','TEACHER','a1@x.pk')",
            ), 3,
            "insert into notifications(title,body,target_role,created_by_email) values ('Fresh','Body four','ALL','a1@x.pk')",
            "update notifications set title='UPD' where title='Hello'",
            "select count(*) from notifications where title='UPD'",
            "update notifications set is_deleted=true where title='Victim'",
            scopes = 3),
    )

    @Test
    fun bulkImportIsPagedAndCompletelyFetched() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        call("POST", "/__fail", """{"tables":[]}""")
        sql(truncateAll)
        sql("insert into departments(dept_id,name,code) values ('ch','Chemistry','CH')")
        sql("insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ch_2023','ch',2023,2027,50,3,'MORNING','BS')")
        sql("insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-01','Ali','MORNING')")
        // One statement = one transaction = 1,200 rows sharing the same updated_at (a typical bulk import).
        sql("insert into fines(session_id,roll_number,category,amount,reason) select 'ch_2023','R-01','LIBRARY',10,'bulk-'||g from generate_series(1,1200) g")
        call("POST", "/__log/clear")
        val app = startApp()
        val notes = mutableListOf<String>()
        val problems = mutableListOf<String>()

        app.refreshAllReport()
        val first = fetchedByTable()["fines"]?.first ?: 0
        val cached = activeCount("fines")
        val distinct = scalar("select count(distinct fineId) from fines")
        notes += "first refresh: fetched $first fines rows across pages (server 1200) -> cached $cached ($distinct distinct)"
        if (cached != 1200 || distinct != 1200) problems += "bulk import: cached $cached / $distinct distinct of 1200 fines rows (page boundaries lost or duplicated rows)"

        app.refreshAllReport()
        val idle = fetchedByTable()["fines"]?.first ?: 0
        notes += "idle refresh right after a bulk import: fetched $idle rows (all 1200 share one updated_at)"

        sql("insert into fines(session_id,roll_number,category,amount,reason) values ('ch_2023','R-01','LIBRARY',1,'new')")
        app.refreshAllReport()
        val afterNew = fetchedByTable()["fines"]?.first ?: 0
        notes += "after one new fine: fetched $afterNew row(s) -> cached ${activeCount("fines")}"
        if (activeCount("fines") != 1201) problems += "bulk import: the new fine did not arrive"
        if (afterNew != 1) problems += "bulk import: one new fine caused $afterNew rows to be fetched"

        app.refreshAllReport()
        val settled = fetchedByTable()["fines"]?.first ?: 0
        notes += "next idle refresh: fetched $settled rows"
        println("\n=== BULK IMPORT ===\n" + notes.joinToString("\n") + "\nproblems: $problems")
        File(System.getProperty("java.io.tmpdir"), "cms-bulk-sync-report.txt").writeText(notes.joinToString("\n") + "\nproblems: $problems")
        // Rows written together share one updated_at; the strict "> checkpoint" pull must not keep returning them.
        if (idle > 0 || settled > 0) problems += "bulk import: idle refreshes re-downloaded $idle / $settled rows"
        assertTrue(problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun adminRefreshFetchesOnlyChangesForEveryTable() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())

        call("POST", "/__fail", """{"tables":[]}""")
        sql(truncateAll)
        // Parents first: the case order above already respects foreign keys; seeding is one statement per row so every row
        // gets its own updated_at.
        for (case in cases) for (s in case.seed) { sql(s); Thread.sleep(8) }
        call("POST", "/__log/clear")

        val bootstrapper = startApp()
        val failures = mutableListOf<String>()
        val rows = linkedMapOf<String, MutableMap<String, String>>()
        fun cell(case: Case, key: String, value: Any) { rows.getOrPut(case.remote) { linkedMapOf() }[key] = value.toString() }
        fun fail(msg: String) { failures += msg; println("FAIL: $msg") }

        // 1. First refresh: the cache is empty, so every seeded row must be pulled.
        val first = bootstrapper.refreshAllReport()
        val firstFetch = fetchedByTable()
        println("first refresh report: successful=${first.successful} message=${first.message}")
        if (!first.successful) fail("first refresh reported failures: ${first.message}")
        for (case in cases) {
            val got = runCatching { scalar("select count(*) from ${case.local} where ${case.countWhere}") }.getOrElse { -1 }
            cell(case, "seed", case.expectSeed); cell(case, "1st fetch", fmt(firstFetch[case.remote])); cell(case, "cached", got)
            if (got != case.expectSeed) fail("${case.remote}: first refresh cached $got rows in ${case.local}, expected ${case.expectSeed}")
        }

        // 2. Idle refresh: nothing changed, so at most the checkpoint boundary rows come back.
        bootstrapper.refreshAllReport()
        val idleFetch = fetchedByTable()
        for (case in cases) {
            val idle = idleFetch[case.remote]?.first ?: 0
            cell(case, "idle fetch", fmt(idleFetch[case.remote]))
            if (idle > case.scopes) fail("${case.remote}: idle refresh re-downloaded $idle rows (expected at most ${case.scopes} boundary row per scope)")
        }

        // 3. One insert + one update + one soft delete per table, then refresh.
        for (case in cases) { sql(case.insert); Thread.sleep(8); sql(case.update); Thread.sleep(8); sql(case.delete); Thread.sleep(8) }
        call("POST", "/__log/clear")
        val second = bootstrapper.refreshAllReport()
        val deltaFetch = fetchedByTable()
        if (!second.successful) fail("delta refresh reported failures: ${second.message}")
        for (case in cases) {
            val got = runCatching { scalar("select count(*) from ${case.local} where ${case.countWhere}") }.getOrElse { -1 }
            val marked = runCatching { scalar(case.probe) }.getOrElse { -1 }
            val fetched = deltaFetch[case.remote]?.first ?: 0
            cell(case, "delta fetch", fmt(deltaFetch[case.remote])); cell(case, "cached after", got); cell(case, "update seen", if (marked >= 1) "yes" else "NO")
            if (got != case.expectSeed) fail("${case.remote}: after insert+delete the cache holds $got rows, expected ${case.expectSeed}")
            if (marked < 1) fail("${case.remote}: the server-side update did not reach ${case.local} (probe: ${case.probe})")
            // 3 changed rows per scope (the boundary row is one of them or a 4th).
            if (fetched > 4 * case.scopes) fail("${case.remote}: delta refresh fetched $fetched rows for 3 changes (${case.scopes} scope(s))")
        }

        // 4. Idle again: the checkpoint moved past the changes.
        bootstrapper.refreshAllReport()
        val idle2 = fetchedByTable()
        for (case in cases) {
            val f = idle2[case.remote]?.first ?: 0
            cell(case, "idle after", fmt(idle2[case.remote]))
            if (f > case.scopes) fail("${case.remote}: second idle refresh still fetched $f rows")
        }

        // 5. A soft-deleted row that is restored on the server comes back.
        val deptsBefore = activeCount("departments")
        sql("update departments set is_deleted=false, deleted_at=null where dept_id='vic'")
        bootstrapper.refreshAllReport(); fetchedByTable()
        val deptsAfter = activeCount("departments")
        println("restore: departments $deptsBefore -> $deptsAfter")
        if (deptsAfter != deptsBefore + 1) fail("departments: a restored (un-deleted) row did not come back ($deptsBefore -> $deptsAfter)")

        // 6. One table failing must not stop the others, and must not lose its changes once it recovers.
        sql("insert into rooms(room_id,building_id,room_no,name) values ('r4','b1','R17','Room 17')")
        sql("insert into buildings(building_id,name,code) values ('b3','Third','TB')")
        val roomsBefore = activeCount("rooms"); val buildingsBefore = activeCount("buildings")
        call("POST", "/__fail", """{"tables":["rooms"]}""")
        val broken = bootstrapper.refreshAllReport(); fetchedByTable()
        println("failing rooms -> successful=${broken.successful} message=${broken.message}")
        if (broken.successful || broken.message?.contains("room", ignoreCase = true) != true) fail("a failing table was not reported by name: ${broken.message}")
        if (activeCount("buildings") != buildingsBefore + 1) fail("buildings did not sync while rooms was failing")
        if (activeCount("rooms") != roomsBefore) fail("rooms changed although its request failed")
        call("POST", "/__fail", """{"tables":[]}""")
        val healed = bootstrapper.refreshAllReport(); fetchedByTable()
        println("rooms recovered -> successful=${healed.successful}, rooms ${roomsBefore} -> ${activeCount("rooms")}")
        if (!healed.successful) fail("refresh still failing after the table recovered: ${healed.message}")
        if (activeCount("rooms") != roomsBefore + 1) fail("rooms lost the change made while it was failing")

        // 7. Information only: a row physically removed on the server (no is_deleted tombstone).
        val finesBefore = activeCount("fines")
        sql("delete from fines where roll_number='R-04'")
        bootstrapper.refreshAllReport(); fetchedByTable()
        val finesAfter = activeCount("fines")
        report7 = "hard-deleted fine: local fines $finesBefore -> $finesAfter (${if (finesAfter == finesBefore) "NOT removed: a delta sync cannot see a physical delete" else "removed"})"
        println(report7)

        // 8. archive-and-delete-session: the server hard-deletes the session (cascade) and leaves a soft-deleted tombstone row.
        val sessionsBefore = activeCount("academic_sessions")
        val urStudents = scalar("select count(*) from session_students where sessionId = 'ur_2023'")
        val urPeriods = scalar("select count(*) from session_periods where sessionId = 'ur_2023'")
        sql("""do $$ begin
            create temp table archived as select * from academic_sessions where session_id = 'ur_2023';
            delete from academic_sessions where session_id = 'ur_2023';
            update archived set is_active = false, is_deleted = true, deleted_at = now();
            insert into academic_sessions select * from archived;
            drop table archived;
        end $$""")
        val archiveReport = bootstrapper.refreshAllReport(); fetchedByTable()
        println("archive refresh: successful=${archiveReport.successful} ${archiveReport.message}; ur session rows locally: ${scalar("select count(*) from academic_sessions where sessionId = 'ur_2023'")}")
        val leftStudents = scalar("select count(*) from session_students where sessionId = 'ur_2023'")
        val leftPeriods = scalar("select count(*) from session_periods where sessionId = 'ur_2023'")
        val leftover = leftStudents + leftPeriods
        println("archived session leftovers: students=$leftStudents periods=$leftPeriods")
        println("archived session: sessions $sessionsBefore -> ${activeCount("academic_sessions")}; its cached students/periods $urStudents/$urPeriods -> $leftover left")
        if (activeCount("academic_sessions") != sessionsBefore - 1) fail("an archived session (tombstone) was not removed from the cache")
        if (leftover != 0) fail("an archived session's cached students/periods were not cleared ($leftover left)")

        val header = listOf("seed", "1st fetch", "cached", "idle fetch", "delta fetch", "cached after", "update seen", "idle after")
        val report = StringBuilder("\n=== ADMIN SYNC AGAINST LOCAL SUPABASE ===\n")
        report.appendLine("table".padEnd(26) + header.joinToString("") { it.padEnd(13) })
        for ((table, cols) in rows) report.appendLine(table.padEnd(26) + header.joinToString("") { (cols[it] ?: "-").padEnd(13) })
        report.appendLine("\n$report7")
        report.appendLine("failures: ${failures.size}")
        failures.forEach { report.appendLine(" - $it") }
        println(report)
        File(System.getProperty("java.io.tmpdir"), "cms-admin-sync-report.txt").writeText(report.toString())
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
