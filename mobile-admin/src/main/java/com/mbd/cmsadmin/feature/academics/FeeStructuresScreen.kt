package com.mbd.cmsadmin.feature.academics

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.FeeStructuresController
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.ui.components.FeeStructuresWorkspace
import com.mbd.cmscommon.util.rememberDocumentExport
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FeeStructuresViewModel @Inject constructor(
    feeRepository: SessionFeeRepository,
    sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
) : ViewModel() {
    val controller = FeeStructuresController(feeRepository, sessionRepository, departmentRepository, viewModelScope)
}

/** Records > Fee structures: the college-wide base per shift, then every class's fees in one filterable grid. */
@Composable
fun FeeStructuresScreen(
    onEditCollege: (Session) -> Unit,
    onOpenClass: (sessionId: String, shift: Session) -> Unit,
    viewModel: FeeStructuresViewModel = hiltViewModel(),
) {
    val controller = viewModel.controller
    val base by controller.base.collectAsState()
    val grids by controller.grids.collectAsState()
    val loading by controller.loading.collectAsState()
    val error by controller.error.collectAsState()
    val filterScope by controller.filterScope.collectAsState()
    val filterOptions by controller.filterOptions.collectAsState()
    val programType by controller.programType.collectAsState()
    val export = rememberDocumentExport()

    // Coming back from editing the base or a class: show what was just saved.
    LaunchedEffect(Unit) { controller.refresh(fetchRemote = false) }

    FeeStructuresWorkspace(
        base = base,
        grids = grids,
        loading = loading,
        errorMessage = error,
        filterScope = filterScope,
        filterOptions = filterOptions,
        programType = programType,
        onFilterScope = controller::setFilterScope,
        onProgramType = controller::setProgramType,
        onEditCollege = onEditCollege,
        onOpenClass = onOpenClass,
        onRetry = { controller.refresh() },
        onExport = { format -> export(controller.exportDocument(), format) },
    )
}
