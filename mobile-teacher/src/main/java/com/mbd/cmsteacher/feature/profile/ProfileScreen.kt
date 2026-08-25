package com.mbd.cmsteacher.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherProfileWorkspace

@Composable
fun ProfileScreen(onSignedOut: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val profile by viewModel.profile.collectAsState()
    val assignments by viewModel.assignments.collectAsState()
    val departmentName by viewModel.departmentName.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val error by viewModel.error.collectAsState()

    TeacherProfileWorkspace(
        profile = profile,
        accountKey = viewModel.accountKey,
        departmentName = departmentName,
        assignments = assignments,
        loading = profile == null,
        errorMessage = error,
        actionMessage = actionMessage,
        onResetPassword = viewModel::resetPassword,
        onSignOut = {
            viewModel.signOut()
            onSignedOut()
        },
    )
}
