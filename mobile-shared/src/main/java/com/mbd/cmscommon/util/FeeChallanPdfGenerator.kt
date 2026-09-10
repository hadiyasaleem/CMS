package com.mbd.cmscommon.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.mbd.cmscommon.domain.model.FeeChallanHeader
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import java.io.ByteArrayOutputStream

/** Renders a 3-copy (student/college/clerk) fee challan as a portrait PDF. Mirrors
 * [com.mbd.cmsdesktop.util.FeeChallanPdfGenerator]'s layout using the platform's own
 * `android.graphics.pdf.PdfDocument` API instead of pdfbox -- the two are not literally shared code
 * since the drawing APIs differ, matching how RecordsExporter already exists once per platform. */
object FeeChallanPdfGenerator {
    private const val PAGE_W = 595
    private const val PAGE_H = 842
    private const val MARGIN = 28f
    private const val ROW_H = 10f
    private const val GAP = 8f

    private val COPY_LABELS = listOf("STUDENT COPY", "COLLEGE COPY", "CLERK COPY")

    private val titlePaint = Paint().apply { textSize = 11f; isFakeBoldText = true }
    private val copyLabelPaint = Paint().apply { textSize = 7f; isFakeBoldText = true; color = Color.rgb(120, 30, 30) }
    private val subtitlePaint = Paint().apply { textSize = 8f; color = Color.rgb(90, 90, 90) }
    private val fieldPaint = Paint().apply { textSize = 7f }
    private val notePaint = Paint().apply { textSize = 6.5f; color = Color.rgb(90, 90, 90) }
    private val smallLabelPaint = Paint().apply { textSize = 6.5f; color = Color.rgb(90, 90, 90) }
    private val headTextPaint = Paint().apply { textSize = 7f; isFakeBoldText = true; color = Color.WHITE }
    private val bodyPaint = Paint().apply { textSize = 7f }
    private val headerBg = Paint().apply { style = Paint.Style.FILL; color = Color.rgb(30, 30, 30) }
    private val gridPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.6f; color = Color.rgb(120, 120, 120) }
    private val dashPaint = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.8f; color = Color.rgb(150, 150, 150) }

    fun generate(header: FeeChallanHeader, structure: SessionFeeStructure): ByteArray {
        val doc = PdfDocument()
        val heads = structure.heads.filter { it.label.isNotBlank() }
        val copyHeight = estimateCopyHeight(heads.size)
        val usable = PAGE_H - 2 * MARGIN

        if (copyHeight * COPY_LABELS.size + GAP * (COPY_LABELS.size - 1) <= usable) {
            val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, 1).create())
            var y = MARGIN
            COPY_LABELS.forEachIndexed { i, label ->
                drawCopy(page.canvas, MARGIN, y, PAGE_W - MARGIN, header, structure, heads, label)
                y += copyHeight
                if (i < COPY_LABELS.lastIndex) {
                    drawDashedLine(page.canvas, MARGIN, y + GAP / 2, PAGE_W - MARGIN)
                    y += GAP
                }
            }
            doc.finishPage(page)
        } else {
            COPY_LABELS.forEachIndexed { i, label ->
                val page = doc.startPage(PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, i + 1).create())
                drawCopy(page.canvas, MARGIN, MARGIN, PAGE_W - MARGIN, header, structure, heads, label)
                doc.finishPage(page)
            }
        }

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    // Must track drawCopy's actual draw sequence exactly (worst case: both optional note lines
    // present): 9(offset)+11(title)+10(subtitle)+28(4 field rows)+3(gap)=61 pre-grid, +2*ROW_H
    // (head+total rows)=20, +6(post-grid gap), +8(due date)+7(late fine)+7(payment note)+6(pre-box
    // gap), +32(24pt box + 8pt label) = 147 fixed, plus ROW_H per fee-head body row, plus a 13pt
    // safety margin for font-metric rounding.
    private fun estimateCopyHeight(headCount: Int): Float = 160f + headCount * ROW_H

    private fun drawCopy(
        canvas: Canvas,
        x0: Float,
        y0: Float,
        x1: Float,
        header: FeeChallanHeader,
        structure: SessionFeeStructure,
        heads: List<FeeHead>,
        copyLabel: String,
    ) {
        var y = y0 + 9f
        canvas.drawText("GGC-MBD", x0, y, titlePaint)
        canvas.drawTextRight(copyLabel, x1, y, copyLabelPaint)
        y += 11f
        canvas.drawText("Fee Challan", x0, y, subtitlePaint)
        y += 10f

        val fields = listOf(
            "Challan No" to header.challanNumber,
            "Date" to header.issueDate,
            "Student Name" to header.studentName,
            "Roll No" to header.rollNumber,
            "Father's Name" to (header.fatherName ?: "-"),
            "Session" to header.sessionLabel,
            "Shift" to header.shift,
            "Department" to (header.deptCode ?: "-"),
        )
        val halfW = (x1 - x0) / 2
        fields.chunked(2).forEach { pair ->
            canvas.drawText("${pair[0].first}: ${pair[0].second}", x0, y, fieldPaint)
            if (pair.size > 1) canvas.drawText("${pair[1].first}: ${pair[1].second}", x0 + halfW, y, fieldPaint)
            y += 7f
        }
        y += 3f

        val amountColX = x1 - 55f
        canvas.drawRect(x0, y, x1, y + ROW_H, headerBg)
        canvas.drawText("Fee Head", x0 + 3f, y + ROW_H - 2.5f, headTextPaint)
        canvas.drawText("Amount (Rs)", amountColX, y + ROW_H - 2.5f, headTextPaint)
        canvas.drawRect(x0, y, x1, y + ROW_H, gridPaint)
        canvas.drawLine(amountColX - 3f, y, amountColX - 3f, y + ROW_H, gridPaint)
        y += ROW_H
        heads.forEach { head ->
            canvas.drawRect(x0, y, x1, y + ROW_H, gridPaint)
            canvas.drawLine(amountColX - 3f, y, amountColX - 3f, y + ROW_H, gridPaint)
            canvas.drawText(head.label, x0 + 3f, y + ROW_H - 2.5f, bodyPaint)
            canvas.drawTextRight("%.2f".format(head.amount), x1 - 3f, y + ROW_H - 2.5f, bodyPaint)
            y += ROW_H
        }
        canvas.drawRect(x0, y, x1, y + ROW_H, headerBg)
        canvas.drawRect(x0, y, x1, y + ROW_H, gridPaint)
        canvas.drawText("Total", x0 + 3f, y + ROW_H - 2.5f, headTextPaint)
        canvas.drawTextRight("%.2f".format(structure.totalAmount), x1 - 3f, y + ROW_H - 2.5f, headTextPaint)
        y += ROW_H + 6f

        canvas.drawText("Due date: ${structure.dueDate?.takeIf { it.isNotBlank() } ?: "Not set"}", x0, y, fieldPaint)
        y += 8f
        structure.lateFineNote?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText("Late fine: $it", x0, y, notePaint)
            y += 7f
        }
        structure.paymentNote?.takeIf { it.isNotBlank() }?.let {
            canvas.drawText(it, x0, y, notePaint)
            y += 7f
        }
        y += 6f

        val boxW = (x1 - x0) / 2 - 6f
        val boxH = 24f
        canvas.drawRect(x0, y, x0 + boxW, y + boxH, gridPaint)
        canvas.drawText("Accountant Signature", x0 + 4f, y + boxH + 8f, smallLabelPaint)
        canvas.drawRect(x1 - boxW, y, x1, y + boxH, gridPaint)
        canvas.drawText("Stamp", x1 - boxW + 4f, y + boxH + 8f, smallLabelPaint)
    }

    private fun drawDashedLine(canvas: Canvas, x0: Float, y: Float, x1: Float) {
        var x = x0
        while (x < x1) {
            canvas.drawLine(x, y, minOf(x + 4f, x1), y, dashPaint)
            x += 7f
        }
    }

    private fun Canvas.drawTextRight(text: String, rightX: Float, y: Float, paint: Paint) {
        drawText(text, rightX - paint.measureText(text), y, paint)
    }
}
