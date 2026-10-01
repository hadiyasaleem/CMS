package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.shiftForRoll
import kotlinx.coroutines.flow.first
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
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
    private val issuedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val session: StateFlow<AcademicSession?> =
        sessionRepository.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _profile = MutableStateFlow<StudentProfile?>(null)
    val profile: StateFlow<StudentProfile?> = _profile.asStateFlow()

    private val _saveState = MutableStateFlow<Outcome<Unit>?>(null)
    val saveState: StateFlow<Outcome<Unit>?> = _saveState.asStateFlow()

    private val _photoBusy = MutableStateFlow(false)
    val photoBusy: StateFlow<Boolean> = _photoBusy.asStateFlow()

    init {
        launch("load the student's profile") {
            _profile.value = sessionRepository.getStudentProfile(sessionId, rollNumber)
                ?: StudentProfile(
                    sessionId = sessionId,
                    rollNumber = rollNumber,
                    name = "",
                    // No profile cached yet: the roll number's serial block decides the shift.
                    shift = sessionRepository.observeSession(sessionId).first()?.let { shiftForRoll(it, rollNumber) } ?: Session.MORNING,
                )
        }
    }

    fun delinkAccount() = launch("unlink the account") {
        try {
            _saveState.value = Outcome.Loading
            sessionRepository.delinkStudent(sessionId, rollNumber, reviewedBy = issuedBy)
            _profile.value = _profile.value?.copy(linkedEmail = "")
            _saveState.value = Outcome.Success(Unit)
        } catch (t: Throwable) {
            _saveState.value = Outcome.Error(t.userMessageLogged("Couldn't unlink the account."), t)
        }
    }

    fun save(edited: StudentProfile) = launch("save the student's profile") {
        try {
            _saveState.value = Outcome.Loading
            requireValid(edited.sessionId == sessionId && edited.rollNumber == rollNumber) {
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
            validateStudentProfile(normalized).orThrowValidation()
            profileShiftError(session.value, normalized).orThrowValidation()
            if (normalized.isCr || normalized.isGr) {
                val classmates = sessionRepository.observeAllStudentProfiles().first().filter { it.sessionId == sessionId }
                classRoleConflict(normalized, classmates).orThrowValidation()
            }
            sessionRepository.saveStudentProfile(normalized)
            _saveState.value = Outcome.Success(Unit)
            _profile.value = normalized
        } catch (t: Throwable) {
            _saveState.value = Outcome.Error(t.userMessageLogged("Couldn't save the student's profile."), t)
        }
    }

    fun uploadPhoto(imageBytes: ByteArray, mimeType: String) = launch("upload the photo") {
        try {
            _photoBusy.value = true
            sessionRepository.uploadStudentPhoto(sessionId, rollNumber, imageBytes, mimeType)
            _profile.value = sessionRepository.getStudentProfile(sessionId, rollNumber)
        } finally {
            _photoBusy.value = false
        }
    }

    /** For a picked photo failing to read/decode before [uploadPhoto] ever gets called. */
    fun reportPhotoPickFailure(t: Throwable) = launch("read the selected photo") { throw t }
}
