package com.mbd.cmsdesktop.ui.admin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.mbd.cmscommon.controller.BuildingsRoomsController
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.ui.components.BuildingsRoomsWorkspace

@Composable
fun BuildingsRoomsScreen(
    buildingRepository: BuildingRepository,
    roomRepository: RoomRepository,
    createdBy: String?,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(buildingRepository, roomRepository, createdBy) {
        BuildingsRoomsController(buildingRepository, roomRepository, createdBy.orEmpty(), scope)
    }
    val buildings by controller.buildings.collectAsState()
    val rooms by controller.rooms.collectAsState()
    val errorMessage by controller.error.collectAsState()

    BuildingsRoomsWorkspace(
        buildings = buildings,
        rooms = rooms,
        errorMessage = errorMessage,
        onCreateBuilding = controller::createBuilding,
        onUpdateBuilding = controller::updateBuilding,
        onDeleteBuilding = controller::deleteBuilding,
        onCreateRoom = controller::createRoom,
        onUpdateRoom = controller::updateRoom,
        onDeleteRoom = controller::deleteRoom,
        onClearError = controller::clearError,
        modifier = Modifier.fillMaxSize(),
    )
}
