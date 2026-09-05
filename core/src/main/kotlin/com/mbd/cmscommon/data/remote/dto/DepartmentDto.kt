package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DepartmentDto(
    val entityId: Long? = null,
    val deptId: String? = null,
    val name: String? = null,
    val code: String? = null,
    val hodEmail: String? = null,
    val description: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)
