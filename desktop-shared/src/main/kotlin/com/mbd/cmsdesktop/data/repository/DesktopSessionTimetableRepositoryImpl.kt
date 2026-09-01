package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopSessionTimetableMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Durable cache-first timetable repository. */
@Singleton
class DesktopSessionTimetableRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : SessionTimetableRepository {

    private val periods = MutableStateFlow(rows().filterNot { it.isDeleted }.map(DesktopSessionTimetableMapper::dtoToDomain))

    override fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>> =
        periods.asStateFlow().map { list -> list.filter { it.sessionId == sessionId && it.day == day } }

    override fun observeWeek(sessionId: String): Flow<List<SessionPeriod>> =
        periods.asStateFlow().map { list -> list.filter { it.sessionId == sessionId } }

    override fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>> =
        periods.asStateFlow().map { list -> list.filter { it.teacherId == teacherId } }

    override fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>> =
        periods.asStateFlow().map { list -> list.filter { it.day == day } }

    override suspend fun savePeriod(period: SessionPeriod) {
        val dto = DesktopSessionTimetableMapper.domainToDto(period)
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).upsert(dto) { onConflict = "primary_session_id,day,start_time" }
        writeMerged(listOf(dto))
    }

    override suspend fun removePeriod(period: SessionPeriod) {
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).update({ set("is_deleted", true) }) {
            filter {
                eq("primary_session_id", period.sessionId)
                eq("day", period.day.name)
                eq("start_time", period.startTime)
            }
        }
        val key = keyOf(DesktopSessionTimetableMapper.domainToDto(period))
        writeRows(rows().filterNot { keyOf(it) == key })
    }

    override suspend fun syncSession(sessionId: String) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.TIMETABLE_PERIODS,
            SyncCheckpointDefaults.scoped("session" to sessionId),
            TimetablePeriodDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
                filter {
                    eq("primary_session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeMerged(delta)
    }

    private fun rows() = store.readRows(CACHE_FILE, TimetablePeriodDto.serializer())

    private fun keyOf(dto: TimetablePeriodDto) =
        "${dto.sessionId}|${dto.day}|${java.time.LocalTime.parse(dto.startTime)}"

    private fun writeMerged(delta: List<TimetablePeriodDto>) {
        writeRows(mergeIncrementalDelta(rows(), delta, ::keyOf, TimetablePeriodDto::isDeleted))
    }

    private fun writeRows(updated: List<TimetablePeriodDto>) {
        store.writeRows(CACHE_FILE, TimetablePeriodDto.serializer(), updated)
        periods.value = updated.filterNot { it.isDeleted }.map(DesktopSessionTimetableMapper::dtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "timetable-periods.json" }
}
