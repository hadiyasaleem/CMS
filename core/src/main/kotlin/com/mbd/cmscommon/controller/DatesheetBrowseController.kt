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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Cross-datesheet browsing: the department/intake-year/shift cascade (same shape as
 * MasterTimetableController) plus a semester filter, and the datesheet list those narrow down to.
 * Grid views (grouped, filtered, calendar, semester) are all built from [datesheets] /
 * [filteredDatesheets] in the UI layer.
 */
class DatesheetBrowseController(
    private val datesheetRepository: DatesheetRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val datesheets: StateFlow<List<Datesheet>> =
        datesheetRepository.observeDatesheets().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDeptId = MutableStateFlow<String?>(null)
    val selectedDeptId: StateFlow<String?> = _selectedDeptId.asStateFlow()

    private val _selectedStartYear = MutableStateFlow<Int?>(null)
    val selectedStartYear: StateFlow<Int?> = _selectedStartYear.asStateFlow()

    private val _selectedShift = MutableStateFlow<Session?>(null)
    val selectedShift: StateFlow<Session?> = _selectedShift.asStateFlow()

    private val _selectedSemester = MutableStateFlow<Int?>(null)
    val selectedSemester: StateFlow<Int?> = _selectedSemester.asStateFlow()

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

    val filteredDatesheets: StateFlow<List<Datesheet>> = combine(datesheets, resolvedSession, _selectedSemester) { sheets, session, semester ->
        sheets.filter { (session == null || it.sessionId == session.sessionId) && (semester == null || it.semester == semester) }
    }.stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun selectSemester(semester: Int?) {
        _selectedSemester.value = semester
    }

    fun refresh() = launch {
        clearError()
        datesheetRepository.sync()
        datesheetRepository.syncAllSlots()
    }

    /** Creates the datesheet shell for (session, semester); DatesheetEditorController prefills its papers once mounted. */
    suspend fun createDatesheet(sessionId: String, semester: Int, defaultStartTime: String?, defaultEndTime: String?, defaultBuildingId: String?, instructions: String?, createdBy: String): String {
        val draft = DatesheetDraft(sessionId, semester, defaultStartTime, defaultEndTime, defaultBuildingId, instructions, published = false)
        validationMessage(draft).orThrowValidation()
        return datesheetRepository.createDatesheet(draft, createdBy)
    }
}
