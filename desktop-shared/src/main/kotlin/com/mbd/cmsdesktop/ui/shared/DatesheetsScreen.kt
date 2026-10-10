package com.mbd.cmsdesktop.ui.shared

import com.mbd.cmscommon.util.rethrowCancellation
import com.mbd.cmscommon.util.FailureSummary
import com.mbd.cmscommon.controller.observeShiftOf
import com.mbd.cmscommon.controller.studentDatesheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.controller.TeacherDatesheetController
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.ui.components.StudentDatesheetWorkspace
import com.mbd.cmscommon.ui.components.TeacherDatesheetWorkspace
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** The teacher's own read-only "My Datesheet" screen: no filters, pre-loaded with every subject
 * they teach, one grid per semester they currently teach in. */
@Composable
fun DatesheetsScreen(
    datesheetRepository: DatesheetRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    identityKey: String?,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(datesheetRepository) {
        TeacherDatesheetController(datesheetRepository, assignmentsProvider, scope)
    }

    val grids by controller.grids.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()

    TeacherDatesheetWorkspace(
        grids = grids,
        identityKey = identityKey,
        loading = loading,
        errorMessage = error,
        onRetry = controller::refresh,
    )
}

/** Read-only single-datesheet view for the student role: their own session's current semester's papers. */
@Composable
fun StudentDatesheetsScreen(
    sessionId: String,
    rollNumber: String,
    datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
) {
    val session by sessionRepository.observeSession(sessionId).collectAsState(initial = null)
    val semester = session?.currentSemester
    val shift by remember(sessionId, rollNumber) { sessionRepository.observeShiftOf(sessionId, rollNumber) }.collectAsState(initial = null)
    // Datesheets are per shift: the student's own shift's published sheet only.
    val sheet by remember(sessionId, semester, shift) {
        datesheetRepository.observeDatesheets().map { sheets -> studentDatesheet(sheets, sessionId, semester, shift) }
    }.collectAsState(initial = null as Datesheet?)
    val allSlots by datesheetRepository.observeAllSlots().collectAsState(initial = emptyList())
    val slots = sheet?.let { s -> allSlots.filter { it.datesheetId == s.id } }.orEmpty()

    var syncError by remember { mutableStateOf<String?>(null) }
    var syncing by remember { mutableStateOf(false) }
    val syncScope = rememberCoroutineScope()
    suspend fun sync() {
        syncing = true
        val result = runCatching { datesheetRepository.sync(); datesheetRepository.syncAllSlots() }.rethrowCancellation()
        syncError = FailureSummary.describe(FailureSummary.of(listOf("datesheets" to result)), "StudentDatesheetsScreen")
        syncing = false
    }
    LaunchedEffect(datesheetRepository) { sync() }

    StudentDatesheetWorkspace(
        sheet = sheet,
        session = session,
        slots = slots,
        loading = syncing && sheet == null,
        errorMessage = syncError,
        onRetry = { syncScope.launch { sync() } },
    )
}
