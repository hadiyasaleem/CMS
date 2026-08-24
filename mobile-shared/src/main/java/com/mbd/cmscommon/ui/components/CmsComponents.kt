package com.mbd.cmscommon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.min

@Composable
fun Eyebrow(text: String, color: Color = CmsTheme.colors.eyebrow, modifier: Modifier = Modifier) {
    Text(text.uppercase(), modifier, color, style = CmsTextStyles.eyebrow)
}

@Composable
fun AccentRule(color: Color = CmsTheme.colors.accent, width: Int = 40, modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(top = 10.dp)
            .width(width.dp)
            .height(2.dp)
            .background(color),
    )
}

@Composable
fun SectionHeader(
    title: String,
    eyebrow: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Eyebrow(eyebrow)
                Spacer(Modifier.height(6.dp))
            }
            Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodyMedium)
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
fun CmsCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(modifier = modifier, shape = RectangleShape, color = MaterialTheme.colorScheme.surfaceContainerLowest) {
        Box(Modifier.border(1.dp, CmsTheme.colors.rule)) {
            content()
        }
    }
}

@Composable
fun LeftStripeCard(stripeColor: Color, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    CmsCard(modifier) {
        Row(Modifier.fillMaxWidth()) {
            Box(
                Modifier
                    .width(4.dp)
                    .fillMaxSize()
                    .background(stripeColor),
            )
            Box(Modifier.weight(1f)) {
                content()
            }
        }
    }
}

@Composable
fun AvatarInitials(name: String, modifier: Modifier = Modifier, size: Int = 44) {
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }

    Box(
        modifier = modifier.size(size.dp).background(CmsTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initials,
            color = CmsTheme.colors.onInk,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun StatusBadge(text: String, tone: BadgeTone = BadgeTone.Neutral, modifier: Modifier = Modifier) {
    val (bg, fg) = when (tone) {
        BadgeTone.Success -> CmsTheme.colors.success.copy(alpha = 0.14f) to CmsTheme.colors.success
        BadgeTone.Warning -> CmsTheme.colors.redTint to CmsTheme.colors.warn
        BadgeTone.Error -> CmsTheme.colors.redTint to CmsTheme.colors.accent
        BadgeTone.Gold -> CmsTheme.colors.redTint to CmsTheme.colors.accent
        BadgeTone.Navy -> CmsTheme.colors.ink to CmsTheme.colors.onInk
        BadgeTone.Neutral -> MaterialTheme.colorScheme.surfaceContainerHigh to CmsTheme.colors.muted
    }
    Surface(modifier = modifier, shape = RectangleShape, color = bg) {
        Text(
            text.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = fg,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    navy: Boolean = false,
    trend: String? = null,
    valueColor: Color? = null,
) {
    val container = if (navy) CmsTheme.colors.ink else MaterialTheme.colorScheme.surfaceContainerLowest
    val onContainer = if (navy) CmsTheme.colors.onInk else MaterialTheme.colorScheme.onSurface
    val labelColor = if (navy) CmsTheme.colors.onInkMuted else CmsTheme.colors.muted
    val accent = CmsTheme.colors.accent

    Surface(modifier = modifier, shape = RectangleShape, color = container) {
        val strokeMod = if (navy) Modifier else Modifier.border(1.dp, CmsTheme.colors.rule)
        Column(strokeMod.fillMaxWidth().padding(16.dp)) {
            Text(
                value,
                color = valueColor ?: onContainer,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(6.dp))
            Text(label.uppercase(), color = labelColor, style = CmsTextStyles.eyebrow)
            if (trend != null) {
                Spacer(Modifier.height(2.dp))
                Text(trend, color = accent, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun RingGauge(
    percent: Float,
    modifier: Modifier = Modifier,
    size: Int = 120,
    caption: String = "",
    arcColor: Color = CmsTheme.colors.success,
    trackColor: Color = CmsTheme.colors.track,
) {
    val animated by animateFloatAsState(percent.coerceIn(0f, 100f), tween(600), label = "ring")

    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = min(this.size.width, this.size.height) * 0.12f
            val inset = stroke / 2
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Butt),
            )
            drawArc(
                color = arcColor,
                startAngle = -90f,
                sweepAngle = (animated / 100f) * 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(stroke, cap = StrokeCap.Butt),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${percent.toInt()}%",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineMedium,
            )
            if (caption.isNotEmpty()) {
                Text(caption.uppercase(), color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            }
        }
    }
}

@Composable
fun OfflineBanner(message: String = "Offline — showing cached data", modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RectangleShape, color = CmsTheme.colors.redTint) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(CmsTheme.colors.accent))
            Spacer(Modifier.width(8.dp))
            Text(message, color = CmsTheme.colors.accent, style = MaterialTheme.typography.labelMedium)
        }
    }
}
