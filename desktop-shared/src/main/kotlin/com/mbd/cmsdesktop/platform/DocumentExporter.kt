package com.mbd.cmsdesktop.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.awt.ComposeWindow
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.ExportSection
import com.mbd.cmscommon.export.XlsxWriter
import com.mbd.cmscommon.export.pdfColumnWeights
import com.mbd.cmscommon.export.safeFileBase
import com.mbd.cmscommon.ui.components.CmsErrorDialog
import com.mbd.cmscommon.util.userMessageLogged
import java.io.File
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts

/** Asks where to save an [ExportDocument], writes it as .xlsx or .pdf, then opens it. */
object DocumentExporter {

    /** Returns false when the user cancelled the save dialog. */
    fun export(window: ComposeWindow, doc: ExportDocument, format: ExportFormat): Boolean {
        val chosen = AwtDesktopPlatformServices.chooseSaveFile(window, "Export report", safeFileBase(doc.fileBase) + "." + format.extension)
            ?: return false
        val target = if (chosen.extension.equals(format.extension, ignoreCase = true)) chosen else File(chosen.path + "." + format.extension)
        when (format) {
            ExportFormat.EXCEL -> target.outputStream().use { XlsxWriter.write(doc, it) }
            ExportFormat.PDF -> writePdf(doc, target)
        }
        AwtDesktopPlatformServices.open(target)
        return true
    }

    private val regular = PDType1Font(Standard14Fonts.FontName.HELVETICA)
    private val bold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)

    private const val PAGE_W = 842f
    private const val PAGE_H = 595f
    private const val MARGIN = 28f
    private const val ROW_H = 16f

    /** Standard-14 fonts only cover WinAnsi; fold common typography and drop anything else. */
    internal fun pdfSafe(text: String, font: PDFont): String = buildString {
        text.forEach { ch ->
            val folded = when (ch) {
                '–', '—' -> "-"
                '·', '•' -> "-"
                '‹' -> "<"
                '›' -> ">"
                '\n', '\r', '\t' -> " "
                else -> ch.toString()
            }
            append(if (runCatching { font.encode(folded) }.isSuccess) folded else "?")
        }
    }

    private fun fit(text: String, font: PDFont, size: Float, width: Float): String {
        val safe = pdfSafe(text, font)
        if (font.getStringWidth(safe) / 1000 * size <= width) return safe
        var end = safe.length
        while (end > 0 && font.getStringWidth(safe.substring(0, end) + "..") / 1000 * size > width) end--
        return safe.substring(0, end) + ".."
    }

    private fun writePdf(doc: ExportDocument, target: File) {
        val usable = PAGE_W - 2 * MARGIN
        PDDocument().use { pdf ->
            var stream: PDPageContentStream? = null
            var y = 0f

            fun text(value: String, x: Float, baseline: Float, font: PDFont, size: Float, white: Boolean = false) {
                val s = stream!!
                s.beginText()
                s.setFont(font, size)
                if (white) s.setNonStrokingColor(1f, 1f, 1f)
                s.newLineAtOffset(x, PAGE_H - baseline)
                s.showText(value)
                s.endText()
                if (white) s.setNonStrokingColor(0f, 0f, 0f)
            }

            fun newPage() {
                stream?.close()
                val page = PDPage(PDRectangle(PAGE_W, PAGE_H))
                pdf.addPage(page)
                stream = PDPageContentStream(pdf, page)
                y = MARGIN + 14
                text(pdfSafe(doc.title.firstOrNull() ?: "Report", bold), MARGIN, y, bold, 14f)
                y += 15
                doc.title.drop(1).forEach { text(pdfSafe(it, regular), MARGIN, y, regular, 9f); y += 12 }
                y += 6
            }

            fun drawRow(cells: List<String>, widths: List<Float>, header: Boolean) {
                val s = stream!!
                if (header) {
                    s.setNonStrokingColor(0.12f, 0.12f, 0.12f)
                    s.addRect(MARGIN, PAGE_H - (y + ROW_H), widths.sum(), ROW_H)
                    s.fill()
                    s.setNonStrokingColor(0f, 0f, 0f)
                }
                s.setStrokingColor(0.47f, 0.47f, 0.47f)
                s.setLineWidth(0.6f)
                var x = MARGIN
                widths.forEachIndexed { i, w ->
                    s.addRect(x, PAGE_H - (y + ROW_H), w, ROW_H)
                    s.stroke()
                    val font = if (header) bold else regular
                    text(fit(cells.getOrNull(i).orEmpty(), font, 8f, w - 6f), x + 3f, y + ROW_H - 5f, font, 8f, white = header)
                    x += w
                }
                y += ROW_H
            }

            fun drawSection(section: ExportSection, showName: Boolean) {
                val weights = pdfColumnWeights(section).ifEmpty { listOf(1f) }
                val widths = weights.map { it / weights.sum() * usable }
                if (y + ROW_H * 3 > PAGE_H - MARGIN) newPage()
                if (showName) {
                    text(pdfSafe(section.name, bold), MARGIN, y + 11f, bold, 11f)
                    y += 16f
                }
                if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true)
                section.rows.forEach { row ->
                    if (y + ROW_H > PAGE_H - MARGIN) {
                        newPage()
                        if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true)
                    }
                    drawRow(row, widths, header = false)
                }
                y += 14f
            }

            newPage()
            doc.sections.forEach { drawSection(it, showName = doc.sections.size > 1) }
            stream?.close()
            pdf.save(target)
        }
    }
}

/** The app's main window, provided at each desktop app root so any screen can parent a save dialog. */
val LocalAppWindow = staticCompositionLocalOf<ComposeWindow> { error("LocalAppWindow not provided") }

/** Screen-level export hook: save dialog + write + open, with its own error dialog on failure. */
@Composable
fun rememberDocumentExport(window: ComposeWindow = LocalAppWindow.current): (ExportDocument, ExportFormat) -> Unit {
    var error by remember { mutableStateOf<String?>(null) }
    error?.let { CmsErrorDialog(message = it, onDismiss = { error = null }) }
    return remember(window) {
        { doc, format ->
            runCatching { DocumentExporter.export(window, doc, format) }
                .onFailure { error = it.userMessageLogged("DocumentExporter", "Could not export this report.") }
        }
    }
}
