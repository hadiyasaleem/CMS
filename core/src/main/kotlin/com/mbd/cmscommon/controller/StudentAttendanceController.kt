package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentAttendanceController(
    private val sessionId: String,
    rollNumber: String,
    private val attendanceRepository: SessionAttendanceRepository,
    private val curriculumRepository: CurriculumRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val rows: StateFlow<List<SubjectAttendanceRow>> = combine(
        attendanceRepository.observeStudentTallies(sessionId, rollNumber),
        curriculumRepository.observeSessionSubjects(sessionId),
    ) { tallies, subjects ->
        val subjectByCode = subjects.associateBy { it.courseCode.trim().lowercase(Locale.ROOT) }
        tallies.map { tally ->
            val subject = subjectByCode[tally.courseCode.trim().lowercase(Locale.ROOT)]
            SubjectAttendanceRow(
                courseCode = tally.courseCode,
                subjectName = subject?.name ?: tally.courseCode,
                present = tally.present,
                absent = tally.absent,
                leave = tally.leave,
                creditHours = subject?.creditHours,
                subjectType = subject?.subjectType,
                semester = subject?.semester,
            )
        }.sortedBy { it.courseCode }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overallPercent: StateFlow<Float> = rows
        .map { list ->
            val total = list.sumOf { it.total }
            val present = list.sumOf { it.present }
            if (total == 0) 0f else (present * 100f) / total
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), 0f)

    fun refresh() = launch {
        runCatching { attendanceRepository.syncSession(sessionId) }
        runCatching { curriculumRepository.syncSession(sessionId) }
    }
}

fun studentAttendanceSnapshot(rows: List<SubjectAttendanceRow>): StudentAttendanceSnapshot {
    val recorded = rows.filter { it.total > 0 }
    val total = recorded.sumOf { it.total }
    val present = recorded.sumOf { it.present }
    val overallPercent = if (total == 0) 0f else (present * 100f) / total
    val subjectsAtRisk = recorded.count { it.percentage < 75f }
    val weakestSubject = recorded.minByOrNull { it.percentage }

    return StudentAttendanceSnapshot(rows, overallPercent, total, present, subjectsAtRisk, weakestSubject)
}
