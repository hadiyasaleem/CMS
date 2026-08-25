package com.mbd.cmsstudent.feature.results

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.StudentResultsSnapshot
import com.mbd.cmscommon.domain.model.studentResultsSnapshot
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.ui.components.StudentResultsWorkspace
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class ResultsViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val marksRepository: SessionMarksRepository,
) : ViewModel() {

    private var currentSessionId: String? = null
    private var currentRollNumber: String? = null
    private val _refreshTrigger = MutableStateFlow(0)

    val snapshot: StateFlow<StudentResultsSnapshot?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                currentSessionId = null
                currentRollNumber = null
                flowOf<List<com.mbd.cmscommon.domain.model.SemesterGpa>?>(null)
            } else {
                currentSessionId = context.sessionId
                currentRollNumber = context.rollNumber
                _refreshTrigger.map { context.sessionId to context.rollNumber }
                    .map { (sessionId, rollNumber) -> marksRepository.getSemesterGpa(sessionId, rollNumber) }
            }
        }
        .map { results -> results?.let { studentResultsSnapshot(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}

@Composable
fun ResultsScreen(viewModel: ResultsViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()

    StudentResultsWorkspace(
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = null,
        onRetry = viewModel::refresh,
    )
}
