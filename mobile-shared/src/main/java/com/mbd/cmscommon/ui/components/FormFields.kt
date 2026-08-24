package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.matchParentSize
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

data class CmsEntityOption(
    val id: String,
    val label: String,
    val supportingText: String? = null,
)

@Composable
fun CmsDateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    optional: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text(if (optional) "Optional" else "Select date") },
            trailingIcon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Choose $label") },
            supportingText = supportingText?.let { { Text(it) } },
            isError = isError,
            singleLine = true,
            shape = RectangleShape,
        )
        Box(
            Modifier.matchParentSize().clickable(onClickLabel = "Choose $label") { showPicker = true },
        )
    }

    if (showPicker) {
        val initialMillis = toDatePickerMillis(value)
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { onValueChange(toIsoDate(it)) }
                        showPicker = false
                    },
                    enabled = state.selectedDateMillis != null,
                ) { Text("Select") }
            },
            dismissButton = {
                Row {
                    if (optional && value.isNotBlank()) {
                        TextButton(onClick = { onValueChange(""); showPicker = false }) { Text("Clear") }
                    }
                    TextButton(onClick = { showPicker = false }) { Text("Cancel") }
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
fun CmsEntityPicker(
    label: String,
    selectedId: String?,
    options: List<CmsEntityOption>,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    optional: Boolean = false,
    emptyLabel: String = "None",
    error: String? = null,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = options.firstOrNull { it.id == selectedId }

    Column(modifier.fillMaxWidth()) {
        Text(label)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                shape = RectangleShape,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        selected?.label ?: emptyLabel,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    selected?.supportingText?.let {
                        Text(it, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                if (optional) {
                    DropdownMenuItem(
                        text = { Text(emptyLabel) },
                        onClick = { onSelected(null); expanded = false },
                    )
                }
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(option.label)
                                option.supportingText?.let { Text(it) }
                            }
                        },
                        onClick = { onSelected(option.id); expanded = false },
                    )
                }
            }
        }
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error)
        }
    }
}

private fun toDatePickerMillis(value: String): Long? =
    runCatching { LocalDate.parse(value.trim()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli() }.getOrNull()

private fun toIsoDate(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
