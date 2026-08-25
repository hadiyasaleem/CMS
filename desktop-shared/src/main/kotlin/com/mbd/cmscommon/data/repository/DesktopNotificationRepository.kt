package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopNotificationMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.NotificationDto
import com.mbd.cmscommon.domain.model.Notification
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.NotificationRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Desktop repos are always-online: no local persistence, `sync*()` re-fetches into an in-memory
 * cache that the `observe*` methods just expose.
 *
 * `notifications` is a single global table shared by every role/session/department, so unlike
 * [com.mbd.cmscommon.data.repository.DesktopDepartmentRepository]'s single wholesale-replace
 * cache, this repo keeps one flat cache keyed by notificationId and *merges* each sync's page
 * into it (mirroring the shared Room table mobile's NotificationDao upserts deltas into) rather
 * than replacing the whole cache — a `sync(role, context)` call only fetches rows relevant to
 * that role, so a wholesale replace would wipe out notifications fetched for a different
 * role/context. [observeForRole] applies the same targetRole/scope/expiry filtering client-side
 * that mobile's Room query expressed in SQL. `markViewedNow`'s "last viewed" timestamp has no
 * durable store on desktop (no DataStore/Room) — it lives in [lastViewedAt] only for the process
 * lifetime, which is an acceptable trade-off for an always-online admin/teacher desktop client.
 */
@Singleton
class DesktopNotificationRepository @Inject constructor(
    private val postgrest: Postgrest,
) : NotificationRepository {

    private val cache = MutableStateFlow<Map<String, Notification>>(emptyMap())
    private val lastViewedAt = MutableStateFlow(Instant.EPOCH)

    private fun matchesRole(notification: Notification, role: NotificationTargetRole, context: NotificationAudienceContext, now: Instant): Boolean {
        if (notification.targetRole != role && notification.targetRole != NotificationTargetRole.ALL) return false
        val expiresAt = notification.expiresAt
        if (expiresAt != null && expiresAt.isBefore(now)) return false
        if (role == NotificationTargetRole.ADMIN) return true
        val sessionOk = notification.targetOfferingId.isNullOrBlank() || notification.targetOfferingId == context.sessionId
        val deptOk = notification.targetDeptId.isNullOrBlank() || notification.targetDeptId == context.departmentId
        return sessionOk && deptOk
    }

    override fun observeForRole(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<List<Notification>> =
        cache.map { m ->
            val now = Instant.now()
            m.values.filter { matchesRole(it, role, context, now) }.sortedByDescending { it.createdAt }
        }

    override fun observeAuthoredByCurrentUser(uid: String): Flow<List<Notification>> =
        cache.map { m -> m.values.filter { it.createdByUid == uid }.sortedByDescending { it.createdAt } }

    override fun observeUnreadCount(role: NotificationTargetRole, context: NotificationAudienceContext): Flow<Int> =
        combine(observeForRole(role, context), lastViewedAt) { notifications, since ->
            notifications.count { it.createdAt >= since }
        }

    private fun mergePage(page: List<NotificationDto>) {
        cache.update { m ->
            val next = m.toMutableMap()
            for (dto in page) {
                val id = dto.id ?: continue
                if (dto.isDeleted) next.remove(id) else next[id] = DesktopNotificationMapper.dtoToDomain(dto)
            }
            next
        }
    }

    override suspend fun sync(role: NotificationTargetRole, context: NotificationAudienceContext) {
        val page = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
            filter {
                or {
                    eq("targetRole", role.name)
                    eq("targetRole", "ALL")
                }
            }
        }.decodeList<NotificationDto>()
        mergePage(page)
    }

    override suspend fun syncAuthoredByCurrentUser(uid: String) {
        val page = postgrest.from(SupabaseTables.NOTIFICATIONS).select {
            filter { eq("createdByEmail", uid) }
        }.decodeList<NotificationDto>()
        mergePage(page)
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
        val insertedId = inserted.id
        if (insertedId != null) {
            cache.update { it + (insertedId to DesktopNotificationMapper.dtoToDomain(inserted)) }
        }
    }

    override suspend fun delete(notificationId: String) {
        postgrest.from(SupabaseTables.NOTIFICATIONS).update({ set("is_deleted", true) }) {
            filter { eq("id", notificationId) }
        }
        cache.update { it - notificationId }
    }

    override suspend fun markViewedNow() {
        lastViewedAt.value = Instant.now()
    }
}
