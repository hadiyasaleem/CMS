package com.mbd.cmscommon.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.ExportSection
import com.mbd.cmscommon.export.TimetableGridLayout
import com.mbd.cmscommon.export.timetableGridCellLines
import com.mbd.cmscommon.export.XlsxWriter
import com.mbd.cmscommon.export.pdfColumnWeights
import com.mbd.cmscommon.export.safeFileBase
import com.mbd.cmscommon.ui.components.CmsErrorDialog
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Writes an [ExportDocument] to the app cache and hands it to the system share sheet. */
object DocumentExporter {

    /** Writes the file off the main thread, then opens the share sheet. */
    suspend fun export(context: Context, doc: ExportDocument, format: ExportFormat) {
        val file = withContext(Dispatchers.IO) { write(context, doc, format) }
        share(context, file, format.mimeType)
    }

    fun write(context: Context, doc: ExportDocument, format: ExportFormat): File {
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, safeFileBase(doc.fileBase) + "." + format.extension)
        when (format) {
            ExportFormat.EXCEL -> file.outputStream().use { XlsxWriter.write(doc, it) }
            ExportFormat.PDF -> writePdf(doc, file)
        }
        return file
    }

    private fun writePdf(doc: ExportDocument, file: File) {
        val pdf = PdfDocument()
        // Landscape A3, not A4 -- gives a wide grid (a full week's worth of departments and periods)
        // enough room to lay out at a readable size on one page instead of shrinking to
        // MIN_GRID_SCALE or spilling.
        val pageW = 1190
        val pageH = 842
        val margin = 28f
        val rowH = 16f
        // How far drawTimetableGrid will shrink font/row-height to keep a whole printed timetable
        // grid on one page before giving up and letting it spill onto a second page.
        val MIN_GRID_SCALE = 0.4f
        val usable = pageW - 2 * margin
        val titlePaint = Paint().apply { textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        val metaPaint = Paint().apply { textSize = 9f; isAntiAlias = true }
        val sectionPaint = Paint().apply { textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        val bodyPaint = Paint().apply { textSize = 8f; isAntiAlias = true }
        val headText = Paint().apply { textSize = 8f; isFakeBoldText = true; color = Color.WHITE; isAntiAlias = true }
        val gridPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.rgb(120, 120, 120) }
        val blackFill = Paint().apply { style = Paint.Style.FILL; color = Color.BLACK }
        val headerBg = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(30, 30, 30) }

        var pageNo = 0
        var page: PdfDocument.Page? = null
        var y = 0f

        fun newPage() {
            page?.let { pdf.finishPage(it) }
            pageNo++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
            y = margin + 14
            if (doc.title.isNotEmpty()) {
                val canvas = page!!.canvas
                canvas.drawText(doc.title.first(), margin, y, titlePaint)
                y += 15
                doc.title.drop(1).forEach { canvas.drawText(it, margin, y, metaPaint); y += 12 }
                y += 6
            }
        }

        /** Truncates [text] to fit [maxWidth] under [paint], appending "..." (mirrors the desktop
         * `fit()` helper's "..") when it doesn't fit as-is. */
        fun fitCanvasText(text: String, paint: Paint, maxWidth: Float): String {
            if (paint.measureText(text) <= maxWidth) return text
            var end = text.length
            while (end > 0 && paint.measureText(text.substring(0, end) + "..") > maxWidth) end--
            return text.substring(0, end) + ".."
        }

        /** Greedy word-wrap of [text] into lines no wider than [maxWidth] under [paint]. Beyond
         * [maxLines] lines, the remaining words are folded into one final "..."-truncated line instead
         * of growing forever -- this is what keeps one verbose subject/teacher name from blowing up an
         * entire grid row's height (and, transitively, forcing the whole grid off one page). */
        fun wrapCanvasText(text: String, paint: Paint, maxWidth: Float, maxLines: Int = Int.MAX_VALUE): List<String> {
            if (text.isBlank()) return emptyList()
            val lines = mutableListOf<String>()
            var current = StringBuilder()
            text.split(' ').forEach { word ->
                val candidate = if (current.isEmpty()) word else "$current $word"
                if (current.isEmpty() || paint.measureText(candidate) <= maxWidth) {
                    current = StringBuilder(candidate)
                } else {
                    lines += current.toString()
                    current = StringBuilder(word)
                }
            }
            if (current.isNotEmpty()) lines += current.toString()
            if (lines.size <= maxLines || maxLines <= 0) return lines
            val kept = lines.take(maxLines - 1).toMutableList()
            kept += fitCanvasText(lines.drop(maxLines - 1).joinToString(" "), paint, maxWidth)
            return kept
        }

        fun gridPaintFor(size: Float, isBold: Boolean) = Paint().apply { textSize = size; isFakeBoldText = isBold; isAntiAlias = true }

        fun wrapEntries(entries: List<Pair<String, Boolean>>, size: Float, maxWidth: Float, maxLinesPerEntry: Int = Int.MAX_VALUE): List<Pair<String, Boolean>> =
            entries.flatMap { (value, isBold) -> wrapCanvasText(value, gridPaintFor(size, isBold), maxWidth, maxLinesPerEntry).map { it to isBold } }

        /** Centers [lines] (text, bold) within a box, one per line, vertically centered overall. Callers
         * whose box height was computed from a line count MUST pass the exact same [lineH] used for
         * that math, or the text block silently overflows the box. */
        fun drawCenteredLines(canvas: Canvas, lines: List<Pair<String, Boolean>>, boxX: Float, boxW: Float, boxTopY: Float, boxH: Float, size: Float, lineH: Float = size + 4f, white: Boolean = false) {
            if (lines.isEmpty()) return
            var ly = boxTopY + (boxH - lines.size * lineH) / 2f + size
            lines.forEach { (value, isBold) ->
                val paint = gridPaintFor(size, isBold).apply { if (white) color = Color.WHITE }
                val w = paint.measureText(value)
                canvas.drawText(value, (boxX + (boxW - w) / 2f).coerceAtLeast(boxX + 3f), ly, paint)
                ly += lineH
            }
        }

        /** Printed-timetable-style grid: centered multi-line title, a two-line period header, and one
         * merged department cell per block spanning all of its day-split sub-rows. */
        fun drawTimetableGrid(layout: TimetableGridLayout) {
            val daysHeader = layout.secondColumnHeader
            val deptW = 100f
            val daysW = if (daysHeader != null) 78f else 0f
            val slotW = ((usable - deptW - daysW) / layout.columns.size.coerceAtLeast(1)).coerceAtLeast(64f)

            val fridayLabels = layout.fridayTimeLabels
            data class RowLayout(val daysLines: List<Pair<String, Boolean>>, val cellLines: List<List<Pair<String, Boolean>>>, val rowH: Float)
            data class BlockLayout(val deptLines: List<Pair<String, Boolean>>, val rows: List<RowLayout>, val blockHeight: Float)
            data class Measurement(val titleSizes: List<Float>, val cellSize: Float, val headerH: Float, val fridayHeaderH: Float, val deptFontSize: Float, val blocks: List<BlockLayout>, val totalHeight: Float)

            fun measure(scale: Float): Measurement {
                val titleSizes = listOf(15f, 14f, 11f, 11f).map { it * scale }
                val cellSize = 8.5f * scale
                val lineH = cellSize + 4.5f * scale
                val headerH = 32f * scale
                val fridayHeaderH = if (fridayLabels != null) 22f * scale else 0f
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
                val total = titleHeight + headerH + fridayHeaderH + blocks.sumOf { it.blockHeight.toDouble() }.toFloat()
                return Measurement(titleSizes, cellSize, headerH, fridayHeaderH, deptFontSize, blocks, total)
            }

            // Remaining room on the current page, not a fixed one-page assumption -- drawTimetableGrid
            // may start a little below the top margin (e.g. after newPage()'s own leading offset).
            val budget = (pageH - margin) - y
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
            val (titleSizes, cellSize, headerH, fridayHeaderH, deptFontSize, blocks) = measured
            val gridLineH = cellSize + 4.5f * scale
            val deptLineH = deptFontSize + 4.5f * scale

            titleSizes.forEachIndexed { i, size ->
                val line = layout.titleLines.getOrElse(i) { "" }
                if (line.isBlank()) return@forEachIndexed
                val paint = gridPaintFor(size, true)
                val w = paint.measureText(line)
                page!!.canvas.drawText(line, margin + (usable - w) / 2f, y + size, paint)
                y += size + 5f * scale
            }
            y += 6f * scale

            fun headerCell(x: Float, w: Float, h: Float, lines: List<Pair<String, Boolean>>) {
                val canvas = page!!.canvas
                canvas.drawRect(x, y, x + w, y + h, Paint().apply { style = Paint.Style.FILL; color = Color.BLACK })
                canvas.drawRect(x, y, x + w, y + h, Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.WHITE })
                drawCenteredLines(canvas, lines, x, w, y, h, cellSize, gridLineH, white = true)
            }

            fun drawGridHeader() {
                var x = margin
                headerCell(x, deptW, headerH, listOf("Departments" to true))
                x += deptW
                if (daysHeader != null) {
                    headerCell(x, daysW, headerH, listOf(daysHeader to true))
                    x += daysW
                }
                layout.columns.forEach { col ->
                    headerCell(x, slotW, headerH, listOf(col.index to true, col.timeLabel to false))
                    x += slotW
                }
                y += headerH
                if (fridayLabels != null) {
                    x = margin
                    headerCell(x, deptW, fridayHeaderH, listOf("Friday" to true))
                    x += deptW
                    if (daysHeader != null) {
                        headerCell(x, daysW, fridayHeaderH, emptyList())
                        x += daysW
                    }
                    fridayLabels.forEach { label ->
                        headerCell(x, slotW, fridayHeaderH, listOf(label to false))
                        x += slotW
                    }
                    y += fridayHeaderH
                }
            }

            if (y + headerH * 3 > pageH - margin) newPage()
            drawGridHeader()

            blocks.forEach { block ->
                val blockHeight = block.blockHeight
                if (y + blockHeight > pageH - margin && blockHeight <= pageH - 2 * margin - headerH) {
                    newPage()
                    drawGridHeader()
                }
                val blockTopY = y
                block.rows.forEachIndexed { subIdx, row ->
                    if (y + row.rowH > pageH - margin) { newPage(); drawGridHeader() }
                    val canvas = page!!.canvas
                    var x = margin + deptW
                    if (daysHeader != null) {
                        canvas.drawRect(x, y, x + daysW, y + row.rowH, gridPaint)
                        drawCenteredLines(canvas, row.daysLines, x, daysW, y, row.rowH, cellSize, gridLineH)
                        x += daysW
                    }
                    row.cellLines.forEach { lines ->
                        canvas.drawRect(x, y, x + slotW, y + row.rowH, gridPaint)
                        drawCenteredLines(canvas, lines, x, slotW, y, row.rowH, cellSize, gridLineH)
                        x += slotW
                    }
                    y += row.rowH
                    if (subIdx == block.rows.lastIndex) {
                        val fullCanvas = page!!.canvas
                        val thickBorder = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.2f; color = Color.rgb(76, 76, 76) }
                        fullCanvas.drawRect(margin, blockTopY, margin + deptW, blockTopY + blockHeight, thickBorder)
                        drawCenteredLines(fullCanvas, block.deptLines, margin, deptW, blockTopY, blockHeight, deptFontSize, deptLineH)
                    }
                }
            }
            y += 14f * scale
        }

        fun drawRow(cells: List<String>, widths: List<Float>, header: Boolean, black: Set<Int> = emptySet()) {
            val canvas = page!!.canvas
            val tableW = widths.sum()
            if (header) canvas.drawRect(margin, y, margin + tableW, y + rowH, headerBg)
            var x = margin
            widths.forEachIndexed { i, w ->
                if (i in black) canvas.drawRect(x, y, x + w, y + rowH, blackFill)
                canvas.drawRect(x, y, x + w, y + rowH, gridPaint)
                val paint = if (header) headText else bodyPaint
                val text = if (i in black) "" else cells.getOrNull(i).orEmpty()
                val fit = paint.breakText(text, true, w - 6f, null)
                canvas.drawText(text.substring(0, fit), x + 3f, y + rowH - 5f, paint)
                x += w
            }
            y += rowH
        }

        fun drawSection(section: ExportSection, showName: Boolean) {
            val weights = pdfColumnWeights(section).ifEmpty { listOf(1f) }
            val widths = weights.map { it / weights.sum() * usable }
            if (y + rowH * 3 > pageH - margin) newPage()
            if (showName) {
                page!!.canvas.drawText(section.name, margin, y + 11f, sectionPaint)
                y += 16f
            }
            if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true, black = section.blackColumns)
            section.rows.forEach { row ->
                if (y + rowH > pageH - margin) {
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
        page?.let { pdf.finishPage(it) }
        file.outputStream().use { pdf.writeTo(it) }
        pdf.close()
    }

    private fun share(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export report").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

/**
 * Screen-level export hook: returns a callback that writes and shares a document, and shows its own
 * error dialog if that fails.
 */
@Composable
fun rememberDocumentExport(): (ExportDocument, ExportFormat) -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    error?.let { CmsErrorDialog(message = it, title = "Couldn't export the report", onDismiss = { error = null }) }
    return remember(context, scope) {
        { doc, format ->
            scope.launch {
                runCatching { DocumentExporter.export(context, doc, format) }
                    .onFailure { error = FileReadErrors.describeWrite(it, format.label) }
            }
        }
    }
}
