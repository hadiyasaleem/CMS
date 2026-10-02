package com.mbd.cmsdesktop.ui.admin

import compose.icons.TablerIcons
import compose.icons.tablericons.ChartBar
import compose.icons.tablericons.Dots
import compose.icons.tablericons.LayoutGrid
import compose.icons.tablericons.School
import compose.icons.tablericons.Users
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 top-level rail destinations of the admin desktop shell, each rooted at an [AdminScreen]. */
enum class AdminTab(val label: String, val icon: ImageVector, val root: AdminScreen) {
    Dashboard("Dashboard", TablerIcons.LayoutGrid, AdminScreen.Dashboard),
    Academics("Academics", TablerIcons.School, AdminScreen.Academics),
    People("People", TablerIcons.Users, AdminScreen.PeopleHub),
    Records("Records", TablerIcons.ChartBar, AdminScreen.RecordsHub),
    More("More", TablerIcons.Dots, AdminScreen.MoreHub),
}
