package com.mbd.cmsdesktop.sync

import com.mbd.cmsdesktop.di.DesktopAppComponent
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Does a desktop login survive closing and reopening the app? Each "launch" is a brand-new Dagger graph, Supabase
 * client and Room connection over the same database file (the same thing a restart does), against the local shim's
 * minimal sign-in endpoints.
 */
class LocalSupabaseSessionPersistenceTest : LocalSyncSupport() {

    private fun launch(appId: String): DesktopAppComponent {
        System.setProperty("cms.desktop.appId", appId)
        System.setProperty("cms.supabase.url", shim)
        System.setProperty("cms.supabase.anonKey", "anon")
        dbFile = File(System.getenv("APPDATA") ?: System.getProperty("user.home"), "CMSDesktop/$appId/cms.db")
        return DesktopAppComponent.create()
    }

    private suspend fun signedInEmail(c: DesktopAppComponent): String? =
        withTimeoutOrNull(10_000) { c.sessionManager().awaitInitialization() }

    private fun prepare() {
        sql(truncateAll)
        sql("insert into auth.users(id,email) values (gen_random_uuid(),'t1@x.pk')")
    }

    @Test
    fun loginSurvivesReopeningTheApp() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        prepare()
        call("POST", "/__authttl", """{"seconds":3600}""")
        val appId = "persist-${System.nanoTime()}"

        val first = launch(appId)
        first.sessionManager().signIn("t1@x.pk", "pw")
        delay(1500) // let the session be written
        println("stored sessions after sign-in: ${scalar("select count(*) from desktop_auth_session")}")

        val second = launch(appId)
        val email = signedInEmail(second)
        println("after reopen: signed in as $email; stored sessions ${scalar("select count(*) from desktop_auth_session")}")
        assertEquals("the login was lost on reopening the app", "t1@x.pk", email)
    }

    @Test
    fun loginSurvivesReopeningAfterTheAccessTokenExpired() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        prepare()
        call("POST", "/__authttl", """{"seconds":2}""")
        val appId = "persist-exp-${System.nanoTime()}"
        val first = launch(appId)
        first.sessionManager().signIn("t1@x.pk", "pw")
        delay(1000)
        call("POST", "/__authttl", """{"seconds":3600}""")
        delay(3500) // the stored access token is now expired; only the refresh token can restore the login

        val second = launch(appId)
        val email = signedInEmail(second)
        println("after reopen with an expired access token: signed in as $email")
        assertEquals("an expired access token logged the user out instead of being refreshed", "t1@x.pk", email)
    }

    @Test
    fun loginSurvivesReopeningWhileOffline() = runBlocking {
        assumeTrue("local PostgREST shim is not running on $shim", shimUp())
        prepare()
        call("POST", "/__authttl", """{"seconds":2}""")
        val appId = "persist-off-${System.nanoTime()}"
        val first = launch(appId)
        first.sessionManager().signIn("t1@x.pk", "pw")
        delay(1000)
        call("POST", "/__authttl", """{"seconds":3600}""")
        delay(3500)
        call("POST", "/__fail", """{"tables":["auth"]}""") // refresh cannot reach the server

        val second = launch(appId)
        val email = signedInEmail(second)
        println("after reopen, expired token and no connection: signed in as $email")
        call("POST", "/__fail", """{"tables":[]}""")
        assertEquals("being offline at startup logged the user out", "t1@x.pk", email)
    }
}
