package com.mbd.cmsdesktop.sync

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.repository.SessionTimetableRepositoryImpl
import com.mbd.cmscommon.data.sync.RoomSyncCheckpointStore
import com.mbd.cmsdesktop.data.local.DesktopDatabase
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNamingStrategy
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Runs the app's REAL timetable sync code (SessionTimetableRepositoryImpl + Room + the Supabase Postgrest client)
 * against the local scratch Postgres, through supabase/tests/local/postgrest-shim.mjs. Skipped unless that shim
 * is running (node supabase/tests/local/postgrest-shim.mjs, with the embedded Postgres up and migrations applied).
 *
 * It answers: does a refresh fetch everything, or only new/updated rows?
 */
class LocalSupabaseSyncTest {

    private val shim = "http://localhost:54330"
    private val http = HttpClient.newHttpClient()

    private fun call(method: String, path: String, body: String? = null): String {
        val b = HttpRequest.newBuilder(URI.create(shim + path)).header("content-type", "application/json")
        val req = if (body != null) b.method(method, HttpRequest.BodyPublishers.ofString(body)).build() else b.method(method, HttpRequest.BodyPublishers.noBody()).build()
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body()
    }

    private fun sql(statement: String) {
        val out = call("POST", "/__sql", buildJsonObject { put("sql", statement) }.toString())
        check(!out.contains("\"message\"")) { "SQL failed: $out" }
    }

    private fun clearLog() = call("POST", "/__log/clear")

    private fun periodFetches(): List<JsonObject> =
        Json.parseToJsonElement(call("GET", "/__log")).jsonArray.map { it.jsonObject }
            .filter { it["method"]?.jsonPrimitive?.content == "GET" && it["table"]?.jsonPrimitive?.content == "timetable_periods" }

    /** Rows the periods query returned across every page of the last sync(s), then reset the log. */
    private fun rowsFetched(): Int = periodFetches().sumOf { it["returned"]!!.jsonPrimitive.int }.also { clearLog() }

    private fun shimUp(): Boolean = runCatching { call("GET", "/__log"); true }.getOrDefault(false)

    @OptIn(ExperimentalSerializationApi::class)
    @Test
    fun refreshFetchesOnlyNewAndUpdatedRows() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())

        // ---- fresh scratch data, each statement its own transaction so every row gets a distinct updated_at
        sql("truncate period_sessions, timetable_periods, session_students, academic_sessions, teachers, departments cascade")
        sql("insert into departments(dept_id,name,code) values ('ch','Chemistry','CH'),('ur','Urdu','UR')")
        sql("insert into teachers(email,name,dept_id) values ('t1@x.pk','Teacher One','ch'),('t2@x.pk','Teacher Two','ur'),('t3@x.pk','Teacher Three','ch')")
        sql("insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ch_2023','ch',2023,2027,50,3,'MORNING','BS'),('ur_2023','ur',2023,2027,50,3,'MORNING','BS')")
        fun period(session: String, day: String, teacher: String, room: String, course: String) =
            "insert into timetable_periods(primary_session_id,day,start_time,end_time,course_code,subject_name,teacher_email,room_no,shift) values ('$session','$day','09:00','10:00','$course','Subject $course','$teacher','$room','MORNING')"
        sql(period("ch_2023", "MONDAY", "t1@x.pk", "R1", "GE-101"))
        Thread.sleep(30)
        sql(period("ch_2023", "TUESDAY", "t1@x.pk", "R1", "GE-102"))
        Thread.sleep(30)
        sql(period("ch_2023", "WEDNESDAY", "t1@x.pk", "R1", "GE-103"))
        Thread.sleep(30)
        sql(period("ur_2023", "THURSDAY", "t2@x.pk", "R2", "UR-201"))
        sql("insert into period_sessions(period_id,session_id) select id,'ur_2023' from timetable_periods where primary_session_id='ch_2023' and day='MONDAY'")

        // ---- the app side: real Room (in memory) + real Postgrest client + real repository
        val db = Room.inMemoryDatabaseBuilder<DesktopDatabase>().setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
        val client = createSupabaseClient(supabaseUrl = shim, supabaseKey = "anon") {
            defaultSerializer = KotlinXSerializer(Json { namingStrategy = JsonNamingStrategy.SnakeCase; ignoreUnknownKeys = true; encodeDefaults = false; explicitNulls = false })
            install(Auth)
            install(Postgrest)
        }
        val repo = SessionTimetableRepositoryImpl(client.postgrest, db.sessionPeriodDao(), db.academicSessionDao(), RoomSyncCheckpointStore(db.tableSyncStateDao()), SessionManager(client.pluginManager.getPlugin(Auth)))
        val report = StringBuilder("\n=== LOCAL SUPABASE SYNC REPORT ===\n")
        fun note(s: String) { report.appendLine(s); println(s) }

        // 1. First refresh: nothing cached, so it must pull everything for the session.
        clearLog()
        repo.syncSession("ch_2023")
        val first = rowsFetched()
        val week = repo.observeWeek("ch_2023").first()
        note("1. first refresh of ch_2023: fetched $first period rows (server has 3) -> cached ${week.size}")
        assertEquals(3, first)
        assertEquals(3, week.size)
        assertEquals(setOf("ur_2023"), week.single { it.day.name == "MONDAY" }.linkedSessionIds)

        // 2. Refresh again with nothing changed.
        repo.syncSession("ch_2023")
        val idle = rowsFetched()
        note("2. refresh with NO changes: fetched $idle row(s) of 3 (the checkpoint boundary row, not the whole table)")
        assertTrue("an idle refresh must not re-download everything", idle < 3)

        // 3. New + updated + deleted on the server, then refresh.
        sql(period("ch_2023", "FRIDAY", "t1@x.pk", "R1", "GE-104"))
        Thread.sleep(30)
        sql("update timetable_periods set room_no='R9', teacher_email='t3@x.pk' where primary_session_id='ch_2023' and day='TUESDAY'")
        Thread.sleep(30)
        sql("update timetable_periods set is_deleted=true where primary_session_id='ch_2023' and day='WEDNESDAY'")
        repo.syncSession("ch_2023")
        val delta = periodFetches().flatMap { it["ids"]!!.jsonArray }.size
        val fetchedDelta = rowsFetched().let { delta }
        val after = repo.observeWeek("ch_2023").first().associateBy { it.day.name }
        note("3. after 1 insert + 1 update + 1 delete: fetched $fetchedDelta row(s) -> cache now ${after.keys.sorted()}; Tuesday room=${after["TUESDAY"]?.roomNo}, teacher=${after["TUESDAY"]?.teacherId}")
        assertEquals(setOf("MONDAY", "TUESDAY", "FRIDAY"), after.keys)
        assertEquals("R9", after.getValue("TUESDAY").roomNo)
        assertEquals("t3@x.pk", after.getValue("TUESDAY").teacherId)
        assertTrue("delta refresh must not re-download the untouched Monday row", fetchedDelta <= 4)

        // 4. Merge link changes do not touch timetable_periods.updated_at -- confirm they still propagate.
        repo.syncSession("ur_2023")
        val guest = repo.observeWeek("ur_2023").first()
        note("4. ur_2023 grid after its sync: ${guest.map { "${it.day.name}${if (it.isOwnRow) "" else "(merged in)"}" }.sorted()}")
        assertEquals(setOf("THURSDAY", "MONDAY"), guest.map { it.day.name }.toSet())
        sql("update period_sessions set is_deleted=true where session_id='ur_2023'")
        repo.syncSession("ch_2023")
        repo.syncSession("ur_2023")
        val monday = repo.observeWeek("ch_2023").first().single { it.day.name == "MONDAY" }
        val guestAfter = repo.observeWeek("ur_2023").first()
        note("   after unlinking on the server (period rows unchanged): Monday chips=${monday.linkedSessionIds}, ur_2023 grid=${guestAfter.map { it.day.name }}")
        assertTrue("unlink must clear the owner's merge chip", monday.linkedSessionIds.isEmpty())
        assertEquals(listOf("THURSDAY"), guestAfter.map { it.day.name })
        sql("update period_sessions set is_deleted=false where session_id='ur_2023'")
        repo.syncSession("ur_2023")
        assertEquals(2, repo.observeWeek("ur_2023").first().size)
        note("   re-linking on the server brings the shadow row back after the next refresh")

        // 5. College-wide refresh (what the admin app uses): full first time, then only changes.
        clearLog()
        repo.syncAll()
        val allFirst = rowsFetched()
        repo.syncAll()
        val allIdle = rowsFetched()
        sql(period("ur_2023", "SATURDAY", "t2@x.pk", "R2", "UR-202"))
        repo.syncAll()
        val allDelta = rowsFetched()
        note("5. syncAll (admin): first=$allFirst rows, idle=$allIdle row(s), after 1 new period=$allDelta row(s)")
        assertTrue(allFirst >= 4)
        assertTrue(allIdle < allFirst)
        assertTrue(allDelta < allFirst)
        assertTrue(repo.observeAll().first().any { it.day.name == "SATURDAY" })

        java.io.File(System.getProperty("java.io.tmpdir"), "cms-local-sync-report.txt").writeText(report.toString())
        db.close()
    }
}
