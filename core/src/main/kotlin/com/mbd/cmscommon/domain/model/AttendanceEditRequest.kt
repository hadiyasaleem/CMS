package com.mbd.cmscommon.domain.model

import java.time.Instant
import java.time.LocalDate

/** A teacher-requested correction to one attendance cell; [currentStatus] is null when no mark existed. */
data class AttendanceEditRequest(
    val id: String,
    val sessionId: String,
    val semester: Int,
    val courseCode: String,
    val date: LocalDate,
    val rollNumber: String,
    val currentStatus: AttendanceStatus?,
    val currentIsLate: Boolean?,
    val requestedStatus: AttendanceStatus,
    val requestedIsLate: Boolean,
    val reason: String?,
    val status: MarkEditStatus,
    val requestedBy: String?,
    val reviewedBy: String?,
    val requestedAt: Instant,
    val reviewedAt: Instant?,
)

fun attendanceEditReviewIssues(request: AttendanceEditRequest): List<String> = buildList {
    if (request.id.isBlank()) add("The request has no database ID and cannot be reviewed safely.")
    if (request.sessionId.isBlank() || request.courseCode.isBlank() || request.rollNumber.isBlank()) {
        add("The request is missing its session, subject or roll number.")
    }
    if (request.requestedBy.isNullOrBlank()) add("The requesting teacher could not be identified.")
    if (request.currentStatus == request.requestedStatus && request.currentIsLate == request.requestedIsLate) {
        add("The requested attendance is unchanged. Reject this duplicate request.")
    }
}
