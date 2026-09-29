package com.mbd.cmscommon.util

import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FailureSummaryTest {

    private val offline = UnknownHostException("no such host")

    @Test
    fun nothingFailedGivesNoMessage() {
        val failures = FailureSummary.of(listOf("fees" to Result.success(Unit), "marks" to Result.success(1)))
        assertTrue(failures.isEmpty())
        assertNull(FailureSummary.describe(failures))
    }

    @Test
    fun aCancellationIsNotAFailure() {
        val failures = FailureSummary.of(listOf("fees" to Result.failure<Unit>(CancellationException("left the screen"))))
        assertTrue(failures.isEmpty())
    }

    @Test
    fun oneFailureNamesWhatFailedAndWhy() {
        val message = FailureSummary.describe(FailureSummary.of(listOf("fees" to Result.failure<Unit>(offline), "marks" to Result.success(Unit))))
        assertEquals("Couldn't load fees (no connection).", message)
    }

    @Test
    fun failuresWithTheSameReasonShareOneSentence() {
        val message = FailureSummary.describe(
            FailureSummary.of(listOf("fees" to Result.failure<Unit>(offline), "marks" to Result.failure<Unit>(offline), "results" to Result.failure<Unit>(offline))),
        )
        assertEquals("Couldn't load fees, marks and results (no connection).", message)
    }

    @Test
    fun differentReasonsAreListedSeparately() {
        val message = FailureSummary.describe(
            FailureSummary.of(
                listOf(
                    "fees" to Result.failure<Unit>(offline),
                    "marks" to Result.failure<Unit>(CmsException.Permission()),
                    "results" to Result.failure<Unit>(offline),
                ),
            ),
        )
        assertEquals("Couldn't load fees and results (no connection); marks (no permission).", message)
    }

    @Test
    fun longListsAreCutOffWithACount() {
        val results = listOf("a", "b", "c", "d", "e", "f").map { it to Result.failure<Unit>(offline) }
        assertEquals("Couldn't load a, b, c, d and 2 more (no connection).", FailureSummary.describe(FailureSummary.of(results)))
    }

    @Test
    fun theSameNameIsNotRepeated() {
        val failures = listOf(LoadFailure("notifications", offline), LoadFailure("notifications", offline))
        assertEquals("Couldn't refresh notifications (no connection).", FailureSummary.describe(failures, prefix = "Couldn't refresh"))
    }

    @Test
    fun anUnexpectedFailureCarriesAReferenceCode() {
        val message = FailureSummary.describe(listOf(LoadFailure("marks", RuntimeException("boom \$\$ internal"))))!!
        assertTrue(message, Regex("""Couldn't load marks \(unexpected error, Ref [0-9A-Z]{4}\)\.""").matches(message))
    }

    @Test
    fun aTypedNotFoundReadsAsNotFound() {
        assertEquals("Couldn't load your profile (not found).", FailureSummary.describe(listOf(LoadFailure("your profile", CmsException.NotFound()))))
    }

    @Test
    fun aSyncReportIsSuccessfulOnlyWithoutFailures() {
        assertTrue(SyncReport(emptyList()).successful)
        assertNull(SyncReport(emptyList()).message)

        val report = SyncReport(listOf(LoadFailure("fees", offline), LoadFailure("marks", offline)))
        assertFalse(report.successful)
        assertEquals("Couldn't refresh fees and marks (no connection).", report.message)
    }
}
