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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SessionStudent
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

private val ResultCanvas = ModGround
private val ResultBorder = ModTrack
private val ResultGreen = ModSuccess
private val ResultGold = ModWarn
private val ResultRed = ModAccent
private val ResultBlue = ModInk
private val ResultStatuses = listOf("PENDING", "PROMOTED", "PROBATION", "REPEATED")

@Composable
fun SemesterResultsWorkspace(
    sessions: List<Pair<String, String>>,
    sessionId: String?,
    semester: Int,
    roster: List<SessionStudent>,
    results: Map<String, SemesterGpa>,
    subjects: List<String>,
    saveOutcome: Outcome<Unit>,
    loadOutcome: Outcome<Unit>,
    onSelectSession: (String) -> Unit,
    onSemester: (Int) -> Unit,
    onRetry: () -> Unit,
    onClearSave: () -> Unit,
    onRecord: (String, Double, Double, String, String, Int?, String, List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember { mutableStateOf<SessionStudent?>(null) }

    val recorded = roster.count { results.containsKey(it.rollNumber) }
    val missing = roster.size - recorded
    val promoted = results.values.count { it.resultStatus == "PROMOTED" }
    val attention = results.values.count { it.resultStatus == "PROBATION" || it.resultStatus == "REPEATED" }
    val supply = results.values.count { it.supplyCourses.isNotEmpty() }
    val averageGpa = results.values.map { it.gpa }.takeIf { it.isNotEmpty() }?.average()
    val classLabel = sessions.firstOrNull { it.first == sessionId }?.second ?: "Select a class"

    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ResultCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { ResultsHeader(classLabel, semester, averageGpa) }

        if (loadOutcome is Outcome.Error) {
            item { ResultNotice(loadOutcome.message, ResultRed, "Retry", onRetry) }
        }
        if (saveOutcome is Outcome.Error) {
            item { ResultNotice(saveOutcome.message, ResultRed, "Dismiss", onClearSave) }
        }
        if (saveOutcome is Outcome.Success) {
            item { ResultNotice("Result saved.", ResultGreen, "Dismiss", onClearSave) }
        }

        item { SessionPicker(sessions, sessionId, onSelectSession) }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..8).forEach { sem -> CmsChip("Sem $sem", selected = semester == sem, onClick = { onSemester(sem) }) }
            }
        }
        item { ResultsMetrics(roster.size, recorded, missing, promoted, attention, supply) }

        if (roster.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ResultBorder)) {
                    Text("Select a class to record semester results.", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            items(roster, key = { it.rollNumber }) { student ->
                StudentResultCard(student, results[student.rollNumber], onEdit = { editing = student })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    editing?.let { student ->
        ResultEditorDialog(
            student = student,
            existing = results[student.rollNumber],
            subjects = subjects,
            busy = saveOutcome is Outcome.Loading,
            onDismiss = { editing = null },
            onSave = { gpa, cgpa, termLabel, status, position, remarks, supplyCourses ->
                onRecord(student.rollNumber, gpa, cgpa, termLabel, status, position, remarks, supplyCourses)
                editing = null
            },
        )
    }
}

@Composable
private fun ResultsHeader(classLabel: String, semester: Int, averageGpa: Double?) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ACADEMIC OUTCOMES", color = ResultGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Semester results", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$classLabel · Semester $semester" + (averageGpa?.let { " · Avg GPA %.2f".format(it) } ?: ""), color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SessionPicker(sessions: List<Pair<String, String>>, selected: String?, onSelect: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(sessions.firstOrNull { it.first == selected }?.second ?: "Select a class", modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sessions.forEach { (id, label) ->
                DropdownMenuItem(text = { Text(label) }, onClick = { onSelect(id); expanded = false })
            }
        }
    }
}

@Composable
private fun ResultsMetrics(total: Int, recorded: Int, missing: Int, promoted: Int, attention: Int, supply: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ResultMetric("Students", total.toString(), Modifier.weight(1f))
        ResultMetric("Recorded", recorded.toString(), Modifier.weight(1f))
        ResultMetric("Missing", missing.toString(), Modifier.weight(1f), alert = missing > 0)
        ResultMetric("Attention", attention.toString(), Modifier.weight(1f), alert = attention > 0)
        ResultMetric("Supply", supply.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun ResultMetric(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ResultBorder)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) ResultRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun StudentResultCard(student: SessionStudent, result: SemesterGpa?, onEdit: () -> Unit) {
    val tone = when (result?.resultStatus) {
        "PROMOTED" -> ResultGreen
        "PROBATION", "REPEATED" -> ResultRed
        else -> ResultGold
    }
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ResultBorder)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(result?.resultStatus ?: "MISSING", if (result == null) BadgeTone.Neutral else if (result.resultStatus == "PROMOTED") BadgeTone.Success else BadgeTone.Error)
            }
            if (result != null) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultValue("%.2f".format(result.gpa), "GPA")
                    ResultValue("%.2f".format(result.cgpa), "CGPA")
                    ResultValue(result.classPosition?.toString() ?: "--", "Position")
                }
                if (result.supplyCourses.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("SUPPLY: ${result.supplyCourses.joinToString(", ")}", color = tone, style = CmsTextStyles.eyebrow)
                }
            } else {
                Spacer(Modifier.height(6.dp))
                Text("GPA, CGPA, outcome, and progression details are still missing.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onEdit) { Text(if (result == null) "Record result" else "Edit result") }
        }
    }
}

@Composable
private fun ResultValue(value: String, label: String) {
    Surface(shape = RoundedCornerShape(10.dp), color = ResultBlue.copy(alpha = 0.08f)) {
        Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Text(label, color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun ResultNotice(message: String, color: Color, action: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAction) { Text(action, color = color) }
        }
    }
}

@Composable
private fun ResultEditorDialog(
    student: SessionStudent,
    existing: SemesterGpa?,
    subjects: List<String>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (Double, Double, String, String, Int?, String, List<String>) -> Unit,
) {
    var gpa by remember { mutableStateOf(existing?.gpa?.toString() ?: "") }
    var cgpa by remember { mutableStateOf(existing?.cgpa?.toString() ?: "") }
    var termLabel by remember { mutableStateOf(existing?.termLabel ?: "") }
    var status by remember { mutableStateOf(existing?.resultStatus ?: "PENDING") }
    var position by remember { mutableStateOf(existing?.classPosition?.toString() ?: "") }
    var remarks by remember { mutableStateOf(existing?.remarks ?: "") }
    var supplySelection by remember { mutableStateOf(existing?.supplyCourses?.toSet() ?: emptySet()) }

    val parsedGpa = gpa.toDoubleOrNull()
    val parsedCgpa = cgpa.toDoubleOrNull()
    val parsedPosition = position.toIntOrNull()
    val positionError = position.isNotBlank() && (parsedPosition == null || parsedPosition <= 0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit semester result", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = gpa, onValueChange = { gpa = it }, label = { Text("GPA") }, modifier = Modifier.weight(1f), singleLine = true)
                    OutlinedTextField(value = cgpa, onValueChange = { cgpa = it }, label = { Text("CGPA") }, modifier = Modifier.weight(1f), singleLine = true)
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = termLabel, onValueChange = { termLabel = it }, label = { Text("Term label (optional)") }, placeholder = { Text("Fall 2026") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("RESULT", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ResultStatuses.forEach { option -> CmsChip(option, selected = status == option, onClick = { status = option }) }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = position,
                    onValueChange = { position = it },
                    label = { Text("Class position (optional)") },
                    isError = positionError,
                    supportingText = { if (positionError) Text("Enter a positive whole number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (subjects.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("SUPPLY SUBJECTS", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        subjects.forEach { code ->
                            CmsChip(code, selected = code in supplySelection, onClick = {
                                supplySelection = if (code in supplySelection) supplySelection - code else supplySelection + code
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (parsedGpa != null && parsedCgpa != null && !positionError) {
                        onSave(parsedGpa, parsedCgpa, termLabel.trim(), status, parsedPosition, remarks.trim(), supplySelection.toList())
                    }
                },
                enabled = parsedGpa != null && parsedCgpa != null && !positionError && !busy,
            ) { Text(if (busy) "Saving..." else "Save result") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}
