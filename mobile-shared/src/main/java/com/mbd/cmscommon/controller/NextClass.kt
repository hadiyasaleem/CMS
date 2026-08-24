package com.mbd.cmscommon.controller

data class NextClass(
    val courseCode: String,
    val subjectName: String,
    val timeRange: String,
    val teacherName: String,
    val dayLabel: String,
    val location: String? = null,
)
