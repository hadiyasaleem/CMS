package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mbd.cmscommon.ui.theme.CmsTheme

/**
 * Shown for the duration of [com.mbd.cmscommon.ui.state.GlobalRefreshViewModel.refresh] --
 * deliberately not dismissible (no back press, no tap-outside) since a refresh left running in the
 * background would let the user navigate away mid-sync and see partially-updated data.
 */
@Composable
fun SyncProgressDialog(completed: Int, total: Int) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = CmsTheme.colors.ink,
            contentColor = CmsTheme.colors.onInk,
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text("Syncing", style = MaterialTheme.typography.titleMedium, color = CmsTheme.colors.onInk)
                Text(
                    "Please wait while your data is updated.",
                    style = MaterialTheme.typography.bodySmall,
                    color = CmsTheme.colors.onInkMuted,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
                )
                val fraction = if (total > 0) (completed.toFloat() / total).coerceIn(0f, 1f) else 0f
                LinearProgressIndicator(
                    progress = { fraction },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = CmsTheme.colors.accent,
                    trackColor = CmsTheme.colors.track,
                )
                Text(
                    "$completed of $total complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = CmsTheme.colors.onInkMuted,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
