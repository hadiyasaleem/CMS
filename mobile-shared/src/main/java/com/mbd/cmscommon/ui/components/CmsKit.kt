package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.School
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

@Composable
fun AvatarCircle(name: String, modifier: Modifier = Modifier, size: Int = 36) {
    val initials = name.trim().split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase(Locale.ROOT) }
        .ifEmpty { "?" }

    Box(
        modifier = modifier.size(size.dp).background(CmsTheme.colors.accent),
        contentAlignment = Alignment.Center,
    ) {
        Text(initials, color = CmsTheme.colors.onInk, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CmsTopBar(
    modifier: Modifier = Modifier,
    title: String = "GGC-MBD",
    avatarName: String? = null,
    onBack: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    isRefreshing: Boolean = false,
    onNotifications: (() -> Unit)? = null,
    notificationCount: Int = 0,
    goldWordmark: Boolean = false,
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(CmsTheme.colors.ink)
            .windowInsetsPadding(WindowInsets.statusBars),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            when {
                onBack != null -> {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(22.dp), tint = CmsTheme.colors.onInk)
                    }
                    Spacer(Modifier.width(4.dp))
                }
                avatarName != null -> {
                    AvatarCircle(avatarName, size = 28)
                    Spacer(Modifier.width(10.dp))
                }
                else -> {
                    Box(Modifier.size(28.dp).background(CmsTheme.colors.accent), contentAlignment = Alignment.Center) {
                        Icon(Icons.Outlined.School, contentDescription = null, modifier = Modifier.size(16.dp), tint = CmsTheme.colors.onInk)
                    }
                    Spacer(Modifier.width(10.dp))
                }
            }

            Text(
                title,
                modifier = Modifier.weight(1f),
                color = if (goldWordmark) CmsTheme.colors.gold else CmsTheme.colors.onInk,
                style = MaterialTheme.typography.titleMedium,
            )

            if (onRefresh != null) {
                IconButton(onClick = onRefresh, modifier = Modifier.size(40.dp), enabled = !isRefreshing) {
                    if (isRefreshing) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CmsTheme.colors.onInk, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", modifier = Modifier.size(22.dp), tint = CmsTheme.colors.onInk)
                    }
                }
            }

            if (onNotifications != null) {
                IconButton(onClick = onNotifications, modifier = Modifier.size(40.dp)) {
                    BadgedBox(badge = {
                        if (notificationCount > 0) {
                            Badge(containerColor = CmsTheme.colors.accent, contentColor = CmsTheme.colors.onInk) {
                                Text(if (notificationCount > 99) "99+" else notificationCount.toString())
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", tint = CmsTheme.colors.onInk)
                    }
                }
            }
        }
        HorizontalDivider(thickness = 2.dp, color = CmsTheme.colors.rule)
    }
}

@Composable
fun SectionDivider(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(12.dp))
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 2.dp, color = CmsTheme.colors.rule)
    }
}

@Composable
fun ActiveTermBanner(
    termName: String,
    subtitle: String,
    onCta: () -> Unit,
    modifier: Modifier = Modifier,
    ctaLabel: String = "Manage",
    icon: ImageVector = Icons.Outlined.CalendarMonth,
) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RectangleShape, color = CmsTheme.colors.ink) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = CmsTheme.colors.accent)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(termName, color = CmsTheme.colors.onInk, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(subtitle, color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onCta,
                shape = RectangleShape,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CmsTheme.colors.accent),
                border = BorderStroke(2.dp, CmsTheme.colors.accent),
            ) {
                Text(ctaLabel.uppercase(Locale.ROOT))
            }
        }
    }
}

@Composable
fun HubNavCard(label: String, subtitle: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    CmsCard(modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).background(CmsTheme.colors.track),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleLarge)
                Text(subtitle, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CmsTheme.colors.muted)
        }
    }
}

@Composable
fun QuickActionCard(label: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    CmsCard(modifier.clickable(onClick = onClick)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(18.dp))
            Text(label.uppercase(Locale.ROOT), color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun AlertItem(title: String, meta: String, icon: ImageVector, stripeColor: Color, iconTint: Color, modifier: Modifier = Modifier) {
    LeftStripeCard(stripeColor, modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconTint)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(2.dp))
                Text(meta.uppercase(Locale.ROOT), color = CmsTheme.colors.muted, style = CmsTextStyles.eyebrow)
            }
        }
    }
}

@Composable
fun CmsFab(onClick: () -> Unit, modifier: Modifier = Modifier, contentDescription: String = "Add") {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        containerColor = CmsTheme.colors.accent,
        contentColor = CmsTheme.colors.onInk,
    ) {
        Icon(Icons.Filled.Add, contentDescription = contentDescription)
    }
}

@Composable
fun CmsPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: ImageVector? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(containerColor = CmsTheme.colors.accent, contentColor = CmsTheme.colors.onInk),
        contentPadding = PaddingValues(horizontal = 16.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(text.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelLarge)
            if (trailingIcon != null) {
                Spacer(Modifier.weight(1f))
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CmsOutlinedButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, leadingIcon: ImageVector? = null) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(2.dp, CmsTheme.colors.rule),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun CmsDestructiveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CmsTheme.colors.accent),
        border = BorderStroke(2.dp, CmsTheme.colors.accent),
    ) {
        Text(text.uppercase(Locale.ROOT), style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
fun TableToolbar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search…",
    extraActions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CmsTextField(
            value = query,
            onValueChange = onQueryChange,
            label = "",
            placeholder = placeholder,
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null, tint = CmsTheme.colors.muted) },
        )
        if (extraActions != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                extraActions()
            }
        }
    }
}

@Composable
fun ToolbarChip(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
        border = BorderStroke(2.dp, CmsTheme.colors.rule),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}
