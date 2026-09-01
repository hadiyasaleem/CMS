package com.mbd.cmsdesktop.ui.admin

/**
 * Navigation destinations for the admin desktop shell. Mirrors the backstack entries the
 * decompiled `AdminNavHost` pushed/popped by hand (there is no Compose Navigation dependency
 * in this app - [AdminNavHost] keeps a small manual stack of these).
 */
sealed interface AdminScreen {
    data object Dashboard : AdminScreen
    data object Academics : AdminScreen
    data object PeopleHub : AdminScreen
    data object RecordsHub : AdminScreen
    data object MoreHub : AdminScreen
    data object Administrators : AdminScreen
    data object Teachers : AdminScreen
    data object LinkRequests : AdminScreen
    data object MarkEditRequests : AdminScreen
    data object AttendanceRecords : AdminScreen
    data object Calendar : AdminScreen
    data object Datesheets : AdminScreen
    data object MasterTimetable : AdminScreen
    data object FeesPicker : AdminScreen
    data object Insights : AdminScreen
    data object Notifications : AdminScreen
    data object Profile : AdminScreen
    data class DeptDetail(val deptId: String) : AdminScreen
    data class SessionDetail(val sessionId: String) : AdminScreen
    data class SessionStudents(val sessionId: String) : AdminScreen
    data class StudentProfile(val sessionId: String, val roll: String) : AdminScreen
    data class SessionTimetableRoute(val sessionId: String) : AdminScreen
    data class SemesterSubjectsRoute(val sessionId: String, val semester: Int) : AdminScreen
    data class SessionFeesRoute(val sessionId: String) : AdminScreen
}
