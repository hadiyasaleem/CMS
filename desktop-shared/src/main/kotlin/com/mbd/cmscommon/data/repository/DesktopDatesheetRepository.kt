package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.data.mapper.DesktopDatesheetMapper
import com.mbd.cmscommon.data.remote.SupabaseTables
import com.mbd.cmscommon.data.remote.dto.DatesheetDto
import com.mbd.cmscommon.data.remote.dto.DatesheetSlotDto
import com.mbd.cmscommon.data.sync.SyncCheckpointDefaults
import com.mbd.cmscommon.data.sync.fetchIncrementalDelta
import com.mbd.cmscommon.data.sync.mergeIncrementalDelta
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.query.Order
import javax.inject.Inject
import javax.inject.Singleton

/** Durable cache-first datesheet repository. */
@Singleton
class DesktopDatesheetRepository @Inject constructor(
    private val postgrest: Postgrest,
    private val store: DesktopBootstrapSnapshotStore,
    private val sessionManager: SessionManager,
) : DatesheetRepository {
    override suspend fun getDatesheets(): List<Datesheet> =
        cachedDatesheets().filterNot { it.isDeleted }.map(DesktopDatesheetMapper::dtoToDomain)
            .sortedByDescending { it.createdAt }

    override suspend fun getSlots(datesheetId: String): List<DatesheetSlot> =
        cachedSlots().filter { it.datesheetId == datesheetId && !it.isDeleted }
            .map(DesktopDatesheetMapper::slotDtoToDomain).sortedBy { it.examDate }

    override suspend fun sync() {
        val delta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.DATESHEETS, SyncCheckpointDefaults.globalScope(),
            DatesheetDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.DATESHEETS).select {
                filter { gte("updated_at", since) }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeDatesheets(delta)
    }

    override suspend fun syncSlots(datesheetId: String) {
        val delta = fetchIncrementalDelta(
            store, ownerKey(), SupabaseTables.DATESHEET_SLOTS,
            SyncCheckpointDefaults.scoped("datesheet" to datesheetId),
            DatesheetSlotDto::updatedAt,
        ) { since, from, to ->
            postgrest.from(SupabaseTables.DATESHEET_SLOTS).select {
                filter {
                    eq("datesheet_id", datesheetId)
                    gte("updated_at", since)
                }
                order("updated_at", Order.ASCENDING)
                range(from, to)
            }.decodeList()
        }
        writeSlots(delta)
    }

    override suspend fun createDatesheet(
        title: String,
        examType: String,
        sessionId: String?,
        instructions: String,
        published: Boolean,
        createdBy: String,
    ): String {
        val inserted = postgrest.from(SupabaseTables.DATESHEETS).insert(
            DatesheetDto(
                title = title.trim(),
                examType = examType,
                sessionId = sessionId,
                published = published,
                instructions = instructions,
                createdBy = createdBy,
            ),
        ) { select() }.decodeList<DatesheetDto>().first()
        writeDatesheets(listOf(inserted))
        return inserted.id.orEmpty()
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
        }) { filter { eq("id", id) } }
        cachedDatesheets().firstOrNull { it.id == id }?.let { cached ->
            writeDatesheets(
                listOf(
                    cached.copy(
                        title = title.trim(),
                        examType = examType,
                        sessionId = sessionId,
                        instructions = instructions,
                        published = published,
                    ),
                ),
            )
        }
    }

    override suspend fun setPublished(id: String, published: Boolean) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("published", published) }) {
            filter { eq("id", id) }
        }
        cachedDatesheets().firstOrNull { it.id == id }?.let { cached ->
            writeDatesheets(listOf(cached.copy(published = published)))
        }
    }

    override suspend fun deleteDatesheet(id: String) {
        postgrest.from(SupabaseTables.DATESHEETS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("datesheet_id", id) }
        }
        store.writeRows(DATESHEETS_FILE, DatesheetDto.serializer(), cachedDatesheets().filterNot { it.id == id })
        store.writeRows(SLOTS_FILE, DatesheetSlotDto.serializer(), cachedSlots().filterNot { it.datesheetId == id })
    }

    override suspend fun addSlot(slot: DatesheetSlot) {
        val inserted = postgrest.from(SupabaseTables.DATESHEET_SLOTS)
            .insert(DesktopDatesheetMapper.slotDomainToDto(slot)) { select() }
            .decodeList<DatesheetSlotDto>()
        writeSlots(inserted)
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
        }) { filter { eq("id", slot.id) } }
        cachedSlots().firstOrNull { it.id == slot.id }?.let { cached ->
            writeSlots(
                listOf(
                    cached.copy(
                        examDate = slot.examDate,
                        startTime = slot.startTime,
                        endTime = slot.endTime,
                        durationMinutes = slot.durationMinutes,
                        courseCode = slot.courseCode,
                        subjectName = slot.subjectName,
                        roomNo = slot.roomNo,
                        building = slot.building,
                        invigilatorEmail = slot.invigilatorEmail,
                    ),
                ),
            )
        }
    }

    override suspend fun deleteSlot(id: String) {
        postgrest.from(SupabaseTables.DATESHEET_SLOTS).update({ set("is_deleted", true) }) {
            filter { eq("id", id) }
        }
        store.writeRows(SLOTS_FILE, DatesheetSlotDto.serializer(), cachedSlots().filterNot { it.id == id })
    }

    private fun cachedDatesheets() = store.readRows(DATESHEETS_FILE, DatesheetDto.serializer())
    private fun cachedSlots() = store.readRows(SLOTS_FILE, DatesheetSlotDto.serializer())

    private fun writeDatesheets(delta: List<DatesheetDto>) {
        store.writeRows(DATESHEETS_FILE, DatesheetDto.serializer(), mergeIncrementalDelta(
            cachedDatesheets(), delta, { it.id ?: "entity:${it.entityId}" }, DatesheetDto::isDeleted,
        ))
    }

    private fun writeSlots(delta: List<DatesheetSlotDto>) {
        store.writeRows(SLOTS_FILE, DatesheetSlotDto.serializer(), mergeIncrementalDelta(
            cachedSlots(), delta, { it.id ?: "entity:${it.entityId}" }, DatesheetSlotDto::isDeleted,
        ))
    }

    private fun ownerKey() =
        sessionManager.accountKey ?: SyncCheckpointDefaults.ownerKey("anonymous-local")

    private companion object {
        const val DATESHEETS_FILE = "datesheets.json"
        const val SLOTS_FILE = "datesheet-slots.json"
    }
}
