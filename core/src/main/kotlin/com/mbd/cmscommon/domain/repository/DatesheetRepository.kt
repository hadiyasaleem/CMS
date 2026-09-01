package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot

interface DatesheetRepository {
    suspend fun getDatesheets(): List<Datesheet>
    suspend fun sync() = Unit
    suspend fun createDatesheet(title: String, examType: String, sessionId: String?, instructions: String, published: Boolean, createdBy: String): String
    suspend fun updateDatesheet(id: String, title: String, examType: String, sessionId: String?, instructions: String, published: Boolean)
    suspend fun deleteDatesheet(id: String)
    suspend fun setPublished(id: String, published: Boolean)

    suspend fun getSlots(datesheetId: String): List<DatesheetSlot>
    suspend fun syncSlots(datesheetId: String) = Unit
    suspend fun addSlot(slot: DatesheetSlot)
    suspend fun updateSlot(slot: DatesheetSlot)
    suspend fun deleteSlot(id: String)
}
