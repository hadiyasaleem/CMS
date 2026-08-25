package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.PeopleHubSnapshot
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.peopleHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

class PeopleHubController(
    private val administratorRepository: AdministratorRepository,
    private val teacherRepository: TeacherRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val linkRequestRepository: StudentLinkRequestRepository,
    private val markEditRequestRepository: MarkEditRequestRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _snapshot = MutableStateFlow<PeopleHubSnapshot?>(null)
    val snapshot: StateFlow<PeopleHubSnapshot?> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0
    private var cachedAdministrators: List<AdministratorAccount> = emptyList()
    private var cachedTeachers: List<Teacher> = emptyList()
    private var cachedStudentCount: Int = 0
    private var cachedLinks: List<StudentLinkRequest> = emptyList()
    private var cachedEdits: List<MarkEditRequest> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        loadVersion++
        val version = loadVersion
        launch {
            _loading.value = true
            _loadError.value = null
            supervisorScope {
                val administratorsDeferred = async {
                    runCatching {
                        administratorRepository.sync()
                        administratorRepository.observeAdministrators().first()
                    }
                }
                val teachersDeferred = async {
                    runCatching {
                        teacherRepository.sync()
                        teacherRepository.observeActiveTeachers().first()
                    }
                }
                val studentsDeferred = async { runCatching { sessionRepository.observeTotalStudentCount().first() } }
                val linksDeferred = async {
                    runCatching {
                        linkRequestRepository.sync()
                        linkRequestRepository.observePendingRequests().first()
                    }
                }
                val editsDeferred = async { runCatching { markEditRequestRepository.getPendingRequests() } }

                val administratorsResult = administratorsDeferred.await()
                val teachersResult = teachersDeferred.await()
                val studentsResult = studentsDeferred.await()
                val linksResult = linksDeferred.await()
                val editsResult = editsDeferred.await()

                if (version == loadVersion) {
                    administratorsResult.getOrNull()?.let { cachedAdministrators = it }
                    teachersResult.getOrNull()?.let { cachedTeachers = it }
                    studentsResult.getOrNull()?.let { cachedStudentCount = it }
                    linksResult.getOrNull()?.let { cachedLinks = it }
                    editsResult.getOrNull()?.let { cachedEdits = it }

                    _snapshot.value = peopleHubSnapshot(cachedAdministrators, cachedTeachers, cachedStudentCount, cachedLinks, cachedEdits)
                    _loadError.value = listOf(administratorsResult, teachersResult, studentsResult, linksResult, editsResult)
                        .firstNotNullOfOrNull { it.exceptionOrNull() }
                        ?.userMessage("Some people summaries could not be loaded.")
                    _loading.value = false
                }
            }
        }
    }
}
