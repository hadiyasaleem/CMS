package com.mbd.cmscommon.teacher

import org.junit.Test
import org.junit.Assert.assertEquals

class AssignmentFilterTest {
    private fun a(code: String, dept: String, session: String, shift: String) =
        ResolvedAssignment("s-$dept-$session-$shift", "$dept $session", code, code, dept, session, shift)

    private val all = listOf(
        a("A", "IT", "2021", "Morning"),
        a("B", "IT", "2022", "Evening"),
        a("C", "CS", "2021", "Morning"),
    )

    @Test
    fun noFilterKeepsEverything() = assertEquals(3, all.filtered(AssignmentFilter()).size)

    @Test
    fun onlyChosenFiltersApply() {
        assertEquals(listOf("A", "B"), all.filtered(AssignmentFilter(dept = "IT")).map { it.courseCode })
        assertEquals(listOf("A"), all.filtered(AssignmentFilter(dept = "IT", session = "2021")).map { it.courseCode })
        assertEquals(listOf("A", "C"), all.filtered(AssignmentFilter(shift = "Morning")).map { it.courseCode })
    }
}
