package com.mbd.cmsteacher.feature.exams

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.ExamPaperSubmissionWorkspace
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.FileOpener
import kotlinx.coroutines.launch

@Composable
fun ExamPaperSubmissionScreen(viewModel: ExamPaperSubmissionViewModel = hiltViewModel()) {
    val controller = viewModel.controller
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val assignments by viewModel.assignments.collectAsState()
    val selected by controller.selected.collectAsState()
    val examType by controller.examType.collectAsState()
    val submissions by controller.submissions.collectAsState()
    val uploadState by controller.uploadState.collectAsState()

    val pickFile = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            val resolver = context.contentResolver
            var name = "paper.pdf"
            resolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) name = cursor.getString(index)
            }
            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() } ?: return@launch
            controller.upload(bytes, name)
        }
    }

    ExamPaperSubmissionWorkspace(
        assignments = assignments,
        selected = selected,
        examType = examType,
        submissions = submissions,
        outcome = uploadState ?: Outcome.Success(Unit),
        onSelect = controller::select,
        onExamType = controller::selectExamType,
        onChooseFile = { pickFile.launch("application/pdf") },
        onOpen = { submission ->
            controller.downloadAndOpen(submission, context.cacheDir) { file ->
                FileOpener.open(context, file, "application/pdf")
            }
        },
        onDelete = controller::deleteSubmission,
    )
}
