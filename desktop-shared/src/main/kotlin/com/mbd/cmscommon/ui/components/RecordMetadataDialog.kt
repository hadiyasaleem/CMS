package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.BaseEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RecordMetadataInfoButton(title: String, entity: BaseEntity) {
    var open by remember(entity.createdAt) { mutableStateOf(false) }

    IconButton(onClick = { open = true }) {
        Icon(Icons.Outlined.Info, contentDescription = "$title information")
    }
    if (open) {
        RecordMetadataDialog(title, entity, onDismiss = { open = false })
    }
}

@Composable
fun RecordMetadataDialog(title: String, entity: BaseEntity, onDismiss: () -> Unit) {
    RecordMetadataDialog(title, entity.createdAt, entity.createdBy, entity.updatedAt, entity.updatedBy, onDismiss)
}

@Composable
fun RecordMetadataDialog(
    title: String,
    createdAt: Instant?,
    createdBy: String?,
    updatedAt: Instant?,
    updatedBy: String?,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text("$title information") },
        text = {
            Column(Modifier.fillMaxWidth()) {
                MetadataValue("Created at", formatMetadataTime(createdAt))
                MetadataValue("Created by", createdBy ?: "Unavailable")
                Spacer(Modifier.height(8.dp))
                MetadataValue("Updated at", formatMetadataTime(updatedAt))
                MetadataValue("Updated by", updatedBy ?: "Unavailable")
            }
        },
    )
}

@Composable
private fun MetadataValue(label: String, value: String) {
    Text(label.uppercase(), fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
    Text(value, modifier = Modifier.padding(bottom = 8.dp), style = MaterialTheme.typography.bodyMedium)
}

private fun formatMetadataTime(instant: Instant?): String {
    if (instant == null || instant == Instant.EPOCH) return "Unavailable"
    return instant.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.ROOT))
}
