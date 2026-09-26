package com.mbd.cmscommon.domain.model

import java.time.Instant

/** A single shift. Students, timetable periods, fee structures and datesheets each belong to one. */
enum class Session {
    MORNING,
    EVENING;

    /** "Morning" / "Evening". */
    val label: String get() = name.lowercase().replaceFirstChar { it.uppercase() }

    /** "M" / "E", for compact class labels such as "IT-301 · 2022–2026 (E)". */
    val shortLabel: String get() = name.take(1)
}

/** Which shifts one academic session (intake) runs. Subjects, term dates and the semester pointer are shared. */
enum class ShiftMode {
    MORNING,
    EVENING,
    BOTH;

    val shifts: List<Session>
        get() = when (this) {
            MORNING -> listOf(Session.MORNING)
            EVENING -> listOf(Session.EVENING)
            BOTH -> listOf(Session.MORNING, Session.EVENING)
        }

    fun allows(shift: Session): Boolean = shift in shifts

    /** "Morning", "Evening" or "Morning & Evening". */
    val label: String get() = shifts.joinToString(" & ") { it.label }

    companion object {
        /** The mode for a set of ticked shifts, or null when none is ticked. */
        fun of(shifts: Collection<Session>): ShiftMode? = when {
            Session.MORNING in shifts && Session.EVENING in shifts -> BOTH
            Session.MORNING in shifts -> MORNING
            Session.EVENING in shifts -> EVENING
            else -> null
        }
    }
}

/** Parses a stored shift name; null for blank/unknown values. */
fun parseShift(raw: String?): Session? = Session.entries.firstOrNull { it.name == raw?.trim()?.uppercase() }

/** Parses a stored shift mode; null for blank/unknown values. */
fun parseShiftMode(raw: String?): ShiftMode? = ShiftMode.entries.firstOrNull { it.name == raw?.trim()?.uppercase() }

enum class AttendanceStatus {
    PRESENT,
    ABSENT,
    LEAVE,
}

enum class ExamType(val maxMarks: Int) {
    MIDTERM(25),
    SESSIONAL(15),
}

enum class FeeType {
    ANNUAL,
    SEMESTER,
}

data class FeeHead(
    val label: String,
    val amount: Double,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()
