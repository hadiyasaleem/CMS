package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.data.mapper.DesktopAdministratorMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AdministratorAccountDto
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import io.github.jan.supabase.postgrest.Postgrest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DesktopAdministratorRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
) : AdministratorRepository {

    private val cache = MutableStateFlow<List<AdministratorAccount>>(emptyList())

    override fun observeAdministrators(): Flow<List<AdministratorAccount>> = cache.asStateFlow()

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.PROFILES).select {
            filter {
                eq("role", "ADMIN")
                eq("is_deleted", false)
            }
        }.decodeList<AdministratorAccountDto>()

        cache.value = rows.map { DesktopAdministratorMapper.dtoToDomain(it) }.sortedBy { it.email }
    }

    override suspend fun createAdministrator(email: String, password: String) {
        provisioner.createAdmin(email, password)
        sync()
    }
}
