package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.Session

/** Default plan for a new fee structure: Morning pays annually, Evening per semester (editable). */
fun recommendedFeeCadence(shift: Session): FeeType =
    if (shift == Session.MORNING) FeeType.ANNUAL else FeeType.SEMESTER
