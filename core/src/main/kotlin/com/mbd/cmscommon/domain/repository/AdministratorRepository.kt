package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.AdministratorAccount
import kotlinx.coroutines.flow.Flow

interface AdministratorRepository {
    fun observeAdministrators(): Flow<List<AdministratorAccount>>
    suspend fun sync()
    suspend fun createAdministrator(email: String, password: String)
    suspend fun setStatus(email: String, status: String)
    suspend fun resetPassword(email: String, newPassword: String)
    suspend fun deleteAdministrator(email: String)
}
