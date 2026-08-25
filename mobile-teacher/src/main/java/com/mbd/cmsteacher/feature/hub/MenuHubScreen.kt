package com.mbd.cmsteacher.feature.hub

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.TeacherMenuWorkspace
import com.mbd.cmsteacher.R

@Composable
fun MenuHubScreen(
    onOpenMyStudents: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenDocuments: () -> Unit,
    onOpenInsights: () -> Unit,
    onOpenLinkRequests: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenProfile: () -> Unit,
    onSignedOut: () -> Unit,
    viewModel: MenuViewModel = hiltViewModel(),
) {
    val snapshot by viewModel.snapshot.collectAsState()

    TeacherMenuWorkspace(
        heroPainter = painterResource(R.drawable.teacher_menu_hero),
        snapshot = snapshot,
        onOpenMyStudents = onOpenMyStudents,
        onOpenCalendar = onOpenCalendar,
        onOpenDocuments = onOpenDocuments,
        onOpenInsights = onOpenInsights,
        onOpenLinkRequests = onOpenLinkRequests,
        onOpenNotifications = onOpenNotifications,
        onOpenProfile = onOpenProfile,
        onSignOut = {
            viewModel.signOut()
            onSignedOut()
        },
    )
}
