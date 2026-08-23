package com.mbd.cmsadmin.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mbd.cmscommon.ui.components.RefreshBox
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.state.GlobalRefreshViewModel
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsadmin.feature.notifications.NotificationsBadgeViewModel

/** Admin shell: a 5-tab bottom navigation bar over the drill-down NavHost (design.md §6.1). */
@Composable
fun AdminScaffold(onSignedOut: () -> Unit) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activity = LocalContext.current as? Activity
    val refreshVm: GlobalRefreshViewModel = hiltViewModel()
    val badgeVm: NotificationsBadgeViewModel = hiltViewModel()
    val refreshing by refreshVm.refreshing.collectAsState()
    val refreshVersion by refreshVm.refreshVersion.collectAsState()
    val unreadCount by badgeVm.unreadCount.collectAsState()

    Scaffold(
        topBar = {
            CmsTopBar(
                title = "GGC-MBD",
                onBack = { if (!navController.popBackStack()) activity?.finish() },
                onRefresh = refreshVm::refresh,
                isRefreshing = refreshing,
                onBell = {
                    navController.navigate(AdminLeaf.NOTIFICATIONS) { launchSingleTop = true }
                },
                notificationCount = unreadCount,
            )
        },
        bottomBar = {
            // Modernist: ink bar, white icons/labels, accent active indicator.
            NavigationBar(
                containerColor = CmsTheme.colors.ink,
                contentColor = CmsTheme.colors.onInk,
            ) {
                AdminTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(AdminTab.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CmsTheme.colors.onInk,
                            selectedTextColor = CmsTheme.colors.onInk,
                            unselectedIconColor = CmsTheme.colors.onInkMuted,
                            unselectedTextColor = CmsTheme.colors.onInkMuted,
                            indicatorColor = CmsTheme.colors.accent,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        RefreshBox(
            isRefreshing = refreshing,
            onRefresh = refreshVm::refresh,
            modifier = Modifier.padding(padding).fillMaxSize(),
        ) {
            AdminNavHost(
                navController = navController,
                onSignedOut = onSignedOut,
                refreshVersion = refreshVersion,
            )
        }
    }
}
