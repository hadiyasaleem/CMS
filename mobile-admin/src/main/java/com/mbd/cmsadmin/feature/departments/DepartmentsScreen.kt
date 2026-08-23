package com.mbd.cmsadmin.feature.departments

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmsadmin.R
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.ui.components.CmsFab
import com.mbd.cmscommon.ui.components.CmsPrimaryButton
import com.mbd.cmscommon.ui.components.CmsTextField
import com.mbd.cmscommon.ui.components.CmsEntityOption
import com.mbd.cmscommon.ui.components.CmsEntityPicker
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.ui.components.ConfirmDestructiveActionDialog
import com.mbd.cmscommon.ui.components.DepartmentPortfolio
import com.mbd.cmscommon.ui.components.ErrorBanner
import com.mbd.cmscommon.ui.components.InlineErrorCard
import com.mbd.cmscommon.ui.components.SkeletonList
import com.mbd.cmscommon.util.Outcome

@Composable
fun DepartmentsScreen(onOpenDepartment: (String) -> Unit, viewModel: DepartmentsViewModel = hiltViewModel()) {
    val items by viewModel.listState.items.collectAsState()
    val stats by viewModel.departmentStats.collectAsState()
    val actionError by viewModel.actionError.collectAsState()
    val teachers by viewModel.teachers.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<Department?>(null) }
    var pendingDelete by remember { mutableStateOf<Department?>(null) }

    Scaffold(
        floatingActionButton = {
            CmsFab(onClick = { showAddDialog = true }, contentDescription = "Add department")
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = items) {
                is Outcome.Loading -> SkeletonList()
                is Outcome.Error -> ErrorBanner(state.message, onRetry = viewModel::refresh)
                is Outcome.Success -> Column(Modifier.fillMaxSize()) {
                    actionError?.let { message ->
                        InlineErrorCard(
                            message = message,
                            actionLabel = "Dismiss",
                            onAction = viewModel::clearActionError,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        )
                    }
                    DepartmentPortfolio(
                        departments = state.data,
                        stats = stats,
                        heroPainter = painterResource(R.drawable.departments_hero),
                        onOpenDepartment = onOpenDepartment,
                        onEditDepartment = { editingDepartment = it },
                        onDeleteDepartment = { pendingDelete = it },
                        onAddDepartment = { showAddDialog = true },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        DepartmentEditorDialog(
            department = null,
            teachers = teachers,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, code, hodEmail, description ->
                viewModel.createDepartment(name, code, hodEmail, description)
                showAddDialog = false
            },
        )
    }

    editingDepartment?.let { department ->
        DepartmentEditorDialog(
            department = department,
            teachers = teachers,
            onDismiss = { editingDepartment = null },
            onConfirm = { name, code, hodEmail, description ->
                viewModel.updateDepartment(department, name, code, hodEmail, description)
                editingDepartment = null
            },
        )
    }

    pendingDelete?.let { department ->
        ConfirmDestructiveActionDialog(
            title = "Delete ${department.name}?",
            dependentSummary = "This permanently deletes its sessions, curricula, timetables, attendance, marks, exam papers, and fees. Students and teachers are unassigned rather than deleted.",
            onConfirm = { viewModel.delete(department.deptId); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }
}

@Composable
private fun DepartmentEditorDialog(
    department: Department?,
    teachers: List<Teacher>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String?, String?) -> Unit,
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
        title = { Text(if (department == null) "Add department" else "Edit department", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    if (department == null) {
                        "Create the academic unit first; sessions, students, curriculum, and fees can then be added inside it."
                    } else {
                        "Update the department directory details. Existing sessions and student records keep the same department identity."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                CmsTextField(value = name, onValueChange = { name = it }, label = "Department name", placeholder = "Computer Science", isError = name.isNotBlank() && nameError != null, supportingText = nameError.takeIf { name.isNotBlank() })
                Spacer(Modifier.height(12.dp))
                CmsTextField(value = code, onValueChange = { code = it.uppercase().filter(Char::isLetterOrDigit).take(10) }, label = "Department code", placeholder = "CS", isError = code.isNotBlank() && codeError != null, supportingText = codeError.takeIf { code.isNotBlank() })
                Spacer(Modifier.height(12.dp))
                CmsEntityPicker(
                    label = "Head of department",
                    selectedId = hodEmail.ifBlank { null },
                    options = teachers.sortedBy { it.name }.map { CmsEntityOption(it.email, it.name, it.email) },
                    onSelected = { hodEmail = it.orEmpty() },
                    emptyLabel = "HOD not assigned",
                )
                Spacer(Modifier.height(12.dp))
                CmsTextField(value = description, onValueChange = { description = it.take(500) }, label = "Description (optional)")
            }
        },
        confirmButton = {
            CmsPrimaryButton(
                text = if (department == null) "Create department" else "Save changes",
                enabled = canCreate,
                onClick = {
                    onConfirm(name.trim(), code.trim(), hodEmail.trim().ifBlank { null }, description.trim().ifBlank { null })
                },
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
