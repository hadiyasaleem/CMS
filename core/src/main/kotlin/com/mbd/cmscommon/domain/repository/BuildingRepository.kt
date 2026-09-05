package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Building
import kotlinx.coroutines.flow.Flow

interface BuildingRepository {
    fun observeActiveBuildings(): Flow<List<Building>>
    suspend fun getBuilding(buildingId: String): Building?
    suspend fun sync()
    suspend fun createBuilding(building: Building)
    suspend fun updateBuilding(building: Building)
    suspend fun deleteBuilding(buildingId: String)
}
