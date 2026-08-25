package com.mbd.cmsteacher.feature.attendance

data class ExportMeta(
    val teacherName: String = "",
    val subjectName: String = "",
    val creditHours: Int? = null,
    val timeslots: List<String> = emptyList(),
    val sessionLabel: String = "",
    val deptId: String = "",
    val shift: String = "",
    val semester: Int = 0,
)
