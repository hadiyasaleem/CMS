package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.StudentProfile
import org.junit.Assert.assertEquals
import org.junit.Test

class StudentDirectoryTest {

    private val it2021 = AcademicSession("it_2021", "it", 2021, 2025, ShiftMode.BOTH, 7, maxStudents = 100)
    private val cs2023 = AcademicSession("cs_2023", "cs", 2023, 2027, ShiftMode.EVENING, 3)

    private fun row(
        roll: String,
        name: String,
        session: AcademicSession,
        cgpa: Double? = null,
        linked: String = "",
        status: String = "ACTIVE",
        shift: Session = session.shifts.first(),
    ) =
        StudentDirectoryRow(
            StudentProfile(sessionId = session.sessionId, rollNumber = roll, name = name, shift = shift, cgpa = cgpa, linkedEmail = linked, enrollmentStatus = status),
            session,
            session.deptId.uppercase(),
        )

    private val all = listOf(
        row("IT-21-02", "Zara", it2021, cgpa = 3.1, linked = "z@x.com"),
        row("IT-21-01", "Ali", it2021, cgpa = 3.8),
        row("IT-21-51", "Dua", it2021, shift = Session.EVENING),
        row("CS-23-01", "Bilal", cs2023, cgpa = null, status = "LEFT"),
    )

    @Test
    fun filtersCombineAndSortApplies() {
        val page = studentDirectoryPage(all, StudentDirectoryQuery(deptId = "it", sort = StudentDirectorySort.CGPA))
        assertEquals(listOf("IT-21-01", "IT-21-02", "IT-21-51"), page.rows.map { it.profile.rollNumber })
        assertEquals(listOf("IT-21-02"), studentDirectoryPage(all, StudentDirectoryQuery(account = StudentAccountFilter.LINKED)).rows.map { it.profile.rollNumber })
        assertEquals(listOf("CS-23-01"), studentDirectoryPage(all, StudentDirectoryQuery(enrollmentStatus = "left")).rows.map { it.profile.rollNumber })
        // The shift filter uses each student's own shift, so a BOTH session's Evening students match too.
        assertEquals(listOf("CS-23-01", "IT-21-51"), studentDirectoryPage(all, StudentDirectoryQuery(shift = Session.EVENING)).rows.map { it.profile.rollNumber })
        assertEquals(listOf("IT-21-51"), studentDirectoryPage(all, StudentDirectoryQuery(deptId = "it", shift = Session.EVENING)).rows.map { it.profile.rollNumber })
        assertEquals(listOf("Ali"), studentDirectoryPage(all, StudentDirectoryQuery(search = "ali")).rows.map { it.profile.name })
    }

    @Test
    fun pagesAreClampedAndIndexed() {
        val many = (1..60).map { row("IT-21-%02d".format(it), "S$it", it2021) }
        val last = studentDirectoryPage(many, StudentDirectoryQuery(page = 99), pageSize = 25)
        assertEquals(3, last.pageCount)
        assertEquals(2, last.page)
        assertEquals(10, last.rows.size)
        assertEquals(51, last.firstIndex)
        assertEquals(60, last.lastIndex)
        assertEquals(60, last.matches.size)
        val empty = studentDirectoryPage(emptyList(), StudentDirectoryQuery())
        assertEquals(1, empty.pageCount)
        assertEquals(0, empty.firstIndex)
    }
}
