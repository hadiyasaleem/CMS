package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session

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
