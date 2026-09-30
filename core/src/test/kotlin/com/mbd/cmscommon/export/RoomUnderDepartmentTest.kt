package com.mbd.cmscommon.export

import com.mbd.cmscommon.controller.MasterGrid
import com.mbd.cmscommon.controller.MasterGridRow
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftMode
import java.time.DayOfWeek
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RoomUnderDepartmentTest {

    private val session = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.MORNING, 3, maxStudents = 50)
    private val dept = Department("IT", "Information Technology", "IT", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = "")

    private fun period(day: DayOfWeek, start: String, end: String, room: String?) = SessionPeriod(
        "p-$day-$start", "IT_2022", Session.MORNING, day, start, end, "IT-301", "Subject", "t@x", "Teacher", roomNo = room,
    )

    @Test
    fun theUsualRoomIsWrittenOnceUnderTheDepartmentCode() {
        val periods = listOf(
            period(DayOfWeek.MONDAY, "08:00", "09:00", "R14"),
            period(DayOfWeek.TUESDAY, "08:00", "09:00", "R14"),
            period(DayOfWeek.WEDNESDAY, "08:00", "09:00", "R14"),
            period(DayOfWeek.THURSDAY, "08:00", "09:00", "Lab 2"),
        )
        val grid = MasterGrid(3, ProgramType.BS, Session.MORNING, listOf(MasterGridRow(session, dept, periods)))
        val block = masterGridLayout(grid).blocks.single()
        assertEquals(listOf("IT", "R14"), block.deptLines)
        val cellRooms = block.subRows.flatMap { it.cells.values }.map { it.location }
        assertEquals(1, cellRooms.count { it != null })
        assertEquals("Lab 2", cellRooms.single { it != null })
    }

    @Test
    fun noRoomsMeansNothingUnderTheCode() {
        val grid = MasterGrid(3, ProgramType.BS, Session.MORNING, listOf(MasterGridRow(session, dept, listOf(period(DayOfWeek.MONDAY, "08:00", "09:00", null)))))
        val block = masterGridLayout(grid).blocks.single()
        assertEquals(listOf("IT"), block.deptLines)
        assertNull(block.subRows.single().cells.values.single().location)
    }

    @Test
    fun theSessionTimetableStatesItsUsualRoomInTheTitle() {
        val periods = listOf(period(DayOfWeek.MONDAY, "08:00", "09:00", "R14"), period(DayOfWeek.TUESDAY, "08:00", "09:00", "R14"), period(DayOfWeek.WEDNESDAY, "08:00", "09:00", "Lab 2"))
        val doc = timetableExport(session, periods, Session.MORNING)
        assertEquals("Room: R14", doc.title.last())
        assertEquals(listOf("", "", "Lab 2"), doc.sections.single().rows.map { it[7] })
    }
}
