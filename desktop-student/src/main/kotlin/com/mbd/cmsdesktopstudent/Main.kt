package com.mbd.cmsdesktopstudent

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.theme.CmsApp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmsdesktop.di.DesktopAppComponent
import com.mbd.cmsdesktop.ui.student.StudentAuthScreen
import com.mbd.cmsdesktop.ui.student.StudentNavHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

fun main() = application {
    System.setProperty("cms.desktop.appId", "student")
    val component = remember { DesktopAppComponent.create() }
    val windowState = rememberWindowState(size = DpSize(1280.dp, 800.dp))
    var role by remember { mutableStateOf<UserRole?>(null) }
    var authChecked by remember { mutableStateOf(false) }
    var minDurationElapsed by remember { mutableStateOf(false) }
    var roleRefreshInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_100)
        minDurationElapsed = true
    }
    LaunchedEffect(component) {
        val accountKey = withTimeoutOrNull(10_000) { component.sessionManager().awaitInitialization() }
        val cachedRole = accountKey?.let { key ->
            (component.userRepository().getCachedRole(key)).takeIf { it is UserRole.LinkedStudent || it is UserRole.UnlinkedStudent }
        }
        role = cachedRole
        authChecked = cachedRole != null
        if (accountKey != null) {
            roleRefreshInProgress = cachedRole == null
            launch {
                try {
                    val refreshedRole = runCatching { component.userRepository().resolveRole(accountKey) }.getOrNull()
                        ?.takeIf { it is UserRole.LinkedStudent || it is UserRole.UnlinkedStudent }
                    role = refreshedRole ?: cachedRole
                } finally {
                    roleRefreshInProgress = false
                    authChecked = true
                }
            }
        } else {
            authChecked = true
        }
    }

    fun signOut() {
        component.sessionManager().signOut()
        role = null
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CMS Student Desktop",
        state = windowState,
        icon = painterResource("splash_app_logo.png"),
    ) {
        CmsTheme(app = CmsApp.STUDENT) {
            val currentRole = role
            if (!authChecked || !minDurationElapsed || roleRefreshInProgress) {
                StudentSplashScreen(statusText = "VERIFYING SECURE SESSION")
            } else if (currentRole == null) {
                StudentAuthScreen(
                    sessionManager = component.sessionManager(),
                    userRepository = component.userRepository(),
                    roleResolver = component.roleResolver(),
                    onResolved = { resolved -> role = resolved },
                )
            } else {
                StudentNavHost(currentRole, component, window, ::signOut) { resolved -> role = resolved }
            }
        }
    }
}
