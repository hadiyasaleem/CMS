package com.mbd.cmsdesktop.ui.parity

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type

/**
 * Desktop equivalent of Android's predictable-back gesture: Escape or Alt+Left triggers [onBack]
 * while [enabled], mirroring what a phone's system back button does for the same navigation state.
 */
fun Modifier.desktopBackHandler(enabled: Boolean, onBack: () -> Unit): Modifier = onPreviewKeyEvent { event ->
    val isBack = event.key == Key.Escape || (event.isAltPressed && event.key == Key.DirectionLeft)
    if (enabled && isBack && event.type == KeyEventType.KeyDown) {
        onBack()
        true
    } else {
        false
    }
}
