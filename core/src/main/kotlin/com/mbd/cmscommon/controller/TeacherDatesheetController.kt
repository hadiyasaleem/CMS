package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** One of the teacher's own subjects with its published-datesheet slot. */
data class TeacherDatesheetRow(
    val sheet: Datesheet,
    val slot: DatesheetSlot,
    val sessionLabel: String,
)

/** The teacher's own subjects for one semester -- this screen's grid (there is one per semester
 * they currently teach in, never a department/shift pivot, since every row is already their own). */
data class TeacherDatesheetGrid(val semester: Int, val rows: List<TeacherDatesheetRow>) {
    val title: String get() = "Semester $semester"
}

/**
 * The teacher's "My Datesheet" screen: no manual filters, since a teacher only ever wants their own
 * exam schedule. Joins their timetable-driven assignments against published datesheet slots by
 * (sessionId, courseCode) -- the same join [ExamPaperSubmissionController] uses for "papers I can
 * submit" -- then groups into one grid per semester they currently teach in.
 */
class TeacherDatesheetController(
    private val datesheetRepository: DatesheetRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    val grids: StateFlow<List<TeacherDatesheetGrid>> = combine(
        datesheetRepository.observeDatesheets(),
        datesheetRepository.observeAllSlots(),
        assignmentsProvider.observeMyAssignments(),
    ) { datesheets, allSlots, assignments ->
        val published = datesheets.filter { it.published }.associateBy { it.id }
        val myKeys = assignments.map { it.sessionId.trim().lowercase() to it.courseCode.trim().uppercase() }.toSet()
        val labelBySessionId = assignments.associateBy({ it.sessionId }, { it.sessionLabel })
        allSlots
            .mapNotNull { slot ->
                val sheet = published[slot.datesheetId] ?: return@mapNotNull null
                val key = sheet.sessionId.trim().lowercase() to slot.courseCode.trim().uppercase()
                if (key !in myKeys) return@mapNotNull null
                TeacherDatesheetRow(sheet, slot, labelBySessionId[sheet.sessionId] ?: sheet.sessionId)
            }
            .groupBy { it.sheet.semester }
            .map { (semester, rows) -> TeacherDatesheetGrid(semester, rows.sortedWith(compareBy({ it.sessionLabel }, { it.slot.courseCode }))) }
            .sortedBy { it.semester }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refresh()
    }

    fun refresh() = launch("load your datesheet") {
        _loading.value = true
        try {
            clearError()
            datesheetRepository.sync()
            datesheetRepository.syncAllSlots()
        } finally {
            _loading.value = false
        }
    }
}
