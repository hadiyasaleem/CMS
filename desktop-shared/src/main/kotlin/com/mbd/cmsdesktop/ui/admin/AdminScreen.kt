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
    data object SubmittedPapers : AdminScreen
    data object AttendanceRecords : AdminScreen
    data object Calendar : AdminScreen
    data object Datesheets : AdminScreen
    data object MasterTimetable : AdminScreen
    data object BuildingsRooms : AdminScreen
    data object FeeStructures : AdminScreen
    data class CollegeFees(val shift: com.mbd.cmscommon.domain.model.Session) : AdminScreen
    data object Insights : AdminScreen
    data object SemesterResults : AdminScreen
    data object Notifications : AdminScreen
    data object AppLogs : AdminScreen
    data object Profile : AdminScreen
    data class DeptDetail(val deptId: String) : AdminScreen
    data class SessionDetail(val sessionId: String) : AdminScreen
    data class SessionStudents(val sessionId: String) : AdminScreen
    data class StudentProfile(val sessionId: String, val roll: String) : AdminScreen
    data object StudentDirectory : AdminScreen
    data class StudentRecord(val sessionId: String, val roll: String) : AdminScreen
    data class SessionTimetableRoute(val sessionId: String) : AdminScreen
    data class SessionDatesheetRoute(val sessionId: String) : AdminScreen
    data class SemesterSubjectsRoute(val sessionId: String, val semester: Int) : AdminScreen
    data class SessionFeesRoute(val sessionId: String, val shift: com.mbd.cmscommon.domain.model.Session? = null) : AdminScreen
    data class TeacherDetail(val teacherId: String) : AdminScreen
}

/** The top bar's title for this screen: "GGC-MBD" on a tab root, each screen's own short name
 * everywhere else. */
fun AdminScreen.title(): String = when (this) {
    AdminScreen.Dashboard, AdminScreen.Academics, AdminScreen.PeopleHub,
    AdminScreen.RecordsHub, AdminScreen.MoreHub,
    -> "GGC-MBD"
    AdminScreen.Administrators -> "Administrators"
    AdminScreen.Teachers -> "Teachers"
    AdminScreen.LinkRequests -> "Link Requests"
    AdminScreen.MarkEditRequests -> "Edit Requests"
    AdminScreen.SubmittedPapers -> "Submitted Papers"
    AdminScreen.AttendanceRecords -> "Attendance Records"
    AdminScreen.Calendar -> "Calendar"
    AdminScreen.Datesheets -> "Master Datesheet"
    AdminScreen.MasterTimetable -> "Master Timetable"
    AdminScreen.BuildingsRooms -> "Buildings & Rooms"
    AdminScreen.FeeStructures -> "Fee Structures"
    is AdminScreen.CollegeFees -> "College Fees"
    AdminScreen.Insights -> "Insights"
    AdminScreen.SemesterResults -> "Semester Results"
    AdminScreen.Notifications -> "Notifications"
    AdminScreen.AppLogs -> "App Logs"
    AdminScreen.Profile -> "Profile"
    is AdminScreen.DeptDetail -> "Department"
    is AdminScreen.SessionDetail -> "Session"
    is AdminScreen.SessionStudents -> "Session Students"
    is AdminScreen.StudentProfile -> "Student Profile"
    AdminScreen.StudentDirectory -> "Students"
    is AdminScreen.StudentRecord -> "Student Record"
    is AdminScreen.SessionTimetableRoute -> "Timetable"
    is AdminScreen.SessionDatesheetRoute -> "Datesheet"
    is AdminScreen.SemesterSubjectsRoute -> "Curriculum"
    is AdminScreen.SessionFeesRoute -> "Fee Structure"
    is AdminScreen.TeacherDetail -> "Teacher Profile"
}
