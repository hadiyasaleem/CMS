package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.SessionAttendanceDao
import com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SessionAttendanceRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val attendanceDao: SessionAttendanceDao,
    private val sessionDao: AcademicSessionDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : SessionAttendanceRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private fun toDailyMark(entity: SessionAttendanceRowEntity): DailyAttendanceMark = DailyAttendanceMark(
        rollNumber = entity.rollNumber,
        date = LocalDate.parse(entity.date),
        status = runCatching { AttendanceStatus.valueOf(entity.status) }.getOrDefault(AttendanceStatus.PRESENT),
        isLate = entity.isLate,
        remark = entity.remark,
        lectureTopic = entity.lectureTopic,
    )

    private fun attendanceLocalId(dto: AttendanceRowDto): String = "${dto.sessionId}_${dto.courseCode}_${dto.date}_${dto.rollNumber}"

    private fun AttendanceRowDto.toEntity(): SessionAttendanceRowEntity = SessionAttendanceRowEntity(
        id = attendanceLocalId(this),
        sessionId = sessionId ?: "",
        semester = semester,
        courseCode = courseCode ?: "",
        date = date ?: "",
        rollNumber = rollNumber ?: "",
        status = status ?: "",
        teacherEmail = teacherEmail ?: "",
        isLate = isLate,
        remark = remark,
        lectureTopic = lectureTopic,
        recordedAt = PgTime.parseOrEpoch(recordedAt).toEpochMilli(),
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
    )

    private fun toTallies(rows: List<SessionAttendanceRowEntity>): List<SessionAttendanceTallyEntity> =
        rows.groupBy { it.courseCode to it.rollNumber }.map { (key, groupRows) ->
            val (courseCode, rollNumber) = key
            val sessionId = groupRows.first().sessionId
            SessionAttendanceTallyEntity(
                id = "${sessionId}_${courseCode}_$rollNumber",
                sessionId = sessionId,
                courseCode = courseCode,
                rollNumber = rollNumber,
                present = groupRows.count { it.status == "PRESENT" },
                absent = groupRows.count { it.status == "ABSENT" },
                leave = groupRows.count { it.status == "LEAVE" },
            )
        }

    override fun observeTallies(sessionId: String, courseCode: String): Flow<List<AttendanceTally>> =
        attendanceDao.observeTalliesFor(sessionId, courseCode)
            .map { rows -> toTallies(rows).map { AttendanceTally(it.rollNumber, it.present, it.absent, it.leave, it.courseCode) } }

    override fun observeTalliesForSession(sessionId: String): Flow<List<AttendanceTally>> =
        attendanceDao.observeTalliesForSession(sessionId)
            .map { rows -> toTallies(rows).map { AttendanceTally(it.rollNumber, it.present, it.absent, it.leave, it.courseCode) } }

    override fun observeStudentTallies(sessionId: String, rollNumber: String): Flow<List<AttendanceTally>> =
        attendanceDao.observeTalliesForStudent(sessionId, rollNumber)
            .map { rows -> toTallies(rows).map { AttendanceTally(it.rollNumber, it.present, it.absent, it.leave, it.courseCode) } }

    override suspend fun markAttendance(
        sessionId: String,
        courseCode: String,
        date: LocalDate,
        teacherId: String,
        records: Map<String, AttendanceEntry>,
        lectureTopic: String?,
    ) {
        val semester = sessionDao.getById(sessionId)?.currentSemester ?: 1
        val rows = records.map { (roll, entry) ->
            AttendanceRowDto(
                sessionId = sessionId,
                semester = semester,
                courseCode = courseCode,
                date = date.toString(),
                rollNumber = roll,
                status = entry.status.name,
                teacherEmail = teacherId,
                isLate = entry.isLate,
                remark = entry.remark?.trim()?.takeIf { it.isNotBlank() },
                lectureTopic = lectureTopic?.trim()?.takeIf { it.isNotBlank() },
            )
        }
        if (rows.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).insert(rows)
        }
        syncSummary(sessionId, courseCode)
    }

    override suspend fun isMarkedOn(sessionId: String, courseCode: String, date: LocalDate): Boolean {
        runCatching { syncSummary(sessionId, courseCode) }
        return attendanceDao.getMarkedOn(sessionId, courseCode, date.toString()) != null
    }

    override suspend fun marksBetween(sessionId: String, courseCode: String, from: LocalDate, to: LocalDate): List<DailyAttendanceMark> {
        runCatching { syncSummary(sessionId, courseCode) }
        return attendanceDao.getRowsBetween(sessionId, courseCode, from.toString(), to.toString()).map { toDailyMark(it) }
    }

    override suspend fun semesterMarks(sessionId: String, semester: Int): List<DailyAttendanceMark> {
        runCatching { syncSession(sessionId) }
        return attendanceDao.getRowsForSemester(sessionId, semester).map { toDailyMark(it) }
    }

    override suspend fun syncSummary(sessionId: String, courseCode: String) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId, "course_code" to courseCode)
        syncAttendanceDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
                filter {
                    eq("session_id", sessionId)
                    eq("course_code", courseCode)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("entity_id", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<AttendanceRowDto>()
        }
    }

    override suspend fun syncSession(sessionId: String) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId)
        syncAttendanceDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("entity_id", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<AttendanceRowDto>()
        }
    }

    private suspend fun syncAttendanceDelta(scopeKey: String, fetchPage: suspend (since: String, offset: Long) -> List<AttendanceRowDto>) {
        val ownerKey = syncOwnerKey()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_ATTENDANCE, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = fetchPage(since, offset)
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity() }
            val (deleted, active) = entities.partition { it.isDeleted }
            attendanceDao.applyRowDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.SESSION_ATTENDANCE, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
