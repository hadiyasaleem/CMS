package com.mbd.cmscommon.export

/** A report ready to be written as Excel (one sheet per section) or PDF (sections one after another). */
data class ExportDocument(
    val fileBase: String,
    val title: List<String>,
    val sections: List<ExportSection>,
) {
    val isEmpty: Boolean get() = sections.all { it.rows.isEmpty() }
}

data class ExportSection(
    val name: String,
    val header: List<String>,
    val rows: List<List<String>>,
)

fun singleSectionDocument(fileBase: String, title: List<String>, header: List<String>, rows: List<List<String>>) =
    ExportDocument(fileBase, title, listOf(ExportSection(title.firstOrNull() ?: "Report", header, rows)))

/** Filesystem-safe base name ("IT 2024 / Morning" -> "IT_2024_Morning"). */
fun safeFileBase(raw: String): String =
    raw.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('_').ifEmpty { "report" }.take(80)
