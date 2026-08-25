package com.mbd.cmscommon.domain.model

import java.util.Locale

enum class IdentityClaimField(val label: String) {
    NAME("Name"),
    CNIC("CNIC / B-Form"),
    DATE_OF_BIRTH("Date of birth"),
    UNIVERSITY_ROLL("University roll"),
    REGISTRATION("Registration"),
}

enum class IdentityClaimStatus {
    MATCHED,
    MISMATCHED,
    OFFICIAL_MISSING,
    NOT_CLAIMED,
}

data class IdentityClaimComparison(
    val field: IdentityClaimField,
    val claimedValue: String?,
    val officialValue: String?,
    val status: IdentityClaimStatus,
)

data class LinkIdentityVerification(
    val comparisons: List<IdentityClaimComparison>,
) {
    val matchedCount: Int get() = comparisons.count { it.status == IdentityClaimStatus.MATCHED }
    val mismatches: List<IdentityClaimComparison> get() = comparisons.filter { it.status == IdentityClaimStatus.MISMATCHED }
    val officialMissingCount: Int get() = comparisons.count { it.status == IdentityClaimStatus.OFFICIAL_MISSING }
    val claimedCount: Int get() = comparisons.count { it.status != IdentityClaimStatus.NOT_CLAIMED }
    val hasMismatch: Boolean get() = mismatches.isNotEmpty()
}

fun verifyLinkIdentityClaims(request: StudentLinkRequest, profile: StudentProfile): LinkIdentityVerification =
    LinkIdentityVerification(
        listOf(
            compareClaim(IdentityClaimField.NAME, request.nameClaimed, profile.name, ::normalizeWords),
            compareClaim(IdentityClaimField.CNIC, request.cnicClaimed, profile.cnicBform, ::normalizeIdentifier),
            compareClaim(IdentityClaimField.DATE_OF_BIRTH, request.dobClaimed, profile.dob, ::normalizeDate),
            compareClaim(IdentityClaimField.UNIVERSITY_ROLL, request.universityRollClaimed, profile.universityRollNo, ::normalizeIdentifier),
            compareClaim(IdentityClaimField.REGISTRATION, request.registrationNoClaimed, profile.registrationNo, ::normalizeIdentifier),
        ),
    )

private fun compareClaim(
    field: IdentityClaimField,
    claimed: String?,
    official: String?,
    normalize: (String) -> String,
): IdentityClaimComparison {
    val claimedValue = claimed?.trim()?.takeIf { it.isNotBlank() }
    val officialValue = official?.trim()?.takeIf { it.isNotBlank() }
    val status = when {
        claimedValue == null -> IdentityClaimStatus.NOT_CLAIMED
        officialValue == null -> IdentityClaimStatus.OFFICIAL_MISSING
        normalize(claimedValue) == normalize(officialValue) -> IdentityClaimStatus.MATCHED
        else -> IdentityClaimStatus.MISMATCHED
    }
    return IdentityClaimComparison(field, claimedValue, officialValue, status)
}

private fun normalizeWords(value: String): String =
    value.lowercase(Locale.ROOT)
        .map { if (it.isLetterOrDigit()) it else ' ' }
        .joinToString("")
        .trim()
        .replace(Regex("\\s+"), " ")

private fun normalizeIdentifier(value: String): String =
    value.uppercase(Locale.ROOT).filter { it.isLetterOrDigit() }

private fun normalizeDate(value: String): String = value.trim()
