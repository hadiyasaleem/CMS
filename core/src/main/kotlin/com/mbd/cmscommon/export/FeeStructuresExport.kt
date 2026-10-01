package com.mbd.cmscommon.export

import com.mbd.cmscommon.controller.FeeRow
import com.mbd.cmscommon.controller.FeeSource
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import java.time.LocalDate
import java.util.Locale

private fun amount(value: Double): String = "%,.0f".format(Locale.ENGLISH, value)

private fun cadenceLabel(type: FeeType) = if (type == FeeType.ANNUAL) "Annual" else "Per semester"

private fun sourceLabel(source: FeeSource) = when (source) {
    FeeSource.CUSTOM -> "Own structure"
    FeeSource.COLLEGE -> "College base"
    FeeSource.NONE -> "Not set"
}

/**
 * The fee overview as a document: the college-wide base for each shift, then one row per class with a column per
 * fee head (the heads any class has, in the order they first appear), its total and where it comes from.
 */
fun feeStructuresExport(base: List<SessionFeeStructure>, rows: List<FeeRow>): ExportDocument {
    val baseSection = ExportSection(
        "College-wide base",
        listOf("Shift", "Plan", "Fee head", "Amount (PKR)"),
        base.sortedBy { it.shift }.flatMap { fee ->
            fee.heads.map { listOf(fee.shift.name.lowercase().replaceFirstChar { c -> c.uppercase() }, cadenceLabel(fee.cadence), it.label, amount(it.amount)) } +
                listOf(listOf(fee.shift.name.lowercase().replaceFirstChar { c -> c.uppercase() }, cadenceLabel(fee.cadence), "Total", amount(fee.totalAmount)))
        },
    )

    val headLabels = rows.flatMap { row -> row.structure?.heads?.map { it.label }.orEmpty() }.distinct()
    val gridHeader = listOf("Department", "Session", "Semester", "Program", "Shift", "Plan") + headLabels + listOf("Total", "Source")
    val gridRows = rows.map { row ->
        val fee = row.structure
        val byLabel = fee?.heads?.associate { it.label to it.amount }.orEmpty()
        listOf(
            row.departmentName,
            row.session.label,
            row.session.currentSemester.toString(),
            row.session.programType.label,
            row.shift.name.lowercase().replaceFirstChar { it.uppercase() },
            fee?.let { cadenceLabel(it.cadence) } ?: "-",
        ) + headLabels.map { label -> byLabel[label]?.let(::amount) ?: "" } +
            listOf(fee?.let { amount(it.totalAmount) } ?: "-", sourceLabel(row.source))
    }

    return ExportDocument(
        fileBase = "fee_structures",
        title = listOf("Fee Structures", "Generated ${LocalDate.now()}"),
        sections = listOfNotNull(
            baseSection.takeIf { it.rows.isNotEmpty() },
            ExportSection("Fee structure by class", gridHeader, gridRows),
        ),
    )
}
