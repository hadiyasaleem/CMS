package com.mbd.cmsadmin.feature.records

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import java.io.File

/** A simple generic table export (CSV for Excel + a landscape PDF), shared via the system sheet. */
object RecordsExporter {

    fun exportCsv(context: Context, fileBase: String, title: List<String>, header: List<String>, rows: List<List<String>>) {
        val sb = StringBuilder()
        title.forEach { sb.append(csv(it)).append("\n") }
        if (title.isNotEmpty()) sb.append("\n")
        sb.append(header.joinToString(",") { csv(it) }).append("\n")
        rows.forEach { r -> sb.append(r.joinToString(",") { csv(it) }).append("\n") }
        val file = File(context.cacheDir, sanitize(fileBase) + ".csv")
        // Fold en-dash/middle-dot to ASCII, and prepend a UTF-8 BOM (EF BB BF) so Excel reads the
        // file as UTF-8 instead of Latin-1 (which mangles those typographic characters).
        val content = sb.toString().replace('–', '-').replace('·', '-')
        file.outputStream().use { out ->
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            out.write(content.toByteArray(Charsets.UTF_8))
        }
        share(context, file, "text/csv")
    }

    fun exportPdf(context: Context, fileBase: String, title: List<String>, header: List<String>, rows: List<List<String>>) {
        val doc = PdfDocument()
        // Landscape A4 so wide day-grids fit better.
        val pageW = 842; val pageH = 595; val margin = 28f
        val titlePaint = Paint().apply { textSize = 14f; isFakeBoldText = true }
        val metaPaint = Paint().apply { textSize = 9f }
        val headPaint = Paint().apply { textSize = 8f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 8f }
        val gridPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.rgb(120, 120, 120) }
        val headerBg = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(30, 30, 30) }
        val headText = Paint().apply { textSize = 8f; isFakeBoldText = true; color = Color.WHITE }
        val rowH = 16f

        val cols = header.size.coerceAtLeast(1)
        // First two columns (roll/name) get more room; the rest split what's left evenly.
        val usable = pageW - 2 * margin
        val firstTwo = minOf(cols, 2)
        val wideW = if (cols <= 2) usable / cols else usable * 0.30f / firstTwo
        val restW = if (cols <= 2) 0f else usable * 0.70f / (cols - firstTwo)
        fun width(i: Int) = if (i < 2) wideW else restW
        fun colX(i: Int): Float {
            var x = margin
            for (j in 0 until i) x += width(j)
            return x
        }
        val tableRight = margin + usable

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
        var canvas = page.canvas
        var y: Float

        // Draws one bordered row at top-left [y]; returns the next row's top.
        fun drawRow(cells: List<String>, top: Float, headerRow: Boolean): Float {
            val bottom = top + rowH
            if (headerRow) canvas.drawRect(margin, top, tableRight, bottom, headerBg)
            val baseline = top + rowH - 5f
            cells.forEachIndexed { i, c ->
                val x = colX(i)
                canvas.drawRect(x, top, x + width(i), bottom, gridPaint)   // cell border
                val maxChars = if (i < 2) 24 else 6
                canvas.drawText(c.take(maxChars), x + 3f, baseline, if (headerRow) headText else bodyPaint)
            }
            return bottom
        }

        fun startBody(): Float {
            var yy = margin + 14
            canvas.drawText(title.firstOrNull() ?: "Report", margin, yy, titlePaint); yy += 15
            title.drop(1).forEach { canvas.drawText(it, margin, yy, metaPaint); yy += 12 }
            yy += 6
            return drawRow(header, yy, headerRow = true)
        }

        y = startBody()
        rows.forEach { r ->
            if (y + rowH > pageH - margin) {
                doc.finishPage(page); pageNo++
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
                canvas = page.canvas
                y = startBody()
            }
            y = drawRow(r, y, headerRow = false)
        }
        doc.finishPage(page)
        val file = File(context.cacheDir, sanitize(fileBase) + ".pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        share(context, file, "application/pdf")
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

    private fun sanitize(s: String) = s.replace(Regex("[^A-Za-z0-9_]"), "_")
    private fun csv(v: String) = if (v.contains(',') || v.contains('"') || v.contains('\n')) "\"${v.replace("\"", "\"\"")}\"" else v
}
