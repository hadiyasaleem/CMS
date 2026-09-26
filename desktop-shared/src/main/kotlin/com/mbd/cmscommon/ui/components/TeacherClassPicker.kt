package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.inScope
import com.mbd.cmscommon.controller.scopeDepartments
import com.mbd.cmscommon.controller.scopeSessions
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.ModMuted

/**
 * A teacher's class picker: the shared Department -> Session -> Shift filter over their own classes, then the
 * class itself. With nothing chosen every class is listed; each chosen level narrows the list.
 */
@Composable
fun TeacherClassPicker(
    assignments: List<ResolvedAssignment>,
    selected: ResolvedAssignment?,
    onSelect: (ResolvedAssignment) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    var scope by remember { mutableStateOf(ShiftScope.ALL) }
    val visible = remember(assignments, scope) { assignments.inScope(scope) }
    Column(modifier.fillMaxWidth()) {
        Text("MY CLASSES", color = ModMuted, style = CmsTextStyles.eyebrow)
        Spacer(Modifier.height(6.dp))
        ShiftScopeSelector(
            scope = scope,
            departments = assignments.scopeDepartments(),
            sessions = assignments.scopeSessions(),
            onScopeChange = { scope = it },
            label = null,
        )
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth()) {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
                Text(selected?.let { "${it.subjectLabel} · ${it.sessionLabel}" } ?: "Select a class", modifier = Modifier.weight(1f))
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
                if (visible.isEmpty()) DropdownMenuItem(text = { Text("No classes match these filters") }, onClick = { expanded = false }, enabled = false)
                visible.forEach { assignment ->
                    DropdownMenuItem(
                        text = { Text("${assignment.subjectLabel} · ${assignment.sessionLabel}") },
                        onClick = { onSelect(assignment); expanded = false },
                    )
                }
            }
        }
    }
}
