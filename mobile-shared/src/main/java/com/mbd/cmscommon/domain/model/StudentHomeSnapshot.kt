package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.controller.NextClass
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
    val lecturesToday: Int,
    val nextClass: NextClass?,
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
    lecturesToday: Int,
    nextClass: NextClass?,
    weakestSubject: WeakSubject?,
): StudentHomeSnapshot {
    val displayName = name.trim().ifBlank { "Student" }

    val programLine = if (session == null) {
        "Academic dashboard"
    } else {
        val program = session.programName?.takeIf { it.isNotBlank() }
        val semester = "Semester ${session.currentSemester}"
        val label = session.label
        val shift = session.shift.name.lowercase(Locale.ROOT).replaceFirstChar { it.uppercase(Locale.ROOT) }
        listOfNotNull(program, semester, label, shift).joinToString(" / ").ifBlank { "Academic dashboard" }
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
        lecturesToday = lecturesToday.coerceAtLeast(0),
        nextClass = nextClass,
        weakestSubject = weakestSubject,
    )
}
