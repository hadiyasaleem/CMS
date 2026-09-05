package com.mbd.cmscommon.domain.model

import java.time.LocalDate

data class LinkRequestClaimQuality(
    val percentage: Int,
    val issues: List<String>,
) {
    val isReviewable: Boolean get() = issues.isEmpty()
    val summary: String? get() = issues.takeIf { it.isNotEmpty() }?.joinToString(" ")
}

data class LinkRequestQueueSnapshot(
    val requests: List<StudentLinkRequest>,
    val duplicateCount: Int,
    val malformedCount: Int,
)

fun linkRequestQueueSnapshot(requests: List<StudentLinkRequest>): LinkRequestQueueSnapshot {
    val seenIds = mutableSetOf<String>()
    var duplicateCount = 0
    val normalized = requests.filter { request ->
        val id = request.requestId.trim()
        if (id.isBlank() || seenIds.add(id)) {
            true
        } else {
            duplicateCount++
            false
        }
    }
    val malformedCount = normalized.count { !linkRequestClaimQuality(it).isReviewable }
    return LinkRequestQueueSnapshot(normalized, duplicateCount, malformedCount)
}

fun linkRequestClaimQuality(request: StudentLinkRequest): LinkRequestClaimQuality {
    val checks = listOf(
        request.requestId.isNotBlank(),
        isValidAccountEmail(request.requestedByUid),
        !request.sessionIdClaimed.isNullOrBlank(),
        request.rollNumberClaimed.isNotBlank(),
        !request.nameClaimed.isNullOrBlank(),
        isValidCnic(request.cnicClaimed),
        isValidOptionalDate(request.dobClaimed),
    )

    val issues = mutableListOf<String>()
    if (request.requestId.isBlank()) issues += "The request has no database ID and cannot be reviewed safely."
    if (!isValidAccountEmail(request.requestedByUid)) issues += "The requested account email is missing or invalid."
    if (request.sessionIdClaimed.isNullOrBlank()) issues += "A session must be selected."
    if (request.rollNumberClaimed.isBlank()) issues += "A roll number is required."
    if (request.nameClaimed.isNullOrBlank()) issues += "The student's name is required."
    if (!isValidCnic(request.cnicClaimed)) issues += "CNIC / B-Form must contain 13 digits."
    if (!isValidOptionalDate(request.dobClaimed)) issues += "Date of birth must use yyyy-MM-dd."

    val passed = checks.count { it }
    return LinkRequestClaimQuality((passed * 100) / checks.size, issues)
}

fun linkRequestVerificationKey(request: StudentLinkRequest): String {
    val trimmed = request.requestId.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(
        request.requestedByUid, request.sessionIdClaimed,
        request.rollNumberClaimed, request.createdAt,
    ).joinToString("|")
}

private fun isValidAccountEmail(value: String): Boolean {
    val trimmed = value.trim()
    val separator = trimmed.indexOf('@')
    if (separator <= 0 || separator >= trimmed.lastIndex) return false
    val dot = trimmed.indexOf('.', separator)
    return separator + 2 <= dot && dot < trimmed.length
}

private fun isValidCnic(value: String?): Boolean {
    if (value == null) return false
    return value.filter { it.isDigit() }.length == 13
}

private fun isValidOptionalDate(value: String?): Boolean {
    if (value.isNullOrBlank()) return true
    return runCatching { LocalDate.parse(value.trim()) }.isSuccess
}
