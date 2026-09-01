package com.mbd.cmsdesktop.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopCurriculumMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SemesterSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterTermDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/** Durable cache-first curriculum and semester-term repository. */
@Singleton
class DesktopCurriculumRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : CurriculumRepository {

    private val subjects = MutableStateFlow(subjectRows().filterNot { it.isDeleted }.map(DesktopCurriculumMapper::subjectDtoToDomain))

    override fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>> =
        subjects.asStateFlow().map { list -> list.filter { it.sessionId == sessionId && it.semester == semester } }

    override fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>> =
        subjects.asStateFlow().map { list -> list.filter { it.sessionId == sessionId } }

    override suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm? =
        termRows().firstOrNull { it.sessionId == sessionId && it.semester == semester && !it.isDeleted }
            ?.let { DesktopCurriculumMapper.termDtoToDomain(it, sessionId, semester) }

    override suspend fun saveSemesterSubject(subject: SemesterSubject) {
        val dto = DesktopCurriculumMapper.subjectDomainToDto(subject, subject.sessionId, subject.semester)
        postgrest.from(SupabaseTables.SESSION_SUBJECTS).upsert(dto) { onConflict = "session_id,semester,course_code" }
        writeSubjects(subjectRows().filterNot {
            it.sessionId == subject.sessionId && it.semester == subject.semester && it.courseCode == subject.courseCode
        } + dto)
    }

    override suspend fun deleteSemesterSubject(sessionId: String, semester: Int, courseCode: String) {
        postgrest.from(SupabaseTables.SESSION_SUBJECTS).update({ set("is_deleted", true) }) {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
                eq("course_code", courseCode)
            }
        }
        writeSubjects(subjectRows().filterNot {
            it.sessionId == sessionId && it.semester == semester && it.courseCode == courseCode
        })
    }

    override suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?) {
        val dto = SemesterTermDto(sessionId, semester, startDate?.toString(), endDate?.toString())
        postgrest.from(SupabaseTables.SEMESTER_TERMS).upsert(dto) { onConflict = "session_id,semester" }
        store.writeRows(
            TERMS_CACHE_FILE,
            SemesterTermDto.serializer(),
            termRows().filterNot { it.sessionId == sessionId && it.semester == semester } + dto,
        )
    }

    override suspend fun syncSession(sessionId: String) {
        val scope = SyncCheckpointDefaults.scoped("session" to sessionId)
        val subjectDelta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.SESSION_SUBJECTS,
            scope,
            SemesterSubjectDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SESSION_SUBJECTS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeSubjects(mergeIncrementalDelta(
            subjectRows(),
            subjectDelta,
            { "${it.sessionId}|${it.semester}|${it.courseCode}" },
            SemesterSubjectDto::isDeleted,
        ))

        val termDelta = fetchIncrementalDelta(
            store,
            ownerKey(),
            SupabaseTables.SEMESTER_TERMS,
            scope,
            SemesterTermDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.SEMESTER_TERMS).select {
                filter {
                    eq("session_id", sessionId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        store.writeRows(
            TERMS_CACHE_FILE,
            SemesterTermDto.serializer(),
            mergeIncrementalDelta(
                termRows(),
                termDelta,
                { "${it.sessionId}|${it.semester}" },
                SemesterTermDto::isDeleted,
            ),
        )
    }

    private fun subjectRows() = store.readRows(SUBJECTS_CACHE_FILE, SemesterSubjectDto.serializer())

    private fun termRows() = store.readRows(TERMS_CACHE_FILE, SemesterTermDto.serializer())

    private fun writeSubjects(rows: List<SemesterSubjectDto>) {
        store.writeRows(SUBJECTS_CACHE_FILE, SemesterSubjectDto.serializer(), rows)
        subjects.value = rows.filterNot { it.isDeleted }.map(DesktopCurriculumMapper::subjectDtoToDomain)
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val SUBJECTS_CACHE_FILE = "session-subjects.json"
        const val TERMS_CACHE_FILE = "semester-terms.json"
    }
}
