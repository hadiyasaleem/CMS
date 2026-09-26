package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.ui.theme.CmsTheme

/** "Export" text button that offers Excel or PDF. */
@Composable
fun ExportMenuButton(
    onExport: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = CmsTheme.colors.accent,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        TextButton(onClick = { open = true }, enabled = enabled) {
            Icon(Icons.Outlined.FileDownload, contentDescription = null, tint = tint)
            Text(" Export", color = tint)
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ExportFormat.entries.forEach { format ->
                DropdownMenuItem(text = { Text("Export as ${format.label}") }, onClick = { open = false; onExport(format) })
            }
        }
    }
}
