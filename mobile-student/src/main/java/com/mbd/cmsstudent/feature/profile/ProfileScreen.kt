package com.mbd.cmsstudent.feature.profile

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentOwnProfileWorkspace

@Composable
fun ProfileScreen(onSignedOut: () -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val actionMessage by viewModel.actionMessage.collectAsState()
    val error by viewModel.error.collectAsState()

    StudentOwnProfileWorkspace(
        session = state?.context?.session,
        studentName = state?.context?.name.orEmpty(),
        rollNumber = state?.context?.rollNumber.orEmpty(),
        gpa = state?.context?.gpa,
        cgpa = state?.context?.cgpa,
        linkedEmail = viewModel.accountKey,
        profile = state?.profile,
        departmentName = state?.department?.name,
        accountKey = viewModel.accountKey,
        fines = state?.fines.orEmpty(),
        loading = state == null,
        errorMessage = error,
        actionMessage = actionMessage,
        onRetry = viewModel::refresh,
        onResetPassword = viewModel::resetPassword,
        onSignOut = {
            viewModel.signOut()
            onSignedOut()
        },
    )
}
