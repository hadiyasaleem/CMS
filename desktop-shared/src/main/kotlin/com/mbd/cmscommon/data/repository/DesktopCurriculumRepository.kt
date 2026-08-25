package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopCurriculumMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.SemesterSubjectDto
import com.mbd.cmscommon.data.remote.dto.SemesterTermDto
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.repository.CurriculumRepository
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

/**
 * Desktop repos are always-online: no local persistence, `syncSession()` does a full re-fetch
 * into an in-memory [MutableStateFlow] cache that `observe*` just derives from. Subjects are
 * cached as a `Map<sessionId, List<SemesterSubject>>` — session-scoped, since every method in
 * the interface is keyed off a session. Semester terms are never cached (matching mobile, which
 * has no local DAO for them either) — [getSemesterTerm] always hits Postgrest directly.
 */
@Singleton
class DesktopCurriculumRepository @Inject constructor(
    private val postgrest: Postgrest,
) : CurriculumRepository {

    private val subjectsCache = MutableStateFlow<Map<String, List<SemesterSubject>>>(emptyMap())

    override fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>> =
        subjectsCache.asStateFlow().map { it[sessionId]?.filter { subject -> subject.semester == semester } ?: emptyList() }

    override fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>> =
        subjectsCache.asStateFlow().map { it[sessionId] ?: emptyList() }

    override suspend fun getSemesterTerm(sessionId: String, semester: Int): SemesterTerm? {
        val dto = postgrest.from(SupabaseTables.SEMESTER_TERMS).select {
            filter {
                eq("session_id", sessionId)
                eq("semester", semester)
            }
        }.decodeList<SemesterTermDto>().firstOrNull() ?: return null
        return DesktopCurriculumMapper.termDtoToDomain(dto, sessionId, semester)
    }

    override suspend fun saveSemesterSubjects(sessionId: String, semester: Int, subjects: List<SemesterSubject>) {
        val dtos = subjects.map { DesktopCurriculumMapper.subjectDomainToDto(it, sessionId, semester) }
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
        syncSession(sessionId)
    }

    override suspend fun saveSemesterTerm(sessionId: String, semester: Int, startDate: LocalDate?, endDate: LocalDate?) {
        val dto = SemesterTermDto(sessionId, semester, startDate?.toString(), endDate?.toString())
        postgrest.from(SupabaseTables.SEMESTER_TERMS).upsert(dto) { onConflict = "session_id,semester" }
    }

    override suspend fun syncSession(sessionId: String) {
        val rows = postgrest.from(SupabaseTables.SESSION_SUBJECTS).select {
            filter {
                eq("session_id", sessionId)
                eq("is_deleted", false)
            }
            order("semester", Order.ASCENDING)
            order("course_code", Order.ASCENDING)
        }.decodeList<SemesterSubjectDto>()
        val subjects = rows.map { DesktopCurriculumMapper.subjectDtoToDomain(it) }
        subjectsCache.value = subjectsCache.value + (sessionId to subjects)
    }
}
