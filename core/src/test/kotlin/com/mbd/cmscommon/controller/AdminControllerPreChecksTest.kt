package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Building
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.BuildingRepository
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import java.lang.reflect.Proxy
import java.time.DayOfWeek
import java.time.Instant
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdminControllerPreChecksTest {

    private val now = Instant.EPOCH

    private fun building(id: String, name: String) =
        Building(buildingId = id, name = name, code = null, createdAt = now, createdBy = "", updatedAt = now, updatedBy = "")

    private fun room(buildingId: String, no: String) = Room(
        roomId = "$buildingId--$no", buildingId = buildingId, roomNo = no, name = null, capacity = null, isOffice = false,
        createdAt = now, createdBy = "", updatedAt = now, updatedBy = "",
    )

    private fun department(id: String, name: String, code: String) =
        Department(id, name, code, createdAt = now, createdBy = "", updatedAt = now, updatedBy = "")

    private class FakeBuildings(initial: List<Building>) : BuildingRepository {
        val state = MutableStateFlow(initial)
        override fun observeActiveBuildings(): Flow<List<Building>> = state
        override suspend fun getBuilding(buildingId: String) = state.value.firstOrNull { it.buildingId == buildingId }
        override suspend fun sync() {}
        override suspend fun createBuilding(building: Building) { state.value = state.value + building }
        override suspend fun updateBuilding(building: Building) {}
        override suspend fun deleteBuilding(buildingId: String) { state.value = state.value.filterNot { it.buildingId == buildingId } }
    }

    private class FakeRooms(initial: List<Room>) : RoomRepository {
        val state = MutableStateFlow(initial)
        override fun observeActiveRooms(): Flow<List<Room>> = state
        override suspend fun getRoom(roomId: String) = state.value.firstOrNull { it.roomId == roomId }
        override suspend fun sync() {}
        override suspend fun createRoom(room: Room) { state.value = state.value + room }
        override suspend fun updateRoom(room: Room) {}
        override suspend fun deleteRoom(roomId: String) { state.value = state.value.filterNot { it.roomId == roomId } }
    }

    private class FakeDepartments(initial: List<Department>) : DepartmentRepository {
        val state = MutableStateFlow(initial)
        var deleted: String? = null
        override fun observeActiveDepartments(): Flow<List<Department>> = state
        override suspend fun getDepartment(deptId: String) = state.value.firstOrNull { it.deptId == deptId }
        override suspend fun sync() {}
        override suspend fun createDepartment(department: Department) { state.value = state.value + department }
        override suspend fun updateDepartment(department: Department) {}
        override suspend fun deleteDepartment(deptId: String) { deleted = deptId }
    }

    /** Only observeSessionsForDept is implemented; anything else the controller touches fails the test loudly. */
    private fun sessionRepository(sessions: List<AcademicSession>): AcademicSessionRepository =
        Proxy.newProxyInstance(javaClass.classLoader, arrayOf(AcademicSessionRepository::class.java)) { _, method, args ->
            if (method.name == "observeSessionsForDept") flowOf(sessions.filter { it.deptId == args[0] }) else error("unexpected call: ${method.name}")
        } as AcademicSessionRepository

    private val scope = CoroutineScope(Dispatchers.Unconfined)

    private fun buildingsController(buildings: List<Building>, rooms: List<Room>): BuildingsRoomsController {
        val controller = BuildingsRoomsController(FakeBuildings(buildings), FakeRooms(rooms), "admin@x", scope)
        scope.launch { controller.buildings.collect { } }
        scope.launch { controller.rooms.collect { } }
        return controller
    }

    // ---- buildings & rooms ----

    @Test
    fun deletingABuildingThatStillHasRoomsNamesTheRooms() {
        val controller = buildingsController(
            listOf(building("main", "Main Block")),
            listOf(room("main", "101"), room("main", "102"), room("main", "103"), room("main", "104"), room("main", "105")),
        )
        controller.deleteBuilding("main")
        assertEquals("Main Block still has 5 room(s): 101, 102, 103 and 2 more. Delete those rooms first.", controller.error.value)
    }

    @Test
    fun addingABuildingWithAnExistingNameNamesIt() {
        val controller = buildingsController(listOf(building("main", "Main Block")), emptyList())
        controller.createBuilding("main block", null)
        assertEquals("A building called \"Main Block\" already exists.", controller.error.value)
    }

    @Test
    fun addingADuplicateRoomNamesTheBuildingAndRoom() {
        val controller = buildingsController(listOf(building("main", "Main Block")), listOf(room("main", "101")))
        controller.createRoom("main", "101", null, null, false)
        assertEquals("Main Block already has a room 101.", controller.error.value)
    }

    @Test
    fun addingARoomToABuildingThatNoLongerExistsSaysSo() {
        val controller = buildingsController(emptyList(), emptyList())
        controller.createRoom("gone", "101", null, null, false)
        assertEquals("That building no longer exists. Refresh and choose again.", controller.error.value)
    }

    @Test
    fun aUnexpectedFailureNamesTheAction() {
        val rooms = object : RoomRepository {
            override fun observeActiveRooms(): Flow<List<Room>> = flowOf(emptyList())
            override suspend fun getRoom(roomId: String): Room? = null
            override suspend fun sync() {}
            override suspend fun createRoom(room: Room) { throw RuntimeException("boom") }
            override suspend fun updateRoom(room: Room) {}
            override suspend fun deleteRoom(roomId: String) {}
        }
        val controller = BuildingsRoomsController(FakeBuildings(listOf(building("main", "Main Block"))), rooms, "admin@x", scope)
        scope.launch { controller.buildings.collect { } }
        controller.createRoom("main", "201", null, null, false)
        assertTrue(controller.error.value!!.startsWith("Couldn't add the room. (Ref "))
    }

    // ---- departments ----

    @Test
    fun creatingADepartmentWithAnExistingCodeNamesTheOtherDepartment() {
        val repo = FakeDepartments(listOf(department("eng", "English", "ENG")))
        val controller = DepartmentsActionController(repo, "admin@x", scope)
        controller.create("English Literature", "ENG")
        assertEquals("A department with code ENG already exists (English).", controller.error.value)
    }

    @Test
    fun creatingADepartmentWithAnExistingNameNamesTheCode() {
        val repo = FakeDepartments(listOf(department("eng", "English", "ENG")))
        val controller = DepartmentsActionController(repo, "admin@x", scope)
        controller.create("english", "EL")
        assertEquals("A department named \"English\" already exists (code ENG).", controller.error.value)
    }

    @Test
    fun deletingADepartmentThatStillHasSessionsNamesThem() {
        val repo = FakeDepartments(listOf(department("eng", "English", "ENG")))
        val sessions = listOf(
            AcademicSession("eng_2023", "eng", 2023, 2027, ShiftMode.MORNING, 5, maxStudents = 100),
            AcademicSession("eng_2024", "eng", 2024, 2028, ShiftMode.MORNING, 3, maxStudents = 100),
        )
        val controller = DepartmentsActionController(repo, "admin@x", scope, sessionRepository(sessions))
        controller.delete("eng")
        assertEquals("ENG still has 2 session(s) (2023–2027, 2024–2028). Delete or graduate those sessions first.", controller.error.value)
        assertNull(repo.deleted)
    }

    @Test
    fun aDepartmentWithNoSessionsIsDeleted() {
        val repo = FakeDepartments(listOf(department("eng", "English", "ENG")))
        val controller = DepartmentsActionController(repo, "admin@x", scope, sessionRepository(emptyList()))
        controller.delete("eng")
        assertNull(controller.error.value)
        assertEquals("eng", repo.deleted)
    }

    // ---- master timetable column moves ----

    private val engSession = AcademicSession("eng_2023", "eng", 2023, 2027, ShiftMode.MORNING, 5, maxStudents = 100)
    private val islSession = AcademicSession("isl_2023", "isl", 2023, 2027, ShiftMode.MORNING, 3, maxStudents = 100)
    private val depts = listOf(department("eng", "English", "ENG"), department("isl", "Islamic Studies", "ISL"))

    private fun period(sessionId: String, course: String, teacher: String, start: String, end: String, room: String) = SessionPeriod(
        SessionPeriod.buildId(sessionId, Session.MORNING, DayOfWeek.MONDAY, start),
        sessionId, Session.MORNING, DayOfWeek.MONDAY, start, end, course, "Subject $course", teacher, "Teacher $teacher", roomNo = room,
    )

    private fun grid(session: AcademicSession, periods: List<SessionPeriod>) =
        MasterGrid(session.currentSemester, ProgramType.BS, Session.MORNING, listOf(MasterGridRow(session, null, periods)))

    @Test
    fun movingAColumnOntoAnotherClassesLectureNamesThatClass() {
        val mine = period("eng_2023", "EL-309", "t1", "09:00", "10:00", "R1")
        val theirs = period("isl_2023", "BS-301", "t1", "10:00", "11:00", "R9")
        val moves = columnMoves(grid(engSession, listOf(mine)), mapOf(("09:00" to "10:00") to ("10:00" to "11:00")))

        val message = columnMoveConflict(moves, listOf(mine, theirs), listOf(engSession, islSession), depts)

        assertNotNull(message)
        assertTrue(message!!, message.startsWith("Teacher Teacher t1 already has a lecture with ISL Semester 3 Morning — Subject BS-301 (BS-301) on Monday"))
    }

    @Test
    fun twoColumnsSwappingPlacesDoNotFlagEachOther() {
        val a = period("eng_2023", "EL-309", "t1", "09:00", "10:00", "R1")
        val b = period("eng_2023", "EL-310", "t1", "10:00", "11:00", "R1")
        val moves = columnMoves(
            grid(engSession, listOf(a, b)),
            mapOf(("09:00" to "10:00") to ("10:00" to "11:00"), ("10:00" to "11:00") to ("09:00" to "10:00")),
        )
        assertNull(columnMoveConflict(moves, listOf(a, b), listOf(engSession), depts))
    }

    @Test
    fun movingToAFreeSlotIsAllowed() {
        val mine = period("eng_2023", "EL-309", "t1", "09:00", "10:00", "R1")
        val theirs = period("isl_2023", "BS-301", "t1", "12:00", "13:00", "R9")
        val moves = columnMoves(grid(engSession, listOf(mine)), mapOf(("09:00" to "10:00") to ("10:00" to "11:00")))
        assertNull(columnMoveConflict(moves, listOf(mine, theirs), listOf(engSession, islSession), depts))
    }
}
