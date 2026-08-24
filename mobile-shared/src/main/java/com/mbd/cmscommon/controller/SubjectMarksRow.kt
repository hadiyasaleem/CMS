package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SubjectType

data class SubjectMarksRow(
    val courseCode: String,
    val subjectName: String,
    val midterm: Int?,
    val sessional: Int?,
    val midtermAbsent: Boolean = false,
    val sessionalAbsent: Boolean = false,
    val midtermMaxMarks: Int? = null,
    val sessionalMaxMarks: Int? = null,
    val midtermRemarks: String? = null,
    val sessionalRemarks: String? = null,
    val semester: Int? = null,
    val creditHours: Int? = null,
    val subjectType: SubjectType? = null,
    val isElective: Boolean = false,
) {
    val total: Int get() = (midterm ?: 0) + (sessional ?: 0)
    val totalMaxMarks: Int get() = (midtermMaxMarks ?: 0) + (sessionalMaxMarks ?: 0)
    val percentage: Float? get() = totalMaxMarks.takeIf { it > 0 }?.let { (total * 100f) / it }
    val enteredAssessments: Int get() = listOf(midtermMaxMarks, sessionalMaxMarks).count { it != null }
    val absentAssessments: Int get() = listOf(midtermAbsent, sessionalAbsent).count { it }
}
