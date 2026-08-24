package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.CollegeInfo

private val StudentAuthCanvas = Color(0xFFF7F5F0)
private val AuthRed = Color(0xFFB43A31)
private val AuthGreen = Color(0xFF2F6B4F)

data class StudentAuthUiState(
    val email: String = "",
    val password: String = "",
    val registerMode: Boolean = false,
    val loading: Boolean = false,
    val errorMessage: String? = null,
    val noticeMessage: String? = null,
)

data class StudentAuthActions(
    val onEmailChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onModeChange: (Boolean) -> Unit,
    val onSubmit: () -> Unit,
    val onPasswordReset: () -> Unit,
)

@Composable
fun StudentAuthWorkspace(state: StudentAuthUiState, actions: StudentAuthActions, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize().background(StudentAuthCanvas),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { StudentAuthBrand() }
        if (!state.errorMessage.isNullOrBlank()) {
            item { AuthStatusCard(state.errorMessage, error = true) }
        }
        if (!state.noticeMessage.isNullOrBlank()) {
            item { AuthStatusCard(state.noticeMessage, error = false) }
        }
        item { StudentAuthForm(state, actions) }
    }
}

@Composable
private fun StudentAuthBrand(modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("GGC-MBD · STUDENT PORTAL", color = CmsTheme.colors.accent, style = CmsTextStyles.eyebrow)
        Spacer(Modifier.height(8.dp))
        Text("Student Portal", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(6.dp))
        Text(
            "Attendance, marks, timetable and fee records in one secure student portal.",
            color = Color(0xFF77716A),
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(4.dp))
        Text(CollegeInfo.NAME, color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StudentAuthForm(state: StudentAuthUiState, actions: StudentAuthActions, modifier: Modifier = Modifier) {
    var showPassword by remember { mutableStateOf(false) }

    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp), color = Color.White) {
        Column(Modifier.padding(20.dp)) {
            Text(if (state.registerMode) "Create your account" else "Welcome back", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(
                if (state.registerMode) "Use an email you can access. Your college record is linked after verification." else "Sign in to continue to your academic workspace.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(16.dp))
            CmsTextField(
                value = state.email,
                onValueChange = actions.onEmailChange,
                label = "Email address",
                supportingText = if (state.registerMode) "Use your personal or college email address." else null,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            CmsTextField(
                value = state.password,
                onValueChange = actions.onPasswordChange,
                label = "Password",
                isPassword = !showPassword,
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(if (showPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password visibility")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            CmsPrimaryButton(
                text = if (state.loading) "Please wait..." else if (state.registerMode) "Create account" else "Sign in",
                onClick = actions.onSubmit,
                enabled = !state.loading && state.email.isNotBlank() && state.password.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.loading) {
                Spacer(Modifier.height(8.dp))
                CircularProgressIndicator(modifier = Modifier.height(20.dp), strokeWidth = 2.dp)
            }
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { actions.onModeChange(!state.registerMode) }) {
                Text(if (state.registerMode) "Login instead" else "Register instead")
            }
            if (!state.registerMode) {
                TextButton(onClick = actions.onPasswordReset) { Text("Forgot password?") }
            }
        }
    }
}

@Composable
private fun AuthStatusCard(message: String, error: Boolean) {
    val color = if (error) AuthRed else AuthGreen
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f)) {
        Text(message, modifier = Modifier.padding(14.dp), color = color, style = MaterialTheme.typography.bodyMedium)
    }
}
