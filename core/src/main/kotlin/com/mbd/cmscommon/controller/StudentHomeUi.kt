package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SessionPeriod

data class StudentHomeUi(
    val overallPercent: Float = 0f,
    val subjectCount: Int = 0,
    /** Today's lectures, sorted by start time -- mirrors the teacher app's own "Today's classes" list. */
    val todaysClasses: List<SessionPeriod> = emptyList(),
    /** The id of whichever entry in [todaysClasses] is current/next, or null once today's schedule is done. */
    val nextClassId: String? = null,
    val weakestSubject: WeakSubject? = null,
)
