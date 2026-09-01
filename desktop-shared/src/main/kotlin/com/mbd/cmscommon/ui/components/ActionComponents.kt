package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModSurface
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceStatus

@Composable
fun ActionTile(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmsCard(modifier.clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(18.dp))
            Text(
                label.uppercase(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun palLetter(s: AttendanceStatus): String = when (s) {
    AttendanceStatus.PRESENT -> "P"
    AttendanceStatus.ABSENT -> "A"
    AttendanceStatus.LEAVE -> "L"
}

@Composable
fun PalSegment(
    selected: AttendanceStatus,
    onSelect: (AttendanceStatus) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier.border(1.dp, CmsTheme.colors.rule)) {
        val entries = AttendanceStatus.entries
        entries.forEachIndexed { index, status ->
            val isSelected = selected == status
            val bg = if (isSelected) {
                when (status) {
                    AttendanceStatus.PRESENT -> CmsTheme.colors.ink
                    AttendanceStatus.ABSENT -> CmsTheme.colors.accent
                    AttendanceStatus.LEAVE -> CmsTheme.colors.warn
                }
            } else {
                MaterialTheme.colorScheme.surfaceContainerLowest
            }
            val fg = if (isSelected) ModSurface else CmsTheme.colors.muted

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .background(bg)
                    .clickable(enabled = enabled) { onSelect(status) },
                contentAlignment = Alignment.Center,
            ) {
                Text(palLetter(status), color = fg, style = MaterialTheme.typography.labelLarge)
            }
            if (index < entries.lastIndex) {
                VerticalDivider(modifier = Modifier.height(40.dp), color = CmsTheme.colors.rule)
            }
        }
    }
}

@Composable
fun <T> SegmentToggle(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.border(1.dp, CmsTheme.colors.rule)) {
        options.forEachIndexed { index, option ->
            val isSelected = option == selected
            val bg = if (isSelected) CmsTheme.colors.ink else MaterialTheme.colorScheme.surfaceContainerLowest
            val fg = if (isSelected) CmsTheme.colors.onInk else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(bg)
                    .clickable { onSelect(option) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(label(option).uppercase(), color = fg, style = MaterialTheme.typography.labelLarge)
            }
            if (index < options.lastIndex) {
                VerticalDivider(modifier = Modifier.fillMaxHeight(), color = CmsTheme.colors.rule)
            }
        }
    }
}
