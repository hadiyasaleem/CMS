package com.mbd.cmscommon.ui.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.FeeGrid
import com.mbd.cmscommon.controller.FeeSource
import com.mbd.cmscommon.controller.ScopeFilterOptions
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.feeGridExport
import com.mbd.cmscommon.export.feeStructuresExport
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import java.util.Locale

private val GridDeptWidth = 190.dp
private val GridHeadWidth = 96.dp
private val GridTotalWidth = 90.dp
private val GridSourceWidth = 110.dp
private val GridRowHeight = 52.dp

private fun money(value: Double) = "%,.0f".format(Locale.ENGLISH, value)

private fun planLabel(type: FeeType) = if (type == FeeType.ANNUAL) "annual" else "per semester"

/**
 * Every class's fees on one screen. At the top, the college-wide base for each shift (Morning and Evening can
 * differ) applies to all classes until one gets a structure of its own; below, filters and, like the master
 * timetable, one grid per semester+program+shift with one row per department and a column per fee head.
 */
@Composable
fun FeeStructuresWorkspace(
    base: List<SessionFeeStructure>,
    grids: List<FeeGrid>,
    loading: Boolean,
    errorMessage: String?,
    filterOptions: ScopeFilterOptions,
    availableSemesters: List<Int>,
    selectedDeptId: String?,
    selectedSemester: Int?,
    selectedShift: Session?,
    selectedProgramType: ProgramType?,
    onSelectDepartment: (String?) -> Unit,
    onSelectSemester: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onSelectProgramType: (ProgramType?) -> Unit,
    onClearFilters: () -> Unit,
    onEditCollege: (Session) -> Unit,
    onOpenClass: (sessionId: String, shift: Session) -> Unit,
    onRetry: () -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasRows = grids.any { it.rows.isNotEmpty() }
    val anyFilterActive = selectedDeptId != null || selectedSemester != null || selectedShift != null || selectedProgramType != null

    TopBarActions {
        ExportMenuButton(
            onExport = { format -> onExport(feeStructuresExport(base, grids.flatMap { it.rows }), format) },
            enabled = hasRows && !loading,
            tint = CmsTheme.colors.onInk,
        )
    }

    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry) }
        }

        item { Text("COLLEGE-WIDE BASE", color = ModMuted, style = CmsTextStyles.eyebrow) }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Session.entries.forEach { shift ->
                    BaseCard(shift, base.firstOrNull { it.shift == shift }, { onEditCollege(shift) }, Modifier.weight(1f))
                }
            }
        }

        item {
            FeeFilterBar(
                departments = filterOptions.departments,
                availableSemesters = availableSemesters,
                selectedDeptId = selectedDeptId,
                selectedSemester = selectedSemester,
                selectedShift = selectedShift,
                selectedProgramType = selectedProgramType,
                anyFilterActive = anyFilterActive,
                onSelectDepartment = onSelectDepartment,
                onSelectSemester = onSelectSemester,
                onSelectShift = onSelectShift,
                onSelectProgramType = onSelectProgramType,
                onClearFilters = onClearFilters,
            )
        }

        when {
            loading && grids.isEmpty() -> items(3) { SkeletonRow() }
            !hasRows -> item {
                Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
                    Text("No classes match these filters.", modifier = Modifier.padding(24.dp), color = ModMuted, style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
                grids.forEach { grid -> item { FeeGridSection(grid, onOpenClass, onExport) } }
                item {
                    Text(
                        "Select a row to change that class's fees. Classes marked College base follow the structure above; saving a change gives the class its own.",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
private fun BaseCard(shift: Session, fee: SessionFeeStructure?, onEdit: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(shift.label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(4.dp))
            if (fee == null) {
                Text("Not set", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Classes have no fees until you set it or give them their own.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                Text("Rs ${money(fee.totalAmount)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("${planLabel(fee.cadence)} · ${fee.heads.size} fee head(s)", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onEdit) { Text(if (fee == null) "Set up" else "Edit") }
        }
    }
}

/** Department/Semester/Shift/Program dropdown filters, identical in layout and behaviour to the master
 * timetable's own filter bar ([MasterFilterBar]) -- each defaults to "All" and narrows independently. */
@Composable
private fun FeeFilterBar(
    departments: List<Pair<String, String>>,
    availableSemesters: List<Int>,
    selectedDeptId: String?,
    selectedSemester: Int?,
    selectedShift: Session?,
    selectedProgramType: ProgramType?,
    anyFilterActive: Boolean,
    onSelectDepartment: (String?) -> Unit,
    onSelectSemester: (Int?) -> Unit,
    onSelectShift: (Session?) -> Unit,
    onSelectProgramType: (ProgramType?) -> Unit,
    onClearFilters: () -> Unit,
) {
    val deptOptions = departments.map { CmsEntityOption(it.first, it.second) }
    val semesterOptions = availableSemesters.map { CmsEntityOption(it.toString(), "Semester $it") }
    val shiftOptions = Session.entries.map { CmsEntityOption(it.name, it.label) }
    val programOptions = ProgramType.entries.map { CmsEntityOption(it.name, it.label) }

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DropdownChip(
                    selectedLabel = deptOptions.firstOrNull { it.id == selectedDeptId }?.label,
                    emptyLabel = "All departments",
                    options = deptOptions,
                    onSelected = onSelectDepartment,
                )
                DropdownChip(
                    selectedLabel = semesterOptions.firstOrNull { it.id == selectedSemester?.toString() }?.label,
                    emptyLabel = "All semesters",
                    options = semesterOptions,
                    onSelected = { id -> onSelectSemester(id?.toIntOrNull()) },
                )
                DropdownChip(
                    selectedLabel = shiftOptions.firstOrNull { it.id == selectedShift?.name }?.label,
                    emptyLabel = "All shifts",
                    options = shiftOptions,
                    onSelected = { id -> onSelectShift(id?.let { name -> Session.entries.firstOrNull { it.name == name } }) },
                )
                DropdownChip(
                    selectedLabel = programOptions.firstOrNull { it.id == selectedProgramType?.name }?.label,
                    emptyLabel = "All programs",
                    options = programOptions,
                    onSelected = { id -> onSelectProgramType(id?.let { name -> ProgramType.entries.firstOrNull { it.name == name } }) },
                )
            }
            if (anyFilterActive) {
                TextButton(onClick = onClearFilters) { Text("Clear filters") }
            }
        }
    }
}

/** One semester+program+shift grid, mirroring the master timetable's per-grid sections: a title, then
 * one row per department (a department appears at most once per grid) and a column per fee head that
 * grid's classes actually use. */
@Composable
private fun FeeGridSection(grid: FeeGrid, onOpenClass: (String, Session) -> Unit, onExport: (ExportDocument, ExportFormat) -> Unit) {
    val headLabels = grid.rows.flatMap { row -> row.structure?.heads?.map { it.label }.orEmpty() }.distinct()
    val scroll = rememberScrollState()
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    grid.title,
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium,
                )
                ExportMenuButton(onExport = { format -> onExport(feeGridExport(grid), format) })
            }
            Column(Modifier.horizontalScroll(scroll)) {
                Row(Modifier.background(ModInk).height(40.dp), verticalAlignment = Alignment.CenterVertically) {
                    GridHead("DEPARTMENT", GridDeptWidth)
                    headLabels.forEach { GridHead(it.uppercase(), GridHeadWidth) }
                    GridHead("TOTAL", GridTotalWidth)
                    GridHead("SOURCE", GridSourceWidth)
                }
                grid.rows.forEachIndexed { index, row ->
                    val fee = row.structure
                    val byLabel = fee?.heads?.associate { it.label to it.amount }.orEmpty()
                    Row(
                        Modifier.clickable { onOpenClass(row.session.sessionId, row.shift) }.height(GridRowHeight),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.width(GridDeptWidth).padding(horizontal = 8.dp)) {
                            Text(row.departmentName, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
                            Text(row.session.label, color = ModMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
                        }
                        headLabels.forEach { label ->
                            Box(Modifier.width(GridHeadWidth).padding(horizontal = 8.dp)) {
                                Text(byLabel[label]?.let(::money) ?: "-", color = if (label in byLabel) CmsTheme.colors.ink else ModMuted, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Box(Modifier.width(GridTotalWidth).padding(horizontal = 8.dp)) {
                            Text(fee?.let { money(it.totalAmount) } ?: "-", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        }
                        Box(Modifier.width(GridSourceWidth).padding(horizontal = 8.dp)) {
                            val (text, color) = when (row.source) {
                                FeeSource.CUSTOM -> "Own structure" to ModSuccess
                                FeeSource.COLLEGE -> "College base" to ModMuted
                                FeeSource.NONE -> "Not set" to ModAccent
                            }
                            Text(text, color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (index < grid.rows.lastIndex) HorizontalDivider(color = ModTrack)
                }
            }
        }
    }
}

@Composable
private fun GridHead(text: String, width: androidx.compose.ui.unit.Dp) {
    Box(Modifier.width(width).padding(horizontal = 8.dp)) {
        Text(text, color = CmsTheme.colors.onInk, maxLines = 1, overflow = TextOverflow.Ellipsis, style = CmsTextStyles.eyebrow)
    }
}
