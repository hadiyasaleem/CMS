package com.mbd.cmscommon.util

import java.io.ByteArrayInputStream
import java.util.Locale
import java.util.zip.ZipInputStream
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

data class StudentImportResult(
    val rows: List<ImportedStudentRow>,
    val errors: List<String>,
)

object StudentImportParser {

    fun parseCsv(text: String): StudentImportResult = fromTable(parseCsvTable(text))

    fun parseXlsx(bytes: ByteArray): StudentImportResult = fromTable(parseXlsxTable(bytes))

    private fun fromTable(table: List<List<String>>): StudentImportResult {
        if (table.isEmpty()) {
            return StudentImportResult(emptyList(), listOf("The file is empty."))
        }

        val header = table.first().map { it.trim().lowercase(Locale.ROOT) }
        val rollCol = header.indexOfFirst { it.contains("roll") }
        val nameCol = header.indexOfFirst { it.contains("name") }
        val hasHeader = rollCol >= 0 && nameCol >= 0
        val actualRollCol = if (hasHeader) rollCol else 0
        val actualNameCol = if (hasHeader) nameCol else 1
        val dataStart = if (hasHeader) 1 else 0

        val errors = mutableListOf<String>()
        val seenRolls = mutableSetOf<String>()
        val rows = mutableListOf<ImportedStudentRow>()

        for (i in dataStart until table.size) {
            val line = table[i]
            if (line.all { it.isBlank() }) continue

            val displayRow = i + 1
            val roll = line.getOrNull(actualRollCol)?.trim() ?: ""
            val name = line.getOrNull(actualNameCol)?.trim() ?: ""

            if (roll.isBlank() || name.isBlank()) {
                errors += "Row $displayRow: missing roll number or name — skipped."
            } else if (!seenRolls.add(roll.lowercase(Locale.ROOT))) {
                errors += "Row $displayRow: duplicate roll '$roll' in file — skipped."
            } else {
                rows += ImportedStudentRow(displayRow, roll, name)
            }
        }

        return StudentImportResult(rows, errors)
    }

    private fun parseCsvTable(text: String): List<List<String>> =
        text.lines().filter { it.isNotBlank() }.map { parseCsvLine(it) }

    private fun parseCsvLine(line: String): List<String> {
        val fields = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (inQuotes && c == '"' && i + 1 < line.length && line[i + 1] == '"') {
                sb.append('"')
                i++
            } else if (c == '"') {
                inQuotes = !inQuotes
            } else if (c != ',' || inQuotes) {
                sb.append(c)
            } else {
                fields += sb.toString().trim()
                sb.clear()
            }
            i++
        }
        fields += sb.toString().trim()
        return fields
    }

    private fun parseXlsxTable(bytes: ByteArray): List<List<String>> {
        val sharedStrings = readZipEntry(bytes, "xl/sharedStrings.xml")?.let { parseSharedStrings(it) } ?: emptyList()
        val sheetBytes = readZipEntry(bytes, "xl/worksheets/sheet1.xml")
            ?: throw IllegalArgumentException("Couldn't find a worksheet in this Excel file.")
        return parseSheetRows(sheetBytes, sharedStrings)
    }

    private fun readZipEntry(bytes: ByteArray, entryName: String): ByteArray? {
        ZipInputStream(ByteArrayInputStream(bytes)).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                if (entry.name == entryName) {
                    return zis.readBytes()
                }
                entry = zis.nextEntry
            }
            return null
        }
    }

    private fun parseSharedStrings(xmlBytes: ByteArray): List<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(xmlBytes))
        val siNodes = doc.getElementsByTagName("si")
        return (0 until siNodes.length).map { i ->
            val tNodes = (siNodes.item(i) as Element).getElementsByTagName("t")
            (0 until tNodes.length).joinToString("") { tNodes.item(it).textContent }
        }
    }

    private fun parseSheetRows(xmlBytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(xmlBytes))
        val rowNodes = doc.getElementsByTagName("row")
        val rows = mutableListOf<List<String>>()
        for (i in 0 until rowNodes.length) {
            val cellNodes = (rowNodes.item(i) as Element).getElementsByTagName("c")
            val row = mutableListOf<String>()
            for (j in 0 until cellNodes.length) {
                val cell = cellNodes.item(j) as Element
                val ref = cell.getAttribute("r")
                val col = if (ref.isNotBlank()) colIndexOf(ref) else j
                val value = cellValue(cell, sharedStrings)
                while (row.size <= col) row.add("")
                row[col] = value
            }
            rows += row
        }
        return rows
    }

    private fun cellValue(cell: Element, sharedStrings: List<String>): String {
        return when (cell.getAttribute("t")) {
            "s" -> {
                val v = cell.getElementsByTagName("v").item(0)?.textContent?.toIntOrNull()
                v?.let { sharedStrings.getOrNull(it) } ?: ""
            }
            "inlineStr" -> cell.getElementsByTagName("t").item(0)?.textContent ?: ""
            else -> cell.getElementsByTagName("v").item(0)?.textContent ?: ""
        }
    }

    private fun colIndexOf(cellRef: String): Int {
        var idx = 0
        for (c in cellRef) {
            if (!c.isLetter()) break
            idx = (idx * 26) + (c.uppercaseChar() - 'A') + 1
        }
        return idx - 1
    }
}
