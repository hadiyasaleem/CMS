package com.mbd.cmsadmin.feature.buildings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.BuildingsRoomsController
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.ui.components.BuildingsRoomsWorkspace
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BuildingsRoomsViewModel @Inject constructor(
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = BuildingsRoomsController(
        buildingRepository = buildingRepository,
        roomRepository = roomRepository,
        actorEmail = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val buildings = controller.buildings
    val rooms = controller.rooms
    val error = controller.error

    fun createBuilding(name: String, code: String?) = controller.createBuilding(name, code)
    fun updateBuilding(existing: com.mbd.cmscommon.domain.model.Building, name: String, code: String?) =
        controller.updateBuilding(existing, name, code)
    fun deleteBuilding(buildingId: String) = controller.deleteBuilding(buildingId)

    fun createRoom(buildingId: String, roomNo: String, name: String?, capacity: Int?, isOffice: Boolean) =
        controller.createRoom(buildingId, roomNo, name, capacity, isOffice)
    fun updateRoom(existing: com.mbd.cmscommon.domain.model.Room, roomNo: String, name: String?, capacity: Int?, isOffice: Boolean) =
        controller.updateRoom(existing, roomNo, name, capacity, isOffice)
    fun deleteRoom(roomId: String) = controller.deleteRoom(roomId)

    fun clearError() = controller.clearError()
}

@Composable
fun BuildingsRoomsScreen(viewModel: BuildingsRoomsViewModel = hiltViewModel()) {
    val buildings by viewModel.buildings.collectAsState()
    val rooms by viewModel.rooms.collectAsState()
    val errorMessage by viewModel.error.collectAsState()

    BuildingsRoomsWorkspace(
        buildings = buildings,
        rooms = rooms,
        errorMessage = errorMessage,
        onCreateBuilding = viewModel::createBuilding,
        onUpdateBuilding = viewModel::updateBuilding,
        onDeleteBuilding = viewModel::deleteBuilding,
        onCreateRoom = viewModel::createRoom,
        onUpdateRoom = viewModel::updateRoom,
        onDeleteRoom = viewModel::deleteRoom,
        onClearError = viewModel::clearError,
    )
}
