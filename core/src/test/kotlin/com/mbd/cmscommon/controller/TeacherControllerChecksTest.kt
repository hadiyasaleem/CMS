package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.MarkEditRequest
import com.mbd.cmscommon.domain.model.LinkRequestStatus
import com.mbd.cmscommon.domain.model.MarkEditStatus
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.StudentLinkRequest
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.MarkEditRequestRepository
import com.mbd.cmscommon.domain.repository.NotificationRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.Outcome
import java.lang.reflect.Proxy
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherControllerChecksTest {

    /** Implements only what a test names; anything else the controller touches fails the test loudly. */
    private inline fun <reified T : Any> fake(crossinline handler: (String, Array<Any?>) -> Any?): T =
        Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { _, method, args ->
            handler(method.name, args ?: emptyArray())
        } as T

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private val session = AcademicSession("isl_2023", "isl", 2023, 2027, ShiftMode.MORNING, 3, maxStudents = 100)
    private val students = listOf(
        SessionStudent(SessionStudent.buildId("isl_2023", "IT-22-01"), "isl_2023", "isl", "IT-22-01", "Ali", Session.MORNING),
        SessionStudent(SessionStudent.buildId("isl_2023", "IT-22-02"), "isl_2023", "isl", "IT-22-02", "Sara", Session.MORNING),
    )
    private val assignment = ResolvedAssignment("isl_2023", "ISL 2023–2027 (M)", "BS-301", "Quran Studies")

    // ------------------------------------------------------------------ MarksEntryController

    private fun pendingRequest(roll: String, requested: Int) = MarkEditRequest(
        id = "req1", sessionId = "isl_2023", semester = 3, courseCode = "BS-301", examType = ExamType.MIDTERM, rollNumber = roll,
        currentScore = 10, requestedScore = requested, reason = null, status = MarkEditStatus.PENDING,
        requestedBy = "t@x", reviewedBy = null, requestedAt = Instant.EPOCH, reviewedAt = null,
    )

    private fun marksController(saved: Map<String, Int> = emptyMap(), pending: List<MarkEditRequest> = emptyList()): MarksEntryController {
        val marks = fake<SessionMarksRepository> { name, _ ->
            when (name) {
                "observeScores" -> MutableStateFlow(saved)
                "observeAbsentRolls" -> flowOf(emptySet<String>())
                "saveScores" -> Unit
                else -> error("unexpected marks call: $name")
            }
        }
        val sessions = fake<AcademicSessionRepository> { name, _ ->
            when (name) {
                "observeStudents" -> flowOf(students)
                "observeSession" -> flowOf(session)
                else -> error("unexpected session call: $name")
            }
        }
        val requests = fake<MarkEditRequestRepository> { name, _ ->
            when (name) {
                "getPendingForAssignment" -> pending
                "submitRequest" -> Unit
                else -> error("unexpected request call: $name")
            }
        }
        val controller = MarksEntryController(marks, sessions, requests, "t@x", scope)
        scope.launch { controller.roster.collect { } }
        scope.launch { controller.savedScores.collect { } }
        scope.launch { controller.displayScores.collect { } }
        scope.launch { controller.session.collect { } }
        controller.select(assignment)
        return controller
    }

    private fun MarksEntryController.saveError(): String = (saveState.value as Outcome.Error).message
    private fun MarksEntryController.requestError(): String = (requestState.value as Outcome.Error).message

    @Test
    fun aNonNumericScoreNamesTheStudentAndTheRange() {
        val controller = marksController()
        controller.setScore(students[0].id, "abc")
        controller.save()
        assertEquals("'abc' isn't a whole number for IT-22-01. Enter a score from 0 to 25.", controller.saveError())
    }

    @Test
    fun aScoreAboveTheMaximumSaysWhichExamAndTheLimit() {
        val controller = marksController()
        controller.setScore(students[1].id, "30")
        controller.save()
        assertEquals("IT-22-02's score of 30 is above the Midterm maximum of 25.", controller.saveError())
    }

    @Test
    fun aNegativeScoreIsExplained() {
        val controller = marksController()
        controller.setScore(students[1].id, "-3")
        controller.save()
        assertEquals("IT-22-02's score can't be negative. Enter a score from 0 to 25.", controller.saveError())
    }

    @Test
    fun anEditRequestForAnUnsavedScoreSaysThereIsNothingToChange() {
        val controller = marksController()
        controller.requestMarkEdit(students[0], 12, null)
        assertEquals("IT-22-01 has no saved Midterm score yet, so there is nothing to change. Enter the score directly.", controller.requestError())
    }

    @Test
    fun anEditRequestToTheSameScoreIsRefused() {
        val controller = marksController(saved = mapOf("IT-22-01" to 10))
        controller.requestMarkEdit(students[0], 10, null)
        assertEquals("IT-22-01's Midterm score is already 10, so there is nothing to change.", controller.requestError())
    }

    @Test
    fun anEditRequestOutsideTheRangeNamesTheRange() {
        val controller = marksController(saved = mapOf("IT-22-01" to 10))
        controller.requestMarkEdit(students[0], 40, null)
        assertEquals("40 is outside the Midterm range of 0 to 25.", controller.requestError())
    }

    @Test
    fun aSecondEditRequestWhileOneIsPendingSaysSo() {
        val controller = marksController(saved = mapOf("IT-22-01" to 10), pending = listOf(pendingRequest("IT-22-01", 12)))
        controller.requestMarkEdit(students[0], 15, null)
        assertEquals("A change of IT-22-01's Midterm score to 12 is already waiting for the admin's review.", controller.requestError())
    }

    @Test
    fun aValidEditRequestIsSubmitted() {
        val controller = marksController(saved = mapOf("IT-22-01" to 10))
        controller.requestMarkEdit(students[0], 15, "typo")
        assertTrue(controller.requestState.value is Outcome.Success<*>)
    }

    // ------------------------------------------------------------------ MarkAttendanceController

    private fun attendanceController(existingAfterOpen: () -> List<DailyAttendanceMark>): MarkAttendanceController {
        val attendance = fake<SessionAttendanceRepository> { name, _ ->
            when (name) {
                "marksBetween" -> existingAfterOpen()
                "markAttendance" -> Unit
                "observeTallies" -> flowOf(emptyList<Any>())
                else -> error("unexpected attendance call: $name")
            }
        }
        val sessions = fake<AcademicSessionRepository> { name, _ ->
            when (name) {
                "observeStudents" -> flowOf(students)
                "observeSession" -> flowOf(session)
                else -> error("unexpected session call: $name")
            }
        }
        val notifications = fake<NotificationRepository> { _, _ -> Unit }
        val curriculum = fake<CurriculumRepository> { name, _ ->
            if (name == "observeSemesterSubjects") flowOf(emptyList<Any>()) else error("unexpected curriculum call: $name")
        }
        val controller = MarkAttendanceController(attendance, sessions, notifications, curriculum, "t@x", scope)
        scope.launch { controller.roster.collect { } }
        controller.select(assignment)
        return controller
    }

    private fun MarkAttendanceController.submitError(): String = (submitState.value as Outcome.Error).message

    @Test
    fun submittingWithUnmarkedStudentsNamesThem() {
        val controller = attendanceController { emptyList() }
        controller.setStatus(students[0].id, AttendanceStatus.PRESENT)
        controller.submit()
        assertEquals("1 of 2 students still need a status: IT-22-02.", controller.submitError())
    }

    @Test
    fun submittingAlreadyMarkedAttendanceSaysSoAndOverwritesNothing() {
        var existing = emptyList<DailyAttendanceMark>()
        val controller = attendanceController { existing }
        students.forEach { controller.setStatus(it.id, AttendanceStatus.PRESENT) }
        // Another device marks the register after this one opened it.
        existing = students.map { DailyAttendanceMark(it.rollNumber, java.time.LocalDate.now(), AttendanceStatus.PRESENT) }

        controller.submit()

        val message = controller.submitError()
        assertTrue(message, message.startsWith("Attendance for Quran Studies on "))
        assertTrue(message, message.endsWith("was already marked for 2 of 2 students. It has been reloaded; nothing was overwritten."))
    }

    @Test
    fun aFullyMarkedRegisterSubmitsCleanly() {
        val controller = attendanceController { emptyList() }
        students.forEach { controller.setStatus(it.id, AttendanceStatus.PRESENT) }
        controller.submit()
        assertTrue(controller.submitState.value is Outcome.Success<*>)
        assertNull(controller.error.value)
    }

    // ------------------------------------------------------------------ link request approval

    private fun linkRequest(name: String? = "Ali") = StudentLinkRequest(
        requestId = "r1", requestedByUid = "ali@x.pk", sessionIdClaimed = "isl_2023", rollNumberClaimed = "IT-22-01", nameClaimed = name,
        cnicClaimed = null, dobClaimed = null, status = LinkRequestStatus.PENDING, reviewedBy = null, reviewedAt = null, createdAt = Instant.EPOCH,
    )

    @Test
    fun anUnverifiedOrMissingRosterEntryBlocksApprovalWithAReason() {
        assertEquals(
            "Roll number IT-22-01 isn't on the selected session's roster, so Ali can't be linked yet. Add that student to the roster first.",
            linkApprovalBlockedMessage(linkRequest(), LinkRequestVerification(RosterVerificationState.MISSING), override = false),
        )
        assertEquals(
            "Ali's request is still being checked against the roster. Wait a moment and try again.",
            linkApprovalBlockedMessage(linkRequest(), null, override = false),
        )
        assertEquals(
            "Couldn't verify Ali: The roster record exists, but its official profile could not be loaded.",
            linkApprovalBlockedMessage(
                linkRequest(),
                LinkRequestVerification(RosterVerificationState.FAILED, message = "The roster record exists, but its official profile could not be loaded."),
                override = false,
            ),
        )
    }

    @Test
    fun anIdentityMismatchBlocksApprovalUnlessTheReviewerOverrides() {
        val mismatch = LinkRequestVerification(RosterVerificationState.IDENTITY_MISMATCH)
        assertTrue(linkApprovalBlockedMessage(linkRequest(), mismatch, override = false)!!.startsWith("The details Ali gave don't match the official record for roll number IT-22-01."))
        assertNull(linkApprovalBlockedMessage(linkRequest(), mismatch, override = true))
    }

    @Test
    fun matchedAndRelinkRequestsCanBeApproved() {
        assertNull(linkApprovalBlockedMessage(linkRequest(), LinkRequestVerification(RosterVerificationState.MATCHED), override = false))
        assertNull(linkApprovalBlockedMessage(linkRequest(), LinkRequestVerification(RosterVerificationState.RELINK), override = false))
    }

    @Test
    fun aRequestWithoutANameIsDescribedByItsRollNumber() {
        assertEquals("IT-22-01", linkRequestWho(linkRequest(name = null)))
        assertEquals("Ali", linkRequestWho(linkRequest()))
    }
}
