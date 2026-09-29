package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import java.lang.reflect.Proxy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** A hub that loads several summaries at once must say WHICH ones failed and why, not the first failure's text. */
class HubFailureMessageTest {

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    /** Cache reads return empty flows/lists, `sync*` calls go through [onSync], everything else fails the test. */
    private inline fun <reified T : Any> repo(crossinline onSync: (String) -> Unit = {}): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, _ ->
            when {
                method.name.startsWith("observe") -> flowOf(if (method.name == "observeSession") null else emptyList<Any>())
                method.name.startsWith("sync") -> onSync(method.name)
                method.name == "getSemesterGpa" -> emptyList<Any>()
                else -> error("unexpected call ${method.name}")
            }
        } as T

    private fun hub(marksSync: (String) -> Unit, datesheetSync: (String) -> Unit) = StudentExamsHubController(
        sessionId = "isl_2023",
        rollNumber = "IT-22-01",
        marksRepository = repo<SessionMarksRepository>(marksSync),
        datesheetRepository = repo<DatesheetRepository>(datesheetSync),
        sessionRepository = repo<AcademicSessionRepository>(),
        scope = scope,
    )

    @Test
    fun aHubThatLoadedEverythingShowsNoError() {
        val hub = hub({}, {})
        hub.refresh()
        assertNull(hub.loadError.value)
    }

    @Test
    fun aHubNamesTheSummaryThatFailed() {
        val hub = hub(marksSync = {}, datesheetSync = { throw RuntimeException("Unable to resolve host \"example.supabase.co\": No address associated with hostname") })
        hub.refresh()
        assertEquals("Couldn't load datesheets (no connection).", hub.loadError.value)
    }

    @Test
    fun aHubNamesEverySummaryThatFailedWhenTheyShareACause() {
        val offline = { _: String -> throw RuntimeException("Unable to resolve host \"example.supabase.co\": No address associated with hostname") }
        val hub = hub(marksSync = offline, datesheetSync = offline)
        hub.refresh()
        assertEquals("Couldn't load marks and datesheets (no connection).", hub.loadError.value)
    }
}
