package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import java.util.Locale

@Composable
fun NavyBrandPanel(collegeName: String, description: String, systemLabel: String = "", modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth().background(CmsTheme.colors.ink).padding(horizontal = 28.dp, vertical = 32.dp),
    ) {
        Column {
            Text(collegeName, color = CmsTheme.colors.onInk, style = MaterialTheme.typography.headlineMedium)
            Box(
                Modifier.padding(top = 12.dp).width(40.dp).height(2.dp).background(CmsTheme.colors.accent),
            )
            Spacer(Modifier.height(14.dp))
            Text(description, color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
            if (systemLabel.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
                Text(systemLabel.uppercase(), color = CmsTheme.colors.onInkMuted, style = CmsTextStyles.eyebrow)
            }
        }
    }
}

@Composable
fun CmsTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    supportingText: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
) {
    Column(modifier.fillMaxWidth()) {
        if (label.isNotBlank()) {
            Text(
                label.lowercase(Locale.ROOT),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
            )
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            isError = isError,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            placeholder = placeholder?.let { { Text(it, color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodyLarge) } },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            supportingText = supportingText?.let {
                {
                    Text(it, color = if (isError) MaterialTheme.colorScheme.error else CmsTheme.colors.muted)
                }
            },
            shape = RectangleShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                focusedBorderColor = MaterialTheme.colorScheme.onSurface,
                unfocusedBorderColor = MaterialTheme.colorScheme.onSurface,
                errorBorderColor = MaterialTheme.colorScheme.error,
            ),
        )
    }
}
