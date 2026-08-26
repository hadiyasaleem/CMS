package com.mbd.cmsdesktopteacher

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
import com.mbd.cmsdesktop.ui.login.LoginScreen
import com.mbd.cmsdesktop.ui.teacher.TeacherNavHost
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

fun main() = application {
    System.setProperty("cms.desktop.appId", "teacher")
    val component = remember { DesktopAppComponent.create() }
    val windowState = rememberWindowState(size = DpSize(1280.dp, 800.dp))
    var role by remember { mutableStateOf<UserRole.Teacher?>(null) }
    var authChecked by remember { mutableStateOf(false) }
    var minDurationElapsed by remember { mutableStateOf(false) }
    var roleRefreshInProgress by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_100)
        minDurationElapsed = true
    }
    LaunchedEffect(component) {
        val accountKey = withTimeoutOrNull(10_000) { component.sessionManager().awaitInitialization() }
        val cachedRole = accountKey?.let { key -> component.userRepository().getCachedRole(key) as? UserRole.Teacher }
        role = cachedRole
        authChecked = cachedRole != null
        if (accountKey != null) {
            roleRefreshInProgress = cachedRole == null
            launch {
                try {
                    val refreshedRole = runCatching { component.userRepository().resolveRole(accountKey) }.getOrNull() as? UserRole.Teacher
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
        title = "CMS Teacher Desktop",
        state = windowState,
        icon = painterResource("icon.png"),
    ) {
        CmsTheme(app = CmsApp.TEACHER) {
            val currentRole = role
            if (!authChecked || !minDurationElapsed || roleRefreshInProgress) {
                TeacherSplashScreen(statusText = "VERIFYING SECURE SESSION")
            } else if (currentRole == null) {
                LoginScreen(
                    sessionManager = component.sessionManager(),
                    roleResolver = component.roleResolver(),
                    userRepository = component.userRepository(),
                    portalEyebrow = "Faculty Portal",
                    screenTitle = "Teacher login",
                    brandDescription = "Faculty console - schedule, attendance, marks, examinations & student records.",
                    systemLabel = "GGC-MBD - FACULTY PORTAL",
                    emailLabel = "Email Address",
                    emailPlaceholder = "teacher@ggcmbd.edu.pk",
                    footerText = "Teacher accounts are created by an administrator. Self-registration isn't available.",
                    isAccepted = { it is UserRole.Teacher },
                    wrongRoleMessage = "This account is not a Teacher account",
                    onResolved = { resolved -> role = resolved as UserRole.Teacher },
                )
            } else {
                TeacherNavHost(currentRole, component, window, ::signOut)
            }
        }
    }
}
