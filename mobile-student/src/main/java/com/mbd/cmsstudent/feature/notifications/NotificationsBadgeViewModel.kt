package com.mbd.cmsstudent.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsBadgeViewModel @Inject constructor(
    private val repository: NotificationRepository,
    currentStudentProvider: CurrentStudentProvider,
) : ViewModel() {
    val unreadCount = currentStudentProvider.observeContext()
        .flatMapLatest { context ->
            repository.observeUnreadCount(
                NotificationTargetRole.STUDENT,
                NotificationAudienceContext(sessionId = context?.sessionId, departmentId = context?.deptId),
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch { runCatching { repository.sync(NotificationTargetRole.STUDENT) } }
    }
}
