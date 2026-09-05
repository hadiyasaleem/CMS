package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.util.FieldValidators

@Composable
fun BuildingsRoomsWorkspace(
    buildings: List<Building>,
    rooms: List<Room>,
    errorMessage: String?,
    onCreateBuilding: (String, String?) -> Unit,
    onUpdateBuilding: (Building, String, String?) -> Unit,
    onDeleteBuilding: (String) -> Unit,
    onCreateRoom: (String, String, String?, Int?, Boolean) -> Unit,
    onUpdateRoom: (Room, String, String?, Int?, Boolean) -> Unit,
    onDeleteRoom: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showAddBuilding by remember { mutableStateOf(false) }
    var editingBuilding by remember { mutableStateOf<Building?>(null) }
    var pendingDeleteBuilding by remember { mutableStateOf<Building?>(null) }
    var addingRoomFor by remember { mutableStateOf<String?>(null) }
    var editingRoom by remember { mutableStateOf<Room?>(null) }
    var pendingDeleteRoom by remember { mutableStateOf<Room?>(null) }

    val roomsByBuilding = rooms.groupBy { it.buildingId }

    Box(modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { BuildingsRoomsHero(buildings.size, rooms.size) }

            if (buildings.isEmpty()) {
                item {
                    Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainerLowest, border = BorderStroke(1.dp, ModTrack)) {
                        Text(
                            "No buildings yet. Add one to start listing its rooms.",
                            modifier = Modifier.padding(24.dp),
                            color = ModMuted,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                items(buildings, key = { it.buildingId }) { building ->
                    BuildingCard(
                        building = building,
                        rooms = roomsByBuilding[building.buildingId].orEmpty().sortedBy { it.roomNo },
                        onEdit = { editingBuilding = building },
                        onDelete = { pendingDeleteBuilding = building },
                        onAddRoom = { addingRoomFor = building.buildingId },
                        onEditRoom = { editingRoom = it },
                        onDeleteRoom = { pendingDeleteRoom = it },
                    )
                }
            }

            item { Spacer(Modifier.height(72.dp)) }
        }
        CmsFab(
            onClick = { showAddBuilding = true },
            contentDescription = "Add building",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showAddBuilding) {
        BuildingEditorDialog(
            existing = null,
            onDismiss = { showAddBuilding = false },
            onConfirm = { name, code -> onCreateBuilding(name, code); showAddBuilding = false },
        )
    }

    editingBuilding?.let { building ->
        BuildingEditorDialog(
            existing = building,
            onDismiss = { editingBuilding = null },
            onConfirm = { name, code -> onUpdateBuilding(building, name, code); editingBuilding = null },
        )
    }

    pendingDeleteBuilding?.let { building ->
        ConfirmDestructiveActionDialog(
            title = "Delete ${building.name}?",
            dependentSummary = "Remove this building's rooms first, then delete the building itself.",
            onConfirm = { onDeleteBuilding(building.buildingId); pendingDeleteBuilding = null },
            onDismiss = { pendingDeleteBuilding = null },
        )
    }

    addingRoomFor?.let { buildingId ->
        RoomEditorDialog(
            existing = null,
            onDismiss = { addingRoomFor = null },
            onConfirm = { roomNo, name, capacity, isOffice ->
                onCreateRoom(buildingId, roomNo, name, capacity, isOffice)
                addingRoomFor = null
            },
        )
    }

    editingRoom?.let { room ->
        RoomEditorDialog(
            existing = room,
            onDismiss = { editingRoom = null },
            onConfirm = { roomNo, name, capacity, isOffice ->
                onUpdateRoom(room, roomNo, name, capacity, isOffice)
                editingRoom = null
            },
        )
    }

    pendingDeleteRoom?.let { room ->
        ConfirmDestructiveActionDialog(
            title = "Delete room ${room.roomNo}?",
            dependentSummary = "It will no longer be selectable for class periods or teacher offices.",
            onConfirm = { onDeleteRoom(room.roomId); pendingDeleteRoom = null },
            onDismiss = { pendingDeleteRoom = null },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Something went wrong") },
            text = { Text(errorMessage, color = ModAccent) },
            confirmButton = { TextButton(onClick = onClearError) { Text("OK") } },
        )
    }
}

@Composable
private fun BuildingsRoomsHero(buildingCount: Int, roomCount: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("CAMPUS", color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Buildings & rooms", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$buildingCount building(s), $roomCount room(s)", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BuildingCard(
    building: Building,
    rooms: List<Room>,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddRoom: () -> Unit,
    onEditRoom: (Room) -> Unit,
    onDeleteRoom: (Room) -> Unit,
) {
    var menuOpen by remember { mutableStateOf(false) }

    CmsCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(building.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    building.code?.let { Text(it, color = ModMuted, style = MaterialTheme.typography.bodySmall) }
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Building options") }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, onClick = { menuOpen = false; onEdit() })
                        DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Text("ROOMS", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))

            if (rooms.isEmpty()) {
                Text("No rooms yet.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                rooms.forEach { room ->
                    RoomRow(room, onEdit = { onEditRoom(room) }, onDelete = { onDeleteRoom(room) })
                }
            }

            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onAddRoom) { Text("+ Add room") }
        }
    }
}

@Composable
private fun RoomRow(room: Room, onEdit: () -> Unit, onDelete: () -> Unit) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(room.roomNo, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                if (room.isOffice) {
                    Spacer(Modifier.width(8.dp))
                    StatusBadge("Office", BadgeTone.Navy)
                }
            }
            val subtitle = listOfNotNull(room.name, room.capacity?.let { "Capacity $it" }).joinToString(" · ")
            if (subtitle.isNotBlank()) Text(subtitle, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
        Box {
            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Filled.MoreVert, contentDescription = "Room options") }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("Edit") }, onClick = { menuOpen = false; onEdit() })
                DropdownMenuItem(text = { Text("Delete") }, onClick = { menuOpen = false; onDelete() })
            }
        }
    }
}

@Composable
private fun BuildingEditorDialog(existing: Building?, onDismiss: () -> Unit, onConfirm: (String, String?) -> Unit) {
    var name by remember(existing?.buildingId) { mutableStateOf(existing?.name.orEmpty()) }
    var code by remember(existing?.buildingId) { mutableStateOf(existing?.code.orEmpty()) }
    val nameError = FieldValidators.nameError(name, "Building name")
    val canSave = nameError == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add building" else "Edit building") },
        text = {
            Column {
                CmsTextField(value = name, onValueChange = { name = it }, label = "Building name", placeholder = "Main Block", isError = name.isNotBlank() && nameError != null, supportingText = nameError.takeIf { name.isNotBlank() })
                Spacer(Modifier.height(12.dp))
                CmsTextField(value = code, onValueChange = { code = it.uppercase().take(10) }, label = "Code (optional)", placeholder = "MB")
            }
        },
        confirmButton = {
            CmsPrimaryButton(
                text = if (existing == null) "Create building" else "Save changes",
                enabled = canSave,
                onClick = { onConfirm(name.trim(), code.trim().ifBlank { null }) },
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}

@Composable
private fun RoomEditorDialog(existing: Room?, onDismiss: () -> Unit, onConfirm: (String, String?, Int?, Boolean) -> Unit) {
    var roomNo by remember(existing?.roomId) { mutableStateOf(existing?.roomNo.orEmpty()) }
    var name by remember(existing?.roomId) { mutableStateOf(existing?.name.orEmpty()) }
    var capacity by remember(existing?.roomId) { mutableStateOf(existing?.capacity?.toString().orEmpty()) }
    var isOffice by remember(existing?.roomId) { mutableStateOf(existing?.isOffice ?: false) }
    val roomNoError = FieldValidators.textError(roomNo, "Room number", maxLength = 30)
    val capacityValue = capacity.trim().toIntOrNull()
    val capacityError = capacity.isNotBlank() && (capacityValue == null || capacityValue <= 0)
    val canSave = roomNoError == null && !capacityError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Add room" else "Edit room") },
        text = {
            Column {
                CmsTextField(value = roomNo, onValueChange = { roomNo = it }, label = "Room number", placeholder = "101", isError = roomNo.isNotBlank() && roomNoError != null, supportingText = roomNoError.takeIf { roomNo.isNotBlank() })
                Spacer(Modifier.height(12.dp))
                CmsTextField(value = name, onValueChange = { name = it }, label = "Name (optional)", placeholder = "Physics lab")
                Spacer(Modifier.height(12.dp))
                CmsTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter(Char::isDigit) },
                    label = "Capacity (optional)",
                    isError = capacityError,
                    supportingText = if (capacityError) "Capacity must be greater than zero." else null,
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isOffice, onCheckedChange = { isOffice = it })
                    Text("Usable as a teacher office")
                }
            }
        },
        confirmButton = {
            CmsPrimaryButton(
                text = if (existing == null) "Create room" else "Save changes",
                enabled = canSave,
                onClick = { onConfirm(roomNo.trim(), name.trim().ifBlank { null }, capacityValue, isOffice) },
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
