package com.mbd.cmsdesktop.sync

import com.mbd.cmsdesktop.di.DesktopAppComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/** One app launch in its own JVM (started by [LocalSupabaseRealRestartTest]): mode "login" signs in and exits, "reopen" restores. */
object SessionProbeMain {
    @JvmStatic
    fun main(args: Array<String>) = runBlocking {
        val (appId, mode, url) = args
        System.setProperty("cms.desktop.appId", appId)
        System.setProperty("cms.supabase.url", url)
        System.setProperty("cms.supabase.anonKey", "anon")
        val component = DesktopAppComponent.create()
        when (mode) {
            "login" -> {
                component.sessionManager().signIn("t1@x.pk", "pw")
                val role = component.userRepository().resolveRole("t1@x.pk")
                println("PROBE login ok role=${role::class.simpleName}")
                delay(1500)
            }
            "reopen" -> {
                val key = withTimeoutOrNull(10_000) { component.sessionManager().awaitInitialization() }
                println("PROBE reopen accountKey=$key")
                val cached = runCatching { key?.let { component.userRepository().getCachedRole(it) } }
                println("PROBE reopen cachedRole=${cached.getOrNull()?.let { it::class.simpleName }} error=${cached.exceptionOrNull()?.message}")
            }
        }
        // Like closing the window: the process just ends.
        System.exit(0)
    }
}
