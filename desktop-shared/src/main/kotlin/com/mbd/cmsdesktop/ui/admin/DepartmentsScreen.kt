package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.DepartmentPortfolioStats
import com.mbd.cmscommon.controller.DepartmentsActionController
import com.mbd.cmscommon.controller.departmentPortfolioStats
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.CmsEntityOption
import com.mbd.cmscommon.ui.components.CmsEntityPicker
import com.mbd.cmscommon.ui.components.CmsFab
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.ConfirmDestructiveActionDialog
import com.mbd.cmscommon.ui.components.DepartmentPortfolio
import com.mbd.cmscommon.ui.components.ErrorBanner
import com.mbd.cmscommon.ui.components.InlineErrorCard
import com.mbd.cmscommon.ui.components.SkeletonList
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.userMessage
import java.util.Locale
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Department directory: a searchable portfolio of department cards (see [DepartmentPortfolio])
 * with per-department session/student rollups, plus create/edit/delete flows. A persistent
 * [CmsFab] pinned to the bottom-right always opens the add-department dialog, in addition to
 * the portfolio's own empty-state add affordance.
 */
@Composable
fun DepartmentsScreen(
    repository: DepartmentRepository,
    sessionRepository: AcademicSessionRepository,
    teacherRepository: TeacherRepository,
    createdBy: String?,
    onOpenDepartment: (String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val teachers by teacherRepository.observeActiveTeachers().collectAsState(initial = emptyList())
    val departments by repository.observeActiveDepartments().collectAsState(initial = emptyList())
    val sessions by sessionRepository.observeAllSessions().collectAsState(initial = emptyList())

    val statsFlow = remember(sessions) {
        if (sessions.isEmpty()) {
            flowOf(emptyMap())
        } else {
            combine(sessions.map { s -> sessionRepository.observeStudents(s.sessionId).map { s to it.size } }) { pairs ->
                departmentPortfolioStats(pairs.toList())
            }
        }
    }
    val departmentStats by statsFlow.collectAsState(initial = emptyMap<String, DepartmentPortfolioStats>())

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<Department?>(null) }
    var pendingDelete by remember { mutableStateOf<Department?>(null) }

    val actionController = remember(repository, createdBy) {
        DepartmentsActionController(repository, createdBy.orEmpty(), scope)
    }
    val actionError by actionController.error.collectAsState()

    // Full resync: departments, then every department's sessions, then every session's
    // students (needed for the per-department student-count rollups shown on each card).
    suspend fun refresh() {
        loading = true
        errorMessage = null
        try {
            repository.sync()
            repository.observeActiveDepartments().first().forEach { dept ->
                sessionRepository.syncSessionsForDept(dept.deptId)
            }
            sessionRepository.observeAllSessions().first().forEach { session ->
                sessionRepository.syncStudents(session.sessionId)
            }
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load departments.")
        } finally {
            loading = false
        }
    }

    Box(Modifier.fillMaxSize()) {
        when {
            loading -> SkeletonList()
            errorMessage != null -> ErrorBanner(errorMessage!!, onRetry = { scope.launch { refresh() } })
            else -> Column(Modifier.fillMaxSize()) {
                actionError?.let { message ->
                    InlineErrorCard(
                        message = message,
                        actionLabel = "Dismiss",
                        onAction = actionController::clearError,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    )
                }
                DepartmentPortfolio(
                    departments = departments,
                    stats = departmentStats,
                    heroPainter = painterResource("departments-hero.png"),
                    onOpenDepartment = onOpenDepartment,
                    onEditDepartment = { editingDepartment = it },
                    onDeleteDepartment = { pendingDelete = it },
                    onAddDepartment = { showAddDialog = true },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        CmsFab(
            onClick = { showAddDialog = true },
            contentDescription = "Add department",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAddDialog) {
        DesktopDepartmentEditorDialog(
            department = null,
            teachers = teachers,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, code, hodEmail, description ->
                scope.launch { actionController.create(name, code, hodEmail, description) }
                showAddDialog = false
            },
        )
    }

    editingDepartment?.let { department ->
        DesktopDepartmentEditorDialog(
            department = department,
            teachers = teachers,
            onDismiss = { editingDepartment = null },
            onConfirm = { name, code, hodEmail, description ->
                scope.launch { actionController.update(department, name, code, hodEmail, description) }
                editingDepartment = null
            },
        )
    }

    pendingDelete?.let { department ->
        ConfirmDestructiveActionDialog(
            title = "Delete ${department.name}?",
            dependentSummary = "This permanently deletes its sessions, curricula, timetables, attendance, marks, exam papers, and fees. " +
                "Students and teachers are unassigned rather than deleted.",
            onConfirm = {
                scope.launch { actionController.delete(department.deptId) }
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun DesktopDepartmentEditorDialog(
    department: Department?,
    teachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, code: String, hodEmail: String?, description: String?) -> Unit,
) {
    var name by remember(department?.deptId) { mutableStateOf(department?.name.orEmpty()) }
    var code by remember(department?.deptId) { mutableStateOf(department?.code.orEmpty()) }
    var hodEmail by remember(department?.deptId) { mutableStateOf(department?.hodEmail.orEmpty()) }
    var description by remember(department?.deptId) { mutableStateOf(department?.description.orEmpty()) }

    val nameError = FieldValidators.nameError(name, "Department name")
    val codeError = FieldValidators.departmentCodeError(code)
    val canCreate = nameError == null && codeError == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (department == null) "Add department" else "Edit department") },
        text = {
            Column {
                Text(
                    text = if (department == null) {
                        "Create the academic unit first; sessions, students, curriculum, and fees can then be added inside it."
                    } else {
                        "Update the department directory details. Existing sessions and student records keep the same department identity."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                CmsTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Department name",
                    placeholder = "Computer Science",
                    isError = name.isNotBlank() && nameError != null,
                    supportingText = if (name.isNotBlank()) nameError else null,
                )
                Spacer(Modifier.height(12.dp))
                CmsTextField(
                    value = code,
                    onValueChange = { input -> code = input.uppercase(Locale.ROOT).filter(Char::isLetterOrDigit).take(10) },
                    label = "Department code",
                    placeholder = "CS",
                    isError = code.isNotBlank() && codeError != null,
                    supportingText = if (code.isNotBlank()) codeError else null,
                )
                Spacer(Modifier.height(12.dp))
                CmsEntityPicker(
                    label = "Head of department",
                    selectedId = hodEmail.takeIf { it.isNotBlank() },
                    options = teachers.sortedBy { it.name }.map { CmsEntityOption(it.email, it.name, it.email) },
                    onSelected = { selected -> hodEmail = selected ?: "" },
                    optional = true,
                    emptyLabel = "HOD not assigned",
                )
                Spacer(Modifier.height(12.dp))
                CmsTextField(
                    value = description,
                    onValueChange = { description = it.take(500) },
                    label = "Description (optional)",
                )
            }
        },
        confirmButton = {
            CmsPrimaryButton(
                text = if (department == null) "Create department" else "Save changes",
                onClick = {
                    onConfirm(name.trim(), code.trim(), hodEmail.trim().ifBlank { null }, description.trim().ifBlank { null })
                },
                enabled = canCreate,
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
