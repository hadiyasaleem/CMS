package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.util.requireAffected
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.PoolSubjectDao
import com.mbd.cmscommon.data.local.dao.SemesterSubjectDao
import com.mbd.cmscommon.data.local.dao.SemesterTermDao
import com.mbd.cmscommon.data.local.entity.PoolSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterTermEntity
import com.mbd.cmscommon.data.mapper.AcademicStructureMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.data.remote.dto.MarkRowDto
import com.mbd.cmscommon.data.remote.dto.PoolSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterTermDto
import com.mbd.cmscommon.data.remote.dto.TimetablePeriodDto
import com.mbd.cmscommon.util.CmsException
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.PoolSubject
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** The columns a session_subjects read needs to flatten a link row back into a full [SemesterSubject]: the
 * link's own fields plus its course's current definition, embedded from subject_pool via the FK. */
private const val LINK_WITH_POOL = "*, subject_pool(*)"

class CurriculumRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val subjectDao: SemesterSubjectDao,
    private val poolDao: PoolSubjectDao,
    private val termDao: SemesterTermDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : CurriculumRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private fun PoolSubject.toDto(): PoolSubjectDto = PoolSubjectDto(
        courseCode = courseCode,
        name = name,
        creditHours = creditHours,
        subjectType = subjectType.name,
        courseType = courseType.name,
        outline = outline,
    )

    private fun subjectLocalId(sessionId: String, semester: Int, courseCode: String): String = "${sessionId}_${semester}_$courseCode"

    private fun PoolSubjectDto.toEntity(): PoolSubjectEntity = PoolSubjectEntity(
        courseCode = courseCode ?: "",
        name = name ?: "",
        creditHours = creditHours,
        subjectType = subjectType ?: "THEORY",
        courseType = courseType ?: "MAJOR",
        outline = outline,
        createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
        createdBy = createdBy,
        updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
        updatedBy = updatedBy,
        isDeleted = isDeleted,
        deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
        deletedBy = deletedBy,
    )

    /** Flattens a link row (with its embedded pool subject) into the local cache's denormalized shape. Falls
     * back to whatever is already cached locally for that course when the embed is missing (e.g. a link
     * written by an older client before its own pool row had synced down yet). */
    private suspend fun SemesterSubjectDto.toEntity(): SemesterSubjectEntity {
        val code = courseCode ?: ""
        val pool = subjectPool ?: poolDao.getByCode(code)?.let {
            PoolSubjectDto(it.courseCode, it.name, it.creditHours, it.subjectType, it.courseType, it.outline)
        }
        return SemesterSubjectEntity(
            id = subjectLocalId(sessionId ?: "", semester, code),
            sessionId = sessionId ?: "",
            semester = semester,
            courseCode = code,
            name = pool?.name ?: "",
            creditHours = pool?.creditHours ?: 3,
            subjectType = pool?.subjectType ?: "THEORY",
            courseType = pool?.courseType ?: "MAJOR",
            isElective = isElective,
            outline = pool?.outline,
            createdAt = PgTime.parseOrEpoch(createdAt).toEpochMilli(),
            createdBy = createdBy,
            updatedAt = PgTime.parseOrEpoch(updatedAt).toEpochMilli(),
            updatedBy = updatedBy,
            isDeleted = isDeleted,
            deletedAt = PgTime.parse(deletedAt)?.toEpochMilli(),
            deletedBy = deletedBy,
        )
    }

    private fun SemesterTermDto.toEntity(): SemesterTermEntity = SemesterTermEntity(
        sessionId = sessionId ?: "",
        semester = semester,
        startDate = startDate,
        endDate = endDate,
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

    override fun observePoolSubjects(): Flow<List<PoolSubject>> =
        poolDao.observeAll().map { rows -> rows.map { AcademicStructureMapper.poolEntityToDomain(it) } }

    override suspend fun saveSemesterSubject(subject: SemesterSubject) {
        val poolDto = PoolSubject(
            courseCode = subject.courseCode,
            name = subject.name,
            creditHours = subject.creditHours,
            subjectType = subject.subjectType,
            courseType = subject.courseType,
            outline = subject.outline,
        ).toDto()
        val savedPool = postgrest.from(SupabaseTables.SUBJECT_POOL).upsert(poolDto) { onConflict = "course_code"; select() }
            .decodeList<PoolSubjectDto>().first()
        poolDao.upsertAll(listOf(savedPool.toEntity()))

        val linkDto = SemesterSubjectDto(sessionId = subject.sessionId, semester = subject.semester, courseCode = subject.courseCode, isElective = subject.isElective)
        postgrest.from(SupabaseTables.SESSION_SUBJECTS).upsert(linkDto) { onConflict = "session_id,semester,course_code" }
        subjectDao.upsertAll(listOf(linkDto.copy(subjectPool = savedPool).toEntity()))
    }

    override suspend fun linkSemesterSubject(sessionId: String, semester: Int, courseCode: String, isElective: Boolean) {
        val linkDto = SemesterSubjectDto(sessionId = sessionId, semester = semester, courseCode = courseCode, isElective = isElective)
        postgrest.from(SupabaseTables.SESSION_SUBJECTS).upsert(linkDto) { onConflict = "session_id,semester,course_code" }
        subjectDao.upsertAll(listOf(linkDto.toEntity()))
    }

    override suspend fun copySemesterSubjects(fromSessionId: String, fromSemester: Int, toSessionId: String, toSemester: Int) {
        val existing = subjectDao.observeSemesterSubjects(toSessionId, toSemester).first().map { it.courseCode }.toSet()
        val source = subjectDao.observeSemesterSubjects(fromSessionId, fromSemester).first()
        source.filter { it.courseCode !in existing }.forEach { row ->
            linkSemesterSubject(toSessionId, toSemester, row.courseCode, row.isElective)
        }
    }

    override suspend fun deleteSemesterSubject(sessionId: String, semester: Int, courseCode: String) {
        val blocker = firstDependencyBlocking(sessionId, semester, courseCode)
        if (blocker != null) {
            throw CmsException.Conflict(
                "Can't remove $courseCode: it already has $blocker on record. Remove those first, or keep the subject.",
            )
        }
        postgrest.from(SupabaseTables.SESSION_SUBJECTS).update({ set("is_deleted", true) }) {
            select()
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
                eq("course_code", courseCode)
            }
        }.requireAffected(onNone = { subjectDao.deleteByCourseCode(sessionId, semester, courseCode) })
        subjectDao.deleteByCourseCode(sessionId, semester, courseCode)
    }

    /** Names the first kind of record still referencing [courseCode], or null if it's safe to unlink. Only this
     * session+semester's own records block it -- the pool definition and any other semester's link are unaffected
     * either way. */
    private suspend fun firstDependencyBlocking(sessionId: String, semester: Int, courseCode: String): String? {
        val hasAttendance = postgrest.from(SupabaseTables.SESSION_ATTENDANCE).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
                eq("course_code", courseCode)
            }
            range(0, 0)
        }.decodeList<AttendanceRowDto>().isNotEmpty()
        if (hasAttendance) return "attendance records"

        val hasMarks = postgrest.from(SupabaseTables.SESSION_MARKS).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
                eq("course_code", courseCode)
            }
            range(0, 0)
        }.decodeList<MarkRowDto>().isNotEmpty()
        if (hasMarks) return "marks"

        val hasPeriods = postgrest.from(SupabaseTables.TIMETABLE_PERIODS).select {
            filter {
                eq("primary_session_id", sessionId)
                eq("course_code", courseCode)
            }
            range(0, 0)
        }.decodeList<TimetablePeriodDto>().isNotEmpty()
        if (hasPeriods) return "timetable periods"

        return null
    }

    override suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm? =
        termDao.getForSemester(sessionId, semester)?.let { AcademicStructureMapper.termEntityToDomain(it) }

    override suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?) {
        val dto = SemesterTermDto(
            sessionId = sessionId,
            semester = semester,
            startDate = startDate?.toString(),
            endDate = endDate?.toString(),
        )
        postgrest.from(SupabaseTables.SEMESTER_TERMS).upsert(dto) { onConflict = "session_id,semester" }
        termDao.upsertAll(listOf(dto.toEntity()))
    }

    /** The pool has no session scope, so one global checkpointed sync serves every caller (a per-session sync
     * still benefits from it, since a session's subjects are rarely the only ones whose pool entries changed). */
    private suspend fun syncPool() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        fetchIncrementalDelta(
            checkpointStore,
            ownerKey,
            SupabaseTables.SUBJECT_POOL,
            scopeKey,
            PoolSubjectDto::updatedAt,
            applyDelta = { delta ->
                val entities = delta.map { it.toEntity() }
                val (deleted, active) = entities.partition { it.isDeleted }
                poolDao.applyDelta(active, deleted.map { it.courseCode })
            },
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SUBJECT_POOL).select {
                filter { gt("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun syncSession(sessionId: String) {
        syncPool()
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("session" to sessionId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_SUBJECTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.SESSION_SUBJECTS).select(Columns.raw(LINK_WITH_POOL)) {
                filter {
                    eq("session_id", sessionId)
                    gt("updated_at", since)
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

        fetchIncrementalDelta(
            checkpointStore,
            ownerKey,
            SupabaseTables.SEMESTER_TERMS,
            scopeKey,
            SemesterTermDto::updatedAt,
            applyDelta = { termDelta ->
                val entities = termDelta.map { it.toEntity() }
                val (deleted, active) = entities.partition { it.isDeleted }
                termDao.applyDelta(active, deleted.map { it.sessionId to it.semester })
            },
        ) { termSince, from, to ->
            postgrest.from(SupabaseTables.SEMESTER_TERMS).select {
                filter {
                    eq("session_id", sessionId)
                    gt("updated_at", termSince)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    override suspend fun syncAll() {
        syncPool()
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.SESSION_SUBJECTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.SESSION_SUBJECTS).select(Columns.raw(LINK_WITH_POOL)) {
                filter { gt("updated_at", since) }
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

        fetchIncrementalDelta(
            checkpointStore,
            ownerKey,
            SupabaseTables.SEMESTER_TERMS,
            scopeKey,
            SemesterTermDto::updatedAt,
            applyDelta = { termDelta ->
                val entities = termDelta.map { it.toEntity() }
                val (deleted, active) = entities.partition { it.isDeleted }
                termDao.applyDelta(active, deleted.map { it.sessionId to it.semester })
            },
        ) { termSince, from, to ->
            postgrest.from(SupabaseTables.SEMESTER_TERMS).select {
                filter { gt("updated_at", termSince) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
