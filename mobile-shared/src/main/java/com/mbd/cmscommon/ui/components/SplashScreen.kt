package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun BrandedSplashScreen(
    background: Painter,
    logo: Painter,
    portalLabel: String,
    statusText: String = "VERIFYING SECURE SESSION",
    modifier: Modifier = Modifier,
) {
    val entrance = remember { Animatable(0f) }
    val transition = rememberInfiniteTransition(label = "branded-splash")
    val pulse by transition.animateFloat(
        initialValue = 0.52f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "status-pulse",
    )

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, tween(900, easing = FastOutSlowInEasing))
    }

    BoxWithConstraints(modifier.fillMaxSize().background(CmsTheme.colors.ink)) {
        val compact = maxWidth < 600.dp
        val logoSize = if (compact) 224.dp else 268.dp

        Image(
            painter = background,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().graphicsLayer {
                val scale = 1.06f - entrance.value * 0.06f
                scaleX = scale
                scaleY = scale
            },
            contentScale = ContentScale.Crop,
        )
        androidx.compose.foundation.layout.Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    0f to CmsTheme.colors.ink.copy(alpha = 0.82f),
                    0.44f to CmsTheme.colors.ink.copy(alpha = 0.36f),
                    1f to CmsTheme.colors.ink.copy(alpha = 0.94f),
                ),
            ),
        )

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(horizontal = if (compact) 28.dp else 48.dp, vertical = if (compact) 40.dp else 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(if (compact) 0.62f else 0.48f))
            Image(
                painter = logo,
                contentDescription = "$portalLabel application logo",
                modifier = Modifier.size(logoSize).graphicsLayer { alpha = entrance.value },
                contentScale = ContentScale.Fit,
            )
            Text(
                "GGC-MBD",
                modifier = Modifier.graphicsLayer { alpha = entrance.value },
                color = Color.White,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.displaySmall,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                portalLabel.uppercase(Locale.ROOT),
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                style = CmsTextStyles.eyebrow,
            )
            Spacer(Modifier.weight(if (compact) 0.78f else 0.62f))
            LinearProgressIndicator(
                modifier = Modifier.width(if (compact) 180.dp else 220.dp).height(3.dp),
                color = CmsTheme.colors.accent,
                trackColor = Color.White.copy(alpha = 0.16f),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                statusText.uppercase(Locale.ROOT),
                color = Color.White.copy(alpha = pulse),
                textAlign = TextAlign.Center,
                style = CmsTextStyles.eyebrow,
            )
        }
    }
}
