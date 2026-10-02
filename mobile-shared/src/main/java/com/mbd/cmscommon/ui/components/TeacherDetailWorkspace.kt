package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.TeacherGrid
import com.mbd.cmscommon.controller.TeacherWorkloadStats
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.teacherDetailExport
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn

private val DetailCanvas = ModGround
private val DetailGold = ModWarn

/** The admin-facing "one teacher" screen: profile, computed workload stats, their classes, and their
 * own weekly schedule grids (the exact same read-only grid the teacher app shows them) -- plus export
 * buttons for the whole screen, just the stats, and each grid on its own. */
@Composable
fun TeacherDetailWorkspace(
    teacher: Teacher?,
    department: Department?,
    stats: TeacherWorkloadStats,
    assignments: List<ResolvedAssignment>,
    sessions: List<AcademicSession>,
    grids: List<TeacherGrid>,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    var detailPeriod by remember { mutableStateOf<SessionPeriod?>(null) }
    var dismissedError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(DetailCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            TeacherDetailHeader(
                teacher = teacher,
                department = department,
                exportEnabled = teacher != null,
                onExportAll = { format ->
                    teacher?.let { onExport(teacherDetailExport(it, department, stats, assignments, grids.map { g -> g.grid }, grids.associate { g -> g.grid.title to g.breakSlot }), format) }
                },
            )
        }
        if (loading) {
            item { SkeletonRow() }
        }
        if (!loading && grids.isEmpty()) {
            item { TeacherDetailEmptyCard() }
        } else {
            grids.forEach { teacherGrid ->
                item { TeacherGridSection(teacherGrid, onCellClick = { period -> detailPeriod = period }, onExport = onExport) }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }

    detailPeriod?.let { period ->
        TeacherPeriodDetailDialog(period, sessions.firstOrNull { it.sessionId == period.sessionId }, onDismiss = { detailPeriod = null })
    }

    if (!errorMessage.isNullOrBlank() && errorMessage != dismissedError) {
        CmsErrorDialog(
            message = errorMessage,
            title = "Couldn't load this teacher",
            onDismiss = { dismissedError = errorMessage },
            onRetry = { dismissedError = null; onRetry() },
        )
    }
}

@Composable
private fun TeacherDetailHeader(
    teacher: Teacher?,
    department: Department?,
    exportEnabled: Boolean,
    onExportAll: (ExportFormat) -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text("TEACHER PROFILE", color = DetailGold, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text(teacher?.name ?: "Loading…", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        listOfNotNull(department?.name, teacher?.designation?.takeIf { it.isNotBlank() }).joinToString(" · ").ifBlank { "Department not assigned" },
                        color = CmsTheme.colors.onInkMuted,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    ExportMenuButton(onExport = onExportAll, enabled = exportEnabled, tint = CmsTheme.colors.onInk)
                    Text("Export all", color = CmsTheme.colors.onInkMuted, style = CmsTextStyles.eyebrow)
                }
            }
            if (teacher != null) {
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(teacher.status.name, if (teacher.status == TeacherStatus.ACTIVE) BadgeTone.Success else BadgeTone.Error)
                    Text(listOfNotNull(teacher.email, teacher.phone?.takeIf { it.isNotBlank() }).joinToString(" · "), color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun TeacherDetailEmptyCard() {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp)) {
            Text("No timetable periods yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Once this teacher is assigned periods, their weekly grids will appear here.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
