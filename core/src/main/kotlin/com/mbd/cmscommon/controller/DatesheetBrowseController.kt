package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.validationMessage
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Cross-datesheet browsing: the shared Department/Semester/Shift/Program filter (same shape as
 * MasterTimetableController's). Grid views (grouped, filtered, calendar, semester) are all built
 * from [datesheets] in the UI layer. There is no manual semester picker for CREATING a datesheet --
 * it is always created for whichever semester [resolvedSession] is currently in, the same way
 * SessionTimetableController derives subjects from the session's own currentSemester rather than
 * letting the caller pick one; the filter's own "Semester" dropdown is what resolves which batch.
 */
class DatesheetBrowseController(
    private val datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    /** Graduated sessions are excluded here -- there is no legitimate reason to create or browse
     * Mid Term datesheets for a batch that has already finished, and this is the single source
     * every filter/picker/grouping in the datesheet UI is built from. */
    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().map { it.filter(AcademicSession::isActive) }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val datesheets: StateFlow<List<Datesheet>> =
        datesheetRepository.observeDatesheets().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _filterScope = MutableStateFlow(DeptSemesterScope.ALL)
    val filterScope: StateFlow<DeptSemesterScope> = _filterScope.asStateFlow()

    val availableSemesters: StateFlow<List<Int>> = sessions.map { it.availableSemesters() }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** The one batch the filter resolves to -- only once department, semester and shift (and, if
     * ambiguous, program type) narrow it down to exactly one; a datesheet is per shift, so shift is
     * required here even though [DeptSemesterScope.resolveSession] itself doesn't demand it. */
    val resolvedSession: StateFlow<AcademicSession?> = combine(sessions, _filterScope) { all, scope ->
        if (scope.shift == null) null else scope.resolveSession(all)
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    fun setFilterScope(scope: DeptSemesterScope) {
        _filterScope.value = scope
    }

    fun refresh() = launch("refresh the datesheets") {
        clearError()
        datesheetRepository.sync()
        datesheetRepository.syncAllSlots()
    }

    /** Creates the datesheet shell for (session, semester); DatesheetEditorController prefills its papers once mounted. */
    suspend fun createDatesheet(
        sessionId: String,
        semester: Int,
        defaultStartTime: String?,
        defaultEndTime: String?,
        defaultBuildingId: String?,
        defaultRoomId: String?,
        instructions: String?,
        createdBy: String,
        shift: Session? = null,
    ): String {
        val session = sessions.value.firstOrNull { it.sessionId == sessionId }
        requireValid(session?.isActive == true) { "This session has graduated and can no longer have new datesheets created for it." }
        // Datesheets are per shift; default to the shift chosen in the browse filters.
        val sheetShift = shift ?: _filterScope.value.shift?.takeIf { session?.runs(it) == true } ?: session?.shifts?.firstOrNull() ?: Session.MORNING
        val draft = DatesheetDraft(sessionId, sheetShift, semester, defaultStartTime, defaultEndTime, defaultBuildingId, defaultRoomId, instructions, published = false)
        validationMessage(draft).orThrowValidation()
        return datesheetRepository.createDatesheet(draft, createdBy)
    }
}
