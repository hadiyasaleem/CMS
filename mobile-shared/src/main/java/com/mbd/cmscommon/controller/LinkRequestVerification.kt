package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.IdentityClaimComparison

data class LinkRequestVerification(
    val state: RosterVerificationState,
    val linkedEmail: String? = null,
    val message: String? = null,
    val identityComparisons: List<IdentityClaimComparison> = emptyList(),
)
