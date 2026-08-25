package com.mbd.cmsteacher.feature.hub

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.teacherMenuSnapshot
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.StudentLinkRequestRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    teacherRepository: TeacherRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    notificationRepository: NotificationRepository,
    linkRequestRepository: StudentLinkRequestRepository,
) : ViewModel() {

    private val teacherId = sessionManager.accountKey.orEmpty()

    val snapshot = combine(
        teacherRepository.observeTeacher(teacherId),
        assignmentsProvider.observeMyAssignments(),
        notificationRepository.observeUnreadCount(NotificationTargetRole.TEACHER),
        linkRequestRepository.observePendingRequests(),
    ) { profile, assignments, unread, pending ->
        teacherMenuSnapshot(profile, assignments, unread, pending.size)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        teacherMenuSnapshot(null, emptyList(), 0, 0),
    )

    fun signOut() {
        sessionManager.signOut()
    }
}
