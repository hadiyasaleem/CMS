package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.mapper.AcademicStructureMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SemesterSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterTermDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurriculumRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val subjectDao: SemesterSubjectDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : CurriculumRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private fun SemesterSubject.toDto(sessionId: String, semester: Int): SemesterSubjectDto = SemesterSubjectDto(
        sessionId = sessionId,
        semester = semester,
        courseCode = courseCode,
        name = name,
        creditHours = creditHours,
        subjectType = subjectType.name,
        isElective = isElective,
        outline = outline,
    )

    private fun subjectLocalId(dto: SemesterSubjectDto): String = "${dto.sessionId}_${dto.semester}_${dto.courseCode}"

    private fun SemesterSubjectDto.toEntity(): SemesterSubjectEntity = SemesterSubjectEntity(
        id = subjectLocalId(this),
        sessionId = sessionId ?: "",
        semester = semester,
        courseCode = courseCode ?: "",
        name = name ?: "",
        creditHours = creditHours,
        subjectType = subjectType ?: "THEORY",
        isElective = isElective,
        outline = outline,
        entityId = entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

    override fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>> =
        subjectDao.observeSemesterSubjects(sessionId, semester).map { rows -> rows.map { AcademicStructureMapper.subjectEntityToDomain(it) } }

    override fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>> =
        subjectDao.observeSessionSubjects(sessionId).map { rows -> rows.map { AcademicStructureMapper.subjectEntityToDomain(it) } }

    override suspend fun saveSemesterSubjects(sessionId: String, semester: Int, subjects: List<SemesterSubject>) {
        val dtos = subjects.map { it.toDto(sessionId, semester) }
        if (dtos.isNotEmpty()) {
            postgrest.from(SupabaseTables.SESSION_SUBJECTS).upsert(dtos) { onConflict = "session_id,semester,course_code" }
        }
        val keepCodes = subjects.map { it.courseCode }
        if (keepCodes.isEmpty()) {
            postgrest.from(SupabaseTables.SESSION_SUBJECTS).update({ set("is_deleted", true) }) {
                filter {
                    eq("session_id", sessionId)
                    eq("semester", semester)
                }
            }
        } else {
            postgrest.from(SupabaseTables.SESSION_SUBJECTS).update({ set("is_deleted", true) }) {
                filter {
                    eq("session_id", sessionId)
                    eq("semester", semester)
                    filterNot("course_code", FilterOperator.IN, "(${keepCodes.joinToString(",")})")
                }
            }
        }
        subjectDao.deleteForSemester(sessionId, semester)
        subjectDao.upsertAll(dtos.map { it.toEntity() })
    }

    override suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm? {
        val dto = postgrest.from(SupabaseTables.SEMESTER_TERMS).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
            }
        }.decodeList<SemesterTermDto>().firstOrNull() ?: return null

        return SemesterTerm(
            sessionId = sessionId,
            semester = semester,
            startDate = dto.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
            endDate = dto.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        )
    }

    override suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?) {
        val dto = SemesterTermDto(sessionId, semester, startDate?.toString(), endDate?.toString())
        postgrest.from(SupabaseTables.SEMESTER_TERMS).upsert(dto) { onConflict = "session_id,semester" }
    }

    override suspend fun syncSession(sessionId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("session" to sessionId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_SUBJECTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.SESSION_SUBJECTS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<SemesterSubjectDto>()
            if (page.isEmpty()) break

            val entities = page.map { it.toEntity() }
            val (deleted, active) = entities.partition { it.isDeleted }
            subjectDao.applyDelta(active, deleted.map { it.id })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.SESSION_SUBJECTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
