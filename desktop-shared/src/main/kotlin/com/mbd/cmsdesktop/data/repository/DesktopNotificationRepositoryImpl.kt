package com.mbd.cmsdesktop.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.mbd.cmscommon.data.mapper.DesktopNotificationMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
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

/**
 * `notifications` is a single global table; [audienceCache]/[authoredCache] are each replaced
 * wholesale on every [sync]/[syncAuthoredByCurrentUser] call (the server-side `or(...)` filter in
 * [sync] is intentionally broad — role match OR "ALL" — with the precise session/department scope
 * re-applied client-side in [observeForRole], mirroring mobile's Room query). Unlike the other
 * desktop repos, "last viewed" durably persists via [dataStore] (a small [Preferences] file) since
 * an admin/teacher desktop session may be long-lived.
 */
@Singleton
class DesktopNotificationRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val dataStore: DataStore<Preferences>,
) : NotificationRepository {

    private val audienceCache = MutableStateFlow<List<Notification>>(emptyList())
    private val authoredCache = MutableStateFlow<List<Notification>>(emptyList())

    override fun observeForRole(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<List<Notification>> =
        audienceCache.asStateFlow().map { list ->
            val now = Instant.now()
            list.filter { n ->
                val roleMatches = n.targetRole == null || n.targetRole == NotificationTargetRole.ALL || n.targetRole == role
                val scopeMatches = role == NotificationTargetRole.ADMIN ||
                    ((n.targetOfferingId == null || n.targetOfferingId == context.sessionId) &&
                        (n.targetDeptId == null || n.targetDeptId == context.departmentId))
                val isActive = n.expiresAt?.isAfter(now) ?: true
                roleMatches && scopeMatches && isActive
            }.sortedByDescending { it.createdAt }
        }

    override fun observeAuthoredByCurrentUser(uid: String): Flow<List<Notification>> =
        authoredCache.asStateFlow().map { list -> list.filter { it.createdByUid.equals(uid, ignoreCase = true) } }

    override fun observeUnreadCount(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<Int> =
        combine(
            dataStore.data.map { it[LAST_VIEWED_KEY] ?: 0L },
            observeForRole(role, context),
        ) { lastViewed, list -> list.count { it.createdAt.toEpochMilli() > lastViewed } }

    override suspend fun sync(role: NotificationTargetRole, context: NotificationAudienceContext) {
        val rows = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
            filter {
                or {
                    eq("target_role", role.name)
                    eq("target_role", "ALL")
                    context.sessionId?.let { eq("target_session_id", it) }
                }
            }
            order("created_at", Order.DESCENDING)
            limit(50)
        }.decodeList<NotificationDto>()

        val mapped = rows.map { DesktopNotificationMapper.dtoToDomain(it) }.filter { n ->
            role == NotificationTargetRole.ADMIN ||
                ((n.targetOfferingId == null || n.targetOfferingId == context.sessionId) &&
                    (n.targetDeptId == null || n.targetDeptId == context.departmentId))
        }
        audienceCache.value = mapped
    }

    override suspend fun syncAuthoredByCurrentUser(uid: String) {
        if (uid.isBlank()) return
        val rows = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
            filter { eq("created_by_email", uid) }
            order("created_at", Order.DESCENDING)
            limit(100)
        }.decodeList<NotificationDto>()
        authoredCache.value = rows.map { DesktopNotificationMapper.dtoToDomain(it) }
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
        val payload = DesktopNotificationMapper.domainToDto(domain)
        val inserted = postgrest.from(SupabaseTables.NOTIFICATIONS).insert(payload) { select() }.decodeList<NotificationDto>().first()
        val notification = DesktopNotificationMapper.dtoToDomain(inserted)
        authoredCache.value = listOf(notification) + authoredCache.value.filterNot { it.notificationId == notification.notificationId }
        audienceCache.value = listOf(notification) + audienceCache.value.filterNot { it.notificationId == notification.notificationId }
    }

    override suspend fun delete(notificationId: String) {
        postgrest.from(SupabaseTables.NOTIFICATIONS).delete { filter { eq("id", notificationId) } }
        audienceCache.value = audienceCache.value.filterNot { it.notificationId == notificationId }
        authoredCache.value = authoredCache.value.filterNot { it.notificationId == notificationId }
    }

    override suspend fun markViewedNow() {
        dataStore.edit { it[LAST_VIEWED_KEY] = System.currentTimeMillis() }
    }
}
