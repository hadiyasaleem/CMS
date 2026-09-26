package com.mbd.cmscommon.export

import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Minimal Office Open XML (.xlsx) writer: inline strings, one sheet per [ExportSection], bold header
 * row, title rows above it. Written by hand so Android and desktop share it without pulling Apache POI.
 */
object XlsxWriter {

    private const val STYLE_TITLE = 1
    private const val STYLE_HEADER = 2
    private val NUMBER = Regex("^-?(0|[1-9]\\d{0,13})(\\.\\d+)?$")

    fun write(doc: ExportDocument, out: OutputStream) {
        val sections = doc.sections.ifEmpty { listOf(ExportSection("Report", emptyList(), emptyList())) }
        val names = sheetNames(sections.map { it.name })
        ZipOutputStream(out).use { zip ->
            zip.entry("[Content_Types].xml", contentTypes(sections.size))
            zip.entry("_rels/.rels", ROOT_RELS)
            zip.entry("xl/workbook.xml", workbook(names))
            zip.entry("xl/_rels/workbook.xml.rels", workbookRels(sections.size))
            zip.entry("xl/styles.xml", STYLES)
            sections.forEachIndexed { i, section -> zip.entry("xl/worksheets/sheet${i + 1}.xml", sheet(doc.title, section)) }
        }
    }

    private fun ZipOutputStream.entry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    /** Excel sheet names: <= 31 chars, no []:*?/\, unique (case-insensitive). */
    internal fun sheetNames(raw: List<String>): List<String> {
        val used = mutableSetOf<String>()
        return raw.mapIndexed { i, name ->
            val base = name.replace(Regex("[\\[\\]:*?/\\\\]"), " ").trim().ifEmpty { "Sheet${i + 1}" }.take(31)
            var candidate = base
            var n = 2
            while (!used.add(candidate.lowercase())) {
                val suffix = " ($n)"
                candidate = base.take(31 - suffix.length) + suffix
                n++
            }
            candidate
        }
    }

    private fun sheet(title: List<String>, section: ExportSection): String {
        val rows = mutableListOf<Pair<List<String>, Int>>()
        title.forEach { rows += listOf(it) to STYLE_TITLE }
        if (title.isNotEmpty()) rows += emptyList<String>() to 0
        if (section.header.isNotEmpty()) rows += section.header to STYLE_HEADER
        section.rows.forEach { rows += it to 0 }

        val columnCount = (listOf(section.header.size) + section.rows.map { it.size }).maxOrNull() ?: 0
        val widths = (0 until columnCount).map { c ->
            val longest = (listOf(section.header.getOrNull(c)) + section.rows.map { it.getOrNull(c) }).maxOf { it?.length ?: 0 }
            (longest + 2).coerceIn(6, 60)
        }

        return buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
            if (section.header.isNotEmpty()) {
                val headerRow = rows.indexOfFirst { it.second == STYLE_HEADER } + 1
                append("""<sheetViews><sheetView workbookViewId="0"><pane ySplit="$headerRow" topLeftCell="A${headerRow + 1}" activePane="bottomLeft" state="frozen"/></sheetView></sheetViews>""")
            }
            if (widths.isNotEmpty()) {
                append("<cols>")
                widths.forEachIndexed { i, w -> append("""<col min="${i + 1}" max="${i + 1}" width="$w" customWidth="1"/>""") }
                append("</cols>")
            }
            append("<sheetData>")
            rows.forEachIndexed { r, (cells, style) ->
                append("""<row r="${r + 1}">""")
                cells.forEachIndexed { c, value -> append(cell(columnName(c) + (r + 1), value, style)) }
                append("</row>")
            }
            append("</sheetData></worksheet>")
        }
    }

    private fun cell(ref: String, value: String, style: Int): String {
        val s = if (style != 0) """ s="$style"""" else ""
        return if (style == 0 && NUMBER.matches(value)) {
            """<c r="$ref"$s><v>$value</v></c>"""
        } else {
            """<c r="$ref"$s t="inlineStr"><is><t xml:space="preserve">${escape(value)}</t></is></c>"""
        }
    }

    internal fun columnName(index: Int): String {
        var n = index + 1
        val sb = StringBuilder()
        while (n > 0) {
            val rem = (n - 1) % 26
            sb.insert(0, 'A' + rem)
            n = (n - 1) / 26
        }
        return sb.toString()
    }

    private fun escape(value: String): String = buildString(value.length) {
        value.forEach { ch ->
            when {
                ch == '&' -> append("&amp;")
                ch == '<' -> append("&lt;")
                ch == '>' -> append("&gt;")
                ch == '"' -> append("&quot;")
                ch < ' ' && ch != '\t' && ch != '\n' && ch != '\r' -> Unit // not allowed in XML 1.0
                else -> append(ch)
            }
        }
    }

    private fun contentTypes(sheets: Int) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">""")
        append("""<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>""")
        append("""<Default Extension="xml" ContentType="application/xml"/>""")
        append("""<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>""")
        append("""<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>""")
        (1..sheets).forEach {
            append("""<Override PartName="/xl/worksheets/sheet$it.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>""")
        }
        append("</Types>")
    }

    private fun workbook(names: List<String>) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships"><sheets>""")
        names.forEachIndexed { i, name -> append("""<sheet name="${escape(name)}" sheetId="${i + 1}" r:id="rId${i + 1}"/>""") }
        append("</sheets></workbook>")
    }

    private fun workbookRels(sheets: Int) = buildString {
        append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
        (1..sheets).forEach {
            append("""<Relationship Id="rId$it" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet$it.xml"/>""")
        }
        append("""<Relationship Id="rId${sheets + 1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>""")
        append("</Relationships>")
    }

    private const val ROOT_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/></Relationships>"""

    // xf 0 = default, 1 = title (bold 13pt), 2 = header (bold, light grey fill, thin border).
    private const val STYLES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main"><fonts count="3"><font><sz val="11"/><name val="Calibri"/></font><font><b/><sz val="13"/><name val="Calibri"/></font><font><b/><sz val="11"/><name val="Calibri"/></font></fonts><fills count="3"><fill><patternFill patternType="none"/></fill><fill><patternFill patternType="gray125"/></fill><fill><patternFill patternType="solid"><fgColor rgb="FFE7E6E6"/><bgColor indexed="64"/></patternFill></fill></fills><borders count="2"><border><left/><right/><top/><bottom/><diagonal/></border><border><left style="thin"/><right style="thin"/><top style="thin"/><bottom style="thin"/><diagonal/></border></borders><cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs><cellXfs count="3"><xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/><xf numFmtId="0" fontId="1" fillId="0" borderId="0" xfId="0" applyFont="1"/><xf numFmtId="0" fontId="2" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1"/></cellXfs><cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles></styleSheet>"""
}
