package com.mbd.cmscommon.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun FileUploadPicker(
    label: String,
    onFilePicked: (Uri) -> Unit,
    modifier: Modifier = Modifier,
    mimeTypes: Array<String> = arrayOf(
        "application/pdf",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    ),
) {
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) onFilePicked(uri)
    }
    Button(onClick = { launcher.launch(mimeTypes) }, modifier = modifier) {
        Text(label)
    }
}
