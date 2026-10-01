package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Edits the college-wide base fee structure: one per shift (Morning and Evening can differ). Every session's
 * structure for a shift follows the base until the session saves one of its own.
 */
class CollegeFeesController(
    private val repo: SessionFeeRepository,
    private val updatedBy: String,
    scope: CoroutineScope,
    initialShift: Session = Session.MORNING,
) : ScreenController(scope) {

    val shifts: List<Session> = Session.entries

    private val _shift = MutableStateFlow(initialShift)
    val shift: StateFlow<Session> = _shift.asStateFlow()

    private val _structures = MutableStateFlow<Map<Session, SessionFeeStructure>>(emptyMap())
    val structures: StateFlow<List<SessionFeeStructure>> = _structures.map { it.values.sortedBy { fee -> fee.shift } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** The open shift's base, or null when it has none yet. */
    val structure: StateFlow<SessionFeeStructure?> = combine(_structures, _shift) { all, current -> all[current] }
        .stateIn(scope, SharingStarted.Eagerly, null)

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved.asStateFlow()
    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    init {
        launch("load the college fee structure") {
            try {
                _structures.value = repo.getCollegeFees().associateBy { it.shift }
            } finally {
                _loading.value = false
            }
        }
    }

    fun selectShift(picked: Session) {
        _shift.value = picked
        _saved.value = false
    }

    fun save(cadence: FeeType, heads: List<FeeHead>, academicYear: String, dueDate: String, paymentNote: String) = launch("save the college fee structure") {
        try {
            _saving.value = true
            val updated = buildFeeStructure("", _shift.value, cadence, heads, academicYear, dueDate, paymentNote)
            repo.saveCollegeFee(updated, updatedBy)
            _structures.value = _structures.value + (_shift.value to updated)
            _saved.value = true
        } finally {
            _saving.value = false
        }
    }

    fun consumeSaved() {
        _saved.value = false
    }
}
