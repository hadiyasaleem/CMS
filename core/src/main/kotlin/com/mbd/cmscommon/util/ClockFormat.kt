package com.mbd.cmscommon.util

import java.time.LocalDate
import java.time.LocalTime

private val CLOCK_WITH_SECONDS = Regex("^(\\d{1,2}:\\d{2}):\\d{2}(\\.\\d+)?$")

/** Postgres `time` columns round-trip as "HH:mm:ss"; every screen shows "HH:mm". */
fun clockDisplay(value: String?): String {
    val trimmed = value?.trim().orEmpty()
    return CLOCK_WITH_SECONDS.find(trimmed)?.groupValues?.get(1) ?: trimmed
}

fun parseClock(value: String?): LocalTime? =
    value?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { LocalTime.parse(clockDisplay(it)) }.getOrNull() }

fun parseIsoDate(value: String?): LocalDate? =
    value?.trim()?.takeIf { it.isNotEmpty() }?.let { runCatching { LocalDate.parse(it) }.getOrNull() }

/** True when both ends are set and [end] comes before [start]; a blank end is not an error. */
fun isDateRangeReversed(start: String?, end: String?): Boolean {
    val s = parseIsoDate(start) ?: return false
    val e = parseIsoDate(end) ?: return false
    return e.isBefore(s)
}

/** True when both times are set and [end] is not strictly after [start] (no zero or negative spans). */
fun isTimeRangeInvalid(start: String?, end: String?): Boolean {
    val s = parseClock(start) ?: return false
    val e = parseClock(end) ?: return false
    return !e.isAfter(s)
}
