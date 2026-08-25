package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.mapper.DesktopDatesheetMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * [DatesheetRepository] is a plain suspend-fun interface (no `observe*`/`sync()`), so every screen
 * re-fetches on demand. We still keep small in-memory caches — datesheets keyed globally, slots
 * keyed by datesheetId — mirroring the last fetch for callers that want a synchronous snapshot.
 */
@Singleton
class DesktopDatesheetRepository @Inject constructor(
    private val postgrest: Postgrest,
) : DatesheetRepository {

    private val datesheetsCache = MutableStateFlow<List<Datesheet>>(emptyList())
    private val slotsCache = MutableStateFlow<Map<String, List<DatesheetSlot>>>(emptyMap())

    override suspend fun getDatesheets(): List<Datesheet> {
        val rows = postgrest.from(SupabaseTables.DATESHEETS).select {
            filter { eq("is_deleted", false) }
        }.decodeList<DatesheetDto>()
        val datesheets = rows.map { DesktopDatesheetMapper.dtoToDomain(it) }
        datesheetsCache.value = datesheets
        return datesheets
    }

    override suspend fun createDatesheet(
        title: String,
        examType: String,
        sessionId: String?,
        instructions: String,
        published: Boolean,
        createdBy: String,
    ): String {
        val dto = DatesheetDto(
            title = title.trim(),
            examType = examType,
            sessionId = sessionId,
            published = published,
            instructions = instructions,
            createdBy = createdBy,
        )
        val inserted = postgrest.from(SupabaseTables.DATESHEETS).insert(dto) { select() }.decodeList<DatesheetDto>().first()
        getDatesheets()
        return inserted.id ?: ""
    }

    override suspend fun updateDatesheet(
        id: String,
        title: String,
        examType: String,
        sessionId: String?,
        instructions: String,
        published: Boolean,
    ) {
        postgrest.from(SupabaseTables.DATESHEETS).update({
            set("title", title.trim())
            set("exam_type", examType)
            set("session_id", sessionId)
            set("instructions", instructions)
            set("published", published)
        }) {
            filter { eq("id", id) }
        }
        getDatesheets()
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        getDatesheets()
    }

    override suspend fun deleteDatesheet(id: String) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("datesheet_id", id) }
        }
        getDatesheets()
        slotsCache.value = slotsCache.value - id
    }

    override suspend fun getSlots(datesheetId: String): List<DatesheetSlot> {
        val rows = postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
            filter {
                eq("datesheet_id", datesheetId)
                eq("is_deleted", false)
            }
            order("exam_date", Order.ASCENDING)
        }.decodeList<DatesheetSlotDto>()
        val slots = rows.map { DesktopDatesheetMapper.slotDtoToDomain(it) }
        slotsCache.value = slotsCache.value + (datesheetId to slots)
        return slots
    }

    override suspend fun addSlot(slot: DatesheetSlot) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(DesktopDatesheetMapper.slotDomainToDto(slot))
        getSlots(slot.datesheetId)
    }

    override suspend fun updateSlot(slot: DatesheetSlot) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({
            set("exam_date", slot.examDate)
            set("start_time", slot.startTime)
            set("end_time", slot.endTime)
            set("duration_minutes", slot.durationMinutes)
            set("course_code", slot.courseCode)
            set("subject_name", slot.subjectName)
            set("room_no", slot.roomNo)
            set("building", slot.building)
            set("invigilator_email", slot.invigilatorEmail)
        }) {
            filter { eq("id", slot.id) }
        }
        getSlots(slot.datesheetId)
    }

    override suspend fun deleteSlot(id: String) {
        val datesheetId = slotsCache.value.entries.find { (_, slots) -> slots.any { it.id == id } }?.key
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        if (datesheetId != null) getSlots(datesheetId)
    }
}
