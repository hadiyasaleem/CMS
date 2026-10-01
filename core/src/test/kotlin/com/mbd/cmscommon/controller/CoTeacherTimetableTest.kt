package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.resolveAssignments
import java.time.DayOfWeek
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoTeacherTimetableTest {
    private fun period(
        id: String,
        teacher: String,
        co: List<String> = emptyList(),
        day: DayOfWeek = DayOfWeek.MONDAY,
        start: String = "09:00",
        end: String = "10:00",
        course: String = "IT-499",
        sessionId: String = "it_2022",
    ) = SessionPeriod(
        id = id, sessionId = sessionId, shift = Session.MORNING, day = day, startTime = start, endTime = end,
        courseCode = course, subjectName = "Project", teacherId = teacher, teacherName = teacher.substringBefore('@').replaceFirstChar { it.uppercase() },
        coTeacherIds = co, coTeacherNames = co.map { it.substringBefore('@').replaceFirstChar { c -> c.uppercase() } },
    )

    @Test
    fun everyTeacherOnAPeriodTeachesIt() {
        val p = period("p1", "ali@x.pk", listOf("sana@x.pk", "omar@x.pk"))
        assertEquals(listOf("ali@x.pk", "sana@x.pk", "omar@x.pk"), p.teacherIds)
        assertEquals("Ali & Sana & Omar", p.teacherLabel)
        assertTrue(p.isTaughtBy("sana@x.pk"))
        assertTrue(p.isTaughtBy("SANA@x.pk"))
        assertFalse(p.isTaughtBy("zoya@x.pk"))
        assertFalse(p.isTaughtBy(""))
    }

    @Test
    fun aSinglyTaughtPeriodStillReadsAsBefore() {
        val p = period("p1", "ali@x.pk")
        assertEquals(listOf("ali@x.pk"), p.teacherIds)
        assertEquals("Ali", p.teacherLabel)
    }

    @Test
    fun aCoTeacherCannotBeBookedOnAnOverlappingLecture() {
        val project = period("p1", "ali@x.pk", listOf("sana@x.pk"))
        val clash = period("p2", "sana@x.pk", course = "IT-301", sessionId = "it_2023", start = "09:30", end = "10:30")
        val message = describeTimetableConflict(clash, listOf(project), emptyList(), emptyList())
        assertNotNull(message)
        assertTrue(message!!, message.startsWith("Teacher Sana already has a lecture"))
    }

    @Test
    fun twoPeriodsSharingNoTeacherDoNotClash() {
        val project = period("p1", "ali@x.pk", listOf("sana@x.pk"))
        val other = period("p2", "omar@x.pk", course = "IT-301", sessionId = "it_2023")
        assertNull(describeTimetableConflict(other, listOf(project), emptyList(), emptyList()))
    }

    @Test
    fun theMasterTimetableFlagsTheSharedTeacher() {
        val a = period("p1", "ali@x.pk", listOf("sana@x.pk"))
        val b = period("p2", "omar@x.pk", listOf("sana@x.pk"), course = "IT-301", sessionId = "it_2023")
        val conflicts = masterTimetableConflicts(listOf(a, b))
        assertEquals(ConflictKind.TEACHER, conflicts.getValue("p1").single().kind)
        assertEquals("Sana", a.sharedTeacherWith(b)?.second)
    }

    @Test
    fun aCoTeacherGetsTheClassInTheirAssignments() {
        val project = period("p1", "ali@x.pk", listOf("sana@x.pk"))
        val session = AcademicSession("it_2022", "it", 2022, 2026, ShiftMode.MORNING, 5)
        val assignments = resolveAssignments(listOf(project), listOf(session), emptyList())
        assertEquals(listOf("IT-499"), assignments.map { it.courseCode })
    }

    @Test
    fun breaksKeepNoTeachers() {
        val chosen = listOf(Teacher("ali@x.pk", "Ali", "ali@x.pk", createdAt = Instant.EPOCH, createdBy = null, updatedAt = Instant.EPOCH, updatedBy = null))
        assertTrue(periodTeachers(PeriodType.BREAK, chosen).isEmpty())
        assertEquals(1, periodTeachers(PeriodType.LECTURE, chosen + chosen).size)
    }

    @Test
    fun theSameTeachersInTheSameRolesGroupTogether() {
        val a = period("p1", "ali@x.pk", listOf("sana@x.pk", "omar@x.pk"))
        val b = period("p2", "ali@x.pk", listOf("omar@x.pk", "sana@x.pk"), day = DayOfWeek.TUESDAY)
        val c = period("p3", "sana@x.pk", listOf("ali@x.pk", "omar@x.pk"))
        assertTrue(a.hasSameTeachersAs(b))
        assertFalse(a.hasSameTeachersAs(c))
    }
}
