package com.mbd.cmsdesktop.ui.student

/**
 * Every destination reachable inside the student desktop shell. Splits into the 5 tab roots (mirrors
 * [StudentTab]) plus the leaves reachable from Home/ExamsHub/More - replaces the stopgap's ad-hoc
 * `StudentLeaf` with a single navigation model shared by the tab bar and the leaf back-stack.
 */
sealed interface StudentScreen {
    data object Home : StudentScreen
    data object Attendance : StudentScreen
    data object ExamsHub : StudentScreen
    data object Timetable : StudentScreen
    data object MoreHub : StudentScreen
    data object Marks : StudentScreen
    data object Results : StudentScreen
    data object Datesheets : StudentScreen
    data object Events : StudentScreen
    data object Fees : StudentScreen
    data object Notifications : StudentScreen
    data object Profile : StudentScreen
}

/** The top bar's title for this screen: "GGC-MBD" (the CmsTopBar default, unchanged from today)
 * on a tab root, each screen's own short name everywhere else. */
fun StudentScreen.title(): String = when (this) {
    StudentScreen.Home, StudentScreen.Attendance, StudentScreen.ExamsHub,
    StudentScreen.Timetable, StudentScreen.MoreHub,
    -> "GGC-MBD"
    StudentScreen.Marks -> "Marks"
    StudentScreen.Results -> "Results"
    StudentScreen.Datesheets -> "Datesheets"
    StudentScreen.Events -> "Events"
    StudentScreen.Fees -> "Fee Challan"
    StudentScreen.Notifications -> "Notifications"
    StudentScreen.Profile -> "Profile"
}
