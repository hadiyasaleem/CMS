package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SubjectExamScore
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class StudentMarksController(
    private val sessionId: String,
    rollNumber: String,
    private val marksRepository: SessionMarksRepository,
    private val curriculumRepository: CurriculumRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val rows: StateFlow<List<SubjectMarksRow>> = combine(
        marksRepository.observeStudentMarks(sessionId, rollNumber),
        curriculumRepository.observeSessionSubjects(sessionId),
    ) { scores, subjects ->
        val subjectByCode = subjects.associateBy { normalizedCourseCode(it.courseCode) }
        val scoresByCode = scores.groupBy { normalizedCourseCode(it.courseCode) }

        scoresByCode.map { (normalizedCode, entries) ->
            val subject = subjectByCode[normalizedCode]
            val midterm = assessment(entries, ExamType.MIDTERM)
            val sessional = assessment(entries, ExamType.SESSIONAL)

            SubjectMarksRow(
                courseCode = subject?.courseCode ?: entries.first().courseCode.trim(),
                subjectName = subject?.name ?: entries.first().courseCode.trim(),
                midterm = midterm?.score,
                sessional = sessional?.score,
                midtermAbsent = midterm?.wasAbsent == true,
                sessionalAbsent = sessional?.wasAbsent == true,
                midtermMaxMarks = midterm?.let { resolvedMaxMarks(it) },
                sessionalMaxMarks = sessional?.let { resolvedMaxMarks(it) },
                midtermRemarks = midterm?.remarks?.trim()?.takeIf { it.isNotEmpty() },
                sessionalRemarks = sessional?.remarks?.trim()?.takeIf { it.isNotEmpty() },
                semester = subject?.semester,
                creditHours = subject?.creditHours,
                subjectType = subject?.subjectType,
                isElective = subject?.isElective == true,
            )
        }.sortedBy { it.courseCode }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        _refreshing.value = true
        launch {
            try {
                val failures = listOfNotNull(
                    runCatching { marksRepository.syncSession(sessionId) }.exceptionOrNull(),
                    runCatching { curriculumRepository.syncSession(sessionId) }.exceptionOrNull(),
                )
                failures.firstOrNull()?.let { throw it }
            } finally {
                _refreshing.value = false
            }
        }
    }
}

fun studentMarksSnapshot(rows: List<SubjectMarksRow>): StudentMarksSnapshot = StudentMarksSnapshot(
    rows = rows,
    earnedMarks = rows.sumOf { it.total },
    availableMarks = rows.sumOf { it.totalMaxMarks },
    assessmentsEntered = rows.sumOf { it.enteredAssessments },
    absentAssessments = rows.sumOf { it.absentAssessments },
    fullyRecordedSubjects = rows.count { it.enteredAssessments == 2 },
)

private fun normalizedCourseCode(value: String): String = value.trim().uppercase(Locale.ROOT)

private fun assessment(scores: List<SubjectExamScore>, type: ExamType): SubjectExamScore? =
    scores.filter { it.examType == type }.maxByOrNull { it.maxMarks }

private fun resolvedMaxMarks(score: SubjectExamScore): Int =
    score.maxMarks.takeIf { it > 0 } ?: score.examType.maxMarks
