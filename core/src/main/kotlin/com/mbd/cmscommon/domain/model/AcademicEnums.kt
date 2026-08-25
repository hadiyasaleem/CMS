package com.mbd.cmscommon.domain.model

import java.time.Instant

enum class Session {
    MORNING,
    EVENING,
}

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE,
}

enum class ExamType(val maxMarks: Int) {
    MIDTERM(25),
    SESSIONAL(15),
}

enum class FeeType {
    ANNUAL,
    SEMESTER,
}

data class FeeHead(
    val label: String,
    val amount: Double,
    override val entityId: Long = 0L,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
