package com.mbd.cmscommon.domain.model

import java.time.Instant

abstract class BaseEntity(
    open val entityId: Long = 0L,
    open val createdAt: Instant = Instant.EPOCH,
    open val createdBy: String? = null,
    open val updatedAt: Instant = Instant.EPOCH,
    open val updatedBy: String? = null,
)
