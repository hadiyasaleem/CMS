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
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme

private val RosterGold = Color(0xFF9A651B)
private val RosterGreen = Color(0xFF2F6B4F)
private val RosterRed = Color(0xFFB43A31)

enum class TeacherRosterFilter(val label: String) {
    ALL("All"),
    AT_RISK("Below 65%"),
    UNLINKED("Not linked"),
    MISSING_GRADES("Grades missing"),
}

enum class TeacherRosterSort(val label: String) {
    NAME("Name"),
    ROLL("Roll number"),
    ATTENDANCE("Attendance"),
}

@Composable
fun TeacherStudentRosterWorkspace(
    assignments: List<ResolvedAssignment>,
    selected: ResolvedAssignment?,
    students: List<SessionStudent>,
    tallies: Map<String, AttendanceTally>,
    onSelectAssignment: (ResolvedAssignment) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TeacherRosterFilter.ALL) }
    var sort by remember { mutableStateOf(TeacherRosterSort.NAME) }

    val linked = students.count { it.linkedEmail.isNotBlank() }
    val avgCgpa = students.mapNotNull { it.cgpa }.takeIf { it.isNotEmpty() }?.average()

    val filtered = students.filter { student ->
        val tally = tallies[student.rollNumber]
        val matchesQuery = query.isBlank() || student.name.contains(query, ignoreCase = true) || student.rollNumber.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            TeacherRosterFilter.ALL -> true
            TeacherRosterFilter.AT_RISK -> tally != null && tally.total > 0 && tally.percentage < 65f
            TeacherRosterFilter.UNLINKED -> student.linkedEmail.isBlank()
            TeacherRosterFilter.MISSING_GRADES -> student.gpa == null
        }
        matchesQuery && matchesFilter
    }

    val visible = when (sort) {
        TeacherRosterSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        TeacherRosterSort.ROLL -> filtered.sortedBy { it.rollNumber }
        TeacherRosterSort.ATTENDANCE -> filtered.sortedBy { tallies[it.rollNumber]?.percentage ?: 100f }
    }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TeacherRosterHero(selected, students.size) }
        item { AssignmentPicker(assignments, selected, onSelectAssignment) }

        if (selected != null) {
            item { TeacherRosterSummaryCard(students.size, avgCgpa, linked) }

            item {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Find attendance risks, account-link gaps, and missing grades") },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TeacherRosterFilter.entries.forEach { option -> CmsChip(option.label, selected = filter == option, onClick = { filter = option }) }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("SORT", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TeacherRosterSort.entries.forEach { option -> CmsChip(option.label, selected = sort == option, onClick = { sort = option }) }
                    }
                }
            }

            if (students.isEmpty()) {
                item { TeacherRosterEmpty("No students found", "This class has no enrolled students yet.") }
            } else if (visible.isEmpty()) {
                item { TeacherRosterEmpty("No matching students", "Try a different search or filter.") }
            } else {
                items(visible, key = { it.rollNumber }) { student -> TeacherStudentCard(student, tallies[student.rollNumber]) }
            }
        } else {
            item { TeacherRosterEmpty("Class roster", "Choose a class to review student progress") }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun TeacherRosterHero(selected: ResolvedAssignment?, count: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("FACULTY ROSTER", color = RosterGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("My Students", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                selected?.let { "${it.subjectLabel} · ${it.sessionLabel} · $count students" } ?: "Student directory",
                color = CmsTheme.colors.onInkMuted,
                style = MaterialTheme.typography.bodyMedium,
            )
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
private fun TeacherRosterSummaryCard(count: Int, avgCgpa: Double?, linked: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TeacherRosterMetric("Students", count.toString(), Modifier.weight(1f))
        TeacherRosterMetric("Avg CGPA", avgCgpa?.let { "%.2f".format(it) } ?: "--", Modifier.weight(1f))
        TeacherRosterMetric("Linked", "$linked / $count", Modifier.weight(1f))
    }
}

@Composable
private fun TeacherRosterMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun TeacherStudentCard(student: SessionStudent, tally: AttendanceTally?) {
    val atRisk = tally != null && tally.total > 0 && tally.percentage < 65f
    Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, if (atRisk) RosterRed.copy(alpha = 0.3f) else Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(student.name, size = 40)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                if (student.linkedEmail.isBlank()) StatusBadge("NOT LINKED", BadgeTone.Neutral)
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (tally == null || tally.total == 0) {
                    StatusBadge("No attendance", BadgeTone.Neutral)
                } else {
                    StatusBadge("${tally.percentage.toInt()}% present", if (atRisk) BadgeTone.Error else BadgeTone.Success)
                }
                StatusBadge("CGPA ${student.cgpa?.let { "%.2f".format(it) } ?: "--"}", BadgeTone.Neutral)
            }
            if (tally == null || tally.total == 0) {
                Spacer(Modifier.height(4.dp))
                Text("Attendance has not been marked for this subject.", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TeacherRosterEmpty(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}
