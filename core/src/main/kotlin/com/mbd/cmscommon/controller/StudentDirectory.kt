package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.studentDirectoryExport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

const val STUDENT_DIRECTORY_PAGE_SIZE = 25

data class StudentDirectoryRow(
    val profile: StudentProfile,
    val session: AcademicSession?,
    val departmentName: String?,
)

enum class StudentDirectorySort(val label: String) {
    ROLL("Roll number"),
    NAME("Name"),
    CGPA("CGPA high to low"),
    NEWEST_SESSION("Newest session"),
}

enum class StudentAccountFilter(val label: String) {
    ALL("Any account"),
    LINKED("Account linked"),
    UNLINKED("Not linked"),
}

data class StudentDirectoryQuery(
    val search: String = "",
    val deptId: String? = null,
    val sessionId: String? = null,
    val shift: Session? = null,
    val enrollmentStatus: String? = null,
    val account: StudentAccountFilter = StudentAccountFilter.ALL,
    val sort: StudentDirectorySort = StudentDirectorySort.ROLL,
    val page: Int = 0,
) {
    val hasFilters: Boolean
        get() = search.isNotBlank() || deptId != null || sessionId != null || shift != null ||
            enrollmentStatus != null || account != StudentAccountFilter.ALL
}

data class StudentDirectoryPage(
    val rows: List<StudentDirectoryRow>,
    val matches: List<StudentDirectoryRow>,
    val page: Int,
    val pageCount: Int,
    val totalStudents: Int,
) {
    val firstIndex: Int get() = if (matches.isEmpty()) 0 else page * STUDENT_DIRECTORY_PAGE_SIZE + 1
    val lastIndex: Int get() = page * STUDENT_DIRECTORY_PAGE_SIZE + rows.size
}

fun studentDirectoryPage(all: List<StudentDirectoryRow>, query: StudentDirectoryQuery, pageSize: Int = STUDENT_DIRECTORY_PAGE_SIZE): StudentDirectoryPage {
    val search = query.search.trim()
    val matches = all.filter { row ->
        val p = row.profile
        (search.isEmpty() || listOfNotNull(p.name, p.rollNumber, p.universityRollNo, p.registrationNo, p.linkedEmail, p.fatherName)
            .any { it.contains(search, ignoreCase = true) }) &&
            (query.deptId == null || row.session?.deptId == query.deptId) &&
            (query.sessionId == null || p.sessionId == query.sessionId) &&
            (query.shift == null || p.shift == query.shift) &&
            (query.enrollmentStatus == null || p.enrollmentStatus.equals(query.enrollmentStatus, ignoreCase = true)) &&
            when (query.account) {
                StudentAccountFilter.ALL -> true
                StudentAccountFilter.LINKED -> p.linkedEmail.isNotBlank()
                StudentAccountFilter.UNLINKED -> p.linkedEmail.isBlank()
            }
    }.let { rows ->
        when (query.sort) {
            StudentDirectorySort.ROLL -> rows.sortedWith(compareBy({ it.profile.rollNumber }, { it.profile.sessionId }))
            StudentDirectorySort.NAME -> rows.sortedBy { it.profile.name.lowercase() }
            StudentDirectorySort.CGPA -> rows.sortedWith(compareByDescending<StudentDirectoryRow> { it.profile.cgpa ?: -1.0 }.thenBy { it.profile.rollNumber })
            StudentDirectorySort.NEWEST_SESSION -> rows.sortedWith(compareByDescending<StudentDirectoryRow> { it.session?.startYear ?: 0 }.thenBy { it.profile.rollNumber })
        }
    }
    val pageCount = ((matches.size + pageSize - 1) / pageSize).coerceAtLeast(1)
    val page = query.page.coerceIn(0, pageCount - 1)
    return StudentDirectoryPage(
        rows = matches.drop(page * pageSize).take(pageSize),
        matches = matches,
        page = page,
        pageCount = pageCount,
        totalStudents = all.size,
    )
}

/** Admin "Student Rosters": every locally cached student with search, filters, sort and paging. */
class StudentDirectoryController(
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _query = MutableStateFlow(StudentDirectoryQuery())
    val query: StateFlow<StudentDirectoryQuery> = _query.asStateFlow()

    val sessions: StateFlow<List<AcademicSession>> =
        sessionRepository.observeAllSessions().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val rows = combine(sessionRepository.observeAllStudentProfiles(), sessions, departments) { profiles, sessions, departments ->
        val sessionsById = sessions.associateBy { it.sessionId }
        val deptNames = departments.associate { it.deptId to it.name }
        profiles.map { p ->
            val session = sessionsById[p.sessionId]
            StudentDirectoryRow(p, session, session?.let { deptNames[it.deptId] })
        }
    }

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    val page: StateFlow<StudentDirectoryPage> = combine(rows, _query) { all, q ->
        _loaded.value = true
        studentDirectoryPage(all, q)
    }.stateIn(scope, SharingStarted.Eagerly, StudentDirectoryPage(emptyList(), emptyList(), 0, 1, 0))

    val enrollmentStatuses: StateFlow<List<String>> = rows
        .map { all -> all.map { it.profile.enrollmentStatus.uppercase() }.filter { it.isNotBlank() }.distinct().sorted() }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    fun setSearch(value: String) = _query.update { it.copy(search = value, page = 0) }

    fun setDepartment(deptId: String?) = _query.update {
        val keepSession = it.sessionId != null && sessions.value.firstOrNull { s -> s.sessionId == it.sessionId }?.deptId == deptId
        it.copy(deptId = deptId, sessionId = if (keepSession) it.sessionId else null, page = 0)
    }

    fun setSession(sessionId: String?) = _query.update {
        val session = sessions.value.firstOrNull { s -> s.sessionId == sessionId }
        it.copy(
            sessionId = sessionId,
            deptId = session?.deptId ?: it.deptId,
            shift = it.shift?.takeIf { shift -> session == null || session.runs(shift) },
            page = 0,
        )
    }
    fun setShift(shift: Session?) = _query.update { it.copy(shift = shift, page = 0) }
    fun setEnrollmentStatus(status: String?) = _query.update { it.copy(enrollmentStatus = status, page = 0) }
    fun setAccount(account: StudentAccountFilter) = _query.update { it.copy(account = account, page = 0) }
    fun setSort(sort: StudentDirectorySort) = _query.update { it.copy(sort = sort, page = 0) }
    fun previousPage() = _query.update { it.copy(page = (page.value.page - 1).coerceAtLeast(0)) }
    fun nextPage() = _query.update { it.copy(page = (page.value.page + 1).coerceAtMost(page.value.pageCount - 1)) }
    fun clearFilters() = _query.update { StudentDirectoryQuery(sort = it.sort) }

    /** Exports every student matching the current filters, not just the visible page. */
    fun exportDocument(): ExportDocument = studentDirectoryExport(page.value.matches, _query.value.hasFilters)
}
