package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.NotificationPriority
import com.mbd.cmscommon.domain.model.NotificationTargetRole
import java.time.Instant

/**
 * A notice to send. Its audience is a Department/Semester/Shift/Program filter, the same shape as the
 * master timetable's own filter bar: department and shift alone still resolve to one college-wide or
 * department-wide send exactly as before, but once semester or program type narrows it, there's no
 * longer a single session that represents "every 3rd-semester class" -- [NotificationsController.send]
 * fans that out into one notice per matching class instead.
 */
data class NotificationDraft(
    val title: String,
    val body: String,
    val targetRole: NotificationTargetRole,
    val priority: NotificationPriority,
    val departmentId: String? = null,
    val semester: Int? = null,
    val programType: ProgramType? = null,
    val expiresAt: Instant? = null,
    /** Narrows the audience to one shift. */
    val shift: Session? = null,
)
