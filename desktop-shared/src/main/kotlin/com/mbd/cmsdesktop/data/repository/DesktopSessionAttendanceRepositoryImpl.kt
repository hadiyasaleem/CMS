package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopSessionAttendanceMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Durable cache-first raw attendance repository; tallies are derived locally. */
@Singleton
class DesktopSessionAttendanceRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : SessionAttendanceRepository {

    private val rows = MutableStateFlow(cachedRows().filterNot { it.isDeleted })

    override fun observeStudentTallies(sessionId: String, rollNumber: String): Flow<List<AttendanceTally>> =
        rows.asStateFlow().map { active ->
            tallies(active.filter { it.sessionId == sessionId && it.rollNumber == rollNumber })
        }

    override fun observeTallies(sessionId: String, courseCode: String): Flow<List<AttendanceTally>> =
        rows.asStateFlow().map { active ->
            tallies(active.filter { it.sessionId == sessionId && it.courseCode == courseCode })
        }

    override fun observeTalliesForSession(sessionId: String): Flow<List<AttendanceTally>> =
        rows.asStateFlow().map { active -> tallies(active.filter { it.sessionId == sessionId }) }

    override suspend fun isMarkedOn(sessionId: String, courseCode: String, date: LocalDate): Boolean =
        rows.value.any { it.sessionId == sessionId && it.courseCode == courseCode && it.date == date.toString() }

    override suspend fun markAttendance(
        sessionId: String,
        courseCode: String,
        date: LocalDate,
        teacherEmail: String,
        entries: Map<String, AttendanceEntry>,
        lectureTopic: String?,
    ) {
        val semester = store.readSessions().firstOrNull { it.sessionId == sessionId }?.currentSemester ?: 1
        val inserted = entries.map { (roll, entry) ->
            DesktopSessionAttendanceMapper.entryToDto(
                sessionId,
                semester,
                courseCode,
                date,
                roll,
                entry,
                teacherEmail,
                lectureTopic,
            )
        }
        if (inserted.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).insert(inserted)
            writeRows(mergeIncrementalDelta(cachedRows(), inserted, ::keyOf, AttendanceRowDto::isDeleted))
        }
    }

    override suspend fun marksBetween(
        sessionId: String,
        courseCode: String,
        from: LocalDate,
        to: LocalDate,
    ): List<DailyAttendanceMark> = rows.value.filter {
        it.sessionId == sessionId &&
            it.courseCode == courseCode &&
            dateOf(it) in from..to
    }.map(DesktopSessionAttendanceMapper::dtoToDomain)

    override suspend fun semesterMarks(sessionId: String, semester: Int): List<DailyAttendanceMark> =
        rows.value.filter { it.sessionId == sessionId && it.semester == semester }
            .map(DesktopSessionAttendanceMapper::dtoToDomain)

    override suspend fun syncSummary(sessionId: String, courseCode: String) {
        syncDelta(
            SyncCheckpointDefaults.scoped("session" to sessionId, "course" to courseCode),
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
                filter {
                    eq("session_id", sessionId)
                    eq("course_code", courseCode)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("entity_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun syncSession(sessionId: String) {
        syncDelta(SyncCheckpointDefaults.scoped("session" to sessionId)) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                order("entity_id", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    private suspend fun syncDelta(
        scope: String,
        fetchPage: suspend (since: String, from: Long, to: Long) -> List<AttendanceRowDto>,
    ) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.SESSION_ATTENDANCE,
            scope,
            AttendanceRowDto::updatedAt,
            fetchPage = fetchPage,
        )
        writeRows(mergeIncrementalDelta(cachedRows(), delta, ::keyOf, AttendanceRowDto::isDeleted))
    }

    private fun tallies(source: List<AttendanceRowDto>): List<AttendanceTally> =
        source.groupBy { it.courseCode.orEmpty() to it.rollNumber.orEmpty() }.map { (key, grouped) ->
            AttendanceTally(
                rollNumber = key.second,
                present = grouped.count { it.status == "PRESENT" },
                absent = grouped.count { it.status == "ABSENT" },
                leave = grouped.count { it.status == "LEAVE" },
                courseCode = key.first,
            )
        }

    private fun dateOf(row: AttendanceRowDto): LocalDate =
        runCatching { LocalDate.parse(row.date) }.getOrDefault(LocalDate.EPOCH)

    private fun keyOf(row: AttendanceRowDto) =
        "${row.sessionId}|${row.courseCode}|${row.date}|${row.rollNumber}"

    private fun cachedRows() = store.readRows(CACHE_FILE, AttendanceRowDto.serializer())

    private fun writeRows(updated: List<AttendanceRowDto>) {
        store.writeRows(CACHE_FILE, AttendanceRowDto.serializer(), updated)
        rows.value = updated.filterNot { it.isDeleted }
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object { const val CACHE_FILE = "session-attendance.json" }
}
