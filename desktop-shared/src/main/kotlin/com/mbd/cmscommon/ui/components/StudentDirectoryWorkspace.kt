package com.mbd.cmscommon.ui.components

import com.mbd.cmscommon.controller.departmentScopeOptions
import com.mbd.cmscommon.domain.model.ShiftScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StudentAccountFilter
import com.mbd.cmscommon.controller.StudentDirectoryPage
import com.mbd.cmscommon.controller.StudentDirectoryQuery
import com.mbd.cmscommon.controller.StudentDirectoryRow
import com.mbd.cmscommon.controller.StudentDirectorySort
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.util.Locale

private fun pretty(raw: String): String = raw.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }

@Composable
fun StudentDirectoryWorkspace(
    page: StudentDirectoryPage,
    query: StudentDirectoryQuery,
    departments: List<Department>,
    sessions: List<AcademicSession>,
    enrollmentStatuses: List<String>,
    loaded: Boolean,
    errorMessage: String?,
    onSearch: (String) -> Unit,
    onDepartment: (String?) -> Unit,
    onSession: (String?) -> Unit,
    onShift: (Session?) -> Unit,
    /** The shared Department -> Session -> Shift filter. */
    onScope: (ShiftScope) -> Unit,
    onEnrollmentStatus: (String?) -> Unit,
    onAccount: (StudentAccountFilter) -> Unit,
    onSort: (StudentDirectorySort) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onClearFilters: () -> Unit,
    onClearError: () -> Unit,
    onOpenStudent: (StudentDirectoryRow) -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    buildExport: () -> ExportDocument,
    modifier: Modifier = Modifier,
) {
    val deptNames = departments.associate { it.deptId to it.name }
    val sessionOptions = sessions
        .filter { query.deptId == null || it.deptId == query.deptId }
        .sortedWith(compareBy({ it.deptId }, { -it.startYear }))

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ModGround),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { DirectoryHero(page.totalStudents) }
        item { ExportBar(onExport, build = buildExport, enabled = page.matches.isNotEmpty()) }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, onDismiss = onClearError) }
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = query.search,
                    onValueChange = onSearch,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search name, roll, university roll, email", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                ShiftScopeSelector(query.scope, departmentScopeOptions(departments), sessions, onScope, label = "FILTER")
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterDropdown("Status", query.enrollmentStatus, listOf(null to "Any status") + enrollmentStatuses.map { it to pretty(it) }, onEnrollmentStatus)
                    FilterDropdown("Account", query.account, StudentAccountFilter.entries.map { it to it.label }, { it?.let(onAccount) })
                }
                Spacer(Modifier.height(8.dp))
                Text("SORT", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StudentDirectorySort.entries.forEach { option -> CmsChip(option.label, selected = query.sort == option, onClick = { onSort(option) }) }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (page.matches.isEmpty()) "No students" else "Showing ${page.firstIndex}-${page.lastIndex} of ${page.matches.size}" +
                        if (query.hasFilters) " (filtered from ${page.totalStudents})" else "",
                    modifier = Modifier.weight(1f),
                    color = ModMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
                if (query.hasFilters) TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
        when {
            !loaded -> items(5) { SkeletonRow() }
            page.totalStudents == 0 -> item { DirectoryEmpty("No students are cached on this device yet. Refresh to download rosters.") }
            page.rows.isEmpty() -> item { DirectoryEmpty("No students match these filters.") }
            else -> items(page.rows, key = { it.profile.sessionId + "/" + it.profile.rollNumber }) { row -> DirectoryCard(row, onClick = { onOpenStudent(row) }) }
        }
        if (page.pageCount > 1) {
            item { PageControls(page, onPreviousPage, onNextPage) }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
private fun DirectoryHero(total: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.fillMaxWidth().padding(20.dp)) {
            Text("PEOPLE", color = ModWarn, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Student Rosters", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$total students across all sessions on this device", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun <T> FilterDropdown(label: String, selected: T?, options: List<Pair<T?, String>>, onSelect: (T?) -> Unit) {
    var open by remember { mutableStateOf(false) }
    val current = options.firstOrNull { it.first == selected }?.second
    val active = selected != null && options.firstOrNull()?.first != selected
    Box {
        CmsChip(if (active) current ?: label else label, selected = active, onClick = { open = true })
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { (value, text) ->
                DropdownMenuItem(text = { Text(text) }, onClick = { open = false; onSelect(value) })
            }
        }
    }
}

@Composable
private fun DirectoryCard(row: StudentDirectoryRow, onClick: () -> Unit) {
    val p = row.profile
    val sessionText = listOfNotNull(
        row.departmentName ?: row.session?.deptId?.uppercase(Locale.ROOT),
        row.session?.label,
        p.shift.label,
        row.session?.currentSemester?.let { "Sem $it" },
    ).joinToString(" · ")
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(ModInk), contentAlignment = Alignment.Center) {
                Text(p.name.trim().take(1).uppercase(), color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(p.rollNumber, color = ModMuted, style = MaterialTheme.typography.bodySmall)
                if (sessionText.isNotBlank()) Text(sessionText, color = ModMuted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Column(horizontalAlignment = Alignment.End) {
                StatusBadge(
                    p.enrollmentStatus.uppercase(),
                    if (p.enrollmentStatus.equals("ACTIVE", ignoreCase = true)) BadgeTone.Success else BadgeTone.Neutral,
                )
                Spacer(Modifier.height(4.dp))
                Text(p.cgpa?.let { "CGPA %.2f".format(Locale.ENGLISH, it) } ?: "No CGPA", color = ModMuted, style = MaterialTheme.typography.labelSmall)
                if (p.linkedEmail.isBlank()) Text("Not linked", color = ModWarn, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun PageControls(page: StudentDirectoryPage, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onPrevious, enabled = page.page > 0) { Text("‹ Previous") }
        Text("Page ${page.page + 1} of ${page.pageCount}", style = MaterialTheme.typography.bodyMedium)
        TextButton(onClick = onNext, enabled = page.page < page.pageCount - 1) { Text("Next ›") }
    }
}

@Composable
private fun DirectoryEmpty(message: String) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Text(message, modifier = Modifier.fillMaxWidth().padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
    }
}
