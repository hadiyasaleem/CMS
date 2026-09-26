package com.mbd.cmsdesktop.platform

import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportSection
import java.io.File
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentExporterPdfTest {

    @Test
    fun writesMultiSectionPdfAcrossPagesWithTypographyFolded() {
        val rows = (1..60).map { listOf("IT-21-%02d".format(it), "Student $it · Morning", "P") }
        val doc = ExportDocument(
            fileBase = "test",
            title = listOf("Student Record", "Amina Sehar · IT–21–09", "‹ Semester 7 ›"),
            sections = listOf(
                ExportSection("Attendance", listOf("Roll", "Name", "Status"), rows),
                ExportSection("Results", listOf("Semester", "GPA"), listOf(listOf("7", "3.45"))),
            ),
        )
        val file = File.createTempFile("export-test", ".pdf")
        try {
            DocumentExporter.writePdf(doc, file)
            Loader.loadPDF(file).use { pdf ->
                assertTrue("60 rows should spill onto a second page", pdf.numberOfPages >= 2)
                val text = PDFTextStripper().getText(pdf)
                assertTrue(text.contains("Amina Sehar - IT-21-09"))
                assertTrue(text.contains("< Semester 7 >"))
                assertTrue(text.contains("Results"))
                assertTrue(text.contains("IT-21-60"))
                assertEquals(false, text.contains("?"))
            }
        } finally {
            file.delete()
        }
    }
}
