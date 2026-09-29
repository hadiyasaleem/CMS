package com.mbd.cmscommon.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * The edge functions (supabase/functions) are TypeScript, so the Kotlin source guards never see them. This scans them for
 * the same regressions: a generic message, a raw exception text echoed to the caller, a hand-built error response that skips
 * the { "error", "code" } contract, a 500 without a specific code, or a database/auth fallback that doesn't say which step failed.
 * (Behaviour is covered by the Deno suite in supabase/functions/_tests; see Documentation/local-test-tools.md.)
 */
class EdgeFunctionGuardTest {

    private val root: File? = generateSequence(File("").absoluteFile) { it.parentFile }
        .firstOrNull { File(it, "supabase/functions").isDirectory && File(it, "core/src/main").isDirectory }

    private class Call(val file: File, val line: Int, val name: String, val args: List<String>)

    private fun functionFiles(): List<File> {
        assumeTrue("supabase/functions not found from the test working directory", root != null)
        return File(root, "supabase/functions").walkTopDown()
            .filter { it.isFile && it.extension == "ts" && it.name != "database.types.ts" && "_tests" !in it.invariantSeparatorsPath }
            .toList()
    }

    /** Every `name(...)` call in [file] with its top-level arguments (strings/templates/nesting respected). */
    private fun calls(file: File, name: String): List<Call> {
        val text = file.readText()
        val out = mutableListOf<Call>()
        var from = 0
        while (true) {
            val at = Regex("""\b$name\(""").find(text, from) ?: break
            from = at.range.last + 1
            val lineStart = text.lastIndexOf('\n', at.range.first) + 1
            if (text.substring(lineStart, at.range.first).trimStart().startsWith("//") || text.substring(lineStart, at.range.first).contains("function ")) continue
            val args = mutableListOf<String>()
            var depth = 1
            var i = from
            val cur = StringBuilder()
            var quote: Char? = null
            while (i < text.length && depth > 0) {
                val c = text[i]
                if (quote != null) {
                    cur.append(c)
                    if (c == '\\') { i++; if (i < text.length) cur.append(text[i]) } else if (c == quote) quote = null
                } else when (c) {
                    '"', '\'', '`' -> { quote = c; cur.append(c) }
                    '(', '[', '{' -> { depth++; cur.append(c) }
                    ')', ']', '}' -> { depth--; if (depth > 0) cur.append(c) }
                    ',' -> if (depth == 1) { args += cur.toString().trim(); cur.clear() } else cur.append(c)
                    else -> cur.append(c)
                }
                i++
            }
            if (cur.isNotBlank()) args += cur.toString().trim()
            out += Call(file, text.substring(0, at.range.first).count { it == '\n' } + 1, name, args)
        }
        return out
    }

    private fun assertNone(what: String, hits: List<String>) {
        assertTrue("$what:\n" + hits.joinToString("\n") { "  $it" }, hits.isEmpty())
    }

    private fun Call.where() = "${file.relativeTo(root!!)}:$line  $name(${args.joinToString(", ").take(110)})"

    private fun literal(arg: String): String? =
        Regex("""^(["'`])(.*)\1$""", RegexOption.DOT_MATCHES_ALL).find(arg)?.groupValues?.get(2)

    private val genericMessages = Regex(
        """^(internal( server)? error|something went wrong|unexpected error|an error occurred|error|failed|failure|bad request|unauthorized|forbidden|not found|server error)\.?$""",
        RegexOption.IGNORE_CASE,
    )

    @Test
    fun theScannerActuallyFindsTheCalls() {
        // Guards against a silently vacuous scan (e.g. a path or parsing change that finds nothing).
        val httpErrors = functionFiles().flatMap { calls(it, "httpError") }
        val dbErrors = functionFiles().flatMap { calls(it, "dbError") }
        assertTrue("expected many httpError calls, found ${httpErrors.size}", httpErrors.size >= 25)
        assertTrue("expected many dbError calls, found ${dbErrors.size}", dbErrors.size >= 15)
        assertTrue("httpError arguments not parsed", httpErrors.all { it.args.size >= 2 })
    }

    @Test
    fun everyHttpErrorHasAPlainSpecificMessage() {
        val hits = functionFiles().flatMap { calls(it, "httpError") }.mapNotNull { call ->
            val message = call.args.getOrNull(1)
            val text = message?.let(::literal)
            when {
                message == null || message.isBlank() -> call.where() + "  -> no message"
                text != null && text.isBlank() -> call.where() + "  -> empty message"
                text != null && genericMessages.matches(text.trim()) -> call.where() + "  -> generic message"
                text != null && text.length > 220 -> call.where() + "  -> message longer than the app's 220-character limit"
                else -> null
            }
        }
        assertNone("httpError with a missing, empty or generic message", hits)
    }

    @Test
    fun serverFailuresCarryASpecificCode() {
        // A 4xx gets its code from the status; a 5xx would default to the vague INTERNAL, so it must name one (DB_FAILED, ARCHIVE_EXPORT_FAILED ...).
        val hits = functionFiles().flatMap { calls(it, "httpError") }.filter { call ->
            val status = call.args.firstOrNull()?.toIntOrNull()
            status != null && status >= 500 && call.args.size < 3
        }.map { it.where() }
        assertNone("A 5xx httpError without an explicit code", hits)
    }

    @Test
    fun rawExceptionTextIsNeverEchoedToTheCaller() {
        val echo = Regex("""\b(e|err|error|\w+Err|\w+Error)\??\.message\b""")
        val hits = functionFiles().flatMap { calls(it, "httpError") }.filter { call -> call.args.drop(1).any { echo.containsMatchIn(it) } }.map { it.where() }
        assertNone("httpError puts a raw error.message in the response -- log it and translate it into plain words", hits)
    }

    @Test
    fun functionsAnswerThroughTheSharedHelpersOnly() {
        val hits = functionFiles().filter { it.name != "auth.ts" }.flatMap { file ->
            file.readLines().mapIndexedNotNull { i, line ->
                val code = line.trim()
                if (!code.startsWith("//") && (code.contains("new Response(") || Regex("""JSON\.stringify\(\{\s*error""").containsMatchIn(code))) {
                    "${file.relativeTo(root!!)}:${i + 1}  $code"
                } else null
            }
        }
        assertNone("A hand-built Response/error body skips the { error, code } contract -- use httpError()/ok()", hits)
    }

    @Test
    fun databaseAndAuthFallbacksSayWhichStepFailed() {
        val hits = functionFiles().flatMap { calls(it, "dbError") + calls(it, "authError") }.filter { it.file.name != "auth.ts" }.mapNotNull { call ->
            val fallback = call.args.getOrNull(1)
            val text = fallback?.let(::literal)
            when {
                fallback == null -> call.where() + "  -> no fallback message"
                text != null && (text.isBlank() || genericMessages.matches(text.trim())) -> call.where() + "  -> generic fallback"
                text != null && !text.contains("Try again", ignoreCase = true) && !text.contains("retry", ignoreCase = true) -> call.where() + "  -> fallback doesn't tell the user what to do next"
                else -> null
            }
        }
        assertNone("dbError/authError fallback that is missing, generic or gives no next step", hits)
    }
}
