package com.mbd.cmscommon.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.Outcome

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(34.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(16.dp))
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyLarge)
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

@Deprecated("Use CmsErrorDialog for modal errors, or CmsNotice(message, tone = NoticeTone.Error) inline.")
@Composable
fun ErrorBanner(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.CloudOff, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(12.dp))
        Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        if (onRetry != null) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onRetry) { Text("Retry") }
        }
    }
}

@Deprecated("Use CmsNotice(message, tone = NoticeTone.Error, actionLabel, onAction) instead.")
@Composable
fun InlineErrorCard(message: String, actionLabel: String? = null, onAction: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    LeftStripeCard(MaterialTheme.colorScheme.error, modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.width(10.dp))
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
fun SkeletonRow(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "alpha",
    )
    Column(modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)) {
        Box(
            Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh).alpha(alpha),
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier.fillMaxWidth(0.9f).height(12.dp).clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh).alpha(alpha),
        )
    }
}

@Composable
fun SkeletonList(rows: Int = 7, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        repeat(rows) { SkeletonRow() }
    }
}

/**
 * How a [CmsNotice] or [CmsErrorDialog] reads: which color and icon it borrows. Not a severity —
 * see [com.mbd.cmscommon.util.Severity] for whether a failure gets logged; this only affects
 * presentation, and callers pick it for messages that aren't errors at all (e.g. [Success]).
 */
enum class NoticeTone { Info, Success, Warning, Error }

@Composable
private fun NoticeTone.color(): Color = when (this) {
    NoticeTone.Info -> MaterialTheme.colorScheme.primary
    NoticeTone.Success -> CmsTheme.colors.success
    NoticeTone.Warning -> CmsTheme.colors.warn
    NoticeTone.Error -> MaterialTheme.colorScheme.error
}

private fun NoticeTone.icon(): ImageVector = when (this) {
    NoticeTone.Info -> Icons.Outlined.Info
    NoticeTone.Success -> Icons.Outlined.CheckCircle
    NoticeTone.Warning -> Icons.Outlined.WarningAmber
    NoticeTone.Error -> Icons.Outlined.ErrorOutline
}

/**
 * The single inline status card meant to replace the app's ~17 bespoke private `*Notice`
 * composables (`CalendarNotice`, `DatesheetNotice`, `ProfileNotice`, `SubmitNotice`, ...) and
 * [InlineErrorCard]. Covers everything those needed: a tone-colored icon or a progress spinner in
 * its place, an optional trailing action button, and an optional dismiss (×) button.
 */
@Composable
fun CmsNotice(
    message: String,
    modifier: Modifier = Modifier,
    tone: NoticeTone = NoticeTone.Error,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    showProgress: Boolean = false,
) {
    val color = tone.color()
    LeftStripeCard(color, modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showProgress) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = color, strokeWidth = 2.dp)
            } else {
                Icon(tone.icon(), contentDescription = null, tint = color)
            }
            Spacer(Modifier.width(10.dp))
            Text(message, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            if (actionLabel != null && onAction != null) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Outlined.Close, contentDescription = "Dismiss", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

/** Renders nothing for [Outcome.Loading]/[Outcome.Success]; an error [CmsNotice] for [Outcome.Error]. */
@Composable
fun CmsNotice(
    outcome: Outcome<*>,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
) {
    if (outcome is Outcome.Error) {
        CmsNotice(
            message = outcome.message,
            modifier = modifier,
            tone = NoticeTone.Error,
            actionLabel = if (onRetry != null) "Retry" else null,
            onAction = onRetry,
            onDismiss = onDismiss,
        )
    }
}

/**
 * The single modal error dialog meant to replace this app's several ad-hoc `AlertDialog`s (all
 * previously titled "Something went wrong", one with a hard-coded custom color).
 */
@Composable
fun CmsErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        icon = { Icon(Icons.Outlined.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onRetry ?: onDismiss) { Text(if (onRetry != null) "Retry" else "OK") }
        },
        dismissButton = if (onRetry != null) {
            { TextButton(onClick = onDismiss) { Text("Dismiss") } }
        } else {
            null
        },
    )
}
