package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.StudentProfileEditController
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.ui.components.StudentProfileWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StudentProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: AcademicSessionRepository,
    fineRepository: FineRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = StudentProfileEditController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        rollNumber = checkNotNull(savedStateHandle["roll"]),
        sessionRepository = sessionRepository,
        fineRepository = fineRepository,
        issuedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val profile = controller.profile
    val session = controller.session
    val saveState = controller.saveState
    val fines = controller.fines
    val error = controller.error

    fun issueFine(category: String, amount: Double, reason: String) = controller.issueFine(category, amount, reason)
    fun deleteFine(id: String) = controller.deleteFine(id)
    fun save(profile: StudentProfile) = controller.save(profile)
    fun clearError() = controller.clearError()
}

@Composable
fun StudentProfileScreen(viewModel: StudentProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()
    val session by viewModel.session.collectAsState()
    val saveState by viewModel.saveState.collectAsState()
    val fines by viewModel.fines.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    StudentProfileWorkspace(
        loadedProfile = profile,
        session = session,
        fines = fines,
        saveState = saveState,
        errorMessage = errorMessage,
        onSave = viewModel::save,
        onIssueFine = viewModel::issueFine,
        onDeleteFine = { viewModel.deleteFine(it.id) },
        onClearError = viewModel::clearError,
    )
}
