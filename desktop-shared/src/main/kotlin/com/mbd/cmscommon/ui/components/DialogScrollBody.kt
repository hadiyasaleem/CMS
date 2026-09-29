package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared default cap for [DialogScrollBody] -- tall enough for most forms, short of most screens. */
val DialogScrollBodyDefaultMaxHeight = 480.dp

/**
 * The body of an `AlertDialog`'s `text` slot that scrolls when its content is taller than the dialog.
 * Caps its own height at [maxHeight] so the dialog doesn't stretch to fill the screen when content is
 * short, and scrolls internally instead of getting clipped or pushing the action buttons off-screen
 * when content is tall -- `AlertDialog`'s own text-slot clamp isn't reliable enough on its own.
 */
@Composable
fun DialogScrollBody(
    modifier: Modifier = Modifier,
    maxHeight: Dp = DialogScrollBodyDefaultMaxHeight,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    WithVerticalScrollbar(scrollState, modifier) {
        Column(
            Modifier.fillMaxWidth().heightIn(max = maxHeight).verticalScroll(scrollState),
            verticalArrangement = verticalArrangement,
            content = content,
        )
    }
}
