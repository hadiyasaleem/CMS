package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetDraft
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.SemesterSubject
import kotlinx.coroutines.flow.Flow

interface DatesheetRepository {
    fun observeDatesheets(): Flow<List<Datesheet>>
    fun observeSlots(datesheetId: String): Flow<List<DatesheetSlot>>
    fun observeAllSlots(): Flow<List<DatesheetSlot>>

    suspend fun sync() = Unit
    suspend fun syncAllSlots() = Unit

    suspend fun createDatesheet(draft: DatesheetDraft, createdBy: String): String
    suspend fun updateDatesheet(id: String, draft: DatesheetDraft)
    suspend fun deleteDatesheet(id: String)
    suspend fun setPublished(id: String, published: Boolean)

    /** Bulk-creates one unscheduled paper per given curriculum subject. */
    suspend fun prefillPapers(datesheetId: String, subjects: List<SemesterSubject>)
    suspend fun addSlot(slot: DatesheetSlot)
    suspend fun updateSlot(slot: DatesheetSlot)
    suspend fun deleteSlot(id: String)

    /** Every paper scheduled on any of [dates], across all datesheets -- for cross-datesheet clash warnings. */
    suspend fun getPapersOnDates(dates: Set<String>): List<DatesheetSlot>
}
