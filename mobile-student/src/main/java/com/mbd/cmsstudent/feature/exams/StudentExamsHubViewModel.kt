package com.mbd.cmsstudent.feature.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentExamsHubController
import com.mbd.cmscommon.domain.model.StudentExamsHubSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private data class ExamsHubState(
    val snapshot: StudentExamsHubSnapshot?,
    val loading: Boolean,
    val error: String?,
)

@HiltViewModel
class StudentExamsHubViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val marksRepository: SessionMarksRepository,
    private val datesheetRepository: DatesheetRepository,
    private val sessionRepository: AcademicSessionRepository,
) : ViewModel() {

    private var controller: StudentExamsHubController? = null

    // Single flatMapLatest is the one source of truth: snapshot/loading/error below are all
    // projections of this same state, so there is no race over which context's controller they read.
    private val state = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                controller = null
                flowOf(ExamsHubState(null, loading = false, error = null))
            } else {
                val c = StudentExamsHubController(context.sessionId, context.rollNumber, marksRepository, datesheetRepository, sessionRepository, viewModelScope)
                controller = c
                combine(c.snapshot, c.loading, c.loadError) { snap, loading, error -> ExamsHubState(snap, loading, error) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ExamsHubState(null, loading = true, error = null))

    val snapshot = state.map { it.snapshot }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val loading = state.map { it.loading }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), true)
    val error = state.map { it.error }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        controller?.refresh()
    }
}
