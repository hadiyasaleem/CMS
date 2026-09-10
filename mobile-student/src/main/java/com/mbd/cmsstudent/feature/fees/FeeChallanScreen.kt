package com.mbd.cmsstudent.feature.fees

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.mbd.cmscommon.ui.components.StudentFeeWorkspace
import com.mbd.cmscommon.util.FeeChallanPdfGenerator
import com.mbd.cmscommon.util.FileOpener
import java.io.File

@Composable
fun FeeChallanScreen(viewModel: FeeChallanViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val snapshot by viewModel.snapshot.collectAsState()
    val header by viewModel.header.collectAsState()
    val error by viewModel.error.collectAsState()

    StudentFeeWorkspace(
        header = header,
        snapshot = snapshot,
        loading = snapshot == null,
        errorMessage = error,
        onRetry = viewModel::refresh,
        onDownloadPdf = {
            val currentHeader = header
            val structure = snapshot?.structure
            if (currentHeader != null && structure != null) {
                val bytes = FeeChallanPdfGenerator.generate(currentHeader, structure)
                val file = File(context.cacheDir, "${currentHeader.challanNumber}.pdf")
                file.writeBytes(bytes)
                FileOpener.open(context, file, "application/pdf")
            }
        },
    )
}
