package com.mbd.cmscommon.domain.model

import java.time.Instant

data class CalendarEvent(
    val id: String,
    val title: String,
    val eventType: String,
    val startDate: String,
    val endDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val description: String? = null,
    val venue: String? = null,
    val audience: String = "ALL",
    val deptId: String? = null,
    val sessionId: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
