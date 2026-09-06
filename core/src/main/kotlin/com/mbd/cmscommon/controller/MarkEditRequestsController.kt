package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.markEditQueueSnapshot
import com.mbd.cmscommon.domain.model.markEditReviewKey
import com.mbd.cmscommon.domain.model.markEditReviewQuality
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn

class MarkEditRequestsController(
    private val repository: MarkEditRequestRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    departmentRepository: DepartmentRepository,
    teacherRepository: TeacherRepository,
    private val reviewedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _requests = MutableStateFlow<List<MarkEditRequest>>(emptyList())
    val requests: StateFlow<List<MarkEditRequest>> = _requests.asStateFlow()

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _details = MutableStateFlow<Map<String, MarkEditRequestDetails>>(emptyMap())
    val details: StateFlow<Map<String, MarkEditRequestDetails>> = _details.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _busyRequestId = MutableStateFlow<String?>(null)
    val busyRequestId: StateFlow<String?> = _busyRequestId.asStateFlow()

    private val _rowErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val rowErrors: StateFlow<Map<String, String>> = _rowErrors.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) = launch {
        _loading.value = true
        try {
            if (fetchRemote) repository.sync()
            val requests = markEditQueueSnapshot(repository.getPendingRequests()).requests
            _requests.value = requests
            _details.value = loadDetails(requests)
        } finally {
            _loading.value = false
        }
    }

    fun approve(request: MarkEditRequest) = launch {
        val requestKey = markEditReviewKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            require(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }

            val quality = markEditReviewQuality(request)
            require(!quality.blocksApproval) { quality.blockingIssues.joinToString(" ") }
            require(_requests.value.any { it.id == request.id }) { "This request is no longer pending. Refresh the queue." }

            repository.approveRequest(request.id, reviewedBy)
            val notice = "${displayStudent(request)} now has ${request.requestedScore} marks for ${request.courseCode}."
            removeResolvedRequest(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Could not approve this score change."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun reject(request: MarkEditRequest) = launch {
        val requestKey = markEditReviewKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            require(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }
            require(request.id.isNotBlank()) { "This request has no database ID and cannot be rejected safely." }
            require(_requests.value.any { it.id == request.id }) { "This request is no longer pending. Refresh the queue." }

            repository.rejectRequest(request.id, reviewedBy)
            val notice = "The score change for ${displayStudent(request)} was rejected."
            removeResolvedRequest(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Could not reject this score change."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun consumeNotice() {
        _notice.value = null
    }

    private suspend fun loadDetails(queue: List<MarkEditRequest>): Map<String, MarkEditRequestDetails> = coroutineScope {
        queue.map { request -> async { detailsFor(request) } }.awaitAll().toMap()
    }

    private suspend fun detailsFor(request: MarkEditRequest): Pair<String, MarkEditRequestDetails> {
        val studentName = runCatching {
            sessionRepository.observeStudents(request.sessionId).first()
                .firstOrNull { it.rollNumber.equals(request.rollNumber, ignoreCase = true) }?.name
        }.getOrNull()
        val subjectName = runCatching {
            curriculumRepository.observeSemesterSubjects(request.sessionId, request.semester).first()
                .firstOrNull { it.courseCode.equals(request.courseCode, ignoreCase = true) }?.name
        }.getOrNull()
        return request.id to MarkEditRequestDetails(studentName, subjectName)
    }

    private fun removeResolvedRequest(request: MarkEditRequest) {
        val requestKey = markEditReviewKey(request)
        _requests.value = _requests.value.filterNot { it.id == request.id }
        _details.value = _details.value - request.id
        _rowErrors.value = _rowErrors.value - request.id - requestKey
    }

    private fun displayStudent(request: MarkEditRequest): String =
        _details.value[request.id]?.studentName?.takeIf { it.isNotBlank() } ?: "Roll ${request.rollNumber}"
}
