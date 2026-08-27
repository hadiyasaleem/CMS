package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.DepartmentPortfolioStats
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.util.Locale

@Composable
fun DepartmentPortfolio(
    departments: List<Department>,
    stats: Map<String, DepartmentPortfolioStats>,
    heroPainter: Painter,
    onOpenDepartment: (String) -> Unit,
    onEditDepartment: (Department) -> Unit,
    onDeleteDepartment: (Department) -> Unit,
    onAddDepartment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }

    val visible = departments.filter { dept ->
        query.isBlank() ||
            dept.name.contains(query, ignoreCase = true) ||
            dept.code.contains(query, ignoreCase = true) ||
            (dept.hodEmail ?: "").contains(query, ignoreCase = true)
    }.sortedBy { it.name.lowercase(Locale.ROOT) }

    val totalStudents = stats.values.sumOf { it.studentCount }
    val totalSessions = stats.values.sumOf { it.activeSessions }
    val totalCapacity = stats.values.sumOf { it.totalCapacity }
    val occupancy = if (totalCapacity == 0) 0f else (totalStudents * 100f) / totalCapacity
    val departmentsWithHod = departments.count { !it.hodEmail.isNullOrBlank() }

    CardGrid(modifier.fillMaxWidth()) {
        fullSpanItem { DepartmentHero(heroPainter, departments.size, onAddDepartment) }
        fullSpanItem { PortfolioSummary(departments.size, totalStudents, totalSessions, occupancy, departmentsWithHod) }
        fullSpanItem {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, code, or HOD email") },
                singleLine = true,
            )
        }
        if (departments.isEmpty()) {
            fullSpanItem { FilteredDepartmentEmptyState(hasQuery = false, onAdd = onAddDepartment, onClear = {}) }
        } else if (visible.isEmpty()) {
            fullSpanItem { FilteredDepartmentEmptyState(hasQuery = true, onAdd = onAddDepartment, onClear = { query = "" }) }
        } else {
            items(visible, key = { it.deptId }) { department ->
                DepartmentPortfolioCard(
                    department = department,
                    stats = stats[department.deptId] ?: DepartmentPortfolioStats(),
                    onOpen = { onOpenDepartment(department.deptId) },
                    onEdit = { onEditDepartment(department) },
                    onDelete = { onDeleteDepartment(department) },
                )
            }
        }
        fullSpanItem { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun DepartmentHero(heroPainter: Painter, departmentCount: Int, onAdd: () -> Unit) {
    Surface(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(22.dp), color = Color(0xFFFFFBF3), border = BorderStroke(1.dp, Color(0xFFE5DED2))) {
        Box(Modifier.fillMaxSize()) {
            Image(
                painter = heroPainter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.CenterEnd,
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize().background(
                    Brush.horizontalGradient(
                        0f to Color(0xFFFFFBF3),
                        0.6f to Color(0xFFFFFBF3).copy(alpha = 0.92f),
                        1f to Color.Transparent,
                    ),
                ),
            )
            Row(Modifier.align(Alignment.CenterStart).padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.fillMaxWidth(0.6f)) {
                    Text("ACADEMIC STRUCTURE", color = Color(0xFF716B64), style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Text("Department portfolio", color = Color(0xFF252321), fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(4.dp))
                    Text("$departmentCount departments across the college", color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
                }
            }
            CmsPrimaryButton(text = "Add department", onClick = onAdd, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp))
        }
    }
}

@Composable
private fun PortfolioSummary(departmentCount: Int, totalStudents: Int, totalSessions: Int, occupancy: Float, departmentsWithHod: Int) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        DepartmentMetric("Departments", departmentCount.toString(), Modifier.weight(1f))
        DepartmentMetric("Students", totalStudents.toString(), Modifier.weight(1f))
        DepartmentMetric("Sessions", totalSessions.toString(), Modifier.weight(1f))
        DepartmentMetric("HOD assigned", "$departmentsWithHod / $departmentCount", Modifier.weight(1f))
    }
}

@Composable
private fun DepartmentMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(Locale.ROOT), color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun DepartmentPortfolioCard(
    department: Department,
    stats: DepartmentPortfolioStats,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val hasHod = !department.hodEmail.isNullOrBlank()
    val semesterSummary = if (stats.minimumSemester != null && stats.maximumSemester != null) {
        "Sem ${stats.minimumSemester}-${stats.maximumSemester}"
    } else {
        "No active sessions"
    }

    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(department.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Code ${department.code}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                StatusBadge(if (hasHod) "HOD ASSIGNED" else "HOD NOT ASSIGNED", if (hasHod) BadgeTone.Success else BadgeTone.Warning)
                Box {
                    IconButton(onClick = { menuExpanded = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { menuExpanded = false; onEdit() })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menuExpanded = false; onDelete() })
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(department.description?.takeIf { it.isNotBlank() } ?: "No department description added yet.", color = Color(0xFF625E58), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge("${stats.studentCount} students", BadgeTone.Neutral)
                StatusBadge("${stats.activeSessions} sessions", BadgeTone.Neutral)
                StatusBadge(semesterSummary, BadgeTone.Navy)
            }
            Spacer(Modifier.height(10.dp))
            TextButton(onClick = onOpen) { Text("Open ${department.name}") }
        }
    }
}

@Composable
private fun FilteredDepartmentEmptyState(hasQuery: Boolean, onAdd: () -> Unit, onClear: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (hasQuery) "No matching departments" else "No departments yet", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (hasQuery) "Try another name, code, or HOD email." else "Create the first department to begin the academic structure.",
                color = Color(0xFF77716A),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = if (hasQuery) "Clear search" else "Add department", onClick = if (hasQuery) onClear else onAdd)
        }
    }
}
