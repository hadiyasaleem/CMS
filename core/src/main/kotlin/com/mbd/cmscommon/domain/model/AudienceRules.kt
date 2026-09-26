package com.mbd.cmscommon.domain.model

/**
 * Event and notification targeting, the same progressive scope as the Department -> Session -> Shift filter:
 * nothing set is college-wide, a department reaches all its sessions and both shifts, a session reaches both of
 * its shifts, and a shift narrows to that shift only. A null level means "no restriction at that level".
 */
data class AudienceTarget(
    val deptId: String? = null,
    val sessionId: String? = null,
    val shift: Session? = null,
) {
    val isCollegeWide: Boolean get() = deptId == null && sessionId == null && shift == null
}

/** One shift of one session a teacher teaches (from their timetable periods). */
data class TaughtClass(val sessionId: String, val deptId: String, val shift: Session)

/** Who is looking. The database applies the same rule in RLS (see teacher_reaches in the migrations). */
sealed interface AudienceViewer {
    /** Admins see every item. */
    data object Admin : AudienceViewer

    /** A student's own department, session and shift; null levels (unlinked, or roster not synced) match nothing narrower. */
    data class Student(val deptId: String?, val sessionId: String?, val shift: Session?) : AudienceViewer

    /** A teacher reaches the sessions and shifts they teach, and department-wide items of their home or taught departments. */
    data class Teacher(val homeDeptId: String?, val classes: Set<TaughtClass>) : AudienceViewer
}

/** Whether [target] reaches [viewer]: every level the target sets must match what the viewer belongs to. */
fun audienceReaches(target: AudienceTarget, viewer: AudienceViewer): Boolean = when (viewer) {
    AudienceViewer.Admin -> true
    is AudienceViewer.Student ->
        levelMatches(target.deptId, viewer.deptId) &&
            levelMatches(target.sessionId, viewer.sessionId) &&
            (target.shift == null || target.shift == viewer.shift)
    is AudienceViewer.Teacher -> when {
        target.sessionId != null -> viewer.classes.any { it.sessionId == target.sessionId && (target.shift == null || it.shift == target.shift) }
        target.deptId != null -> target.deptId.equals(viewer.homeDeptId, ignoreCase = true) ||
            viewer.classes.any { it.deptId.equals(target.deptId, ignoreCase = true) }
        else -> true
    }
}

private fun levelMatches(target: String?, own: String?): Boolean =
    target.isNullOrBlank() || target.equals(own, ignoreCase = true)

val CalendarEvent.audienceTarget: AudienceTarget
    get() = AudienceTarget(deptId?.ifBlank { null }, sessionId?.ifBlank { null }, shift)

val Notification.audienceTarget: AudienceTarget
    get() = AudienceTarget(targetDeptId?.ifBlank { null }, targetOfferingId?.ifBlank { null }, targetShift)
