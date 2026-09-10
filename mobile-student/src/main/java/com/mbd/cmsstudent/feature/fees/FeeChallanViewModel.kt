package com.mbd.cmsstudent.feature.fees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.FeeChallanHeader
import com.mbd.cmscommon.domain.model.StudentFeeSnapshot
import com.mbd.cmscommon.domain.model.feeChallanNumber
import com.mbd.cmscommon.domain.model.studentFeeSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.util.orLogCritical
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val departmentRepository: DepartmentRepository,
    private val academicSessionRepository: AcademicSessionRepository,
) : ViewModel() {

    private var currentSessionId: String? = null
    private val _refreshTrigger = MutableStateFlow(0)
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _header = MutableStateFlow<FeeChallanHeader?>(null)
    val header: StateFlow<FeeChallanHeader?> = _header.asStateFlow()

    val snapshot: StateFlow<StudentFeeSnapshot?> = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                currentSessionId = null
                _header.value = null
                flowOf<StudentFeeSnapshot?>(null)
            } else {
                currentSessionId = context.sessionId
                _refreshTrigger.map {
                    val structureResult = runCatching { feeRepository.getSessionFee(context.sessionId) }
                    val structure = structureResult.orLogCritical("FeeChallanViewModel.getSessionFee")
                    _error.value = if (structureResult.isFailure) "Could not load fee details. Pull to refresh to try again." else null

                    val department = context.deptId.let { runCatching { departmentRepository.getDepartment(it) }.getOrNull() }
                    val profile = runCatching { academicSessionRepository.getStudentProfile(context.sessionId, context.rollNumber) }.getOrNull()
                    _header.value = FeeChallanHeader(
                        studentName = context.name.ifBlank { context.rollNumber },
                        rollNumber = context.rollNumber,
                        fatherName = profile?.fatherName?.takeIf { it.isNotBlank() } ?: profile?.guardianName,
                        sessionLabel = context.session?.label ?: context.sessionId,
                        shift = context.session?.shift?.name ?: "",
                        deptCode = department?.code,
                        challanNumber = feeChallanNumber(context.sessionId, context.rollNumber, structure?.cadence?.name ?: "FEE"),
                        issueDate = LocalDate.now().toString(),
                    )

                    studentFeeSnapshot(structure, LocalDate.now())
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { _refreshTrigger.value += 1 }
    }
}
