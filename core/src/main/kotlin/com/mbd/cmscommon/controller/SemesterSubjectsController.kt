package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.util.FieldValidators
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class SemesterSubjectsController(
    val sessionId: String,
    val semester: Int,
    private val repo: CurriculumRepository,
    sessionRepository: AcademicSessionRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val subjects: StateFlow<List<SemesterSubject>> =
        repo.observeSemesterSubjects(sessionId, semester).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _term = MutableStateFlow<SemesterTerm?>(null)
    val term: StateFlow<SemesterTerm?> = _term.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        launch {
            try {
            } finally {
                _loading.value = false
            }
        }
        launch {
            _term.value = repo.getSemesterTerm(sessionId, semester)
        }
    }

    fun saveTerm(startText: String, endText: String, onDone: (Boolean) -> Unit) {
        val (start, startInvalid) = parseDate(startText)
        val (end, endInvalid) = parseDate(endText)
        if (startInvalid || endInvalid) {
            onDone(false)
            return
        }
        if (start != null && end != null && start.isAfter(end)) {
            onDone(false)
            return
        }
        launch {
            try {
                repo.saveSemesterTerm(sessionId, semester, start, end)
                _term.value = SemesterTerm(sessionId, semester, start, end)
                onDone(true)
            } catch (t: Throwable) {
                onDone(false)
            }
        }
    }

    fun saveSubject(
        originalCourseCode: String?,
        courseCode: String,
        name: String,
        creditHours: Int,
        subjectType: SubjectType,
        isElective: Boolean,
        outline: String?,
    ) = launch {
        val normalizedCode = courseCode.trim().uppercase(Locale.ROOT)
        FieldValidators.courseCodeError(normalizedCode)?.let { throw IllegalStateException(it) }
        FieldValidators.textError(name, "Subject name", maxLength = 120)?.let { throw IllegalStateException(it) }
        FieldValidators.textError(outline ?: "", "Course outline", required = false, maxLength = 2000)?.let {
            throw IllegalStateException("Course outline must not exceed 2,000 characters.")
        }
        require(creditHours in 1..6) { "Credit hours must be between 1 and 6." }

        val conflict = subjects.value.any {
            it.courseCode.equals(normalizedCode, ignoreCase = true) &&
                !it.courseCode.equals(originalCourseCode ?: "", ignoreCase = true)
        }
        require(!conflict) { "Course code $normalizedCode already exists in this semester." }

        val subject = SemesterSubject(
            sessionId = sessionId,
            semester = semester,
            courseCode = normalizedCode,
            name = name.trim(),
            creditHours = creditHours,
            subjectType = subjectType,
            isElective = isElective,
            outline = outline?.trim()?.takeIf { it.isNotBlank() },
        )
        repo.saveSemesterSubject(subject)
    }

    fun addSubject(courseCode: String, name: String, creditHours: Int, subjectType: SubjectType, isElective: Boolean, outline: String?) {
        saveSubject(null, courseCode, name, creditHours, subjectType, isElective, outline)
    }

    fun removeSubject(courseCode: String) = launch {
        repo.deleteSemesterSubject(sessionId, semester, courseCode)
    }

    private fun parseDate(text: String): Pair<LocalDate?, Boolean> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null to false
        return try {
            LocalDate.parse(trimmed) to false
        } catch (e: Exception) {
            null to true
        }
    }
}
