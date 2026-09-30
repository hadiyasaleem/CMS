package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class DepartmentDetailController(
    val deptId: String,
    private val departmentRepository: DepartmentRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val editedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _department = MutableStateFlow<Department?>(null)
    val department: StateFlow<Department?> = _department.asStateFlow()

    val deptName: StateFlow<String> = _department
        .map { it?.name ?: deptId }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), deptId)

    /** All sessions for this department, active and graduated alike -- the UI splits them into
     * the "Current intakes" grid and a separate "Graduated sessions" section so a graduated
     * session's curriculum, timetable, and datesheets stay reachable even after it stops
     * counting toward department stats. */
    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeSessionsForDept(deptId)
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    fun consumeNotice() {
        _notice.value = null
    }

    init {
        launch("load the department") { _department.value = departmentRepository.getDepartment(deptId) }
    }

    /**
     * Creates one session for the intake year serving the ticked [shifts] (Morning, Evening or both), with
     * [maxStudents] seats in total. A department has at most one session per intake year and program type.
     */
    /** [copyFromSessionId] -- an existing session (any department, but normally this one's previous intake) whose
     * whole semester 1-8 curriculum is copied into the new session, so setting up a new intake doesn't mean
     * re-adding the same subjects one by one. */
    fun createSession(
        startYear: Int,
        shifts: Set<Session>,
        maxStudents: Int,
        programType: ProgramType = ProgramType.BS,
        copyFromSessionId: String? = null,
    ) = launch("create the session") {
        // The UI only ever offers full 4-digit years (see intakeYearOptions() in
        // DepartmentDetailWorkspace); this guards the controller boundary in case anything else
        // ever calls this directly with a 2-digit year like 21 instead of 2021.
        requireValid(startYear in 1900..9999) { "Enter a valid 4-digit intake year." }
        createSessionError(startYear, shifts, maxStudents.toString(), sessions.value, programType).orThrowValidation()
        val mode = ShiftMode.of(shifts) ?: return@launch
        val created = sessionRepository.createSession(deptId, startYear, mode, maxStudents, programType)
        if (copyFromSessionId != null) {
            curriculumRepository.copyAllSemesterSubjects(copyFromSessionId, created.sessionId)
            val fromLabel = sessions.value.firstOrNull { it.sessionId == copyFromSessionId }?.label ?: copyFromSessionId
            _notice.value = "Session created with subjects copied from $fromLabel."
        } else {
            _notice.value = "Session created."
        }
    }

    fun updateDetails(name: String, code: String, hodEmail: String?, description: String?) {
        val current = _department.value ?: return
        launch("update the department") {
            FieldValidators.nameError(name, "Department name", required = false).orThrowValidation()
            FieldValidators.departmentCodeError(code).orThrowValidation()
            requireValid(FieldValidators.emailError(hodEmail ?: "", required = false) == null) {
                "Choose a valid head of department."
            }
            requireValid((description ?: "").trim().length <= 500) {
                "Department description must not exceed 500 characters."
            }

            departmentRepository.observeActiveDepartments().first().filter { it.deptId != current.deptId }.let { others ->
                others.firstOrNull { it.code.equals(code.trim(), ignoreCase = true) }?.let {
                    throw CmsException.Conflict("A department with code ${it.code} already exists (${it.name}).")
                }
                others.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }?.let {
                    throw CmsException.Conflict("A department named \"${it.name}\" already exists (code ${it.code}).")
                }
            }

            val updated = current.copy(
                name = name.trim(),
                code = code.trim(),
                hodEmail = hodEmail?.trim()?.takeIf { it.isNotBlank() },
                description = description?.trim()?.takeIf { it.isNotBlank() },
                updatedAt = Instant.now(),
                updatedBy = editedBy,
            )
            departmentRepository.updateDepartment(updated)
            _department.value = updated
            _notice.value = "Department details updated."
        }
    }
}
