package com.mbd.cmscommon.util

import io.github.jan.supabase.postgrest.exception.PostgrestRestException

/**
 * A Postgres/PostgREST failure broken into the parts [ErrorClassifier] needs: the SQLSTATE [code],
 * the server's [message] (for `RAISE EXCEPTION` this is the developer-written text), and the
 * optional [details]/[hint]. The `constraint`/`table`/`column`/key accessors pull structured facts
 * out of the message text Postgres itself generates.
 */
data class PostgresError(
    val code: String?,
    val message: String,
    val details: String? = null,
    val hint: String? = null,
) {
    /** The violated constraint or index name, e.g. `uq_session_slot`. */
    val constraint: String? get() = CONSTRAINT.find(message)?.groupValues?.get(1)

    /** The table the failing statement targeted (the child table for a foreign-key delete block). */
    val table: String?
        get() = FOREIGN_KEY.find(message)?.let { it.groupValues[4].ifBlank { it.groupValues[2] } }
            ?: TABLE_QUOTED.find(message)?.groupValues?.get(1)
            ?: RELATION.find(message)?.groupValues?.get(1)
            ?: PERMISSION_DENIED.find(message)?.groupValues?.get(1)

    /** For a foreign-key violation: true when a delete/update was blocked by rows that still reference this one. */
    val isBlockedByDependents: Boolean get() = FOREIGN_KEY.find(message)?.groupValues?.get(1) == "update or delete"

    /** For a foreign-key violation: the table that owns the row being deleted/updated (or inserted against). */
    val foreignKeyParentTable: String? get() = FOREIGN_KEY.find(message)?.groupValues?.get(2)

    /** The `null value in column "x"` column of a not-null violation. */
    val nullColumn: String? get() = NULL_COLUMN.find(message)?.groupValues?.get(1)

    /** `Key (a, b)=(1, 2)` from [details] as a column -> value map. */
    val keyValues: Map<String, String>
        get() {
            val match = KEY.find(details.orEmpty()) ?: return emptyMap()
            val columns = match.groupValues[1].split(",").map { it.trim() }
            val values = match.groupValues[2].split(",").map { it.trim() }
            return if (columns.size == values.size) columns.zip(values).toMap() else emptyMap()
        }

    private companion object {
        val CONSTRAINT = Regex("""constraint "([^"]+)"""")
        val FOREIGN_KEY = Regex("""(insert or update|update or delete) on table "([^"]+)" violates foreign key constraint "([^"]+)"(?: on table "([^"]+)")?""")
        val TABLE_QUOTED = Regex("""(?:on|for) table "([^"]+)"""")
        val RELATION = Regex("""(?:of|for) relation "([^"]+)"""")
        val PERMISSION_DENIED = Regex("""permission denied for (?:table |relation )?"?([a-z_0-9]+)"?""")
        val NULL_COLUMN = Regex("""null value in column "([^"]+)"""")
        val KEY = Regex("""Key \(([^)]+)\)=\(([^)]*)\)""")
    }
}

object PostgresErrorParser {
    private val JSON_CODE = Regex(""""code"\s*:\s*"([0-9A-Za-z]{5,8})"""")
    private val JSON_MESSAGE = Regex(""""message"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    private val JSON_DETAILS = Regex(""""details"\s*:\s*"((?:[^"\\]|\\.)*)"""")
    private val JSON_HINT = Regex(""""hint"\s*:\s*"((?:[^"\\]|\\.)*)"""")

    /**
     * Finds a Postgres error anywhere in [error]'s cause chain. Prefers supabase-kt's typed
     * [PostgrestRestException] (code/details/hint are separate fields), then falls back to text:
     * a PostgREST JSON body, or the constraint wording Postgres itself uses (the SQLSTATE is
     * inferred from it). Returns null when the failure is not recognisably a database error.
     */
    fun parse(error: Throwable): PostgresError? {
        val causes = generateSequence(error) { it.cause }.take(6).toList()

        causes.filterIsInstance<PostgrestRestException>().firstOrNull()?.let { typed ->
            return PostgresError(typed.code, typed.error, typed.details, typed.hint)
        }

        val raw = causes.mapNotNull { it.message }.joinToString("\n")
        if (raw.isBlank()) return null

        val jsonMessage = JSON_MESSAGE.find(raw)?.groupValues?.get(1)?.let(::unescape)
        val message = jsonMessage ?: raw
        val code = JSON_CODE.find(raw)?.groupValues?.get(1) ?: inferCode(message) ?: return null
        return PostgresError(
            code = code,
            message = message,
            details = JSON_DETAILS.find(raw)?.groupValues?.get(1)?.let(::unescape),
            hint = JSON_HINT.find(raw)?.groupValues?.get(1)?.let(::unescape),
        )
    }

    private fun unescape(value: String): String = value.replace("\\\"", "\"").replace("\\n", "\n").replace("\\\\", "\\")

    private fun inferCode(message: String): String? {
        val text = message.lowercase()
        return when {
            "violates unique constraint" in text || "duplicate key value" in text -> "23505"
            "violates foreign key constraint" in text -> "23503"
            "violates check constraint" in text -> "23514"
            "violates not-null constraint" in text -> "23502"
            "value too long for type" in text -> "22001"
            "invalid input syntax for type" in text -> "22P02"
            "violates row-level security policy" in text || "permission denied for" in text -> "42501"
            else -> null
        }
    }
}
