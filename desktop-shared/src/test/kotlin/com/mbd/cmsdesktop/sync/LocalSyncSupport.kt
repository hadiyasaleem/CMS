package com.mbd.cmsdesktop.sync

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/** Shared plumbing for the tests that run the app's real sync against the local PostgREST shim. */
abstract class LocalSyncSupport {

    protected val shim = "http://localhost:54330"
    protected val http = HttpClient.newHttpClient()

    protected fun call(method: String, path: String, body: String? = null): String {
        val b = HttpRequest.newBuilder(URI.create(shim + path)).header("content-type", "application/json")
        val req = if (body != null) b.method(method, HttpRequest.BodyPublishers.ofString(body)).build() else b.method(method, HttpRequest.BodyPublishers.noBody()).build()
        return http.send(req, HttpResponse.BodyHandlers.ofString()).body()
    }

    protected fun sql(statement: String) {
        val out = call("POST", "/__sql", buildJsonObject { put("sql", statement) }.toString())
        check(!out.contains("\"message\"")) { "SQL failed: $statement -> $out" }
    }

    /** Rows of a query, optionally evaluated as a signed-in user (row-level security applies). */
    protected fun rows(statement: String, email: String? = null, sub: String? = null): List<kotlinx.serialization.json.JsonObject> {
        val body = buildJsonObject {
            put("sql", statement)
            if (email != null) put("as", buildJsonObject { put("email", email); put("sub", sub ?: "") })
        }
        val out = call("POST", "/__sql", body.toString())
        check(!out.startsWith("{")) { "SQL failed: $statement -> $out" }
        return Json.parseToJsonElement(out).jsonArray.map { it.jsonObject }
    }

    /** Every REST request from now on runs as this user (null = the trusted service role). */
    protected fun signInAs(email: String?, sub: String? = null) {
        call("POST", "/__as", if (email == null) "{}" else buildJsonObject { put("email", email); put("sub", sub ?: "") }.toString())
    }

    /** Every test starts as the trusted service role with no injected failures, whatever the previous test left behind. */
    @org.junit.Before
    fun resetShim() {
        if (shimUp()) {
            signInAs(null)
            call("POST", "/__fail", """{"tables":[]}""")
        }
    }

    protected fun shimUp() = runCatching { call("GET", "/__log"); true }.getOrDefault(false)

    /** table -> (rows from the checkpointed "updated_at >= since" queries, rows from other lookups such as merge links). */
    protected fun fetchedByTable(): Map<String, Pair<Int, Int>> {
        val log = Json.parseToJsonElement(call("GET", "/__log")).jsonArray.map { it.jsonObject }
        call("POST", "/__log/clear")
        return log.filter { it["method"]?.jsonPrimitive?.content == "GET" }
            .groupBy { it["table"]!!.jsonPrimitive.content }
            .mapValues { (_, v) ->
                val (inc, other) = v.partition { it["query"]!!.jsonPrimitive.content.contains("updated_at=gt") }
                inc.sumOf { it["returned"]!!.jsonPrimitive.int } to other.sumOf { it["returned"]!!.jsonPrimitive.int }
            }
    }

    protected fun fmt(p: Pair<Int, Int>?): String = if (p == null) "0" else if (p.second == 0) "${p.first}" else "${p.first}+${p.second}lk"

    protected lateinit var dbFile: File
    protected var report7 = ""

    protected val truncateAll = "truncate period_sessions, timetable_periods, session_attendance, session_marks, student_semester_gpa, session_fee_heads, session_fees, fines, exam_paper_submissions, datesheet_slots, datesheets, mark_edit_requests, student_link_requests, notifications, calendar_events, semester_terms, session_subjects, subject_pool, session_students, academic_sessions, teachers, rooms, buildings, departments, profiles, auth.users cascade"

    /** A brand-new app instance (own Room file, own Dagger graph) pointed at the local shim. */
    protected fun startApp(): com.mbd.cmscommon.data.sync.AdminDataBootstrapper {
        val appId = "synctest-${System.nanoTime()}"
        System.setProperty("cms.desktop.appId", appId)
        System.setProperty("cms.supabase.url", shim)
        System.setProperty("cms.supabase.anonKey", "anon")
        dbFile = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "CMSDesktop/$appId/cms.db")
        return DesktopAppComponent.create().adminDataBootstrapper()
    }

    protected fun <T> local(block: (SQLiteConnection) -> T): T {
        val c = BundledSQLiteDriver().open(dbFile.absolutePath)
        return try { block(c) } finally { c.close() }
    }

    protected fun scalar(query: String): Int = local { c ->
        c.prepare(query).use { st -> if (st.step()) st.getLong(0).toInt() else -1 }
    }

    protected fun activeCount(table: String): Int = runCatching { scalar("select count(*) from $table where isDeleted = 0") }.getOrElse { scalar("select count(*) from $table") }

}
