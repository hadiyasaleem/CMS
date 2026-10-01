package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.LocalDate
import java.util.Locale

/** Default plan for a new fee structure: Morning pays annually, Evening per semester (editable). */
fun recommendedFeeCadence(shift: Session): FeeType =
    if (shift == Session.MORNING) FeeType.ANNUAL else FeeType.SEMESTER

/**
 * The shift whose fee structure is shown: the tab the admin picked when the session runs it, otherwise the
 * session's first shift. There is no combined view -- Morning and Evening can use different plans.
 */
fun feeTabShift(session: AcademicSession?, picked: Session?): Session =
    picked?.takeIf { session == null || session.runs(it) } ?: session?.shifts?.firstOrNull() ?: Session.MORNING

/** The fee tabs: only the shifts the session runs (a single-shift session gets one tab). */
fun feeTabs(session: AcademicSession?): List<Session> = session?.shifts ?: listOf(Session.MORNING)

/** "Morning Rs 50000 (annual) · Evening not set" for the session overview; a one-shift session shows one entry. */
fun feeSummaryLine(session: AcademicSession?, fees: List<SessionFeeStructure>): String {
    val shifts = feeTabs(session)
    fun describe(fee: SessionFeeStructure?) = fee?.let {
        "Rs ${"%.0f".format(it.totalAmount)} (${if (it.cadence == FeeType.ANNUAL) "annual" else "per semester"})"
    } ?: "not set"
    if (shifts.size == 1) return fees.firstOrNull { it.shift == shifts.single() }?.let(::describe) ?: "Fee structure not configured"
    return shifts.joinToString(" · ") { shift -> "${shift.label} ${describe(fees.firstOrNull { it.shift == shift })}" }
}

/**
 * Checks what an admin typed for a fee structure and builds it; throws a validation error naming the first problem.
 * Used for a session's own structure and for the college-wide base alike ([sessionId] is blank for the base).
 */
fun buildFeeStructure(
    sessionId: String,
    shift: Session,
    cadence: FeeType,
    heads: List<FeeHead>,
    academicYear: String,
    dueDate: String,
    paymentNote: String,
): SessionFeeStructure {
    val normalizedHeads = heads.map { it.copy(label = it.label.trim()) }
    requireValid(normalizedHeads.isNotEmpty()) { "Add at least one fee head before saving." }
    requireValid(normalizedHeads.all { it.label.isNotBlank() }) { "Every fee head needs a label." }
    requireValid(normalizedHeads.all { it.amount > 0.0 }) { "Every fee amount must be greater than zero." }
    requireValid(normalizedHeads.map { it.label.lowercase(Locale.ROOT) }.distinct().size == normalizedHeads.size) {
        "Fee head labels must be unique."
    }

    val year = academicYear.trim()
    FieldValidators.academicYearError(year).orThrowValidation()

    val due = dueDate.trim()
    if (due.isNotBlank()) {
        requireValid(runCatching { LocalDate.parse(due) }.isSuccess) { "Due date must use YYYY-MM-DD format." }
    }
    requireValid(paymentNote.trim().length <= 1000) { "Payment instructions must not exceed 1,000 characters." }

    return SessionFeeStructure(
        sessionId = sessionId,
        shift = shift,
        cadence = cadence,
        heads = normalizedHeads,
        academicYear = year.takeIf { it.isNotBlank() },
        dueDate = due.takeIf { it.isNotBlank() },
        paymentNote = paymentNote.trim().takeIf { it.isNotBlank() },
    )
}
