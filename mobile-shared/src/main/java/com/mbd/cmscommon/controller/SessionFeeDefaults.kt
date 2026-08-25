package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session

fun recommendedFeeCadence(session: AcademicSession?): FeeType =
    if (session?.shift == Session.MORNING) FeeType.ANNUAL else FeeType.SEMESTER
