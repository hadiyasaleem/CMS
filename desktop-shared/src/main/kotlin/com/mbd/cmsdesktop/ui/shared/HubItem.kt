package com.mbd.cmsdesktop.ui.shared

import androidx.compose.ui.graphics.vector.ImageVector
import com.mbd.cmscommon.ui.components.BadgeTone

/**
 * A single tile in [HubScreen] - the generic hub-grid pattern reused across the desktop apps'
 * People/Records/More hubs (mirrors mobile's per-role hub screens, but factored into one shared
 * composable here instead of being duplicated per role/app).
 */
data class HubItem(
    val label: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: Pair<String, BadgeTone>? = null,
    val onClick: () -> Unit,
)
