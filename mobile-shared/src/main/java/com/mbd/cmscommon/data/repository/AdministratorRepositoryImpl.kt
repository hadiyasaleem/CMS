package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.normalizeEmail
import com.mbd.cmscommon.data.remote.PgTime
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
class AdministratorRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
) : AdministratorRepository {
    private val administrators = MutableStateFlow<List<AdministratorAccount>>(emptyList())

    override fun observeAdministrators(): Flow<List<AdministratorAccount>> = administrators.asStateFlow()

    override suspend fun sync() {
        val rows = postgrest.from(SupabaseTables.PROFILES).select {
            filter {
                eq("role", "ADMIN")
                eq("is_deleted", false)
            }
        }.decodeList<AdministratorAccountDto>()

        administrators.value = rows.map { row ->
            AdministratorAccount(
                id = row.id ?: "",
                entityId = 0L,
                email = row.email ?: "",
                status = row.status ?: "",
                createdAt = PgTime.parse(row.createdAt),
                lastLoginAt = PgTime.parse(row.lastLoginAt),
            )
        }.sortedBy { it.email }
    }

    override suspend fun createAdministrator(email: String, password: String) {
        provisioner.createAdmin(email.normalizeEmail(), password)
        sync()
    }
}
