package com.mbd.cmsteacher.feature.attendance

data class StudentTotals(
    val present: Int,
    val absent: Int,
    val leave: Int,
    val late: Int,
) {
    val marked: Int get() = present + absent + leave
    val percent: Int get() = if (marked == 0) 0 else (present * 100) / marked
    val latePercent: Int get() = if (marked == 0) 0 else (late * 100) / marked
}
