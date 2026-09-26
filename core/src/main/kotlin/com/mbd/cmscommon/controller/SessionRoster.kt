package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.morningCapacity
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.nextRollFor
import com.mbd.cmscommon.domain.model.rollBlockError
import com.mbd.cmscommon.domain.model.rollPrefix

/*
 * Roster rules for one session: All / Morning / Evening tabs, adding a student to exactly one shift, and
 * class roles per shift. A student's shift is fixed by their roll number's serial (Morning 1..capacity,
 * Evening above it), so the database's roll_block_error and these helpers always agree.
 */

/** Tabs for the roster: `null` (All) first, then each shift the session runs. */
fun rosterTabs(session: AcademicSession?): List<Session?> = listOf<Session?>(null) + session?.shifts.orEmpty()

/** The students under [tab]; `null` means All. */
fun studentsForTab(students: List<SessionStudent>, tab: Session?): List<SessionStudent> =
    if (tab == null) students else students.filter { it.shift == tab }

/** "All (38)", "Morning (20)", "Evening (18)". */
fun rosterTabLabel(tab: Session?, students: List<SessionStudent>): String =
    "${tab?.label ?: "All"} (${studentsForTab(students, tab).size})"

/**
 * The shift a new student starts in: the open shift tab when the session runs it, otherwise the
 * session's first shift. A single-shift session always gets its only shift (and the form locks it).
 */
fun defaultShiftForNewStudent(session: AcademicSession?, tab: Session?): Session =
    tab?.takeIf { session?.runs(it) == true } ?: session?.shifts?.firstOrNull() ?: Session.MORNING

/** The next free roll number in [shift]'s block ("IT-22-03", "IT-22-51"), or null when unknown/full. */
fun suggestedRollNumber(session: AcademicSession?, departmentCode: String?, shift: Session, students: List<SessionStudent>): String? {
    if (session == null || departmentCode.isNullOrBlank()) return null
    return nextRollFor(session, shift, students.map { it.rollNumber }, rollPrefix(departmentCode, session.startYear))
}

/** Why [roll] can't be added to [shift], or null. Checks seats, duplicates and the shift's roll block. */
fun addStudentError(session: AcademicSession?, shift: Session, roll: String, students: List<SessionStudent>): String? {
    val normalized = roll.trim().uppercase()
    if (session != null && students.size >= session.maxStudents) return "This session is full (${session.maxStudents} students)."
    if (students.any { it.rollNumber.equals(normalized, ignoreCase = true) }) return "Roll number $normalized is already enrolled in this session."
    return session?.let { rollBlockError(it, shift, normalized) }
}

/**
 * One CR and one GR per shift: why [edited] can't hold the role(s) it claims, or null. [classmates] are the
 * session's profiles (the edited student may be among them).
 */
fun classRoleConflict(edited: StudentProfile, classmates: List<StudentProfile>): String? {
    val others = classmates.filter { it.sessionId == edited.sessionId && it.shift == edited.shift && !it.rollNumber.equals(edited.rollNumber, ignoreCase = true) }
    others.firstOrNull { edited.isCr && it.isCr }?.let {
        return "${it.rollNumber} is already the ${edited.shift.label} CR. Remove that role first."
    }
    others.firstOrNull { edited.isGr && it.isGr }?.let {
        return "${it.rollNumber} is already the ${edited.shift.label} GR. Remove that role first."
    }
    return null
}

/** Why [edited]'s shift can't be saved for [session], or null. The roll number decides the shift. */
fun profileShiftError(session: AcademicSession?, edited: StudentProfile): String? =
    session?.let { rollBlockError(it, edited.shift, edited.rollNumber) }

/** Explains how roll numbers map to shifts for [session], e.g. "Morning uses serials 01–50; Evening starts at 51." */
fun rollBlockHint(session: AcademicSession?): String? {
    session ?: return null
    val cap = morningCapacity(session)
    val first = "01"
    val last = cap.toString().padStart(2, '0')
    val eveningStart = (cap + 1).toString().padStart(2, '0')
    return when (session.shiftMode) {
        ShiftMode.BOTH -> "Morning uses serials $first–$last; Evening starts at $eveningStart."
        ShiftMode.MORNING -> "Morning uses serials $first–$last."
        ShiftMode.EVENING -> "Evening uses serials from $eveningStart."
    }
}
