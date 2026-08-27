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
