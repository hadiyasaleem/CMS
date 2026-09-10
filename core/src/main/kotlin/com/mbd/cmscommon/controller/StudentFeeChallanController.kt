package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.FeeChallanHeader
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.feeChallanNumber
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class StudentFeeChallanController(
    private val sessionId: String,
    private val rollNumber: String,
    private val feeRepository: SessionFeeRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val departmentRepository: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _fee = MutableStateFlow<SessionFeeStructure?>(null)
    val fee: StateFlow<SessionFeeStructure?> = _fee.asStateFlow()

    private val _header = MutableStateFlow<FeeChallanHeader?>(null)
    val header: StateFlow<FeeChallanHeader?> = _header.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        load(fetchRemote = false)
    }

    private fun load(fetchRemote: Boolean) {
        clearError()
        launch {
            _loading.value = true
            try {
                if (fetchRemote) feeRepository.syncSession(sessionId)
                _fee.value = feeRepository.getSessionFee(sessionId)

                val resolvedSession = sessionRepository.observeSession(sessionId).first()
                val profile = sessionRepository.getStudentProfile(sessionId, rollNumber)
                val department = resolvedSession?.deptId?.let { departmentRepository.getDepartment(it) }

                _header.value = FeeChallanHeader(
                    studentName = profile?.name?.takeIf { it.isNotBlank() } ?: rollNumber,
                    rollNumber = rollNumber,
                    fatherName = profile?.fatherName?.takeIf { it.isNotBlank() } ?: profile?.guardianName,
                    sessionLabel = resolvedSession?.label ?: sessionId,
                    shift = resolvedSession?.shift?.name ?: "",
                    deptCode = department?.code,
                    challanNumber = feeChallanNumber(sessionId, rollNumber, _fee.value?.cadence?.name ?: "FEE"),
                    issueDate = LocalDate.now().toString(),
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun refresh() {
        load(fetchRemote = true)
    }
}
