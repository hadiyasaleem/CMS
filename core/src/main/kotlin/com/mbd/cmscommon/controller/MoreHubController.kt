package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.model.MoreHubSnapshot
import com.mbd.cmscommon.domain.model.MoreSummarySource
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.moreHubSnapshot
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope

class MoreHubController(
    private val accountKey: String,
    private val administratorRepository: AdministratorRepository,
    private val notificationRepository: NotificationRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _snapshot = MutableStateFlow<MoreHubSnapshot?>(null)
    val snapshot: StateFlow<MoreHubSnapshot?> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var loadVersion = 0

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        loadVersion++
        val version = loadVersion
        launch {
            _loading.value = true
            _loadError.value = null
            supervisorScope {
                val administratorsDeferred = async {
                    runCatching {
                        if (fetchRemote) administratorRepository.sync()
                        administratorRepository.observeAdministrators().first()
                    }
                }
                val authoredDeferred = async {
                    runCatching {
                        if (fetchRemote) notificationRepository.syncAuthoredByCurrentUser(accountKey)
                        notificationRepository.observeAuthoredByCurrentUser(accountKey).first()
                    }
                }
                val unreadDeferred = async {
                    runCatching {
                        if (fetchRemote) notificationRepository.sync(NotificationTargetRole.ADMIN)
                        notificationRepository.observeUnreadCount(NotificationTargetRole.ADMIN).first()
                    }
                }

                val administratorsResult = administratorsDeferred.await()
                val authoredResult = authoredDeferred.await()
                val unreadResult = unreadDeferred.await()

                if (version == loadVersion) {
                    val unavailableSources = buildSet {
                        if (administratorsResult.isFailure) add(MoreSummarySource.ADMINISTRATORS)
                        if (authoredResult.isFailure) add(MoreSummarySource.AUTHORED_NOTIFICATIONS)
                        if (unreadResult.isFailure) add(MoreSummarySource.UNREAD_NOTIFICATIONS)
                    }

                    _snapshot.value = moreHubSnapshot(
                        accountKey,
                        administratorsResult.getOrDefault(emptyList<AdministratorAccount>()),
                        authoredResult.getOrDefault(emptyList<Notification>()),
                        unreadResult.getOrDefault(0),
                        unavailableSources,
                    )
                    _loadError.value = listOf(administratorsResult, authoredResult, unreadResult)
                        .firstNotNullOfOrNull { it.exceptionOrNull() }
                        ?.userMessage("Some account summaries could not be loaded.")
                    _loading.value = false
                }
            }
        }
    }
}
