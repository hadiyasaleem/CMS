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
import com.mbd.cmscommon.export.TimetableGridLayout
import com.mbd.cmscommon.export.timetableGridCellLines
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
    /** How far [DocumentExporter.writePdf]'s grid renderer will shrink font/row-height to keep a whole
     * printed timetable grid on one page before giving up and letting it spill onto a second page. */
    private const val MIN_GRID_SCALE = 0.4f

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

    /** Greedy word-wrap of [text] into lines no wider than [maxWidth] at [size]pt in [font]. A single
     * word wider than [maxWidth] is kept whole (overflowing slightly) rather than broken mid-word.
     * Beyond [maxLines] lines, the remaining words are folded into one final "..."-truncated line
     * instead of growing forever -- this is what keeps one verbose subject/teacher name from blowing
     * up an entire grid row's height (and, transitively, forcing the whole grid off one page). */
    private fun wrapPdfText(text: String, font: PDFont, size: Float, maxWidth: Float, maxLines: Int = Int.MAX_VALUE): List<String> {
        val safe = pdfSafe(text, font)
        if (safe.isBlank()) return emptyList()
        fun width(s: String) = font.getStringWidth(s) / 1000 * size
        val lines = mutableListOf<String>()
        var current = StringBuilder()
        safe.split(' ').forEach { word ->
            val candidate = if (current.isEmpty()) word else "$current $word"
            if (current.isEmpty() || width(candidate) <= maxWidth) {
                current = StringBuilder(candidate)
            } else {
                lines += current.toString()
                current = StringBuilder(word)
            }
        }
        if (current.isNotEmpty()) lines += current.toString()
        if (lines.size <= maxLines || maxLines <= 0) return lines
        val kept = lines.take(maxLines - 1).toMutableList()
        kept += fit(lines.drop(maxLines - 1).joinToString(" "), font, size, maxWidth)
        return kept
    }

    internal fun writePdf(doc: ExportDocument, target: File) {
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
                if (doc.title.isNotEmpty()) {
                    text(pdfSafe(doc.title.first(), bold), MARGIN, y, bold, 14f)
                    y += 15
                    doc.title.drop(1).forEach { text(pdfSafe(it, regular), MARGIN, y, regular, 9f); y += 12 }
                    y += 6
                }
            }

            fun drawRow(cells: List<String>, widths: List<Float>, header: Boolean, black: Set<Int> = emptySet()) {
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
                    if (i in black) {
                        s.setNonStrokingColor(0f, 0f, 0f)
                        s.addRect(x, PAGE_H - (y + ROW_H), w, ROW_H)
                        s.fill()
                        s.setStrokingColor(0.47f, 0.47f, 0.47f)
                    }
                    s.addRect(x, PAGE_H - (y + ROW_H), w, ROW_H)
                    s.stroke()
                    val font = if (header) bold else regular
                    if (i !in black) text(fit(cells.getOrNull(i).orEmpty(), font, 8f, w - 6f), x + 3f, y + ROW_H - 5f, font, 8f, white = header)
                    x += w
                }
                y += ROW_H
            }

            /** Centers [text] at [size]pt in [font] within a box, drawing lines evenly spaced (each
             * [lineH] apart -- callers whose box height was computed from a line count MUST pass the
             * exact same [lineH] used for that math, or the text block silently overflows the box) and
             * vertically centered inside [boxTopY]..[boxTopY]+[boxH]. */
            fun drawCenteredLines(lines: List<Pair<String, Boolean>>, boxX: Float, boxW: Float, boxTopY: Float, boxH: Float, size: Float, lineH: Float = size + 4f, white: Boolean = false) {
                if (lines.isEmpty()) return
                var ly = boxTopY + (boxH - lines.size * lineH) / 2f + size
                lines.forEach { (value, isBold) ->
                    val font = if (isBold) bold else regular
                    val w = font.getStringWidth(pdfSafe(value, font)) / 1000 * size
                    text(value, (boxX + (boxW - w) / 2f).coerceAtLeast(boxX + 3f), ly, font, size, white = white)
                    ly += lineH
                }
            }

            /** Wraps each (text, bold) entry to [maxWidth], flattening into the final drawn lines. */
            fun wrapEntries(entries: List<Pair<String, Boolean>>, size: Float, maxWidth: Float, maxLinesPerEntry: Int = Int.MAX_VALUE): List<Pair<String, Boolean>> =
                entries.flatMap { (value, isBold) -> wrapPdfText(value, if (isBold) bold else regular, size, maxWidth, maxLinesPerEntry).map { it to isBold } }

            /** Printed-timetable-style grid: centered multi-line title, a two-line period header, and
             * one merged department cell per block spanning all of its day-split sub-rows. Shrinks its
             * own font/row-height uniformly (down to [MIN_GRID_SCALE]) so the whole grid fits on one
             * page like a printed timetable -- only a genuinely oversized grid still spills onto a
             * second page, as a last-resort fallback rather than the common case. */
            fun drawTimetableGrid(layout: TimetableGridLayout) {
                val daysHeader = layout.secondColumnHeader
                val deptW = 100f
                val daysW = if (daysHeader != null) 78f else 0f
                val slotW = ((usable - deptW - daysW) / layout.columns.size.coerceAtLeast(1)).coerceAtLeast(64f)

                data class RowLayout(val daysLines: List<Pair<String, Boolean>>, val cellLines: List<List<Pair<String, Boolean>>>, val rowH: Float)
                data class BlockLayout(val deptLines: List<Pair<String, Boolean>>, val rows: List<RowLayout>, val blockHeight: Float)
                data class Measurement(val titleSizes: List<Float>, val cellSize: Float, val headerH: Float, val deptFontSize: Float, val blocks: List<BlockLayout>, val totalHeight: Float)

                fun measure(scale: Float): Measurement {
                    val titleSizes = listOf(15f, 14f, 11f, 11f).map { it * scale }
                    val cellSize = 8.5f * scale
                    val lineH = cellSize + 4.5f * scale
                    val headerH = 32f * scale
                    val deptFontSize = 9.5f * scale
                    val rowPad = 8f * scale

                    val blocks = layout.blocks.map { block ->
                        val deptLines = wrapEntries(block.deptLines.mapIndexed { i, v -> v to (i == 0) }, deptFontSize, deptW - 8f)
                        val rows = block.subRows.map { subRow ->
                            val daysLines = if (daysHeader != null) wrapEntries(listOf(subRow.daysLabel to false), cellSize, daysW - 8f) else emptyList()
                            val cellLines = layout.columns.indices.map { i -> wrapEntries(timetableGridCellLines(subRow.cells[i]), cellSize, slotW - 8f, maxLinesPerEntry = 2) }
                            val maxLines = (listOf(daysLines.size) + cellLines.map { it.size }).maxOrNull() ?: 0
                            RowLayout(daysLines, cellLines, maxOf(3, maxLines) * lineH + rowPad)
                        }
                        BlockLayout(deptLines, rows, rows.sumOf { it.rowH.toDouble() }.toFloat())
                    }
                    val titleHeight = titleSizes.sumOf { (it + 5f * scale).toDouble() }.toFloat() + 6f * scale
                    val total = titleHeight + headerH + blocks.sumOf { it.blockHeight.toDouble() }.toFloat()
                    return Measurement(titleSizes, cellSize, headerH, deptFontSize, blocks, total)
                }

                // Remaining room on the current page, not a fixed one-page assumption -- drawTimetableGrid
                // may start a little below the top margin (e.g. after newPage()'s own leading offset).
                val budget = (PAGE_H - MARGIN) - y
                val at1 = measure(1f)
                // Every term in measure() scales linearly with `scale` for a *fixed* line-wrap outcome, so
                // the exact fraction of page 1 needed is budget/total -- and shrinking the font can only
                // wrap text into fewer lines, never more, so this is never an underestimate.
                var scale = if (at1.totalHeight > budget) (budget / at1.totalHeight).coerceIn(MIN_GRID_SCALE, 1f) else 1f
                var measured = if (scale >= 1f) at1 else measure(scale)
                while (measured.totalHeight > budget && scale > MIN_GRID_SCALE) {
                    scale = (scale - 0.03f).coerceAtLeast(MIN_GRID_SCALE)
                    measured = measure(scale)
                }
                val (titleSizes, cellSize, headerH, deptFontSize, blocks) = measured
                val gridLineH = cellSize + 4.5f * scale
                val deptLineH = deptFontSize + 4.5f * scale

                titleSizes.forEachIndexed { i, size ->
                    val safe = pdfSafe(layout.titleLines.getOrElse(i) { "" }, bold)
                    if (safe.isBlank()) return@forEachIndexed
                    val w = bold.getStringWidth(safe) / 1000 * size
                    text(safe, MARGIN + (usable - w) / 2f, y + size, bold, size)
                    y += size + 5f * scale
                }
                y += 6f * scale

                fun headerCell(x: Float, w: Float, lines: List<Pair<String, Boolean>>) {
                    val s = stream!!
                    s.setNonStrokingColor(0f, 0f, 0f)
                    s.addRect(x, PAGE_H - (y + headerH), w, headerH)
                    s.fill()
                    s.setStrokingColor(1f, 1f, 1f)
                    s.setLineWidth(0.6f)
                    s.addRect(x, PAGE_H - (y + headerH), w, headerH)
                    s.stroke()
                    drawCenteredLines(lines, x, w, y, headerH, cellSize, gridLineH, white = true)
                }

                fun drawGridHeader() {
                    var x = MARGIN
                    headerCell(x, deptW, listOf("Departments" to true))
                    x += deptW
                    if (daysHeader != null) {
                        headerCell(x, daysW, listOf(daysHeader to true))
                        x += daysW
                    }
                    layout.columns.forEach { col ->
                        headerCell(x, slotW, listOf(col.index to true, col.timeLabel to false))
                        x += slotW
                    }
                    y += headerH
                }

                if (y + headerH * 3 > PAGE_H - MARGIN) newPage()
                drawGridHeader()

                blocks.forEach { block ->
                    val blockHeight = block.blockHeight
                    if (y + blockHeight > PAGE_H - MARGIN && blockHeight <= PAGE_H - 2 * MARGIN - headerH) {
                        newPage()
                        drawGridHeader()
                    }
                    val blockTopY = y
                    val s = stream!!
                    block.rows.forEachIndexed { subIdx, row ->
                        if (y + row.rowH > PAGE_H - MARGIN) { newPage(); drawGridHeader() }
                        var x = MARGIN + deptW
                        s.setStrokingColor(0.3f, 0.3f, 0.3f)
                        s.setLineWidth(0.8f)
                        if (daysHeader != null) {
                            s.addRect(x, PAGE_H - (y + row.rowH), daysW, row.rowH); s.stroke()
                            drawCenteredLines(row.daysLines, x, daysW, y, row.rowH, cellSize, gridLineH)
                            x += daysW
                        }
                        row.cellLines.forEach { lines ->
                            s.addRect(x, PAGE_H - (y + row.rowH), slotW, row.rowH); s.stroke()
                            drawCenteredLines(lines, x, slotW, y, row.rowH, cellSize, gridLineH)
                            x += slotW
                        }
                        y += row.rowH
                        if (subIdx == block.rows.lastIndex) {
                            s.setLineWidth(1.2f)
                            s.addRect(MARGIN, PAGE_H - (blockTopY + blockHeight), deptW, blockHeight)
                            s.stroke()
                            drawCenteredLines(block.deptLines, MARGIN, deptW, blockTopY, blockHeight, deptFontSize, deptLineH)
                        }
                    }
                }
                y += 14f * scale
            }

            fun drawSection(section: ExportSection, showName: Boolean) {
                val weights = pdfColumnWeights(section).ifEmpty { listOf(1f) }
                val widths = weights.map { it / weights.sum() * usable }
                if (y + ROW_H * 3 > PAGE_H - MARGIN) newPage()
                if (showName) {
                    text(pdfSafe(section.name, bold), MARGIN, y + 11f, bold, 11f)
                    y += 16f
                }
                if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true, black = section.blackColumns)
                section.rows.forEach { row ->
                    if (y + ROW_H > PAGE_H - MARGIN) {
                        newPage()
                        if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true, black = section.blackColumns)
                    }
                    drawRow(row, widths, header = false, black = section.blackColumns)
                }
                y += 14f
            }

            newPage()
            doc.sections.forEachIndexed { index, section ->
                val grid = section.grid
                if (grid != null) {
                    if (index > 0) newPage()
                    drawTimetableGrid(grid)
                } else {
                    drawSection(section, showName = doc.sections.size > 1)
                }
            }
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
