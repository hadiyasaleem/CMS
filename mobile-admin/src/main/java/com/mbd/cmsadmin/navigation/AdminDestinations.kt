package com.mbd.cmsadmin.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * The five bottom-navigation tabs. The information architecture follows the college's real
 * structure: Department → 8-semester curriculum + fee structure + Sessions (intakes like
 * "2021-2025 Morning") → students + weekly timetable. No terms/offerings indirection.
 */
enum class AdminTab(val route: String, val label: String, val icon: ImageVector) {
    Dashboard("tab_dashboard", "Dashboard", Icons.Filled.Dashboard),
    Academics("tab_academics", "Academics", Icons.Filled.School),
    People("tab_people", "People", Icons.Filled.Groups),
    Records("tab_records", "Records", Icons.Filled.Assessment),
    More("tab_more", "More", Icons.Filled.MoreHoriz),
}

/** Leaf routes reached from tabs (registered once in the NavHost). */
object AdminLeaf {
    const val ADMINISTRATORS = "administrators"
    const val TEACHERS = "teachers"
    const val LINK_REQUESTS = "link_requests"
    const val MARK_EDIT_REQUESTS = "mark_edit_requests"
    const val NOTIFICATIONS = "notifications"
    const val PROFILE = "profile"
    const val MASTER_TIMETABLE = "master_timetable"
    const val FEES_PICKER = "fees_picker"          // pick a department → straight to ITS fees
    const val ATTENDANCE_RECORDS = "attendance_records"
    const val CALENDAR = "calendar"
    const val DATESHEETS = "datesheets"
    const val DOCUMENTS = "documents"
    const val INSIGHTS = "insights"
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

    fun deptDetail(deptId: String) = "dept/$deptId"
    fun semesterSubjects(sessionId: String, semester: Int) = "session/$sessionId/semester/$semester"
    fun sessionDetail(sessionId: String) = "session/$sessionId"
    fun sessionStudents(sessionId: String) = "session/$sessionId/students"
    fun studentProfile(sessionId: String, roll: String) = "session/$sessionId/student/$roll"
    fun sessionTimetable(sessionId: String) = "session/$sessionId/timetable"
    fun sessionFees(sessionId: String) = "session/$sessionId/fees"
}
