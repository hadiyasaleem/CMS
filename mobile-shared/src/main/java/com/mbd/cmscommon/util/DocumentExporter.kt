package com.mbd.cmscommon.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.ExportSection
import com.mbd.cmscommon.export.XlsxWriter
import com.mbd.cmscommon.export.pdfColumnWeights
import com.mbd.cmscommon.export.safeFileBase
import java.io.File
import kotlinx.coroutines.Dispatchers
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
        // Landscape A4 so wide registers fit.
        val pageW = 842
        val pageH = 595
        val margin = 28f
        val rowH = 16f
        val usable = pageW - 2 * margin
        val titlePaint = Paint().apply { textSize = 14f; isFakeBoldText = true; isAntiAlias = true }
        val metaPaint = Paint().apply { textSize = 9f; isAntiAlias = true }
        val sectionPaint = Paint().apply { textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        val bodyPaint = Paint().apply { textSize = 8f; isAntiAlias = true }
        val headText = Paint().apply { textSize = 8f; isFakeBoldText = true; color = Color.WHITE; isAntiAlias = true }
        val gridPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.rgb(120, 120, 120) }
        val headerBg = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(30, 30, 30) }

        var pageNo = 0
        var page: PdfDocument.Page? = null
        var y = 0f

        fun newPage() {
            page?.let { pdf.finishPage(it) }
            pageNo++
            page = pdf.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
            y = margin + 14
            val canvas = page!!.canvas
            canvas.drawText(doc.title.firstOrNull() ?: "Report", margin, y, titlePaint)
            y += 15
            doc.title.drop(1).forEach { canvas.drawText(it, margin, y, metaPaint); y += 12 }
            y += 6
        }

        fun drawRow(cells: List<String>, widths: List<Float>, header: Boolean) {
            val canvas = page!!.canvas
            val tableW = widths.sum()
            if (header) canvas.drawRect(margin, y, margin + tableW, y + rowH, headerBg)
            var x = margin
            widths.forEachIndexed { i, w ->
                canvas.drawRect(x, y, x + w, y + rowH, gridPaint)
                val paint = if (header) headText else bodyPaint
                val text = cells.getOrNull(i).orEmpty()
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
            if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true)
            section.rows.forEach { row ->
                if (y + rowH > pageH - margin) {
                    newPage()
                    if (section.header.isNotEmpty()) drawRow(section.header, widths, header = true)
                }
                drawRow(row, widths, header = false)
            }
            y += 14f
        }

        newPage()
        doc.sections.forEach { drawSection(it, showName = doc.sections.size > 1) }
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
