package com.mbd.cmscommon.domain.model

import java.time.LocalDate

data class StudentMoreSnapshot(
    val upcomingEvents: Int,
    val nextEvent: CalendarEvent?,
    val feeConfigured: Boolean,
    val feeTotal: Double?,
    val feeDueDate: LocalDate?,
    val unreadNotifications: Int,
    val profileCompletion: Int,
    val missingProfileFields: List<String>,
)

fun studentMoreSnapshot(
    events: List<CalendarEvent>,
    fee: SessionFeeStructure?,
    unreadNotifications: Int,
    profile: StudentProfile?,
    viewer: CalendarViewerContext,
    today: LocalDate,
): StudentMoreSnapshot {
    val visibleEvents = events
        .filter { isVisibleTo(it, viewer) && isUpcomingOn(it, today) }
        .distinctBy { it.id }
    val nextEvent = visibleEvents.minByOrNull { startDateOrNull(it) ?: LocalDate.MAX }

    val required = listOf(
        "Name" to profile?.name,
        "Father or guardian" to (profile?.fatherName ?: profile?.guardianName),
        "Identity number" to profile?.cnicBform,
        "Date of birth" to profile?.dob,
        "Phone" to profile?.phone,
        "Personal email" to profile?.personalEmail,
        "Current address" to profile?.currentAddress,
        "Emergency contact" to profile?.emergencyContactPhone,
    )
    val missing = required.filter { it.second.isNullOrBlank() }.map { it.first }
    val completion = (((required.size - missing.size) * 100) / required.size).coerceIn(0, 100)

    return StudentMoreSnapshot(
        upcomingEvents = visibleEvents.size,
        nextEvent = nextEvent,
        feeConfigured = fee != null,
        feeTotal = fee?.totalAmount,
        feeDueDate = fee?.dueDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        unreadNotifications = unreadNotifications.coerceAtLeast(0),
        profileCompletion = completion,
        missingProfileFields = missing,
    )
}
