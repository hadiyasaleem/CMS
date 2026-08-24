package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.CalendarEventDao
import com.mbd.cmscommon.data.local.dao.FineDao
import com.mbd.cmscommon.data.mapper.CalendarEventMapper
import com.mbd.cmscommon.data.mapper.FineMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.data.remote.dto.FineDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmscommon.domain.repository.FineRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject

private const val RECORDS_DELTA_PAGE_SIZE = 500L

private fun SessionManager.syncOwnerKey(): String = accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

class CalendarRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val calendarEventDao: CalendarEventDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : CalendarRepository {

    override suspend fun getEvents(): List<CalendarEvent> {
        runCatching { syncEvents() }
        return calendarEventDao.getAll().map { CalendarEventMapper.entityToDomain(it) }
    }

    override suspend fun createEvent(event: CalendarEvent, createdBy: String) {
        val dto = CalendarEventDto(
            title = event.title.trim(),
            eventType = event.eventType.ifBlank { "EVENT" },
            startDate = event.startDate,
            endDate = event.endDate?.trim()?.takeIf { it.isNotBlank() },
            startTime = event.startTime?.trim()?.takeIf { it.isNotBlank() },
            endTime = event.endTime?.trim()?.takeIf { it.isNotBlank() },
            description = event.description?.trim()?.takeIf { it.isNotBlank() },
            venue = event.venue?.trim()?.takeIf { it.isNotBlank() },
            audience = event.audience.ifBlank { "ALL" },
            deptId = event.deptId?.trim()?.takeIf { it.isNotBlank() },
            sessionId = event.sessionId?.trim()?.takeIf { it.isNotBlank() },
            createdBy = createdBy,
        )
        val inserted = postgrest.from(SupabaseTables.CALENDAR_EVENTS).insert(dto) { select() }.decodeList<CalendarEventDto>().first()
        calendarEventDao.upsertAll(listOf(CalendarEventMapper.dtoToEntity(inserted)))
    }

    override suspend fun deleteEvent(id: String) {
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        calendarEventDao.deleteById(id)
    }

    suspend fun syncEvents() {
        val ownerKey = sessionManager.syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.CALENDAR_EVENTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.CALENDAR_EVENTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + RECORDS_DELTA_PAGE_SIZE - 1)
            }.decodeList<CalendarEventDto>()
            if (page.isEmpty()) break

            val entities = page.map { CalendarEventMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            calendarEventDao.applyDelta(active, deleted.map { it.eventId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < RECORDS_DELTA_PAGE_SIZE) break
            offset += RECORDS_DELTA_PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.CALENDAR_EVENTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }
}

class FineRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val fineDao: FineDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : FineRepository {

    override suspend fun getFines(sessionId: String, rollNumber: String): List<Fine> {
        runCatching { syncFines(sessionId, rollNumber) }
        return fineDao.getForStudent(sessionId, rollNumber).map { FineMapper.entityToDomain(it) }
    }

    override suspend fun issueFine(sessionId: String, rollNumber: String, category: String, amount: Double, reason: String, issuedBy: String) {
        val dto = FineDto(
            sessionId = sessionId,
            rollNumber = rollNumber,
            category = category,
            amount = amount,
            reason = reason,
            issuedBy = issuedBy,
            issuedAt = PgTime.format(Instant.now()),
        )
        val inserted = postgrest.from(SupabaseTables.FINES).insert(dto) { select() }.decodeList<FineDto>().first()
        fineDao.upsertAll(listOf(FineMapper.dtoToEntity(inserted)))
    }

    override suspend fun deleteFine(id: String) {
        postgrest.from(SupabaseTables.FINES).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        fineDao.deleteById(id)
    }

    suspend fun syncFines(sessionId: String, rollNumber: String) {
        val ownerKey = sessionManager.syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("session" to sessionId, "roll" to rollNumber)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.FINES, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.FINES).select {
                filter {
                    eq("session_id", sessionId)
                    eq("roll_number", rollNumber)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + RECORDS_DELTA_PAGE_SIZE - 1)
            }.decodeList<FineDto>()
            if (page.isEmpty()) break

            val entities = page.map { FineMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            fineDao.applyDelta(active, deleted.map { it.fineId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < RECORDS_DELTA_PAGE_SIZE) break
            offset += RECORDS_DELTA_PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.FINES, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }
}
