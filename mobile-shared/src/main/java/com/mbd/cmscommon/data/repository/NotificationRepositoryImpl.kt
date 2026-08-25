package com.mbd.cmscommon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.NotificationDao
import com.mbd.cmscommon.data.mapper.NotificationMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

private val LAST_VIEWED_KEY = longPreferencesKey("last_notifications_viewed_at")

class NotificationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val notificationDao: NotificationDao,
    private val dataStore: DataStore<Preferences>,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : NotificationRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override fun observeForRole(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<List<Notification>> =
        notificationDao.observeForRole(role.name, context.sessionId, context.departmentId, role == NotificationTargetRole.ADMIN, System.currentTimeMillis())
            .map { rows -> rows.map { NotificationMapper.entityToDomain(it) } }

    override fun observeAuthoredByCurrentUser(uid: String): Flow<List<Notification>> =
        notificationDao.observeAuthoredBy(uid).map { rows -> rows.map { NotificationMapper.entityToDomain(it) } }

    override fun observeUnreadCount(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<Int> =
        dataStore.data.map { it[LAST_VIEWED_KEY] ?: 0L }.distinctUntilChanged().flatMapLatest { since ->
            notificationDao.observeUnreadCount(role.name, context.sessionId, context.departmentId, role == NotificationTargetRole.ADMIN, System.currentTimeMillis(), since)
        }

    override suspend fun sync(role: NotificationTargetRole, context: NotificationAudienceContext) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped(
            "role" to role.name,
            "session" to context.sessionId,
            "dept" to context.departmentId,
        )
        val since = checkpointStore.get(ownerKey, SupabaseTables.NOTIFICATIONS, scopeKey)?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
                filter {
                    or {
                        eq("targetRole", role.name)
                        eq("targetRole", "ALL")
                    }
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<NotificationDto>()
            if (page.isEmpty()) break

            val entities = page.map { NotificationMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            notificationDao.applyDelta(active, deleted.map { it.notificationId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.NOTIFICATIONS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun syncAuthoredByCurrentUser(uid: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("authored_by" to uid)
        val since = checkpointStore.get(ownerKey, SupabaseTables.NOTIFICATIONS, scopeKey)?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
                filter {
                    eq("createdByEmail", uid)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<NotificationDto>()
            if (page.isEmpty()) break

            val entities = page.map { NotificationMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            notificationDao.applyDelta(active, deleted.map { it.notificationId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.NOTIFICATIONS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    override suspend fun send(
        title: String,
        body: String,
        targetRole: NotificationTargetRole,
        targetOfferingId: String?,
        createdByUid: String,
        priority: NotificationPriority,
        targetDeptId: String?,
        expiresAt: Instant?,
    ) {
        val domain = Notification(
            notificationId = "",
            title = title,
            body = body,
            targetRole = targetRole,
            targetOfferingId = targetOfferingId,
            createdByUid = createdByUid,
            priority = priority,
            targetDeptId = targetDeptId,
            expiresAt = expiresAt,
            createdAt = Instant.EPOCH,
        )
        val payload = NotificationMapper.domainToDto(domain)
        val inserted = postgrest.from(SupabaseTables.NOTIFICATIONS).insert(payload) { select() }.decodeList<NotificationDto>().first()
        notificationDao.upsertAll(listOf(NotificationMapper.dtoToEntity(inserted)))
    }

    override suspend fun delete(notificationId: String) {
        postgrest.from(SupabaseTables.NOTIFICATIONS).update({ set("is_deleted", true) }) {
            filter { eq("id", notificationId) }
        }
        notificationDao.deleteById(notificationId)
    }

    override suspend fun markViewedNow() {
        dataStore.edit { it[LAST_VIEWED_KEY] = System.currentTimeMillis() }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
