package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.ImportedStudentRow
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class SessionStudentsController(
    val sessionId: String,
    private val repo: AcademicSessionRepository,
    private val departmentRepo: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val students: StateFlow<List<SessionStudent>> =
        repo.observeStudents(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val session: StateFlow<AcademicSession?> =
        repo.observeSession(sessionId).stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    private val _importResult = MutableStateFlow<BulkImportSummary?>(null)
    val importResult: StateFlow<BulkImportSummary?> = _importResult.asStateFlow()

    private val _importing = MutableStateFlow(false)
    val importing: StateFlow<Boolean> = _importing.asStateFlow()

    /**
     * [AcademicSession.deptId] is the department's row id (e.g. "botany"), not its display code
     * (e.g. "BOT") -- validating roll numbers against it let "BOTANY-24-01" through instead of
     * requiring "BOT-24-01", since normalizeRollNumber uppercases the whole thing before matching.
     */
    private suspend fun currentDepartmentCode(currentSession: AcademicSession?): String? =
        currentSession?.deptId?.let { departmentRepo.getDepartment(it)?.code }

    fun addStudent(rollNumber: String, name: String, gpa: Double?, cgpa: Double?) = launch {
        try {
            val normalizedRoll = FieldValidators.normalizeRollNumber(rollNumber)
            val normalizedName = name.trim()
            val currentSession = session.value
            val departmentCode = currentDepartmentCode(currentSession)

            FieldValidators.rollNumberError(normalizedRoll, departmentCode, currentSession?.startYear).orThrowValidation()
            FieldValidators.nameError(normalizedName, "Student name").orThrowValidation()
            requireValid(gpa == null || gpa in 0.0..4.0) { "GPA must be between 0 and 4." }
            requireValid(cgpa == null || cgpa in 0.0..4.0) { "CGPA must be between 0 and 4." }
            requireValid(students.value.none { it.rollNumber.equals(normalizedRoll, ignoreCase = true) }) {
                "Roll number $normalizedRoll is already enrolled in this session."
            }

            repo.addStudent(sessionId, normalizedRoll, normalizedName, gpa, cgpa)
        } catch (t: Throwable) {
            throw IllegalStateException(t.userMessageLogged("Could not add the student."), t)
        }
    }

    fun importStudents(rows: List<ImportedStudentRow>) = launch {
        _importing.value = true
        try {
            val knownRolls = students.value.map { it.rollNumber.uppercase() }.toMutableSet()
            val currentSession = session.value
            val departmentCode = currentDepartmentCode(currentSession)
            val failures = mutableListOf<String>()
            var succeeded = 0

            for (row in rows) {
                val normalizedRoll = FieldValidators.normalizeRollNumber(row.rollNumber)
                val normalizedName = row.name.trim()
                val rollError = FieldValidators.rollNumberError(normalizedRoll, departmentCode, currentSession?.startYear)
                val nameError = FieldValidators.nameError(normalizedName, "Student name")
                when {
                    rollError != null -> failures += "Row ${row.rowNumber}: $rollError"
                    nameError != null -> failures += "Row ${row.rowNumber}: $nameError"
                    !knownRolls.add(normalizedRoll) -> failures += "Row ${row.rowNumber}: Roll number $normalizedRoll is already enrolled."
                    else -> {
                        try {
                            repo.addStudent(sessionId, normalizedRoll, normalizedName, null, null)
                            succeeded++
                        } catch (t: Throwable) {
                            failures += "Row ${row.rowNumber}: ${t.userMessageLogged("Could not add this student.")}"
                        }
                    }
                }
            }

            _importResult.value = BulkImportSummary(succeeded, failures)
        } finally {
            _importing.value = false
        }
    }

    fun clearImportResult() {
        _importResult.value = null
    }

    fun deleteStudent(studentId: String) = launch {
        repo.deleteStudent(studentId)
    }
}
