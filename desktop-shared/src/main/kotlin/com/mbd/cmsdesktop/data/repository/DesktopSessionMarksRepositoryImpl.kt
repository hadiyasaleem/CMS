package com.mbd.cmsdesktop.data.repository

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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * [marks] is one flat list of raw [Mark] rows spanning every session/course/exam ever synced —
 * the narrower `observe*` views (per course+examType, per student) filter that same list rather
 * than keeping separate caches. [sync]/[syncSession] filterNot-replace only the slice matching
 * their own scope. Semester GPA is never cached — [getSemesterGpa]/[getSemesterResults] always
 * hit `student_semester_gpa` directly, matching [SessionFeeRepositoryImpl]'s no-cache shape.
 */
@Singleton
class DesktopSessionMarksRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : SessionMarksRepository {

    private data class Mark(
        val sessionId: String,
        val courseCode: String,
        val examType: String,
        val rollNumber: String,
        val score: Int?,
        val maxMarks: Int,
        val wasAbsent: Boolean,
        val remarks: String?,
    )

    private val marks = MutableStateFlow<List<Mark>>(emptyList())

    private fun MarkRowDto.toRow(): Mark = Mark(
        sessionId = sessionId ?: "",
        courseCode = courseCode ?: "",
        examType = examType ?: "",
        rollNumber = rollNumber ?: "",
        score = score,
        maxMarks = maxMarks,
        wasAbsent = wasAbsent,
        remarks = remarks,
    )

    private fun Mark.toDto(): MarkRowDto = MarkRowDto(
        sessionId = sessionId,
        courseCode = courseCode,
        examType = examType,
        rollNumber = rollNumber,
        score = score,
        maxMarks = maxMarks,
        wasAbsent = wasAbsent,
        remarks = remarks,
    )

    override fun observeScores(sessionId: String, courseCode: String, examType: ExamType): Flow<Map<String, Int>> =
        marks.asStateFlow().map { rows ->
            rows.filter { it.sessionId == sessionId && it.courseCode == courseCode && it.examType == examType.name }
                .associate { it.rollNumber to (it.score ?: 0) }
        }

    override fun observeAbsentRolls(sessionId: String, courseCode: String, examType: ExamType): Flow<Set<String>> =
        marks.asStateFlow().map { rows ->
            rows.filter { it.sessionId == sessionId && it.courseCode == courseCode && it.examType == examType.name && it.wasAbsent }
                .map { it.rollNumber }
                .toSet()
        }

    override fun observeStudentMarks(sessionId: String, rollNumber: String): Flow<List<SubjectExamScore>> =
        marks.asStateFlow().map { rows ->
            rows.filter { it.sessionId == sessionId && it.rollNumber == rollNumber }
                .mapNotNull { DesktopSessionMarksMapper.dtoToDomain(it.toDto()) }
        }

    private suspend fun currentSemesterOf(sessionId: String): Int {
        val session = postgrest.from(SupabaseTables.ACADEMIC_SESSIONS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<AcademicSessionDto>().firstOrNull()
        return session?.currentSemester ?: 1
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
        val semester = currentSemesterOf(sessionId)
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
        }.decodeList<MarkRowDto>().filterNot { it.isDeleted }.map { it.toRow() }
        marks.value = marks.value.filterNot {
            it.sessionId == sessionId && it.courseCode == courseCode && it.examType == examType.name
        } + rows
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_MARKS).select {
            filter { eq("session_id", sessionId) }
        }.decodeList<MarkRowDto>().filterNot { it.isDeleted }.map { it.toRow() }
        marks.value = marks.value.filterNot { it.sessionId == sessionId } + rows
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
    }

    override suspend fun getSemesterGpa(sessionId: String, rollNumber: String): List<SemesterGpa> {
        val rows = postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
            filter {
                eq("session_id", sessionId)
                eq("roll_number", rollNumber)
            }
        }.decodeList<SemesterGpaDto>().filterNot { it.isDeleted }
        return rows.map { DesktopSessionMarksMapper.gpaDtoToDomain(it) }
    }

    override suspend fun getSemesterResults(sessionId: String, semester: Int): List<SemesterGpa> {
        val rows = postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
            }
        }.decodeList<SemesterGpaDto>().filterNot { it.isDeleted }
        return rows.map { DesktopSessionMarksMapper.gpaDtoToDomain(it) }
    }
}
