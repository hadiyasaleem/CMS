package com.mbd.cmscommon.export

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipInputStream
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class XlsxWriterTest {

    private fun unzip(bytes: ByteArray): Map<String, String> {
        val parts = mutableMapOf<String, String>()
        ZipInputStream(ByteArrayInputStream(bytes)).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                parts[entry.name] = zip.readBytes().toString(Charsets.UTF_8)
                entry = zip.nextEntry
            }
        }
        return parts
    }

    @Test
    fun writesOneSheetPerSectionWithEscapedTextAndNumericCells() {
        val doc = ExportDocument(
            fileBase = "report",
            title = listOf("Attendance <Register>"),
            sections = listOf(
                ExportSection("Marks", listOf("Roll", "Name", "Score"), listOf(listOf("09", "Ali & Sons", "42"))),
                ExportSection("Marks", listOf("A"), listOf(listOf("1.5"))),
            ),
        )
        val parts = unzip(ByteArrayOutputStream().also { XlsxWriter.write(doc, it) }.toByteArray())

        assertTrue(parts.keys.containsAll(listOf("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/styles.xml", "xl/worksheets/sheet1.xml", "xl/worksheets/sheet2.xml")))
        val sheet1 = parts.getValue("xl/worksheets/sheet1.xml")
        assertTrue(sheet1.contains("Attendance &lt;Register&gt;"))
        assertTrue(sheet1.contains("Ali &amp; Sons"))
        assertTrue("roll with a leading zero stays text", sheet1.contains("<t xml:space=\"preserve\">09</t>"))
        assertTrue("plain integers become numbers", sheet1.contains("<v>42</v>"))
        assertTrue(parts.getValue("xl/worksheets/sheet2.xml").contains("<v>1.5</v>"))
        val workbook = parts.getValue("xl/workbook.xml")
        assertTrue(workbook.contains("name=\"Marks\"") && workbook.contains("name=\"Marks (2)\""))
    }

    @Test
    fun sheetNamesAreSanitisedTruncatedAndUnique() {
        val names = XlsxWriter.sheetNames(listOf("a/b:c", "A long name that is well over thirty-one characters", "", "a b c"))
        assertEquals("a b c", names[0])
        assertEquals(31, names[1].length)
        assertEquals("Sheet3", names[2])
        assertEquals("a b c (2)", names[3])
        names.forEach { assertFalse(it.contains("/")) }
    }

    @Test
    fun columnNamesRollOverPastZ() {
        assertEquals("A", XlsxWriter.columnName(0))
        assertEquals("Z", XlsxWriter.columnName(25))
        assertEquals("AA", XlsxWriter.columnName(26))
        assertEquals("AH", XlsxWriter.columnName(33))
    }
}
