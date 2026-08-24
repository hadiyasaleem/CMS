package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.SessionPeriodDao
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.mapper.AcademicStructureMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.DayOfWeek
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
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
        dto.id ?: "${dto.sessionId ?: fallbackSessionId}_${dto.day}_${dto.startTime}"

    private fun TimetablePeriodDto.toEntity(sessionId: String, deptId: String): SessionPeriodEntity = SessionPeriodEntity(
        id = periodLocalId(this, sessionId),
        sessionId = this.sessionId ?: sessionId,
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
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

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
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).upsert(dto) { onConflict = "primary_session_id,day,start_time" }
        resyncDay(period.sessionId, period.day)
    }

    override suspend fun removePeriod(period: SessionPeriod) {
        postgrest.from(SupabaseTables.TIMETABLE_PERIODS).update({ set("is_deleted", true) }) {
            filter {
                eq("primary_session_id", period.sessionId)
                eq("day", period.day.name)
                eq("start_time", period.startTime)
            }
        }
        resyncDay(period.sessionId, period.day)
    }

    private suspend fun resyncDay(sessionId: String, day: DayOfWeek) {
        val deptId = deptOf(sessionId)
        val rows = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
            filter {
                eq("primary_session_id", sessionId)
                eq("day", day.name)
            }
        }.decodeList<TimetablePeriodDto>()
        periodDao.deleteForSessionDay(sessionId, day.name)
        val entities = rows.filterNot { it.isDeleted }.map { it.toEntity(sessionId, deptId) }
        if (entities.isNotEmpty()) periodDao.upsertAll(entities)
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
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
