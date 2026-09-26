package com.mbd.cmscommon.util

import com.mbd.cmscommon.domain.model.Session

data class ImportedStudentRow(
    val rowNumber: Int,
    val rollNumber: String,
    val name: String,
    /** From an optional "Shift" column; null means "follow the roll number's block". */
    val shift: Session? = null,
)
