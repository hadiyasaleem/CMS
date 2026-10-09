package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.PeopleHubSnapshot
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.peopleHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.ExamPaperSubmissionRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

class PeopleHubController(
    private val teacherRepository: TeacherRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val linkRequestRepository: StudentLinkRequestRepository,
    private val markEditRequestRepository: MarkEditRequestRepository,
    private val examPaperSubmissionRepository: ExamPaperSubmissionRepository,
    scope: CoroutineScope,
    /** Department names for the filter; without it departments are shown by their code. */
    private val departmentRepository: DepartmentRepository? = null,
) : ScreenController(scope) {

    private val _filterScope = MutableStateFlow(DeptSemesterScope.ALL)

    /** The Department / Semester / Shift filter the hub's counts follow. */
    val filterScope: StateFlow<DeptSemesterScope> = _filterScope.asStateFlow()

    private val _filterOptions = MutableStateFlow(ScopeFilterOptions())
    val filterOptions: StateFlow<ScopeFilterOptions> = _filterOptions.asStateFlow()

    private val _availableSemesters = MutableStateFlow<List<Int>>(emptyList())
    val availableSemesters: StateFlow<List<Int>> = _availableSemesters.asStateFlow()

    fun setFilterScope(scope: DeptSemesterScope) {
        _filterScope.value = scope
        publish()
    }

    private val _snapshot = MutableStateFlow<PeopleHubSnapshot?>(null)
    val snapshot: StateFlow<PeopleHubSnapshot?> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0
    private var cachedTeachers: List<Teacher> = emptyList()
    private var cachedStudentCount: Int = 0
    private var cachedLinks: List<StudentLinkRequest> = emptyList()
    private var cachedEdits: List<MarkEditRequest> = emptyList()
    private var cachedSubmissions: List<ExamPaperSubmission> = emptyList()
    private var cachedSessions: List<AcademicSession> = emptyList()
    private var cachedProfiles: List<StudentProfile> = emptyList()

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        loadVersion++
        val version = loadVersion
        launch("load the people summaries") {
            _loading.value = true
            _loadError.value = null
            supervisorScope {
                val teachersDeferred = async {
                    runCatching {
                        if (fetchRemote) teacherRepository.sync()
                        teacherRepository.observeActiveTeachers().first()
                    }
                }
                val studentsDeferred = async {
                    runCatching {
                        if (fetchRemote) {
                            sessionRepository.observeAllSessions().first().forEach { session ->
                                sessionRepository.syncStudents(session.sessionId)
                            }
                        }
                        sessionRepository.observeActiveSessionStudentCount().first()
                    }
                }
                val linksDeferred = async {
                    runCatching {
                        if (fetchRemote) linkRequestRepository.sync()
                        linkRequestRepository.observePendingRequests().first()
                    }
                }
                val editsDeferred = async {
                    runCatching {
                        if (fetchRemote) markEditRequestRepository.sync()
                        markEditRequestRepository.getPendingRequests()
                    }
                }
                val submittedPapersDeferred = async {
                    runCatching {
                        if (fetchRemote) examPaperSubmissionRepository.syncAll()
                        examPaperSubmissionRepository.observeAllSubmissions().first()
                    }
                }
                val scopeDataDeferred = async {
                    runCatching {
                        val sessions = sessionRepository.observeAllSessions().first()
                        val departments = departmentRepository?.observeActiveDepartments()?.first()
                        Triple(sessions, sessionRepository.observeAllStudentProfiles().first(), departments)
                    }
                }

                val teachersResult = teachersDeferred.await()
                val studentsResult = studentsDeferred.await()
                val linksResult = linksDeferred.await()
                val editsResult = editsDeferred.await()
                val submittedPapersResult = submittedPapersDeferred.await()
                val scopeDataResult = scopeDataDeferred.await()

                if (version == loadVersion) {
                    teachersResult.getOrNull()?.let { cachedTeachers = it }
                    studentsResult.getOrNull()?.let { cachedStudentCount = it }
                    linksResult.getOrNull()?.let { cachedLinks = it }
                    editsResult.getOrNull()?.let { cachedEdits = it }
                    submittedPapersResult.getOrNull()?.let { cachedSubmissions = it }
                    scopeDataResult.getOrNull()?.let { (sessions, profiles, departments) ->
                        cachedSessions = sessions
                        cachedProfiles = profiles
                        _filterOptions.value = departments?.let { ScopeFilterOptions.of(it, sessions) }
                            ?: ScopeFilterOptions(sessions.map { it.deptId to it.deptId.uppercase() }.distinct(), sessions)
                        _availableSemesters.value = sessions.availableSemesters()
                    }

                    publish()
                    _loadError.value = FailureSummary.describe(
                        FailureSummary.of(
                            listOf(
                                "teachers" to teachersResult,
                                "student counts" to studentsResult,
                                "link requests" to linksResult,
                                "edit requests" to editsResult,
                                "submitted papers" to submittedPapersResult,
                            ),
                        ),
                        "PeopleHubController",
                    )
                    _loading.value = false
                }
            }
        }
    }

    private fun publish() {
        _snapshot.value = peopleHubSnapshotInScope(
            PeopleHubSources(cachedTeachers, cachedStudentCount, cachedProfiles, cachedLinks, cachedEdits, cachedSubmissions, cachedSessions),
            _filterScope.value,
        )
    }
}

data class PeopleHubSources(
    val teachers: List<Teacher>,
    val activeStudentCount: Int,
    val profiles: List<StudentProfile>,
    val linkRequests: List<StudentLinkRequest>,
    val markEdits: List<MarkEditRequest>,
    val submissions: List<ExamPaperSubmission>,
    val sessions: List<AcademicSession>,
)

/** The People hub counts inside a scope; with nothing chosen they are college-wide. */
fun peopleHubSnapshotInScope(sources: PeopleHubSources, scope: DeptSemesterScope): PeopleHubSnapshot {
    if (scope.isEmpty) {
        return peopleHubSnapshot(sources.teachers, sources.activeStudentCount, sources.linkRequests, sources.markEdits, sources.submissions.size)
    }
    val activeIds = sources.sessions.filter { it.isActive && scope.matches(it) }.map { it.sessionId }.toSet()
    return peopleHubSnapshot(
        teachers = sources.teachers.filter { scope.deptId == null || it.deptId == scope.deptId },
        studentCount = sources.profiles.count { profile ->
            profile.sessionId in activeIds && scope.matchesSessionItem(profile.sessionId, null, profile.shift, sources.sessions)
        },
        linkRequests = sources.linkRequests.inScope(scope, sources.sessions),
        markEditRequests = sources.markEdits.inScope(scope, sources.sessions),
        submittedPapers = submittedPapersMatching(
            sources.submissions,
            SubmittedPapersFilters(deptId = scope.deptId, semester = scope.semester, shift = scope.shift),
            sources.sessions,
        ).size,
    )
}
