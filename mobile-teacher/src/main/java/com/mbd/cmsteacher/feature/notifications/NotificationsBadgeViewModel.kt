package com.mbd.cmsteacher.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class NotificationsBadgeViewModel @Inject constructor(
    private val repository: NotificationRepository,
) : ViewModel() {
    val unreadCount = repository.observeUnreadCount(NotificationTargetRole.TEACHER)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    init {
        viewModelScope.launch { runCatching { repository.sync(NotificationTargetRole.TEACHER) } }
    }
}
