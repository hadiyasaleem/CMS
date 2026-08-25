package com.mbd.cmsteacher.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.root.RoleGatedRoot
import com.mbd.cmsteacher.feature.auth.LoginScreen
import com.mbd.cmsteacher.navigation.TeacherScaffold
import kotlinx.coroutines.delay

@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val role by viewModel.role.collectAsState()
    val authChecked by viewModel.authChecked.collectAsState()
    var minDurationElapsed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1_100)
        minDurationElapsed = true
    }

    if (!authChecked || !minDurationElapsed) {
        TeacherSplashScreen()
        return
    }

    RoleGatedRoot(
        role = role,
        isAccepted = { it is UserRole.Teacher },
        wrongRoleMessage = "This account is not a Teacher account.",
        onSignOut = { viewModel.signOut() },
        loginScreen = { LoginScreen(onLoginSuccess = { /* Role flow updates from the local cache. */ }) },
        content = { TeacherScaffold(onSignedOut = { viewModel.signOut() }) },
    )
}
