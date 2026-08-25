package com.mbd.cmsdesktop.ui.admin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

/** The 5 top-level rail destinations of the admin desktop shell, each rooted at an [AdminScreen]. */
enum class AdminTab(val label: String, val icon: ImageVector, val root: AdminScreen) {
    Dashboard("Dashboard", Icons.Filled.Dashboard, AdminScreen.Dashboard),
    Academics("Academics", Icons.Filled.School, AdminScreen.Academics),
    People("People", Icons.Filled.Groups, AdminScreen.PeopleHub),
    Records("Records", Icons.Filled.Assessment, AdminScreen.RecordsHub),
    More("More", Icons.Filled.MoreHoriz, AdminScreen.MoreHub),
}
