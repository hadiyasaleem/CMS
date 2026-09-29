package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.requireValid

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceEditRequest
import com.mbd.cmscommon.domain.model.attendanceEditReviewIssues
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.markEditQueueSnapshot
import com.mbd.cmscommon.domain.model.markEditReviewKey
import com.mbd.cmscommon.domain.model.markEditReviewQuality
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AttendanceEditRequestRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.orLogCritical
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
    private val attendanceRepository: AttendanceEditRequestRepository,
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

    private val _attendanceRequests = MutableStateFlow<List<AttendanceEditRequest>>(emptyList())
    val attendanceRequests: StateFlow<List<AttendanceEditRequest>> = _attendanceRequests.asStateFlow()

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

    fun refresh(fetchRemote: Boolean = true) = launch("load the edit requests") {
        _loading.value = true
        try {
            if (fetchRemote) repository.sync()
            val requests = markEditQueueSnapshot(repository.getPendingRequests()).requests
            _requests.value = requests
            val attendance = runCatching { attendanceRepository.getPendingRequests() }
                .onFailure { _rowErrors.value = _rowErrors.value + (ATTENDANCE_LOAD_KEY to it.userMessageLogged("Couldn't load the attendance edit requests.")) }
                .getOrDefault(emptyList())
            _attendanceRequests.value = attendance
            _details.value = loadDetails(requests) + loadAttendanceDetails(attendance)
        } finally {
            _loading.value = false
        }
    }

    fun approve(request: MarkEditRequest) = launch("approve the score change") {
        val requestKey = markEditReviewKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            requireValid(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }

            val quality = markEditReviewQuality(request)
            requireValid(!quality.blocksApproval) { quality.blockingIssues.joinToString(" ") }
            requireValid(_requests.value.any { it.id == request.id }) { "The score change for ${displayStudent(request)} in ${request.courseCode} is no longer pending. Refresh the queue." }

            repository.approveRequest(request.id, reviewedBy)
            val notice = "${displayStudent(request)} now has ${request.requestedScore} marks for ${request.courseCode}."
            removeResolvedRequest(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Couldn't approve the score change for ${displayStudent(request)} in ${request.courseCode}."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun reject(request: MarkEditRequest) = launch("reject the score change") {
        val requestKey = markEditReviewKey(request)
        try {
            _busyRequestId.value = requestKey
            _notice.value = null
            requireValid(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }
            requireValid(request.id.isNotBlank()) { "This request has no database ID and cannot be rejected safely." }
            requireValid(_requests.value.any { it.id == request.id }) { "The score change for ${displayStudent(request)} in ${request.courseCode} is no longer pending. Refresh the queue." }

            repository.rejectRequest(request.id, reviewedBy)
            val notice = "The score change for ${displayStudent(request)} was rejected."
            removeResolvedRequest(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (requestKey to t.userMessageLogged("Couldn't reject the score change for ${displayStudent(request)} in ${request.courseCode}."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun approveAttendance(request: AttendanceEditRequest) = launch("approve the attendance change") {
        try {
            _busyRequestId.value = request.id
            _notice.value = null
            requireValid(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }
            val issues = attendanceEditReviewIssues(request)
            requireValid(issues.isEmpty()) { issues.joinToString(" ") }
            requireValid(_attendanceRequests.value.any { it.id == request.id }) { "The attendance change for ${displayStudent(request.id, request.rollNumber)} on ${request.date} is no longer pending. Refresh the queue." }

            attendanceRepository.approveRequest(request.id, reviewedBy)
            val notice = "${displayStudent(request.id, request.rollNumber)} is now marked ${request.requestedStatus.name.lowercase()} on ${request.date} for ${request.courseCode}."
            removeResolvedAttendance(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (request.id to t.userMessageLogged("Couldn't approve the attendance change for ${displayStudent(request.id, request.rollNumber)} on ${request.date}."))
        } finally {
            _busyRequestId.value = null
        }
    }

    fun rejectAttendance(request: AttendanceEditRequest) = launch("reject the attendance change") {
        try {
            _busyRequestId.value = request.id
            _notice.value = null
            requireValid(reviewedBy.isNotBlank()) { "Your signed-in account could not be identified." }
            requireValid(request.id.isNotBlank()) { "This request has no database ID and cannot be rejected safely." }
            requireValid(_attendanceRequests.value.any { it.id == request.id }) { "The attendance change for ${displayStudent(request.id, request.rollNumber)} on ${request.date} is no longer pending. Refresh the queue." }

            attendanceRepository.rejectRequest(request.id, reviewedBy)
            val notice = "The attendance change for ${displayStudent(request.id, request.rollNumber)} was rejected."
            removeResolvedAttendance(request)
            _notice.value = notice
        } catch (t: Throwable) {
            _rowErrors.value = _rowErrors.value + (request.id to t.userMessageLogged("Couldn't reject the attendance change for ${displayStudent(request.id, request.rollNumber)} on ${request.date}."))
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

    private suspend fun loadAttendanceDetails(queue: List<AttendanceEditRequest>): Map<String, MarkEditRequestDetails> = coroutineScope {
        queue.map { request ->
            async { request.id to lookupDetails(request.sessionId, request.semester, request.courseCode, request.rollNumber) }
        }.awaitAll().toMap()
    }

    private suspend fun detailsFor(request: MarkEditRequest): Pair<String, MarkEditRequestDetails> =
        request.id to lookupDetails(request.sessionId, request.semester, request.courseCode, request.rollNumber)

    // Best-effort: names are display sugar for the request row (it falls back to the roll number / course code); failures are logged.
    private suspend fun lookupDetails(sessionId: String, semester: Int, courseCode: String, rollNumber: String): MarkEditRequestDetails {
        val studentName = runCatching {
            sessionRepository.observeStudents(sessionId).first()
                .firstOrNull { it.rollNumber.equals(rollNumber, ignoreCase = true) }?.name
        }.orLogCritical("MarkEditRequestsController.detailsFor.studentName")
        val subjectName = runCatching {
            curriculumRepository.observeSemesterSubjects(sessionId, semester).first()
                .firstOrNull { it.courseCode.equals(courseCode, ignoreCase = true) }?.name
        }.orLogCritical("MarkEditRequestsController.detailsFor.subjectName")
        return MarkEditRequestDetails(studentName, subjectName)
    }

    private fun removeResolvedAttendance(request: AttendanceEditRequest) {
        _attendanceRequests.value = _attendanceRequests.value.filterNot { it.id == request.id }
        _details.value = _details.value - request.id
        _rowErrors.value = _rowErrors.value - request.id
    }

    private fun removeResolvedRequest(request: MarkEditRequest) {
        val requestKey = markEditReviewKey(request)
        _requests.value = _requests.value.filterNot { it.id == request.id }
        _details.value = _details.value - request.id
        _rowErrors.value = _rowErrors.value - request.id - requestKey
    }

    private fun displayStudent(request: MarkEditRequest): String = displayStudent(request.id, request.rollNumber)

    private fun displayStudent(id: String, rollNumber: String): String =
        _details.value[id]?.studentName?.takeIf { it.isNotBlank() } ?: "Roll $rollNumber"

    companion object {
        const val ATTENDANCE_LOAD_KEY = "attendance-load"
    }
}
