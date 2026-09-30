package com.mbd.cmscommon.ui.components

import androidx.compose.material3.Checkbox
import com.mbd.cmscommon.controller.capacityError
import com.mbd.cmscommon.controller.capacityForShiftSelection
import com.mbd.cmscommon.controller.createSessionError
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.style.TextOverflow
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
import java.util.Locale

@Composable
fun DepartmentDetailWorkspace(
    department: Department?,
    fallbackName: String,
    sessions: List<AcademicSession>,
    studentCounts: Map<String, Int>,
    teachers: List<Teacher>,
    errorMessage: String?,
    actionMessage: String?,
    onOpenSession: (String) -> Unit,
    onCreateSession: (Int, Set<Session>, Int, ProgramType, String?) -> Unit,
    onUpdateDepartment: (String, String, String?, String?) -> Unit,
    onClearError: () -> Unit,
    onConsumeNotice: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var shiftFilter by remember { mutableStateOf<Session?>(null) }
    var showAddSession by remember { mutableStateOf(false) }
    var showEditDepartment by remember { mutableStateOf(false) }
    var showGraduated by remember { mutableStateOf(false) }

    val snapshot = departmentDetailSnapshot(sessions, studentCounts)
    fun List<AcademicSession>.matchingQuery() = filter { it.runs(shiftFilter ?: return@filter true) }
        .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) || (it.programName ?: "").contains(query, ignoreCase = true) }
        .sortedByDescending { it.startYear }
    val filtered = snapshot.sessions.matchingQuery()
    val graduatedSessions = sessions.filter { !it.isActive }.matchingQuery()

    Box(modifier.fillMaxSize()) {
        CardGrid(Modifier.fillMaxWidth()) {
            fullSpanItem {
                DepartmentIdentityCard(
                    department = department,
                    fallbackName = fallbackName,
                    hasHod = !department?.hodEmail.isNullOrBlank(),
                    onEdit = { showEditDepartment = true },
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                fullSpanItem { CmsNotice(errorMessage, tone = NoticeTone.Error, onDismiss = onClearError) }
            }
            if (!actionMessage.isNullOrBlank()) {
                fullSpanItem { CmsNotice(actionMessage, tone = NoticeTone.Success, onDismiss = onConsumeNotice) }
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
                Text("Current intakes", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CmsChip("All shifts", selected = shiftFilter == null, onClick = { shiftFilter = null })
                        Session.entries.forEach { shift ->
                            CmsChip(shift.label, selected = shiftFilter == shift, onClick = { shiftFilter = shift })
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

            if (graduatedSessions.isNotEmpty()) {
                fullSpanItem {
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable { showGraduated = !showGraduated },
                        shape = RoundedCornerShape(14.dp),
                        color = ModSurface,
                        border = BorderStroke(1.dp, ModTrack),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Graduated sessions", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "${graduatedSessions.size} session(s) no longer active -- tap to ${if (showGraduated) "hide" else "view"}.",
                                    color = ModMuted,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                if (showGraduated) {
                    items(graduatedSessions, key = { "graduated_${it.sessionId}" }) { session ->
                        DepartmentSessionCard(session, studentCounts[session.sessionId] ?: 0, onClick = { onOpenSession(session.sessionId) })
                    }
                }
            }

            fullSpanItem { Spacer(Modifier.height(72.dp)) }
        }
        CmsFab(
            onClick = { showAddSession = true },
            contentDescription = "Create session",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAddSession) {
        AddDepartmentSessionDialog(
            existing = sessions,
            onDismiss = { showAddSession = false },
            onConfirm = { year, shifts, capacity, programType, copyFrom -> onCreateSession(year, shifts, capacity, programType, copyFrom); showAddSession = false },
        )
    }

    if (showEditDepartment && department != null) {
        EditDepartmentDetailsDialog(
            department = department,
            teachers = teachers,
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
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Column(Modifier.padding(16.dp)) {
            Column {
                Text("Session ${session.label}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${session.programType.label} · ${session.shiftMode.label} · Semester ${session.currentSemester}",
                    color = ModMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusBadge(
                    session.shiftMode.label.uppercase(),
                    when (session.shiftMode) {
                        ShiftMode.MORNING -> BadgeTone.Navy
                        ShiftMode.EVENING -> BadgeTone.Gold
                        ShiftMode.BOTH -> BadgeTone.Neutral
                    },
                )
                if (!session.isActive) {
                    StatusBadge("GRADUATED", BadgeTone.Neutral)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                session.programName?.takeIf { it.isNotBlank() } ?: "Program name not configured",
                color = ModMuted,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                session.inchargeEmail?.takeIf { it.isNotBlank() } ?: "Session in-charge not assigned",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Text("$studentCount / ${session.maxStudents} enrolled", color = ModMuted, style = MaterialTheme.typography.bodySmall)
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
private fun AddDepartmentSessionDialog(existing: List<AcademicSession>, onDismiss: () -> Unit, onConfirm: (Int, Set<Session>, Int, ProgramType, String?) -> Unit) {
    var year by remember { mutableStateOf<Int?>(null) }
    var shifts by remember { mutableStateOf(setOf(Session.MORNING)) }
    var capacity by remember { mutableStateOf(AcademicSession.defaultMaxStudents(ShiftMode.MORNING).toString()) }
    var programType by remember { mutableStateOf(ProgramType.BS) }
    var copyFromSessionId by remember { mutableStateOf<String?>(null) }
    // Years already taken by a session of the SAME program type -- a BS and an MA-Replacement
    // session can share an intake year in the same department.
    val takenYears = existing.filter { it.programType == programType }.map { it.startYear }.toSet()
    val error = createSessionError(year, shifts, capacity, existing, programType)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create session", style = MaterialTheme.typography.headlineSmall) },
        text = { DialogScrollBody {
            Column {
                Text("PROGRAM", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ProgramType.entries.forEach { type ->
                        CmsChip(type.label, selected = programType == type, onClick = { programType = type; year = null })
                    }
                }
                Spacer(Modifier.height(10.dp))
                // One session per intake year and program type: years already taken by this program
                // type are not offered.
                CmsEntityPicker(
                    label = "Intake year",
                    selectedId = year?.toString(),
                    options = intakeYearOptions().filterNot { it in takenYears }.map { CmsEntityOption(it.toString(), "$it–${programType.endYear(it)}") },
                    onSelected = { year = it?.toIntOrNull() },
                    emptyLabel = "Select intake year",
                )
                Spacer(Modifier.height(10.dp))
                SessionShiftFields(
                    shifts = shifts,
                    onShiftsChange = { picked -> shifts = picked; capacity = capacityForShiftSelection(picked, capacity) },
                    capacity = capacity,
                    onCapacityChange = { capacity = it },
                    capacityMessage = capacityError(capacity),
                )
                if (error != null && year != null && capacityError(capacity) == null) {
                    Spacer(Modifier.height(6.dp))
                    Text(error, color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                }
                if (existing.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    Text("SPEED UP SETUP", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(4.dp))
                    CmsEntityPicker(
                        label = "Copy subjects from (optional)",
                        selectedId = copyFromSessionId,
                        options = existing.sortedByDescending { it.startYear }.map { CmsEntityOption(it.sessionId, "${it.label} · ${it.shiftMode.label}") },
                        onSelected = { copyFromSessionId = it },
                        optional = true,
                        emptyLabel = "Don't copy",
                    )
                    Text(
                        "Links every semester's subjects from the picked session into this new one, so you don't have to add them again.",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }},
        confirmButton = {
            TextButton(
                onClick = { year?.let { y -> capacity.toIntOrNull()?.let { onConfirm(y, shifts, it, programType, copyFromSessionId) } } },
                enabled = error == null,
            ) { Text("Create session") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

/**
 * Morning/Evening checkboxes plus the max-students field. Ticking or unticking a shift re-fills the
 * capacity with its default (50 for one shift, 100 for both); the admin can still type another value.
 */
@Composable
internal fun SessionShiftFields(
    shifts: Set<Session>,
    onShiftsChange: (Set<Session>) -> Unit,
    capacity: String,
    onCapacityChange: (String) -> Unit,
    capacityMessage: String?,
) {
    Text("SHIFTS", color = ModMuted, style = CmsTextStyles.eyebrow)
    Spacer(Modifier.height(4.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Session.entries.forEach { shift ->
            Row(
                modifier = Modifier.clickable { onShiftsChange(if (shift in shifts) shifts - shift else shifts + shift) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(checked = shift in shifts, onCheckedChange = { checked -> onShiftsChange(if (checked) shifts + shift else shifts - shift) })
                Text(shift.label, modifier = Modifier.padding(end = 12.dp))
            }
        }
    }
    Text(
        if (shifts.size == 2) "One session serves both shifts; subjects and term dates are shared." else "Tick both to run Morning and Evening in this one session.",
        color = ModMuted,
        style = MaterialTheme.typography.bodySmall,
    )
    Spacer(Modifier.height(10.dp))
    OutlinedTextField(
        value = capacity,
        onValueChange = { onCapacityChange(it.filter(Char::isDigit).take(3)) },
        label = { Text("Max students") },
        supportingText = { Text(capacityMessage ?: "Filled in from the shifts you tick (50 per shift); you can change it.") },
        isError = capacityMessage != null,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
    )
}

/** Full 4-digit intake years to choose from, most recent first -- next year (pre-registration) down to 15 years back. */
private fun intakeYearOptions(): List<Int> {
    val currentYear = java.time.Year.now().value
    return (currentYear + 1 downTo currentYear - 15).toList()
}

@Composable
private fun EditDepartmentDetailsDialog(
    department: Department,
    teachers: List<Teacher>,
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
        text = { DialogScrollBody {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Department name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Head of department",
                    selectedId = hodEmail.ifBlank { null },
                    options = teachers.sortedBy { it.name }.map { CmsEntityOption(it.email, it.name, it.email) },
                    onSelected = { hodEmail = it.orEmpty() },
                    emptyLabel = "HOD not assigned",
                )
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
        }},
        confirmButton = {
            TextButton(onClick = { onConfirm(name, code, hodEmail.ifBlank { null }, description.ifBlank { null }) }) { Text("Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
