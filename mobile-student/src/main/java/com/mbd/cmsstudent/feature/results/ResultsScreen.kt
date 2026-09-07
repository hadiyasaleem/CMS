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
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
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

    private data class ResultsState(val snapshot: StudentResultsSnapshot?, val error: String?)

    private val state = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                currentSessionId = null
                currentRollNumber = null
                flowOf(ResultsState(null, null))
            } else {
                currentSessionId = context.sessionId
                currentRollNumber = context.rollNumber
                _refreshTrigger.map {
                    val result = runCatching { marksRepository.getSemesterGpa(context.sessionId, context.rollNumber) }
                    val results = result.orLogCritical("ResultsViewModel.getSemesterGpa")
                    val error = if (result.isFailure) "Could not load your results. Pull to refresh to try again." else null
                    ResultsState(results?.let { studentResultsSnapshot(it) }, error)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ResultsState(null, null))

    val snapshot = state.map { it.snapshot }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val error = state.map { it.error }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}

@Composable
fun ResultsScreen(viewModel: ResultsViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    StudentResultsWorkspace(
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = errorMessage,
        onRetry = viewModel::refresh,
    )
}
