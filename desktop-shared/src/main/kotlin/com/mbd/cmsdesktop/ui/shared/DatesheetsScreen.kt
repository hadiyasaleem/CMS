package com.mbd.cmsdesktop.ui.shared

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mbd.cmscommon.controller.DatesheetsController
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.ui.components.DatesheetWorkspace
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Self-contained Datesheets screen shared by every desktop role: builds its own
 * [DatesheetsController], resolves the session list for the create/edit forms, and lazily loads
 * per-session subjects (used to populate the exam-paper subject picker) on demand instead of
 * eagerly fetching every session's curriculum up front. The nav host only needs to supply the
 * [viewer] context (which role is looking, and whether they can manage the schedule) and the
 * invigilator roster.
 */
@Composable
fun DatesheetsScreen(
    repository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    curriculumRepository: CurriculumRepository,
    viewer: DatesheetViewerContext,
    invigilators: List<Teacher> = emptyList(),
    createdBy: String? = null,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, createdBy) { DatesheetsController(repository, createdBy.orEmpty(), scope) }

    val sheets by controller.sheets.collectAsState()
    val slots by controller.slots.collectAsState()
    val loadingSlots by controller.loadingSlots.collectAsState()
    val refreshing by controller.refreshing.collectAsState()
    val busy by controller.busy.collectAsState()
    val actionMessage by controller.actionMessage.collectAsState()

    var sessions by remember { mutableStateOf<List<AcademicSession>>(emptyList()) }
    val subjectsBySession = remember { mutableStateMapOf<String, List<SemesterSubject>>() }

    androidx.compose.runtime.LaunchedEffect(sessionRepository) {
        sessionRepository.observeAllSessions().collect { sessions = it }
    }

    DatesheetWorkspace(
        datesheets = sheets.orEmpty(),
        slots = slots,
        loadingSlots = loadingSlots,
        sessions = sessions,
        subjectsBySession = subjectsBySession,
        invigilators = invigilators,
        viewer = viewer,
        loading = refreshing,
        busy = busy,
        errorMessage = null,
        actionMessage = actionMessage,
        onRetry = controller::refresh,
        onLoadSlots = controller::loadSlots,
        onLoadSubjects = { sessionId ->
            if (!subjectsBySession.containsKey(sessionId)) {
                scope.launch {
                    val subjects = runCatching { curriculumRepository.observeSessionSubjects(sessionId).first() }.getOrDefault(emptyList())
                    subjectsBySession[sessionId] = subjects
                }
            }
        },
        onCreate = controller::createDatesheet,
        onUpdate = controller::updateDatesheet,
        onSetPublished = controller::setPublished,
        onDelete = controller::deleteDatesheet,
        onAddSlot = controller::addSlot,
        onUpdateSlot = controller::updateSlot,
        onDeleteSlot = controller::deleteSlot,
    )
}
