package com.mbd.cmscommon.domain.model

import java.time.Duration
import java.time.Instant

data class AdministratorDirectorySnapshot(
    val accounts: List<AdministratorAccount>,
    val activeCount: Int,
    val unavailableCount: Int,
    val recentlyActiveCount: Int,
    val neverSignedInCount: Int,
)

fun administratorDirectorySnapshot(
    administrators: List<AdministratorAccount>,
    now: Instant = Instant.now(),
): AdministratorDirectorySnapshot {
    val accounts = administrators
        .map { it.copy(email = it.email.trim()) }
        .distinctBy { it.email.lowercase().ifBlank { it.id } }
        .sortedBy { it.email.lowercase() }

    val recentCutoff = now.minus(Duration.ofDays(30))
    val activeCount = accounts.count { it.status.equals("ACTIVE", ignoreCase = true) }
    val unavailableCount = accounts.count { !it.status.equals("ACTIVE", ignoreCase = true) }
    val recentlyActiveCount = accounts.count { account ->
        val lastLogin = account.lastLoginAt
        lastLogin != null && lastLogin.isAfter(recentCutoff) && !lastLogin.isAfter(now)
    }
    val neverSignedInCount = accounts.count { it.lastLoginAt == null }

    return AdministratorDirectorySnapshot(accounts, activeCount, unavailableCount, recentlyActiveCount, neverSignedInCount)
}
