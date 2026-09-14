package com.mbd.cmscommon.controller

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
) : ScreenController(scope) {

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
    private var cachedSubmittedPapers: Int = 0

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        loadVersion++
        val version = loadVersion
        launch {
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
                        examPaperSubmissionRepository.observeAllSubmissions().first().size
                    }
                }

                val teachersResult = teachersDeferred.await()
                val studentsResult = studentsDeferred.await()
                val linksResult = linksDeferred.await()
                val editsResult = editsDeferred.await()
                val submittedPapersResult = submittedPapersDeferred.await()

                if (version == loadVersion) {
                    teachersResult.getOrNull()?.let { cachedTeachers = it }
                    studentsResult.getOrNull()?.let { cachedStudentCount = it }
                    linksResult.getOrNull()?.let { cachedLinks = it }
                    editsResult.getOrNull()?.let { cachedEdits = it }
                    submittedPapersResult.getOrNull()?.let { cachedSubmittedPapers = it }

                    _snapshot.value = peopleHubSnapshot(
                        cachedTeachers,
                        cachedStudentCount,
                        cachedLinks,
                        cachedEdits,
                        cachedSubmittedPapers,
                    )
                    _loadError.value = listOf(teachersResult, studentsResult, linksResult, editsResult, submittedPapersResult)
                        .firstNotNullOfOrNull { it.exceptionOrNull() }
                        ?.userMessageLogged("Some people summaries could not be loaded.")
                    _loading.value = false
                }
            }
        }
    }
}
