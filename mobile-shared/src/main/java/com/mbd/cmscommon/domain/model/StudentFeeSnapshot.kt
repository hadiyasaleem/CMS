package com.mbd.cmscommon.domain.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class FeeDueState {
    NOT_SET,
    INVALID,
    UPCOMING,
    DUE_TODAY,
    OVERDUE,
}

data class StudentFeeSnapshot(
    val structure: SessionFeeStructure?,
    val dueDate: LocalDate?,
    val dueState: FeeDueState,
    val daysUntilDue: Long?,
    val itemCount: Int,
    val totalAmount: Double,
    val largestHead: FeeHead?,
)

fun studentFeeSnapshot(structure: SessionFeeStructure?, today: LocalDate): StudentFeeSnapshot {
    val rawDue = structure?.dueDate?.trim()?.takeIf { it.isNotEmpty() }
    val due = rawDue?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
    val days = due?.let { ChronoUnit.DAYS.between(today, it) }

    val dueState = when {
        rawDue == null -> FeeDueState.NOT_SET
        due == null -> FeeDueState.INVALID
        days!! < 0 -> FeeDueState.OVERDUE
        days == 0L -> FeeDueState.DUE_TODAY
        else -> FeeDueState.UPCOMING
    }

    val heads = (structure?.heads ?: emptyList()).filter { it.label.isNotBlank() && it.amount >= 0.0 }

    return StudentFeeSnapshot(
        structure = structure,
        dueDate = due,
        dueState = dueState,
        daysUntilDue = days,
        itemCount = heads.size,
        totalAmount = heads.sumOf { it.amount },
        largestHead = heads.maxByOrNull { it.amount },
    )
}
