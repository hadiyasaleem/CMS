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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mbd.cmscommon.domain.model.StudentResultsSnapshot
import com.mbd.cmscommon.domain.model.StudentSemesterResult
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn

private val ResultsCanvas = ModGround
private val ResultsGreen = ModSuccess
private val ResultsGold = ModWarn
private val ResultsRed = ModAccent
private val ResultsBlue = ModInk

@Composable
fun StudentResultsWorkspace(
    snapshot: StudentResultsSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ResultsCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { StudentResultsHeader() }

        if (loading) {
            item { StatusCard("Refreshing your semester results...") }
        }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }

        if (snapshot != null) {
            item { ResultsOverview(snapshot) }
            if (snapshot.strongestSemester != null) {
                item { ResultsHighlight(snapshot) }
            }
            if (snapshot.activeSupplyCourses.isNotEmpty()) {
                item { SupplyCard(snapshot.activeSupplyCourses) }
            }
            if (snapshot.semesters.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                        Text("No GPA recorded", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                items(snapshot.semesters.reversed(), key = { it.result.semester }) { row -> SemesterResultCard(row) }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun StudentResultsHeader() {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("PROGRESSION", color = ResultsGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Results", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Semester GPA, cumulative progress and recorded outcomes", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultsOverview(snapshot: StudentResultsSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("CURRENT CGPA", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(snapshot.currentCgpa?.let { "%.2f".format(it) } ?: "Not available", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            snapshot.currentGpa?.let { Text("Latest semester GPA %.2f".format(it), color = ModMuted, style = MaterialTheme.typography.bodySmall) }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultMetric(snapshot.promotedSemesters.toString(), "Promoted")
                ResultMetric(snapshot.semesters.size.toString(), "Semesters")
                ResultMetric(snapshot.cgpaChange?.let { (if (it >= 0) "+" else "") + "%.2f".format(it) } ?: "--", "CGPA change")
            }
        }
    }
}

@Composable
private fun ResultMetric(value: String, label: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun ResultsHighlight(snapshot: StudentResultsSnapshot) {
    val strongest = snapshot.strongestSemester ?: return
    Surface(shape = RoundedCornerShape(14.dp), color = ResultsGreen.copy(alpha = 0.08f), border = BorderStroke(1.dp, ResultsGreen.copy(alpha = 0.25f))) {
        Column(Modifier.padding(14.dp)) {
            Text("STRONGEST SEMESTER", color = ResultsGreen, style = CmsTextStyles.eyebrow)
            Text("Semester ${strongest.semester} · GPA %.2f".format(strongest.gpa), color = ResultsGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("This reflects the latest published semester result.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SupplyCard(courses: List<String>) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModWarn.copy(alpha = 0.14f), border = BorderStroke(1.dp, ResultsGold.copy(alpha = 0.3f))) {
        Column(Modifier.padding(14.dp)) {
            Text("Active retake courses", color = ResultsGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text("Retake: ${courses.joinToString(", ")}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SemesterResultCard(row: StudentSemesterResult) {
    val result = row.result
    val tone = when (result.resultStatus) {
        "PROMOTED" -> ResultsGreen
        "PROBATION", "REPEATED" -> ResultsRed
        else -> ResultsGold
    }
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Semester ${result.semester}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text(result.termLabel?.takeIf { it.isNotBlank() } ?: "Published result", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(result.resultStatus.ifBlank { "PENDING" }, if (result.resultStatus == "PROMOTED") BadgeTone.Success else if (result.resultStatus.isBlank() || result.resultStatus == "PENDING") BadgeTone.Neutral else BadgeTone.Error)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResultMetric("%.2f".format(result.gpa), "GPA")
                ResultMetric("%.2f".format(result.cgpa), "CGPA")
                ResultMetric(result.classPosition?.toString() ?: "--", "Position")
                row.gpaChange?.let { ResultMetric((if (it >= 0) "+" else "") + "%.2f".format(it), "Change") }
            }
            if (result.supplyCourses.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Retake: ${result.supplyCourses.joinToString(", ")}", color = ResultsGold, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun StatusCard(message: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = ResultsBlue.copy(alpha = 0.08f)) {
        Text(message, modifier = Modifier.padding(14.dp), color = ResultsBlue, style = MaterialTheme.typography.bodyMedium)
    }
}

