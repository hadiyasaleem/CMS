package com.mbd.cmscommon.export

import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReportExportsTest {

    private fun student(roll: String, name: String) = SessionStudent(id = roll, sessionId = "s1", deptId = "it", rollNumber = roll, name = name, shift = Session.MORNING)

    @Test
    fun registerHasEveryDayMarksSundayHolidaysAndTotals() {
        val month = YearMonth.of(2026, 9) // 30 days; Sundays 6, 13, 20, 27
        val marks = mapOf(
            "IT-21-09" to mapOf(
                LocalDate.of(2026, 9, 1) to DailyAttendanceMark("IT-21-09", LocalDate.of(2026, 9, 1), AttendanceStatus.PRESENT, isLate = true),
                LocalDate.of(2026, 9, 2) to DailyAttendanceMark("IT-21-09", LocalDate.of(2026, 9, 2), AttendanceStatus.ABSENT),
            ),
        )
        val doc = attendanceRegisterExport("GE-163", null, month, listOf(student("IT-21-09", "Amina")), marks)
        val section = doc.sections.single()
        assertEquals(2 + 30 + 5, section.header.size)
        assertEquals("06", section.header[2 + 5]) // Sep 6
        assertEquals(setOf(2 + 5, 2 + 12, 2 + 19, 2 + 26), section.blackColumns)
        val row = section.rows.single()
        assertEquals("P*", row[2])
        assertEquals("A", row[3])
        assertEquals("", row[2 + 5])
        assertEquals(listOf("1", "1", "0", "1", "50%"), row.takeLast(5))
    }

    @Test
    fun marksSheetDistinguishesAbsentFromNotEntered() {
        val assignment = ResolvedAssignment(sessionId = "s1", sessionLabel = "IT 2021", courseCode = "CS-101", subjectLabel = "Programming")
        val doc = marksSheetExport(
            assignment, ExamType.MIDTERM,
            listOf(student("IT-21-02", "B"), student("IT-21-01", "A"), student("IT-21-03", "C")),
            scores = mapOf("IT-21-01" to "20"),
            absentRolls = setOf("IT-21-02"),
        )
        val rows = doc.sections.single().rows
        assertEquals(listOf("IT-21-01", "A", "20", "25", "Entered"), rows[0])
        assertEquals(listOf("IT-21-02", "B", "", "25", "Absent"), rows[1])
        assertEquals("Not entered", rows[2][4])
    }
}
