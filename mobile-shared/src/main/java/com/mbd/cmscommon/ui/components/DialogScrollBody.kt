package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The body of an `AlertDialog`'s `text` slot that scrolls when its content is taller than the dialog. The slot is
 * already limited to the screen height, so a tall form scrolls here instead of losing its last fields and buttons.
 */
@Composable
fun DialogScrollBody(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().verticalScroll(rememberScrollState()), content = content)
}
