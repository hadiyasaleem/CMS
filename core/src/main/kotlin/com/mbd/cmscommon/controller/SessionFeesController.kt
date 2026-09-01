package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.util.FieldValidators
import java.time.LocalDate
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class SessionFeesController(
    val sessionId: String,
    private val repo: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    private val updatedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _structure = MutableStateFlow<SessionFeeStructure?>(null)
    val structure: StateFlow<SessionFeeStructure?> = _structure.asStateFlow()

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
        launch {
            try {
                val loaded = repo.getSessionFee(sessionId)
                if (loadVersion == structureVersion) _structure.value = loaded
            } finally {
                _loading.value = false
            }
        }
    }

    fun save(cadence: FeeType, heads: List<FeeHead>, academicYear: String, dueDate: String, lateFineNote: String, paymentNote: String) = launch {
        try {
            _saving.value = true
            val normalizedHeads = heads.map { it.copy(label = it.label.trim()) }
            require(normalizedHeads.isNotEmpty()) { "Add at least one fee head before saving." }
            require(normalizedHeads.all { it.label.isNotBlank() }) { "Every fee head needs a label." }
            require(normalizedHeads.all { it.amount > 0.0 }) { "Every fee amount must be greater than zero." }
            require(normalizedHeads.map { it.label.lowercase(Locale.ROOT) }.distinct().size == normalizedHeads.size) {
                "Fee head labels must be unique."
            }

            val year = academicYear.trim()
            FieldValidators.academicYearError(year)?.let { throw IllegalStateException(it) }

            val due = dueDate.trim()
            if (due.isNotBlank()) {
                require(runCatching { LocalDate.parse(due) }.isSuccess) { "Due date must use YYYY-MM-DD format." }
            }
            require(lateFineNote.trim().length <= 300) { "Late fine note must not exceed 300 characters." }
            require(paymentNote.trim().length <= 1000) { "Payment instructions must not exceed 1,000 characters." }

            val updated = SessionFeeStructure(
                sessionId = sessionId,
                cadence = cadence,
                heads = normalizedHeads,
                academicYear = year.takeIf { it.isNotBlank() },
                dueDate = due.takeIf { it.isNotBlank() },
                lateFineNote = lateFineNote.trim().takeIf { it.isNotBlank() },
                paymentNote = paymentNote.trim().takeIf { it.isNotBlank() },
            )
            repo.saveSessionFee(updated, updatedBy)
            structureVersion++
            _structure.value = updated
            _saved.value = true
        } finally {
            _saving.value = false
        }
    }

    fun consumeSaved() {
        _saved.value = false
    }
}
