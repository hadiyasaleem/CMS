package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopSessionMarksMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.SemesterGpaDto
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SubjectExamScore
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import io.github.jan.supabase.postgrest.Postgrest
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Desktop repos are always-online: no local persistence, `sync()`/`syncSession()` do a full
 * re-fetch into an in-memory cache that the `observe*` methods just derive from.
 *
 * Caching keys: [marksCache] and [gpaCache] are both keyed by sessionId (matching every method
 * in this interface being session-scoped), holding the raw rows for that session. The narrower
 * `observe*`/`get*` views (per course+examType, per student, per semester) are derived by
 * filtering that per-session list rather than maintaining separate caches per filter combo —
 * [sync] and the two scoped GPA fetches only replace the subset of the cached list matching
 * their own filter (via [mergeMarks]/[mergeGpa]), so a narrow sync never evicts unrelated rows
 * that a broader `syncSession`/earlier call had already cached.
 */
@Singleton
class DesktopSessionMarksRepository @Inject constructor(
    private val postgrest: Postgrest,
) : SessionMarksRepository {

    private val marksCache = MutableStateFlow<Map<String, List<MarkRowDto>>>(emptyMap())
    private val gpaCache = MutableStateFlow<Map<String, List<SemesterGpa>>>(emptyMap())

    override fun observeScores(sessionId: String, courseCode: String, examType: ExamType): Flow<Map<String, Int>> =
        marksCache.map { cache ->
            cache[sessionId].orEmpty()
                .filter { it.courseCode == courseCode && it.examType == examType.name }
                .associate { (it.rollNumber ?: "") to (it.score ?: 0) }
        }

    override fun observeAbsentRolls(sessionId: String, courseCode: String, examType: ExamType): Flow<Set<String>> =
        marksCache.map { cache ->
            cache[sessionId].orEmpty()
                .filter { it.courseCode == courseCode && it.examType == examType.name && it.wasAbsent }
                .mapNotNull { it.rollNumber }
                .toSet()
        }

    override fun observeStudentMarks(sessionId: String, rollNumber: String): Flow<List<SubjectExamScore>> =
        marksCache.map { cache ->
            cache[sessionId].orEmpty()
                .filter { it.rollNumber == rollNumber }
                .mapNotNull { DesktopSessionMarksMapper.dtoToDomain(it) }
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
        val semester = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter { eq("session_id", sessionId) }
        }.decodeSingleOrNull<AcademicSessionDto>()?.currentSemester ?: 1

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
        }
        sync(sessionId, courseCode, examType)
    }

    override suspend fun sync(sessionId: String, courseCode: String, examType: ExamType) {
        val rows = postgrest.from(SupabaseTables.SESSION_MARKS).select {
            filter {
                eq("session_id", sessionId)
                eq("course_code", courseCode)
                eq("exam_type", examType.name)
            }
        }.decodeList<MarkRowDto>().filterNot { it.isDeleted }
        mergeMarks(sessionId, rows) { it.courseCode == courseCode && it.examType == examType.name }
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_MARKS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<MarkRowDto>().filterNot { it.isDeleted }
        mergeMarks(sessionId, rows) { true }
    }

    private fun mergeMarks(sessionId: String, rows: List<MarkRowDto>, replaces: (MarkRowDto) -> Boolean) {
        marksCache.update { cache ->
            val kept = cache[sessionId].orEmpty().filterNot(replaces)
            cache + (sessionId to (kept + rows))
        }
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
        val params: JsonObject = buildJsonObject {
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
        getSemesterGpa(sessionId, rollNumber)
    }

    override suspend fun getSemesterGpa(sessionId: String, rollNumber: String): List<SemesterGpa> {
        val rows = postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
            }
        }.decodeList<SemesterGpaDto>().filterNot { it.isDeleted }.map { DesktopSessionMarksMapper.gpaDtoToDomain(it) }
        mergeGpa(sessionId, rows) { it.rollNumber == rollNumber }
        return rows
    }

    override suspend fun getSemesterResults(sessionId: String, semester: Int): List<SemesterGpa> {
        val rows = postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
            }
        }.decodeList<SemesterGpaDto>().filterNot { it.isDeleted }.map { DesktopSessionMarksMapper.gpaDtoToDomain(it) }
        mergeGpa(sessionId, rows) { it.semester == semester }
        return rows
    }

    private fun mergeGpa(sessionId: String, rows: List<SemesterGpa>, replaces: (SemesterGpa) -> Boolean) {
        gpaCache.update { cache ->
            val kept = cache[sessionId].orEmpty().filterNot(replaces)
            cache + (sessionId to (kept + rows))
        }
    }
}
