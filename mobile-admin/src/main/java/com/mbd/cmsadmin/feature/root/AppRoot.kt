package com.mbd.cmsadmin.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmsadmin.feature.auth.LoginScreen
import com.mbd.cmsadmin.navigation.AdminScaffold
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.root.RoleGatedRoot
import kotlinx.coroutines.delay

/** Routes only after both the startup animation and persisted-session check have completed. */
@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val role by viewModel.role.collectAsState()
    val authChecked by viewModel.authChecked.collectAsState()
    val readyAccount by viewModel.readyAccount.collectAsState()
    val isBootstrapping by viewModel.isBootstrapping.collectAsState()
    var minDurationElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_100)
        minDurationElapsed = true
    }

    val adminAccount = (role as? UserRole.Admin)?.uid
    val dataReady = adminAccount == null || readyAccount == adminAccount
    if (!authChecked || !minDurationElapsed || !dataReady || isBootstrapping) {
        AdminSplashScreen(
            statusText = if (adminAccount != null) "LOADING ADMIN DATA" else "VERIFYING SECURE SESSION",
        )
        return
    }

    RoleGatedRoot(
        role = role,
        isAccepted = { it is UserRole.Admin },
        wrongRoleMessage = "This account is not an Admin account.",
        onSignOut = { viewModel.signOut() },
        loginScreen = { LoginScreen(onLoginSuccess = { /* Role flow updates from the local cache. */ }) },
        content = { AdminScaffold(onSignedOut = { viewModel.signOut() }) },
    )
}
