package com.mbd.cmsdesktop.ui.shared

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.components.SectionHeader
import com.mbd.cmscommon.ui.components.StatusBadge
import com.mbd.cmscommon.ui.theme.CmsTheme

private val HubCanvas = Color(0xFFF7F5F0)
private val HubBorder = Color(0xFFE5E0D7)
private val HubMuted = Color(0xFF77716A)
private val HubTileColors = listOf(
    Color(0xFF2F4B7A), Color(0xFF2F6B4F), Color(0xFF9A651B), Color(0xFFB43A31),
)

/**
 * Generic hub-grid screen: an eyebrow/title header (optionally with a signed-in-user avatar) over
 * a list of [HubItem] tiles. Reused by every role's People/Records/More hub on desktop instead of
 * hand-rolling the same card-list layout per hub (see `PeopleHubWorkspace`/`RecordsHubWorkspace`/
 * `MoreHubWorkspace` for the older, per-hub-duplicated version of this pattern).
 */
@Composable
fun HubScreen(
    eyebrow: String,
    title: String,
    items: List<HubItem>,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    avatarName: String? = null,
    onAvatarClick: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(HubCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showTopBar) {
            item {
                SectionHeader(
                    title = title,
                    eyebrow = eyebrow,
                    trailing = if (avatarName != null) {
                        { HubAvatar(avatarName, onAvatarClick) }
                    } else {
                        null
                    },
                )
            }
        }
        items(items, key = { it.label }) { item -> HubTile(item, HubTileColors[items.indexOf(item) % HubTileColors.size]) }
        item { Spacer(Modifier.size(72.dp)) }
    }
}

@Composable
private fun HubAvatar(name: String, onClick: (() -> Unit)?) {
    Surface(
        modifier = Modifier.size(40.dp).let { if (onClick != null) it.clickable(onClick = onClick) else it },
        shape = CircleShape,
        color = CmsTheme.colors.ink,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                name.trim().take(1).uppercase().ifBlank { "?" },
                color = CmsTheme.colors.onInk,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun HubTile(item: HubItem, tone: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = item.onClick),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, tone.copy(alpha = 0.25f)),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(tone.copy(alpha = 0.12f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(item.icon, contentDescription = null, tint = tone)
            }
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(item.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(item.subtitle, color = HubMuted, style = MaterialTheme.typography.bodySmall)
            }
            item.badge?.let { (text, tone2) ->
                Spacer(Modifier.size(8.dp))
                StatusBadge(text, tone2)
            }
        }
    }
}
