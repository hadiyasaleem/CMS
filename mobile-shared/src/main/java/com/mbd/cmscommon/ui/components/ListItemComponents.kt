package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.Teacher
import java.time.Instant

@Composable
fun TeacherListItem(teacher: Teacher, onClick: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarInitials(teacher.name)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(teacher.name, style = MaterialTheme.typography.titleLarge)
            Text(teacher.email, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun NotificationListItem(notification: Notification, modifier: Modifier = Modifier) {
    val expired = notification.expiresAt?.isBefore(Instant.now()) == true

    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (notification.priority != NotificationPriority.NORMAL) {
                    StatusBadge(
                        notification.priority.name,
                        if (notification.priority == NotificationPriority.URGENT) BadgeTone.Error else BadgeTone.Gold,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    notification.title,
                    color = if (expired) CmsTheme.colors.muted else MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            Spacer(Modifier.padding(top = 2.dp))
            Text(
                notification.body,
                color = if (expired) CmsTheme.colors.muted else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (expired) {
                Text("Expired", color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            }
        }
    }
}
