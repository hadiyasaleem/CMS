package com.mbd.cmsdesktop.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopNotificationMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

private val LAST_VIEWED_KEY = longPreferencesKey("notifications_last_viewed_at")

/** Durable cache-first notifications repository. */
@Singleton
class DesktopNotificationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val dataStore: DataStore<Preferences>,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : NotificationRepository {

    private val cache = MutableStateFlow(rows().filterNot { it.isDeleted }.map(DesktopNotificationMapper::dtoToDomain))

    override fun observeForRole(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<List<Notification>> =
        cache.asStateFlow().map { list ->
            val now = Instant.now()
            list.filter { notification ->
                val roleMatches =
                    notification.targetRole == null ||
                        notification.targetRole == NotificationTargetRole.ALL ||
                        notification.targetRole == role
                val scopeMatches =
                    role == NotificationTargetRole.ADMIN ||
                        ((notification.targetOfferingId == null || notification.targetOfferingId == context.sessionId) &&
                            (notification.targetDeptId == null || notification.targetDeptId == context.departmentId))
                val isActive = notification.expiresAt?.isAfter(now) ?: true
                roleMatches && scopeMatches && isActive
            }.sortedByDescending { it.createdAt }
        }

    override fun observeAuthoredByCurrentUser(uid: String): Flow<List<Notification>> =
        cache.asStateFlow().map { list ->
            list.filter { it.createdByUid.equals(uid, ignoreCase = true) }
                .sortedByDescending { it.createdAt }
        }

    override fun observeUnreadCount(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<Int> =
        combine(
            dataStore.data.map { it[LAST_VIEWED_KEY] ?: 0L },
            observeForRole(role, context),
        ) { lastViewed, list -> list.count { it.createdAt.toEpochMilli() > lastViewed } }

    override suspend fun sync(role: NotificationTargetRole, context: NotificationAudienceContext) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.NOTIFICATIONS,
            SyncCheckpointDefaults.globalScope(),
            NotificationDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.NOTIFICATIONS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                order("id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun syncAuthoredByCurrentUser(uid: String) {
        if (uid.isBlank()) return
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.NOTIFICATIONS,
            SyncCheckpointDefaults.scoped("authored_by" to uid),
            NotificationDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.NOTIFICATIONS).select {
                filter {
                    eq("created_by_email", uid)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
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
        val inserted = postgrest.from(SupabaseTables.NOTIFICATIONS)
            .insert(DesktopNotificationMapper.domainToDto(domain)) { select() }
            .decodeList<NotificationDto>()
        writeMerged(inserted)
    }

    override suspend fun delete(notificationId: String) {
        postgrest.from(SupabaseTables.NOTIFICATIONS).update({ set("is_deleted", true) }) {
            filter { eq("id", notificationId) }
        }
        writeRows(rows().filterNot { keyOf(it) == notificationId })
    }

    override suspend fun markViewedNow() {
        dataStore.edit { it[LAST_VIEWED_KEY] = System.currentTimeMillis() }
    }

    private fun rows() = store.readRows(CACHE_FILE, NotificationDto.serializer())

    private fun keyOf(dto: NotificationDto) = dto.id ?: "entity:${dto.entityId}"

    private fun writeMerged(delta: List<NotificationDto>) {
        val merged = store.updateRows(CACHE_FILE, NotificationDto.serializer()) { existing ->
            mergeIncrementalDelta(existing, delta, ::keyOf, NotificationDto::isDeleted)
        }
        cache.value = merged.filterNot { it.isDeleted }.map(DesktopNotificationMapper::dtoToDomain)
    }

    private fun writeRows(updated: List<NotificationDto>) {
        store.writeRows(CACHE_FILE, NotificationDto.serializer(), updated)
        cache.value = updated.filterNot { it.isDeleted }.map(DesktopNotificationMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "notifications.json" }
}
