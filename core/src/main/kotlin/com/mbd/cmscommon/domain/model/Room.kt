package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Room(
    val roomId: String,
    val buildingId: String,
    val roomNo: String,
    val name: String? = null,
    val capacity: Int? = null,
    val isOffice: Boolean = false,
    val isActive: Boolean = true,
    override val createdAt: Instant,
    override val createdBy: String,
    override val updatedAt: Instant,
    override val updatedBy: String,
) : BaseEntity()
