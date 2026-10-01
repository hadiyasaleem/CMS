package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.FeeStructuresController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.FeeStructuresWorkspace
import com.mbd.cmsdesktop.platform.rememberDocumentExport

/** Records > Fee structures: the college-wide base per shift, then every class's fees in one filterable grid. */
@Composable
fun FeeStructuresScreen(
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    onEditCollege: (Session) -> Unit,
    onOpenClass: (sessionId: String, shift: Session) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(feeRepository, sessionRepository, departmentRepository) {
        FeeStructuresController(feeRepository, sessionRepository, departmentRepository, scope)
    }
    val base by controller.base.collectAsState()
    val rows by controller.rows.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()
    val filterScope by controller.filterScope.collectAsState()
    val filterOptions by controller.filterOptions.collectAsState()
    val programType by controller.programType.collectAsState()
    val semester by controller.semester.collectAsState()
    val export = rememberDocumentExport()

    // Coming back from editing the base or a class: show what was just saved.
    LaunchedEffect(controller) { controller.refresh(fetchRemote = false) }

    FeeStructuresWorkspace(
        base = base,
        rows = rows,
        loading = loading,
        errorMessage = error,
        filterScope = filterScope,
        filterOptions = filterOptions,
        programType = programType,
        semester = semester,
        onFilterScope = controller::setFilterScope,
        onProgramType = controller::setProgramType,
        onSemester = controller::setSemester,
        onEditCollege = onEditCollege,
        onOpenClass = onOpenClass,
        onRetry = { controller.refresh() },
        onExport = { format -> export(controller.exportDocument(), format) },
    )
}
