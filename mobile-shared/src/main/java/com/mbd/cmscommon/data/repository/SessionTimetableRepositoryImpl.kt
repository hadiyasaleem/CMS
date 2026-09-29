package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.mapper.AcademicStructureMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.PeriodSessionDto
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.DayOfWeek
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SessionTimetableRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val periodDao: SessionPeriodDao,
    private val sessionDao: AcademicSessionDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : SessionTimetableRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private suspend fun deptOf(sessionId: String): String = sessionDao.getById(sessionId)?.deptId ?: ""

    private fun periodLocalId(dto: TimetablePeriodDto, fallbackSessionId: String): String =
        dto.id ?: "${dto.sessionId ?: fallbackSessionId}_${dto.shift}_${dto.day}_${dto.startTime}"

    private fun TimetablePeriodDto.toEntity(sessionId: String, deptId: String, linked: String = ""): SessionPeriodEntity = SessionPeriodEntity(
        id = periodLocalId(this, sessionId),
        sessionId = this.sessionId ?: sessionId,
        linkedSessionIds = linked,
        shift = shift ?: Session.MORNING.name,
        deptId = deptId,
        day = day ?: DayOfWeek.MONDAY.name,
        startTime = startTime,
        endTime = endTime,
        courseCode = courseCode,
        subjectName = subjectName,
        teacherId = teacherEmail,
        teacherName = teacherName,
        periodType = periodType ?: "LECTURE",
        creditHours = creditHours,
        roomNo = roomNo,
        building = building,
        notes = notes,
        effectiveFrom = effectiveFrom,
        effectiveTo = effectiveTo,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

    override fun observeAll(): Flow<List<SessionPeriod>> =
        periodDao.observeAll().map { rows -> rows.map { AcademicStructureMapper.periodEntityToDomain(it) } }

    override fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>> =
        periodDao.observeForSessionDay(sessionId, day.name).map { rows -> rows.map { AcademicStructureMapper.periodEntityToDomain(it) } }

    override fun observeWeek(sessionId: String): Flow<List<SessionPeriod>> =
        periodDao.observeForSession(sessionId).map { rows -> rows.map { AcademicStructureMapper.periodEntityToDomain(it) } }

    override fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>> =
        periodDao.observeForTeacher(teacherId).map { rows -> rows.map { AcademicStructureMapper.periodEntityToDomain(it) } }

    override fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>> =
        periodDao.observeForDay(day.name).map { rows -> rows.map { AcademicStructureMapper.periodEntityToDomain(it) } }

    override suspend fun savePeriod(period: SessionPeriod) {
        val dto = TimetablePeriodDto(
            sessionId = period.sessionId,
            shift = period.shift.name,
            day = period.day.name,
            startTime = period.startTime,
            endTime = period.endTime,
            periodType = period.periodType.name,
            courseCode = period.courseCode,
            subjectName = period.subjectName,
            creditHours = period.creditHours,
            teacherEmail = period.teacherId.takeIf { it.isNotBlank() },
            teacherName = period.teacherName,
            roomNo = period.roomNo,
            building = period.building,
            notes = period.notes,
            effectiveFrom = period.effectiveFrom?.toString(),
            effectiveTo = period.effectiveTo?.toString(),
            createdBy = period.createdBy,
            updatedBy = period.updatedBy,
        )
        val saved = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).upsert(dto) {
            onConflict = "primary_session_id,shift,day,start_time"
            select()
        }.decodeList<TimetablePeriodDto>().first()
        periodDao.deleteForSlot(period.sessionId, period.shift.name, period.day.name, period.startTime)
        val remoteId = saved.id
        // Editing an already-merged period's fields must keep (and refresh) its merge, not silently drop it.
        val linked = remoteId?.let { linkedSessionIdsFor(it) }.orEmpty()
        periodDao.upsertAll(listOf(saved.toEntity(period.sessionId, deptOf(period.sessionId), linked.joinToString(","))))
        linked.forEach { linkedSessionId -> upsertShadowRow(saved, linkedSessionId, (linked - linkedSessionId) + period.sessionId) }
    }

    override suspend fun removePeriod(period: SessionPeriod) {
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).update({ set("is_deleted", true) }) {
            filter {
                eq("primary_session_id", period.sessionId)
                eq("shift", period.shift.name)
                eq("day", period.day.name)
                eq("start_time", period.startTime)
            }
        }
        // A merged lecture is one row shared by several sessions -- removing it must clear every linked
        // session's share of it too, both remotely (their period_sessions rows) and locally (their shadow rows).
        postgrest.from(SupabaseTables.PERIOD_SESSIONS).update({ set("is_deleted", true) }) {
            filter { eq("period_id", period.id) }
        }
        periodDao.deleteForSlot(period.sessionId, period.shift.name, period.day.name, period.startTime)
        periodDao.deleteAllRowsForRemotePeriod(period.id)
    }

    /** The sessions (besides the lecture's own primary) currently linked to remote period [periodId]. */
    private suspend fun linkedSessionIdsFor(periodId: String): Set<String> =
        postgrest.from(SupabaseTables.PERIOD_SESSIONS).select {
            filter { eq("period_id", periodId); eq("is_deleted", false) }
        }.decodeList<PeriodSessionDto>().mapNotNull { it.sessionId }.toSet()

    /**
     * Refreshes [viewSessionId]'s local "shadow" view of [dto]'s lecture -- letting a session that's merely
     * linked into a merged lecture see it occupying that slot on its own grid, read-only. [othersFromHere] is
     * every OTHER session sharing the lecture as seen from [viewSessionId] (excludes [viewSessionId] itself).
     */
    private suspend fun upsertShadowRow(dto: TimetablePeriodDto, viewSessionId: String, othersFromHere: Set<String>) {
        val remoteId = dto.id ?: return
        val entity = dto.toEntity(viewSessionId, deptOf(viewSessionId), othersFromHere.filter { it.isNotBlank() }.joinToString(","))
            .copy(id = "$remoteId::$viewSessionId", sessionId = viewSessionId, remotePeriodId = remoteId)
        periodDao.upsertAll(listOf(entity))
    }

    override suspend fun setPeriodLink(period: SessionPeriod, sessionId: String, link: Boolean) {
        if (link) {
            postgrest.from(SupabaseTables.PERIOD_SESSIONS).upsert(
                PeriodSessionDto(periodId = period.id, sessionId = sessionId),
            ) { onConflict = "period_id,session_id" }
        } else {
            postgrest.from(SupabaseTables.PERIOD_SESSIONS).update({ set("is_deleted", true) }) {
                filter { eq("period_id", period.id); eq("session_id", sessionId) }
            }
        }
        // Refresh locally: the primary's own row's chip list, and the target session's shadow row (added or removed).
        syncSession(period.sessionId)
        syncSession(sessionId)
    }

    override suspend fun syncSession(sessionId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("session" to sessionId)
        val deptId = deptOf(sessionId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.TIMETABLE_PERIODS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
                filter {
                    eq("primary_session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<TimetablePeriodDto>()
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity(sessionId, deptId) }
            val (deleted, active) = entities.partition { it.isDeleted }
            periodDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.TIMETABLE_PERIODS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
        syncPeriodLinksFor(sessionId)
    }

    /**
     * Merged-lecture links for [sessionId], not checkpointed (bounded to this one session's periods/links, so
     * a fresh fetch each time is cheap): (a) refreshes [sessionId]'s own periods' `linkedSessionIds` chip
     * list, and (b) refreshes the shadow rows for any lecture [sessionId] is merely linked into elsewhere,
     * dropping any that are no longer current.
     */
    private suspend fun syncPeriodLinksFor(sessionId: String) {
        // (a) This session's own (primary) periods.
        for (row in periodDao.observeForSession(sessionId).first().filter { it.id == it.remotePeriodId }) {
            val linked = linkedSessionIdsFor(row.remotePeriodId).joinToString(",")
            if (linked != row.linkedSessionIds) periodDao.setLinkedSessionIds(row.remotePeriodId, linked)
        }

        // (b) Lectures this session is linked into (shadow rows).
        val linksAsGuest = postgrest.from(SupabaseTables.PERIOD_SESSIONS).select {
            filter { eq("session_id", sessionId); eq("is_deleted", false) }
        }.decodeList<PeriodSessionDto>()
        val guestPeriodIds = linksAsGuest.mapNotNull { it.periodId }.distinct()
        // A merge is a rare, occasional admin action, so one query per linked period (typically 0-2) is fine.
        val guestPeriods = guestPeriodIds.mapNotNull { pid ->
            postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
                filter { eq("id", pid); eq("is_deleted", false) }
            }.decodeList<TimetablePeriodDto>().firstOrNull()
        }
        for (dto in guestPeriods) {
            val remoteId = dto.id ?: continue
            val others = (linkedSessionIdsFor(remoteId) + (dto.sessionId ?: "") - sessionId).filter { it.isNotBlank() }.toSet()
            upsertShadowRow(dto, sessionId, others)
        }
        val stillValidShadowIds = guestPeriods.mapNotNull { it.id }.map { "$it::$sessionId" }
        periodDao.deleteStaleShadowsForSession(sessionId, stillValidShadowIds)
    }

    override suspend fun syncAll() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.TIMETABLE_PERIODS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<TimetablePeriodDto>()
            if (page.isEmpty()) break

            val entities = page.map { dto -> val sid = dto.sessionId ?: ""; dto.toEntity(sid, deptOf(sid)) }
            val (deleted, active) = entities.partition { it.isDeleted }
            periodDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.TIMETABLE_PERIODS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
        syncAllPeriodLinks()
    }

    /**
     * Global merged-lecture link sync, run after [syncAll]'s own period pagination: the admin timetable
     * editor reads its grid from the local cache built by [syncAll] (not per-session [syncSession]), so
     * shadow rows and linked-session chips must be kept current here too. Not checkpointed -- college-wide
     * merges are expected to be few, so refetching them in full each time is simplest and cheap enough.
     */
    private suspend fun syncAllPeriodLinks() {
        var offset = 0L
        val allLinks = mutableListOf<PeriodSessionDto>()
        while (true) {
            val page = postgrest.from(SupabaseTables.PERIOD_SESSIONS).select {
                filter { eq("is_deleted", false) }
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<PeriodSessionDto>()
            if (page.isEmpty()) break
            allLinks += page
            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }
        val linkedPeriodIds = allLinks.mapNotNull { it.periodId }.distinct()
        val primarySessionIds = linkedPeriodIds.mapNotNull { pid ->
            postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select { filter { eq("id", pid) } }
                .decodeList<TimetablePeriodDto>().firstOrNull()?.sessionId
        }
        // Every session that currently has a link, plus every session whose LOCAL cache still remembers one
        // (so a merge dropped entirely since the last sync gets its stale chip/shadow rows cleared too).
        val sessionsToRefresh = (primarySessionIds + allLinks.mapNotNull { it.sessionId } + periodDao.getSessionIdsWithLinks()).distinct()
        sessionsToRefresh.forEach { sid -> syncPeriodLinksFor(sid) }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
