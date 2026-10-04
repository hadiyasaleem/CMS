package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.shiftCapacity
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile

/*
 * Roster rules for one session: All / Morning / Evening tabs, adding a student to exactly one shift, and
 * class roles per shift. Roll numbers are free-form; a shift simply holds up to shiftCapacity(session)
 * students, mirrored by the database's fn_enforce_roster_cap.
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

/** Why [roll] can't be added to [shift], or null. Checks the shift's seat cap and duplicate roll numbers. */
fun addStudentError(session: AcademicSession?, shift: Session, roll: String, students: List<SessionStudent>): String? {
    val normalized = roll.trim().uppercase()
    if (normalized.isBlank()) return "Enter a roll number."
    if (session != null && !session.shiftMode.allows(shift)) return "This session does not run the ${shift.label} shift."
    if (session != null && students.count { it.shift == shift } >= shiftCapacity(session)) {
        return "${shift.label} is full (${shiftCapacity(session)} students)."
    }
    if (students.any { it.rollNumber.equals(normalized, ignoreCase = true) }) return "Roll number $normalized is already enrolled in this session."
    return null
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

/** How many more students [shift] can take in [session], e.g. "12 of 50 seats used in Morning." */
fun shiftCapacityHint(session: AcademicSession?, shift: Session, students: List<SessionStudent>): String? {
    session ?: return null
    val used = students.count { it.shift == shift }
    return "$used of ${shiftCapacity(session)} seats used in ${shift.label}."
}
