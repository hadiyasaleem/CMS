package com.mbd.cmsdesktop.sync

import java.io.File
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** The same login/reopen check as [LocalSupabaseSessionPersistenceTest] but with a genuinely separate JVM per launch. */
class LocalSupabaseRealRestartTest : LocalSyncSupport() {

    private fun runProbe(appId: String, mode: String): String {
        val java = File(System.getProperty("java.home"), "bin/java").absolutePath
        val proc = ProcessBuilder(java, "-cp", System.getProperty("java.class.path"), "com.mbd.cmsdesktop.sync.SessionProbeMain", appId, mode, shim)
            .redirectErrorStream(true).start()
        val out = proc.inputStream.bufferedReader().readText()
        proc.waitFor(90, TimeUnit.SECONDS)
        return out.lines().filter { it.startsWith("PROBE") || it.contains("Exception") }.joinToString("\n")
    }

    @Test
    fun loginSurvivesARealRestart() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        sql(truncateAll)
        sql("insert into auth.users(id,email) values (gen_random_uuid(),'t1@x.pk')")
        sql("insert into teachers(email,name) values ('t1@x.pk','Teacher One')")
        sql("update profiles set role='TEACHER', teacher_email='t1@x.pk' where email='t1@x.pk'")
        call("POST", "/__authttl", """{"seconds":3600}""")
        val appId = "restart-${System.nanoTime()}"
        val first = runProbe(appId, "login")
        println("launch 1:\n$first")
        val second = runProbe(appId, "reopen")
        println("launch 2 (after closing and reopening):\n$second")
        assertTrue("login did not complete: $first", first.contains("login ok"))
        assertTrue("the login was lost after a restart: $second", second.contains("accountKey=t1@x.pk"))
        assertTrue("the cached role was lost after a restart: $second", second.contains("cachedRole=Teacher"))
    }

    @Test
    fun loginSurvivesTheLocalDatabaseBeingWiped() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        sql(truncateAll)
        sql("insert into auth.users(id,email) values (gen_random_uuid(),'t1@x.pk')")
        sql("insert into teachers(email,name) values ('t1@x.pk','Teacher One')")
        sql("update profiles set role='TEACHER', teacher_email='t1@x.pk' where email='t1@x.pk'")
        call("POST", "/__authttl", """{"seconds":3600}""")
        val appId = "wipe-${System.nanoTime()}"
        runProbe(appId, "login")
        // What a schema change does: the whole Room cache is dropped and rebuilt.
        val dir = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "CMSDesktop/$appId")
        dir.listFiles { f -> f.name.startsWith("cms.db") }?.forEach { it.delete() }
        val after = runProbe(appId, "reopen")
        println("after the database was wiped: $after")
        assertTrue("the login was lost when the local database was rebuilt: $after", after.contains("accountKey=t1@x.pk"))
    }
}
