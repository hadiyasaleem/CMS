package com.mbd.cmsteacher.feature.attendance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.AttendanceHistoryWorkspace

@Composable
fun AttendanceHistoryScreen(
    onOpenStudent: (sessionId: String, courseCode: String, rollNumber: String) -> Unit,
    viewModel: AttendanceHistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val month by viewModel.month.collectAsState()
    val monthLabel by viewModel.monthLabel.collectAsState()
    val pendingCells by viewModel.pendingCells.collectAsState()
    val requestState by viewModel.requestState.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val roster by viewModel.roster.collectAsState()
    val marks by viewModel.marks.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    AttendanceHistoryWorkspace(
        courseCode = viewModel.courseCode,
        month = month,
        monthLabel = monthLabel,
        loading = loading,
        roster = roster,
        marks = marks,
        pendingCells = pendingCells,
        requestState = requestState,
        onOpenStudent = { roll -> onOpenStudent(viewModel.sessionId, viewModel.courseCode, roll) },
        onSubmitEditRequest = viewModel::submitEditRequest,
        onRequestStateConsumed = viewModel::consumeRequestState,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onExport = { format -> viewModel.export(context, format) },
        errorMessage = errorMessage,
        onClearError = viewModel::clearError,
    )
}
