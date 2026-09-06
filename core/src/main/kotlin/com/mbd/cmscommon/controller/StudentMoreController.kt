package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.CalendarViewerContext
import com.mbd.cmscommon.domain.model.CalendarViewerRole
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.model.StudentMoreSnapshot
import com.mbd.cmscommon.domain.model.studentMoreSnapshot
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

class StudentMoreController(
    private val sessionId: String,
    private val departmentId: String,
    private val rollNumber: String,
    private val calendarRepository: CalendarRepository,
    private val feeRepository: SessionFeeRepository,
    private val notificationRepository: NotificationRepository,
    private val sessionRepository: AcademicSessionRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _snapshot = MutableStateFlow(emptySnapshot())
    val snapshot: StateFlow<StudentMoreSnapshot> = _snapshot.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _loadError = MutableStateFlow<String?>(null)
    val loadError: StateFlow<String?> = _loadError.asStateFlow()

    private var version = 0

    init {
        refresh(fetchRemote = false)
    }

    fun refresh(fetchRemote: Boolean = true) {
        version++
        val request = version
        launch {
            _loading.value = true
            _loadError.value = null
            coroutineScope {
                val events = async {
                    runCatching {
                        if (fetchRemote) calendarRepository.sync()
                        calendarRepository.getEvents()
                    }
                }
                val fee = async {
                    runCatching {
                        if (fetchRemote) feeRepository.syncSession(sessionId)
                        feeRepository.getSessionFee(sessionId)
                    }
                }
                val profile = async { runCatching { sessionRepository.getStudentProfile(sessionId, rollNumber) } }
                val unread = async {
                    runCatching {
                        val audience = NotificationAudienceContext(sessionId, departmentId)
                        if (fetchRemote) notificationRepository.sync(NotificationTargetRole.STUDENT, audience)
                        notificationRepository.observeUnreadCount(NotificationTargetRole.STUDENT, audience).first()
                    }
                }

                val eventResult = events.await()
                val feeResult = fee.await()
                val profileResult = profile.await()
                val unreadResult = unread.await()
                val results = listOf(eventResult, feeResult, profileResult, unreadResult)

                if (request == version) {
                    val viewer = CalendarViewerContext(CalendarViewerRole.STUDENT, departmentId, setOf(sessionId))
                    _snapshot.value = studentMoreSnapshot(
                        eventResult.getOrDefault(emptyList()),
                        feeResult.getOrNull(),
                        unreadResult.getOrDefault(0),
                        profileResult.getOrNull(),
                        viewer,
                        LocalDate.now(),
                    )
                    _loadError.value = results.firstNotNullOfOrNull { it.exceptionOrNull() }
                        ?.userMessageLogged("Some portal summaries could not be loaded.")
                    _loading.value = false
                }
            }
        }
    }

    private fun emptySnapshot(): StudentMoreSnapshot {
        val viewer = CalendarViewerContext(CalendarViewerRole.STUDENT, departmentId, setOf(sessionId))
        return studentMoreSnapshot(emptyList(), null, 0, null, viewer, LocalDate.now())
    }
}
