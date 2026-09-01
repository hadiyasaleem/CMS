package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.SessionStudent
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

private val MarksCanvas = ModGround
private val MarksGreen = ModSuccess
private val MarksGold = ModWarn
private val MarksRed = ModAccent
private val MarksBlue = ModInk

@Composable
fun MarksEntryWorkspace(
    assignments: List<ResolvedAssignment>,
    selected: ResolvedAssignment?,
    examType: ExamType,
    roster: List<SessionStudent>,
    scores: Map<String, String>,
    lockedRolls: Set<String>,
    pendingByRoll: Map<String, MarkEditRequest>,
    absentRolls: Set<String>,
    savedAbsentRolls: Set<String>,
    saveOutcome: Outcome<Unit>,
    requestOutcome: Outcome<Unit>,
    onSelect: (ResolvedAssignment) -> Unit,
    onExamType: (ExamType) -> Unit,
    onScore: (String, String) -> Unit,
    onToggleAbsent: (String) -> Unit,
    onSave: () -> Unit,
    onClearRequestState: () -> Unit,
    onRequestEdit: (String, Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editTarget by remember { mutableStateOf<SessionStudent?>(null) }
    val maxMarks = examType.maxMarks

    val validScores = roster.count { student ->
        val raw = scores[student.rollNumber]
        val n = raw?.toIntOrNull()
        (n != null && n in 0..maxMarks) || absentRolls.contains(student.rollNumber)
    }
    val locked = roster.count { lockedRolls.contains(it.rollNumber) }
    val pending = pendingByRoll.size
    val invalid = roster.size - validScores - locked

    val average = roster.mapNotNull { scores[it.rollNumber]?.toIntOrNull() }.takeIf { it.isNotEmpty() }?.average()

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(MarksCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { MarksHeader(selected, examType, average) }
        item { AssignmentPicker(assignments, selected, onSelect) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ExamType.entries.forEach { type -> CmsChip(type.name, selected = examType == type, onClick = { onExamType(type) }) }
            }
        }
        item { MarksMetrics(roster.size, locked, validScores, absentRolls.size, pending) }

        if (roster.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text("Select a class to begin marks entry.", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(roster, key = { it.rollNumber }) { student ->
                StudentMarkCard(
                    student = student,
                    rawScore = scores[student.rollNumber] ?: "",
                    maxMarks = maxMarks,
                    locked = lockedRolls.contains(student.rollNumber),
                    pending = pendingByRoll[student.rollNumber],
                    absent = absentRolls.contains(student.rollNumber),
                    savedAbsent = savedAbsentRolls.contains(student.rollNumber),
                    onScore = { onScore(student.rollNumber, it) },
                    onToggleAbsent = { onToggleAbsent(student.rollNumber) },
                    onRequestEdit = { editTarget = student },
                )
            }
        }

        item { SaveMarksCard(examType, validScores, invalid, saveOutcome, onSave) }
        item { MarksNotice(requestOutcome, onClearRequestState) }
        item { Spacer(Modifier.height(72.dp)) }
    }

    editTarget?.let { student ->
        RequestMarkEditDialog(
            student = student,
            currentScore = scores[student.rollNumber]?.toIntOrNull(),
            maxMarks = maxMarks,
            onDismiss = { editTarget = null },
            onSubmit = { newScore, reason -> onRequestEdit(student.rollNumber, newScore, reason); editTarget = null },
        )
    }
}

@Composable
private fun MarksHeader(selected: ResolvedAssignment?, examType: ExamType, average: Double?) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ASSESSMENT WORKSPACE", color = MarksGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Marks entry", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                selected?.let { "${it.subjectLabel} · ${it.sessionLabel} · $examType" } ?: "Select a class",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            if (average != null) {
                Spacer(Modifier.height(4.dp))
                Text("Class average: %.1f".format(average), color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun AssignmentPicker(assignments: List<ResolvedAssignment>, selected: ResolvedAssignment?, onSelect: (ResolvedAssignment) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
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

@Composable
private fun MarksMetrics(total: Int, locked: Int, ready: Int, absent: Int, pending: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        MarkMetric("Students", total.toString(), Modifier.weight(1f))
        MarkMetric("Ready", ready.toString(), Modifier.weight(1f))
        MarkMetric("Locked", locked.toString(), Modifier.weight(1f))
        MarkMetric("Absent", absent.toString(), Modifier.weight(1f))
        MarkMetric("Pending", pending.toString(), Modifier.weight(1f), alert = pending > 0)
    }
}

@Composable
private fun MarkMetric(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) MarksRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun StudentMarkCard(
    student: SessionStudent,
    rawScore: String,
    maxMarks: Int,
    locked: Boolean,
    pending: MarkEditRequest?,
    absent: Boolean,
    savedAbsent: Boolean,
    onScore: (String) -> Unit,
    onToggleAbsent: () -> Unit,
    onRequestEdit: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                if (savedAbsent) StatusPill("Saved as absent", MarksGold)
                if (locked) StatusPill("Saved and locked", MarksBlue)
                if (pending != null) StatusPill("Awaiting review", MarksGold)
            }
            Spacer(Modifier.height(8.dp))
            if (locked) {
                Text("Score locked (${rawScore.ifBlank { "--" }}/$maxMarks).", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                if (pending != null) {
                    Text("Pending review | requested ${pending.requestedScore}", color = MarksGold, style = MaterialTheme.typography.bodySmall)
                } else {
                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = onRequestEdit) { Text("Request edit") }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = rawScore,
                        onValueChange = onScore,
                        modifier = Modifier.width(100.dp),
                        enabled = !absent,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("Score / $maxMarks") },
                    )
                    Spacer(Modifier.width(12.dp))
                    TextButton(onClick = onToggleAbsent) { Text(if (absent) "Will be saved as absent" else "Absent") }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, tone: Color) {
    Surface(shape = RoundedCornerShape(8.dp), color = tone.copy(alpha = 0.1f), border = BorderStroke(1.dp, tone.copy(alpha = 0.25f))) {
        Text(label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = tone, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SaveMarksCard(examType: ExamType, ready: Int, invalid: Int, outcome: Outcome<Unit>, onSave: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text(
                if (invalid > 0) "Enter a score or mark a student absent to enable saving." else "Ready to save $examType marks for $ready student(s).",
                color = ModMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(10.dp))
            val loading = outcome is Outcome.Loading
            CmsPrimaryButton(text = if (loading) "Saving..." else "Save marks", onClick = onSave, enabled = ready > 0 && invalid == 0 && !loading)
            if (outcome is Outcome.Success) {
                Spacer(Modifier.height(8.dp))
                Text("Marks saved successfully.", color = MarksGreen, style = MaterialTheme.typography.bodySmall)
            } else if (outcome is Outcome.Error) {
                Spacer(Modifier.height(8.dp))
                Text(outcome.message, color = MarksRed, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun MarksNotice(outcome: Outcome<Unit>, onDismiss: () -> Unit) {
    when (outcome) {
        is Outcome.Loading -> Text("Submitting...", color = MarksBlue, style = MaterialTheme.typography.bodySmall)
        is Outcome.Success -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Edit request sent for review.", modifier = Modifier.weight(1f), color = MarksGreen, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
        is Outcome.Error -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(outcome.message, modifier = Modifier.weight(1f), color = MarksRed, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}

@Composable
private fun RequestMarkEditDialog(
    student: SessionStudent,
    currentScore: Int?,
    maxMarks: Int,
    onDismiss: () -> Unit,
    onSubmit: (Int, String) -> Unit,
) {
    var newScore by remember { mutableStateOf(currentScore?.toString() ?: "") }
    var reason by remember { mutableStateOf("") }
    val parsed = newScore.toIntOrNull()
    val valid = parsed != null && parsed in 0..maxMarks

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Request edit", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Current score: ${currentScore ?: "--"}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = newScore,
                    onValueChange = { newScore = it },
                    label = { Text("New score / $maxMarks") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = reason,
                    onValueChange = { if (it.length <= 500) reason = it },
                    label = { Text("Reason (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
                Text("Maximum 500 characters", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(onClick = { parsed?.let { onSubmit(it, reason) } }, enabled = valid) { Text("Send request") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
