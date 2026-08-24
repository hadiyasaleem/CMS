package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject

private fun DatesheetDto.toDomain(): Datesheet = Datesheet(
    id = id ?: "",
    title = title ?: "",
    examType = examType,
    sessionId = sessionId,
    published = published,
    instructions = instructions,
    entityId = entityId ?: 0L,
    createdAt = PgTime.parseOrEpoch(createdAt),
    createdBy = createdBy,
    updatedAt = PgTime.parseOrEpoch(updatedAt),
    updatedBy = updatedBy,
)

private fun DatesheetSlotDto.toDomain(): DatesheetSlot = DatesheetSlot(
    id = id ?: "",
    datesheetId = datesheetId ?: "",
    examDate = examDate ?: "",
    startTime = startTime,
    endTime = endTime,
    durationMinutes = durationMinutes,
    courseCode = courseCode,
    subjectName = subjectName,
    roomNo = roomNo,
    building = building,
    invigilatorEmail = invigilatorEmail,
    entityId = entityId ?: 0L,
    createdAt = PgTime.parseOrEpoch(createdAt),
    createdBy = createdBy,
    updatedAt = PgTime.parseOrEpoch(updatedAt),
    updatedBy = updatedBy,
)

class DatesheetRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : DatesheetRepository {

    override suspend fun getDatesheets(): List<Datesheet> =
        postgrest.from(SupabaseTables.DATESHEETS).select {
            filter { eq("is_deleted", false) }
        }.decodeList<DatesheetDto>().map { it.toDomain() }

    override suspend fun getSlots(datesheetId: String): List<DatesheetSlot> =
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
            filter {
                eq("datesheet_id", datesheetId)
                eq("is_deleted", false)
            }
            order("exam_date", Order.ASCENDING)
        }.decodeList<DatesheetSlotDto>().map { it.toDomain() }

    override suspend fun createDatesheet(title: String, examType: String, sessionId: String?, instructions: String, published: Boolean, createdBy: String): String {
        val dto = DatesheetDto(
            title = title.trim(),
            examType = examType,
            sessionId = sessionId,
            published = published,
            instructions = instructions,
            createdBy = createdBy,
        )
        return postgrest.from(SupabaseTables.DATESHEETS).insert(dto) { select() }.decodeList<DatesheetDto>().first().id ?: ""
    }

    override suspend fun updateDatesheet(id: String, title: String, examType: String, sessionId: String?, instructions: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({
            set("title", title.trim())
            set("exam_type", examType)
            set("session_id", sessionId)
            set("instructions", instructions)
            set("published", published)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun deleteDatesheet(id: String) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("datesheet_id", id) }
        }
    }

    override suspend fun addSlot(slot: DatesheetSlot) {
        val dto = DatesheetSlotDto(
            datesheetId = slot.datesheetId,
            examDate = slot.examDate,
            startTime = slot.startTime,
            endTime = slot.endTime,
            durationMinutes = slot.durationMinutes,
            courseCode = slot.courseCode,
            subjectName = slot.subjectName,
            roomNo = slot.roomNo,
            building = slot.building,
            invigilatorEmail = slot.invigilatorEmail,
        )
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(dto)
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
    }

    override suspend fun deleteSlot(id: String) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
    }
}
