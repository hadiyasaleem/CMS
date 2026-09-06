package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.RiskSignal
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.averagePercentage
import com.mbd.cmscommon.domain.model.insightsSummary
import com.mbd.cmscommon.domain.model.reviewReasons
import com.mbd.cmscommon.domain.model.riskSignals
import com.mbd.cmscommon.domain.model.scopeTeacherInsights
import com.mbd.cmscommon.teacher.ResolvedAssignment
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
import java.util.Locale
import kotlin.math.roundToInt

private val InsightsCanvas = ModGround
private val InsightsNavy = ModInk
private val InsightsGreen = ModSuccess
private val InsightsGold = ModWarn
private val InsightsRed = ModAccent

enum class InsightsViewer { ADMIN, TEACHER }

private enum class InsightsTab(val label: String) {
    SESSIONS("Sessions"),
    AT_RISK("At risk"),
    ASSESSMENTS("Assessments"),
}

@Composable
fun InsightsWorkspace(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    sessions: List<AcademicSession>,
    departments: List<Department>,
    viewer: InsightsViewer,
    assignments: List<ResolvedAssignment> = emptyList(),
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var tab by remember { mutableStateOf(InsightsTab.SESSIONS) }
    var query by remember { mutableStateOf("") }
    var selectedSessionId by remember { mutableStateOf<String?>(null) }

    val scope = if (viewer == InsightsViewer.TEACHER) {
        scopeTeacherInsights(overviews, atRisk, examStats, assignments)
    } else {
        null
    }
    val scopedOverviews = scope?.overviews ?: overviews
    val scopedAtRisk = scope?.atRisk ?: atRisk
    val scopedExamStats = scope?.examStats ?: examStats
    val validSessionIds = sessions.map { it.sessionId }.toSet()

    val summary = insightsSummary(scopedOverviews, scopedAtRisk, scopedExamStats)

    fun sessionLabel(sessionId: String): String {
        val session = sessions.firstOrNull { it.sessionId == sessionId }
        val dept = departments.firstOrNull { it.deptId == session?.deptId }?.name
        return if (session != null) "${dept ?: session.deptId} ${session.label}" else sessionId
    }

    val filteredOverviews = scopedOverviews
        .filter { selectedSessionId == null || it.sessionId == selectedSessionId }
        .filter { query.isBlank() || sessionLabel(it.sessionId).contains(query, ignoreCase = true) }
        .sortedBy { sessionLabel(it.sessionId) }

    val filteredRisk = scopedAtRisk
        .filter { selectedSessionId == null || it.sessionId == selectedSessionId }
        .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.rollNumber.contains(query, ignoreCase = true) }
        .sortedByDescending { riskSignals(it).size }

    val filteredExams = scopedExamStats
        .filter { selectedSessionId == null || it.sessionId == selectedSessionId }
        .filter { query.isBlank() || it.courseCode.contains(query, ignoreCase = true) }
        .sortedBy { it.courseCode }

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(InsightsCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { InsightsHeader(viewer) }

        if (!errorMessage.isNullOrBlank()) {
            item { InsightsNotice(errorMessage, onRetry) }
        }

        item { InsightsSummaryStrip(summary.sessions, summary.students, summary.atRiskStudents, summary.weightedPassRate) }

        item {
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InsightsTab.entries.forEach { option ->
                        CmsChip(option.label, selected = tab == option, onClick = { tab = option })
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search sessions, students, or courses") },
                    singleLine = true,
                )
                if (sessions.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    CmsEntityPicker(
                        label = "Session",
                        selectedId = selectedSessionId,
                        options = sessions.map { CmsEntityOption(it.sessionId, sessionLabel(it.sessionId)) },
                        onSelected = { selectedSessionId = it },
                        optional = true,
                        emptyLabel = "All sessions",
                    )
                }
            }
        }

        when {
            loading -> items(3) { SkeletonRow() }
            tab == InsightsTab.SESSIONS -> if (filteredOverviews.isEmpty()) {
                item { InsightsEmpty("No sessions match these filters.") }
            } else {
                items(filteredOverviews, key = { it.sessionId }) { overview ->
                    SessionInsightCard(overview, sessionLabel(overview.sessionId), viewer, reviewReasons(overview, validSessionIds))
                }
            }
            tab == InsightsTab.AT_RISK -> if (filteredRisk.isEmpty()) {
                item { InsightsEmpty("No students currently show a risk signal.") }
            } else {
                items(filteredRisk, key = { it.sessionId + it.rollNumber }) { student ->
                    RiskStudentCard(student, sessionLabel(student.sessionId))
                }
            }
            else -> if (filteredExams.isEmpty()) {
                item { InsightsEmpty("No assessments match these filters.") }
            } else {
                items(filteredExams, key = { listOf(it.sessionId, it.semester, it.courseCode, it.examType).toString() }) { stat ->
                    ExamInsightCard(stat, sessionLabel(stat.sessionId))
                }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun InsightsHeader(viewer: InsightsViewer) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text(if (viewer == InsightsViewer.ADMIN) "INSTITUTIONAL INTELLIGENCE" else "MY CLASS INTELLIGENCE", color = InsightsGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Academic Insights", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                if (viewer == InsightsViewer.ADMIN) {
                    "College-wide performance, risk and assessment signals."
                } else {
                    "Performance and risk signals scoped to the classes you teach."
                },
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun InsightsSummaryStrip(sessions: Int, students: Int, atRisk: Int, weightedPassRate: Double?) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        InsightSummaryTile("Sessions", sessions.toString(), Modifier.weight(1f))
        InsightSummaryTile("Students", students.toString(), Modifier.weight(1f))
        InsightSummaryTile("At risk", atRisk.toString(), Modifier.weight(1f), alert = atRisk > 0)
        InsightSummaryTile("Weighted pass", weightedPassRate?.let { "${it.roundToInt()}%" } ?: "--", Modifier.weight(1f))
    }
}

@Composable
private fun InsightSummaryTile(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) InsightsRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun SessionInsightCard(overview: SessionOverview, sessionLabel: String, viewer: InsightsViewer, reasons: List<String>) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(sessionLabel, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("SEM ${overview.currentSemester} · ${overview.shift}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (viewer == InsightsViewer.ADMIN && reasons.isNotEmpty()) {
                    StatusBadge("ADMIN REVIEW", BadgeTone.Warning)
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("Students", overview.students.toString(), InsightsNavy)
                MetricPill("Average CGPA", overview.avgCgpa?.let { "%.2f".format(it) } ?: "--", InsightsGreen)
                MetricPill("Average attendance", overview.avgAttendance?.let { "${it.roundToInt()}%" } ?: "--", InsightsGold)
            }
            if (reasons.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                InsightReviewReasons(reasons)
            }
        }
    }
}

@Composable
private fun RiskStudentCard(student: AtRiskStudent, sessionLabel: String) {
    val signals = riskSignals(student)
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, if (signals.isNotEmpty()) InsightsRed.copy(alpha = 0.3f) else ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Roll ${student.rollNumber} · $sessionLabel", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("CGPA", student.cgpa?.let { "%.2f".format(it) } ?: "--", if (signals.contains(RiskSignal.CGPA)) InsightsRed else InsightsNavy)
                MetricPill("Attendance", student.attendance?.let { "${it.roundToInt()}%" } ?: "--", if (signals.contains(RiskSignal.ATTENDANCE)) InsightsRed else InsightsNavy)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (signals.contains(RiskSignal.ATTENDANCE)) StatusBadge("ATTENDANCE <75%", BadgeTone.Error)
                if (signals.contains(RiskSignal.CGPA)) StatusBadge("CGPA <2.00", BadgeTone.Error)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                when {
                    signals.size == 2 -> "Attendance and academic standing both need follow-up."
                    signals.contains(RiskSignal.ATTENDANCE) -> "Attendance is below the college's 75% threshold."
                    signals.contains(RiskSignal.CGPA) -> "CGPA is below the 2.00 academic-standing threshold."
                    else -> "No active risk signal."
                },
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun ExamInsightCard(stat: ExamStat, sessionLabel: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("${stat.courseCode} · ${stat.examType}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("$sessionLabel · SEM ${stat.semester}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge("Entered ${stat.entered}", BadgeTone.Neutral)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MetricPill("Average score", averagePercentage(stat)?.let { "${it.roundToInt()}%" } ?: "--", InsightsNavy)
                MetricPill("Pass rate", stat.passRate?.let { "${it.roundToInt()}%" } ?: "--", InsightsGreen)
                MetricPill("Range", "${stat.minScore ?: "--"} - ${stat.maxScore ?: "--"} / ${stat.outOf}", InsightsGold)
            }
            if (stat.stddev != null) {
                Spacer(Modifier.height(6.dp))
                Text("Variation: %.2f".format(stat.stddev), color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(6.dp))
            Text("Pass rate uses the college rule: score at least 40% of maximum marks.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, tone: Color) {
    Surface(shape = RoundedCornerShape(10.dp), color = tone.copy(alpha = 0.1f), border = BorderStroke(1.dp, tone.copy(alpha = 0.25f))) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(value, color = tone, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(label.uppercase(Locale.ROOT), color = tone, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun InsightReviewReasons(reasons: List<String>) {
    Column {
        Text("NEEDS REVIEW", color = InsightsRed, style = CmsTextStyles.eyebrow)
        reasons.forEach { reason -> Text("· $reason", color = ModMuted, style = MaterialTheme.typography.bodySmall) }
    }
}

@Composable
private fun InsightsNotice(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = InsightsRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, InsightsRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = InsightsRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = InsightsRed) }
        }
    }
}

@Composable
private fun InsightsEmpty(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Text(message, modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
