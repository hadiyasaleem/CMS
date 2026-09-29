package com.mbd.cmscommon.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ErrorClassifierTest {

    @Test
    fun surfacesTheDbTriggersRoomConflictMessageInsteadOfTheGenericFallback() {
        val error = RuntimeException("Room 201 is already booked overlapping 10:00:00 on MONDAY")
        val classified = ErrorClassifier.classify(error, fallback = "Could not refresh the master timetable.")
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals(Severity.EXPECTED, classified.severity)
        assertEquals("Room 201 is already booked overlapping 10:00:00 on MONDAY", classified.userMessage)
    }

    @Test
    fun surfacesTheDbTriggersTeacherConflictMessageInsteadOfTheGenericFallback() {
        val error = RuntimeException("Teacher jane@example.com already has an overlapping lecture on MONDAY at 10:00:00")
        val classified = ErrorClassifier.classify(error, fallback = "Could not refresh the master timetable.")
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals(Severity.EXPECTED, classified.severity)
        assertEquals("Teacher jane@example.com already has an overlapping lecture on MONDAY at 10:00:00", classified.userMessage)
    }

    @Test
    fun surfacesTheDatesheetTriggersRoomConflictMessage() {
        val error = RuntimeException("Room is already booked for an overlapping exam on 2026-05-01")
        val classified = ErrorClassifier.classify(error)
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals(Severity.EXPECTED, classified.severity)
        assertEquals("Room is already booked for an overlapping exam on 2026-05-01", classified.userMessage)
    }

    @Test
    fun trulyUnrecognizedFailuresStillFallBackToTheGenericMessage() {
        val error = RuntimeException("some completely unrelated backend failure")
        val classified = ErrorClassifier.classify(error, fallback = "Could not refresh the master timetable.")
        assertEquals(ErrorKind.UNEXPECTED, classified.kind)
        assertEquals(Severity.CRITICAL, classified.severity)
        assertEquals("Could not refresh the master timetable.", classified.userMessage)
    }
}
