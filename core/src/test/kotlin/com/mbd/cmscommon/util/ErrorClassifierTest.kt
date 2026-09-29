package com.mbd.cmscommon.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ErrorClassifierTest {

    private val refSuffix = Regex(""" \(Ref [0-9A-F]{4}\)$""")

    /** A PostgREST JSON error body, as supabase-kt surfaces it in the exception message. */
    private fun pgBody(code: String, message: String, details: String? = null): RuntimeException {
        val escaped = message.replace("\"", "\\\"")
        val detailsJson = details?.let { "\"${it.replace("\"", "\\\"")}\"" } ?: "null"
        return RuntimeException("""{"code":"$code","details":$detailsJson,"hint":null,"message":"$escaped"}""")
    }

    private fun classify(error: Throwable, fallback: String = "Could not save.") = ErrorClassifier.classify(error, fallback)

    // ---- timetable trigger (previous fix, text-only messages with no SQLSTATE) ----

    @Test
    fun surfacesTheDbTriggersRoomConflictMessageInsteadOfTheGenericFallback() {
        val classified = classify(RuntimeException("Room 201 is already booked overlapping 10:00:00 on MONDAY"), "Could not refresh the master timetable.")
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals(Severity.EXPECTED, classified.severity)
        assertEquals("Room 201 is already booked overlapping 10:00:00 on MONDAY", classified.userMessage)
    }

    @Test
    fun surfacesTheDbTriggersTeacherConflictMessageInsteadOfTheGenericFallback() {
        val classified = classify(RuntimeException("Teacher jane@example.com already has an overlapping lecture on MONDAY at 10:00:00"), "Could not refresh the master timetable.")
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals("Teacher jane@example.com already has an overlapping lecture on MONDAY at 10:00:00", classified.userMessage)
    }

    @Test
    fun surfacesTheDatesheetTriggersRoomConflictMessage() {
        val classified = ErrorClassifier.classify(RuntimeException("Room is already booked for an overlapping exam on 2026-05-01"))
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals("Room is already booked for an overlapping exam on 2026-05-01", classified.userMessage)
    }

    // ---- RAISE EXCEPTION (P0001) passes through ----

    @Test
    fun raisedExceptionTextIsShownVerbatim() {
        val classified = classify(pgBody("P0001", "Session ISL 2026 is full (50 students max)"))
        assertEquals(ErrorKind.CONFLICT, classified.kind)
        assertEquals(Severity.EXPECTED, classified.severity)
        assertEquals("Session ISL 2026 is full (50 students max)", classified.userMessage)
    }

    @Test
    fun raisedExceptionWithoutConflictWordingIsAValidationError() {
        val classified = classify(pgBody("P0001", "This session does not run a Morning shift."))
        assertEquals(ErrorKind.VALIDATION, classified.kind)
        assertEquals("This session does not run a Morning shift.", classified.userMessage)
    }

    @Test
    fun raisedExceptionContainingInternalsFallsBackToTheGenericPath() {
        val classified = classify(pgBody("P0001", "failed calling https://x.supabase.co/rest/v1/teachers"))
        assertEquals(ErrorKind.UNEXPECTED, classified.kind)
        assertTrue(classified.userMessage.startsWith("Could not save."))
    }

    // ---- unique violations (23505) ----

    private fun unique(constraint: String, details: String?) =
        pgBody("23505", "duplicate key value violates unique constraint \"$constraint\"", details)

    @Test
    fun duplicateRollNumberNamesTheRollNumber() {
        val c = classify(unique("session_students_pkey", "Key (session_id, roll_number)=(isl_2026, IT-22-01) already exists."))
        assertEquals(ErrorKind.CONFLICT, c.kind)
        assertEquals("Roll number IT-22-01 is already in this session.", c.userMessage)
    }

    @Test
    fun duplicateTeacherEmailNamesTheEmail() {
        val c = classify(unique("teachers_pkey", "Key (email)=(a@b.pk) already exists."))
        assertEquals("A teacher with email a@b.pk already exists.", c.userMessage)
    }

    @Test
    fun duplicateDepartmentCodeNamesTheCode() {
        val c = classify(unique("departments_pkey", "Key (dept_id)=(ENG) already exists."))
        assertEquals("A department with code ENG already exists.", c.userMessage)
    }

    @Test
    fun namedUniqueConstraintsGetTheirOwnSentence() {
        assertEquals("This class already has a period at that day and time.", classify(unique("uq_session_slot", null)).userMessage)
        assertEquals("This building already has a room with that number.", classify(unique("rooms_building_id_room_no_key", null)).userMessage)
        assertEquals(
            "A session for this department, intake year and program type already exists.",
            classify(unique("academic_sessions_dept_id_start_year_program_key", null)).userMessage,
        )
        assertTrue(classify(unique("one_cr_per_session_shift", null)).userMessage.startsWith("This class already has a class representative"))
    }

    @Test
    fun uniqueViolationInferredFromTextAloneStillGetsAFriendlyMessage() {
        val c = classify(RuntimeException("duplicate key value violates unique constraint \"uq_university_roll\""))
        assertEquals(ErrorKind.CONFLICT, c.kind)
        assertEquals("University roll number is already assigned to another student.", c.userMessage)
    }

    @Test
    fun unknownUniqueConstraintUsesTheTableLabelWithoutLeakingTheConstraintName() {
        val c = classify(unique("teachers_something_key", null))
        assertEquals("A teacher with the same details already exists.", c.userMessage)
        assertFalse(c.userMessage.contains("teachers_something_key"))
    }

    // ---- foreign keys (23503) ----

    @Test
    fun deletingAParentThatStillHasChildrenNamesBoth() {
        val body = pgBody(
            "23503",
            "update or delete on table \"departments\" violates foreign key constraint \"academic_sessions_dept_id_fkey\" on table \"academic_sessions\"",
            "Key (dept_id)=(ENG) is still referenced from table \"academic_sessions\".",
        )
        val c = classify(body)
        assertEquals(ErrorKind.CONFLICT, c.kind)
        assertEquals("This department can't be deleted or changed because it still has sessions. Remove those first.", c.userMessage)
    }

    @Test
    fun pointingAtSomethingThatNoLongerExistsNamesIt() {
        val body = pgBody(
            "23503",
            "insert or update on table \"academic_sessions\" violates foreign key constraint \"academic_sessions_dept_id_fkey\"",
            "Key (dept_id)=(zzz) is not present in table \"departments\".",
        )
        assertEquals("The department you selected no longer exists. Refresh and choose again.", classify(body).userMessage)
    }

    // ---- check (23514), not-null (23502), format/length ----

    @Test
    fun namedCheckConstraintsExplainTheRule() {
        val c = classify(pgBody("23514", "new row for relation \"session_marks\" violates check constraint \"session_marks_check\""))
        assertEquals(ErrorKind.VALIDATION, c.kind)
        assertEquals("The score must be between 0 and the maximum marks for this exam.", c.userMessage)

        val sem = classify(pgBody("23514", "new row for relation \"academic_sessions\" violates check constraint \"academic_sessions_current_semester_check\""))
        assertTrue(sem.userMessage.contains("BS runs 1-8"))
    }

    @Test
    fun unnamedCheckConstraintsDeriveALabelFromTheColumn() {
        val c = classify(pgBody("23514", "new row for relation \"session_subjects\" violates check constraint \"session_subjects_semester_check\""))
        assertEquals("Semester is outside the allowed range for this subject.", c.userMessage)
    }

    @Test
    fun notNullViolationNamesTheField() {
        val c = classify(pgBody("23502", "null value in column \"roll_number\" of relation \"session_students\" violates not-null constraint"))
        assertEquals("Roll number is required.", c.userMessage)
        val unknown = classify(pgBody("23502", "null value in column \"internal_col\" of relation \"x\" violates not-null constraint"))
        assertEquals("A required field is missing.", unknown.userMessage)
    }

    @Test
    fun tooLongAndBadFormatValuesAreExplained() {
        assertEquals(
            "One of the entered values is too long (at most 50 characters).",
            classify(pgBody("22001", "value too long for type character varying(50)")).userMessage,
        )
        val fmt = classify(pgBody("22P02", "invalid input syntax for type uuid: \"abc\""))
        assertEquals(ErrorKind.VALIDATION, fmt.kind)
        assertFalse(fmt.userMessage.contains("abc"))
    }

    // ---- permissions ----

    @Test
    fun rowLevelSecurityDenialNamesWhatYouCannotChange() {
        val c = classify(pgBody("42501", "new row violates row-level security policy for table \"teachers\""))
        assertEquals(ErrorKind.PERMISSION, c.kind)
        assertEquals("You don't have permission to change teacher records.", c.userMessage)
    }

    // ---- typed exceptions and the unexpected fallback ----

    @Test
    fun typedConflictIsShownVerbatim() {
        val c = classify(CmsException.Conflict("Teacher Ayesha already has a lecture with ENG Semester 5 Morning on Monday."))
        assertEquals(ErrorKind.CONFLICT, c.kind)
        assertEquals("Teacher Ayesha already has a lecture with ENG Semester 5 Morning on Monday.", c.userMessage)
        assertNull(c.reference)
    }

    @Test
    fun trulyUnknownFailuresGetTheActionAwareFallbackAndAReferenceCode() {
        val c = classify(RuntimeException("some completely unrelated backend failure"), "Could not refresh the master timetable.")
        assertEquals(ErrorKind.UNEXPECTED, c.kind)
        assertEquals(Severity.CRITICAL, c.severity)
        assertTrue(c.userMessage.startsWith("Could not refresh the master timetable."))
        assertTrue(refSuffix.containsMatchIn(c.userMessage))
        assertNotNull(c.reference)
        assertTrue(c.userMessage.endsWith("(Ref ${c.reference})"))
    }

    @Test
    fun theSameFailureAlwaysGetsTheSameReference() {
        val a = classify(RuntimeException("boom 42"))
        val b = classify(RuntimeException("boom 42"))
        val other = classify(RuntimeException("different"))
        assertEquals(a.reference, b.reference)
        assertNotNull(other.reference)
    }

    @Test
    fun typedUnexpectedFailuresAlsoCarryAReference() {
        val c = classify(CmsException.Unexpected("Could not decode the server response."))
        assertEquals(Severity.CRITICAL, c.severity)
        assertTrue(refSuffix.containsMatchIn(c.userMessage))
    }

    @Test
    fun fallbackForBuildsAnActionSentence() {
        assertEquals("Couldn't save the teacher.", ErrorClassifier.fallbackFor("save the teacher"))
        assertEquals("Couldn't delete the department.", ErrorClassifier.fallbackFor("delete the department."))
        assertEquals(ErrorClassifier.DEFAULT_FALLBACK, ErrorClassifier.fallbackFor(null))
        assertEquals(ErrorClassifier.DEFAULT_FALLBACK, ErrorClassifier.fallbackFor("  "))
    }

    // ---- guardrail: internals never reach the user ----

    @Test
    fun noClassifiedMessageEverLeaksInternals() {
        val samples = listOf(
            pgBody("23505", "duplicate key value violates unique constraint \"weird_internal_idx\"", "Key (secret_col)=(xyz) already exists."),
            pgBody("23503", "insert or update on table \"secret_table\" violates foreign key constraint \"secret_fk\""),
            pgBody("23514", "new row for relation \"secret_table\" violates check constraint \"secret_table_hidden_col_check\""),
            pgBody("23502", "null value in column \"hidden_col\" of relation \"secret_table\" violates not-null constraint"),
            pgBody("42501", "permission denied for table secret_table"),
            RuntimeException("io.ktor.client.plugins.HttpRequestTimeoutException: https://x.supabase.co/rest/v1/teachers?select=* timed out"),
            IllegalStateException("java.lang.NullPointerException at com.mbd.Foo.bar(Foo.kt:12) {\"a\":1}"),
        )
        val forbidden = listOf("http", "supabase", "postgrest", "select=", "secret_", "weird_internal", "hidden_col", "xyz", "Exception", "constraint", ".kt")
        samples.forEach { sample ->
            val message = classify(sample).userMessage
            forbidden.forEach { bad ->
                assertFalse("'$message' leaks '$bad'", message.contains(bad, ignoreCase = bad != "Exception"))
            }
        }
    }
}
