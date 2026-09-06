package com.mbd.cmsadmin.feature.academics

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.SessionStudentsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.StudentRosterWorkspace
import com.mbd.cmscommon.util.ImportedStudentRow
import com.mbd.cmscommon.util.StudentImportParser
import com.mbd.cmscommon.util.StudentImportResult
import com.mbd.cmscommon.util.userMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private val ROSTER_FILE_MIME_TYPES = arrayOf(
    "text/csv",
    "text/comma-separated-values",
    "application/vnd.ms-excel",
    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
    "application/octet-stream",
)

@HiltViewModel
class SessionStudentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    private val controller = SessionStudentsController(
        sessionId = checkNotNull(savedStateHandle["sessionId"]),
        repo = sessionRepository,
        departmentRepo = departmentRepository,
        scope = viewModelScope,
    )

    val sessionId = controller.sessionId
    val students = controller.students
    val session = controller.session
    val departmentCode = controller.departmentCode
    val error = controller.error
    val importResult = controller.importResult
    val importing = controller.importing

    fun addStudent(rollNumber: String, name: String) = controller.addStudent(rollNumber, name, null, null)
    fun importStudents(rows: List<ImportedStudentRow>) = controller.importStudents(rows)
    fun clearImportResult() = controller.clearImportResult()
    fun deleteStudent(studentId: String) = controller.deleteStudent(studentId)
    fun clearError() = controller.clearError()
}

@Composable
fun SessionStudentsScreen(
    onOpenStudent: (sessionId: String, roll: String) -> Unit = { _, _ -> },
    viewModel: SessionStudentsViewModel = hiltViewModel(),
) {
    val students by viewModel.students.collectAsState()
    val session by viewModel.session.collectAsState()
    val departmentCode by viewModel.departmentCode.collectAsState()
    val controllerError by viewModel.error.collectAsState()
    val importResult by viewModel.importResult.collectAsState()
    val importing by viewModel.importing.collectAsState()
    var importPreview by remember { mutableStateOf<StudentImportResult?>(null) }
    var fileError by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Couldn't read the selected file.")
            importPreview = if (bytes.isZipFile()) {
                StudentImportParser.parseXlsx(bytes)
            } else {
                StudentImportParser.parseCsv(String(bytes, Charsets.UTF_8))
            }
        } catch (t: Throwable) {
            fileError = t.userMessage("Couldn't read this file.")
        }
    }

    StudentRosterWorkspace(
        session = session,
        departmentCode = departmentCode,
        students = students,
        importing = importing,
        importPreview = importPreview,
        importResult = importResult,
        errorMessage = fileError ?: controllerError,
        onOpenStudent = { onOpenStudent(viewModel.sessionId, it.rollNumber) },
        onAddStudent = viewModel::addStudent,
        onDeleteStudent = { viewModel.deleteStudent(it.id) },
        onPickImportFile = { importLauncher.launch(ROSTER_FILE_MIME_TYPES) },
        onConfirmImport = viewModel::importStudents,
        onDismissImportPreview = { importPreview = null },
        onDismissImportResult = viewModel::clearImportResult,
        onClearError = {
            if (fileError != null) fileError = null else viewModel.clearError()
        },
    )
}

private fun ByteArray.isZipFile(): Boolean = size >= 2 && this[0] == 0x50.toByte() && this[1] == 0x4B.toByte()
