package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.AcademicSessionDao
import com.mbd.cmscommon.data.local.dao.SessionMarkDao
import com.mbd.cmscommon.data.local.dao.StudentSemesterGpaDao
import com.mbd.cmscommon.data.local.entity.SessionMarkEntity
import com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.SemesterGpaDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SubjectExamScore
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

class SessionMarksRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val markDao: SessionMarkDao,
    private val gpaDao: StudentSemesterGpaDao,
    private val sessionDao: AcademicSessionDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : SessionMarksRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private fun markLocalId(dto: MarkRowDto): String = "${dto.sessionId}_${dto.courseCode}_${dto.examType}_${dto.rollNumber}"

    private fun MarkRowDto.toEntity(): SessionMarkEntity? {
        val type = examType ?: return null
        return SessionMarkEntity(
            id = markLocalId(this),
            sessionId = sessionId ?: "",
            courseCode = courseCode ?: "",
            examType = type,
            rollNumber = rollNumber ?: "",
            score = score ?: 0,
            maxMarks = maxMarks,
            wasAbsent = wasAbsent,
            remarks = remarks,
            entityId = entityId ?: 0L,
            createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
            createdBy = createdBy,
            updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
            updatedBy = updatedBy,
            isDeleted = isDeleted,
            deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
            deletedBy = deletedBy,
        )
    }

    private fun gpaLocalId(dto: SemesterGpaDto): String = "${dto.sessionId}_${dto.rollNumber}_${dto.semester}"

    private fun SemesterGpaDto.toEntity(): StudentSemesterGpaEntity = StudentSemesterGpaEntity(
        id = gpaLocalId(this),
        sessionId = sessionId ?: "",
        rollNumber = rollNumber ?: "",
        semester = semester,
        gpa = gpa,
        cgpa = cgpa,
        termLabel = termLabel,
        resultStatus = resultStatus ?: "PENDING",
        classPosition = classPosition,
        remarks = remarks,
        supplyCoursesJson = supplyCourses.joinToString(","),
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

    private fun StudentSemesterGpaEntity.toDomain(): SemesterGpa = SemesterGpa(
        sessionId = sessionId,
        rollNumber = rollNumber,
        semester = semester,
        gpa = gpa,
        cgpa = cgpa,
        termLabel = termLabel,
        resultStatus = resultStatus,
        classPosition = classPosition,
        remarks = remarks,
        supplyCourses = supplyCoursesJson.split(",").filter { it.isNotBlank() },
        entityId = entityId,
        createdAt = Instant.ofEpochMilli(createdAt),
        createdBy = createdBy,
        updatedAt = Instant.ofEpochMilli(updatedAt),
        updatedBy = updatedBy,
    )

    override fun observeScores(sessionId: String, courseCode: String, examType: ExamType): Flow<Map<String, Int>> =
        markDao.observeScores(sessionId, courseCode, examType.name).map { rows -> rows.associate { it.rollNumber to it.score } }

    override fun observeAbsentRolls(sessionId: String, courseCode: String, examType: ExamType): Flow<Set<String>> =
        markDao.observeScores(sessionId, courseCode, examType.name).map { rows -> rows.filter { it.wasAbsent }.map { it.rollNumber }.toSet() }

    override fun observeStudentMarks(sessionId: String, rollNumber: String): Flow<List<SubjectExamScore>> =
        markDao.observeForStudent(sessionId, rollNumber).map { rows ->
            rows.mapNotNull { row ->
                val type = runCatching { ExamType.valueOf(row.examType) }.getOrNull() ?: return@mapNotNull null
                SubjectExamScore(row.courseCode, type, row.score, row.maxMarks, row.wasAbsent, row.remarks)
            }
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
        val semester = sessionDao.getById(sessionId)?.currentSemester ?: 1
        val rows = scores.map { (roll, score) ->
            MarkRowDto(
                sessionId = sessionId,
                semester = semester,
                courseCode = courseCode,
                examType = examType.name,
                rollNumber = roll,
                score = score,
                maxMarks = examType.maxMarks,
                wasAbsent = absentRolls.contains(roll),
                examDate = examDate?.toString(),
                teacherEmail = teacherEmail,
            )
        }
        if (rows.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_MARKS).upsert(rows) { onConflict = "session_id,semester,course_code,exam_type,roll_number" }
        }
        markDao.deleteFor(sessionId, courseCode, examType.name)
        val entities = rows.mapNotNull { it.toEntity() }
        if (entities.isNotEmpty()) markDao.upsertAll(entities)
    }

    override suspend fun sync(sessionId: String, courseCode: String, examType: ExamType) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId, "course_code" to courseCode, "exam_type" to examType.name)
        syncMarksDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.SESSION_MARKS).select {
                filter {
                    eq("session_id", sessionId)
                    eq("course_code", courseCode)
                    eq("exam_type", examType.name)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<MarkRowDto>()
        }
    }

    override suspend fun syncSession(sessionId: String) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId)
        syncMarksDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.SESSION_MARKS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<MarkRowDto>()
        }
        syncGpaForSession(sessionId)
    }

    private suspend fun syncMarksDelta(scopeKey: String, fetchPage: suspend (since: String, offset: Long) -> List<MarkRowDto>) {
        val ownerKey = syncOwnerKey()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_MARKS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = fetchPage(since, offset)
            if (page.isEmpty()) break

            val entities = page.mapNotNull { it.toEntity() }
            val (deleted, active) = entities.partition { it.isDeleted }
            markDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.SESSION_MARKS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
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
        val now = System.currentTimeMillis()
        gpaDao.upsertAll(
            listOf(
                SemesterGpaDto(
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
                ).toEntity().copy(createdAt = now, updatedAt = now),
            ),
        )
    }

    override suspend fun getSemesterGpa(sessionId: String, rollNumber: String): List<SemesterGpa> =
        gpaDao.getForStudent(sessionId, rollNumber).map { it.toDomain() }

    override suspend fun getSemesterResults(sessionId: String, semester: Int): List<SemesterGpa> =
        gpaDao.getForSemester(sessionId, semester).map { it.toDomain() }

    private suspend fun syncGpaForSession(sessionId: String) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId)
        syncGpaDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<SemesterGpaDto>()
        }
    }

    private suspend fun syncGpaForStudent(sessionId: String, rollNumber: String) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId, "roll" to rollNumber)
        syncGpaDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
                filter {
                    eq("session_id", sessionId)
                    eq("roll_number", rollNumber)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<SemesterGpaDto>()
        }
    }

    private suspend fun syncGpaForSemester(sessionId: String, semester: Int) {
        val scopeKey = SyncCheckpointDefaults.scoped("session_id" to sessionId, "semester" to semester)
        syncGpaDelta(scopeKey) { since, offset ->
            postgrest.from(SupabaseTables.STUDENT_SEMESTER_GPA).select {
                filter {
                    eq("session_id", sessionId)
                    eq("semester", semester)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<SemesterGpaDto>()
        }
    }

    private suspend fun syncGpaDelta(scopeKey: String, fetchPage: suspend (since: String, offset: Long) -> List<SemesterGpaDto>) {
        val ownerKey = syncOwnerKey()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.STUDENT_SEMESTER_GPA, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = fetchPage(since, offset)
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity() }
            val (deleted, active) = entities.partition { it.isDeleted }
            gpaDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.STUDENT_SEMESTER_GPA, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
