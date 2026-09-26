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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceCounts
import com.mbd.cmscommon.domain.model.MonthlyAttendance
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentTermAttendance
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SummaryMonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val SummaryDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH)

@Composable
fun StudentAttendanceSummaryWorkspace(
    courseCode: String,
    rollNumber: String,
    student: SessionStudent?,
    session: AcademicSession?,
    term: SemesterTerm?,
    summary: StudentTermAttendance?,
    loading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onExport: (ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ModGround),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { SummaryHero(student?.name ?: "Roll $rollNumber", courseCode, onBack, canExport = summary != null && !loading, onExport = onExport) }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry, onDismiss = onClearError) }
        }
        item { StudentInfoCard(rollNumber, student, session, term) }
        when {
            loading -> items(3) { SkeletonRow() }
            summary == null -> Unit
            else -> {
                item { OverallCard(summary.overall) }
                item { Text("BY MONTH", color = ModMuted, style = CmsTextStyles.eyebrow) }
                if (summary.months.isEmpty()) {
                    item { Text("No attendance has been recorded for this subject yet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium) }
                } else {
                    items(summary.months, key = { it.month.toString() }) { MonthRow(it) }
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun SummaryHero(name: String, courseCode: String, onBack: () -> Unit, canExport: Boolean, onExport: (ExportFormat) -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(start = 8.dp, end = 20.dp, top = 8.dp, bottom = 20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Register", color = CmsTheme.colors.onInk) }
                Spacer(Modifier.weight(1f))
                ExportMenuButton(onExport = onExport, enabled = canExport, tint = ModWarn)
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("TERM ATTENDANCE · $courseCode", color = ModWarn, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(name, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            }
        }
    }
}

@Composable
private fun StudentInfoCard(rollNumber: String, student: SessionStudent?, session: AcademicSession?, term: SemesterTerm?) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("STUDENT", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            InfoLine("Roll number", student?.rollNumber ?: rollNumber)
            InfoLine("Session", session?.let { "${it.label} · ${it.shift.name.lowercase().replaceFirstChar { c -> c.uppercase() }}" } ?: "--")
            InfoLine("Semester", session?.currentSemester?.toString() ?: "--")
            InfoLine(
                "Term",
                if (term?.startDate != null) {
                    "${term.startDate!!.format(SummaryDateFormatter)} – ${term.endDate?.format(SummaryDateFormatter) ?: "ongoing"}"
                } else {
                    "Dates not set"
                },
            )
            InfoLine("Student account", student?.linkedEmail?.takeIf { it.isNotBlank() } ?: "Not linked")
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, modifier = Modifier.width(120.dp), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

private fun percentColor(counts: AttendanceCounts): Color = when {
    counts.total == 0 -> ModMuted
    counts.isAtRisk -> ModAccent
    else -> ModSuccess
}

@Composable
private fun OverallCard(overall: AttendanceCounts) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text("OVERALL THIS TERM", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (overall.total == 0) "--" else "${overall.percentage}%",
                    color = percentColor(overall),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.displaySmall,
                )
                Spacer(Modifier.width(10.dp))
                Text("${overall.total} classes recorded", color = ModMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 6.dp))
            }
            if (overall.isAtRisk) {
                Text("BELOW 65% ATTENDANCE", color = ModAccent, style = CmsTextStyles.eyebrow)
            }
            Spacer(Modifier.height(10.dp))
            CountsRow(overall)
        }
    }
}

@Composable
private fun CountsRow(counts: AttendanceCounts) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        CountPill("P ${counts.present}", ModSuccess, Modifier.weight(1f))
        CountPill("A ${counts.absent}", ModAccent, Modifier.weight(1f))
        CountPill("L ${counts.leave}", ModWarn, Modifier.weight(1f))
        CountPill("Late ${counts.late}", ModInk, Modifier.weight(1f))
    }
}

@Composable
private fun CountPill(text: String, color: Color, modifier: Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MonthRow(month: MonthlyAttendance) {
    val counts = month.tally
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(month.month.format(SummaryMonthFormatter), modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(
                    if (counts.total == 0) "No classes" else "${counts.percentage}%",
                    color = percentColor(counts),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            if (counts.total > 0) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { counts.percentage / 100f },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = percentColor(counts),
                    trackColor = ModTrack,
                )
                Spacer(Modifier.height(8.dp))
                CountsRow(counts)
            }
        }
    }
}
