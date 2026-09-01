package com.mbd.cmsdesktopadmin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.mbd.cmsdesktop.ui.admin.AdminNavHost
import com.mbd.cmsdesktop.ui.login.LoginScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

fun main() = application {
    System.setProperty("cms.desktop.appId", "admin")
    val component = remember { DesktopAppComponent.create() }
    val windowState = rememberWindowState(size = DpSize(1280.dp, 800.dp))
    var role by remember { mutableStateOf<UserRole.Admin?>(null) }
    var authChecked by remember { mutableStateOf(false) }
    var minDurationElapsed by remember { mutableStateOf(false) }
    var bootstrapInProgress by remember { mutableStateOf(false) }
    var bootstrapRequest by remember { mutableStateOf(0) }
    var bootstrapAccountKey by remember { mutableStateOf<String?>(null) }
    var roleRefreshInProgress by remember { mutableStateOf(false) }
    val bootstrapStore = remember(component) { component.bootstrapSnapshotStore() }

    suspend fun loadAdminData(): Boolean = runCatching {
        withTimeoutOrNull(180_000) {
            component.adminDataBootstrapper().refreshAll()
        } ?: false
    }.getOrDefault(false)

    fun requestBootstrap(accountKey: String) {
        bootstrapAccountKey = accountKey
        bootstrapInProgress = true
        bootstrapRequest += 1
    }

    LaunchedEffect(Unit) {
        delay(1_100)
        minDurationElapsed = true
    }
    LaunchedEffect(component) {
        val accountKey = withTimeoutOrNull(10_000) { component.sessionManager().awaitInitialization() }
        val cachedRole = accountKey?.let { key -> component.userRepository().getCachedRole(key) as? UserRole.Admin }
        role = cachedRole
        authChecked = cachedRole != null
        if (cachedRole != null) requestBootstrap(cachedRole.uid)

        if (accountKey != null) {
            roleRefreshInProgress = cachedRole == null
            launch {
                try {
                    val refreshedRole = runCatching { component.userRepository().resolveRole(accountKey) }.getOrNull() as? UserRole.Admin
                    role = refreshedRole ?: cachedRole
                    if ((refreshedRole ?: cachedRole) != null && cachedRole == null && !bootstrapInProgress) {
                        requestBootstrap(accountKey)
                    }
                } finally {
                    roleRefreshInProgress = false
                    authChecked = true
                }
            }
        } else {
            authChecked = true
        }
    }
    LaunchedEffect(bootstrapRequest) {
        if (bootstrapRequest == 0) return@LaunchedEffect
        val accountKey = bootstrapAccountKey ?: return@LaunchedEffect
        try {
            if (loadAdminData()) {
                bootstrapStore.markBootstrapComplete(ADMIN_BOOTSTRAP_SCOPE, accountKey)
            }
        } finally {
            bootstrapInProgress = false
        }
    }

    fun signOut() {
        component.sessionManager().signOut()
        role = null
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "CMS Admin Desktop",
        state = windowState,
        icon = painterResource("icon.png"),
    ) {
        CmsTheme(app = CmsApp.ADMIN) {
            Box(
                modifier = Modifier.fillMaxSize().background(CmsTheme.colors.ink),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .width(ADMIN_MOBILE_CANVAS_WIDTH)
                        .background(CmsTheme.colors.faint),
                ) {
                    val currentRole = role
                    if (!authChecked || !minDurationElapsed || roleRefreshInProgress || bootstrapInProgress) {
                        AdminSplashScreen(
                            statusText = if (bootstrapInProgress) "LOADING ADMIN DATA" else "VERIFYING SECURE SESSION",
                        )
                    } else if (currentRole == null) {
                        LoginScreen(
                            sessionManager = component.sessionManager(),
                            roleResolver = component.roleResolver(),
                            userRepository = component.userRepository(),
                            portalEyebrow = "Security Portal",
                            screenTitle = "Admin login",
                            brandDescription = "Central console — enrolment, faculty, attendance, examinations & records.",
                            systemLabel = "GGC-MBD - ADMIN PORTAL",
                            emailLabel = "Email Address",
                            emailPlaceholder = "admin@ggcmbd.edu.pk",
                            footerText = "Admin accounts are created by another administrator. Self-registration isn't available.",
                            isAccepted = { it is UserRole.Admin },
                            wrongRoleMessage = "This account is not an Admin account",
                            onResolved = { resolved ->
                                role = resolved as UserRole.Admin
                                requestBootstrap(resolved.uid)
                            },
                        )
                    } else {
                        AdminNavHost(currentRole, component, window, ::signOut)
                    }
                }
            }
        }
    }
}

private const val ADMIN_BOOTSTRAP_SCOPE = "admin-data"
private val ADMIN_MOBILE_CANVAS_WIDTH = 412.dp
