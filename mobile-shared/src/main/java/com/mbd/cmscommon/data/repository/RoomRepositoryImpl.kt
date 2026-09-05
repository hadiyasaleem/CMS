package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.RoomDao
import com.mbd.cmscommon.data.mapper.RoomMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.RoomDto
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.repository.RoomRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val roomDao: RoomDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : RoomRepository {

    override fun observeActiveRooms(): Flow<List<Room>> =
        roomDao.observeActive().map { rows -> rows.map { RoomMapper.entityToDomain(it) } }

    override suspend fun getRoom(roomId: String): Room? =
        roomDao.getById(roomId)?.let { RoomMapper.entityToDomain(it) }

    override suspend fun sync() {
        val ownerKey = SyncCheckpointDefaults.ownerKey(sessionManager.accountKey ?: "")
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.ROOMS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.ROOMS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<RoomDto>()

            if (page.isEmpty()) break

            val entities = page.map { RoomMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            roomDao.applyDelta(active, deleted.map { it.roomId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(
            SyncCheckpoint(
                ownerKey = ownerKey,
                tableName = SupabaseTables.ROOMS,
                scopeKey = scopeKey,
                lastUpdatedAt = maxUpdatedAt,
                lastSuccessfulSyncAt = PgTime.format(java.time.Instant.now()) ?: since,
            ),
        )
    }

    override suspend fun createRoom(room: Room) {
        postgrest.from(SupabaseTables.ROOMS).insert(RoomMapper.domainToDto(room))
        roomDao.upsert(RoomMapper.domainToEntity(room))
    }

    override suspend fun updateRoom(room: Room) = createRoom(room)

    override suspend fun deleteRoom(roomId: String) {
        postgrest.from(SupabaseTables.ROOMS).update({ set("is_deleted", true) }) {
            filter { eq("room_id", roomId) }
        }
        roomDao.deleteById(roomId)
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
