package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.Department
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
 * Cross-datesheet browsing: the department/intake-year/shift cascade (same shape as
 * MasterTimetableController). Grid views (grouped, filtered, calendar, semester) are all built
 * from [datesheets] in the UI layer. There is no manual semester picker -- a datesheet is always
 * created for whichever semester [resolvedSession] is currently in, the same way
 * SessionTimetableController derives subjects from the session's own currentSemester rather than
 * letting the caller pick one.
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

    private val _selectedDeptId = MutableStateFlow<String?>(null)
    val selectedDeptId: StateFlow<String?> = _selectedDeptId.asStateFlow()

    private val _selectedStartYear = MutableStateFlow<Int?>(null)
    val selectedStartYear: StateFlow<Int?> = _selectedStartYear.asStateFlow()

    private val _selectedShift = MutableStateFlow<Session?>(null)
    val selectedShift: StateFlow<Session?> = _selectedShift.asStateFlow()

    val sessionsInDepartment: StateFlow<List<AcademicSession>> = combine(sessions, _selectedDeptId) { all, deptId ->
        if (deptId == null) emptyList() else all.filter { it.deptId == deptId }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val shiftsForSelection: StateFlow<List<Session>> = combine(sessionsInDepartment, _selectedStartYear) { inDept, year ->
        if (year == null) emptyList() else inDept.filter { it.startYear == year }.map { it.shift }.distinct()
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val resolvedSession: StateFlow<AcademicSession?> =
        combine(sessions, _selectedDeptId, _selectedStartYear, _selectedShift) { all, deptId, year, shift ->
            if (deptId == null || year == null || shift == null) {
                null
            } else {
                all.firstOrNull { it.deptId == deptId && it.startYear == year && it.shift == shift }
            }
        }.stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    fun selectDepartment(deptId: String?) {
        _selectedDeptId.value = deptId
        _selectedStartYear.value = null
        _selectedShift.value = null
    }

    fun selectStartYear(year: Int?) {
        _selectedStartYear.value = year
        _selectedShift.value = null
    }

    fun selectShift(shift: Session?) {
        _selectedShift.value = shift
    }

    fun refresh() = launch {
        clearError()
        datesheetRepository.sync()
        datesheetRepository.syncAllSlots()
    }

    /** Creates the datesheet shell for (session, semester); DatesheetEditorController prefills its papers once mounted. */
    suspend fun createDatesheet(sessionId: String, semester: Int, defaultStartTime: String?, defaultEndTime: String?, defaultBuildingId: String?, instructions: String?, createdBy: String): String {
        val session = sessions.value.firstOrNull { it.sessionId == sessionId }
        requireValid(session?.isActive == true) { "This session has graduated and can no longer have new datesheets created for it." }
        val draft = DatesheetDraft(sessionId, semester, defaultStartTime, defaultEndTime, defaultBuildingId, instructions, published = false)
        validationMessage(draft).orThrowValidation()
        return datesheetRepository.createDatesheet(draft, createdBy)
    }
}
