package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.ArrowRight
import compose.icons.tablericons.Calendar
import compose.icons.tablericons.ChevronDown
import compose.icons.tablericons.ChevronUp
import compose.icons.tablericons.Clock
import compose.icons.tablericons.Dashboard
import compose.icons.tablericons.School
import compose.icons.tablericons.Speakerphone
import compose.icons.tablericons.UserCheck
import compose.icons.tablericons.Users
import com.mbd.cmscommon.controller.ScopeFilterOptions
import com.mbd.cmscommon.domain.model.ShiftScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.DashboardState
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSurfaceAlt
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.ui.theme.ModRedTint
import java.util.Locale
import kotlin.math.roundToInt

data class DashboardMetric(
    val label: String,
    val value: String,
    val detail: String,
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
)

data class DashboardActionUi(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

@Composable
fun AdminDashboardContent(
    state: DashboardState,
    heroPainter: Painter,
    actions: List<DashboardActionUi>,
    onOpenMasterTimetable: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenNotifications: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    /** The Department -> Session -> Shift filter for the counters; hidden when [filterOptions] is null. */
    filterScope: ShiftScope = ShiftScope.ALL,
    filterOptions: ScopeFilterOptions? = null,
    onFilterScope: (ShiftScope) -> Unit = {},
) {
    val studentsPerTeacher = if (state.teachers > 0) (state.students.toDouble() / state.teachers).roundToInt() else 0
    val studentsPerSession = if (state.activeSessions > 0) (state.students.toDouble() / state.activeSessions).roundToInt() else 0
    val sessionsPerDepartment = if (state.departments > 0) (state.activeSessions.toDouble() / state.departments).roundToInt() else 0

    val metrics = listOf(
        DashboardMetric(
            "Students",
            state.students.toString(),
            if (state.activeSessions > 0) "$studentsPerSession per active session" else "No active sessions",
            TablerIcons.School,
            ModInk,
            ModInk.copy(alpha = 0.08f),
        ),
        DashboardMetric(
            "Teachers",
            state.teachers.toString(),
            if (state.teachers > 0) "$studentsPerTeacher students per teacher" else "Faculty directory is empty",
            TablerIcons.Users,
            ModSuccess,
            ModSuccess.copy(alpha = 0.12f),
        ),
        DashboardMetric(
            "Departments",
            state.departments.toString(),
            if (state.departments > 0) "$sessionsPerDepartment sessions per department" else "Create the first department",
            TablerIcons.Dashboard,
            ModWarn,
            ModWarn.copy(alpha = 0.14f),
        ),
        DashboardMetric(
            "Active sessions",
            state.activeSessions.toString(),
            if (state.activeSessions > 0) "$studentsPerSession students per session" else "No active intakes",
            TablerIcons.Clock,
            ModInk,
            ModInk.copy(alpha = 0.08f),
        ),
        DashboardMetric(
            "Link requests",
            state.pendingRequests.toString(),
            if (state.pendingRequests > 0) "Waiting for review" else "Queue is clear",
            TablerIcons.UserCheck,
            if (state.pendingRequests > 0) CmsTheme.colors.accent else ModSuccess,
            if (state.pendingRequests > 0) ModRedTint else ModSuccess.copy(alpha = 0.12f),
        ),
    )

    var snapshotExpanded by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier.fillMaxSize().background(ModGround)) {
        val wide = maxWidth >= 900.dp
        val contentPadding = if (wide) 32.dp else 16.dp
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .widthIn(max = 1180.dp)
                .verticalScroll(scrollState)
                .padding(horizontal = contentPadding, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(if (wide) 24.dp else 18.dp),
        ) {
            if (!errorMessage.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ModRedTint,
                    border = BorderStroke(1.dp, CmsTheme.colors.accent.copy(alpha = 0.22f)),
                ) {
                    Text(errorMessage, modifier = Modifier.padding(16.dp), color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodyMedium)
                }
            }

            FoldableDashboardSectionHeading(
                "College snapshot",
                expanded = snapshotExpanded,
                onToggle = { snapshotExpanded = !snapshotExpanded },
            )
            if (snapshotExpanded) {
                if (filterOptions != null) {
                    ShiftScopeSelector(filterScope, filterOptions.departments, filterOptions.sessions, onFilterScope)
                }
                DashboardGrid(metrics, if (wide) 5 else 2) { metric, itemModifier -> DashboardMetricCard(metric, itemModifier) }
            }

            DashboardSectionHeading("Needs attention")
            if (wide) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TimetableCard(onOpenMasterTimetable, Modifier.weight(1.35f))
                    ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.weight(1f))
                    BroadcastCard(onOpenNotifications, Modifier.weight(1f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TimetableCard(onOpenMasterTimetable, Modifier.fillMaxWidth())
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ReviewQueueCard(state.pendingRequests, onOpenLinkRequests, Modifier.weight(1f))
                        BroadcastCard(onOpenNotifications, Modifier.weight(1f))
                    }
                }
            }

            DashboardSectionHeading("Quick access")
            DashboardGrid(actions, if (wide) 3 else 2) { action, itemModifier -> DashboardActionCard(action, itemModifier) }

            Spacer(Modifier.height(72.dp))
        }

        VerticalScrollbar(
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
            adapter = rememberScrollbarAdapter(scrollState),
        )
    }
}

@Composable
private fun DashboardSectionHeading(title: String) {
    Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
}

@Composable
private fun FoldableDashboardSectionHeading(title: String, expanded: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DashboardSectionHeading(title)
        Icon(
            imageVector = if (expanded) TablerIcons.ChevronUp else TablerIcons.ChevronDown,
            contentDescription = if (expanded) "Collapse" else "Expand",
            tint = ModMuted,
        )
    }
}

@Composable
private fun <T> DashboardGrid(items: List<T>, columns: Int, itemContent: @Composable (T, Modifier) -> Unit) {
    val rows = items.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        rows.forEach { rowItems ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                rowItems.forEach { item -> itemContent(item, Modifier.weight(1f)) }
                repeat(columns - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DashboardMetricCard(metric: DashboardMetric, modifier: Modifier = Modifier) {
    CmsCard(modifier) {
        Column(Modifier.padding(18.dp)) {
            Box(Modifier.size(40.dp).background(metric.container, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(metric.icon, contentDescription = null, tint = metric.tint, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(metric.value, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(metric.label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
            Text(
                metric.detail,
                color = metric.tint,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun TimetableCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Master timetable", null, "Open schedule",
        TablerIcons.Calendar, ModInk, ModInk.copy(alpha = 0.08f), onClick, modifier,
    )
}

@Composable
private fun ReviewQueueCard(pendingRequests: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val title = if (pendingRequests > 0) "$pendingRequests requests" else "Queue clear"
    val tint = if (pendingRequests > 0) CmsTheme.colors.accent else ModSuccess
    val container = if (pendingRequests > 0) ModRedTint else ModSuccess.copy(alpha = 0.12f)
    DashboardOperationCard(title, null, "Review queue", TablerIcons.UserCheck, tint, container, onClick, modifier)
}

@Composable
private fun BroadcastCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    DashboardOperationCard(
        "Broadcast", null, "New notice",
        TablerIcons.Speakerphone, ModWarn, ModWarn.copy(alpha = 0.14f), onClick, modifier,
    )
}

@Composable
private fun DashboardOperationCard(
    title: String,
    body: String?,
    label: String,
    icon: ImageVector,
    tint: Color,
    container: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = container,
        border = BorderStroke(1.dp, tint.copy(alpha = 0.16f)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            if (body != null) {
                Text(body, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(label.uppercase(Locale.ROOT), color = tint, style = CmsTextStyles.eyebrow)
                Icon(TablerIcons.ArrowRight, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun DashboardActionCard(action: DashboardActionUi, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = action.onClick),
        shape = RoundedCornerShape(18.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(Modifier.size(42.dp).background(ModSurfaceAlt, RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) {
                Icon(action.icon, contentDescription = null, tint = ModInk, modifier = Modifier.size(21.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(action.label, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
