package com.mbd.cmscommon.domain.model

data class MarksEntrySummary(
    val totalStudents: Int,
    val lockedStudents: Int,
    val editableStudents: Int,
    val absentStudents: Int,
    val pendingRequests: Int,
    val invalidScores: Int,
    val savableEntries: Int,
    val classAverage: Double?,
)

fun scoreValidationMessage(raw: String, maxMarks: Int): String? {
    if (raw.isBlank()) return null
    val score = raw.trim().toIntOrNull() ?: return "Numbers only"
    if (score < 0) return "Can't be negative"
    if (score > maxMarks) return "Max is $maxMarks"
    return null
}

fun marksEntrySummary(
    roster: List<SessionStudent>,
    displayScores: Map<String, String>,
    lockedRolls: Set<String>,
    pendingRolls: Set<String>,
    absentRolls: Set<String>,
    savedAbsentRolls: Set<String>,
    maxMarks: Int,
): MarksEntrySummary {
    val rosterRolls = roster.map { it.rollNumber }.toSet()
    val locked = lockedRolls intersect rosterRolls
    val absent = (absentRolls + savedAbsentRolls) intersect rosterRolls
    val editable = rosterRolls - locked

    fun hasValidScore(roll: String): Boolean {
        val raw = displayScores[roll] ?: return false
        return raw.isNotBlank() && scoreValidationMessage(raw, maxMarks) == null
    }

    val invalid = editable.count { roll -> !absent.contains(roll) && hasValidScore(roll) }
    val savable = editable.count { roll -> absentRolls.contains(roll) || hasValidScore(roll) }

    val scores = rosterRolls.mapNotNull { roll ->
        if (absent.contains(roll)) return@mapNotNull null
        val score = displayScores[roll]?.trim()?.toIntOrNull() ?: return@mapNotNull null
        score.takeIf { it in 0..maxMarks }
    }

    return MarksEntrySummary(
        totalStudents = roster.size,
        lockedStudents = locked.size,
        editableStudents = editable.size,
        absentStudents = absent.size,
        pendingRequests = (pendingRolls intersect rosterRolls).size,
        invalidScores = invalid,
        savableEntries = savable,
        classAverage = scores.takeIf { it.isNotEmpty() }?.average(),
    )
}
