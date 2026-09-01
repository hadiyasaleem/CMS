package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceRegisterSummary
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.attendanceRegisterSummary
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
import com.mbd.cmscommon.util.Outcome
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val RegisterCanvas = ModGround
private val RegisterGreen = ModSuccess
private val RegisterGold = ModWarn
private val RegisterRed = ModAccent
private val RegisterBlue = ModInk
private val RegisterDateFormat = DateTimeFormatter.ofPattern("EEEE · dd MMM yyyy")

@Composable
fun MarkAttendanceWorkspace(
    heroPainter: Painter,
    assignments: List<ResolvedAssignment>,
    selected: ResolvedAssignment?,
    roster: List<SessionStudent>,
    termPercents: Map<String, Float>,
    statuses: Map<String, AttendanceStatus>,
    lateRolls: Set<String>,
    remarks: Map<String, String>,
    alreadyMarked: Boolean,
    allMarked: Boolean,
    lectureTopic: String,
    outcome: Outcome<Unit>,
    onSelect: (ResolvedAssignment) -> Unit,
    onStatus: (String, AttendanceStatus) -> Unit,
    onToggleLate: (String) -> Unit,
    onRemark: (String, String) -> Unit,
    onLectureTopic: (String) -> Unit,
    onHistory: (String, String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var noteRoll by remember { mutableStateOf<String?>(null) }
    val locked = alreadyMarked
    val summary = attendanceRegisterSummary(roster, statuses, lateRolls, termPercents)

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(RegisterCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { RegisterHeader(heroPainter) }
        item { AssignmentPicker(assignments, selected, onSelect) }
        item { RegisterSummary(summary, locked) }
        item {
            RegisterTools(
                selected = selected,
                locked = locked,
                topic = lectureTopic,
                onTopic = onLectureTopic,
                onHistory = { selected?.let { onHistory(it.sessionId, it.courseCode) } },
            )
        }

        if (roster.isEmpty()) {
            item { RegisterEmpty("No students are enrolled in ${selected?.subjectLabel ?: "this class"}.") }
        } else {
            items(roster, key = { it.rollNumber }) { student ->
                StudentAttendanceCard(
                    student = student,
                    status = statuses[student.rollNumber],
                    percent = termPercents[student.rollNumber],
                    isLate = lateRolls.contains(student.rollNumber),
                    remark = remarks[student.rollNumber],
                    locked = locked,
                    onStatus = { status -> onStatus(student.rollNumber, status) },
                    onToggleLate = { onToggleLate(student.rollNumber) },
                    onNote = { noteRoll = student.rollNumber },
                )
            }
        }

        item {
            SubmitCard(
                unmarked = summary.unmarked,
                locked = locked,
                allMarked = allMarked,
                loading = outcome is Outcome.Loading,
                onSubmit = onSubmit,
            )
        }
        item { SubmitNotice(outcome) }
        item { Spacer(Modifier.height(72.dp)) }
    }

    noteRoll?.let { roll ->
        val student = roster.firstOrNull { it.rollNumber == roll }
        var text by remember(roll) { mutableStateOf(remarks[roll] ?: "") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { noteRoll = null },
            title = { Text("Note: ${student?.name ?: roll}", style = MaterialTheme.typography.headlineSmall) },
            text = { OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth(), minLines = 2) },
            confirmButton = { TextButton(onClick = { onRemark(roll, text); noteRoll = null }) { Text("Add note") } },
            dismissButton = { TextButton(onClick = { noteRoll = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun RegisterHeader(heroPainter: Painter) {
    Surface(modifier = Modifier.fillMaxWidth().height(140.dp), shape = RoundedCornerShape(18.dp), color = ModInk) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = heroPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.CenterEnd,
                contentScale = ContentScale.Crop,
                alpha = 0.35f,
            )
            Column(Modifier.align(Alignment.CenterStart).padding(20.dp)) {
                Text("ATTENDANCE REGISTRY", color = RegisterGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Mark Attendance", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(LocalDate.now().format(RegisterDateFormat), color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun AssignmentPicker(assignments: List<ResolvedAssignment>, selected: ResolvedAssignment?, onSelect: (ResolvedAssignment) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text("MY CLASSES", color = ModMuted, style = CmsTextStyles.eyebrow)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selected?.let { "${it.subjectLabel} (${it.courseCode})" } ?: "Select a class", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                assignments.forEach { assignment ->
                    DropdownMenuItem(
                        text = { Text("${assignment.subjectLabel} · ${assignment.sessionLabel}") },
                        onClick = { onSelect(assignment); expanded = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun RegisterSummary(summary: AttendanceRegisterSummary, locked: Boolean) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("MARKING PROGRESS", modifier = Modifier.weight(1f), color = ModMuted, style = CmsTextStyles.eyebrow)
                if (locked) StatusBadge("REGISTER LOCKED", BadgeTone.Neutral)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { summary.progress },
                modifier = Modifier.fillMaxWidth().height(6.dp),
                color = RegisterGreen,
                trackColor = ModTrack,
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SummaryPill("Present ${summary.present}", RegisterGreen)
                SummaryPill("Absent ${summary.absent}", RegisterRed)
                SummaryPill("Leave ${summary.leave}", RegisterGold)
                SummaryPill("Late ${summary.late}", RegisterBlue)
                SummaryPill("Risk ${summary.atRisk}", RegisterRed)
            }
        }
    }
}

@Composable
private fun SummaryPill(text: String, color: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Text(text, modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), color = color, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun RegisterTools(selected: ResolvedAssignment?, locked: Boolean, topic: String, onTopic: (String) -> Unit, onHistory: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = topic,
                onValueChange = onTopic,
                label = { Text("Lecture topic (optional)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !locked,
                singleLine = true,
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onHistory, enabled = selected != null) { Text("View Attendance History") }
        }
    }
}

@Composable
private fun StudentAttendanceCard(
    student: SessionStudent,
    status: AttendanceStatus?,
    percent: Float?,
    isLate: Boolean,
    remark: String?,
    locked: Boolean,
    onStatus: (AttendanceStatus) -> Unit,
    onToggleLate: () -> Unit,
    onNote: () -> Unit,
) {
    val atRisk = percent != null && percent < 65f
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, if (atRisk) RegisterRed.copy(alpha = 0.3f) else ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (atRisk) StatusBadge("AT RISK", BadgeTone.Error)
                if (percent != null) Text("${percent.toInt()}%", modifier = Modifier.padding(start = 8.dp), color = if (atRisk) RegisterRed else ModMuted, style = MaterialTheme.typography.labelMedium)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (status != null) {
                    PalSegment(selected = status, onSelect = onStatus, enabled = !locked)
                } else {
                    Row {
                        AttendanceStatus.entries.forEach { entry ->
                            TextButton(onClick = { onStatus(entry) }, enabled = !locked) { Text(entry.name.take(1)) }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onToggleLate, enabled = !locked) { Text(if (isLate) "Late" else "New") }
                TextButton(onClick = onNote, enabled = !locked) { Text("Note") }
            }
            if (!remark.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text("Note: $remark", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun SubmitCard(unmarked: Int, locked: Boolean, allMarked: Boolean, loading: Boolean, onSubmit: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (locked) "Submitted for today. This register cannot be changed." else if (allMarked) "The register is complete and ready to submit." else "Every student needs an explicit P, A, or L status.",
                color = ModMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(10.dp))
            if (locked) {
                StatusBadge("Already Submitted", BadgeTone.Neutral)
            } else {
                CmsPrimaryButton(text = if (loading) "Submitting..." else "Submit Attendance", onClick = onSubmit, enabled = allMarked && unmarked == 0 && !loading)
            }
        }
    }
}

@Composable
private fun SubmitNotice(outcome: Outcome<Unit>) {
    when (outcome) {
        is Outcome.Loading -> Row(verticalAlignment = Alignment.CenterVertically) {
            CircularProgressIndicator(modifier = Modifier.height(16.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text("Attendance has been submitted.", color = RegisterBlue)
        }
        is Outcome.Success -> Text("Attendance submitted successfully.", color = RegisterGreen, style = MaterialTheme.typography.bodyMedium)
        is Outcome.Error -> Text(outcome.message, color = RegisterRed, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun RegisterEmpty(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Text(message, modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
