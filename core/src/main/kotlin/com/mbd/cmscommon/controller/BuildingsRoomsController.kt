package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

/** Powers the admin "Buildings & Rooms" screen: lists both and creates/edits/archives either. */
class BuildingsRoomsController(
    private val buildingRepository: BuildingRepository,
    private val roomRepository: RoomRepository,
    private val actorEmail: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val buildings: StateFlow<List<Building>> =
        buildingRepository.observeActiveBuildings().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rooms: StateFlow<List<Room>> =
        roomRepository.observeActiveRooms().stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() = launch {
        buildingRepository.sync()
        roomRepository.sync()
    }

    fun createBuilding(name: String, code: String?) = launch {
        FieldValidators.nameError(name, "Building name").orThrowValidation()
        val cleanCode = code?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
        val slugSource = cleanCode ?: name
        val buildingId = slugify(slugSource)
        requireValid(buildings.value.none { it.buildingId == buildingId }) { "A building with that name already exists." }

        val now = Instant.now()
        buildingRepository.createBuilding(
            Building(
                buildingId = buildingId,
                name = name.trim(),
                code = cleanCode,
                createdAt = now,
                createdBy = actorEmail,
                updatedAt = now,
                updatedBy = actorEmail,
            ),
        )
    }

    fun updateBuilding(existing: Building, name: String, code: String?) = launch {
        FieldValidators.nameError(name, "Building name").orThrowValidation()
        buildingRepository.updateBuilding(
            existing.copy(
                name = name.trim(),
                code = code?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotBlank() },
                updatedAt = Instant.now(),
                updatedBy = actorEmail,
            ),
        )
    }

    fun deleteBuilding(buildingId: String) = launch {
        requireValid(rooms.value.none { it.buildingId == buildingId }) { "Remove this building's rooms first." }
        buildingRepository.deleteBuilding(buildingId)
    }

    fun createRoom(buildingId: String, roomNo: String, name: String?, capacity: Int?, isOffice: Boolean) = launch {
        requireValid(buildingId.isNotBlank()) { "Choose a building." }
        FieldValidators.textError(roomNo, "Room number", maxLength = 30).orThrowValidation()
        requireValid(capacity == null || capacity > 0) { "Capacity must be greater than zero." }
        val roomId = "$buildingId--${slugify(roomNo)}"
        requireValid(rooms.value.none { it.roomId == roomId }) { "This building already has a room with that number." }

        val now = Instant.now()
        roomRepository.createRoom(
            Room(
                roomId = roomId,
                buildingId = buildingId,
                roomNo = roomNo.trim(),
                name = name?.trim()?.takeIf { it.isNotBlank() },
                capacity = capacity,
                isOffice = isOffice,
                createdAt = now,
                createdBy = actorEmail,
                updatedAt = now,
                updatedBy = actorEmail,
            ),
        )
    }

    fun updateRoom(existing: Room, roomNo: String, name: String?, capacity: Int?, isOffice: Boolean) = launch {
        FieldValidators.textError(roomNo, "Room number", maxLength = 30).orThrowValidation()
        requireValid(capacity == null || capacity > 0) { "Capacity must be greater than zero." }
        roomRepository.updateRoom(
            existing.copy(
                roomNo = roomNo.trim(),
                name = name?.trim()?.takeIf { it.isNotBlank() },
                capacity = capacity,
                isOffice = isOffice,
                updatedAt = Instant.now(),
                updatedBy = actorEmail,
            ),
        )
    }

    fun deleteRoom(roomId: String) = launch {
        roomRepository.deleteRoom(roomId)
    }

    private fun slugify(value: String): String =
        Regex("[^a-z0-9-]").replace(value.trim().lowercase(Locale.ROOT).replace(' ', '-'), "-").trim('-')
}
