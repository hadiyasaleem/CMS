package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.NotificationPublisherKind
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.controller.inScope
import com.mbd.cmscommon.controller.departmentScopeOptions
import com.mbd.cmscommon.domain.model.ShiftScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.NotificationDraft
import com.mbd.cmscommon.controller.NotificationPublishAccess
import com.mbd.cmscommon.controller.NotificationsController
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn

private val NoticeGold = ModWarn
private val NoticeRed = ModAccent

enum class NoticeTab(val label: String) {
    INBOX("Inbox"),
    SENT("Sent"),
}

@Composable
fun NotificationControllerWorkspace(controller: NotificationsController, modifier: Modifier = Modifier) {
    val allInbox by controller.inbox.collectAsState()
    val allSent by controller.sent.collectAsState()
    val departments by controller.departments.collectAsState()
    val publishSessions by controller.publishSessions.collectAsState()
    val teachingShifts by controller.teachingShifts.collectAsState()
    // Department -> Session -> Shift filter over each notice's audience (students are already scoped to theirs).
    var filterScope by remember { mutableStateOf(ShiftScope.ALL) }
    val showScopeFilter = controller.viewerRole != NotificationTargetRole.STUDENT && publishSessions.isNotEmpty()
    val filterDepartments = if (departments.isNotEmpty()) {
        departmentScopeOptions(departments)
    } else {
        publishSessions.map { it.deptId to it.deptId.uppercase() }.distinct()
    }
    val inbox = allInbox.inScope(filterScope, publishSessions)
    val sent = allSent.inScope(filterScope, publishSessions)
    val publishAccess by controller.publishAccess.collectAsState()
    val loading by controller.loading.collectAsState()
    val busyActionId by controller.busyActionId.collectAsState()
    val rowErrors by controller.rowErrors.collectAsState()
    val composeError by controller.composeError.collectAsState()
    val notice by controller.notice.collectAsState()
    val loadError by controller.error.collectAsState()

    var tab by remember { mutableStateOf(NoticeTab.INBOX) }
    var showCompose by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Notification?>(null) }

    val canPublish = publishAccess == NotificationPublishAccess.ALLOWED

    Box(modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showScopeFilter) {
            item { ShiftScopeSelector(filterScope, filterDepartments, publishSessions, { filterScope = it }) }
        }

        if (!loadError.isNullOrBlank()) {
            item { CmsNotice(loadError ?: "", tone = NoticeTone.Error, actionLabel = "Retry", onAction = controller::refresh) }
        }
        if (!composeError.isNullOrBlank()) {
            item { CmsNotice(composeError ?: "", tone = NoticeTone.Error, onDismiss = controller::clearComposeError) }
        }
        if (!notice.isNullOrBlank()) {
            item { CmsNotice(notice ?: "", tone = NoticeTone.Success, onDismiss = controller::consumeNotice) }
        }

        val showTabs = controller.publisherKind != NotificationPublisherKind.NONE
        if (showTabs) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NoticeTab.entries.forEach { option ->
                        CmsChip(option.label, selected = tab == option, onClick = { tab = option })
                    }
                }
            }
        }

        val effectiveTab = if (showTabs) tab else NoticeTab.INBOX
        val list = if (effectiveTab == NoticeTab.INBOX) inbox else sent
        when {
            loading -> items(3) { SkeletonRow() }
            list.isEmpty() -> item { NotificationEmpty(effectiveTab) }
            else -> items(list, key = { it.notificationId }) { notification ->
                NotificationCard(
                    notification = notification,
                    departmentLabel = departments.firstOrNull { it.deptId == notification.targetDeptId }?.name,
                    sentView = effectiveTab == NoticeTab.SENT,
                    busy = busyActionId == notification.notificationId,
                    rowError = rowErrors[notification.notificationId],
                    onDelete = { pendingDelete = notification },
                )
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
        if (canPublish) {
            CmsFab(
                onClick = { showCompose = true },
                contentDescription = "Compose",
                modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
            )
        }
    }

    if (showCompose) {
        ComposeNotificationDialog(
            viewerRole = controller.viewerRole,
            teacherComposer = controller.publisherKind == NotificationPublisherKind.TEACHER,
            departments = departments,
            sessions = teacherShiftSessions(publishSessions, teachingShifts),
            busy = busyActionId == NotificationsController.SEND_ACTION,
            onDismiss = { showCompose = false },
            onSend = { draft -> controller.send(draft); showCompose = false },
        )
    }

    pendingDelete?.let { notification ->
        ConfirmDestructiveActionDialog(
            title = "Delete notification",
            dependentSummary = "\"${notification.title}\" will be permanently removed.",
            onConfirm = { controller.delete(notification); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    departmentLabel: String?,
    sentView: Boolean,
    busy: Boolean,
    rowError: String?,
    onDelete: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            NotificationListItem(notification, modifier = Modifier.padding(0.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge((notification.targetRole?.name ?: "ALL"), BadgeTone.Neutral)
                if (departmentLabel != null) StatusBadge(departmentLabel, BadgeTone.Neutral)
            }
            if (!rowError.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(rowError, color = NoticeRed, style = MaterialTheme.typography.bodySmall)
            }
            if (sentView) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = onDelete, enabled = !busy) { Text(if (busy) "Working..." else "Delete", color = CmsTheme.colors.accent) }
            }
        }
    }
}

@Composable
private fun NotificationEmpty(tab: NoticeTab) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Text(
            if (tab == NoticeTab.INBOX) "No notifications right now." else "You have not sent any notifications yet.",
            modifier = Modifier.padding(24.dp),
            color = ModMuted,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ComposeNotificationDialog(
    viewerRole: NotificationTargetRole,
    teacherComposer: Boolean,
    departments: List<com.mbd.cmscommon.domain.model.Department>,
    sessions: List<com.mbd.cmscommon.domain.model.AcademicSession>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSend: (NotificationDraft) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var targetRole by remember { mutableStateOf(NotificationTargetRole.ALL) }
    var priority by remember { mutableStateOf(NotificationPriority.NORMAL) }
    // Department -> Session -> Shift target; every level is optional (college-wide when none is chosen).
    var target by remember { mutableStateOf(ShiftScope.ALL) }

    val titleValid = title.trim().length in 3..120
    val bodyValid = body.trim().length in 5..2000
    val teacherAudience = teacherComposer

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compose notification", style = MaterialTheme.typography.headlineSmall) },
        text = { DialogScrollBody {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = body, onValueChange = { body = it }, label = { Text("Message") }, modifier = Modifier.fillMaxWidth(), minLines = 3)
                if (!teacherAudience) {
                    Spacer(Modifier.height(10.dp))
                    Text("AUDIENCE", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // Everyone first, then the narrower audiences.
                        listOf(NotificationTargetRole.ALL, NotificationTargetRole.ADMIN, NotificationTargetRole.TEACHER, NotificationTargetRole.STUDENT).forEach { role ->
                            CmsChip(role.name, selected = targetRole == role, onClick = { targetRole = role })
                        }
                    }
                    if (targetRole != NotificationTargetRole.ADMIN) {
                        Spacer(Modifier.height(10.dp))
                        ShiftScopeSelector(target, departmentScopeOptions(departments), sessions, { target = it }, label = "REACHES")
                        Spacer(Modifier.height(4.dp))
                        Text(audienceHint(target), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                    // Teachers notify students of the sessions (and shifts) they teach.
                    ShiftScopeSelector(target, sessions.map { it.deptId to it.deptId.uppercase() }.distinct(), sessions, { target = it }, label = "YOUR CLASS")
                    Spacer(Modifier.height(4.dp))
                    Text(if (target.sessionId == null) "Choose one of your sessions." else audienceHint(target), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(10.dp))
                Text("PRIORITY", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NotificationPriority.entries.forEach { option ->
                        CmsChip(option.name, selected = priority == option, onClick = { priority = option })
                    }
                }
            }
        }},
        confirmButton = {
            TextButton(
                onClick = {
                    onSend(
                        NotificationDraft(
                            title = title,
                            body = body,
                            targetRole = if (teacherAudience) NotificationTargetRole.STUDENT else targetRole,
                            priority = priority,
                            departmentId = target.deptId.takeIf { teacherAudience || targetRole != NotificationTargetRole.ADMIN },
                            sessionId = target.sessionId.takeIf { teacherAudience || targetRole != NotificationTargetRole.ADMIN },
                            shift = target.shift.takeIf { teacherAudience || targetRole != NotificationTargetRole.ADMIN },
                        ),
                    )
                },
                enabled = titleValid && bodyValid && !busy && (!teacherAudience || target.sessionId != null),
            ) { Text(if (busy) "Sending..." else "Send notification") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}

/** "Reaches: IT 2022–2026, Evening shift" -- who a scoped notice or event is for. */
private fun audienceHint(target: ShiftScope): String = when {
    target.isEmpty -> "Reaches the whole college."
    target.sessionId == null -> "Reaches every session and both shifts of this department."
    target.shift == null -> "Reaches both shifts of this session."
    else -> "Reaches only the ${target.shift?.label} shift of this session."
}

/** For a teacher, each session offers only the shifts they teach in it (an admin's map is empty: all shifts). */
private fun teacherShiftSessions(sessions: List<AcademicSession>, teachingShifts: Map<String, Set<Session>>): List<AcademicSession> =
    sessions.map { session -> ShiftMode.of(teachingShifts[session.sessionId].orEmpty())?.let { session.copy(shiftMode = it) } ?: session }
