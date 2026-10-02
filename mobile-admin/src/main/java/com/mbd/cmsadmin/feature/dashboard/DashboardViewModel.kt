package com.mbd.cmsadmin.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.DashboardController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Live institutional counters, all observed from the Room cache. */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    linkRequestRepository: StudentLinkRequestRepository,
) : ViewModel() {
    private val controller = DashboardController(
        sessionRepository, teacherRepository, departmentRepository, linkRequestRepository, viewModelScope,
    )
    val state = controller.state
}
