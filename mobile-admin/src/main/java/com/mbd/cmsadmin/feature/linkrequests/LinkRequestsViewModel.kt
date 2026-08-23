package com.mbd.cmsadmin.feature.linkrequests

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.LinkRequestsController
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LinkRequestsViewModel @Inject constructor(
    repository: StudentLinkRequestRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = LinkRequestsController(
        repository = repository,
        sessionRepository = sessionRepository,
        departmentRepository = departmentRepository,
        reviewerId = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val requests = controller.requests
    val sessions = controller.sessions
    val departments = controller.departments
    val verifications = controller.verifications
    val access = controller.access
    val loading = controller.loading
    val busyRequestId = controller.busyRequestId
    val rowErrors = controller.rowErrors
    val notice = controller.notice
    val error = controller.error

    fun refresh() = controller.refresh()
    fun approve(request: StudentLinkRequest) = controller.approve(request)
    fun reject(request: StudentLinkRequest, reason: String) = controller.reject(request, reason)
    fun consumeNotice() = controller.consumeNotice()
    fun clearError() = controller.clearError()
}
