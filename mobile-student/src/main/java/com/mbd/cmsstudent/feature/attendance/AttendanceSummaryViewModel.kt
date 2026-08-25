package com.mbd.cmsstudent.feature.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentAttendanceController
import com.mbd.cmscommon.controller.StudentAttendanceSnapshot
import com.mbd.cmscommon.controller.studentAttendanceSnapshot
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AttendanceSummaryViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val attendanceRepository: SessionAttendanceRepository,
    private val curriculumRepository: CurriculumRepository,
) : ViewModel() {

    private var controller: StudentAttendanceController? = null

    val snapshot = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                controller = null
                flowOf<StudentAttendanceSnapshot?>(null)
            } else {
                val c = StudentAttendanceController(context.sessionId, context.rollNumber, attendanceRepository, curriculumRepository, viewModelScope)
                controller = c
                c.rows.map { rows -> studentAttendanceSnapshot(rows) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { controller?.refresh() }
    }
}
