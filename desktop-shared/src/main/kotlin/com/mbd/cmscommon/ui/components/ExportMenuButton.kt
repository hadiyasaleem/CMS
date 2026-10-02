package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.Download
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.ui.theme.CmsTheme

/** Icon-only Export button that offers Excel or PDF. */
@Composable
fun ExportMenuButton(
    onExport: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tint: Color = CmsTheme.colors.accent,
) {
    var open by remember { mutableStateOf(false) }
    Box(modifier) {
        IconButton(onClick = { open = true }, enabled = enabled) {
            Icon(TablerIcons.Download, contentDescription = "Export", tint = tint)
        }
        CmsDropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            ExportFormat.entries.forEach { format ->
                DropdownMenuItem(text = { Text("Export as ${format.label}") }, onClick = { open = false; onExport(format) })
            }
        }
    }
}

/** Right-aligned Export row for the top of a report list; [build] runs only when a format is picked. */
@Composable
fun ExportBar(
    onExport: (ExportDocument, ExportFormat) -> Unit,
    build: () -> ExportDocument,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        ExportMenuButton(onExport = { format -> onExport(build(), format) }, enabled = enabled)
    }
}
