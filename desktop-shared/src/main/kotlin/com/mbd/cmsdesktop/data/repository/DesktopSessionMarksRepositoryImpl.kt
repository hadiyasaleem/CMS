package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopSessionMarksMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.SemesterGpaDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SubjectExamScore
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/** Durable cache-first marks and semester-result repository. */
@Singleton
class DesktopSessionMarksRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : SessionMarksRepository {

    private val marks = MutableStateFlow(markRows().filterNot { it.isDeleted })

    override fun observeScores(sessionId: String, courseCode: String, examType: ExamType): Flow<Map<String, Int>> =
        marks.asStateFlow().map { rows ->
            rows.filter { it.sessionId == sessionId && it.courseCode == courseCode && it.examType == examType.name }
                .associate { it.rollNumber.orEmpty() to (it.score ?: 0) }
        }

    override fun observeAbsentRolls(sessionId: String, courseCode: String, examType: ExamType): Flow<Set<String>> =
        marks.asStateFlow().map { rows ->
            rows.filter {
                it.sessionId == sessionId &&
                    it.courseCode == courseCode &&
                    it.examType == examType.name &&
                    it.wasAbsent
            }.map { it.rollNumber.orEmpty() }.toSet()
        }

    override fun observeStudentMarks(sessionId: String, rollNumber: String): Flow<List<SubjectExamScore>> =
        marks.asStateFlow().map { rows ->
            rows.filter { it.sessionId == sessionId && it.rollNumber == rollNumber }
                .mapNotNull(DesktopSessionMarksMapper::dtoToDomain)
        }

    override suspend fun saveScores(
        sessionId: String,
        courseCode: String,
        examType: ExamType,
        teacherEmail: String,
        scores: Map<String, Int>,
        absentRolls: Set<String>,
        examDate: LocalDate?,
    ) {
        val semester = store.readSessions().firstOrNull { it.sessionId == sessionId }?.currentSemester ?: 1
        val rows = scores.map { (roll, score) ->
            DesktopSessionMarksMapper.domainToDto(
                sessionId = sessionId,
                semester = semester,
                courseCode = courseCode,
                examType = examType,
                rollNumber = roll,
                score = score,
                wasAbsent = absentRolls.contains(roll),
                examDate = examDate,
                teacherEmail = teacherEmail,
            )
        }
        if (rows.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_MARKS).upsert(rows) {
                onConflict = "session_id,semester,course_code,exam_type,roll_number"
            }
            writeMarks(mergeIncrementalDelta(markRows(), rows, ::markKey, MarkRowDto::isDeleted))
        }
    }

    override suspend fun sync(sessionId: String, courseCode: String, examType: ExamType) {
        syncMarks(
            SyncCheckpointDefaults.scoped(
                "session" to sessionId,
                "course" to courseCode,
                "exam" to examType.name,
            ),
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_MARKS).select {
                filter {
                    eq("session_id", sessionId)
                    eq("course_code", courseCode)
                    eq("exam_type", examType.name)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun syncSession(sessionId: String) {
        val scope = SyncCheckpointDefaults.scoped("session" to sessionId)
        syncMarks(scope) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_MARKS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }

        val gpaDelta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.STUDENT_SEMESTER_GPA,
            scope,
            SemesterGpaDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        store.writeRows(
            GPA_CACHE_FILE,
            SemesterGpaDto.serializer(),
            mergeIncrementalDelta(gpaRows(), gpaDelta, ::gpaKey, SemesterGpaDto::isDeleted),
        )
    }

    override suspend fun recordSemesterResult(
        sessionId: String,
        rollNumber: String,
        semester: Int,
        gpa: Double,
        cgpa: Double,
        termLabel: String?,
        resultStatus: String,
        classPosition: Int?,
        remarks: String?,
        supplyCourses: List<String>,
    ) {
        val params = buildJsonObject {
            put("p_session", sessionId)
            put("p_roll", rollNumber)
            put("p_semester", semester)
            put("p_gpa", gpa)
            put("p_cgpa", cgpa)
            put("p_result", resultStatus.ifBlank { "PENDING" })
            termLabel?.trim()?.takeIf { it.isNotBlank() }?.let { put("p_term_label", it) }
            classPosition?.let { put("p_class_position", it) }
            remarks?.trim()?.takeIf { it.isNotBlank() }?.let { put("p_remarks", it) }
            putJsonArray("p_supply") { supplyCourses.forEach { add(JsonPrimitive(it)) } }
        }
        postgrest.rpc(SupabaseTables.RPC_RECORD_SEMESTER_RESULT, params)
        val row = SemesterGpaDto(
            sessionId = sessionId,
            rollNumber = rollNumber,
            semester = semester,
            gpa = gpa,
            cgpa = cgpa,
            termLabel = termLabel?.trim()?.takeIf { it.isNotBlank() },
            resultStatus = resultStatus.ifBlank { "PENDING" },
            classPosition = classPosition,
            remarks = remarks?.trim()?.takeIf { it.isNotBlank() },
            supplyCourses = supplyCourses,
            updatedAt = Instant.now().toString(),
        )
        store.writeRows(
            GPA_CACHE_FILE,
            SemesterGpaDto.serializer(),
            mergeIncrementalDelta(gpaRows(), listOf(row), ::gpaKey, SemesterGpaDto::isDeleted),
        )
    }

    override suspend fun getSemesterGpa(sessionId: String, rollNumber: String): List<SemesterGpa> =
        gpaRows().filter {
            it.sessionId == sessionId && it.rollNumber == rollNumber && !it.isDeleted
        }.map(DesktopSessionMarksMapper::gpaDtoToDomain)

    override suspend fun getSemesterResults(sessionId: String, semester: Int): List<SemesterGpa> =
        gpaRows().filter {
            it.sessionId == sessionId && it.semester == semester && !it.isDeleted
        }.map(DesktopSessionMarksMapper::gpaDtoToDomain)

    private suspend fun syncMarks(
        scope: String,
        fetchPage: suspend (since: String, from: Long, to: Long) -> List<MarkRowDto>,
    ) {
        val delta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.SESSION_MARKS,
            scope,
            MarkRowDto::updatedAt,
            fetchPage = fetchPage,
        )
        writeMarks(mergeIncrementalDelta(markRows(), delta, ::markKey, MarkRowDto::isDeleted))
    }

    private fun markRows() = store.readRows(MARKS_CACHE_FILE, MarkRowDto.serializer())

    private fun gpaRows() = store.readRows(GPA_CACHE_FILE, SemesterGpaDto.serializer())

    private fun markKey(row: MarkRowDto) =
        "${row.sessionId}|${row.semester}|${row.courseCode}|${row.examType}|${row.rollNumber}"

    private fun gpaKey(row: SemesterGpaDto) =
        "${row.sessionId}|${row.rollNumber}|${row.semester}"

    private fun writeMarks(rows: List<MarkRowDto>) {
        store.writeRows(MARKS_CACHE_FILE, MarkRowDto.serializer(), rows)
        marks.value = rows.filterNot { it.isDeleted }
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val MARKS_CACHE_FILE = "session-marks.json"
        const val GPA_CACHE_FILE = "semester-gpa.json"
    }
}
