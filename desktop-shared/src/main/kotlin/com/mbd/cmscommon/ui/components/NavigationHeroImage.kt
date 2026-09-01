package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.Painter

@Composable
fun NavigationHeroImage(
    painter: Painter,
    contentDescription: String,
    eyebrow: String,
    title: String,
    subtitle: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: (() -> Unit)? = null,
    secondaryActionLabel: String? = null,
    onSecondaryAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val compact = maxWidth < 660.dp
        val heroHeight = if (compact) 232.dp else 260.dp

        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 1440.dp).height(heroHeight),
            shape = RoundedCornerShape(20.dp),
            color = ModGround,
            border = BorderStroke(1.dp, ModTrack),
        ) {
            androidx.compose.foundation.layout.Box(Modifier.fillMaxSize()) {
                Image(
                    painter = painter,
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().background(
                        Brush.horizontalGradient(
                            0f to Color.Black.copy(alpha = 0.84f),
                            0.58f to Color.Black.copy(alpha = 0.58f),
                            1f to Color.Black.copy(alpha = 0.12f),
                        ),
                    ),
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .widthIn(max = if (compact) 350.dp else 620.dp)
                        .padding(if (compact) 18.dp else 24.dp),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 6.dp else 8.dp),
                ) {
                    Text(eyebrow.uppercase(), color = ModWarn, style = CmsTextStyles.eyebrow)
                    Text(
                        title,
                        color = ModSurface,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = if (compact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
                    )
                    Text(
                        subtitle,
                        color = ModSurface.copy(alpha = 0.88f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (primaryActionLabel != null && onPrimaryAction != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HeroAction(primaryActionLabel, onPrimaryAction, primary = true)
                            if (secondaryActionLabel != null && onSecondaryAction != null) {
                                HeroAction(secondaryActionLabel, onSecondaryAction, primary = false)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeroAction(label: String, onClick: () -> Unit, primary: Boolean) {
    val bg = if (primary) ModSurface else Color.Black.copy(alpha = 0.28f)
    val fg = if (primary) ModInk else ModSurface
    val border = if (primary) null else BorderStroke(1.dp, ModSurface.copy(alpha = 0.58f))

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = bg,
        contentColor = fg,
        border = border,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(label, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, maxLines = 1, style = MaterialTheme.typography.labelLarge)
            Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}
