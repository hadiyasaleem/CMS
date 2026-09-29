package com.mbd.cmscommon.util

import java.util.concurrent.CancellationException

/** One named part of a screen or sync that failed, and the exception behind it. */
data class LoadFailure(val what: String, val cause: Throwable)

/**
 * Hands a coroutine cancellation back to the caller instead of letting it be recorded as a failure. `runCatching` catches
 * [CancellationException] like any other throwable; a cancelled sync step is not "unexpected error, Ref XXXX", and swallowing
 * it would also break structured concurrency. Use it between `runCatching { }` and `.onFailure { }` / result inspection.
 */
fun <T> Result<T>.rethrowCancellation(): Result<T> {
    val error = exceptionOrNull()
    if (error is CancellationException) throw error
    return this
}

/**
 * What a full data refresh did: empty [failures] means everything synced; otherwise [message] says which parts failed and why.
 * Cancellations are never failures, even if one is passed in.
 */
class SyncReport(failures: List<LoadFailure>) {
    val failures: List<LoadFailure> = failures.filterNot { it.cause is CancellationException }
    val successful: Boolean get() = failures.isEmpty()
    val message: String? by lazy { FailureSummary.describe(this.failures, prefix = "Couldn't refresh") }
}

/**
 * Turns "several things loaded in parallel, some failed" into ONE message that names them and says why,
 * e.g. `Couldn't load attendance and marks (no connection); fees (unexpected error, Ref 1A2B).`
 * instead of the first failure's message (or a generic "Some summaries could not be loaded") for all of them.
 *
 * Failures that share a reason are grouped so a dropped connection reads as one sentence, not one per item.
 * Unexpected failures are written to [CmsLog] (with the full throwable) when a [tag] is given -- expected ones
 * (offline, permission, ...) stay on screen only.
 */
object FailureSummary {

    /** The failures among named [results]; cancellations are not failures. */
    fun of(results: List<Pair<String, Result<*>>>): List<LoadFailure> =
        results.mapNotNull { (what, result) ->
            result.exceptionOrNull()?.takeUnless { it is CancellationException }?.let { LoadFailure(what, it) }
        }

    /** [prefix] is the opening of the sentence: "Couldn't load", "Couldn't refresh". Null when nothing failed. */
    fun describe(allFailures: List<LoadFailure>, tag: String? = null, prefix: String = "Couldn't load"): String? {
        val failures = allFailures.filterNot { it.cause is CancellationException }
        if (failures.isEmpty()) return null

        val groups = LinkedHashMap<String, MutableList<String>>()
        for (failure in failures) {
            val classified = ErrorClassifier.classify(failure.cause)
            if (tag != null && classified.severity == Severity.CRITICAL) {
                CmsLog.critical("$tag.${failure.what}", classified.userMessage, failure.cause)
            }
            groups.getOrPut(reasonOf(classified)) { mutableListOf() }.let { if (failure.what !in it) it += failure.what }
        }
        val parts = groups.map { (reason, names) -> "${joinNames(names)} ($reason)" }
        return "$prefix ${parts.joinToString("; ")}."
    }

    /** A short phrase for why [classified] happened, fit for inside parentheses. */
    private fun reasonOf(classified: ClassifiedError): String {
        val message = classified.userMessage
        return when {
            message.startsWith("Unable to connect") -> "no connection"
            message.contains("took too long") -> "timed out"
            message.startsWith("Too many") -> "too many requests"
            message.contains("temporarily unavailable") -> "server unavailable"
            classified.kind == ErrorKind.PERMISSION -> "no permission"
            classified.kind == ErrorKind.AUTH -> "sign-in expired"
            classified.kind == ErrorKind.NOT_FOUND -> "not found"
            classified.kind == ErrorKind.UNEXPECTED -> "unexpected error, Ref ${classified.reference}"
            else -> message.trimEnd('.').replaceFirstChar { it.lowercase() }.take(70)
        }
    }

    /** "a", "a and b", "a, b and c", "a, b, c, d and 2 more". */
    private fun joinNames(names: List<String>): String = when {
        names.size == 1 -> names[0]
        names.size in 2..4 -> names.dropLast(1).joinToString(", ") + " and " + names.last()
        else -> names.take(4).joinToString(", ") + " and ${names.size - 4} more"
    }
}
