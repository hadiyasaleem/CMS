package com.mbd.cmscommon.domain.model

data class MarkEditReviewQuality(
    val blockingIssues: List<String>,
    val warnings: List<String>,
) {
    val blocksApproval: Boolean get() = blockingIssues.isNotEmpty()
    val needsAttention: Boolean get() = blocksApproval || warnings.isNotEmpty()
    val primaryMessage: String? get() = (blockingIssues + warnings).firstOrNull()
}

data class MarkEditQueueSnapshot(
    val requests: List<MarkEditRequest>,
    val duplicateCount: Int,
    val blockedCount: Int,
    val warningCount: Int,
)

fun markEditQueueSnapshot(requests: List<MarkEditRequest>): MarkEditQueueSnapshot {
    val seenIds = mutableSetOf<String>()
    var duplicates = 0
    val normalized = requests.filter { request ->
        val id = request.id.trim()
        if (id.isBlank() || seenIds.add(id)) {
            true
        } else {
            duplicates++
            false
        }
    }
    val quality = normalized.map { markEditReviewQuality(it) }
    val blocked = quality.count { it.blocksApproval }
    val warnings = quality.count { !it.blocksApproval && it.warnings.isNotEmpty() }
    return MarkEditQueueSnapshot(normalized, duplicates, blocked, warnings)
}

fun markEditReviewQuality(request: MarkEditRequest): MarkEditReviewQuality {
    val blocking = mutableListOf<String>()
    if (request.id.isBlank()) blocking += "The request has no database ID and cannot be reviewed safely."
    if (request.sessionId.isBlank()) blocking += "The request has no academic session."
    if (request.semester <= 0) blocking += "The semester number is invalid."
    if (request.courseCode.isBlank()) blocking += "The course code is missing."
    if (request.rollNumber.isBlank()) blocking += "The student roll number is missing."
    if (request.requestedBy.isNullOrBlank()) blocking += "The requesting teacher could not be identified."

    val maxMarks = request.examType.maxMarks
    if (request.currentScore == null) {
        blocking += "The stored current score is missing. Verify the marks record before approval."
    } else if (request.currentScore !in 0..maxMarks) {
        blocking += "The stored current score is outside the 0-$maxMarks range."
    }
    if (request.requestedScore !in 0..maxMarks) {
        blocking += "The requested score is outside the allowed 0-$maxMarks range."
    }
    if (request.currentScore != null && request.currentScore == request.requestedScore) {
        blocking += "The requested score is unchanged. Reject this duplicate request."
    }

    val warnings = mutableListOf<String>()
    if (request.reason.isNullOrBlank()) {
        warnings += "No reason was supplied. Confirm the correction with the teacher."
    }

    return MarkEditReviewQuality(blocking, warnings)
}

fun markEditReviewKey(request: MarkEditRequest): String {
    val trimmed = request.id.trim()
    if (trimmed.isNotBlank()) return trimmed
    return listOf(
        request.sessionId, request.semester, request.courseCode,
        request.examType, request.rollNumber, request.requestedBy, request.requestedAt,
    ).joinToString("|")
}
