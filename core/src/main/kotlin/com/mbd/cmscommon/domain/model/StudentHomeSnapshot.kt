package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.controller.WeakSubject
import java.util.Locale

data class StudentHomeSnapshot(
    val name: String,
    val rollNumber: String,
    val programLine: String,
    val semesterLabel: String,
    val gpaLabel: String,
    val overallAttendance: Float,
    val subjectCount: Int,
    /** Today's lectures, sorted by start time -- mirrors the teacher app's own "Today's classes" list. */
    val todaysClasses: List<SessionPeriod>,
    /** The id of whichever entry in [todaysClasses] is current/next, or null once today's schedule is done. */
    val nextClassId: String?,
    val weakestSubject: WeakSubject?,
)

fun studentHomeSnapshot(
    name: String,
    rollNumber: String,
    session: AcademicSession?,
    gpa: Double?,
    cgpa: Double?,
    overallAttendance: Float,
    subjectCount: Int,
    todaysClasses: List<SessionPeriod>,
    nextClassId: String?,
    weakestSubject: WeakSubject?,
    shift: Session? = null,
): StudentHomeSnapshot {
    val displayName = name.trim().ifBlank { "Student" }

    val programLine = if (session == null) {
        "Academic dashboard"
    } else {
        val program = session.programName?.takeIf { it.isNotBlank() }
        val semester = "Semester ${session.currentSemester}"
        val label = session.label
        val shiftLabel = shift?.label ?: session.shiftMode.label
        listOfNotNull(program, semester, label, shiftLabel).joinToString(" / ").ifBlank { "Academic dashboard" }
    }

    val semesterLabel = session?.let { "${it.currentSemester} of 8" } ?: "-"

    val gpaLabel = if (cgpa != null) {
        val gpaText = gpa?.let { "%.2f".format(it) } ?: "-"
        "%.2f / %s".format(cgpa, gpaText)
    } else {
        "No CGPA yet"
    }

    return StudentHomeSnapshot(
        name = displayName,
        rollNumber = rollNumber.trim(),
        programLine = programLine,
        semesterLabel = semesterLabel,
        gpaLabel = gpaLabel,
        overallAttendance = overallAttendance.coerceIn(0f, 100f),
        subjectCount = subjectCount.coerceAtLeast(0),
        todaysClasses = todaysClasses,
        nextClassId = nextClassId,
        weakestSubject = weakestSubject,
    )
}
