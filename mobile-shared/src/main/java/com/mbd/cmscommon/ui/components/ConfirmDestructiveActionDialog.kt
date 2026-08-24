package com.mbd.cmscommon.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun ConfirmDestructiveActionDialog(
    title: String,
    dependentSummary: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text("$dependentSummary\n\nThis cannot be undone.") },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Delete permanently") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
