package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.BulkImportSummary
import com.mbd.cmscommon.util.ImportedStudentRow
import com.mbd.cmscommon.util.StudentImportResult

@Composable
fun StudentImportPreviewDialog(
    result: StudentImportResult,
    currentCount: Int,
    maxStudents: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val wouldOverflow = result.rows.size + currentCount > maxStudents

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = result.rows.isNotEmpty()) { Text("Import") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Import ${result.rows.size} student(s)?", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                if (result.rows.isEmpty()) {
                    Text("No valid rows were found in this file.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    LazyColumn(Modifier.height(220.dp)) {
                        items(result.rows, key = { it.rowNumber }) { row: ImportedStudentRow ->
                            Text(
                                "${row.rollNumber} — ${row.name}",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    if (wouldOverflow) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "This session allows up to $maxStudents students ($currentCount already enrolled) — " +
                                "some rows may be rejected once the cap is reached.",
                            color = CmsTheme.colors.accent,
                        )
                    }
                    if (result.errors.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        Text("Skipped (${result.errors.size}):", style = MaterialTheme.typography.labelMedium)
                        LazyColumn {
                            items(result.errors) { msg ->
                                Text(
                                    msg,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Composable
fun StudentImportResultDialog(summary: BulkImportSummary, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("OK") }
        },
        title = { Text("Import complete", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("${summary.succeeded} student(s) added.", style = MaterialTheme.typography.bodyMedium)
                if (summary.failures.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "${summary.failures.size} failed:",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    LazyColumn {
                        items(summary.failures) { msg ->
                            Text(
                                msg,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        },
    )
}
