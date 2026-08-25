package com.mbd.cmsstudent.feature.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.ui.components.BrandedSplashScreen
import com.mbd.cmscommon.ui.root.RoleGatedRoot
import com.mbd.cmsstudent.R
import com.mbd.cmsstudent.feature.auth.AuthScreen
import com.mbd.cmsstudent.feature.linkrequest.LinkRequestScreen
import com.mbd.cmsstudent.navigation.StudentScaffold

@Composable
fun AppRoot(viewModel: AppRootViewModel = hiltViewModel()) {
    val role by viewModel.role.collectAsState()
    val authChecked by viewModel.authChecked.collectAsState()

    if (!authChecked) {
        BrandedSplashScreen(
            background = painterResource(R.drawable.splash_postgraduate_block),
            logo = painterResource(R.drawable.splash_app_logo),
            portalLabel = "Student Portal",
            statusText = "VERIFYING SECURE SESSION",
        )
        return
    }

    RoleGatedRoot(
        role = role,
        isAccepted = { it is UserRole.LinkedStudent || it is UserRole.UnlinkedStudent },
        wrongRoleMessage = "This account is not a Student account.",
        onSignOut = { viewModel.signOut() },
        loginScreen = { AuthScreen(onLoginSuccess = { /* Role flow updates from the local cache. */ }) },
        content = { resolvedRole ->
            if (resolvedRole is UserRole.LinkedStudent) {
                StudentScaffold(onSignedOut = { viewModel.signOut() })
            } else {
                LinkRequestScreen()
            }
        },
    )
}
