package com.mbd.cmscommon.domain.model

import java.time.LocalDate

enum class RecordsSummarySource {
    SESSIONS,
    CALENDAR,
    DATESHEETS,
    DOCUMENTS,
    INSIGHTS,
}

data class RecordsHubSnapshot(
    val activeSessions: Int,
    val upcomingEvents: Int,
    val publishedDatesheets: Int,
    val draftDatesheets: Int,
    val publishedDocuments: Int,
    val draftDocuments: Int,
    val atRiskStudents: Int,
    val unavailableSources: Set<RecordsSummarySource> = emptySet(),
) {
    val publishedResources: Int get() = publishedDatesheets + publishedDocuments
    val draftResources: Int get() = draftDatesheets + draftDocuments
}

fun recordsHubSnapshot(
    sessions: List<AcademicSession>,
    events: List<CalendarEvent>,
    datesheets: List<Datesheet>,
    documents: List<Document>,
    atRiskStudents: List<AtRiskStudent>,
    today: LocalDate,
    unavailableSources: Set<RecordsSummarySource> = emptySet(),
): RecordsHubSnapshot {
    val upcomingEvents = events.count { event ->
        val endDate = runCatching { LocalDate.parse(event.endDate ?: event.startDate) }.getOrNull()
        endDate != null && !endDate.isBefore(today)
    }

    return RecordsHubSnapshot(
        activeSessions = sessions.count { it.isActive && it.archivedAt == null },
        upcomingEvents = upcomingEvents,
        publishedDatesheets = datesheets.count { it.published },
        draftDatesheets = datesheets.count { !it.published },
        publishedDocuments = documents.count { it.published },
        draftDocuments = documents.count { !it.published },
        atRiskStudents = atRiskStudents.distinctBy { it.sessionId to it.rollNumber }.size,
        unavailableSources = unavailableSources,
    )
}
