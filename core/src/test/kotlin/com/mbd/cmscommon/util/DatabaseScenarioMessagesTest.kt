package com.mbd.cmscommon.util

import java.io.File
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Runs the real errors captured from a scratch Postgres (migrations applied, every failure provoked by
 * `.testtools/run/scenarios.mjs`) through [ErrorClassifier] exactly as PostgREST would deliver them, and checks the
 * user-visible wording. Skipped when the capture file is absent, so it costs nothing on a machine without the tools
 * (see Documentation/local-test-tools.md to regenerate it).
 */
class DatabaseScenarioMessagesTest {

    private val captureFile: File? = generateSequence(File("").absoluteFile) { it.parentFile }
        .map { File(it, ".testtools/run/scenario-results.json") }
        .firstOrNull { it.isFile }

    private class Captured(val name: String, val ok: Boolean, val code: String?, val message: String?, val detail: String?)

    private fun captured(): List<Captured> {
        assumeTrue("no database capture found (run .testtools/run/scenarios.mjs)", captureFile != null)
        return Json.parseToJsonElement(captureFile!!.readText()).jsonArray.map { row ->
            val o = row.jsonObject
            fun str(key: String) = o[key]?.takeIf { it !is JsonNull }?.jsonPrimitive?.content
            Captured(str("name")!!, o["ok"]?.jsonPrimitive?.content == "true", str("code"), str("message"), str("detail"))
        }
    }

    /** The JSON body PostgREST returns for a database error (what supabase-kt puts in the exception message). */
    private fun body(c: Captured): RuntimeException {
        val json: JsonObject = buildJsonObject {
            put("code", JsonPrimitive(c.code))
            put("details", c.detail?.let { JsonPrimitive(it) } ?: JsonNull)
            put("hint", JsonNull)
            put("message", JsonPrimitive(c.message))
        }
        return RuntimeException(json.toString())
    }

    private val internals = listOf("violates", "constraint \"", "relation \"", "Failing row", "SQLSTATE", "http", "supabase", "postgrest", "exception", "{", "}")

    @Test
    fun everyProvokedDatabaseErrorReadsAsAPlainSentence() {
        val problems = captured().filterNot { it.ok }.mapNotNull { c ->
            val classified = ErrorClassifier.classify(body(c), "Couldn't save.")
            val message = classified.userMessage
            val leaked = internals.filter { message.contains(it, ignoreCase = true) }
            when {
                classified.kind == ErrorKind.UNEXPECTED -> "${c.name}: fell through to a Ref-code error: $message  (raw: ${c.message})"
                leaked.isNotEmpty() -> "${c.name}: leaks $leaked: $message"
                message.length > 220 -> "${c.name}: ${message.length} chars: $message"
                else -> null
            }
        }
        assertTrue("Database errors that would not read well:\n" + problems.joinToString("\n"), problems.isEmpty())
    }

    @Test
    fun raisedTriggerTextIsShownExactlyAsWritten() {
        val raised = captured().filter { !it.ok && it.code == "P0001" }
        assumeTrue("no P0001 rows captured", raised.isNotEmpty())
        for (c in raised) {
            val classified = ErrorClassifier.classify(body(c), "Couldn't save.")
            assertTrue("${c.name}: expected the raised text verbatim, got \"${classified.userMessage}\"", classified.userMessage == c.message)
        }
    }

    @Test
    fun printsTheWholeMatrixForReview() {
        val rows = captured().filterNot { it.ok }.joinToString("\n") { c ->
            val classified = ErrorClassifier.classify(body(c), "Couldn't save.")
            "%-52s %-5s %-10s %s".format(c.name, c.code, classified.kind, classified.userMessage)
        }
        File(captureFile!!.parentFile, "scenario-classified.txt").writeText(rows + "\n")
    }

    @Test
    fun everyConstraintNameWeWordSpeciallyExistsInTheDatabase() {
        assumeTrue("no database capture found", captureFile != null)
        val namesFile = File(captureFile!!.parentFile, "db-names.txt")
        assumeTrue("no constraint-name capture (rerun scenarios.mjs)", namesFile.isFile)
        val real = namesFile.readLines().toSet()
        val missing = ConstraintMessages.namedConstraints.filterNot { it in real }
        assertTrue("ConstraintMessages words constraints that do not exist (renamed or dropped?): $missing", missing.isEmpty())
    }

    @Test
    fun everyEdgeFunctionFailureIsShownAsTheServerWroteIt() {
        assumeTrue("no database capture found", captureFile != null)
        val edge = File(captureFile!!.parentFile, "edge-captures.jsonl")
        assumeTrue("no edge-function capture (run deno test with EDGE_CAPTURE set)", edge.isFile)
        val problems = edge.readLines().filter { it.isNotBlank() }.mapNotNull { line ->
            val row = Json.parseToJsonElement(line).jsonObject
            val status = row["status"]!!.jsonPrimitive.content.toInt()
            val body = row["body"]!!.jsonPrimitive.content
            val serverText = Json.parseToJsonElement(body).jsonObject["error"]!!.jsonPrimitive.content
            val e = EdgeFunctionErrors.parse(status, body)
            val expectedKind = when (status) {
                400 -> CmsException.Validation::class
                401 -> CmsException.Auth::class
                403 -> CmsException.Permission::class
                404 -> CmsException.NotFound::class
                409 -> CmsException.Conflict::class
                else -> CmsException.Unexpected::class
            }
            when {
                e.message != serverText -> "status $status: \"$serverText\" was replaced by \"${e.message}\""
                e::class != expectedKind -> "status $status: \"$serverText\" became ${e::class.simpleName}, expected ${expectedKind.simpleName}"
                else -> null
            }
        }
        assertTrue("Edge-function errors the app would word differently than the server:\n" + problems.joinToString("\n"), problems.isEmpty())
    }
}
