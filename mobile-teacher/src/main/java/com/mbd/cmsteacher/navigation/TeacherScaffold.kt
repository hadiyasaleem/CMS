package com.mbd.cmsteacher.navigation

import android.app.Activity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.components.RefreshBox
import com.mbd.cmscommon.ui.state.GlobalRefreshViewModel
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsteacher.feature.notifications.NotificationsBadgeViewModel

@Composable
fun TeacherScaffold(onSignedOut: () -> Unit) {
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
                title = "CMS Teacher",
                onBack = { if (!navController.popBackStack()) activity?.finish() },
                onRefresh = refreshVm::refresh,
                isRefreshing = refreshing,
                onNotifications = {
                    navController.navigate(TeacherDestination.Notifications.route) { launchSingleTop = true }
                },
                notificationCount = unreadCount,
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CmsTheme.colors.ink,
                contentColor = CmsTheme.colors.onInk,
            ) {
                TeacherDestination.bottomNavItems.forEach { destination ->
                    NavigationBarItem(
                        selected = currentRoute == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                // Always reset to this tab's root screen, discarding any drill-down --
                                // no saveState/restoreState, which would otherwise restore that
                                // drill-down instead of the tab root.
                                popUpTo(TeacherDestination.Home.route)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(destination.navIcon!!, contentDescription = destination.navLabel) },
                        label = { Text(destination.navLabel) },
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
            TeacherNavHost(
                navController = navController,
                onSignedOut = onSignedOut,
                refreshVersion = refreshVersion,
            )
        }
    }
}
