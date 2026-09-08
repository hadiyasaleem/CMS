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
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import java.time.Instant
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DatesheetRepositoryLocalImpl @Inject constructor(
    private val postgrest: Postgrest,
    private val datesheetDao: DatesheetDao,
    private val checkpointStore: SyncCheckpointStore,
    private val sessionManager: SessionManager,
) : DatesheetRepository {

    private fun syncOwnerKey(): String = sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    override fun observeDatesheets(): Flow<List<Datesheet>> =
        datesheetDao.observeDatesheets().map { rows -> rows.map { DatesheetMapper.entityToDomain(it) } }

    override fun observeSlots(datesheetId: String): Flow<List<DatesheetSlot>> =
        datesheetDao.observeSlots(datesheetId).map { rows -> rows.map { DatesheetMapper.slotEntityToDomain(it) } }

    override fun observeAllSlots(): Flow<List<DatesheetSlot>> =
        datesheetDao.observeAllSlots().map { rows -> rows.map { DatesheetMapper.slotEntityToDomain(it) } }

    override suspend fun sync() {
        syncDatesheets()
    }

    override suspend fun syncAllSlots() {
        syncSlotsDelta()
    }

    override suspend fun createDatesheet(draft: DatesheetDraft, createdBy: String): String {
        val dto = DatesheetDto(
            sessionId = draft.sessionId,
            semester = draft.semester,
            defaultStartTime = draft.defaultStartTime,
            defaultEndTime = draft.defaultEndTime,
            defaultBuildingId = draft.defaultBuildingId,
            published = draft.published,
            instructions = draft.instructions,
            createdBy = createdBy,
        )
        val inserted = postgrest.from(SupabaseTables.DATESHEETS).insert(dto) { select() }.decodeList<DatesheetDto>().first()
        datesheetDao.upsertDatesheets(listOf(DatesheetMapper.dtoToEntity(inserted)))
        return inserted.id ?: ""
    }

    override suspend fun updateDatesheet(id: String, draft: DatesheetDraft) {
        postgrest.from(SupabaseTables.DATESHEETS).update({
            set("session_id", draft.sessionId)
            set("semester", draft.semester)
            set("default_start_time", draft.defaultStartTime)
            set("default_end_time", draft.defaultEndTime)
            set("default_building_id", draft.defaultBuildingId)
            set("instructions", draft.instructions)
            set("published", draft.published)
        }) {
            filter { eq("id", id) }
        }
        datesheetDao.getDatesheetById(id)?.let { cached ->
            datesheetDao.upsertDatesheets(
                listOf(
                    cached.copy(
                        sessionId = draft.sessionId,
                        semester = draft.semester,
                        defaultStartTime = draft.defaultStartTime,
                        defaultEndTime = draft.defaultEndTime,
                        defaultBuildingId = draft.defaultBuildingId,
                        instructions = draft.instructions,
                        published = draft.published,
                        updatedAt = System.currentTimeMillis(),
                    ),
                ),
            )
        }
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        datesheetDao.getDatesheetById(id)?.let { cached ->
            datesheetDao.upsertDatesheets(listOf(cached.copy(published = published, updatedAt = System.currentTimeMillis())))
        }
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

    override suspend fun prefillPapers(datesheetId: String, subjects: List<SemesterSubject>) {
        if (subjects.isEmpty()) return
        val dtos = subjects.map { subject ->
            DatesheetSlotDto(datesheetId = datesheetId, courseCode = subject.courseCode, subjectName = subject.name)
        }
        val inserted = postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(dtos) { select() }.decodeList<DatesheetSlotDto>()
        datesheetDao.upsertSlots(inserted.map { DatesheetMapper.slotDtoToEntity(it) })
    }

    override suspend fun addSlot(slot: DatesheetSlot) {
        val dto = DatesheetSlotDto(
            datesheetId = slot.datesheetId,
            courseCode = slot.courseCode,
            subjectName = slot.subjectName,
            examDate = slot.examDate,
            startTime = slot.startTime,
            endTime = slot.endTime,
            buildingId = slot.buildingId,
            building = slot.building,
            roomId = slot.roomId,
            roomNo = slot.roomNo,
            invigilatorEmail = slot.invigilatorEmail,
        )
        val inserted = postgrest.from(SupabaseTables.DATESHEET_SLOTS).insert(dto) { select() }.decodeList<DatesheetSlotDto>().first()
        datesheetDao.upsertSlots(listOf(DatesheetMapper.slotDtoToEntity(inserted)))
    }

    override suspend fun updateSlot(slot: DatesheetSlot) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({
            set("course_code", slot.courseCode)
            set("subject_name", slot.subjectName)
            set("exam_date", slot.examDate)
            set("start_time", slot.startTime)
            set("end_time", slot.endTime)
            set("building_id", slot.buildingId)
            set("building", slot.building)
            set("room_id", slot.roomId)
            set("room_no", slot.roomNo)
            set("invigilator_email", slot.invigilatorEmail)
        }) {
            filter { eq("id", slot.id) }
        }
        datesheetDao.getSlotById(slot.id)?.let { cached ->
            datesheetDao.upsertSlots(
                listOf(
                    cached.copy(
                        courseCode = slot.courseCode,
                        subjectName = slot.subjectName,
                        examDate = slot.examDate,
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        buildingId = slot.buildingId,
                        building = slot.building,
                        roomId = slot.roomId,
                        roomNo = slot.roomNo,
                        invigilatorEmail = slot.invigilatorEmail,
                        updatedAt = System.currentTimeMillis(),
                    ),
                ),
            )
        }
    }

    override suspend fun deleteSlot(id: String) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        datesheetDao.deleteSlotById(id)
    }

    override suspend fun getPapersOnDates(dates: Set<String>): List<DatesheetSlot> {
        if (dates.isEmpty()) return emptyList()
        return datesheetDao.getSlotsOnDates(dates.toList()).map { DatesheetMapper.slotEntityToDomain(it) }
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

    private suspend fun syncSlotsDelta() {
        val ownerKey = syncOwnerKey()
        val scopeKey = SyncCheckpointDefaults.globalScope()
        val checkpoint = checkpointStore.get(ownerKey, SupabaseTables.DATESHEET_SLOTS, scopeKey)
        val since = checkpoint?.lastUpdatedAt ?: SyncCheckpointDefaults.EPOCH
        var maxUpdatedAt = since

        var offset = 0L
        while (true) {
            val page = postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
                filter { gte("updated_at", since) }
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
