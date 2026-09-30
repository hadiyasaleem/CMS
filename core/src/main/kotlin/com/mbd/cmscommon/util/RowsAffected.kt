package com.mbd.cmscommon.util

import io.github.jan.supabase.postgrest.result.PostgrestResult
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject

/**
 * PostgREST answers an `UPDATE`/`DELETE` that matched **no row** with a plain success -- and row-level security makes that
 * easy to hit: a row the caller may not change is silently filtered out, not refused. A repository that then updates its
 * local cache and reports success has told the user something that did not happen.
 *
 * Build the write with `select()` so PostgREST returns the changed rows, then call [requireAffected]:
 *
 * ```
 * postgrest.from(T).update({ set("is_deleted", true) }) {
 *     select()
 *     filter { eq("id", id) }
 * }.requireAffected(onNone = { dao.deleteById(id) })
 * ```
 *
 * See Documentation/remote-write-audit.md for which writes need it (soft-deletes, approvals, rejections) and which don't.
 */
const val ROW_GONE_MESSAGE = "That item was already changed or removed. Refresh and try again."

/** How many rows a `return=representation` response [body] holds (an array of rows, or a single object). */
fun rowsIn(body: String): Int {
    if (body.isBlank()) return 0
    return when (val element = runCatching { Json.parseToJsonElement(body) }.getOrNull()) {
        is JsonArray -> element.size
        is JsonObject -> 1
        is JsonNull, null -> 0
        else -> 0
    }
}

/**
 * Throws [CmsException.NotFound] when [body] holds no rows. [onNone] runs first so a stale local copy (the reason the user
 * asked for the change in the first place) is dropped instead of lingering as an undeletable ghost.
 */
suspend fun requireRows(body: String, message: String = ROW_GONE_MESSAGE, onNone: suspend () -> Unit = {}) {
    if (rowsIn(body) == 0) {
        onNone()
        throw CmsException.NotFound(message)
    }
}

/** [requireRows] for the result of a write built with `select()`. */
suspend fun PostgrestResult.requireAffected(message: String = ROW_GONE_MESSAGE, onNone: suspend () -> Unit = {}) =
    requireRows(data, message, onNone)
