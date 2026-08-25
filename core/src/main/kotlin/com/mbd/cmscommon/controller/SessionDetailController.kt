package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.FieldValidators
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionDetailController(
    val sessionId: String,
    private val sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val feeRepository: SessionFeeRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val students: StateFlow<List<SessionStudent>> =
        sessionRepository.observeStudents(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subjectCounts: StateFlow<Map<Int, Int>> = curriculumRepository.observeSessionSubjects(sessionId)
        .map { subjects -> subjects.groupingBy { it.semester }.eachCount() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeWeek(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fee = MutableStateFlow<SessionFeeStructure?>(null)
    val fee: StateFlow<SessionFeeStructure?> = _fee.asStateFlow()

    private val _feeLoading = MutableStateFlow(true)
    val feeLoading: StateFlow<Boolean> = _feeLoading.asStateFlow()

    init {
        launch { runCatching { sessionRepository.syncStudents(sessionId) } }
        launch { runCatching { curriculumRepository.syncSession(sessionId) } }
        launch { runCatching { timetableRepository.syncSession(sessionId) } }
        launch {
            try {
                _fee.value = feeRepository.getSessionFee(sessionId)
            } finally {
                _feeLoading.value = false
            }
        }
    }

    fun setSemester(semester: Int) = launch {
        sessionRepository.setCurrentSemester(sessionId, semester)
    }

    fun updateDetails(programName: String?, inchargeEmail: String?, maxStudents: Int) = launch {
        require((programName ?: "").trim().length <= 120) { "Program name must not exceed 120 characters." }
        require(FieldValidators.emailError(inchargeEmail ?: "", required = false) == null) { "Choose a valid session in-charge." }
        require(maxStudents in 1..50) { "Student capacity must be between 1 and 50." }
        sessionRepository.updateSessionDetails(sessionId, programName, inchargeEmail, maxStudents)
    }

    fun deleteSession(onDone: () -> Unit) = launch {
        sessionRepository.deleteSession(sessionId)
        onDone()
    }
}
