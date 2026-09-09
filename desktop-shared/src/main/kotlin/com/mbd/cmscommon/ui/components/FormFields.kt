package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
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
import java.time.LocalTime
import java.time.ZoneOffset

data class CmsEntityOption(
    val id: String,
    val label: String,
    val supportingText: String? = null,
)

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
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

/** A time field backed by Material3's clock-face [TimePicker] (12-hour, AM/PM), storing "HH:MM" (24h). */
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CmsTimeField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
) {
    var showPicker by remember { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("Select time") },
            trailingIcon = { Icon(Icons.Outlined.AccessTime, contentDescription = "Choose $label") },
            isError = isError,
            singleLine = true,
            shape = RectangleShape,
        )
        Box(Modifier.matchParentSize().clickable(onClickLabel = "Choose $label") { showPicker = true })
    }

    if (showPicker) {
        val initial = runCatching { LocalTime.parse(value.trim()) }.getOrNull() ?: LocalTime.of(8, 0)
        val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = false)
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text(label) },
            text = { TimePicker(state = state) },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange("%02d:%02d".format(state.hour, state.minute))
                    showPicker = false
                }) { Text("Select") }
            },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } },
        )
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
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
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

/**
 * Linked building/room pickers: picking a room back-fills its building, and changing the building
 * clears a room that no longer belongs to it. [onChange] always reports the full, self-consistent
 * (buildingId, buildingName, roomId, roomNo) result of either interaction.
 */
@Composable
fun CmsBuildingRoomPicker(
    buildings: List<com.mbd.cmscommon.domain.model.Building>,
    rooms: List<com.mbd.cmscommon.domain.model.Room>,
    selectedBuildingId: String?,
    selectedRoomId: String?,
    onChange: (buildingId: String?, buildingName: String?, roomId: String?, roomNo: String?) -> Unit,
    modifier: Modifier = Modifier,
    buildingOptional: Boolean = true,
    buildingLabel: String = "Building",
    roomLabel: String = "Room",
    buildingEmptyLabel: String = "Any building",
    roomEmptyLabel: String = "Not assigned",
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CmsEntityPicker(
            label = buildingLabel,
            selectedId = selectedBuildingId,
            options = buildings.map { CmsEntityOption(it.buildingId, it.name) },
            onSelected = { id ->
                val name = buildings.firstOrNull { it.buildingId == id }?.name
                val currentRoom = rooms.firstOrNull { it.roomId == selectedRoomId }
                val keepRoom = currentRoom != null && currentRoom.buildingId == id
                onChange(id, name, if (keepRoom) selectedRoomId else null, if (keepRoom) currentRoom?.roomNo else null)
            },
            optional = buildingOptional,
            emptyLabel = buildingEmptyLabel,
            modifier = Modifier.weight(1f),
        )
        CmsEntityPicker(
            label = roomLabel,
            selectedId = selectedRoomId,
            options = rooms.filter { selectedBuildingId == null || it.buildingId == selectedBuildingId }
                .map { CmsEntityOption(it.roomId, it.roomNo, it.name) },
            onSelected = { id ->
                val picked = rooms.firstOrNull { it.roomId == id }
                val buildingId = picked?.buildingId ?: selectedBuildingId
                val buildingName = buildings.firstOrNull { it.buildingId == buildingId }?.name
                onChange(buildingId, buildingName, id, picked?.roomNo)
            },
            optional = true,
            emptyLabel = roomEmptyLabel,
            modifier = Modifier.weight(1f),
        )
    }
}
