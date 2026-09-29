package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Shared default cap for [CmsDropdownMenu] -- enough rows to show a page of options before scrolling. */
val CmsDropdownMenuDefaultMaxHeight = 240.dp

/**
 * Thin wrapper over Material3's [DropdownMenu] that caps its own height at [maxHeight] so a long
 * option list scrolls inside the menu instead of overflowing past the bottom of the screen.
 */
@Composable
fun CmsDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    maxHeight: Dp = CmsDropdownMenuDefaultMaxHeight,
    content: @Composable ColumnScope.() -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier.heightIn(max = maxHeight),
        content = content,
    )
}
