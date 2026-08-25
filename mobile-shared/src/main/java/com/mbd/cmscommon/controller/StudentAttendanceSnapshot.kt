package com.mbd.cmscommon.controller

data class StudentAttendanceSnapshot(
    val rows: List<SubjectAttendanceRow>,
    val overallPercent: Float,
    val markedLectures: Int,
    val attendedLectures: Int,
    val subjectsAtRisk: Int,
    val weakestSubject: SubjectAttendanceRow?,
)
