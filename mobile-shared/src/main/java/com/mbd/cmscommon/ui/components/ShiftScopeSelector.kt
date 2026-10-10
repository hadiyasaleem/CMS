package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.DeptSemesterScope
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.ModMuted
import java.util.Locale

/**
 * The shared Department -> Session -> Shift filter. Every level is optional ("All ..."), only the chosen levels
 * apply, session choices follow the chosen department, and shift choices follow the chosen session (a
 * single-shift session offers only its shift). [departments] are (id, name) pairs; [sessions] are the sessions
 * the screen can show.
 */
@Composable
fun ShiftScopeSelector(
    scope: ShiftScope,
    departments: List<Pair<String, String>>,
    sessions: List<AcademicSession>,
    onScopeChange: (ShiftScope) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "SHOW",
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val deptOptions = departments.map { (id, name) -> CmsEntityOption(id, name) }
    val sessionChoices = ShiftScope.sessionOptions(scope, sessions)
        .sortedWith(compareByDescending<AcademicSession> { it.startYear }.thenBy { it.deptId })
    val sessionOptions = sessionChoices.map { session ->
        // Without a department, name it so "2022–2026" of IT and CS can be told apart.
        val prefix = if (scope.deptId == null) "${session.deptId.uppercase(Locale.ROOT)} " else ""
        CmsEntityOption(session.sessionId, prefix + session.label)
    }
    val shiftOptions = ShiftScope.shiftOptions(scope, sessions).map { CmsEntityOption(it.name, it.label) }

    Column(modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownChip(
                selectedLabel = deptOptions.firstOrNull { it.id == scope.deptId }?.label ?: scope.deptId,
                emptyLabel = "All departments",
                options = deptOptions,
                onSelected = { onScopeChange(scope.withDept(it, sessions)) },
            )
            DropdownChip(
                selectedLabel = sessionOptions.firstOrNull { it.id == scope.sessionId }?.label ?: scope.sessionId,
                emptyLabel = "All sessions",
                options = sessionOptions,
                onSelected = { id -> onScopeChange(scope.withSession(sessions.firstOrNull { it.sessionId == id })) },
            )
            DropdownChip(
                selectedLabel = scope.shift?.label,
                emptyLabel = "All shifts",
                options = shiftOptions,
                onSelected = { name -> onScopeChange(scope.withShift(Session.entries.firstOrNull { it.name == name })) },
            )
            trailingContent()
        }
    }
}

/**
 * The Department / Semester / Shift / Program filter bar for browse/list screens (vs.
 * [ShiftScopeSelector]'s Department -> Session -> Shift cascade, used where picking one exact academic
 * batch is the point -- a teacher's own class picker for marks entry). Every level is independent and
 * defaults to "All ..."; a "Clear filters" action appears once any is chosen -- the same look and
 * behaviour as the master timetable's own filter bar.
 */
@Composable
fun DeptSemesterScopeSelector(
    scope: DeptSemesterScope,
    departments: List<Pair<String, String>>,
    availableSemesters: List<Int>,
    onScopeChange: (DeptSemesterScope) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = "SHOW",
    /** False when the caller already renders its own "Clear filters" covering more than this scope
     * (e.g. also a teacher filter) -- avoids showing two clear actions at once. */
    showClearAction: Boolean = true,
    /** Program types to offer; defaults to every one (a BS-only screen can narrow this). */
    availableProgramTypes: List<ProgramType> = ProgramType.entries,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    val deptOptions = departments.map { (id, name) -> CmsEntityOption(id, name) }
    val semesterOptions = availableSemesters.map { CmsEntityOption(it.toString(), "Semester $it") }
    val shiftOptions = Session.entries.map { CmsEntityOption(it.name, it.label) }
    val programOptions = availableProgramTypes.map { CmsEntityOption(it.name, it.label) }

    Column(modifier.fillMaxWidth()) {
        if (label != null) {
            Text(label, color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
        }
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownChip(
                selectedLabel = deptOptions.firstOrNull { it.id == scope.deptId }?.label ?: scope.deptId,
                emptyLabel = "All departments",
                options = deptOptions,
                onSelected = { onScopeChange(scope.copy(deptId = it)) },
            )
            DropdownChip(
                selectedLabel = semesterOptions.firstOrNull { it.id == scope.semester?.toString() }?.label,
                emptyLabel = "All semesters",
                options = semesterOptions,
                onSelected = { id -> onScopeChange(scope.copy(semester = id?.toIntOrNull())) },
            )
            DropdownChip(
                selectedLabel = scope.shift?.label,
                emptyLabel = "All shifts",
                options = shiftOptions,
                onSelected = { name -> onScopeChange(scope.copy(shift = name?.let { n -> Session.entries.firstOrNull { it.name == n } })) },
            )
            DropdownChip(
                selectedLabel = programOptions.firstOrNull { it.id == scope.programType?.name }?.label,
                emptyLabel = "All programs",
                options = programOptions,
                onSelected = { name -> onScopeChange(scope.copy(programType = name?.let { n -> ProgramType.entries.firstOrNull { it.name == n } })) },
            )
            trailingContent()
        }
        if (showClearAction && !scope.isEmpty) {
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { onScopeChange(DeptSemesterScope.ALL) }) { Text("Clear filters") }
        }
    }
}
