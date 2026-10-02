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
    const val APP_LOGS = "app_logs"
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
    const val SESSION_DATESHEET = "session/{sessionId}/datesheet"
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
    fun sessionDatesheet(sessionId: String) = "session/$sessionId/datesheet"
    fun sessionFees(sessionId: String) = "session/$sessionId/fees"
    fun sessionFees(sessionId: String, shift: com.mbd.cmscommon.domain.model.Session) = "session/$sessionId/fees/${shift.name}"
    fun collegeFees(shift: com.mbd.cmscommon.domain.model.Session) = "college_fees/${shift.name}"
    fun studentRecord(sessionId: String, roll: String) = "student_record/$sessionId/$roll"
    fun teacherDetail(teacherId: String) = "teacher/$teacherId"
}

/** The top bar's title for the current route: "GGC-MBD" on a tab root, each screen's own short
 * name everywhere else. [route] is the route PATTERN from NavController (with `{placeholders}`),
 * which matches the constants above directly. */
fun adminScreenTitle(route: String?): String = when (route) {
    AdminTab.Dashboard.route, AdminTab.Academics.route, AdminTab.People.route,
    AdminTab.Records.route, AdminTab.More.route,
    -> "GGC-MBD"
    AdminLeaf.ADMINISTRATORS -> "Administrators"
    AdminLeaf.TEACHERS -> "Teachers"
    AdminLeaf.LINK_REQUESTS -> "Link Requests"
    AdminLeaf.MARK_EDIT_REQUESTS -> "Edit Requests"
    AdminLeaf.SUBMITTED_PAPERS -> "Submitted Papers"
    AdminLeaf.NOTIFICATIONS -> "Notifications"
    AdminLeaf.APP_LOGS -> "App Logs"
    AdminLeaf.PROFILE -> "Profile"
    AdminLeaf.MASTER_TIMETABLE -> "Master Timetable"
    AdminLeaf.BUILDINGS_ROOMS -> "Buildings & Rooms"
    AdminLeaf.FEE_STRUCTURES -> "Fee Structures"
    AdminLeaf.ATTENDANCE_RECORDS -> "Attendance Records"
    AdminLeaf.CALENDAR -> "Calendar"
    AdminLeaf.DATESHEETS -> "Master Datesheet"
    AdminLeaf.INSIGHTS -> "Insights"
    AdminLeaf.SEMESTER_RESULTS -> "Semester Results"
    AdminLeaf.STUDENT_DIRECTORY -> "Students"
    AdminRoutes.DEPT_DETAIL -> "Department"
    AdminRoutes.SESSION_DETAIL -> "Session"
    AdminRoutes.SEMESTER_SUBJECTS -> "Curriculum"
    AdminRoutes.SESSION_STUDENTS -> "Session Students"
    AdminRoutes.STUDENT_PROFILE -> "Student Profile"
    AdminRoutes.SESSION_TIMETABLE -> "Timetable"
    AdminRoutes.SESSION_DATESHEET -> "Datesheet"
    AdminRoutes.SESSION_FEES, AdminRoutes.SESSION_FEES_SHIFT -> "Fee Structure"
    AdminRoutes.COLLEGE_FEES -> "College Fees"
    AdminRoutes.STUDENT_RECORD -> "Student Record"
    AdminRoutes.TEACHER_DETAIL -> "Teacher Profile"
    else -> "GGC-MBD"
}
