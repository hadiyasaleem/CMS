package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionAttendanceMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.data.remote.dto.AttendanceSummaryRowDto
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Not in [SupabaseTables] — the constants list has no summary-view entry, so it's kept local. */
private const val SESSION_ATTENDANCE_SUMMARY_TABLE = "session_attendance_summary"

/**
 * Attendance *tallies* are backed by the server-side `session_attendance_summary` view (already
 * aggregated per session/course/roll), not by re-aggregating raw `session_attendance` rows
 * client-side — [tallies] caches [Tally] rows straight out of that view. Day-level queries
 * ([isMarkedOn], [marksBetween], [semesterMarks]) go straight to the raw `session_attendance`
 * table on every call instead; they don't read from [tallies] at all.
 */
@Singleton
class DesktopSessionAttendanceRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : SessionAttendanceRepository {

    private data class Tally(
        val sessionId: String,
        val courseCode: String,
        val rollNumber: String,
        val present: Int,
        val absent: Int,
        val leave: Int,
    )

    private val tallies = MutableStateFlow<List<Tally>>(emptyList())

    private fun Tally.toDomain(): AttendanceTally = AttendanceTally(
        rollNumber = rollNumber,
        present = present,
        absent = absent,
        leave = leave,
        courseCode = courseCode,
    )

    private fun AttendanceSummaryRowDto.toTally(sessionId: String): Tally = Tally(
        sessionId = sessionId,
        courseCode = courseCode ?: "",
        rollNumber = rollNumber ?: "",
        present = present,
        absent = absent,
        leave = leave,
    )

    override fun observeStudentTallies(sessionId: String, rollNumber: String): Flow<List<AttendanceTally>> =
        tallies.asStateFlow().map { rows -> rows.filter { it.sessionId == sessionId && it.rollNumber == rollNumber }.map { it.toDomain() } }

    override fun observeTallies(sessionId: String, courseCode: String): Flow<List<AttendanceTally>> =
        tallies.asStateFlow().map { rows -> rows.filter { it.sessionId == sessionId && it.courseCode == courseCode }.map { it.toDomain() } }

    override fun observeTalliesForSession(sessionId: String): Flow<List<AttendanceTally>> =
        tallies.asStateFlow().map { rows -> rows.filter { it.sessionId == sessionId }.map { it.toDomain() } }

    override suspend fun isMarkedOn(sessionId: String, courseCode: String, date: LocalDate): Boolean {
        val rows = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("date", date.toString())
            }
            limit(1)
        }.decodeList<AttendanceRowDto>()
        return rows.isNotEmpty()
    }

    private suspend fun currentSemesterOf(sessionId: String): Int {
        val session = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<AcademicSessionDto>().firstOrNull()
        return session?.currentSemester ?: 1
    }

    override suspend fun markAttendance(
        sessionId: String,
        courseCode: String,
        date: LocalDate,
        teacherEmail: String,
        entries: Map<String, AttendanceEntry>,
        lectureTopic: String?,
    ) {
        val semester = currentSemesterOf(sessionId)
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
        val rows = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                gte("date", from.toString())
                lte("date", to.toString())
            }
        }.decodeList<AttendanceRowDto>()
        return rows.map { DesktopSessionAttendanceMapper.dtoToDomain(it) }
    }

    override suspend fun semesterMarks(sessionId: String, semester: Int): List<DailyAttendanceMark> {
        val rows = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
            }
        }.decodeList<AttendanceRowDto>()
        return rows.map { DesktopSessionAttendanceMapper.dtoToDomain(it) }
    }

    override suspend fun syncSummary(sessionId: String, courseCode: String) {
        val rows = postgrest.from(SESSION_ATTENDANCE_SUMMARY_TABLE).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
            }
        }.decodeList<AttendanceSummaryRowDto>()
        val mapped = rows.map { it.toTally(sessionId) }
        tallies.value = tallies.value.filterNot { it.sessionId == sessionId && it.courseCode == courseCode } + mapped
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SESSION_ATTENDANCE_SUMMARY_TABLE).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<AttendanceSummaryRowDto>()
        val mapped = rows.map { it.toTally(sessionId) }
        tallies.value = tallies.value.filterNot { it.sessionId == sessionId } + mapped
    }
}
