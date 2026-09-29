package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.SubjectExamScore
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.studentRecordExport
import com.mbd.cmscommon.util.orLogCritical
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first

/** Everything the app holds about one student, gathered from the local cache for the record screen. */
data class StudentRecord(
    val profile: StudentProfile,
    val session: AcademicSession?,
    val department: Department?,
    val subjectNames: Map<String, String>,
    val attendance: List<AttendanceTally>,
    val marks: List<SubjectExamScore>,
    val results: List<SemesterGpa>,
    val feeStructure: SessionFeeStructure?,
    val snapshot: StudentProfileSnapshot,
)

class StudentRecordController(
    val sessionId: String,
    val rollNumber: String,
    private val sessionRepository: AcademicSessionRepository,
    private val departmentRepository: DepartmentRepository,
    private val curriculumRepository: CurriculumRepository,
    private val attendanceRepository: SessionAttendanceRepository,
    private val marksRepository: SessionMarksRepository,
    private val feeRepository: SessionFeeRepository,
    private val fineRepository: FineRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _record = MutableStateFlow<StudentRecord?>(null)
    val record: StateFlow<StudentRecord?> = _record.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _notFound = MutableStateFlow(false)
    val notFound: StateFlow<Boolean> = _notFound.asStateFlow()

    init {
        refresh()
    }

    fun refresh() = launch("load the student record") {
        _loading.value = true
        try {
            val profile = sessionRepository.getStudentProfile(sessionId, rollNumber)
            _notFound.value = profile == null
            if (profile == null) return@launch
            val session = sessionRepository.observeSession(sessionId).first()
            val department = session?.let { s -> departmentRepository.observeActiveDepartments().first().firstOrNull { it.deptId == s.deptId } }
            // Each block below is optional: a missing cache for one area shouldn't hide the rest of the record.
            val subjects = runCatching { curriculumRepository.observeSessionSubjects(sessionId).first() }
                .orLogCritical("StudentRecordController.subjects").orEmpty()
            val fines = runCatching { fineRepository.getFines(sessionId, rollNumber) }.orLogCritical("StudentRecordController.fines").orEmpty()
            _record.value = StudentRecord(
                profile = profile,
                session = session,
                department = department,
                subjectNames = subjects.associate { it.courseCode to it.name },
                attendance = runCatching { attendanceRepository.observeStudentTallies(sessionId, rollNumber).first() }
                    .orLogCritical("StudentRecordController.attendance").orEmpty(),
                marks = runCatching { marksRepository.observeStudentMarks(sessionId, rollNumber).first() }
                    .orLogCritical("StudentRecordController.marks").orEmpty(),
                results = runCatching { marksRepository.getSemesterGpa(sessionId, rollNumber) }
                    .orLogCritical("StudentRecordController.results").orEmpty().sortedBy { it.semester },
                feeStructure = runCatching { feeRepository.getSessionFee(sessionId, profile.shift) }.orLogCritical("StudentRecordController.fees"),
                snapshot = studentProfileSnapshot(profile, fines),
            )
        } finally {
            _loading.value = false
        }
    }

    fun exportDocument(): ExportDocument? = _record.value?.let(::studentRecordExport)
}
