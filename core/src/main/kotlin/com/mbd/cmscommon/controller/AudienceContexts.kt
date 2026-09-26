package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.NotificationAudienceContext
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.util.StudentIdCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** A teacher reads notices for their home department and for the sessions and shifts they teach. */
fun teacherNotificationAudience(teacher: Teacher?, assignments: List<ResolvedAssignment>): NotificationAudienceContext =
    NotificationAudienceContext(departmentId = teacher?.deptId, taughtClasses = assignments.taughtClasses())

/** A student reads notices for their department, session and shift. */
fun studentNotificationAudience(sessionId: String?, shift: Session?): NotificationAudienceContext =
    NotificationAudienceContext(sessionId = sessionId, departmentId = sessionId?.let(StudentIdCodec::deptIdOf), shift = shift)

/** The student's audience, following their roster row's shift once it has synced. */
fun AcademicSessionRepository.observeStudentAudience(sessionId: String, rollNumber: String): Flow<NotificationAudienceContext> =
    observeShiftOf(sessionId, rollNumber).map { studentNotificationAudience(sessionId, it) }
