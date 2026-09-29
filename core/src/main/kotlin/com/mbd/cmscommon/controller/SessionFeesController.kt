package com.mbd.cmscommon.controller

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

class SessionFeesController(
    val sessionId: String,
    private val repo: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    private val updatedBy: String,
    scope: CoroutineScope,
    initialShift: Session? = null,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    /** For the sample-challan preview's department code -- looked up reactively as [session] resolves. */
    val department: StateFlow<Department?> = session
        .flatMapLatest { s -> if (s == null) flowOf(null) else flowOf(departmentRepository.getDepartment(s.deptId)) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _pickedShift = MutableStateFlow(initialShift)

    /** The Morning/Evening tab being edited (only shifts the session runs; no combined view). */
    val shift: StateFlow<Session> = combine(session, _pickedShift) { s, picked -> feeTabShift(s, picked) }
        .stateIn(scope, SharingStarted.Eagerly, initialShift ?: Session.MORNING)

    val shifts: StateFlow<List<Session>> = session.map { feeTabs(it) }
        .stateIn(scope, SharingStarted.Eagerly, feeTabs(null))

    /** Every shift's structure; each shift has its own plan, heads and amounts. */
    private val _structures = MutableStateFlow<Map<Session, SessionFeeStructure>>(emptyMap())
    val structures: StateFlow<List<SessionFeeStructure>> = _structures.map { it.values.sortedBy { fee -> fee.shift } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The selected shift's structure, or null when that shift has none yet. */
    val structure: StateFlow<SessionFeeStructure?> = combine(_structures, shift) { all, current -> all[current] }
        .stateIn(scope, SharingStarted.Eagerly, null)

    fun selectShift(picked: Session) {
        _pickedShift.value = picked
        _saved.value = false
    }

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()
    private var structureVersion = 0


    init {
        load()
    }

    private fun load() {
        val loadVersion = structureVersion
        launch("load the fee structure") {
            try {
                val loaded = repo.getSessionFees(sessionId).associateBy { it.shift }
                if (loadVersion == structureVersion) _structures.value = loaded
            } finally {
                _loading.value = false
            }
        }
    }

    fun save(cadence: FeeType, heads: List<FeeHead>, academicYear: String, dueDate: String, lateFineNote: String, paymentNote: String) = launch("save the fee structure") {
        try {
            _saving.value = true
            val normalizedHeads = heads.map { it.copy(label = it.label.trim()) }
            requireValid(normalizedHeads.isNotEmpty()) { "Add at least one fee head before saving." }
            requireValid(normalizedHeads.all { it.label.isNotBlank() }) { "Every fee head needs a label." }
            requireValid(normalizedHeads.all { it.amount > 0.0 }) { "Every fee amount must be greater than zero." }
            requireValid(normalizedHeads.map { it.label.lowercase(Locale.ROOT) }.distinct().size == normalizedHeads.size) {
                "Fee head labels must be unique."
            }

            val year = academicYear.trim()
            FieldValidators.academicYearError(year).orThrowValidation()

            val due = dueDate.trim()
            if (due.isNotBlank()) {
                requireValid(runCatching { LocalDate.parse(due) }.isSuccess) { "Due date must use YYYY-MM-DD format." }
            }
            requireValid(lateFineNote.trim().length <= 300) { "Late fine note must not exceed 300 characters." }
            requireValid(paymentNote.trim().length <= 1000) { "Payment instructions must not exceed 1,000 characters." }

            val editing = feeTabShift(session.value, _pickedShift.value)
            val updated = SessionFeeStructure(
                sessionId = sessionId,
                shift = editing,
                cadence = cadence,
                heads = normalizedHeads,
                academicYear = year.takeIf { it.isNotBlank() },
                dueDate = due.takeIf { it.isNotBlank() },
                lateFineNote = lateFineNote.trim().takeIf { it.isNotBlank() },
                paymentNote = paymentNote.trim().takeIf { it.isNotBlank() },
            )
            repo.saveSessionFee(updated, updatedBy)
            structureVersion++
            _structures.value = _structures.value + (editing to updated)
            _saved.value = true
        } finally {
            _saving.value = false
        }
    }

    fun consumeSaved() {
        _saved.value = false
    }
}
