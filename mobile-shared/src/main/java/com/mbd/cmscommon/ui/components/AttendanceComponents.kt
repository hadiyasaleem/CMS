package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModWarn

@Composable
fun AttendanceStatusChip(status: AttendanceStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        AttendanceStatus.PRESENT -> "P" to ModSuccess
        AttendanceStatus.ABSENT -> "A" to ModAccent
        AttendanceStatus.LEAVE -> "L" to ModWarn
    }
    Text(
        label,
        modifier = modifier.background(color).padding(horizontal = 10.dp, vertical = 4.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelLarge,
    )
}

@Composable
fun AttendancePercentageBadge(percentage: Float, modifier: Modifier = Modifier) {
    val color = when {
        percentage >= 75f -> ModSuccess
        percentage >= 60f -> ModWarn
        else -> ModAccent
    }
    Text(
        "%.1f%%".format(percentage),
        modifier = modifier.background(color).padding(horizontal = 10.dp, vertical = 4.dp),
        color = Color.White,
        style = MaterialTheme.typography.labelLarge,
    )
}
