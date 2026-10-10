package com.mbd.cmsstudent.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentHomeController
import com.mbd.cmscommon.domain.model.StudentHomeSnapshot
import com.mbd.cmscommon.domain.model.studentHomeSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val sessionRepository: AcademicSessionRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val timetableRepository: SessionTimetableRepository,
) : ViewModel() {

    private var controller: StudentHomeController? = null

    val snapshot = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                controller = null
                flowOf<StudentHomeSnapshot?>(null)
            } else {
                val c = StudentHomeController(context.sessionId, context.rollNumber, sessionRepository, attendanceRepository, timetableRepository, viewModelScope)
                controller = c
                combine(c.ui, c.me) { ui, student ->
                    studentHomeSnapshot(
                        name = context.name.ifBlank { "Student" },
                        rollNumber = context.rollNumber,
                        session = context.session,
                        gpa = context.gpa,
                        cgpa = context.cgpa,
                        overallAttendance = ui.overallPercent,
                        subjectCount = ui.subjectCount,
                        todaysClasses = ui.todaysClasses,
                        nextClassId = ui.nextClassId,
                        weakestSubject = ui.weakestSubject,
                        shift = student?.shift,
                    )
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { controller?.refresh() }
    }
}
