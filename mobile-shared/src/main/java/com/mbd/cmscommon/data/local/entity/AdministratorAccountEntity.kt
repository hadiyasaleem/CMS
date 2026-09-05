package com.mbd.cmscommon.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "administrator_accounts",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["updatedAt"]),
    ],
)
data class AdministratorAccountEntity(
    @PrimaryKey val id: String,
    val email: String,
    val status: String,
    val lastLoginAt: Long?,
    val createdAt: Long = 0L,
    val createdBy: String? = null,
    val updatedAt: Long = 0L,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val deletedBy: String? = null,
)
