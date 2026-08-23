package com.mbd.cmsadmin.feature.hub

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.components.CmsCard
import com.mbd.cmscommon.ui.components.SectionHeader
import com.mbd.cmscommon.ui.theme.CmsTheme

data class HubItem(val label: String, val subtitle: String, val icon: ImageVector, val onClick: () -> Unit)

/** Generic tab landing: brand top bar + SectionHeader hero + a list of navigation cards. */
@Composable
fun HubScreen(
    eyebrow: String,
    title: String,
    items: List<HubItem>,
    modifier: Modifier = Modifier,
    onBell: (() -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 24.dp),
    ) {
        item { SectionHeader(eyebrow = eyebrow, title = title) }
        items(items, key = { it.label }) { hub ->
            CmsCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable(onClick = hub.onClick),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier.size(44.dp).background(CmsTheme.colors.track),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(hub.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(hub.label, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface)
                        Text(hub.subtitle, style = MaterialTheme.typography.bodyMedium, color = CmsTheme.colors.muted)
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = CmsTheme.colors.muted)
                }
            }
        }
    }
}
