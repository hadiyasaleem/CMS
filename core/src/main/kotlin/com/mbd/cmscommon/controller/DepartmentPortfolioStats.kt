package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session

data class DepartmentPortfolioStats(
    val studentCount: Int = 0,
    val activeSessions: Int = 0,
    val totalCapacity: Int = 0,
    val morningSessions: Int = 0,
    val eveningSessions: Int = 0,
    val minimumSemester: Int? = null,
    val maximumSemester: Int? = null,
)

fun departmentPortfolioStats(sessionCounts: List<Pair<AcademicSession, Int>>): Map<String, DepartmentPortfolioStats> {
    val activeRows = sessionCounts.filter { (session, _) -> session.isActive }
    val byDept = activeRows.groupBy { (session, _) -> session.deptId }

    return byDept.mapValues { (_, rows) ->
        val studentCount = rows.sumOf { it.second }
        val totalCapacity = rows.sumOf { it.first.maxStudents }
        val morningSessions = rows.count { it.first.shift == Session.MORNING }
        val eveningSessions = rows.count { it.first.shift == Session.EVENING }
        val semesters = rows.map { it.first.currentSemester }
        DepartmentPortfolioStats(
            studentCount = studentCount,
            activeSessions = rows.size,
            totalCapacity = totalCapacity,
            morningSessions = morningSessions,
            eveningSessions = eveningSessions,
            minimumSemester = semesters.minOrNull(),
            maximumSemester = semesters.maxOrNull(),
        )
    }
}
