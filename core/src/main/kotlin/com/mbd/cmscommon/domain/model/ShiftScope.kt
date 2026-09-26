package com.mbd.cmscommon.domain.model

/**
 * A progressive Department -> Session -> Shift filter. A null level means "any", so an empty scope matches
 * everything, a department alone matches all its sessions and both shifts, and adding a session then a shift
 * narrows further. Only the chosen levels apply.
 *
 * The same rule decides who an event or notification reaches: its target is a ShiftScope, and a student
 * (dept, session, shift) is inside it when every level the target sets matches.
 */
data class ShiftScope(
    val deptId: String? = null,
    val sessionId: String? = null,
    val shift: Session? = null,
) {
    val isEmpty: Boolean get() = deptId == null && sessionId == null && shift == null

    /**
     * Whether an item at ([itemDeptId], [itemSessionId], [itemShift]) falls inside this scope. A null item
     * level is shared across that level -- e.g. a session's subjects carry no shift, so they match any shift
     * filter -- and never excludes the item.
     */
    fun matches(itemDeptId: String?, itemSessionId: String?, itemShift: Session?): Boolean =
        levelMatches(deptId, itemDeptId) && levelMatches(sessionId, itemSessionId) && levelMatches(shift, itemShift)

    /** A session is inside the scope when its department/id match and it runs the chosen shift (if any). */
    fun matches(session: AcademicSession): Boolean =
        levelMatches(deptId, session.deptId) && levelMatches(sessionId, session.sessionId) &&
            (shift == null || session.runs(shift))

    /** Picking a department clears a session/shift that no longer belongs under it. */
    fun withDept(newDeptId: String?, sessions: Collection<AcademicSession>): ShiftScope {
        val keepSession = sessionId != null && newDeptId != null &&
            sessions.any { it.sessionId == sessionId && it.deptId == newDeptId }
        return if (keepSession) copy(deptId = newDeptId) else ShiftScope(deptId = newDeptId)
    }

    /** Picking a session also fixes its department and drops a shift the session does not run. */
    fun withSession(session: AcademicSession?): ShiftScope = when (session) {
        null -> copy(sessionId = null, shift = null)
        else -> ShiftScope(
            deptId = session.deptId,
            sessionId = session.sessionId,
            shift = shift?.takeIf { session.runs(it) },
        )
    }

    fun withShift(newShift: Session?): ShiftScope = copy(shift = newShift)

    private fun <T> levelMatches(filter: T?, value: T?): Boolean = filter == null || value == null || filter == value

    companion object {
        val ALL = ShiftScope()

        /** Sessions to offer under [scope]'s department (all sessions when no department is chosen). */
        fun sessionOptions(scope: ShiftScope, sessions: Collection<AcademicSession>): List<AcademicSession> =
            sessions.filter { scope.deptId == null || it.deptId == scope.deptId }

        /** Shifts to offer: the chosen session's shifts, or both when no session is chosen. */
        fun shiftOptions(scope: ShiftScope, sessions: Collection<AcademicSession>): List<Session> =
            scope.sessionId?.let { id -> sessions.firstOrNull { it.sessionId == id }?.shifts } ?: Session.entries
    }
}
