package com.mbd.cmscommon.ui.datesheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DatesheetBrowseController
import com.mbd.cmscommon.controller.DatesheetEditorController
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Admin/teacher Datesheets screen support: the browse cascade plus whichever datesheet is open. */
@HiltViewModel
class DatesheetsViewModel @Inject constructor(
    private val datesheetRepository: DatesheetRepository,
    private val sessionRepository: AcademicSessionRepository,
    departmentRepository: DepartmentRepository,
    private val curriculumRepository: CurriculumRepository,
    private val teacherRepository: TeacherRepository,
    private val buildingRepository: BuildingRepository,
    private val roomRepository: RoomRepository,
    userRepository: UserRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val browseController = DatesheetBrowseController(datesheetRepository, sessionRepository, departmentRepository, viewModelScope)

    val viewer: StateFlow<DatesheetViewerContext> = userRepository.observeCurrentUserRole()
        .map { role -> buildViewerContext(role) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DatesheetViewerContext(DatesheetViewerRole.TEACHER))

    val allSlots: StateFlow<List<DatesheetSlot>> =
        datesheetRepository.observeAllSlots().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val buildings: StateFlow<List<Building>> =
        buildingRepository.observeActiveBuildings().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _openDatesheetId = MutableStateFlow<String?>(null)
    val openDatesheetId: StateFlow<String?> = _openDatesheetId.asStateFlow()

    private val _editorController = MutableStateFlow<DatesheetEditorController?>(null)
    val editorController: StateFlow<DatesheetEditorController?> = _editorController.asStateFlow()

    fun openDatesheet(id: String?) {
        _openDatesheetId.value = id
        _editorController.value = id?.let {
            DatesheetEditorController(it, datesheetRepository, sessionRepository, curriculumRepository, teacherRepository, buildingRepository, roomRepository, viewModelScope)
        }
    }

    fun createDatesheet(sessionId: String, semester: Int, defaultStart: String?, defaultEnd: String?, defaultBuildingId: String?, instructions: String?) {
        viewModelScope.launch {
            runCatching {
                browseController.createDatesheet(sessionId, semester, defaultStart, defaultEnd, defaultBuildingId, instructions, sessionManager.accountKey.orEmpty())
            }.onSuccess { id -> openDatesheet(id) }
        }
    }

    private fun buildViewerContext(role: UserRole?): DatesheetViewerContext = when (role) {
        is UserRole.Teacher -> DatesheetViewerContext(DatesheetViewerRole.TEACHER, canManage = role.permissions.canManageDatesheets, identityKey = role.teacherId)
        is UserRole.Admin -> DatesheetViewerContext(DatesheetViewerRole.ADMIN, canManage = true)
        else -> DatesheetViewerContext(DatesheetViewerRole.TEACHER)
    }
}
