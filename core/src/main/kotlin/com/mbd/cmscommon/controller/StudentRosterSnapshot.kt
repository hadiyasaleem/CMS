package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionStudent

data class StudentRosterSnapshot(
    val enrolled: Int,
    val linked: Int,
    val unlinked: Int,
    val gradeRecords: Int,
    val missingGradeRecords: Int,
    val averageCgpa: Double?,
)

fun validAcademicGrade(value: Double?): Double? = value?.takeIf { it in 0.0..4.0 }

fun studentRosterSnapshot(students: List<SessionStudent>): StudentRosterSnapshot {
    val validCgpa = students.mapNotNull { validAcademicGrade(it.cgpa) }
    val gradeRecords = students.count { validAcademicGrade(it.gpa) != null || validAcademicGrade(it.cgpa) != null }
    val linked = students.count { it.linkedEmail.isNotBlank() }

    return StudentRosterSnapshot(
        enrolled = students.size,
        linked = linked,
        unlinked = students.size - linked,
        gradeRecords = gradeRecords,
        missingGradeRecords = students.size - gradeRecords,
        averageCgpa = validCgpa.takeIf { it.isNotEmpty() }?.average(),
    )
}
