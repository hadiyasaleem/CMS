package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Datesheet(
    val id: String,
    val title: String,
    val examType: String? = null,
    val sessionId: String? = null,
    val published: Boolean = false,
    val instructions: String? = null,
    override val entityId: Long = 0L,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

data class DatesheetSlot(
    val id: String,
    val datesheetId: String,
    val examDate: String,
    val startTime: String? = null,
    val endTime: String? = null,
    val durationMinutes: Int? = null,
    val courseCode: String? = null,
    val subjectName: String? = null,
    val roomNo: String? = null,
    val building: String? = null,
    val invigilatorEmail: String? = null,
    override val entityId: Long = 0L,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
