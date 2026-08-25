package com.mbd.cmscommon.ui.root

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.UserRole

@Composable
fun RoleGatedRoot(
    role: UserRole?,
    isAccepted: (UserRole) -> Boolean,
    wrongRoleMessage: String,
    onSignOut: () -> Unit,
    loginScreen: @Composable () -> Unit,
    content: @Composable (UserRole) -> Unit,
) {
    when {
        role == null -> loginScreen()
        isAccepted(role) -> content(role)
        else -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(wrongRoleMessage)
                Button(onClick = onSignOut, modifier = Modifier.padding(top = 16.dp)) {
                    Text("Sign out")
                }
            }
        }
    }
}
