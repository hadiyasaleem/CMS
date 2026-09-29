package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.LocalScrollbarStyle
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.ScrollbarStyle
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
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
 *
 * The scrollbar uses `matchParentSize()`, not `fillMaxHeight()`: the latter asks to fill whatever
 * max height the *incoming* constraints allow, which -- when [modifier] carries no size of its own
 * (e.g. inside an `AlertDialog`'s unbounded text slot) -- can be the whole window, stretching this
 * Box (and the dialog around it) far past [content]'s own bounded height. `matchParentSize()` only
 * ever matches the size this Box actually resolves to from [content], so a height-capped [content]
 * keeps this wrapper capped too instead of ballooning to fill the dialog.
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
            modifier = Modifier.align(Alignment.CenterEnd).matchParentSize(),
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
            modifier = Modifier.align(Alignment.CenterEnd).matchParentSize(),
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
