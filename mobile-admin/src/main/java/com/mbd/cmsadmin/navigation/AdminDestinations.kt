package com.mbd.cmsadmin.navigation

import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Dashboard
import compose.icons.tablericons.Dots
import compose.icons.tablericons.School
import compose.icons.tablericons.Users
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The five bottom-navigation tabs. The information architecture follows the college's real
 * structure: Department → 8-semester curriculum + fee structure + Sessions (intakes like
 * "2021-2025 Morning") → students + weekly timetable. No terms/offerings indirection.
 */
enum class AdminTab(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("tab_dashboard", "Dashboard", TablerIcons.Dashboard),
    Academics("tab_academics", "Academics", TablerIcons.School),
    People("tab_people", "People", TablerIcons.Users),
    Records("tab_records", "Records", TablerIcons.ChartBar),
    More("tab_more", "More", TablerIcons.Dots),
}

/** Leaf routes reached from tabs (registered once in the NavHost). */
object AdminLeaf {
    const val ADMINISTRATORS = "administrators"
    const val TEACHERS = "teachers"
    const val LINK_REQUESTS = "link_requests"
    const val MARK_EDIT_REQUESTS = "mark_edit_requests"
    const val SUBMITTED_PAPERS = "submitted_papers"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val MASTER_TIMETABLE = "master_timetable"
    const val BUILDINGS_ROOMS = "buildings_rooms"
    const val FEE_STRUCTURES = "fee_structures"    // college-wide base + every class's fees in one grid
    const val ATTENDANCE_RECORDS = "attendance_records"
    const val CALENDAR = "calendar"
    const val DATESHEETS = "datesheets"
    const val INSIGHTS = "insights"
    const val SEMESTER_RESULTS = "semester_results"
    const val STUDENT_DIRECTORY = "student_directory"
}

/** Parameterized drill-down routes: department → curriculum / sessions / fees → session detail. */
object AdminRoutes {
    const val DEPT_DETAIL = "dept/{deptId}"
    const val SESSION_DETAIL = "session/{sessionId}"
    const val SEMESTER_SUBJECTS = "session/{sessionId}/semester/{semester}"
    const val SESSION_STUDENTS = "session/{sessionId}/students"
    const val STUDENT_PROFILE = "session/{sessionId}/student/{roll}"
    const val SESSION_TIMETABLE = "session/{sessionId}/timetable"
    const val SESSION_FEES = "session/{sessionId}/fees"
    const val SESSION_FEES_SHIFT = "session/{sessionId}/fees/{shift}"
    const val COLLEGE_FEES = "college_fees/{shift}"
    const val STUDENT_RECORD = "student_record/{sessionId}/{roll}"
    const val TEACHER_DETAIL = "teacher/{teacherId}"

    fun deptDetail(deptId: String) = "dept/$deptId"
    fun semesterSubjects(sessionId: String, semester: Int) = "session/$sessionId/semester/$semester"
    fun sessionDetail(sessionId: String) = "session/$sessionId"
    fun sessionStudents(sessionId: String) = "session/$sessionId/students"
    fun studentProfile(sessionId: String, roll: String) = "session/$sessionId/student/$roll"
    fun sessionTimetable(sessionId: String) = "session/$sessionId/timetable"
    fun sessionFees(sessionId: String) = "session/$sessionId/fees"
    fun sessionFees(sessionId: String, shift: com.mbd.cmscommon.domain.model.Session) = "session/$sessionId/fees/${shift.name}"
    fun collegeFees(shift: com.mbd.cmscommon.domain.model.Session) = "college_fees/${shift.name}"
    fun studentRecord(sessionId: String, roll: String) = "student_record/$sessionId/$roll"
    fun teacherDetail(teacherId: String) = "teacher/$teacherId"
}
