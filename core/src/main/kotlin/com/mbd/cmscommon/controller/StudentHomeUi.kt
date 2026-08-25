package com.mbd.cmscommon.controller

data class StudentHomeUi(
    val overallPercent: Float = 0f,
    val subjectCount: Int = 0,
    val lecturesToday: Int = 0,
    val nextClass: NextClass? = null,
    val weakestSubject: WeakSubject? = null,
)
