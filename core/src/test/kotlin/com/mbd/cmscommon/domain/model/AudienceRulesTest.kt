package com.mbd.cmscommon.domain.model

import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.domain.repository.notificationReaches
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AudienceRulesTest {

    private val college = AudienceTarget()
    private val itDept = AudienceTarget(deptId = "IT")
    private val it22 = AudienceTarget("IT", "IT_2022")
    private val it22Evening = AudienceTarget("IT", "IT_2022", Session.EVENING)
    private val csDept = AudienceTarget(deptId = "CS")

    private val eveningStudent = AudienceViewer.Student("IT", "IT_2022", Session.EVENING)
    private val morningStudent = AudienceViewer.Student("IT", "IT_2022", Session.MORNING)

    @Test
    fun studentSeesWhenEverySetLevelMatches() {
        listOf(college, itDept, it22, it22Evening).forEach { assertTrue(it.toString(), audienceReaches(it, eveningStudent)) }
        assertFalse(audienceReaches(it22Evening, morningStudent))
        assertFalse(audienceReaches(csDept, eveningStudent))
        assertFalse(audienceReaches(AudienceTarget("IT", "IT_2023"), eveningStudent))
        // A student whose shift isn't known yet doesn't see shift-only notices, as on the server.
        assertFalse(audienceReaches(it22Evening, AudienceViewer.Student("IT", "IT_2022", null)))
        // An unlinked student sees college-wide items only.
        val unlinked = AudienceViewer.Student(null, null, null)
        assertTrue(audienceReaches(college, unlinked))
        assertFalse(audienceReaches(itDept, unlinked))
    }

    @Test
    fun teacherSeesWhatTheyTeachOrWider() {
        val eveningTeacher = AudienceViewer.Teacher("CS", setOf(TaughtClass("IT_2022", "IT", Session.EVENING)))
        assertTrue(audienceReaches(college, eveningTeacher))
        assertTrue(audienceReaches(it22, eveningTeacher))
        assertTrue(audienceReaches(it22Evening, eveningTeacher))
        assertFalse(audienceReaches(it22Evening.copy(shift = Session.MORNING), eveningTeacher))
        assertFalse(audienceReaches(AudienceTarget("IT", "IT_2023"), eveningTeacher))
        // A department notice reaches teachers of that department (home) or teaching in it.
        assertTrue(audienceReaches(itDept, eveningTeacher))
        assertTrue(audienceReaches(csDept, eveningTeacher))
        assertFalse(audienceReaches(AudienceTarget(deptId = "EE"), eveningTeacher))
    }

    @Test
    fun adminsSeeEverything() {
        listOf(college, itDept, it22Evening, csDept).forEach { assertTrue(audienceReaches(it, AudienceViewer.Admin)) }
    }

    @Test
    fun notificationsCheckRoleThenScope() {
        fun notice(role: NotificationTargetRole, session: String? = null, shift: Session? = null, dept: String? = null) =
            Notification("n", "t", "b", role, session, "admin@x", targetDeptId = dept ?: session?.substringBefore('_'), targetShift = shift, createdAt = Instant.EPOCH)
        val student = NotificationAudienceContext("IT_2022", "IT", Session.EVENING)
        assertTrue(notificationReaches(notice(NotificationTargetRole.STUDENT, "IT_2022", Session.EVENING), NotificationTargetRole.STUDENT, student))
        assertFalse(notificationReaches(notice(NotificationTargetRole.STUDENT, "IT_2022", Session.MORNING), NotificationTargetRole.STUDENT, student))
        assertFalse(notificationReaches(notice(NotificationTargetRole.TEACHER), NotificationTargetRole.STUDENT, student))
        assertTrue(notificationReaches(notice(NotificationTargetRole.ALL, dept = "IT"), NotificationTargetRole.STUDENT, student))

        val teacher = NotificationAudienceContext(departmentId = "CS", taughtClasses = setOf(TaughtClass("IT_2022", "IT", Session.MORNING)))
        assertTrue(notificationReaches(notice(NotificationTargetRole.TEACHER, "IT_2022"), NotificationTargetRole.TEACHER, teacher))
        assertFalse(notificationReaches(notice(NotificationTargetRole.TEACHER, "IT_2022", Session.EVENING), NotificationTargetRole.TEACHER, teacher))
        assertTrue(notificationReaches(notice(NotificationTargetRole.ALL, dept = "CS"), NotificationTargetRole.TEACHER, teacher))
    }

    @Test
    fun calendarUsesTheSameRule() {
        fun event(target: AudienceTarget, audience: String = "ALL") =
            CalendarEvent("e", "Event", "EVENT", "2026-10-01", audience = audience, deptId = target.deptId, sessionId = target.sessionId, shift = target.shift)
        val student = CalendarViewerContext(CalendarViewerRole.STUDENT, "IT", setOf("IT_2022"), Session.EVENING)
        assertTrue(isVisibleTo(event(it22Evening), student))
        assertFalse(isVisibleTo(event(it22Evening.copy(shift = Session.MORNING)), student))
        assertFalse(isVisibleTo(event(college, audience = "TEACHER"), student))

        val teacher = CalendarViewerContext(
            CalendarViewerRole.TEACHER, "CS", setOf("IT_2022"),
            taughtClasses = setOf(TaughtClass("IT_2022", "IT", Session.MORNING)),
        )
        assertTrue(isVisibleTo(event(AudienceTarget("IT", "IT_2022", Session.MORNING)), teacher))
        assertFalse(isVisibleTo(event(it22Evening), teacher))
        // Without taught shifts (older callers) a teacher's sessions count for both shifts.
        assertTrue(isVisibleTo(event(it22Evening), teacher.copy(taughtClasses = null)))
        assertEquals(true, isVisibleTo(event(college), CalendarViewerContext(CalendarViewerRole.ADMIN)))
    }
}
