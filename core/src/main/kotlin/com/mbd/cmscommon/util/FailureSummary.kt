package com.mbd.cmscommon.util

import java.util.concurrent.CancellationException

/** One named part of a screen or sync that failed, and the exception behind it. */
data class LoadFailure(val what: String, val cause: Throwable)

/** What a full data refresh did: empty [failures] means everything synced; otherwise [message] says which parts failed and why. */
class SyncReport(val failures: List<LoadFailure>) {
    val successful: Boolean get() = failures.isEmpty()
    val message: String? by lazy { FailureSummary.describe(failures, prefix = "Couldn't refresh") }
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
    fun describe(failures: List<LoadFailure>, tag: String? = null, prefix: String = "Couldn't load"): String? {
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
