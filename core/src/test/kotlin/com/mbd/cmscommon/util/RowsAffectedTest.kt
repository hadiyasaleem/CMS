package com.mbd.cmscommon.util

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class RowsAffectedTest {

    @Test
    fun countsRowsInAnArrayOrObject() {
        assertEquals(0, rowsIn(""))
        assertEquals(0, rowsIn("   "))
        assertEquals(0, rowsIn("[]"))
        assertEquals(0, rowsIn("null"))
        assertEquals(0, rowsIn("not json"))
        assertEquals(1, rowsIn("""[{"id":"a"}]"""))
        assertEquals(3, rowsIn("""[{"id":"a"},{"id":"b"},{"id":"c"}]"""))
        assertEquals(1, rowsIn("""{"id":"a"}"""))
    }

    @Test
    fun aWriteThatChangedARowPasses() = runBlocking {
        var cleaned = false
        requireRows("""[{"id":"a"}]""") { cleaned = true }
        assertTrue("the cleanup must only run when nothing matched", !cleaned)
    }

    @Test
    fun aWriteThatChangedNothingIsNotFoundAndCleansUpFirst() {
        var cleaned = false
        try {
            runBlocking { requireRows("[]") { cleaned = true } }
            fail("expected NotFound")
        } catch (e: CmsException.NotFound) {
            assertEquals(ROW_GONE_MESSAGE, e.message)
        }
        assertTrue("the stale local copy should be dropped", cleaned)
    }

    @Test
    fun theMessageCanBeSpecific() {
        try {
            runBlocking { requireRows("", "The marks record for this request no longer exists.") }
            fail("expected NotFound")
        } catch (e: CmsException.NotFound) {
            assertEquals("The marks record for this request no longer exists.", e.message)
        }
    }

    @Test
    fun theDefaultMessageIsPlainAndTellsTheUserWhatToDo() {
        val classified = ErrorClassifier.classify(CmsException.NotFound(ROW_GONE_MESSAGE), "Couldn't delete it.")
        assertEquals(ErrorKind.NOT_FOUND, classified.kind)
        assertEquals(ROW_GONE_MESSAGE, classified.userMessage)
    }
}
