package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionAttendanceMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * Desktop repos are always-online: no local persistence, `syncSession()`/`syncSummary()` do a
 * full re-fetch into an in-memory [MutableStateFlow] cache that `observe*` derives tallies from.
 *
 * The cache is a `Map<sessionId, List<AttendanceRow>>` — session-scoped, holding every raw
 * attendance row (across all courses) for that session, since [observeTalliesForSession] needs
 * the whole session and [semesterMarks]/[marksBetween] filter by semester/date range out of the
 * same slice. [AttendanceRow] is a private row shape (this repo's stand-in for mobile's Room
 * entity) that both the tally aggregation and [DailyAttendanceMark] mapping read from.
 *
 * [markAttendance] mirrors mobile exactly except it has no `AcademicSessionDao` to read
 * `currentSemester` from — it looks that up with a direct Postgrest call against
 * `academic_sessions` by `session_id`, matching the fix already used by the sibling marks repo.
 */
@Singleton
class DesktopSessionAttendanceRepository @Inject constructor(
    private val postgrest: Postgrest,
) : SessionAttendanceRepository {

    private data class AttendanceRow(
        val courseCode: String,
        val rollNumber: String,
        val date: String,
        val status: String,
        val isLate: Boolean,
        val remark: String?,
        val lectureTopic: String?,
        val semester: Int,
    )

    private val cache = MutableStateFlow<Map<String, List<AttendanceRow>>>(emptyMap())

    private fun AttendanceRowDto.toRow(): AttendanceRow = AttendanceRow(
        courseCode = courseCode ?: "",
        rollNumber = rollNumber ?: "",
        date = date ?: "",
        status = status ?: "",
        isLate = isLate,
        remark = remark,
        lectureTopic = lectureTopic,
        semester = semester,
    )

    private fun AttendanceRow.toDailyMark(): DailyAttendanceMark = DesktopSessionAttendanceMapper.dtoToDomain(
        AttendanceRowDto(
            courseCode = courseCode,
            rollNumber = rollNumber,
            date = date,
            status = status,
            isLate = isLate,
            remark = remark,
            lectureTopic = lectureTopic,
        ),
    )

    private fun tallies(rows: List<AttendanceRow>): List<AttendanceTally> =
        rows.groupBy { it.courseCode to it.rollNumber }.map { (key, groupRows) ->
            val (courseCode, rollNumber) = key
            AttendanceTally(
                rollNumber = rollNumber,
                present = groupRows.count { it.status == "PRESENT" },
                absent = groupRows.count { it.status == "ABSENT" },
                leave = groupRows.count { it.status == "LEAVE" },
                courseCode = courseCode,
            )
        }

    override fun observeStudentTallies(sessionId: String, rollNumber: String): Flow<List<AttendanceTally>> =
        cache.asStateFlow().map { rows -> tallies(rows[sessionId]?.filter { it.rollNumber == rollNumber } ?: emptyList()) }

    override fun observeTallies(sessionId: String, courseCode: String): Flow<List<AttendanceTally>> =
        cache.asStateFlow().map { rows -> tallies(rows[sessionId]?.filter { it.courseCode == courseCode } ?: emptyList()) }

    override fun observeTalliesForSession(sessionId: String): Flow<List<AttendanceTally>> =
        cache.asStateFlow().map { rows -> tallies(rows[sessionId] ?: emptyList()) }

    override suspend fun isMarkedOn(sessionId: String, courseCode: String, date: LocalDate): Boolean {
        runCatching { syncSummary(sessionId, courseCode) }
        return cache.value[sessionId]?.any { it.courseCode == courseCode && it.date == date.toString() } ?: false
    }

    override suspend fun markAttendance(
        sessionId: String,
        courseCode: String,
        date: LocalDate,
        teacherEmail: String,
        entries: Map<String, AttendanceEntry>,
        lectureTopic: String?,
    ) {
        val semester = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter { eq("session_id", sessionId) }
        }.decodeSingleOrNull<AcademicSessionDto>()?.currentSemester?.coerceIn(1, 8) ?: 1

        val rows = entries.map { (roll, entry) ->
            DesktopSessionAttendanceMapper.entryToDto(
                sessionId = sessionId,
                semester = semester,
                courseCode = courseCode,
                date = date,
                rollNumber = roll,
                entry = entry,
                teacherEmail = teacherEmail,
                lectureTopic = lectureTopic,
            )
        }
        if (rows.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_ATTENDANCE).insert(rows)
        }
        syncSummary(sessionId, courseCode)
    }

    override suspend fun marksBetween(sessionId: String, courseCode: String, from: LocalDate, to: LocalDate): List<DailyAttendanceMark> {
        runCatching { syncSummary(sessionId, courseCode) }
        val fromStr = from.toString()
        val toStr = to.toString()
        return (cache.value[sessionId] ?: emptyList())
            .filter { it.courseCode == courseCode && it.date >= fromStr && it.date <= toStr }
            .map { it.toDailyMark() }
    }

    override suspend fun semesterMarks(sessionId: String, semester: Int): List<DailyAttendanceMark> {
        runCatching { syncSession(sessionId) }
        return (cache.value[sessionId] ?: emptyList())
            .filter { it.semester == semester }
            .map { it.toDailyMark() }
    }

    override suspend fun syncSummary(sessionId: String, courseCode: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("is_deleted", false)
            }
            order("date", Order.ASCENDING)
        }.decodeList<AttendanceRowDto>().map { it.toRow() }
        val existing = cache.value[sessionId] ?: emptyList()
        cache.value = cache.value + (sessionId to (existing.filterNot { it.courseCode == courseCode } + rows))
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("is_deleted", false)
            }
            order("date", Order.ASCENDING)
        }.decodeList<AttendanceRowDto>().map { it.toRow() }
        cache.value = cache.value + (sessionId to rows)
    }
}
