package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

data class PickerOption(
    val id: String,
    val label: String,
)

@Composable
fun DeptSemesterSessionSubjectPicker(
    departments: List<PickerOption>,
    offerings: List<PickerOption>,
    subjects: List<PickerOption>,
    selectedDeptId: String?,
    selectedOfferingId: String?,
    selectedSubjectId: String?,
    onDeptSelected: (String) -> Unit,
    onOfferingSelected: (String) -> Unit,
    onSubjectSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        PickerDropdown("Department", departments, selectedDeptId, onDeptSelected)
        if (selectedDeptId != null) {
            PickerDropdown(
                "Semester / Session",
                offerings,
                selectedOfferingId,
                onOfferingSelected,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        if (selectedOfferingId != null) {
            PickerDropdown(
                "Subject",
                subjects,
                selectedSubjectId,
                onSubjectSelected,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun PickerDropdown(
    label: String,
    options: List<PickerOption>,
    selectedId: String?,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label ?: ""

    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedLabel,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                label = { Text(label) },
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) },
            )
            Box(Modifier.matchParentSize().clickable { expanded = true })
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = { onSelected(option.id); expanded = false },
                )
            }
        }
    }
}
