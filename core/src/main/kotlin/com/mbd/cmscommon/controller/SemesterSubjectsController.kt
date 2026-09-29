package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
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

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    fun consumeNotice() {
        _notice.value = null
    }

    private val _termError = MutableStateFlow<String?>(null)
    /** Why the last [saveTerm] did not save (bad date, start after end, or the server's reason). */
    val termError: StateFlow<String?> = _termError.asStateFlow()

    fun clearTermError() {
        _termError.value = null
    }

    init {
        launch("load the semester") {
            try {
            } finally {
                _loading.value = false
            }
        }
        launch("load the class term dates") {
            _term.value = repo.getSemesterTerm(sessionId, semester)
        }
    }

    fun saveTerm(startText: String, endText: String, onDone: (Boolean) -> Unit) {
        _termError.value = null
        val (start, startInvalid) = parseDate(startText)
        val (end, endInvalid) = parseDate(endText)
        if (startInvalid) {
            _termError.value = "Enter the start date as YYYY-MM-DD (for example 2026-09-01)."
            onDone(false)
            return
        }
        if (endInvalid) {
            _termError.value = "Enter the end date as YYYY-MM-DD (for example 2027-01-15)."
            onDone(false)
            return
        }
        if (start != null && end != null && start.isAfter(end)) {
            _termError.value = "The term can't end ($end) before it starts ($start)."
            onDone(false)
            return
        }
        launch("save the term dates") {
            try {
                repo.saveSemesterTerm(sessionId, semester, start, end)
                _term.value = SemesterTerm(sessionId, semester, start, end)
                _notice.value = "Class term dates saved."
                onDone(true)
            } catch (t: Throwable) {
                _termError.value = t.userMessageLogged("Couldn't save the term dates.")
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
    ) = launch("save the subject") {
        val normalizedCode = courseCode.trim().uppercase(Locale.ROOT)
        FieldValidators.courseCodeError(normalizedCode).orThrowValidation()
        FieldValidators.textError(name, "Subject name", maxLength = 120).orThrowValidation()
        FieldValidators.textError(outline ?: "", "Course outline", required = false, maxLength = 2000)?.let {
            throw CmsException.Validation("Course outline must not exceed 2,000 characters.")
        }
        requireValid(creditHours in 1..6) { "Credit hours must be between 1 and 6." }

        val conflict = subjects.value.any {
            it.courseCode.equals(normalizedCode, ignoreCase = true) &&
                !it.courseCode.equals(originalCourseCode ?: "", ignoreCase = true)
        }
        requireValid(!conflict) { "Course code $normalizedCode already exists in this semester." }

        val renamed = originalCourseCode != null && !originalCourseCode.equals(normalizedCode, ignoreCase = true)
        if (renamed) {
            // The course code is changing: retire the old row *before* creating the new one, so a
            // subject with existing attendance/marks/timetable data (blocked by deleteSemesterSubject)
            // fails cleanly instead of leaving both the old and new codes behind as duplicates.
            repo.deleteSemesterSubject(sessionId, semester, originalCourseCode!!)
        }

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
        _notice.value = if (originalCourseCode != null) "$normalizedCode updated." else "$normalizedCode added."
    }

    fun addSubject(courseCode: String, name: String, creditHours: Int, subjectType: SubjectType, isElective: Boolean, outline: String?) {
        saveSubject(null, courseCode, name, creditHours, subjectType, isElective, outline)
    }

    fun removeSubject(courseCode: String) = launch("remove the subject") {
        repo.deleteSemesterSubject(sessionId, semester, courseCode)
        _notice.value = "$courseCode removed."
    }

    private fun parseDate(text: String): Pair<LocalDate?, Boolean> {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return null to false
        val parsed = runCatching { LocalDate.parse(trimmed) }.getOrNull()
        return parsed to (parsed == null)
    }
}
