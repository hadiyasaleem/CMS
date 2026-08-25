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

class DatesheetRepositoryImpl @Inject constructor(
    private val postgrest: Postgrest,
) : DatesheetRepository {

    override suspend fun getDatesheets(): List<Datesheet> {
        val rows = postgrest.from(SupabaseTables.DATESHEETS).select {
            order("created_at", Order.DESCENDING)
        }.decodeList<DatesheetDto>()
        return rows.map { it.toDomain() }
    }

    override suspend fun getSlots(datesheetId: String): List<DatesheetSlot> {
        val rows = postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
            filter { eq("datesheet_id", datesheetId) }
            order("exam_date", Order.ASCENDING)
        }.decodeList<DatesheetSlotDto>()
        return rows.map { it.toDomain() }
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
            examType = examType.trim().takeIf { it.isNotBlank() },
            sessionId = sessionId?.trim()?.takeIf { it.isNotBlank() },
            published = published,
            instructions = instructions.trim().takeIf { it.isNotBlank() },
            createdBy = createdBy,
        )
        val inserted = postgrest.from(SupabaseTables.DATESHEETS).insert(dto) {
            select()
        }.decodeList<DatesheetDto>()
        return inserted.first().id.orEmpty()
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
            set("exam_type", examType.trim().takeIf { it.isNotBlank() })
            set("session_id", sessionId?.trim()?.takeIf { it.isNotBlank() })
            set("instructions", instructions.trim().takeIf { it.isNotBlank() })
            set("published", published)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({
            set("published", published)
        }) {
            filter { eq("id", id) }
        }
    }

    override suspend fun deleteDatesheet(id: String) {
        postgrest.from(SupabaseTables.DATESHEETS).delete {
            filter { eq("id", id) }
        }
    }

    override suspend fun addSlot(slot: DatesheetSlot) {
        val dto = DatesheetSlotDto(
            datesheetId = slot.datesheetId,
            examDate = slot.examDate,
            startTime = slot.startTime?.trim()?.takeIf { it.isNotBlank() },
            endTime = slot.endTime?.trim()?.takeIf { it.isNotBlank() },
            durationMinutes = slot.durationMinutes,
            courseCode = slot.courseCode?.trim()?.takeIf { it.isNotBlank() },
            subjectName = slot.subjectName?.trim()?.takeIf { it.isNotBlank() },
            roomNo = slot.roomNo?.trim()?.takeIf { it.isNotBlank() },
            building = slot.building?.trim()?.takeIf { it.isNotBlank() },
            invigilatorEmail = slot.invigilatorEmail?.trim()?.takeIf { it.isNotBlank() },
        )
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(dto)
    }

    override suspend fun updateSlot(slot: DatesheetSlot) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({
            set("exam_date", slot.examDate)
            set("start_time", slot.startTime?.trim()?.takeIf { it.isNotBlank() })
            set("end_time", slot.endTime?.trim()?.takeIf { it.isNotBlank() })
            set("duration_minutes", slot.durationMinutes)
            set("course_code", slot.courseCode?.trim()?.takeIf { it.isNotBlank() })
            set("subject_name", slot.subjectName?.trim()?.takeIf { it.isNotBlank() })
            set("room_no", slot.roomNo?.trim()?.takeIf { it.isNotBlank() })
            set("building", slot.building?.trim()?.takeIf { it.isNotBlank() })
            set("invigilator_email", slot.invigilatorEmail?.trim()?.takeIf { it.isNotBlank() })
        }) {
            filter { eq("id", slot.id) }
        }
    }

    override suspend fun deleteSlot(id: String) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).delete {
            filter { eq("id", id) }
        }
    }

    private fun DatesheetDto.toDomain() = Datesheet(
        id = id.orEmpty(),
        title = title.orEmpty(),
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

    private fun DatesheetSlotDto.toDomain() = DatesheetSlot(
        id = id.orEmpty(),
        datesheetId = datesheetId.orEmpty(),
        examDate = examDate.orEmpty(),
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
}
