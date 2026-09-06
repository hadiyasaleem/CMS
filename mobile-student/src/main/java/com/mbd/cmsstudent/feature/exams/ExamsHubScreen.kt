package com.mbd.cmsstudent.feature.exams

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentExamsDestination
import com.mbd.cmscommon.ui.components.StudentExamsHubWorkspace
import com.mbd.cmsstudent.R
import com.mbd.cmsstudent.navigation.StudentDestination

@Composable
fun ExamsHubScreen(onOpen: (String) -> Unit, viewModel: StudentExamsHubViewModel = hiltViewModel()) {
    val snapshot by viewModel.snapshot.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val error by viewModel.error.collectAsState()

    StudentExamsHubWorkspace(
        heroPainter = painterResource(R.drawable.student_exams_hero),
        snapshot = snapshot,
        loading = loading,
        errorMessage = error,
        onRetry = viewModel::refresh,
        onOpen = { destination ->
            onOpen(
                when (destination) {
                    StudentExamsDestination.MARKS -> StudentDestination.Marks.route
                    StudentExamsDestination.RESULTS -> StudentDestination.Results.route
                    StudentExamsDestination.DATESHEETS -> StudentDestination.Datesheets.route
                },
            )
        },
    )
}
