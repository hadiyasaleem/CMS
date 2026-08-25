package com.mbd.cmsdesktop.data.repository

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * No local persistence. [periods] is one flat list spanning every session ever synced —
 * [observeAllForDay]/[observeMyPeriods] read across the whole cache, [observeDay]/[observeWeek]
 * filter to one session. [syncSession] filterNot-replaces just its own session's slice.
 */
@Singleton
class DesktopSessionTimetableRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : SessionTimetableRepository {

    private val periods = MutableStateFlow<List<SessionPeriod>>(emptyList())

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
        periods.value = periods.value.filterNot { it.sessionId == sessionId } + rows
    }
}
