package com.mbd.cmscommon.export

enum class ExportFormat(val label: String, val extension: String, val mimeType: String) {
    EXCEL("Excel (.xlsx)", "xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
    PDF("PDF", "pdf", "application/pdf"),
}

/**
 * Relative column widths for a PDF table, from the longest value in each column (clamped so a long
 * name can't starve the other columns and a one-letter column stays readable).
 */
fun pdfColumnWeights(section: ExportSection): List<Float> {
    val columns = (listOf(section.header.size) + section.rows.map { it.size }).maxOrNull() ?: 0
    return (0 until columns).map { c ->
        val longest = (listOf(section.header.getOrNull(c)) + section.rows.map { it.getOrNull(c) }).maxOf { it?.length ?: 0 }
        longest.coerceIn(3, 36).toFloat()
    }
}
