package com.mbd.cmscommon.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class EdgeFunctionErrorsTest {

    private fun body(message: String, code: String) = """{"error":"$message","code":"$code"}"""

    @Test
    fun aSessionStillActiveConflictIsShownVerbatim() {
        val e = EdgeFunctionErrors.parse(409, body("This session is still active. Deactivate it before archiving and deleting it.", "SESSION_ACTIVE"))
        assertTrue(e is CmsException.Conflict)
        val classified = ErrorClassifier.classify(e)
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals("This session is still active. Deactivate it before archiving and deleting it.", classified.userMessage)
    }

    @Test
    fun serverCodesChooseTheKindEvenWhenTheStatusIsGeneric() {
        assertTrue(EdgeFunctionErrors.parse(409, body("An account with this email already exists.", "EMAIL_EXISTS")) is CmsException.Conflict)
        assertTrue(EdgeFunctionErrors.parse(400, body("Choose a stronger password (at least 6 characters).", "WEAK_PASSWORD")) is CmsException.Validation)
        assertTrue(EdgeFunctionErrors.parse(403, body("Only an admin can do this.", "FORBIDDEN")) is CmsException.Permission)
        assertTrue(EdgeFunctionErrors.parse(401, body("Your session has expired. Sign in again.", "UNAUTHORIZED")) is CmsException.Auth)
        assertTrue(EdgeFunctionErrors.parse(404, body("That session no longer exists. Refresh the list.", "NOT_FOUND")) is CmsException.NotFound)
        assertTrue(EdgeFunctionErrors.parse(429, body("Too many attempts. Wait a minute and try again.", "RATE_LIMIT")) is CmsException.Network)
    }

    @Test
    fun aServerSideFailureKeepsItsExplanationAndGetsAReferenceCode() {
        val e = EdgeFunctionErrors.parse(500, body("Couldn't back up the session's attendance records, so nothing was deleted. Try again.", "ARCHIVE_EXPORT_FAILED"))
        assertTrue(e is CmsException.Unexpected)
        val classified = ErrorClassifier.classify(e)
        assertEquals(Severity.CRITICAL, classified.severity)
        assertTrue(classified.userMessage.startsWith("Couldn't back up the session's attendance records, so nothing was deleted. Try again. (Ref "))
    }

    @Test
    fun withoutACodeTheStatusDecidesTheKind() {
        assertTrue(EdgeFunctionErrors.parse(409, """{"error":"Something conflicts."}""") is CmsException.Conflict)
        assertTrue(EdgeFunctionErrors.parse(503, """{"error":"Down for maintenance."}""") is CmsException.Unexpected)
    }

    @Test
    fun gatewayAndPlainTextBodiesAreNeverShownVerbatim() {
        val jwt = EdgeFunctionErrors.parse(401, """{"code":401,"message":"Invalid JWT"}""")
        assertTrue(jwt is CmsException.Auth)
        assertEquals("Your session has expired. Sign in again.", jwt.message)

        val html = EdgeFunctionErrors.parse(502, "<html><body>Bad gateway</body></html>")
        assertEquals("The server hit a problem. Try again in a moment.", html.message)

        val plain = EdgeFunctionErrors.parse(404, "Requested function was not found")
        assertTrue(plain is CmsException.NotFound)
        assertEquals("The item you're working on no longer exists. Refresh and try again.", plain.message)
    }

    @Test
    fun aBodyThatLeaksInternalsFallsBackToTheStatusSentence() {
        val e = EdgeFunctionErrors.parse(500, """{"error":"duplicate key at https://x.supabase.co/rest/v1/teachers","code":"INTERNAL"}""")
        assertEquals("The server hit a problem. Try again in a moment.", e.message)
        assertFalse(ErrorClassifier.classify(e).userMessage.contains("supabase"))
    }

    @Test
    fun anEmptyOrMissingBodyStillYieldsAStatusSentence() {
        assertEquals("You don't have permission to do this.", EdgeFunctionErrors.parse(403, null).message)
        assertEquals("You don't have permission to do this.", EdgeFunctionErrors.parse(403, "").message)
        assertEquals("Too many attempts. Wait a minute and try again.", EdgeFunctionErrors.parse(429, "").message)
    }

    @Test
    fun theOriginalExceptionIsKeptAsTheCause() {
        val original = RuntimeException("http failure")
        val e = EdgeFunctionErrors.parse(409, body("x", "CONFLICT"), original)
        assertSame(original, e.cause)
    }

    @Test
    fun nonHttpFailuresPassThroughTranslateUnchanged() = runBlocking {
        val offline = java.io.IOException("Unable to resolve host")
        val thrown = runCatching { EdgeFunctionErrors.translate<Unit> { throw offline } }.exceptionOrNull()
        assertSame(offline, thrown)
        assertEquals("ok", EdgeFunctionErrors.translate { "ok" })
    }
}
