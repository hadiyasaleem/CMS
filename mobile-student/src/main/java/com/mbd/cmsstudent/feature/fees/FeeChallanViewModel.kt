package com.mbd.cmsstudent.feature.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.StudentFeeSnapshot
import com.mbd.cmscommon.domain.model.studentFeeSnapshot
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
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
class FeeChallanViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val feeRepository: SessionFeeRepository,
) : ViewModel() {

    private var currentSessionId: String? = null
    private val _refreshTrigger = MutableStateFlow(0)

    val snapshot: StateFlow<StudentFeeSnapshot?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                currentSessionId = null
                flowOf<StudentFeeSnapshot?>(null)
            } else {
                currentSessionId = context.sessionId
                _refreshTrigger.map {
                    val structure = runCatching { feeRepository.getSessionFee(context.sessionId) }.getOrNull()
                    studentFeeSnapshot(structure, LocalDate.now())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}
