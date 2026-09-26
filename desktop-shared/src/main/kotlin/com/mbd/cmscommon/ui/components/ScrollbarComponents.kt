package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.ui.theme.CmsTheme

/**
 * Wraps [content] with a themed, always-mounted vertical scrollbar bound to [state]. Use around a
 * screen's single top-level `LazyColumn`; put layout modifiers (fillMaxSize/weight/etc.) that used
 * to live on that LazyColumn onto [modifier] here instead.
 */
@Composable
fun WithVerticalScrollbar(
    state: LazyListState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        content()
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state),
            style = cmsScrollbarStyle(),
        )
    }
}

/** Same as the [LazyListState] overload, for a `Column(Modifier.verticalScroll(state))` container. */
@Composable
fun WithVerticalScrollbar(
    state: ScrollState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(modifier) {
        content()
        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(state),
            style = cmsScrollbarStyle(),
        )
    }
}

@Composable
private fun cmsScrollbarStyle(): ScrollbarStyle {
    val colors = CmsTheme.colors
    return LocalScrollbarStyle.current.copy(
        unhoverColor = colors.muted.copy(alpha = 0.35f),
        hoverColor = colors.accent.copy(alpha = 0.7f),
    )
}
