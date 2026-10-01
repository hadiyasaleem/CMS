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
class LocalSupabaseAdminSyncTest : LocalSyncSupport() {


    @Test
    fun bulkImportIsPagedAndCompletelyFetched() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        call("POST", "/__fail", """{"tables":[]}""")
        sql(truncateAll)
        sql("insert into departments(dept_id,name,code) values ('ch','Chemistry','CH')")
        sql("insert into academic_sessions(session_id,dept_id,start_year,end_year,max_students,current_semester,shift_mode,program_type) values ('ch_2023','ch',2023,2027,50,3,'MORNING','BS')")
        sql("insert into session_students(session_id,roll_number,name,shift) values ('ch_2023','R-01','Ali','MORNING')")
        // One statement = one transaction = 1,200 rows sharing the same updated_at (a typical bulk import).
        sql("insert into calendar_events(title,event_type,start_date) select 'bulk-'||g,'EVENT',date '2026-10-01' from generate_series(1,1200) g")
        call("POST", "/__log/clear")
        val app = startApp()
        val notes = mutableListOf<String>()
        val problems = mutableListOf<String>()

        app.refreshAllReport()
        val first = fetchedByTable()["calendar_events"]?.first ?: 0
        val cached = activeCount("calendar_events")
        val distinct = scalar("select count(distinct eventId) from calendar_events")
        notes += "first refresh: fetched $first calendar event rows across pages (server 1200) -> cached $cached ($distinct distinct)"
        if (cached != 1200 || distinct != 1200) problems += "bulk import: cached $cached / $distinct distinct of 1200 calendar event rows (page boundaries lost or duplicated rows)"

        app.refreshAllReport()
        val idle = fetchedByTable()["calendar_events"]?.first ?: 0
        notes += "idle refresh right after a bulk import: fetched $idle rows (all 1200 share one updated_at)"

        sql("insert into calendar_events(title,event_type,start_date) values ('new','EVENT','2026-10-02')")
        app.refreshAllReport()
        val afterNew = fetchedByTable()["calendar_events"]?.first ?: 0
        notes += "after one new event: fetched $afterNew row(s) -> cached ${activeCount("calendar_events")}"
        if (activeCount("calendar_events") != 1201) problems += "bulk import: the new event did not arrive"
        if (afterNew != 1) problems += "bulk import: one new event caused $afterNew rows to be fetched"

        app.refreshAllReport()
        val settled = fetchedByTable()["calendar_events"]?.first ?: 0
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
        for (case in syncCases) for (s in case.seed) { sql(s); Thread.sleep(8) }
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
        for (case in syncCases) {
            val got = runCatching { scalar("select count(*) from ${case.local} where ${case.countWhere}") }.getOrElse { -1 }
            cell(case, "seed", case.expectSeed); cell(case, "1st fetch", fmt(firstFetch[case.remote])); cell(case, "cached", got)
            if (got != case.expectSeed) fail("${case.remote}: first refresh cached $got rows in ${case.local}, expected ${case.expectSeed}")
        }

        // 2. Idle refresh: nothing changed, so at most the checkpoint boundary rows come back.
        bootstrapper.refreshAllReport()
        val idleFetch = fetchedByTable()
        for (case in syncCases) {
            val idle = idleFetch[case.remote]?.first ?: 0
            cell(case, "idle fetch", fmt(idleFetch[case.remote]))
            if (idle > case.scopes) fail("${case.remote}: idle refresh re-downloaded $idle rows (expected at most ${case.scopes} boundary row per scope)")
        }

        // 3. One insert + one update + one soft delete per table, then refresh.
        for (case in syncCases) { sql(case.insert); Thread.sleep(8); sql(case.update); Thread.sleep(8); sql(case.delete); Thread.sleep(8) }
        call("POST", "/__log/clear")
        val second = bootstrapper.refreshAllReport()
        val deltaFetch = fetchedByTable()
        if (!second.successful) fail("delta refresh reported failures: ${second.message}")
        for (case in syncCases) {
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
        for (case in syncCases) {
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
        val eventsBefore = activeCount("calendar_events")
        sql("delete from calendar_events where title='Convocation'")
        bootstrapper.refreshAllReport(); fetchedByTable()
        val eventsAfter = activeCount("calendar_events")
        report7 = "hard-deleted event: local calendar events $eventsBefore -> $eventsAfter (${if (eventsAfter == eventsBefore) "NOT removed: a delta sync cannot see a physical delete" else "removed"})"
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
