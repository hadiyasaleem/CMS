package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.CalendarEvent
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamPaperSubmission
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.MarkEditStatus
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ShiftScope
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShiftScopeFiltersTest {

    private val it22 = AcademicSession("IT_2022", "IT", 2022, 2026, ShiftMode.BOTH, 3, maxStudents = 100)
    private val it23 = AcademicSession("IT_2023", "IT", 2023, 2027, ShiftMode.MORNING, 1, maxStudents = 50)
    private val cs22 = AcademicSession("CS_2022", "CS", 2022, 2026, ShiftMode.EVENING, 3, maxStudents = 50)
    private val sessions = listOf(it22, it23, cs22)
    private val departments = listOf(
        Department("IT", "Information Technology", "IT", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = ""),
        Department("CS", "Computer Science", "CS", createdAt = Instant.EPOCH, createdBy = "", updatedAt = Instant.EPOCH, updatedBy = ""),
    )

    private val itEvening = ShiftScope("IT", "IT_2022", Session.EVENING)

    private fun assignment(session: AcademicSession, shift: Session, code: String) =
        ResolvedAssignment(session.sessionId, "${session.deptId} · ${session.label} (${shift.shortLabel})", code, code, session.deptId, session.label, shift.label, shift, session.deptId, session)

    @Test
    fun teacherClassPickerNarrowsByEachChosenLevel() {
        val classes = listOf(
            assignment(it22, Session.MORNING, "IT-301"),
            assignment(it22, Session.EVENING, "IT-301"),
            assignment(it23, Session.MORNING, "IT-101"),
            assignment(cs22, Session.EVENING, "CS-301"),
        )
        assertEquals(4, classes.inScope(ShiftScope.ALL).size)
        assertEquals(3, classes.inScope(ShiftScope(deptId = "IT")).size)
        assertEquals(2, classes.inScope(ShiftScope("IT", "IT_2022")).size)
        assertEquals(listOf("IT_2022@EVENING"), classes.inScope(itEvening).map { it.classKey })
        // A shift alone (no department) keeps every Evening class.
        assertEquals(listOf("IT-301", "CS-301"), classes.inScope(ShiftScope(shift = Session.EVENING)).map { it.courseCode })
        // The picker's options are the teacher's own departments and sessions.
        assertEquals(listOf("CS" to "CS", "IT" to "IT"), classes.scopeDepartments())
        assertEquals(listOf("IT_2023", "CS_2022", "IT_2022"), classes.scopeSessions().map { it.sessionId })
    }

    @Test
    fun semesterResultsClassesFollowTheScope() {
        val classes = shiftClassOptions(sessions) { "${it.deptId} ${it.label}" }
        assertEquals(listOf("IT_2022@MORNING", "IT_2022@EVENING", "IT_2023@MORNING", "CS_2022@EVENING"), classes.map { it.first })
        assertEquals(listOf("IT_2022@EVENING"), classesInScope(classes, itEvening, sessions).map { it.first })
        assertEquals(listOf("IT_2022@MORNING", "IT_2022@EVENING", "IT_2023@MORNING"), classesInScope(classes, ShiftScope(deptId = "IT"), sessions).map { it.first })
        assertEquals(listOf("IT_2022@EVENING", "CS_2022@EVENING"), classesInScope(classes, ShiftScope(shift = Session.EVENING), sessions).map { it.first })
    }

    @Test
    fun reviewQueuesUseTheRollNumbersShift() {
        fun edit(roll: String, session: String = "IT_2022") =
            MarkEditRequest(roll, session, 3, "IT-301", ExamType.MIDTERM, roll, 10, 12, null, MarkEditStatus.PENDING, "t", null, Instant.EPOCH, null)
        val edits = listOf(edit("IT-22-05"), edit("IT-22-55"), edit("CS-22-60", "CS_2022"))
        assertEquals(listOf("IT-22-55"), edits.inScope(itEvening, sessions).map { it.rollNumber })
        assertEquals(listOf("IT-22-05", "IT-22-55"), edits.inScope(ShiftScope(deptId = "IT"), sessions).map { it.rollNumber })

        fun link(roll: String, session: String?) =
            StudentLinkRequest("r$roll", "u", session, roll, null, null, null, status = LinkRequestStatus.PENDING, reviewedBy = null, reviewedAt = null, createdAt = Instant.EPOCH)
        val links = listOf(link("IT-22-02", "IT_2022"), link("IT-22-52", "IT_2022"), link("X-1", null))
        assertEquals(listOf("IT-22-52"), links.inScope(itEvening, sessions).map { it.rollNumberClaimed })
        // A request without a claimed session only shows under "All".
        assertEquals(3, links.inScope(ShiftScope.ALL, sessions).size)
        assertEquals(2, links.inScope(ShiftScope(deptId = "IT"), sessions).size)
    }

    @Test
    fun eventsReachTheScopesTheyTarget() {
        val events = listOf(
            CalendarEvent("college", "Convocation", "EVENT", "2026-10-01"),
            CalendarEvent("it", "IT seminar", "EVENT", "2026-10-02", deptId = "IT"),
            CalendarEvent("it22", "IT 2022 trip", "EVENT", "2026-10-03", deptId = "IT", sessionId = "IT_2022"),
            CalendarEvent("it22m", "Morning quiz", "EXAM", "2026-10-04", deptId = "IT", sessionId = "IT_2022", shift = Session.MORNING),
            CalendarEvent("cs", "CS seminar", "EVENT", "2026-10-05", deptId = "CS"),
        )
        assertEquals(listOf("college", "it", "it22"), events.inScope(itEvening, sessions).map { it.id })
        assertEquals(listOf("college", "it", "it22", "it22m"), events.inScope(ShiftScope(deptId = "IT"), sessions).map { it.id })
        assertEquals(5, events.inScope(ShiftScope.ALL, sessions).size)
    }

    @Test
    fun insightsSplitByShift() {
        val overviews = listOf(
            SessionOverview("IT_2022", "IT", Session.MORNING, 3, 40, 3.1, 80.0),
            SessionOverview("IT_2022", "IT", Session.EVENING, 3, 30, 2.9, 76.0),
            SessionOverview("CS_2022", "CS", Session.EVENING, 3, 20, 3.0, 90.0),
        )
        // The older cached risk row has no shift: its roll number's block puts it in Evening.
        val risk = listOf(AtRiskStudent("IT_2022", "IT-22-03", "A", 1.5, 60.0, Session.MORNING), AtRiskStudent("IT_2022", "IT-22-60", "B", 1.8, null))
        val scoped = scopeInsights(overviews, risk, emptyList(), itEvening, sessions)
        assertEquals(listOf(Session.EVENING), scoped.overviews.map { it.shift })
        assertEquals(listOf("IT-22-60"), scoped.atRisk.map { it.rollNumber })
        assertEquals(2, scopeInsights(overviews, risk, emptyList(), ShiftScope(shift = Session.EVENING), sessions).overviews.size)
    }

    @Test
    fun papersAreSharedByBothShiftsOfTheirSession() {
        fun paper(session: String) = ExamPaperSubmission("p$session", "slot", session, 3, "IT-301", "t@x", "path", "f.pdf", uploadedAt = Instant.EPOCH, createdBy = "t")
        val papers = listOf(paper("IT_2022"), paper("IT_2023"), paper("CS_2022"))
        assertEquals(listOf("IT_2022"), submittedPapersMatching(papers, SubmittedPapersFilters(deptId = "IT", sessionId = "IT_2022", shift = Session.EVENING), sessions).map { it.offeringId })
        // IT 2023 runs Morning only, so it has no Evening papers.
        assertEquals(listOf("IT_2022", "CS_2022"), submittedPapersMatching(papers, SubmittedPapersFilters(shift = Session.EVENING), sessions).map { it.offeringId })
    }

    @Test
    fun dashboardAndHubsCountInsideTheScope() {
        fun teacher(id: String, dept: String) = Teacher(id, id, "$id@x", deptId = dept, createdAt = Instant.EPOCH, createdBy = null, updatedAt = Instant.EPOCH, updatedBy = null)
        val profiles = listOf(
            StudentProfile(sessionId = "IT_2022", rollNumber = "IT-22-01", name = "A", shift = Session.MORNING),
            StudentProfile(sessionId = "IT_2022", rollNumber = "IT-22-51", name = "B", shift = Session.EVENING),
            StudentProfile(sessionId = "CS_2022", rollNumber = "CS-22-51", name = "C", shift = Session.EVENING),
        )
        val sources = DashboardSources(3, profiles, listOf(teacher("t1", "IT"), teacher("t2", "CS")), departments, sessions)
        val all = dashboardState(sources, emptyList(), ShiftScope.ALL)
        assertEquals(DashboardState(students = 3, teachers = 2, departments = 2, pendingRequests = 0, activeSessions = 3), all)
        val evening = dashboardState(sources, emptyList(), itEvening)
        assertEquals(DashboardState(students = 1, teachers = 1, departments = 1, pendingRequests = 0, activeSessions = 1), evening)

        val records = RecordsHubSources(
            sessions,
            listOf(CalendarEvent("cs", "CS", "EVENT", "2026-10-05", deptId = "CS"), CalendarEvent("all", "All", "EVENT", "2026-10-05")),
            listOf(Datesheet("m", "IT_2022", Session.MORNING, 3, published = true), Datesheet("e", "IT_2022", Session.EVENING, 3)),
            emptyList(),
        )
        val snapshot = recordsHubSnapshotInScope(records, itEvening, LocalDate.of(2026, 10, 1))
        assertEquals(1, snapshot.activeSessions)
        assertEquals(1, snapshot.upcomingEvents)
        assertEquals(0, snapshot.publishedDatesheets)
        assertEquals(1, snapshot.draftDatesheets)
    }

    @Test
    fun exportTitlesNameTheScope() {
        assertEquals("Information Technology · 2022–2026 · Evening shift", itEvening.title(departments, sessions))
        assertEquals("Computer Science", ShiftScope(deptId = "CS").title(departments, sessions))
        assertNull(ShiftScope.ALL.title(departments, sessions))
        assertEquals(listOf("Insights", "Computer Science", "Generated"), scopedTitle(listOf("Insights", "Generated"), "Computer Science"))
        assertEquals("_IT_2022_evening", itEvening.fileSuffix())
    }

    @Test
    fun cascadeScreensMapToTheScope() {
        assertEquals(itEvening, cascadeScope("IT", 2022, Session.EVENING, sessions))
        assertEquals(ScopeCascade("IT", 2022, Session.EVENING), itEvening.toCascade(sessions))
        assertEquals(ScopeCascade("CS", null, null), ShiftScope(deptId = "CS").toCascade(sessions))
    }
}
