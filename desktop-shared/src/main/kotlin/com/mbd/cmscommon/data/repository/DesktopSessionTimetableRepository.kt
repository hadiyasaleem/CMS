package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionTimetableMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.DayOfWeek
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/**
 * Desktop repos are always-online: no local persistence, `syncSession()` does a full re-fetch
 * into an in-memory cache that the `observe*` methods just derive from.
 *
 * Caching key: [cache] is keyed by sessionId (the only scope [syncSession] refreshes), holding
 * every period for that session. [observeAllForDay] and [observeMyPeriods] span *every* session
 * that has ever been synced, so they flatten across the whole map — matching mobile's local
 * Room table, which likewise accumulates rows from however many `syncSession` calls each screen
 * has triggered rather than being scoped to one session.
 */
@Singleton
class DesktopSessionTimetableRepository @Inject constructor(
    private val postgrest: Postgrest,
) : SessionTimetableRepository {

    private val cache = MutableStateFlow<Map<String, List<SessionPeriod>>>(emptyMap())

    override fun observeDay(sessionId: String, day: DayOfWeek): Flow<List<SessionPeriod>> =
        cache.map { it[sessionId].orEmpty().filter { p -> p.day == day } }

    override fun observeWeek(sessionId: String): Flow<List<SessionPeriod>> =
        cache.map { it[sessionId].orEmpty() }

    override fun observeMyPeriods(teacherId: String): Flow<List<SessionPeriod>> =
        cache.map { m -> m.values.flatten().filter { it.teacherId == teacherId } }

    override fun observeAllForDay(day: DayOfWeek): Flow<List<SessionPeriod>> =
        cache.map { m -> m.values.flatten().filter { it.day == day } }

    override suspend fun savePeriod(period: SessionPeriod) {
        val dto = DesktopSessionTimetableMapper.domainToDto(period)
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).upsert(dto) { onConflict = "primary_session_id,day,start_time" }
        syncSession(period.sessionId)
    }

    override suspend fun removePeriod(period: SessionPeriod) {
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).update({ set("is_deleted", true) }) {
            filter {
                eq("primary_session_id", period.sessionId)
                eq("day", period.day.name)
                eq("start_time", period.startTime)
            }
        }
        syncSession(period.sessionId)
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
            filter { eq("primary_session_id", sessionId) }
        }.decodeList<TimetablePeriodDto>()
            .filterNot { it.isDeleted }
            .map { DesktopSessionTimetableMapper.dtoToDomain(it) }
        cache.update { it + (sessionId to rows) }
    }
}
