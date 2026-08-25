package com.mbd.cmsdesktop.data.repository

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
 * No local persistence. [subjects] is kept as one flat list (not scoped per session) — every
 * [syncSession] call filterNot-replaces just the rows for its own session, mirroring mobile's
 * Room table. [getSemesterTerm] always hits Postgrest directly; semester terms are never cached.
 */
@Singleton
class DesktopCurriculumRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : CurriculumRepository {

    private val subjects = MutableStateFlow<List<SemesterSubject>>(emptyList())

    override fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubject>> =
        subjects.asStateFlow().map { list -> list.filter { it.sessionId == sessionId && it.semester == semester } }

    override fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubject>> =
        subjects.asStateFlow().map { list -> list.filter { it.sessionId == sessionId } }

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
        val mapped = rows.map { DesktopCurriculumMapper.subjectDtoToDomain(it) }
        subjects.value = subjects.value.filterNot { it.sessionId == sessionId } + mapped
    }
}
