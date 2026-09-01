package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopCalendarMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.CalendarEventDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.repository.CalendarRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/** Durable cache-first calendar repository. */
@Singleton
class DesktopCalendarRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : CalendarRepository {
    override suspend fun getEvents(): List<CalendarEvent> =
        cachedRows().filterNot { it.isDeleted }.map(DesktopCalendarMapper::dtoToDomain).sortedBy { it.startDate }

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.CALENDAR_EVENTS, SyncCheckpointDefaults.globalScope(),
            CalendarEventDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.CALENDAR_EVENTS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    override suspend fun createEvent(event: CalendarEvent, createdBy: String) {
        val inserted = postgrest.from(SupabaseTables.CALENDAR_EVENTS)
            .insert(DesktopCalendarMapper.domainToDto(event, createdBy)) { select() }
            .decodeList<CalendarEventDto>()
        writeMerged(inserted)
    }

    override suspend fun deleteEvent(id: String) {
        postgrest.from(SupabaseTables.CALENDAR_EVENTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        store.writeRows(CACHE_FILE, CalendarEventDto.serializer(), cachedRows().filterNot { it.id == id })
    }

    private fun cachedRows() = store.readRows(CACHE_FILE, CalendarEventDto.serializer())

    private fun writeMerged(delta: List<CalendarEventDto>) {
        store.writeRows(CACHE_FILE, CalendarEventDto.serializer(), mergeIncrementalDelta(
            cachedRows(), delta, { it.id ?: "entity:${it.entityId}" }, CalendarEventDto::isDeleted,
        ))
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "calendar-events.json" }
}
