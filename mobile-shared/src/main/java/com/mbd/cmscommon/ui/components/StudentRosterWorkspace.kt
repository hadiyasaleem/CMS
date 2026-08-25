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
import androidx.compose.material3.AlertDialog
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
import com.mbd.cmscommon.controller.BulkImportSummary
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.ImportedStudentRow
import com.mbd.cmscommon.util.StudentImportResult

private val RosterGreen = Color(0xFF2F6B4F)
private val RosterGold = Color(0xFF9A651B)
private val RosterRed = Color(0xFFB43A31)

@Composable
fun StudentRosterWorkspace(
    session: AcademicSession?,
    students: List<SessionStudent>,
    importing: Boolean,
    importPreview: StudentImportResult?,
    importResult: BulkImportSummary?,
    errorMessage: String?,
    onOpenStudent: (SessionStudent) -> Unit,
    onAddStudent: (String, String) -> Unit,
    onDeleteStudent: (SessionStudent) -> Unit,
    onPickImportFile: () -> Unit,
    onConfirmImport: (List<ImportedStudentRow>) -> Unit,
    onDismissImportPreview: () -> Unit,
    onDismissImportResult: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var showAddStudent by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<SessionStudent?>(null) }

    val maxStudents = session?.maxStudents ?: 0
    val isFull = maxStudents > 0 && students.size >= maxStudents
    val withGpa = students.count { it.cgpa != null }
    val avgCgpa = students.mapNotNull { it.cgpa }.takeIf { it.isNotEmpty() }?.average()

    val visible = students.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.rollNumber.contains(query, ignoreCase = true) }
        .sortedBy { it.rollNumber }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            RosterHero(
                session = session,
                studentCount = students.size,
                maxStudents = maxStudents,
                importing = importing,
                onAdd = { showAddStudent = true },
                onImport = onPickImportFile,
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = RosterRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, RosterRed.copy(alpha = 0.25f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(errorMessage, modifier = Modifier.weight(1f), color = RosterRed, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onClearError) { Text("Dismiss") }
                    }
                }
            }
        }

        item { RosterSummaryCard(students.size, avgCgpa, withGpa, (maxStudents - students.size).coerceAtLeast(0)) }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or roll number") },
                singleLine = true,
            )
        }

        when {
            students.isEmpty() -> item { RosterEmptyState(hasStudents = false, isFull = false, onAdd = { showAddStudent = true }, onClear = {}) }
            visible.isEmpty() -> item { RosterEmptyState(hasStudents = true, isFull = false, onAdd = {}, onClear = { query = "" }) }
            else -> items(visible, key = { it.rollNumber }) { student ->
                StudentProfileCard(student, onOpen = { onOpenStudent(student) }, onDelete = { pendingDelete = student })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (showAddStudent) {
        AddRosterStudentDialog(
            existingRolls = students.map { it.rollNumber.uppercase() }.toSet(),
            isFull = isFull,
            onDismiss = { showAddStudent = false },
            onConfirm = { roll, name -> onAddStudent(roll, name); showAddStudent = false },
        )
    }

    importPreview?.let { preview ->
        StudentImportPreviewDialog(
            result = preview,
            currentCount = students.size,
            maxStudents = maxStudents,
            onConfirm = { onConfirmImport(preview.rows) },
            onDismiss = onDismissImportPreview,
        )
    }

    importResult?.let { result ->
        StudentImportResultDialog(summary = result, onDismiss = onDismissImportResult)
    }

    pendingDelete?.let { student ->
        ConfirmDestructiveActionDialog(
            title = "Remove student",
            dependentSummary = "Removes ${student.name} (Roll ${student.rollNumber}) from this session.",
            onConfirm = { onDeleteStudent(student); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun RosterHero(
    session: AcademicSession?,
    studentCount: Int,
    maxStudents: Int,
    importing: Boolean,
    onAdd: () -> Unit,
    onImport: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("CLASS ROSTER", color = RosterGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Session students", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "${session?.label ?: "Session"} · $studentCount" + (if (maxStudents > 0) " / $maxStudents enrolled" else " enrolled"),
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmsPrimaryButton(text = "Add student", onClick = onAdd)
                TextButton(onClick = onImport, enabled = !importing) { Text(if (importing) "Importing" else "Import file", color = CmsTheme.colors.onInk) }
            }
        }
    }
}

@Composable
private fun RosterSummaryCard(enrolled: Int, avgCgpa: Double?, gpaRecords: Int, seatsRemaining: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RosterMetric("Enrolled", enrolled.toString(), Modifier.weight(1f))
        RosterMetric("Avg CGPA", avgCgpa?.let { "%.2f".format(it) } ?: "No valid CGPA recorded", Modifier.weight(1f))
        RosterMetric("GPA records", gpaRecords.toString(), Modifier.weight(1f))
        RosterMetric("Seats remaining", seatsRemaining.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun RosterMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun StudentProfileCard(student: SessionStudent, onOpen: () -> Unit, onDelete: () -> Unit) {
    val linked = student.linkedEmail.isNotBlank()
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(student.name, size = 40)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(if (linked) "LINKED" else "NOT LINKED", if (linked) BadgeTone.Success else BadgeTone.Neutral)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge("GPA ${student.gpa?.let { "%.2f".format(it) } ?: "--"}", BadgeTone.Neutral)
                StatusBadge("CGPA ${student.cgpa?.let { "%.2f".format(it) } ?: "--"}", BadgeTone.Neutral)
            }
            if (!linked) {
                Spacer(Modifier.height(4.dp))
                Text("Student-app account not connected", color = RosterGold, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onOpen) { Text("Open profile") }
                TextButton(onClick = onDelete) { Text("Remove", color = CmsTheme.colors.accent) }
            }
        }
    }
}

@Composable
private fun RosterEmptyState(hasStudents: Boolean, isFull: Boolean, onAdd: () -> Unit, onClear: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (hasStudents) "No matching students" else "No students enrolled", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasStudents) "Clear the search and filters to see the full roster." else "Add a student manually or import a CSV/Excel roster.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            if (hasStudents) {
                CmsPrimaryButton(text = "Clear filters", onClick = onClear)
            } else if (!isFull) {
                CmsPrimaryButton(text = "Add student", onClick = onAdd)
            }
        }
    }
}

@Composable
private fun AddRosterStudentDialog(existingRolls: Set<String>, isFull: Boolean, onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var roll by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    val duplicate = roll.trim().uppercase() in existingRolls
    val error = when {
        isFull -> "Roster is full"
        duplicate -> "This roll number is already enrolled."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add student", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Class roll number *") }, placeholder = { Text("IT-21-09") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name *") }, placeholder = { Text("Student name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = RosterRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(roll.trim(), name.trim()) }, enabled = roll.isNotBlank() && name.isNotBlank() && error == null) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
