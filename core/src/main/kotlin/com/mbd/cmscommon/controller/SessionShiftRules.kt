package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.rollBlockError

/*
 * Rules behind the create-session and edit-session forms. The database enforces the same limits
 * (academic_sessions constraints and trg_session_shift_change); these give the admin a clear reason
 * before anything is sent.
 */

/**
 * The max-students text after the Morning/Evening boxes change: re-filled with the default for the new
 * selection (50 for one shift, 100 for both). With no box ticked the admin's text is left alone.
 */
fun capacityForShiftSelection(shifts: Set<Session>, current: String): String =
    ShiftMode.of(shifts)?.let { AcademicSession.defaultMaxStudents(it).toString() } ?: current

/** Why [raw] is not a usable capacity, or null. [enrolled] students must still fit. */
fun capacityError(raw: String, enrolled: Int = 0): String? {
    val value = raw.trim().toIntOrNull()
    return when {
        value == null || value !in 1..AcademicSession.MAX_CAPACITY -> "Student capacity must be between 1 and ${AcademicSession.MAX_CAPACITY}."
        value < enrolled -> "Student capacity can't be below the $enrolled student(s) already enrolled."
        else -> null
    }
}

/** Why a new session can't be created, or null. One session per department, intake year and program type. */
fun createSessionError(
    startYear: Int?,
    shifts: Set<Session>,
    capacityText: String,
    existing: List<AcademicSession>,
    programType: ProgramType = ProgramType.BS,
): String? = when {
    startYear == null -> "Select the intake year."
    ShiftMode.of(shifts) == null -> "Tick Morning, Evening, or both."
    existing.any { it.startYear == startYear && it.programType == programType } ->
        "A $startYear–${programType.endYear(startYear)} ${programType.label} session already exists in this department."
    else -> capacityError(capacityText)
}

/** Student counts per shift, both shifts always present (zero when empty). */
fun studentCountsByShift(students: List<SessionStudent>): Map<Session, Int> =
    Session.entries.associateWith { shift -> students.count { it.shift == shift } }

/**
 * Why [session] can't switch to [target] with capacity [maxStudents], or null when it can.
 * - Adding a shift is always fine.
 * - Dropping a shift is blocked while it still has students, a fee structure, timetable periods or a
 *   datesheet. The message says what must be moved or removed first.
 * - Every enrolled student must still fit their shift's roll-number block and the new capacity.
 */
fun shiftModeChangeError(
    session: AcademicSession,
    target: ShiftMode,
    maxStudents: Int,
    students: List<SessionStudent>,
    fees: List<SessionFeeStructure>,
    periods: List<SessionPeriod>,
    datesheets: List<Datesheet>,
): String? {
    val dropped = session.shifts.filterNot { target.allows(it) }
    for (shift in dropped) {
        val parts = buildList {
            students.count { it.shift == shift }.takeIf { it > 0 }?.let { add("$it student(s)") }
            if (fees.any { it.sessionId == session.sessionId && it.shift == shift }) add("a fee structure")
            periods.count { it.sessionId == session.sessionId && it.shift == shift }.takeIf { it > 0 }?.let { add("$it timetable period(s)") }
            datesheets.count { it.sessionId == session.sessionId && it.shift == shift }.takeIf { it > 0 }?.let { add("$it datesheet(s)") }
        }
        if (parts.isNotEmpty()) {
            return "Cannot switch to ${target.label} only: the ${shift.label} shift still has ${parts.joinToString(", ")}. Move or remove them first."
        }
    }
    capacityError(maxStudents.toString(), students.size)?.let { return it }
    for (student in students) {
        rollBlockError(target, maxStudents, student.shift, student.rollNumber)?.let {
            return "$it Adjust the capacity or renumber that student first."
        }
    }
    return null
}

/**
 * Curriculum is per session, not per shift: subjects (with their outline topics), term dates and the
 * current semester are shared by both shifts. Returns the note the curriculum screen shows for a two-shift
 * session, or null for a single-shift one.
 */
fun sharedCurriculumNote(session: AcademicSession?): String? =
    if (session?.shiftMode == ShiftMode.BOTH) "Shared by the Morning and Evening shifts: subjects, topics and term dates are entered once." else null

/** Confirmation text for promoting (or graduating) a session; one promotion moves every shift it runs. */
fun promotionConfirmText(session: AcademicSession?): String {
    val semester = session?.currentSemester ?: 1
    val who = if (session?.shiftMode == ShiftMode.BOTH) "both the Morning and Evening shifts" else "the whole class"
    return if (semester >= AcademicSession.TOTAL_SEMESTERS) {
        "This marks $who as graduated and archives the session. This cannot be undone."
    } else {
        "This promotes $who from semester $semester to ${semester + 1} and removes that semester's exam papers."
    }
}
