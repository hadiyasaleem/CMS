package com.mbd.cmscommon.util

import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * More rows for the regression matrix (see [ErrorMessageMatrixTest]): transient and platform database codes, edge-function
 * HTTP failures, and the file/export paths, which go through [FileReadErrors] rather than [ErrorClassifier].
 */
class ErrorMessageMatrixExtrasTest {

    private class Case(
        val name: String,
        val error: Throwable,
        val kind: ErrorKind,
        val message: String? = null,
        val contains: List<String> = emptyList(),
    )

    private fun pg(code: String, message: String): RuntimeException =
        RuntimeException("""{"code":"$code","details":null,"hint":null,"message":"$message"}""")

    private val internals = listOf("http://", "https://", "supabase", "postgrest", "exception", "sqlstate", "violates", "{", "}", "at com.")

    private val cases = listOf(
        Case("jwt expired", pg("PGRST301", "JWT expired"), ErrorKind.AUTH, "Your session has expired. Sign in again."),
        Case("anonymous access disabled", pg("PGRST302", "Anonymous access is disabled"), ErrorKind.AUTH, "Your session has expired. Sign in again."),
        Case("serialization failure", pg("40001", "could not serialize access due to concurrent update"), ErrorKind.CONFLICT, contains = listOf("Someone else changed this")),
        Case("deadlock", pg("40P01", "deadlock detected"), ErrorKind.CONFLICT, contains = listOf("Someone else changed this")),
        Case("statement timeout", pg("57014", "canceling statement due to statement timeout"), ErrorKind.NETWORK, "The request took too long. Check your connection and try again."),
        Case("too many connections", pg("53300", "remaining connection slots are reserved"), ErrorKind.NETWORK, contains = listOf("temporarily unavailable")),
        Case("connection failure", pg("08006", "connection failure"), ErrorKind.NETWORK, contains = listOf("temporarily unavailable")),
        Case("schema out of date (missing column)", pg("PGRST204", "Could not find the 'foo' column of 'teachers' in the schema cache"), ErrorKind.UNEXPECTED, contains = listOf("out of date", "(Ref ")),
        Case("schema out of date (missing relationship)", pg("PGRST200", "Could not find a relationship between 'a' and 'b' in the schema cache"), ErrorKind.UNEXPECTED, contains = listOf("out of date", "(Ref ")),
        Case("schema out of date (missing function)", pg("PGRST202", "Could not find the function public.foo in the schema cache"), ErrorKind.UNEXPECTED, contains = listOf("out of date", "(Ref ")),
        Case("schema out of date (undefined table)", pg("42P01", "relation \\\"public.x\\\" does not exist"), ErrorKind.UNEXPECTED, contains = listOf("out of date", "(Ref ")),
        Case("no row for single()", pg("PGRST116", "JSON object requested, multiple (or no) rows returned"), ErrorKind.NOT_FOUND, "That record no longer exists. Refresh and try again."),
        Case(
            "teacher re-saves a locked score",
            pg("42501", "new row violates row-level security policy (USING expression) for table \\\"session_marks\\\""),
            ErrorKind.PERMISSION,
            contains = listOf("already saved and are locked", "request an edit"),
        ),
        Case("edge 401", EdgeFunctionErrors.parse(401, null), ErrorKind.AUTH, "Your session has expired. Sign in again."),
        Case("edge 403", EdgeFunctionErrors.parse(403, null), ErrorKind.PERMISSION, "You don't have permission to do this."),
        Case("edge 404", EdgeFunctionErrors.parse(404, ""), ErrorKind.NOT_FOUND, contains = listOf("no longer exists")),
        Case(
            "edge 422 with a server sentence",
            EdgeFunctionErrors.parse(422, """{"error":"That email address isn't valid.","code":"INVALID_EMAIL"}"""),
            ErrorKind.VALIDATION,
            "That email address isn't valid.",
        ),
        Case("edge 429", EdgeFunctionErrors.parse(429, ""), ErrorKind.NETWORK, "Too many attempts. Wait a minute and try again."),
        Case(
            "edge 500 with a server sentence",
            EdgeFunctionErrors.parse(500, """{"error":"Couldn't back up the session's marks records, so nothing was deleted. Try again.","code":"ARCHIVE_EXPORT_FAILED"}"""),
            ErrorKind.UNEXPECTED,
            contains = listOf("Couldn't back up the session's marks records", "(Ref "),
        ),
        Case(
            "edge permission-check outage",
            EdgeFunctionErrors.parse(500, """{"error":"Couldn't check your permissions right now. Try again in a moment.","code":"PERMISSION_CHECK_FAILED"}"""),
            ErrorKind.UNEXPECTED,
            contains = listOf("Couldn't check your permissions right now"),
        ),
        Case("edge 502 gateway page", EdgeFunctionErrors.parse(502, "<html>Bad gateway</html>"), ErrorKind.UNEXPECTED, contains = listOf("server hit a problem")),
        Case("edge 503", EdgeFunctionErrors.parse(503, null), ErrorKind.UNEXPECTED, contains = listOf("server hit a problem")),
    )

    @Test
    fun databaseAndEdgeFunctionFailuresGetTheirKindAndWording() {
        val problems = cases.mapNotNull { case ->
            val c = ErrorClassifier.classify(case.error, "Couldn't save.")
            val missing = case.contains.filterNot { c.userMessage.contains(it, ignoreCase = true) }
            val leaked = internals.filter { c.userMessage.contains(it, ignoreCase = true) }
            when {
                c.kind != case.kind -> "${case.name}: got ${c.kind} \"${c.userMessage}\", expected ${case.kind}"
                case.message != null && c.userMessage != case.message -> "${case.name}: got \"${c.userMessage}\", expected \"${case.message}\""
                missing.isNotEmpty() -> "${case.name}: \"${c.userMessage}\" is missing $missing"
                leaked.isNotEmpty() -> "${case.name}: leaks $leaked in \"${c.userMessage}\""
                c.userMessage == ErrorClassifier.DEFAULT_FALLBACK -> "${case.name}: bare generic fallback"
                else -> null
            }
        }
        assertTrue("Mismatched rows:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    private val fileInternals = listOf("java.", "exception", "\\", "/data/", "content://", "errno")

    @Test
    fun pickedFileFailuresReadAsPlainSentences() {
        val rows = listOf(
            java.util.zip.ZipException("invalid entry size") to "isn't a valid Excel",
            java.io.FileNotFoundException("/storage/emulated/0/x.xlsx (No such file)") to "can no longer be found",
            java.nio.file.NoSuchFileException("C:\\Users\\a\\x.xlsx") to "can no longer be found",
            SecurityException("Permission Denial: opening provider") to "isn't allowed to read",
            java.nio.file.AccessDeniedException("C:\\x.xlsx") to "isn't allowed to read",
            OutOfMemoryError("Java heap space") to "too large",
            java.io.IOException("Stream closed") to "open in another program",
            RuntimeException("boom") to "Couldn't read that file",
            CmsException.Validation("The file is too large (5 MB at most).") to "5 MB at most",
        )
        for ((error, expected) in rows) {
            val message = FileReadErrors.describe(error)
            assertTrue("$error -> \"$message\" should contain \"$expected\"", message.contains(expected, ignoreCase = true))
            val leaked = fileInternals.filter { message.contains(it, ignoreCase = true) }
            assertTrue("$error leaks $leaked in \"$message\"", leaked.isEmpty())
            assertTrue("$error message too long: $message", message.length <= 200)
        }
    }

    @Test
    fun exportWriteFailuresReadAsPlainSentences() {
        val rows = listOf(
            SecurityException("denied") to "isn't allowed to save",
            java.nio.file.AccessDeniedException("C:\\out.pdf") to "isn't allowed to save",
            java.io.IOException("No space left on device") to "enough free space",
            RuntimeException("wrapper", java.io.IOException("There is not enough space on the disk")) to "enough free space",
            java.io.IOException("The process cannot access the file because it is being used by another process") to "open in another program",
            RuntimeException("boom") to "Couldn't create the PDF file",
        )
        for ((error, expected) in rows) {
            val message = FileReadErrors.describeWrite(error, "PDF")
            assertTrue("$error -> \"$message\" should contain \"$expected\"", message.contains(expected, ignoreCase = true))
            val leaked = fileInternals.filter { message.contains(it, ignoreCase = true) }
            assertTrue("$error leaks $leaked in \"$message\"", leaked.isEmpty())
        }
    }
}
