package com.mbd.cmscommon.controller

import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
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
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val me: StateFlow<SessionStudent?> = sessionRepository.observeStudents(sessionId)
        .map { list -> list.firstOrNull { it.rollNumber == rollNumber } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        clearError()
        launch("load your profile") {
            _loading.value = true
            try {
                val rosterSync = if (fetchRemote) runCatching { sessionRepository.syncStudents(sessionId) } else Result.success(Unit)
                val profileLoad = runCatching { sessionRepository.getStudentProfile(sessionId, rollNumber) }

                if (profileLoad.isSuccess) _profile.value = profileLoad.getOrNull()

                showError(
                    FailureSummary.describe(
                        FailureSummary.of(
                            listOf("student details" to rosterSync, "your profile" to profileLoad),
                        ),
                        "StudentProfileController",
                    ),
                )
            } finally {
                _loading.value = false
            }
        }
    }
}
