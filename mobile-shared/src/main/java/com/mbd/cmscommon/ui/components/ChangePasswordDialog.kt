package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.util.FieldValidators

/**
 * Asks for the current password, the new one and the new one again, then calls [onSubmit]; its callback gets null on
 * success or the sentence to show. No email or reset link is involved.
 */
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (current: String, new: String, onResult: (String?) -> Unit) -> Unit,
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var failure by remember { mutableStateOf<String?>(null) }
    var done by remember { mutableStateOf(false) }

    // Only complain about the fields once there is something to compare.
    val problem = if (new.isEmpty() && confirm.isEmpty()) null else FieldValidators.passwordChangeError(current, new, confirm)
    val canSubmit = !busy && current.isNotEmpty() && new.isNotEmpty() && confirm.isNotEmpty() &&
        FieldValidators.passwordChangeError(current, new, confirm) == null

    AlertDialog(
        onDismissRequest = { if (!busy) onDismiss() },
        title = { Text("Change password", style = MaterialTheme.typography.headlineSmall) },
        text = {
            DialogScrollBody {
                if (done) {
                    Text("Your password was changed. Use the new one the next time you sign in.", style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column {
                        OutlinedTextField(
                            value = current, onValueChange = { current = it; failure = null },
                            label = { Text("Current password") }, singleLine = true, enabled = !busy,
                            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = new, onValueChange = { new = it; failure = null },
                            label = { Text("New password") }, singleLine = true, enabled = !busy,
                            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = confirm, onValueChange = { confirm = it; failure = null },
                            label = { Text("Retype new password") }, singleLine = true, enabled = !busy,
                            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth(),
                        )
                        val shown = failure ?: problem
                        if (shown != null) {
                            Spacer(Modifier.height(8.dp))
                            Text(shown, color = CmsTheme.colors.accent, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (done) {
                TextButton(onClick = onDismiss) { Text("Done") }
            } else {
                TextButton(
                    onClick = {
                        busy = true
                        failure = null
                        onSubmit(current, new) { result ->
                            busy = false
                            if (result == null) done = true else failure = result
                        }
                    },
                    enabled = canSubmit,
                ) { Text(if (busy) "Changing..." else "Change password") }
            }
        },
        dismissButton = if (done) null else {
            { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } }
        },
    )
}
