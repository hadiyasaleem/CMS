package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Building(
    val buildingId: String,
    val name: String,
    val code: String? = null,
    val isActive: Boolean = true,
    override val createdAt: Instant,
    override val createdBy: String,
    override val updatedAt: Instant,
    override val updatedBy: String,
) : BaseEntity()
