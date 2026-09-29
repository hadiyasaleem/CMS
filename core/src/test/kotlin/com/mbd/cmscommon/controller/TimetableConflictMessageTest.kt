package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftMode
import java.time.DayOfWeek
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TimetableConflictMessageTest {

    private val eng = Department("ENG", "English", "ENG", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = "")
    private val isl = Department("ISL", "Islamic Studies", "ISL", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = "")
    private val engSession = AcademicSession("ENG_2023", "ENG", 2023, 2027, ShiftMode.MORNING, 5, maxStudents = 100)
    private val islSession = AcademicSession("ISL_2023", "ISL", 2023, 2027, ShiftMode.MORNING, 3, maxStudents = 100)
    private val departments = listOf(eng, isl)
    private val sessions = listOf(engSession, islSession)

    private fun period(
        sessionId: String,
        course: String,
        teacherId: String,
        teacherName: String = "Teacher $teacherId",
        day: DayOfWeek = DayOfWeek.WEDNESDAY,
        start: String = "13:40",
        end: String = "15:05",
        roomNo: String? = "R#14",
    ) = SessionPeriod(
        SessionPeriod.buildId(sessionId, Session.MORNING, day, start),
        sessionId, Session.MORNING, day, start, end, course, "Subject $course", teacherId, teacherName,
        roomNo = roomNo,
    )

    @Test
    fun namesTheOtherClassWhenATeacherIsDoubleBooked() {
        val existing = period("ENG_2023", "EL-309", "majidbashir@ggcmbdin.edu.pk", "Majid Bashir")
        val candidate = period("ISL_2023", "BS-301", "majidbashir@ggcmbdin.edu.pk", "Majid Bashir", roomNo = "R#20")

        val message = describeTimetableConflict(candidate, listOf(existing), sessions, departments)

        assertEquals(
            "Teacher Majid Bashir already has a lecture with ENG Semester 5 Morning — Subject EL-309 (EL-309) on Wednesday at 13:40-15:05.",
            message,
        )
    }

    @Test
    fun namesTheOtherClassWhenARoomIsDoubleBooked() {
        val existing = period("ENG_2023", "EL-309", "teacherA@x", roomNo = "R#14")
        val candidate = period("ISL_2023", "BS-301", "teacherB@x", roomNo = "R#14")

        val message = describeTimetableConflict(candidate, listOf(existing), sessions, departments)

        assertEquals(
            "Room R#14 already has a lecture with ENG Semester 5 Morning — Subject EL-309 (EL-309) on Wednesday at 13:40-15:05.",
            message,
        )
    }

    @Test
    fun noConflictWhenNeitherTeacherNorRoomOverlap() {
        val existing = period("ENG_2023", "EL-309", "teacherA@x", roomNo = "R#14")
        val candidate = period("ISL_2023", "BS-301", "teacherB@x", roomNo = "R#20")

        assertNull(describeTimetableConflict(candidate, listOf(existing), sessions, departments))
    }

    @Test
    fun editingAPeriodInPlaceDoesNotConflictWithItsOwnPriorSelf() {
        val existing = period("ENG_2023", "EL-309", "majidbashir@x")
        val candidate = existing.copy(subjectName = "Renamed", endTime = "15:30")

        assertNull(describeTimetableConflict(candidate, listOf(existing), sessions, departments, excludedId = existing.id))
    }

    @Test
    fun breakPeriodsAreNeverFlaggedAsConflicts() {
        val existing = period("ENG_2023", "EL-309", "teacherA@x")
        val candidate = period("ISL_2023", "BS-301", "teacherA@x").copy(periodType = PeriodType.BREAK)

        assertNull(describeTimetableConflict(candidate, listOf(existing), sessions, departments))
    }
}
