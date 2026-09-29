package com.mbd.cmscommon.teacher

import com.mbd.cmscommon.controller.shiftClassKey
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session

data class ResolvedAssignment(
    val sessionId: String,
    val sessionLabel: String,
    val courseCode: String,
    val subjectLabel: String,
    /** Structured parts of [sessionLabel], used by the class filters. Blank when unknown. */
    val deptName: String = "",
    val sessionName: String = "",
    val shift: String = "",
    /** The shift this class is taught in; its roster is that shift's students only. Null when unknown. */
    val classShift: Session? = null,
    /** The class's department id and session, for the Department -> Session -> Shift filter. */
    val deptId: String = "",
    val session: AcademicSession? = null,
    /** Sessions merged into this class besides [sessionId] (the primary). Empty when not a merged lecture. */
    val linkedSessionIds: Set<String> = emptySet(),
) {
    /** "IT_2022@EVENING": identifies this class for pickers and navigation. */
    val classKey: String get() = shiftClassKey(sessionId, classShift)

    /** Every session whose roster/attendance/marks belong to this class: the primary plus every linked session. */
    val sessionIds: Set<String> get() = setOf(sessionId) + linkedSessionIds
    val isMerged: Boolean get() = linkedSessionIds.isNotEmpty()
}
