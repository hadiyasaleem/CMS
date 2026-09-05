package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Department(
    val deptId: String,
    val name: String,
    val code: String,
    val hodEmail: String? = null,
    val description: String? = null,
    override val entityId: Long = 0L,
    override val createdAt: Instant,
    override val createdBy: String,
    override val updatedAt: Instant,
    override val updatedBy: String,
) : BaseEntity()
