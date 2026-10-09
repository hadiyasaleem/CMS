package com.mbd.cmscommon.domain.model

/**
 * An independent Department / Semester / Shift filter for browse/list screens -- e.g. the master
 * timetable's own filter bar, or fee structures. Every level is optional ("All ...") and applies on
 * its own, with no cascade between them (unlike [ShiftScope], whose session level narrows picking a
 * specific academic batch -- the right choice when the point IS to pick one exact batch, such as
 * composing a notification's audience or a teacher's own class picker). This type is for the opposite
 * case: narrowing a listing across many batches at once, where "current semester" is the natural unit
 * rather than any one session's id.
 */
data class DeptSemesterScope(
    val deptId: String? = null,
    val semester: Int? = null,
    val shift: Session? = null,
) {
    val isEmpty: Boolean get() = deptId == null && semester == null && shift == null

    /** A session is inside the scope when its department/semester match and it runs the chosen shift (if any). */
    fun matches(session: AcademicSession): Boolean =
        (deptId == null || deptId == session.deptId) &&
            (semester == null || semester == session.currentSemester) &&
            (shift == null || session.runs(shift))

    /**
     * Whether an item already resolved to ([itemDeptId], [itemSemester], [itemShift]) falls inside this
     * scope. A null item level is shared across that level and never excludes the item -- same
     * convention as [ShiftScope.matches].
     */
    fun matches(itemDeptId: String?, itemSemester: Int?, itemShift: Session?): Boolean =
        (deptId == null || itemDeptId == null || deptId == itemDeptId) &&
            (semester == null || itemSemester == null || semester == itemSemester) &&
            (shift == null || itemShift == null || shift == itemShift)

    companion object {
        val ALL = DeptSemesterScope()
    }
}
