package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.util.Outcome
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class StudentProfileEditController(
    val sessionId: String,
    val rollNumber: String,
    private val sessionRepository: AcademicSessionRepository,
    private val fineRepository: FineRepository,
    private val issuedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    private val _saveState = MutableStateFlow<Outcome<Unit>?>(null)
    val saveState: StateFlow<Outcome<Unit>?> = _saveState.asStateFlow()

    private val _fines = MutableStateFlow<List<Fine>>(emptyList())
    val fines: StateFlow<List<Fine>> = _fines.asStateFlow()

    init {
        launch {
            _profile.value = sessionRepository.getStudentProfile(sessionId, rollNumber)
                ?: StudentProfile(sessionId = sessionId, rollNumber = rollNumber, name = "")
        }
        loadFines()
    }

    private fun loadFines() = launch {
        _fines.value = fineRepository.getFines(sessionId, rollNumber)
    }

    fun issueFine(category: String, amount: Double, reason: String) = launch {
        val normalizedCategory = category.trim().uppercase(Locale.ROOT).ifBlank { "OTHER" }
        val normalizedReason = reason.trim()
        require(normalizedCategory in setOf("LIBRARY", "ATTENDANCE", "EXAM", "DISCIPLINARY", "OTHER")) {
            "Choose a valid fine category."
        }
        require(amount > 0.0) { "Fine amount must be greater than zero." }
        require(normalizedReason.isNotBlank()) { "Fine reason is required." }
        require(normalizedReason.length <= 300) { "Fine reason must not exceed 300 characters." }

        fineRepository.issueFine(sessionId, rollNumber, normalizedCategory, amount, normalizedReason, issuedBy)
        loadFines()
    }

    fun deleteFine(id: String) = launch {
        fineRepository.deleteFine(id)
        loadFines()
    }

    fun save(edited: StudentProfile) = launch {
        try {
            _saveState.value = Outcome.Loading
            require(edited.sessionId == sessionId && edited.rollNumber == rollNumber) {
                "Student identity cannot be changed from this profile."
            }
            val normalized = edited.copy(
                name = edited.name.trim(),
                fatherName = edited.fatherName?.trim(),
                guardianName = edited.guardianName?.trim(),
                currentAddress = edited.currentAddress?.trim(),
                permanentAddress = edited.permanentAddress?.trim(),
                domicile = edited.domicile?.trim(),
                religion = edited.religion?.trim(),
                emergencyContactName = edited.emergencyContactName?.trim(),
                emergencyContactRelation = edited.emergencyContactRelation?.trim(),
                specialNeeds = edited.specialNeeds?.trim(),
            )
            validateStudentProfile(normalized)?.let { throw IllegalArgumentException(it) }
            sessionRepository.saveStudentProfile(normalized)
            _saveState.value = Outcome.Success(Unit)
            _profile.value = normalized
        } catch (t: Throwable) {
            _saveState.value = Outcome.Error(t.userMessageLogged("Could not save the student profile."), t)
        }
    }
}
