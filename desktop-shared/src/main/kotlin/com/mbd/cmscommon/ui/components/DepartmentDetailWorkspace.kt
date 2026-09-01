package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
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
import com.mbd.cmscommon.controller.departmentDetailSnapshot
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModRedTint
import java.util.Locale

@Composable
fun DepartmentDetailWorkspace(
    department: Department?,
    fallbackName: String,
    sessions: List<AcademicSession>,
    studentCounts: Map<String, Int>,
    teachers: List<Teacher>,
    errorMessage: String?,
    onOpenSession: (String) -> Unit,
    onCreateSession: (Int, Session) -> Unit,
    onUpdateDepartment: (String, String, String?, String?) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var shiftFilter by remember { mutableStateOf<Session?>(null) }
    var showAddSession by remember { mutableStateOf(false) }
    var showEditDepartment by remember { mutableStateOf(false) }

    val snapshot = departmentDetailSnapshot(sessions, studentCounts)
    val filtered = snapshot.sessions
        .filter { shiftFilter == null || it.shift == shiftFilter }
        .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) || (it.programName ?: "").contains(query, ignoreCase = true) }
        .sortedByDescending { it.startYear }

    CardGrid(modifier.fillMaxWidth()) {
        fullSpanItem {
            DepartmentIdentityCard(
                department = department,
                fallbackName = fallbackName,
                hasHod = !department?.hodEmail.isNullOrBlank(),
                onEdit = { showEditDepartment = true },
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            fullSpanItem {
                Surface(shape = RoundedCornerShape(14.dp), color = ModRedTint, border = BorderStroke(1.dp, CmsTheme.colors.accent.copy(alpha = 0.25f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(errorMessage, modifier = Modifier.weight(1f), color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onClearError) { Text("Dismiss") }
                    }
                }
            }
        }

        fullSpanItem {
            DepartmentSummary(
                sessionCount = snapshot.sessions.size,
                studentCount = snapshot.studentCount,
                totalCapacity = snapshot.totalCapacity,
                remainingSeats = snapshot.remainingSeats,
                occupiedPercent = snapshot.occupancyPercent,
                sessionsNeedingSetup = snapshot.sessionsNeedingSetup,
                hasHod = !department?.hodEmail.isNullOrBlank(),
            )
        }

        fullSpanItem {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Current intakes", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                CmsPrimaryButton(text = "Create session", onClick = { showAddSession = true })
            }
        }

        fullSpanItem {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search intakes or programs") },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CmsChip("All shifts", selected = shiftFilter == null, onClick = { shiftFilter = null })
                    Session.entries.forEach { shift ->
                        CmsChip(shift.name, selected = shiftFilter == shift, onClick = { shiftFilter = shift })
                    }
                }
            }
        }

        if (snapshot.sessions.isEmpty()) {
            fullSpanItem {
                SessionEmptyState(filtered = false, onAction = { showAddSession = true })
            }
        } else if (filtered.isEmpty()) {
            fullSpanItem {
                SessionEmptyState(filtered = true, onAction = { query = ""; shiftFilter = null })
            }
        } else {
            items(filtered, key = { it.sessionId }) { session ->
                DepartmentSessionCard(session, studentCounts[session.sessionId] ?: 0, onClick = { onOpenSession(session.sessionId) })
            }
        }

        fullSpanItem { Spacer(Modifier.height(72.dp)) }
    }

    if (showAddSession) {
        AddDepartmentSessionDialog(
            onDismiss = { showAddSession = false },
            onConfirm = { year, shift -> onCreateSession(year, shift); showAddSession = false },
        )
    }

    if (showEditDepartment && department != null) {
        EditDepartmentDetailsDialog(
            department = department,
            onDismiss = { showEditDepartment = false },
            onConfirm = { name, code, hod, description ->
                onUpdateDepartment(name, code, hod, description)
                showEditDepartment = false
            },
        )
    }
}

@Composable
private fun DepartmentIdentityCard(department: Department?, fallbackName: String, hasHod: Boolean, onEdit: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("DEPARTMENT", color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(department?.name ?: fallbackName, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                if (department != null) {
                    Text("Code ${department.code}", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(6.dp))
                StatusBadge(if (hasHod) "HOD ASSIGNED" else "HOD NOT ASSIGNED", if (hasHod) BadgeTone.Success else BadgeTone.Warning)
            }
            TextButton(onClick = onEdit) { Text("Edit", color = CmsTheme.colors.onInk) }
        }
    }
}

@Composable
private fun DepartmentSummary(
    sessionCount: Int,
    studentCount: Int,
    totalCapacity: Int,
    remainingSeats: Int,
    occupiedPercent: Float,
    sessionsNeedingSetup: Int,
    hasHod: Boolean,
) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SessionMetric("Sessions", sessionCount.toString(), Modifier.weight(1f))
            SessionMetric("Students", studentCount.toString(), Modifier.weight(1f))
            SessionMetric("Seats left", remainingSeats.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        CapacityBar(count = studentCount, max = totalCapacity.coerceAtLeast(1))
        Spacer(Modifier.height(10.dp))
        Text(
            if (sessionsNeedingSetup > 0) "$sessionsNeedingSetup session(s) need program or in-charge" else "Sessions and HOD configured",
            color = if (sessionsNeedingSetup > 0 || !hasHod) CmsTheme.colors.accent else ModSuccess,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun SessionMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun DepartmentSessionCard(session: AcademicSession, studentCount: Int, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Column {
                Text("Session ${session.label}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${session.shift} · Semester ${session.currentSemester}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(8.dp))
            StatusBadge(session.shift.name, if (session.shift == Session.MORNING) BadgeTone.Navy else BadgeTone.Gold)
            Spacer(Modifier.height(8.dp))
            Text(session.programName?.takeIf { it.isNotBlank() } ?: "Program name not configured", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            Text(session.inchargeEmail?.takeIf { it.isNotBlank() } ?: "Session in-charge not assigned", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            Text("$studentCount / ${session.maxStudents} enrolled", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onClick) { Text("Open session") }
        }
    }
}

@Composable
private fun SessionEmptyState(filtered: Boolean, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching sessions" else "No sessions yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try another year, shift, or in-charge." else "Create the first intake to add students, curriculum, timetable, and fees.",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = if (filtered) "Clear search" else "Create session", onClick = onAction)
        }
    }
}

@Composable
private fun AddDepartmentSessionDialog(onDismiss: () -> Unit, onConfirm: (Int, Session) -> Unit) {
    var year by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf(Session.MORNING) }
    val parsedYear = year.trim().toIntOrNull()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create session", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Intake year") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("SHIFT", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Session.entries.forEach { option ->
                        CmsChip(option.name, selected = shift == option, onClick = { shift = option })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedYear?.let { onConfirm(it, shift) } }, enabled = parsedYear != null) { Text("Create session") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun EditDepartmentDetailsDialog(
    department: Department,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit,
) {
    var name by remember { mutableStateOf(department.name) }
    var code by remember { mutableStateOf(department.code) }
    var hodEmail by remember { mutableStateOf(department.hodEmail ?: "") }
    var description by remember { mutableStateOf(department.description ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Department", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Department name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = hodEmail, onValueChange = { hodEmail = it }, label = { Text("Head of department") }, placeholder = { Text("Assign a department HOD") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Add a short description to help administrators identify this program.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(name, code, hodEmail.ifBlank { null }, description.ifBlank { null }) }) { Text("Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
