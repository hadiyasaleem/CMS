package com.mbd.cmsadmin.feature.academics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mbd.cmscommon.util.Outcome
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

    val loadedProfile = profile
    if (loadedProfile == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    StudentProfileWorkspace(
        loadedProfile = loadedProfile,
        session = session,
        fines = fines,
        saveOutcome = saveState ?: Outcome.Success(Unit),
        errorMessage = errorMessage,
        onSave = viewModel::save,
        onIssueFine = viewModel::issueFine,
        onDeleteFine = { viewModel.deleteFine(it.id) },
        onClearError = viewModel::clearError,
    )
}
