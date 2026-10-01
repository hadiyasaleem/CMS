package com.mbd.cmsdesktop.sync

import java.io.File
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The teacher and student apps run the same refresh as the admin app, but the server (row-level security) only
 * returns what that person may read. This drives the real refresh as each kind of user against the local Postgres
 * (postgrest-shim.mjs, with the real policies applied) and checks that the cache holds exactly what the person is
 * allowed to read -- nothing extra (privacy) and nothing missing -- and that delta/idle behave.
 */
class LocalSupabaseRoleSyncTest : LocalSyncSupport() {

    private class Who(val label: String, val email: String, val role: String, val linkedSession: String? = null, val linkedRoll: String? = null) {
        var sub: String = ""
    }

    private val people = listOf(
        Who("teacher T1 (ch_2023 + merged ur_2023)", "t1@x.pk", "TEACHER"),
        Who("teacher T2 (ur_2023 only)", "t2@x.pk", "TEACHER"),
        Who("teacher T3 (no classes)", "nobody@x.pk", "TEACHER"),
        Who("student S1 (ch_2023 R-01)", "s1@x.pk", "STUDENT", "ch_2023", "R-01"),
        Who("student S2 (ur_2023 R-01)", "s2@x.pk", "STUDENT", "ur_2023", "R-01"),
    )

    private fun seedWorld() {
        call("POST", "/__fail", """{"tables":[]}""")
        signInAs(null)
        sql(truncateAll)
        // Supabase grants table access to signed-in users by default; the bare local database does not.
        sql("grant select, insert, update, delete on all tables in schema public to authenticated")
        for (case in syncCases) for (s in case.seed) { sql(s); Thread.sleep(5) }
        listOf(
            "insert into teachers(email,name,dept_id) values ('nobody@x.pk','No Classes','ch')",
            "insert into session_students(session_id,roll_number,name,shift) values ('ur_2023','R-02','Zoya','MORNING')",
            "insert into session_students(session_id,roll_number,name,shift) values ('ch_2024','R-01','Hina','MORNING')",
            "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('ur_2023','THURSDAY','09:00','10:00','UR-201','Poetry','t2@x.pk','R2','MORNING')",
            "insert into session_marks(session_id,semester,course_code,exam_type,roll_number,score,max_marks) values ('ur_2023',3,'UR-201','MIDTERM','R-01',9,25)",
            "insert into session_attendance(session_id,semester,course_code,date,roll_number,status) values ('ur_2023',3,'UR-201','2026-09-01','R-01','PRESENT')",
            "insert into student_link_requests(requested_by_email,roll_number_claimed,session_id) values ('s2@x.pk','R-01','ur_2023')",
        ).forEach { sql(it); Thread.sleep(5) }
        for (p in people) {
            sql("do $$ begin insert into auth.users(id,email) values (gen_random_uuid(),'${p.email}'); update profiles set role='${p.role}' where email='${p.email}'; end $$")
            if (p.role == "TEACHER") sql("update profiles set teacher_email='${p.email}' where email='${p.email}'")
            if (p.linkedSession != null) sql("update profiles set linked_session_id='${p.linkedSession}', linked_roll='${p.linkedRoll}' where email='${p.email}'")
            p.sub = rows("select id::text as id from auth.users where email='${p.email}'").single()["id"]!!.jsonPrimitive.content
        }
        // The seeded notifications name no session; add one aimed at each session so scoping is exercised.
        sql("insert into notifications(title,body,target_role,target_session_id,created_by_email) values ('For ch','x','STUDENT','ch_2023','a1@x.pk')")
        sql("insert into notifications(title,body,target_role,target_session_id,created_by_email) values ('For ur','x','STUDENT','ur_2023','a1@x.pk')")
    }

    private fun oracle(case: Case, who: Who): Int {
        val query = when (case.remote) {
            "profiles" -> "select count(*)::int as c from profiles where role='ADMIN' and not is_deleted"
            else -> "select count(*)::int as c from ${case.remote} where not is_deleted"
        }
        return rows(query, who.email, who.sub).single()["c"]!!.jsonPrimitive.content.toInt()
    }

    private fun localCount(case: Case): Int = runCatching { scalar("select count(*) from ${case.local} where ${case.countWhere}") }.getOrElse { -1 }

    @Test
    fun eachRoleSyncsExactlyWhatItMayRead() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        seedWorld()
        val failures = mutableListOf<String>()
        val matrix = StringBuilder()
        fun fail(m: String) { failures += m; println("FAIL: $m") }

        for (who in people) {
            signInAs(who.email, who.sub)
            val app = startApp()
            call("POST", "/__log/clear")
            val report = app.refreshAllReport()
            fetchedByTable()
            matrix.appendLine("\n--- ${who.label}: refresh ${if (report.successful) "ok" else "FAILED: ${report.message}"}")
            if (!report.successful) fail("${who.label}: refresh failed: ${report.message}")

            // 1. The cache holds exactly what row-level security lets this person read.
            val line = StringBuilder()
            for (case in syncCases) {
                val expected = oracle(case, who)
                val got = localCount(case)
                line.append("${case.remote}=$got/$expected ")
                if (got != expected) fail("${who.label}: ${case.remote} cached $got rows but the server allows $expected")
            }
            matrix.appendLine(line)

            // 2. An idle refresh downloads nothing.
            app.refreshAllReport()
            val idle = fetchedByTable().filterValues { it.first > 0 }
            if (idle.isNotEmpty()) fail("${who.label}: idle refresh still fetched $idle")
            signInAs(null)
        }

        // 3. Explicit privacy checks (independent of the oracle).
        val s1 = people[3]; val s2 = people[4]; val t1 = people[0]; val t2 = people[1]; val t3 = people[2]
        suspend fun cachedFor(who: Who, block: () -> Unit) {
            signInAs(who.email, who.sub)
            startApp().refreshAllReport(); fetchedByTable()
            block()
            signInAs(null)
        }
        cachedFor(s1) {
            val students = scalar("select count(*) from session_students")
            val otherMarks = scalar("select count(*) from session_marks where not (sessionId = 'ch_2023' and rollNumber = 'R-01')")
            val otherAtt = scalar("select count(*) from session_attendance_rows where not (sessionId = 'ch_2023' and rollNumber = 'R-01')")
            println("S1 cache: students=$students otherMarks=$otherMarks otherAttendance=$otherAtt")
            if (students != 1) fail("student S1 caches $students student rows (only their own is allowed)")
            if (otherMarks + otherAtt != 0) fail("student S1 cached someone else's marks/attendance ($otherMarks/$otherAtt)")
        }
        cachedFor(s2) {
            if (scalar("select count(*) from session_students where not (sessionId = 'ur_2023' and rollNumber = 'R-01')") != 0) fail("student S2 cached another student's record")
            if (scalar("select count(*) from student_link_requests where requestedByUid <> 's2@x.pk'") != 0) fail("student S2 cached someone else's link request")
        }
        cachedFor(t3) {
            val n = scalar("select count(*) from session_students") + scalar("select count(*) from session_marks") + scalar("select count(*) from session_attendance_rows")
            if (n != 0) fail("a teacher with no classes cached $n student-level rows")
        }
        cachedFor(t2) {
            if (scalar("select count(*) from session_students where sessionId <> 'ur_2023'") != 0) fail("teacher T2 cached students outside ur_2023")
        }
        cachedFor(t1) {
            val own = scalar("select count(*) from session_students where sessionId = 'ch_2023'")
            val merged = scalar("select count(*) from session_students where sessionId = 'ur_2023'")
            val outside = scalar("select count(*) from session_students where sessionId = 'ch_2024'")
            println("T1 cache: ch_2023 students=$own, ur_2023 (merged) students=$merged, ch_2024 students=$outside")
            if (merged == 0) fail("teacher T1 does not receive the merged session's students")
            if (outside != 0) fail("teacher T1 cached a session they do not teach")
        }

        // 4. Delta as a teacher: a change they may see arrives; a change they may not see costs nothing.
        signInAs(t1.email, t1.sub)
        val app = startApp(); app.refreshAllReport(); fetchedByTable()
        sql("update session_students set name='UPD' where session_id='ch_2023' and roll_number='R-01'")
        Thread.sleep(8)
        sql("update session_students set name='HIDDEN' where session_id='ch_2024' and roll_number='R-01'")
        app.refreshAllReport()
        val delta = fetchedByTable()["session_students"]?.first ?: 0
        val seen = scalar("select count(*) from session_students where name = 'UPD'")
        val leaked = scalar("select count(*) from session_students where name = 'HIDDEN'")
        println("T1 delta: fetched $delta student row(s); visible update cached=$seen; hidden update cached=$leaked")
        if (seen != 1) fail("teacher T1 did not receive an update to a student they teach")
        if (leaked != 0) fail("teacher T1 received an update to a student they do not teach")
        if (delta != 1) fail("teacher T1's delta fetched $delta student rows for 1 visible change")

        // 5. Information only: what happens on this device when the teacher loses a class.
        val before = scalar("select count(*) from session_students where sessionId = 'ch_2023'")
        sql("update timetable_periods set teacher_email='t2@x.pk' where primary_session_id='ch_2023'")
        app.refreshAllReport(); fetchedByTable()
        val after = scalar("select count(*) from session_students where sessionId = 'ch_2023'")
        val revoke = "teacher loses their ch_2023 classes: cached ch_2023 students $before -> $after (${if (after == before) "STALE: the device keeps the rows it could read before" else "cleared"})"
        println(revoke)
        signInAs(null)

        val report = "=== ROLE SYNC AGAINST LOCAL SUPABASE (cached/allowed by row-level security) ===$matrix\n\n$revoke\nfailures: ${failures.size}\n" + failures.joinToString("\n") { " - $it" }
        println(report)
        File(System.getProperty("java.io.tmpdir"), "cms-role-sync-report.txt").writeText(report)
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}
