package com.mbd.cmscommon.ui.navigation

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
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.mbd.cmscommon.ui.components.CmsTopBar
import com.mbd.cmscommon.ui.components.RefreshBox
import com.mbd.cmscommon.ui.state.GlobalRefreshViewModel
import com.mbd.cmscommon.ui.theme.CmsTheme

@Composable
fun RoleBottomNavScaffold(
    title: String,
    tabs: List<BottomNavTab>,
    homeRoute: String,
    goldWordmark: Boolean = false,
    navController: NavHostController,
    onBell: () -> Unit,
    notificationCount: Int = 0,
    navHost: @Composable (Modifier, Int) -> Unit,
) {
    val refreshVm: GlobalRefreshViewModel = hiltViewModel()
    val refreshing by refreshVm.refreshing.collectAsState()
    val refreshVersion by refreshVm.refreshVersion.collectAsState()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val activity = LocalContext.current as? Activity

    Scaffold(
        topBar = {
            CmsTopBar(
                title = title,
                onBack = { if (!navController.popBackStack()) activity?.finish() },
                onRefresh = refreshVm::refresh,
                isRefreshing = refreshing,
                onNotifications = onBell,
                notificationCount = notificationCount,
                goldWordmark = goldWordmark,
            )
        },
        bottomBar = {
            NavigationBar(containerColor = CmsTheme.colors.ink, contentColor = CmsTheme.colors.onInk) {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(homeRoute) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (tab.icon != null) {
                                Icon(tab.icon, contentDescription = tab.navLabel)
                            } else {
                                Text(tab.navLabel.take(1))
                            }
                        },
                        label = { Text(tab.navLabel) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = CmsTheme.colors.onInk,
                            selectedTextColor = CmsTheme.colors.onInk,
                            indicatorColor = CmsTheme.colors.accent,
                            unselectedIconColor = CmsTheme.colors.onInkMuted,
                            unselectedTextColor = CmsTheme.colors.onInkMuted,
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
            navHost(Modifier, refreshVersion)
        }
    }
}
