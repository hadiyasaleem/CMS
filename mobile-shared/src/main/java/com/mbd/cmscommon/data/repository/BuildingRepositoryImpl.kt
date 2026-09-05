package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.BuildingDao
import com.mbd.cmscommon.data.mapper.BuildingMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.BuildingDto
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.repository.BuildingRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class BuildingRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val buildingDao: BuildingDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : BuildingRepository {

    override fun observeActiveBuildings(): Flow<List<Building>> =
        buildingDao.observeActive().map { rows -> rows.map { BuildingMapper.entityToDomain(it) } }

    override suspend fun getBuilding(buildingId: String): Building? =
        buildingDao.getById(buildingId)?.let { BuildingMapper.entityToDomain(it) }

    override suspend fun sync() {
        val ownerKey = SyncCheckpointDefaults.ownerKey(sessionManager.accountKey ?: "")
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.BUILDINGS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.BUILDINGS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<BuildingDto>()

            if (page.isEmpty()) break

            val entities = page.map { BuildingMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            buildingDao.applyDelta(active, deleted.map { it.buildingId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(
            SyncCheckpoint(
                ownerKey = ownerKey,
                tableName = SupabaseTables.BUILDINGS,
                scopeKey = scopeKey,
                lastUpdatedAt = maxUpdatedAt,
                lastSuccessfulSyncAt = PgTime.format(java.time.Instant.now()) ?: since,
            ),
        )
    }

    override suspend fun createBuilding(building: Building) {
        postgrest.from(SupabaseTables.BUILDINGS).insert(BuildingMapper.domainToDto(building))
        buildingDao.upsert(BuildingMapper.domainToEntity(building))
    }

    override suspend fun updateBuilding(building: Building) = createBuilding(building)

    override suspend fun deleteBuilding(buildingId: String) {
        postgrest.from(SupabaseTables.BUILDINGS).update({ set("is_deleted", true) }) {
            filter { eq("building_id", buildingId) }
        }
        buildingDao.deleteById(buildingId)
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
