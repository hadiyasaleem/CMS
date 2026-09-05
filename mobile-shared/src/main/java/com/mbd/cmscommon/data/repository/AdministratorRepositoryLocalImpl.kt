package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.AdminUserProvisioner
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.auth.normalizeEmail
import com.mbd.cmscommon.data.local.dao.AdministratorAccountDao
import com.mbd.cmscommon.data.local.entity.AdministratorAccountEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AdministratorAccountDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private fun AdministratorAccountDto.toEntity(): AdministratorAccountEntity = AdministratorAccountEntity(
    id = id ?: "",
    email = email ?: "",
    status = status ?: "",
    lastLoginAt = PgTime.parse(lastLoginAt)?.toEpochMilli(),
    createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
    createdBy = createdBy,
    updatedAt = PgTime.parseOrEpoch(updatedAt ?: createdAt).toEpochMilli(),
    updatedBy = updatedBy,
    isDeleted = isDeleted,
    deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
    deletedBy = deletedBy,
)

private fun AdministratorAccountEntity.toDomain(): AdministratorAccount = AdministratorAccount(
    id = id,
    email = email,
    status = status,
    createdAt = Instant.ofEpochMilli(createdAt),
    lastLoginAt = lastLoginAt?.let { Instant.ofEpochMilli(it) },
    createdBy = createdBy,
    updatedAt = Instant.ofEpochMilli(updatedAt),
    updatedBy = updatedBy,
    isDeleted = isDeleted,
    deletedAt = deletedAt?.let { Instant.ofEpochMilli(it) },
    deletedBy = deletedBy,
)

@Singleton
class AdministratorRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val administratorDao: AdministratorAccountDao,
    private val provisioner: AdminUserProvisioner,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : AdministratorRepository {

    override fun observeAdministrators(): Flow<List<AdministratorAccount>> =
        administratorDao.observeAll().map { rows -> rows.map { it.toDomain() } }

    override suspend fun sync() {
        val ownerKey = SyncCheckpointDefaults.ownerKey(sessionManager.accountKey ?: "")
        val scopeKey = SyncCheckpointDefaults.scoped("role" to "ADMIN")
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.PROFILES, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.PROFILES).select {
                filter {
                    eq("role", "ADMIN")
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<AdministratorAccountDto>()

            if (page.isEmpty()) break

            val entities = page.map { it.toEntity() }
            val (deleted, active) = entities.partition { it.isDeleted }
            administratorDao.applyDelta(active)
            if (deleted.isNotEmpty()) administratorDao.applyDelta(deleted)
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(
            SyncCheckpoint(
                ownerKey = ownerKey,
                tableName = SupabaseTables.PROFILES,
                scopeKey = scopeKey,
                lastUpdatedAt = maxUpdatedAt,
                lastSuccessfulSyncAt = PgTime.format(Instant.now()) ?: since,
            ),
        )
    }

    override suspend fun createAdministrator(email: String, password: String) {
        val normalizedEmail = email.normalizeEmail()
        val uid = provisioner.createAdmin(normalizedEmail, password)
        val now = System.currentTimeMillis()
        administratorDao.applyDelta(
            listOf(
                AdministratorAccountEntity(
                    id = uid,
                    email = normalizedEmail,
                    status = "ACTIVE",
                    lastLoginAt = null,
                    createdAt = now,
                    updatedAt = now,
                ),
            ),
        )
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
