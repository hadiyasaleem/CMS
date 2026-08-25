package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.DatesheetEntity
import com.mbd.cmscommon.data.local.entity.DatesheetSlotEntity

@Dao
interface DatesheetDao {
    @Query("SELECT * FROM datesheets WHERE isDeleted = 0 ORDER BY title")
    suspend fun getDatesheets(): List<DatesheetEntity>

    @Query("SELECT * FROM datesheet_slots WHERE datesheetId = :datesheetId AND isDeleted = 0 ORDER BY examDate, startTime")
    suspend fun getSlots(datesheetId: String): List<DatesheetSlotEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDatesheets(items: List<DatesheetEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSlots(items: List<DatesheetSlotEntity>)

    @Query("DELETE FROM datesheets WHERE datesheetId = :id")
    suspend fun deleteDatesheetById(id: String)

    @Query("DELETE FROM datesheets WHERE datesheetId IN (:ids)")
    suspend fun deleteDatesheetsByIds(ids: List<String>)

    @Query("DELETE FROM datesheet_slots WHERE slotId = :id")
    suspend fun deleteSlotById(id: String)

    @Query("DELETE FROM datesheet_slots WHERE slotId IN (:ids)")
    suspend fun deleteSlotsByIds(ids: List<String>)

    @Query("DELETE FROM datesheet_slots WHERE datesheetId = :datesheetId")
    suspend fun deleteSlotsForDatesheet(datesheetId: String)

    suspend fun applyDatesheetDelta(upserts: List<DatesheetEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertDatesheets(upserts)
        if (deletedIds.isNotEmpty()) deleteDatesheetsByIds(deletedIds)
    }

    suspend fun applySlotDelta(upserts: List<DatesheetSlotEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertSlots(upserts)
        if (deletedIds.isNotEmpty()) deleteSlotsByIds(deletedIds)
    }
}
