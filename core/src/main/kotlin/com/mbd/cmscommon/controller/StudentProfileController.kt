package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class StudentProfileController(
    private val sessionId: String,
    private val rollNumber: String,
    private val sessionRepository: AcademicSessionRepository,
    private val fineRepository: FineRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val me: StateFlow<SessionStudent?> = sessionRepository.observeStudents(sessionId)
        .map { list -> list.firstOrNull { it.rollNumber == rollNumber } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    private val _fines = MutableStateFlow<List<Fine>>(emptyList())
    val fines: StateFlow<List<Fine>> = _fines.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        clearError()
        launch {
            _loading.value = true
            try {
                val rosterSync = if (fetchRemote) runCatching { sessionRepository.syncStudents(sessionId) } else Result.success(Unit)
                val fineSync = if (fetchRemote) runCatching { fineRepository.sync(sessionId, rollNumber) } else Result.success(Unit)
                val profileLoad = runCatching { sessionRepository.getStudentProfile(sessionId, rollNumber) }
                val finesLoad = runCatching { fineRepository.getFines(sessionId, rollNumber) }

                if (profileLoad.isSuccess) _profile.value = profileLoad.getOrNull()
                finesLoad.getOrNull()?.let { _fines.value = it }

                (rosterSync.exceptionOrNull() ?: fineSync.exceptionOrNull() ?: profileLoad.exceptionOrNull() ?: finesLoad.exceptionOrNull())?.let { throw it }
            } finally {
                _loading.value = false
            }
        }
    }
}
