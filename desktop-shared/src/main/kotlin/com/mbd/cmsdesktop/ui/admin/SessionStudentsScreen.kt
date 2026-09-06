package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.controller.SessionStudentsController
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.ui.components.StudentRosterWorkspace
import com.mbd.cmscommon.util.StudentImportParser
import com.mbd.cmscommon.util.StudentImportResult
import com.mbd.cmscommon.util.userMessage
import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices

@Composable
fun SessionStudentsScreen(
    sessionId: String,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    window: ComposeWindow,
    onOpenStudent: (sessionId: String, roll: String) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, sessionRepository) {
        SessionStudentsController(sessionId, sessionRepository, departmentRepository, scope)
    }
    val students by controller.students.collectAsState()
    val session by controller.session.collectAsState()
    val controllerError by controller.error.collectAsState()
    val importResult by controller.importResult.collectAsState()
    val importing by controller.importing.collectAsState()

    var importPreview by remember { mutableStateOf<StudentImportResult?>(null) }
    var fileError by remember { mutableStateOf<String?>(null) }

    StudentRosterWorkspace(
        session = session,
        students = students,
        importing = importing,
        importPreview = importPreview,
        importResult = importResult,
        errorMessage = fileError ?: controllerError,
        onOpenStudent = { onOpenStudent(sessionId, it.rollNumber) },
        onAddStudent = { roll, name -> controller.addStudent(roll, name, null, null) },
        onDeleteStudent = { controller.deleteStudent(it.id) },
        onPickImportFile = {
            val file = AwtDesktopPlatformServices.pickFile(window, "Choose a CSV or Excel roster file")
            if (file != null) {
                try {
                    val bytes = file.readBytes()
                    importPreview = if (isZipFile(bytes)) {
                        StudentImportParser.parseXlsx(bytes)
                    } else {
                        StudentImportParser.parseCsv(String(bytes, Charsets.UTF_8))
                    }
                } catch (t: Throwable) {
                    fileError = t.userMessage("Couldn't read this file.")
                }
            }
        },
        onConfirmImport = { rows ->
            controller.importStudents(rows)
            importPreview = null
        },
        onDismissImportPreview = { importPreview = null },
        onDismissImportResult = controller::clearImportResult,
        onClearError = {
            if (fileError != null) fileError = null else controller.clearError()
        },
    )
}

private fun isZipFile(bytes: ByteArray): Boolean =
    bytes.size >= 2 && bytes[0] == 80.toByte() && bytes[1] == 75.toByte()
