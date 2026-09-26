package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.requireValid
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class SessionDetailController(
    val sessionId: String,
    private val sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    private val timetableRepository: SessionTimetableRepository,
    private val feeRepository: SessionFeeRepository,
    datesheetRepository: DatesheetRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    val students: StateFlow<List<SessionStudent>> =
        sessionRepository.observeStudents(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Enrolled students per shift (both shifts always present); the total is [students].size. */
    val studentCountsByShift: StateFlow<Map<Session, Int>> = students
        .map { studentCountsByShift(it) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), studentCountsByShift(emptyList()))

    /** This session's datesheets (all shifts) -- consulted before a shift is dropped. */
    val datesheets: StateFlow<List<Datesheet>> = datesheetRepository.observeDatesheets()
        .map { sheets -> sheets.filter { it.sessionId == sessionId } }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fees = MutableStateFlow<List<SessionFeeStructure>>(emptyList())
    /** Each shift's fee structure (Morning and Evening are configured independently). */
    val fees: StateFlow<List<SessionFeeStructure>> = _fees.asStateFlow()

    val subjectCounts: StateFlow<Map<Int, Int>> = curriculumRepository.observeSessionSubjects(sessionId)
        .map { subjects -> subjects.groupingBy { it.semester }.eachCount() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val periods: StateFlow<List<SessionPeriod>> =
        timetableRepository.observeWeek(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _fee = MutableStateFlow<SessionFeeStructure?>(null)
    val fee: StateFlow<SessionFeeStructure?> = _fee.asStateFlow()

    private val _feeLoading = MutableStateFlow(true)
    val feeLoading: StateFlow<Boolean> = _feeLoading.asStateFlow()

    private val _currentSemesterTerm = MutableStateFlow<SemesterTerm?>(null)
    val currentSemesterTerm: StateFlow<SemesterTerm?> = _currentSemesterTerm.asStateFlow()

    /** Promotion (and graduation) is only allowed once the current semester's configured term has ended. */
    val canPromote: StateFlow<Boolean> = _currentSemesterTerm
        .map { term -> term?.endDate?.let { !it.isAfter(LocalDate.now()) } == true }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), false)

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    fun consumeNotice() {
        _notice.value = null
    }

    init {
        launch {
            try {
                val fees = feeRepository.getSessionFees(sessionId)
                _fees.value = fees
                _fee.value = fees.firstOrNull()
            } finally {
                _feeLoading.value = false
            }
        }
        launch {
            session.map { it?.currentSemester }.distinctUntilChanged().collect { semester ->
                _currentSemesterTerm.value = semester?.let { curriculumRepository.getSemesterTerm(sessionId, it) }
            }
        }
    }

    fun promoteSession() = launch {
        requireValid(canPromote.value) { "This can only be done after the current semester's term end date." }
        val currentSemester = session.value?.currentSemester ?: 1
        val graduating = currentSemester >= 8
        sessionRepository.promoteSession(sessionId)
        _notice.value = if (graduating) "Class marked as graduated." else "Promoted to semester ${currentSemester + 1}."
    }

    /**
     * Saves program, in-charge and capacity, and switches the shifts the session runs when [shiftMode]
     * differs. Adding a shift is always allowed; dropping one is refused while it still has students,
     * fees, periods or a datesheet (see [shiftModeChangeError]; the database enforces the same rule).
     */
    fun updateDetails(programName: String?, inchargeEmail: String?, maxStudents: Int, shiftMode: ShiftMode? = null) = launch {
        requireValid((programName ?: "").trim().length <= 120) { "Program name must not exceed 120 characters." }
        requireValid(FieldValidators.emailError(inchargeEmail ?: "", required = false) == null) { "Choose a valid session in-charge." }
        val current = session.value ?: return@launch
        val target = shiftMode ?: current.shiftMode
        val modeChanged = target != current.shiftMode
        if (modeChanged || maxStudents != current.maxStudents) {
            shiftModeChangeError(current, target, maxStudents, students.value, _fees.value, periods.value, datesheets.value)
                .orThrowValidation()
        } else {
            capacityError(maxStudents.toString(), students.value.size).orThrowValidation()
        }
        if (modeChanged) sessionRepository.updateShiftMode(sessionId, target, maxStudents)
        sessionRepository.updateSessionDetails(sessionId, programName, inchargeEmail, maxStudents)
        _notice.value = if (modeChanged) "Session now runs ${target.label}." else "Session details updated."
    }

    fun deleteSession(onDone: () -> Unit) = launch {
        sessionRepository.deleteSession(sessionId)
        onDone()
    }
}
