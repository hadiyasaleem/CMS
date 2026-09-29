package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class SubmittedPapersFilters(
    val teacherEmail: String? = null,
    val deptId: String? = null,
    val semester: Int? = null,
    val shift: Session? = null,
    val sessionId: String? = null,
) {
    /** The Department -> Session -> Shift part of the filters. */
    val scope: ShiftScope get() = ShiftScope(deptId, sessionId, shift)

    val isEmpty: Boolean get() = teacherEmail == null && scope.isEmpty && semester == null
}

/**
 * Papers matching the filters. A paper is set per session and subject and serves both shifts, so a shift filter
 * keeps papers of sessions that run that shift.
 */
fun submittedPapersMatching(
    submissions: List<ExamPaperSubmission>,
    filters: SubmittedPapersFilters,
    sessions: Collection<AcademicSession>,
): List<ExamPaperSubmission> = submissions.filter { sub ->
    (filters.teacherEmail == null || sub.teacherId.equals(filters.teacherEmail, ignoreCase = true)) &&
        (filters.semester == null || sub.semester == filters.semester) &&
        filters.scope.matchesSessionItem(sub.offeringId, null, sessions)
}

/** Admin's browse/download screen for teacher-submitted exam papers -- collected to print, not
 * graded, so this is a filterable list rather than a review queue. */
class SubmittedPapersController(
    private val repo: ExamPaperSubmissionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val submissions: StateFlow<List<ExamPaperSubmission>> =
        repo.observeAllSubmissions().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filters = MutableStateFlow(SubmittedPapersFilters())
    val filters: StateFlow<SubmittedPapersFilters> = _filters.asStateFlow()

    /** Group key -> its papers, newest first. Grouped by teacher whenever no teacher filter is
     * active (the default view); a teacher filter narrows to that one teacher's own group. */
    val grouped: StateFlow<Map<String, List<ExamPaperSubmission>>> = combine(submissions, sessions, teachers, _filters) { subs, sess, techs, filters ->
        val teacherNameByEmail = techs.associateBy({ it.email.lowercase() }, { it.name })
        val matched = submittedPapersMatching(subs, filters, sess)
        val byTeacherLabel = { sub: ExamPaperSubmission -> teacherNameByEmail[sub.teacherId.lowercase()] ?: sub.teacherId }
        matched.groupBy(byTeacherLabel)
            .toSortedMap()
            .mapValues { (_, list) -> list.sortedByDescending { it.uploadedAt } }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launch("refresh the submitted papers") {
        _loading.value = true
        try {
            repo.syncAll()
        } finally {
            _loading.value = false
        }
    }

    fun setTeacherFilter(email: String?) { _filters.value = _filters.value.copy(teacherEmail = email) }
    fun setDeptFilter(deptId: String?) { _filters.value = _filters.value.copy(deptId = deptId) }
    fun setSemesterFilter(semester: Int?) { _filters.value = _filters.value.copy(semester = semester) }
    fun setShiftFilter(shift: Session?) { _filters.value = _filters.value.copy(shift = shift) }
    fun setScope(scope: ShiftScope) {
        _filters.value = _filters.value.copy(deptId = scope.deptId, sessionId = scope.sessionId, shift = scope.shift)
    }
    fun clearFilters() { _filters.value = SubmittedPapersFilters() }

    fun downloadAndOpen(submission: ExamPaperSubmission, targetDir: File, opener: (File) -> Unit) = launch("open the file") {
        runCatching {
            val file = repo.downloadTo(submission, targetDir)
            opener(file)
        }.onFailure { _notice.value = it.userMessageLogged("Couldn't open ${submission.fileName}.") }
    }

    fun consumeNotice() {
        _notice.value = null
    }
}
