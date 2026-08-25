package com.mbd.cmsdesktop.ui.admin

import com.mbd.cmsdesktop.platform.AwtDesktopPlatformServices
import java.io.File
import java.io.FileOutputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts

/**
 * CSV / PDF export helper shared by every desktop admin records screen (attendance reports,
 * roster exports, etc). Writes to [target], then hands the finished file to the OS via
 * [AwtDesktopPlatformServices.open] so the user's default viewer opens it.
 */
object RecordsExporter {

    fun exportCsv(target: File, title: List<String>, header: List<String>, rows: List<List<String>>) {
        val sb = StringBuilder()
        title.forEach { sb.append(csv(it)).append("\n") }
        if (title.isNotEmpty()) sb.append("\n")
        sb.append(header.joinToString(",") { csv(it) }).append("\n")
        rows.forEach { row -> sb.append(row.joinToString(",") { csv(it) }).append("\n") }

        val content = sb.toString()
            .replace('–', '-')
            .replace('·', '-')

        FileOutputStream(target).use { out ->
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        AwtDesktopPlatformServices.open(target)
    }

    fun exportPdf(target: File, title: List<String>, header: List<String>, rows: List<List<String>>) {
        val regular = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val bold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val margin = 28f
        val pageW = 842f
        val pageH = 595f
        val rowH = 16f
        val cols = header.size.coerceAtLeast(1)
        val usable = pageW - 2 * margin
        val firstTwo = minOf(cols, 2)
        val wideW = if (cols <= 2) usable / cols else (usable * 0.3f) / firstTwo
        val restW = if (cols <= 2) 0f else (usable * 0.7f) / (cols - firstTwo)

        fun width(i: Int) = if (i < 2) wideW else restW
        fun colX(i: Int): Float {
            var x = margin
            for (j in 0 until i) x += width(j)
            return x
        }

        fun text(stream: PDPageContentStream, str: String, x: Float, yTop: Float, font: PDFont, size: Float, white: Boolean = false) {
            stream.beginText()
            stream.setFont(font, size)
            if (white) stream.setNonStrokingColor(1f, 1f, 1f)
            stream.newLineAtOffset(x, pageH - yTop)
            stream.showText(str)
            stream.endText()
            if (white) stream.setNonStrokingColor(0f, 0f, 0f)
        }

        fun cellText(s: String, maxChars: Int) = if (s.length > maxChars) s.take(maxChars) else s

        fun drawRow(stream: PDPageContentStream, row: List<String>, top: Float, headerRow: Boolean): Float {
            val bottom = top + rowH
            if (headerRow) {
                stream.setNonStrokingColor(0.11764706f, 0.11764706f, 0.11764706f)
                stream.addRect(margin, pageH - bottom, usable, rowH)
                stream.fill()
            }
            stream.setStrokingColor(0.47058824f, 0.47058824f, 0.47058824f)
            stream.setLineWidth(0.6f)
            val baseline = top + rowH - 5f
            row.forEachIndexed { i, c ->
                val x = colX(i)
                stream.addRect(x, pageH - bottom, width(i), rowH)
                stream.stroke()
                val maxChars = if (i < 2) 24 else 6
                text(stream, cellText(c, maxChars), x + 3f, baseline, if (headerRow) bold else regular, 8f, headerRow)
            }
            return bottom
        }

        fun startBody(stream: PDPageContentStream): Float {
            var yy = margin + 14
            text(stream, title.firstOrNull() ?: "Report", margin, yy, bold, 14f)
            yy += 15
            title.drop(1).forEach { line ->
                text(stream, line, margin, yy, regular, 9f)
                yy += 12
            }
            return drawRow(stream, header, yy + 6, headerRow = true)
        }

        PDDocument().use { doc ->
            var page = PDPage(PDRectangle(pageW, pageH))
            doc.addPage(page)
            var stream = PDPageContentStream(doc, page)
            var y = startBody(stream)
            for (row in rows) {
                if (y + rowH > pageH - margin) {
                    stream.close()
                    page = PDPage(PDRectangle(pageW, pageH))
                    doc.addPage(page)
                    stream = PDPageContentStream(doc, page)
                    y = startBody(stream)
                }
                y = drawRow(stream, row, y, headerRow = false)
            }
            stream.close()
            doc.save(target)
        }
        AwtDesktopPlatformServices.open(target)
    }

    fun sanitize(name: String): String = Regex("[^A-Za-z0-9._-]").replace(name, "_")

    private fun csv(v: String): String =
        if (v.contains(',') || v.contains('"') || v.contains('\n')) "\"" + v.replace("\"", "\"\"") + "\"" else v
}
