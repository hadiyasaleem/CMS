package com.mbd.cmscommon.controller

import java.time.LocalDate

/**
 * Validation that both a screen (to warn while typing) and its controller (the real check) apply. Keeping the wording in
 * one function means the same mistake reads the same way wherever it is caught.
 */

/**
 * Why the term [startText]/[endText] can't be saved, or null when they can. Both are optional (blank = not set), must be
 * `YYYY-MM-DD` dates when given, and the term can't end before it starts.
 */
fun termDatesError(startText: String, endText: String): String? {
    val start = startText.trim().takeIf { it.isNotEmpty() }?.let {
        runCatching { LocalDate.parse(it) }.getOrNull() ?: return "Enter the start date as YYYY-MM-DD (for example 2026-09-01)."
    }
    val end = endText.trim().takeIf { it.isNotEmpty() }?.let {
        runCatching { LocalDate.parse(it) }.getOrNull() ?: return "Enter the end date as YYYY-MM-DD (for example 2027-01-15)."
    }
    if (start != null && end != null && start.isAfter(end)) return "The term can't end ($end) before it starts ($start)."
    return null
}
