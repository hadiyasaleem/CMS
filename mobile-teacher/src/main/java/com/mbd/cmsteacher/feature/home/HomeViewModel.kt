package com.mbd.cmsteacher.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.TeacherHomeSnapshot
import com.mbd.cmscommon.domain.model.teacherHomeSnapshot
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class HomeViewModel @Inject constructor(
    sessionManager: SessionManager,
    timetableRepository: SessionTimetableRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {

    private val teacherId = sessionManager.accountKey.orEmpty()

    val snapshot = combine(
        timetableRepository.observeMyPeriods(teacherId),
        assignmentsProvider.observeMyAssignments(),
    ) { periods, assignments ->
        teacherHomeSnapshot(teacherId, periods, assignments, LocalDate.now(), LocalTime.now())
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        teacherHomeSnapshot(teacherId, emptyList(), emptyList(), LocalDate.now(), LocalTime.now()),
    )
}
