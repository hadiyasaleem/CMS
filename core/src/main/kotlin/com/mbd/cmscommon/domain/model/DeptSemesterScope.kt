package com.mbd.cmscommon.domain.model

/**
 * An independent Department / Semester / Shift / Program filter -- the master timetable's own filter
 * bar, fee structures, and every "browse many batches at once" screen. Every level is optional
 * ("All ...") and applies on its own, with no cascade between them (unlike [ShiftScope], whose session
 * level narrows picking a specific academic batch directly -- the right choice when the point IS to
 * pick one exact batch by name, e.g. a teacher's own class picker for marks entry). This type is for
 * the opposite case: narrowing -- or, once department+semester+program narrow to a single batch,
 * resolving -- across many batches, where "current semester" is the natural unit rather than any one
 * session's id.
 */
data class DeptSemesterScope(
    val deptId: String? = null,
    val semester: Int? = null,
    val shift: Session? = null,
    val programType: ProgramType? = null,
) {
    val isEmpty: Boolean get() = deptId == null && semester == null && shift == null && programType == null

    /** A session is inside the scope when its department/semester/program match and it runs the chosen shift (if any). */
    fun matches(session: AcademicSession): Boolean =
        (deptId == null || deptId == session.deptId) &&
            (semester == null || semester == session.currentSemester) &&
            (programType == null || programType == session.programType) &&
            (shift == null || session.runs(shift))

    /**
     * Whether an item already resolved to ([itemDeptId], [itemSemester], [itemShift], [itemProgramType])
     * falls inside this scope. A null item level is shared across that level and never excludes the
     * item -- same convention as [ShiftScope.matches]. For an item that only knows its session id and
     * needs its department/semester/program resolved from it, use [matchesSessionItem] instead.
     */
    fun matches(itemDeptId: String?, itemSemester: Int?, itemShift: Session?, itemProgramType: ProgramType? = null): Boolean =
        (deptId == null || itemDeptId == null || deptId == itemDeptId) &&
            (semester == null || itemSemester == null || semester == itemSemester) &&
            (programType == null || itemProgramType == null || programType == itemProgramType) &&
            (shift == null || itemShift == null || shift == itemShift)

    companion object {
        val ALL = DeptSemesterScope()
    }
}
