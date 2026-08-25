package com.mbd.cmscommon.data.remote

import java.time.Instant
import java.time.OffsetDateTime

object PgTime {
    fun parse(iso: String?): Instant? {
        if (iso == null) return null
        return runCatching { OffsetDateTime.parse(iso).toInstant() }.getOrNull()
    }

    fun parseOrEpoch(iso: String?): Instant = parse(iso) ?: Instant.EPOCH

    fun format(instant: Instant?): String? = instant?.toString()
}
