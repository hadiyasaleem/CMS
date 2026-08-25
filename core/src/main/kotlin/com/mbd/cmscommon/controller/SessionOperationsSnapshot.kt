package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent

data class SessionOperationsSnapshot(
    val enrolledStudents: Int,
    val availableSeats: Int,
    val linkedStudents: Int,
    val gradeRecords: Int,
    val totalSubjects: Int,
    val configuredSemesters: Int,
    val teachingPeriods: Int,
    val teachingDays: Int,
    val feeHeads: Int,
    val feeTotal: Double,
    val setupTasks: Int,
)

fun sessionOperationsSnapshot(
    session: AcademicSession,
    students: List<SessionStudent>,
    subjectCounts: Map<Int, Int>,
    periods: List<SessionPeriod>,
    fee: SessionFeeStructure?,
): SessionOperationsSnapshot {
    val uniqueLectures = periods
        .filter { it.periodType == PeriodType.LECTURE && it.courseCode.isNotBlank() }
        .distinctBy { it.id }

    val configuredSemesters = (1..8).count { (subjectCounts[it] ?: 0) > 0 }

    val setupTasks = listOf(
        session.programName.isNullOrBlank(),
        session.inchargeEmail.isNullOrBlank(),
        configuredSemesters < 8,
        uniqueLectures.isEmpty(),
        fee == null || fee.heads.isEmpty(),
    ).count { it }

    val availableSeats = (session.maxStudents - students.size).coerceAtLeast(0)
    val linkedStudents = students.count { it.linkedEmail.isNotBlank() }
    val gradeRecords = students.count { it.gpa != null || it.cgpa != null }
    val totalSubjects = subjectCounts.filterKeys { it in 1..8 }.values.sumOf { it.coerceAtLeast(0) }
    val teachingDays = uniqueLectures.map { it.day }.distinct().size

    return SessionOperationsSnapshot(
        enrolledStudents = students.size,
        availableSeats = availableSeats,
        linkedStudents = linkedStudents,
        gradeRecords = gradeRecords,
        totalSubjects = totalSubjects,
        configuredSemesters = configuredSemesters,
        teachingPeriods = uniqueLectures.size,
        teachingDays = teachingDays,
        feeHeads = fee?.heads?.size ?: 0,
        feeTotal = fee?.totalAmount ?: 0.0,
        setupTasks = setupTasks,
    )
}
