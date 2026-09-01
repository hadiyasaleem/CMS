package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.auth.normalizeEmail
import com.mbd.cmscommon.data.mapper.DesktopAdministratorMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AdministratorAccountDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class DesktopAdministratorRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val provisioner: AdminUserProvisioner,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : AdministratorRepository {

    private val cache = MutableStateFlow(toDomains(cachedRows()))

    override fun observeAdministrators(): Flow<List<AdministratorAccount>> = cache.asStateFlow()

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.PROFILES,
            SyncCheckpointDefaults.scoped("role" to "ADMIN"),
            AdministratorAccountDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.PROFILES).select {
                filter {
                    eq("role", "ADMIN")
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("entity_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        val merged = mergeIncrementalDelta(
            cachedRows(),
            delta,
            { it.id ?: it.email.orEmpty() },
            AdministratorAccountDto::isDeleted,
        )
        store.writeRows(CACHE_FILE, AdministratorAccountDto.serializer(), merged)
        cache.value = toDomains(merged)
    }

    override suspend fun createAdministrator(email: String, password: String) {
        val normalizedEmail = email.normalizeEmail()
        val uid = provisioner.createAdmin(normalizedEmail, password)
        val now = PgTime.format(Instant.now())
        val created = AdministratorAccountDto(
            id = uid,
            email = normalizedEmail,
            status = "ACTIVE",
            createdAt = now,
            updatedAt = now,
        )
        val merged = mergeIncrementalDelta(
            cachedRows(),
            listOf(created),
            { it.id ?: it.email.orEmpty() },
            AdministratorAccountDto::isDeleted,
        )
        store.writeRows(CACHE_FILE, AdministratorAccountDto.serializer(), merged)
        cache.value = toDomains(merged)
    }

    private fun cachedRows(): List<AdministratorAccountDto> =
        store.readRows(CACHE_FILE, AdministratorAccountDto.serializer())

    private fun toDomains(rows: List<AdministratorAccountDto>): List<AdministratorAccount> =
        rows.filterNot { it.isDeleted }.map(DesktopAdministratorMapper::dtoToDomain).sortedBy { it.email }

    private fun ownerKey(): String =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val CACHE_FILE = "administrators.json"
    }
}
