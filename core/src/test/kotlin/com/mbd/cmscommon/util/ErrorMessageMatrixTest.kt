package com.mbd.cmscommon.util

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One row per failure a user can realistically hit, with the kind and wording they must get. Beyond the
 * per-row expectations, every row is held to the same rules: never a bare generic line, never internals
 * (URLs, SQL, exception class names, JSON), and an unclassifiable failure always carries a Ref code.
 */
class ErrorMessageMatrixTest {

    private class Case(
        val name: String,
        val error: Throwable,
        val kind: ErrorKind,
        /** Exact message, or null when only [contains] matters. */
        val message: String? = null,
        val contains: List<String> = emptyList(),
        val fallback: String = "Couldn't save the record.",
    )

    private fun pg(code: String, message: String, details: String? = null): RuntimeException {
        val escaped = message.replace("\"", "\\\"")
        val detailsJson = details?.let { "\"${it.replace("\"", "\\\"")}\"" } ?: "null"
        return RuntimeException("""{"code":"$code","details":$detailsJson,"hint":null,"message":"$escaped"}""")
    }

    private val offline = "Unable to connect. Check your internet connection and try again."

    private val cases = listOf(
        // ---- typed exceptions carry their own wording ----
        Case("typed validation", CmsException.Validation("Enter a valid email address."), ErrorKind.VALIDATION, "Enter a valid email address."),
        Case("typed conflict", CmsException.Conflict("Room 201 is already booked on MONDAY."), ErrorKind.CONFLICT, "Room 201 is already booked on MONDAY."),
        Case("typed not found", CmsException.NotFound("This link request is not available on this device yet. Refresh and try again."), ErrorKind.NOT_FOUND,
            "This link request is not available on this device yet. Refresh and try again."),
        Case("typed permission", CmsException.Permission(), ErrorKind.PERMISSION, "You do not have permission to perform this action."),
        Case("typed auth", CmsException.Auth("Your account has no role assigned yet. Contact an administrator."), ErrorKind.AUTH,
            "Your account has no role assigned yet. Contact an administrator."),

        // ---- connectivity ----
        Case("no host", UnknownHostException("example.supabase.co"), ErrorKind.NETWORK, offline),
        Case("resolve failure text", RuntimeException("Unable to resolve host \"example.supabase.co\""), ErrorKind.NETWORK, offline),
        Case("read timeout", SocketTimeoutException("timeout"), ErrorKind.NETWORK, "The request took too long. Check your connection and try again."),
        Case("connection reset", IOException("Connection reset by peer"), ErrorKind.NETWORK, offline),
        Case("rate limited", RuntimeException("HTTP 429 Too Many Requests"), ErrorKind.NETWORK, "Too many attempts. Please wait a moment and try again."),

        // ---- database: constraint and permission failures ----
        Case("duplicate roll number", pg("23505", "duplicate key value violates unique constraint \"session_students_pkey\"", "Key (session_id, roll_number)=(isl_2026, IT-22-01) already exists."),
            ErrorKind.CONFLICT, "Roll number IT-22-01 is already in this session."),
        Case("duplicate unknown constraint", pg("23505", "duplicate key value violates unique constraint \"some_new_key\""), ErrorKind.CONFLICT,
            contains = listOf("already exists")),
        Case("foreign key on delete", pg("23503", "update or delete on table \"departments\" violates foreign key constraint \"x_fkey\" on table \"academic_sessions\""),
            ErrorKind.CONFLICT, contains = listOf("still has sessions")),
        Case("missing required value", pg("23502", "null value in column \"name\" of relation \"teachers\" violates not-null constraint"),
            ErrorKind.VALIDATION, contains = listOf("name")),
        Case("value too long", pg("22001", "value too long for type character varying(40)"), ErrorKind.VALIDATION, contains = listOf("too long")),
        Case("row level security", pg("42501", "new row violates row-level security policy for table \"teachers\""), ErrorKind.PERMISSION, contains = listOf("permission")),
        Case("raised exception", pg("P0001", "Session ISL 2026 is full (50 students max)"), ErrorKind.CONFLICT, "Session ISL 2026 is full (50 students max)"),
        Case("raised permission text", pg("P0001", "You don't have permission to approve link requests. Ask an admin."), ErrorKind.PERMISSION,
            "You don't have permission to approve link requests. Ask an admin."),
        Case("raised admin-only text", pg("P0001", "Only an admin can approve attendance edit requests."), ErrorKind.PERMISSION),
        Case("raised no-longer-exists text", pg("P0001", "This link request no longer exists. Refresh the list."), ErrorKind.NOT_FOUND,
            "This link request no longer exists. Refresh the list."),
        Case("no rows for single()", pg("PGRST116", "JSON object requested, multiple (or no) rows returned"), ErrorKind.NOT_FOUND),

        // ---- edge functions ----
        Case("edge conflict", EdgeFunctionErrors.parse(409, """{"error":"This session is still active. Deactivate it before archiving and deleting it.","code":"SESSION_ACTIVE"}"""),
            ErrorKind.CONFLICT, "This session is still active. Deactivate it before archiving and deleting it."),
        Case("edge forbidden", EdgeFunctionErrors.parse(403, null), ErrorKind.PERMISSION, "You don't have permission to do this."),
        Case("edge gateway page", EdgeFunctionErrors.parse(502, "<html><body>Bad gateway</body></html>"), ErrorKind.UNEXPECTED, contains = listOf("server hit a problem")),

        // ---- sign-in ----
        Case("wrong password", RuntimeException("Invalid login credentials"), ErrorKind.AUTH, "The email or password is incorrect."),
        Case("unconfirmed email", RuntimeException("Email not confirmed"), ErrorKind.AUTH, contains = listOf("Confirm your email")),
        Case("existing account", RuntimeException("User already registered"), ErrorKind.CONFLICT, contains = listOf("already exists")),

        // ---- nothing recognisable: the action label plus a reference, never a bare generic line ----
        Case("unknown failure", RuntimeException("boom"), ErrorKind.UNEXPECTED, contains = listOf("Couldn't save the record.", "(Ref ")),
        Case("failure leaking internals", RuntimeException("failed calling https://x.supabase.co/rest/v1/teachers"), ErrorKind.UNEXPECTED,
            contains = listOf("Couldn't save the record.", "(Ref ")),
        Case("null pointer", NullPointerException(), ErrorKind.UNEXPECTED, contains = listOf("(Ref ")),
    )

    private val internals = listOf("http://", "https://", "supabase", "postgrest", "exception", "sqlstate", "select ", "insert into", "violates", "constraint \"", "{", "}", "at com.")

    @Test
    fun everyRowGetsItsKindAndWording() {
        val problems = cases.mapNotNull { case ->
            val c = ErrorClassifier.classify(case.error, case.fallback)
            val wrongKind = c.kind != case.kind
            val wrongText = case.message != null && c.userMessage != case.message
            val missing = case.contains.filterNot { c.userMessage.contains(it, ignoreCase = true) }
            if (wrongKind || wrongText || missing.isNotEmpty()) {
                "${case.name}: got ${c.kind} \"${c.userMessage}\"; expected ${case.kind}" +
                    (case.message?.let { " \"$it\"" } ?: "") + (if (missing.isEmpty()) "" else " containing $missing")
            } else {
                null
            }
        }
        assertTrue("Mismatched rows:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun noRowShowsInternalsOrABareGenericLine() {
        for (case in cases) {
            val message = ErrorClassifier.classify(case.error, case.fallback).userMessage
            assertTrue("${case.name}: blank message", message.isNotBlank())
            assertNotEquals("${case.name}: bare generic fallback", ErrorClassifier.DEFAULT_FALLBACK, message)
            assertTrue("${case.name}: too long (${message.length}): $message", message.length <= 260)
            val leaked = internals.filter { message.contains(it, ignoreCase = true) }
            assertTrue("${case.name}: leaks $leaked in \"$message\"", leaked.isEmpty())
        }
    }

    @Test
    fun onlyUnclassifiableFailuresCarryAReferenceAndAreCritical() {
        for (case in cases) {
            val c = ErrorClassifier.classify(case.error, case.fallback)
            if (c.kind == ErrorKind.UNEXPECTED) {
                assertTrue("${case.name}: no reference", c.reference != null && c.userMessage.contains("(Ref ${c.reference})"))
                assertEquals("${case.name}: severity", Severity.CRITICAL, c.severity)
            } else {
                assertFalse("${case.name}: unexpected Ref on an understood failure", c.userMessage.contains("(Ref "))
                assertEquals("${case.name}: severity", Severity.EXPECTED, c.severity)
            }
        }
    }

    @Test
    fun theActionLabelBecomesTheFallbackSentence() {
        assertEquals("Couldn't save the teacher.", ErrorClassifier.fallbackFor("save the teacher"))
        assertEquals("Couldn't save the teacher.", ErrorClassifier.fallbackFor(" save the teacher. "))
        assertEquals(ErrorClassifier.DEFAULT_FALLBACK, ErrorClassifier.fallbackFor(null))
        assertEquals(ErrorClassifier.DEFAULT_FALLBACK, ErrorClassifier.fallbackFor("  "))
    }

    @Test
    fun theSameUnknownFailureAlwaysGetsTheSameReference() {
        val a = ErrorClassifier.classify(RuntimeException("boom"), "Couldn't save.")
        val b = ErrorClassifier.classify(RuntimeException("boom"), "Couldn't save.")
        assertEquals(a.reference, b.reference)
    }
}
