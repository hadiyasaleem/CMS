package com.mbd.cmscommon.util

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/**
 * Guards for the code that catches failures itself -- mobile ViewModels, desktop screens and controllers -- where a
 * generic or missing message would not be caught by [ErrorMessageSourceGuardTest]'s text patterns:
 *  - a caller may not lean on the default generic fallback,
 *  - a `catch` block must classify, log or rethrow (never swallow),
 *  - a statement-level `runCatching { }` whose result is dropped must say why (`// Best-effort: ...`).
 * Failures name file:line. Fix the code (show a specific message, or log and comment the deliberate drop) --
 * do not loosen the guard.
 */
class ErrorMessageUiGuardTest {

    private val repoRoot: File? = generateSequence(File("").absoluteFile) { it.parentFile }
        .firstOrNull { File(it, "core/src/main").isDirectory && File(it, "mobile-shared").isDirectory }

    private class Hit(val file: File, val line: Int, val text: String)

    private fun sources(): List<File> {
        assumeTrue("repository sources not found from the test working directory", repoRoot != null)
        return repoRoot!!.listFiles { f -> f.isDirectory && (f.name == "core" || f.name.startsWith("mobile-") || f.name.startsWith("desktop-")) }
            .orEmpty()
            .flatMap { module -> File(module, "src/main").walkTopDown().filter { it.isFile && it.extension == "kt" }.toList() }
    }

    private fun assertNone(what: String, hits: List<Hit>) {
        assertTrue(
            "$what:\n" + hits.joinToString("\n") { "  ${it.file.relativeTo(repoRoot!!)}:${it.line}  ${it.text.take(150)}" },
            hits.isEmpty(),
        )
    }

    /** Code that turns a failure into a message for a person: UI modules and core's controllers (not parsers, log plumbing or model code). */
    private fun uiSources(): List<File> = sources().filter {
        val path = it.invariantSeparatorsPath
        !path.contains("/core/src/") || path.contains("/controller/")
    }

    private fun isComment(line: String): Boolean = line.trim().let { it.startsWith("//") || it.startsWith("*") || it.startsWith("/*") }

    /** Index of the line where the brace opened on [start] closes, or the last line if the file ends first. */
    private fun closingLine(lines: List<String>, start: Int, alreadyOpen: Int = 0): Int {
        var depth = alreadyOpen
        for (j in start until lines.size) {
            depth += lines[j].count { it == '{' } - lines[j].count { it == '}' }
            if (depth <= 0 && (j > start || alreadyOpen == 0)) return j
        }
        return lines.lastIndex
    }

    // ------------------------------------------------------------------------------------------------------------

    @Test
    fun callersNeverRelyOnTheDefaultGenericFallback() {
        // In core controllers `userMessageLogged("...")` is the member taking the fallback itself; outside core the same call shape is
        // the free function (tag, fallback), so a lone argument there is a tag and the fallback is the generic default.
        val always = listOf(Regex("""userMessageLogged\(\)"""), Regex("""\.userMessage\(\)"""))
        val tagOnly = Regex("""userMessageLogged\("[^"]*"\)""")
        val hits = sources().filterNot { it.name in setOf("ResultLogging.kt", "UserFacingErrorHandler.kt", "ScreenController.kt", "ErrorClassifier.kt") }
            .flatMap { file ->
                val outsideCore = !file.invariantSeparatorsPath.contains("/core/src/")
                file.readLines().mapIndexedNotNull { i, line ->
                    val bad = always.any { it.containsMatchIn(line) } || (outsideCore && tagOnly.containsMatchIn(line))
                    if (!isComment(line) && bad) Hit(file, i + 1, line.trim()) else null
                }
            }
        assertNone("A failure is shown with the default 'Something went wrong' fallback -- pass \"Couldn't <what the user was doing>.\"", hits)
    }

    @Test
    fun aCatchBlockNeverSwallowsAFailure() {
        // Anything that classifies (userMessageLogged / ErrorClassifier / FileReadErrors / describe), logs (CmsLog / orLogCritical),
        // hands the failure on (throw / reportFailure / reportPickFailure) or is a pure cancellation rethrow is fine.
        val handled = Regex("""userMessageLogged|orLogCritical|isSuccessLogged|CmsLog|ErrorClassifier|FileReadErrors|describe\(|throw |reportFailure|reportPickFailure|reportPhotoPickFailure|userMessage\(|Outcome\.Error\([^"]*\b\w+\)""")
        val hits = mutableListOf<Hit>()
        for (file in uiSources()) {
            val lines = file.readLines()
            lines.forEachIndexed { i, line ->
                val m = Regex("""catch \((\w+): (\w+)\)\s*\{\s*$""").find(line) ?: return@forEachIndexed
                if (m.groupValues[2] == "CancellationException" || isComment(line)) return@forEachIndexed
                val end = closingLine(lines, i + 1, alreadyOpen = 1)
                val body = lines.subList(i + 1, end).filterNot(::isComment).joinToString("\n")
                if (!handled.containsMatchIn(body)) hits += Hit(file, i + 1, line.trim())
            }
        }
        assertNone("catch block swallows the failure -- classify it (userMessageLogged), log it (CmsLog/orLogCritical) or rethrow", hits)
    }

    @Test
    fun aCatchBlockNeverShowsAFixedGenericSentence() {
        // A message written straight into UI state from inside a catch must carry the reason (a variable/interpolation), not a
        // fixed string that would read the same for every cause.
        val assign = Regex("""(\w*[Ee]rror\w*|\w*[Mm]essage\w*|_?notice)(\.value)?\s*=\s*"([^"$]*)"""")
        val hits = mutableListOf<Hit>()
        for (file in uiSources()) {
            val lines = file.readLines()
            lines.forEachIndexed { i, line ->
                if (!Regex("""catch \((\w+): (\w+)\)\s*\{\s*$""").containsMatchIn(line) || isComment(line)) return@forEachIndexed
                val end = closingLine(lines, i + 1, alreadyOpen = 1)
                for (j in i + 1 until end) {
                    val m = assign.find(lines[j]) ?: continue
                    if (!isComment(lines[j]) && m.groupValues[3].isNotBlank()) hits += Hit(file, j + 1, lines[j].trim())
                }
            }
        }
        assertNone("A catch block sets a fixed message that ignores the cause -- classify the exception instead", hits)
    }

    @Test
    fun anOnFailureBlockActuallyUsesTheFailure() {
        // `.onFailure { }`, `.onFailure { _ -> }` or a body that never looks at the exception swallows it just as surely as an
        // empty catch. The block must use its parameter (`it` or the name given) -- to classify, log, store or rethrow it.
        val hits = mutableListOf<Hit>()
        for (file in sources()) {
            val lines = file.readLines()
            lines.forEachIndexed { i, line ->
                if (isComment(line)) return@forEachIndexed
                val at = line.indexOf(".onFailure")
                if (at < 0) return@forEachIndexed
                val open = line.indexOf('{', at)
                if (open < 0) return@forEachIndexed // onFailure(function reference): the function receives the failure
                // Collect the block text from the opening brace to its matching close, possibly across lines.
                val text = StringBuilder()
                var depth = 0
                var done = false
                var j = i
                var col = open
                while (j < lines.size && !done) {
                    val current = lines[j]
                    for (k in col until current.length) {
                        val c = current[k]
                        if (c == '{') depth++
                        if (c == '}') depth--
                        text.append(c)
                        if (depth == 0) { done = true; break }
                    }
                    if (!done) text.append('\n')
                    j++
                    col = 0
                }
                val body = text.toString().removePrefix("{").removeSuffix("}").trim()
                val declared = Regex("""^(\w+)\s*->""").find(body)
                val param = declared?.groupValues?.get(1) ?: "it"
                val usage = if (declared != null) body.removePrefix(declared.value) else body
                if (param == "_" || !Regex("""\b$param\b""").containsMatchIn(usage)) hits += Hit(file, i + 1, line.trim())
            }
        }
        assertNone("onFailure block never uses the failure -- classify it (userMessageLogged), log it (CmsLog/orLogCritical), store or rethrow it", hits)
    }

    @Test
    fun aDroppedRunCatchingSaysWhy() {
        val hits = mutableListOf<Hit>()
        for (file in sources()) {
            val lines = file.readLines()
            lines.forEachIndexed { i, line ->
                val s = line.trim()
                if (!s.startsWith("runCatching {")) return@forEachIndexed
                val end = closingLine(lines, i)
                // Chained on the same line as the closing brace (`}.onFailure { ... }`) or on the next one: the result is used.
                val tail = lines[end].substringAfterLast('}').trim()
                val next = lines.drop(end + 1).firstOrNull { it.isNotBlank() }?.trim().orEmpty()
                val closer = lines[end].trim()
                val chained = closer.contains("}.") || tail.isNotEmpty() || next.startsWith(".")
                if (chained) return@forEachIndexed
                // A multi-line block that is the last thing in a lambda is that lambda's value (async { runCatching { ... } }), so it is
                // only a dropped result when more statements follow. Single-line ones are always judged.
                val multiLine = end > i
                if (multiLine && (next.startsWith("}") || next.startsWith(")") || next.isEmpty())) return@forEachIndexed
                val why = (maxOf(0, i - 4) until i).map { lines[it].trim() }.any { it.startsWith("//") && it.contains("best-effort", ignoreCase = true) }
                if (!why) hits += Hit(file, i + 1, s)
            }
        }
        assertNone("runCatching result is dropped with no explanation -- handle the failure, or add a '// Best-effort: <why>' comment above", hits)
    }
}
