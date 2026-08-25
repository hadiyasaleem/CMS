package com.mbd.cmsdesktop.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTheme

/**
 * Shown when a signed-in student account has not yet been linked to a real student record.
 * Desktop deliberately excludes the link-request submission flow (mobile-only, see
 * `LinkRequestScreen`/`LinkRequestViewModel` in mobile-student) - this screen just explains that
 * and offers sign-out.
 */
@Composable
fun UnlinkedStudentScreen(onSignOut: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                Icons.Filled.Info,
                contentDescription = null,
                tint = CmsTheme.colors.accent,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Your account isn't linked yet",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                "This account is signed in but hasn't been linked to a student record. " +
                    "Link requests are submitted from the mobile app - install CMS Student on your " +
                    "phone and follow the \"Link my account\" flow there. Once an administrator " +
                    "approves your request, sign back in here to see your dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = CmsTheme.colors.muted,
            )
            Button(onClick = onSignOut) {
                Text("Sign out")
            }
        }
    }
}
