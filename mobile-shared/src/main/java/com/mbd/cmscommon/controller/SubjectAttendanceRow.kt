package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SubjectType

data class SubjectAttendanceRow(
    val courseCode: String,
    val subjectName: String,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val creditHours: Int? = null,
    val subjectType: SubjectType? = null,
    val semester: Int? = null,
) {
    val total: Int get() = present + absent + leave
    val percentage: Float get() = if (total == 0) 0f else (present * 100f) / total
    val lecturesNeededFor75: Int get() =
        if (total == 0 || percentage >= 75f) 0 else ((total * 3) - (present * 4)).coerceAtLeast(0)
    val missesAvailableAbove75: Int get() =
        if (total == 0 || percentage < 75f) 0 else (((present * 4) - (total * 3)) / 3).coerceAtLeast(0)
}
