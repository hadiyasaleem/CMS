package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Room
import kotlinx.coroutines.flow.Flow

interface RoomRepository {
    fun observeActiveRooms(): Flow<List<Room>>
    suspend fun getRoom(roomId: String): Room?
    suspend fun sync()
    suspend fun createRoom(room: Room)
    suspend fun updateRoom(room: Room)
    suspend fun deleteRoom(roomId: String)
}
