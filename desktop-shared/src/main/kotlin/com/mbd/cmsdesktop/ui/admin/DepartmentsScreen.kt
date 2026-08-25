package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.DepartmentPortfolioStats
import com.mbd.cmscommon.controller.DepartmentsActionController
import com.mbd.cmscommon.controller.departmentPortfolioStats
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.ui.components.DepartmentPortfolio
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

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
    var loading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<Department?>(null) }
    var pendingDelete by remember { mutableStateOf<Department?>(null) }

    val actionController = remember(repository, createdBy) {
        DepartmentsActionController(repository, createdBy.orEmpty(), scope)
    }
    val actionError by actionController.error.collectAsState()

    LaunchedEffect(repository, sessionRepository) {
        loading = true
        try {
            repository.sync()
        } catch (t: Throwable) {
            errorMessage = t.userMessage("Could not load departments.")
        } finally {
            loading = false
        }
    }

    Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
        DepartmentPortfolio(
            departments = departments,
            stats = departmentStats,
            heroPainter = painterResource("departments-hero.png"),
            onOpenDepartment = onOpenDepartment,
            onEditDepartment = { editingDepartment = it },
            onDeleteDepartment = { pendingDelete = it },
            onAddDepartment = { showAddDialog = true },
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
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Delete ${department.name}?") },
            text = { Text("This removes the department. Sessions and records under it are not deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { actionController.delete(department.deptId) }
                    pendingDelete = null
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text("Cancel") } },
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
    var name by remember(department) { mutableStateOf(department?.name.orEmpty()) }
    var code by remember(department) { mutableStateOf(department?.code.orEmpty()) }
    var hodEmail by remember(department) { mutableStateOf(department?.hodEmail.orEmpty()) }
    var description by remember(department) { mutableStateOf(department?.description.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (department == null) "Add department" else "Edit department") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Code") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = hodEmail,
                    onValueChange = { hodEmail = it },
                    label = { Text("Head of department email (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirm(name, code, hodEmail.ifBlank { null }, description.ifBlank { null })
            }) { Text(if (department == null) "Create" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
