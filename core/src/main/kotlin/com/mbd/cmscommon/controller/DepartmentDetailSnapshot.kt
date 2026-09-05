package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession

data class DepartmentDetailSnapshot(
    val sessions: List<AcademicSession>,
    val studentCount: Int,
    val totalCapacity: Int,
    val remainingSeats: Int,
    val occupancyPercent: Float,
    val sessionsNeedingSetup: Int,
)

fun departmentDetailSnapshot(sessions: List<AcademicSession>, studentCounts: Map<String, Int>): DepartmentDetailSnapshot {
    val currentSessions = sessions.filter { it.isActive }

    val studentCount = currentSessions.sumOf { (studentCounts[it.sessionId] ?: 0).coerceAtLeast(0) }
    val totalCapacity = currentSessions.sumOf { it.maxStudents.coerceAtLeast(0) }
    val occupiedSeats = studentCount.coerceAtMost(totalCapacity)
    val remainingSeats = (totalCapacity - occupiedSeats).coerceAtLeast(0)
    val occupancyPercent = if (totalCapacity == 0) 0f else (occupiedSeats * 100f) / totalCapacity
    val sessionsNeedingSetup = currentSessions.count {
        it.programName.isNullOrBlank() || it.inchargeEmail.isNullOrBlank()
    }

    return DepartmentDetailSnapshot(currentSessions, studentCount, totalCapacity, remainingSeats, occupancyPercent, sessionsNeedingSetup)
}
