package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.SessionFeesController
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.ui.components.SessionFeeWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SessionFeesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = SessionFeesController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        repo = feeRepository,
        sessionRepository = sessionRepository,
        updatedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val sessionId = controller.sessionId
    val structure = controller.structure
    val session = controller.session
    val loading = controller.loading
    val saving = controller.saving
    val saved = controller.saved
    val error = controller.error

    fun save(
        cadence: FeeType,
        heads: List<FeeHead>,
        academicYear: String,
        dueDate: String,
        lateFineNote: String,
        paymentNote: String,
    ) = controller.save(cadence, heads, academicYear, dueDate, lateFineNote, paymentNote)

    fun consumeSaved() = controller.consumeSaved()
    fun clearError() = controller.clearError()
}

@Composable
fun SessionFeesScreen(viewModel: SessionFeesViewModel = hiltViewModel()) {
    val structure by viewModel.structure.collectAsState()
    val session by viewModel.session.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val saving by viewModel.saving.collectAsState()
    val saved by viewModel.saved.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    SessionFeeWorkspace(
        sessionId = viewModel.sessionId,
        session = session,
        structure = structure,
        loading = loading,
        saving = saving,
        saved = saved,
        errorMessage = errorMessage,
        onSave = viewModel::save,
        onConsumeSaved = viewModel::consumeSaved,
        onClearError = viewModel::clearError,
    )
}
