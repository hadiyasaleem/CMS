package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.local.dao.DatesheetDao
import com.mbd.cmscommon.data.mapper.DatesheetMapper
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.data.sync.SyncCheckpoint
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.SyncCheckpointStore
import com.mbd.cmscommon.data.sync.maxRemoteUpdatedAt
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject

class DatesheetRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val datesheetDao: DatesheetDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : DatesheetRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override suspend fun getDatesheets(): List<Datesheet> {
        runCatching { syncDatesheets() }
        return datesheetDao.getDatesheets().map { DatesheetMapper.entityToDomain(it) }
    }

    override suspend fun getSlots(datesheetId: String): List<DatesheetSlot> {
        runCatching { syncSlots(datesheetId) }
        return datesheetDao.getSlots(datesheetId).map { DatesheetMapper.slotEntityToDomain(it) }
    }

    override suspend fun createDatesheet(title: String, examType: String, sessionId: String?, instructions: String, published: Boolean, createdBy: String): String {
        val dto = DatesheetDto(
            title = title.trim(),
            examType = examType,
            sessionId = sessionId,
            published = published,
            instructions = instructions,
            createdBy = createdBy,
        )
        val inserted = postgrest.from(SupabaseTables.DATESHEETS).insert(dto) { select() }.decodeList<DatesheetDto>().first()
        datesheetDao.upsertDatesheets(listOf(DatesheetMapper.dtoToEntity(inserted)))
        return inserted.id ?: ""
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
        syncDatesheets()
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        syncDatesheets()
    }

    override suspend fun deleteDatesheet(id: String) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("datesheet_id", id) }
        }
        datesheetDao.deleteDatesheetById(id)
        datesheetDao.deleteSlotsForDatesheet(id)
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
        val inserted = postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(dto) { select() }.decodeList<DatesheetSlotDto>().first()
        datesheetDao.upsertSlots(listOf(DatesheetMapper.slotDtoToEntity(inserted)))
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
        syncSlots(slot.datesheetId)
    }

    override suspend fun deleteSlot(id: String) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        datesheetDao.deleteSlotById(id)
    }

    private suspend fun syncDatesheets() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.DATESHEETS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.DATESHEETS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<DatesheetDto>()
            if (page.isEmpty()) break

            val entities = page.map { DatesheetMapper.dtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            datesheetDao.applyDatesheetDelta(active, deleted.map { it.datesheetId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.DATESHEETS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private suspend fun syncSlots(datesheetId: String) {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.scoped("datesheet" to datesheetId)
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.DATESHEET_SLOTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
                filter {
                    eq("datesheet_id", datesheetId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(offset, offset + PAGE_SIZE - 1)
            }.decodeList<DatesheetSlotDto>()
            if (page.isEmpty()) break

            val entities = page.map { DatesheetMapper.slotDtoToEntity(it) }
            val (deleted, active) = entities.partition { it.isDeleted }
            datesheetDao.applySlotDelta(active, deleted.map { it.slotId })
            maxUpdatedAt = page.maxRemoteUpdatedAt(maxUpdatedAt) { it.updatedAt }

            if (page.size < PAGE_SIZE) break
            offset += PAGE_SIZE
        }

        checkpointStore.upsert(SyncCheckpoint(ownerKey, SupabaseTables.DATESHEET_SLOTS, scopeKey, maxUpdatedAt, PgTime.format(Instant.now()) ?: since))
    }

    private companion object {
        const val PAGE_SIZE = 500L
    }
}
