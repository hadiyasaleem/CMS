package com.mbd.cmsteacher.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.teacherNotificationAudience
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsBadgeViewModel @Inject constructor(
    private val repository: NotificationRepository,
    sessionManager: SessionManager,
    teacherRepository: TeacherRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
) : ViewModel() {
    // Scoped notices count only when they reach this teacher's department or the sessions/shifts they teach.
    private val audience = combine(
        teacherRepository.observeTeacher(sessionManager.accountKey.orEmpty()),
        assignmentsProvider.observeMyAssignments(),
        ::teacherNotificationAudience,
    )

    val unreadCount = audience
        .flatMapLatest { repository.observeUnreadCount(NotificationTargetRole.TEACHER, it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch { runCatching { repository.sync(NotificationTargetRole.TEACHER, audience.first()) } }
    }
}
