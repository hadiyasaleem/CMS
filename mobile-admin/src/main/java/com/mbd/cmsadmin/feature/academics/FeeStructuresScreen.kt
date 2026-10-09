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
    val filterOptions by controller.filterOptions.collectAsState()
    val availableSemesters by controller.availableSemesters.collectAsState()
    val selectedDeptId by controller.selectedDeptId.collectAsState()
    val selectedSemester by controller.selectedSemester.collectAsState()
    val selectedShift by controller.selectedShift.collectAsState()
    val selectedProgramType by controller.selectedProgramType.collectAsState()

    // Coming back from editing the base or a class: show what was just saved.
    LaunchedEffect(Unit) { controller.refresh(fetchRemote = false) }

    FeeStructuresWorkspace(
        base = base,
        grids = grids,
        loading = loading,
        errorMessage = error,
        filterOptions = filterOptions,
        availableSemesters = availableSemesters,
        selectedDeptId = selectedDeptId,
        selectedSemester = selectedSemester,
        selectedShift = selectedShift,
        selectedProgramType = selectedProgramType,
        onSelectDepartment = controller::selectDepartment,
        onSelectSemester = controller::selectSemester,
        onSelectShift = controller::selectShift,
        onSelectProgramType = controller::selectProgramType,
        onClearFilters = controller::clearFilters,
        onEditCollege = onEditCollege,
        onOpenClass = onOpenClass,
        onRetry = { controller.refresh() },
        onExport = rememberDocumentExport(),
    )
}
