package com.mbd.cmscommon.ui.components

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
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn

private val NoticeGreen = ModSuccess
private val NoticeGold = ModWarn
private val NoticeRed = ModAccent

enum class NoticeTab(val label: String) {
    INBOX("Inbox"),
    SENT("Sent"),
}

@Composable
fun NotificationControllerWorkspace(controller: NotificationsController, modifier: Modifier = Modifier) {
    val inbox by controller.inbox.collectAsState()
    val sent by controller.sent.collectAsState()
    val departments by controller.departments.collectAsState()
    val publishSessions by controller.publishSessions.collectAsState()
    val publishAccess by controller.publishAccess.collectAsState()
    val loading by controller.loading.collectAsState()
    val busyActionId by controller.busyActionId.collectAsState()
    val rowErrors by controller.rowErrors.collectAsState()
    val composeError by controller.composeError.collectAsState()
    val notice by controller.notice.collectAsState()

    var tab by remember { mutableStateOf(NoticeTab.INBOX) }
    var showCompose by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<Notification?>(null) }

    val canPublish = publishAccess == NotificationPublishAccess.ALLOWED
    val urgentCount = inbox.count { it.priority == NotificationPriority.URGENT }

    Box(modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { NotificationHero(controller.viewerRole, inbox.size) }

        if (!composeError.isNullOrBlank()) {
            item { NotificationNotice(composeError ?: "", NoticeRed, controller::clearComposeError) }
        }
        if (!notice.isNullOrBlank()) {
            item { NotificationNotice(notice ?: "", NoticeGreen, controller::consumeNotice) }
        }

        item { NotificationSummaryCard(inbox.size, sent.size, urgentCount) }

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

        val list = if (tab == NoticeTab.INBOX) inbox else sent
        when {
            loading -> items(3) { SkeletonRow() }
            list.isEmpty() -> item { NotificationEmpty(tab) }
            else -> items(list, key = { it.notificationId }) { notification ->
                NotificationCard(
                    notification = notification,
                    departmentLabel = departments.firstOrNull { it.deptId == notification.targetDeptId }?.name,
                    sentView = tab == NoticeTab.SENT,
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
            departments = departments,
            sessions = publishSessions,
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
private fun NotificationHero(viewerRole: NotificationTargetRole, inboxCount: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ACCOUNT & COMMUNICATIONS", color = NoticeGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Notifications", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$inboxCount notice(s) for ${viewerRole.name.lowercase().replaceFirstChar { it.uppercase() }}s", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NotificationSummaryCard(inbox: Int, sent: Int, urgent: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        NoticeMetric("Inbox", inbox.toString(), Modifier.weight(1f))
        NoticeMetric("Sent", sent.toString(), Modifier.weight(1f))
        NoticeMetric("Urgent", urgent.toString(), Modifier.weight(1f), alert = urgent > 0)
    }
}

@Composable
private fun NoticeMetric(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) NoticeRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
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
private fun NotificationNotice(message: String, color: Color, onDismiss: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss", color = color) }
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
    var deptId by remember { mutableStateOf<String?>(null) }
    var sessionId by remember { mutableStateOf<String?>(null) }

    val titleValid = title.trim().length in 3..120
    val bodyValid = body.trim().length in 5..2000
    val teacherAudience = sessions.isNotEmpty() && departments.isEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compose notification", style = MaterialTheme.typography.headlineSmall) },
        text = {
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
                        NotificationTargetRole.entries.forEach { role ->
                            CmsChip(role.name, selected = targetRole == role, onClick = { targetRole = role })
                        }
                    }
                    if (targetRole == NotificationTargetRole.STUDENT && sessions.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        CmsEntityPicker(
                            label = "Academic session",
                            selectedId = sessionId,
                            options = sessions.map { CmsEntityOption(it.sessionId, "${it.startYear}-${it.endYear} ${it.shift}") },
                            onSelected = { sessionId = it },
                            optional = true,
                            emptyLabel = "All students",
                        )
                    }
                    if (targetRole != NotificationTargetRole.ADMIN && departments.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        CmsEntityPicker(
                            label = "Department scope",
                            selectedId = deptId,
                            options = departments.map { CmsEntityOption(it.deptId, it.name) },
                            onSelected = { deptId = it },
                            optional = true,
                            emptyLabel = "College wide",
                        )
                    }
                } else {
                    Spacer(Modifier.height(10.dp))
                    CmsEntityPicker(
                        label = "Your class session",
                        selectedId = sessionId,
                        options = sessions.map { CmsEntityOption(it.sessionId, "${it.startYear}-${it.endYear} ${it.shift}") },
                        onSelected = { sessionId = it },
                    )
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
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSend(
                        NotificationDraft(
                            title = title,
                            body = body,
                            targetRole = if (teacherAudience) NotificationTargetRole.STUDENT else targetRole,
                            priority = priority,
                            departmentId = deptId,
                            sessionId = sessionId,
                        ),
                    )
                },
                enabled = titleValid && bodyValid && !busy && (!teacherAudience || sessionId != null),
            ) { Text(if (busy) "Sending..." else "Send notification") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}
