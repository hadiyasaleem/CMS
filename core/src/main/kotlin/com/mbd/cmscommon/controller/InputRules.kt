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

/** Why a fine of [amount] can't be issued, or null. [amount] is null when the typed text isn't a number. */
fun fineAmountError(amount: Double?): String? = when {
    amount == null -> "Enter the fine amount as a number, for example 500."
    !amount.isFinite() || amount <= 0.0 -> "Fine amount must be greater than zero."
    else -> null
}
