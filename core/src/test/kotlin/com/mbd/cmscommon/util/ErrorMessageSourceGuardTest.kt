package com.mbd.cmscommon.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Keeps the "say what actually failed" work from quietly regressing: scans the production sources for the
 * patterns the error-message program removed. When one of these fails, the message names the file and line --
 * fix it with a specific message (see Documentation/specific-error-messages-qa.md) rather than editing the guard.
 */
class ErrorMessageSourceGuardTest {

    private val repoRoot: File? = generateSequence(File("").absoluteFile) { it.parentFile }
        .firstOrNull { File(it, "core/src/main").isDirectory && File(it, "mobile-shared").isDirectory }

    private class Hit(val file: File, val line: Int, val text: String)

    private fun productionSources(): List<File> {
        val root = repoRoot ?: return emptyList()
        return root.listFiles { f -> f.isDirectory && (f.name == "core" || f.name.startsWith("mobile-") || f.name.startsWith("desktop-")) }
            .orEmpty()
            .flatMap { module -> File(module, "src/main").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
    }

    private fun scan(files: List<File>, allowed: (File) -> Boolean = { false }, matches: (String) -> Boolean): List<Hit> =
        files.filterNot(allowed).flatMap { file ->
            file.readLines().mapIndexedNotNull { index, line ->
                val code = line.trim()
                val isComment = code.startsWith("//") || code.startsWith("*") || code.startsWith("/*")
                if (!isComment && matches(line)) Hit(file, index + 1, code) else null
            }
        }

    private fun assertNone(what: String, hits: List<Hit>) {
        assertTrue(
            "$what:\n" + hits.joinToString("\n") { "  ${it.file.relativeTo(repoRoot!!)}:${it.line}  ${it.text.take(140)}" },
            hits.isEmpty(),
        )
    }

    private fun sources(): List<File> {
        assumeTrue("repository sources not found from the test working directory", repoRoot != null)
        return productionSources()
    }

    @Test
    fun theGenericFallbackTextLivesOnlyInTheClassifier() {
        val hits = scan(sources(), allowed = { it.name == "ErrorClassifier.kt" }) {
            it.contains("Something went wrong") || it.contains("Please try again.\"")
        }
        assertNone("Generic 'Something went wrong / Please try again.' text outside ErrorClassifier", hits)
    }

    @Test
    fun noScreenShowsAVagueSomeThingCouldNotBeLoadedLine() {
        val hits = scan(sources()) { Regex("""\"Some [^\"]*could not be (loaded|refreshed)""").containsMatchIn(it) }
        assertNone("Vague 'Some ... could not be loaded' messages -- use FailureSummary to name what failed", hits)
    }

    @Test
    fun controllersDoNotReportOnlyTheFirstOfSeveralFailures() {
        val controllers = sources().filter { it.path.replace('\\', '/').contains("/controller/") }
        val hits = scan(controllers) { it.contains("firstNotNullOfOrNull { it.exceptionOrNull() }") || it.contains("failures.firstOrNull()?.userMessage") }
        assertNone("A controller shows just the first failure -- collect them all with FailureSummary", hits)
    }

    @Test
    fun everyControllerLaunchNamesTheActionItPerforms() {
        val controllers = sources().filter { it.path.replace('\\', '/').contains("/controller/") }
        val hits = scan(controllers) { Regex("""(^|[\s=(])launch \{""").containsMatchIn(it) && !it.contains("scope.launch") }
        assertNone("ScreenController.launch without an action label -- use launch(\"save the teacher\") so a failure reads \"Couldn't save the teacher\"", hits)
    }

    @Test
    fun exceptionsAreNeverPutStraightIntoUserFacingText() {
        val hits = scan(sources(), allowed = { it.name == "ErrorClassifier.kt" || it.name == "CmsLog.kt" || it.name == "EdgeFunctionErrors.kt" }) {
            Regex("""(errorMessage|_error\.value|_loadError\.value|_refreshError\.value|_notice\.value)\s*=\s*.*\b(e|t|it|error|throwable|exception|cause)\??\.(message|localizedMessage)\b""").containsMatchIn(it)
        }
        assertNone("Raw exception.message shown to the user -- route it through ErrorClassifier / userMessageLogged", hits)
    }

    @Test
    fun theErrorDialogAlwaysSaysWhatWasBeingDone() {
        // CmsErrorDialog has a required title; a title of just "Error"/"Something went wrong" would defeat that.
        val hits = scan(sources()) { Regex("""CmsErrorDialog\([^)]*title\s*=\s*\"(Error|Oops|Something went wrong)\"""").containsMatchIn(it) }
        assertNone("CmsErrorDialog with a non-descriptive title", hits)
    }
}
