package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.myStudentsExport
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn

private val RosterGold = ModWarn
private val RosterGreen = ModSuccess
private val RosterRed = ModAccent

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
    /** Switches back to the combined roster across every class. */
    onShowAllClasses: () -> Unit = {},
    onExport: ((ExportDocument, ExportFormat) -> Unit)? = null,
    /** Why the roster/attendance refresh failed (a named reason), so an empty list isn't mistaken for an empty class. */
    syncError: String? = null,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(TeacherRosterSort.NAME) }

    val filtered = students.filter { student ->
        query.isBlank() || student.name.contains(query, ignoreCase = true) || student.rollNumber.contains(query, ignoreCase = true)
    }

    val visible = when (sort) {
        TeacherRosterSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        TeacherRosterSort.ROLL -> filtered.sortedBy { it.rollNumber }
        TeacherRosterSort.ATTENDANCE -> filtered.sortedBy { tallies[it.id]?.percentage ?: 100f }
    }

    if (onExport != null) {
        TopBarActions {
            ExportMenuButton(onExport = { format -> onExport(myStudentsExport(selected, students, tallies), format) }, enabled = students.isNotEmpty(), tint = CmsTheme.colors.onInk)
        }
    }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        syncError?.let { message -> item { CmsNotice(message = message) } }
        item {
            TeacherClassPicker(assignments, selected, onSelectAssignment, showAllOption = true, onSelectAll = onShowAllClasses)
        }

        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search name or roll number", maxLines = 1) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Text("SORT", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TeacherRosterSort.entries.forEach { option -> CmsChip(option.label, selected = sort == option, onClick = { sort = option }) }
                }
            }
        }

        if (students.isEmpty()) {
            item { TeacherRosterEmpty("No students found", if (selected == null) "None of your classes have enrolled students yet." else "This class has no enrolled students yet.") }
        } else if (visible.isEmpty()) {
            item { TeacherRosterEmpty("No matching students", "Try a different search or filter.") }
        } else {
            items(visible, key = { it.id }) { student -> TeacherStudentCard(student, tallies[student.id]) }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun TeacherStudentCard(student: SessionStudent, tally: AttendanceTally?) {
    val atRisk = tally != null && tally.total > 0 && tally.percentage < 65f
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, if (atRisk) RosterRed.copy(alpha = 0.3f) else ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(student.name, size = 40)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Text("Roll ${student.rollNumber} · ${student.shift.label}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
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
                Text("Attendance has not been marked for this subject.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TeacherRosterEmpty(title: String, detail: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}
