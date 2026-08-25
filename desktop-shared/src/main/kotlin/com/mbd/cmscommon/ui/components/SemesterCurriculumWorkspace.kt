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
import androidx.compose.material3.Switch
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
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SubjectType
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.LocalDate

private val CurriculumBlue = Color(0xFF24577A)
private val CurriculumGold = Color(0xFF9A651B)
private val CurriculumRed = Color(0xFFB43A31)

@Composable
fun SemesterCurriculumWorkspace(
    sessionId: String,
    session: AcademicSession?,
    semester: Int,
    subjects: List<SemesterSubject>,
    term: SemesterTerm?,
    loading: Boolean,
    errorMessage: String?,
    onSaveSubject: (String, String, String, Int, SubjectType, Boolean, String) -> Unit,
    onRemoveSubject: (String) -> Unit,
    onSaveTerm: (String, String, (Boolean) -> Unit) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var subjectEditor by remember { mutableStateOf<SemesterSubject?>(null) }
    var addingSubject by remember { mutableStateOf(false) }
    var showTermEditor by remember { mutableStateOf(false) }
    var pendingRemove by remember { mutableStateOf<SemesterSubject?>(null) }

    val totalCredits = subjects.sumOf { it.creditHours }
    val electiveCount = subjects.count { it.isElective }

    val visible = subjects.filter { query.isBlank() || it.name.contains(query, ignoreCase = true) || it.courseCode.contains(query, ignoreCase = true) }
        .sortedBy { it.courseCode }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { CurriculumHero(session, semester, subjects.size, totalCredits, onAdd = { addingSubject = true }) }

        if (!errorMessage.isNullOrBlank()) {
            item { ValidationMessage(errorMessage) }
        }

        item { CurriculumSummaryCard(subjects.size, totalCredits, electiveCount) }
        item { TermReadinessCard(term, onClick = { showTermEditor = true }) }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search subjects") },
                singleLine = true,
            )
        }

        when {
            loading -> items(3) { SkeletonRow() }
            subjects.isEmpty() -> item { CurriculumEmptyState(hasSubjects = false, onAdd = { addingSubject = true }, onClear = {}) }
            visible.isEmpty() -> item { CurriculumEmptyState(hasSubjects = true, onAdd = {}, onClear = { query = "" }) }
            else -> items(visible, key = { it.courseCode }) { subject ->
                SubjectCurriculumCard(subject, onEdit = { subjectEditor = subject }, onRemove = { pendingRemove = subject })
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (addingSubject || subjectEditor != null) {
        SubjectEditorDialog(
            existing = subjectEditor,
            existingCodes = subjects.map { it.courseCode.uppercase() }.toSet(),
            onDismiss = { addingSubject = false; subjectEditor = null },
            onSave = { code, name, credits, type, elective, outline ->
                onSaveSubject(subjectEditor?.courseCode ?: code, code, name, credits, type, elective, outline)
                addingSubject = false
                subjectEditor = null
            },
        )
    }

    if (showTermEditor) {
        TermDatesEditorDialog(
            initialStart = term?.startDate,
            initialEnd = term?.endDate,
            onDismiss = { showTermEditor = false },
            onSave = { start, end, onResult -> onSaveTerm(start, end) { done -> onResult(done); if (done) showTermEditor = false } },
        )
    }

    pendingRemove?.let { subject ->
        ConfirmDestructiveActionDialog(
            title = "Remove subject",
            dependentSummary = "Removes ${subject.name} (${subject.courseCode}) from semester $semester.",
            onConfirm = { onRemoveSubject(subject.courseCode); pendingRemove = null },
            onDismiss = { pendingRemove = null },
        )
    }
}

@Composable
private fun CurriculumHero(session: AcademicSession?, semester: Int, subjectCount: Int, totalCredits: Int, onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("SESSION CURRICULUM", color = CurriculumGold, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text("Session curriculum", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${session?.label ?: "Session"} · Semester $semester · $subjectCount subject(s) · $totalCredits credits",
                    color = CmsTheme.colors.onInkMuted,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            CmsPrimaryButton(text = "Add subject", onClick = onAdd)
        }
    }
}

@Composable
private fun CurriculumSummaryCard(subjectCount: Int, totalCredits: Int, electiveCount: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CurriculumMetric("Subjects", subjectCount.toString(), Modifier.weight(1f))
        CurriculumMetric("Credits", totalCredits.toString(), Modifier.weight(1f))
        CurriculumMetric("Electives", electiveCount.toString(), Modifier.weight(1f))
    }
}

@Composable
private fun CurriculumMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun TermReadinessCard(term: SemesterTerm?, onClick: () -> Unit) {
    val configured = term?.startDate != null && term.endDate != null
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Class term", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (configured) "${term?.startDate} — ${term?.endDate}" else "Start and end dates are not fully configured",
                    color = if (configured) Color(0xFF77716A) else CurriculumRed,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TextButton(onClick = onClick) { Text(if (configured) "Edit dates" else "Set dates") }
        }
    }
}

@Composable
private fun SubjectCurriculumCard(subject: SemesterSubject, onEdit: () -> Unit, onRemove: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(subject.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("${subject.courseCode} · ${subject.creditHours} credit(s)", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(subject.subjectType.name, if (subject.subjectType == SubjectType.LAB) BadgeTone.Navy else BadgeTone.Neutral)
                if (subject.isElective) {
                    Spacer(Modifier.width(6.dp))
                    StatusBadge("ELECTIVE", BadgeTone.Gold)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(subject.outline?.takeIf { it.isNotBlank() } ?: "No course outline has been added.", color = Color(0xFF625E58), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onRemove) { Text("Remove", color = CmsTheme.colors.accent) }
            }
        }
    }
}

@Composable
private fun ValidationMessage(message: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = CurriculumRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, CurriculumRed.copy(alpha = 0.25f))) {
        Text(message, modifier = Modifier.padding(14.dp), color = CurriculumRed, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CurriculumEmptyState(hasSubjects: Boolean, onAdd: () -> Unit, onClear: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (hasSubjects) "No matching subjects" else "No subjects configured", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasSubjects) "Clear the search and filters to see the complete curriculum." else "Add the first subject to make it available in timetable and assessment workflows.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = if (hasSubjects) "Clear filters" else "Add subject", onClick = if (hasSubjects) onClear else onAdd)
        }
    }
}

@Composable
private fun SubjectEditorDialog(
    existing: SemesterSubject?,
    existingCodes: Set<String>,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, SubjectType, Boolean, String) -> Unit,
) {
    var code by remember { mutableStateOf(existing?.courseCode ?: "") }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var credits by remember { mutableStateOf(existing?.creditHours?.toString() ?: "") }
    var type by remember { mutableStateOf(existing?.subjectType ?: SubjectType.THEORY) }
    var elective by remember { mutableStateOf(existing?.isElective ?: false) }
    var outline by remember { mutableStateOf(existing?.outline ?: "") }

    val parsedCredits = credits.toIntOrNull()
    val duplicate = existing == null && code.trim().uppercase() in existingCodes
    val error = when {
        code.isBlank() -> null
        duplicate -> "This course code already exists in this semester."
        parsedCredits == null || parsedCredits !in 1..6 -> "Credit hours must be between 1 and 6."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Semester subject" else "Edit subject", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course code") }, placeholder = { Text("IT-301") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Subject name") }, placeholder = { Text("Operating Systems") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = credits, onValueChange = { credits = it }, label = { Text("Credit hours") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("SUBJECT TYPE", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SubjectType.entries.forEach { option -> CmsChip(option.name, selected = type == option, onClick = { type = option }) }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Elective", modifier = Modifier.weight(1f))
                    Switch(checked = elective, onCheckedChange = { elective = it })
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = outline, onValueChange = { outline = it }, label = { Text("Course outline (optional)") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = CurriculumRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedCredits?.let { onSave(code.trim().uppercase(), name.trim(), it, type, elective, outline.trim()) } },
                enabled = code.isNotBlank() && name.isNotBlank() && error == null,
            ) { Text(if (existing == null) "Add" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun TermDatesEditorDialog(
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onDismiss: () -> Unit,
    onSave: (String, String, (Boolean) -> Unit) -> Unit,
) {
    var start by remember { mutableStateOf(initialStart?.toString() ?: "") }
    var end by remember { mutableStateOf(initialEnd?.toString() ?: "") }
    var error by remember { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }

    val parsedStart = runCatching { if (start.isBlank()) null else LocalDate.parse(start.trim()) }
    val parsedEnd = runCatching { if (end.isBlank()) null else LocalDate.parse(end.trim()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Class term", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("Set the semester start and end dates. Leave both blank to clear the term.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = start, onValueChange = { start = it }, label = "Start date", optional = true)
                Spacer(Modifier.height(10.dp))
                CmsDateField(value = end, onValueChange = { end = it }, label = "End date", optional = true)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error ?: "", color = CurriculumRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (parsedStart.isFailure) {
                        error = "Use a valid YYYY-MM-DD start date."
                    } else if (parsedEnd.isFailure) {
                        error = "Use a valid YYYY-MM-DD end date."
                    } else if (parsedStart.getOrNull() != null && parsedEnd.getOrNull() != null && parsedEnd.getOrNull()!!.isBefore(parsedStart.getOrNull())) {
                        error = "End date cannot be before start date."
                    } else {
                        saving = true
                        onSave(start.trim(), end.trim()) { done ->
                            saving = false
                            if (!done) error = "The term dates could not be saved. Please try again."
                        }
                    }
                },
                enabled = !saving,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !saving) { Text("Cancel") } },
    )
}
