package com.mbd.cmsdesktop.util

import com.mbd.cmscommon.domain.model.FeeChallanHeader
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import java.io.ByteArrayOutputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.common.PDRectangle
import org.apache.pdfbox.pdmodel.font.PDFont
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.font.Standard14Fonts

/** Renders a 3-copy (student/college/clerk) fee challan as a portrait PDF, using pdfbox. Mirrors
 * [com.mbd.cmscommon.util.FeeChallanPdfGenerator]'s layout (the mobile equivalent, using Android's
 * own PdfDocument API instead) -- not literally shared code since the drawing APIs differ, matching
 * how RecordsExporter already exists once per platform for the same reason. */
object FeeChallanPdfGenerator {
    private const val PAGE_W = 595f
    private const val PAGE_H = 842f
    private const val MARGIN = 28f
    private const val ROW_H = 10f
    private const val GAP = 8f

    private val COPY_LABELS = listOf("STUDENT COPY", "COLLEGE COPY", "CLERK COPY")

    fun generate(header: FeeChallanHeader, structure: SessionFeeStructure): ByteArray {
        val regular = PDType1Font(Standard14Fonts.FontName.HELVETICA)
        val bold = PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD)
        val heads = structure.heads.filter { it.label.isNotBlank() }
        val copyHeight = estimateCopyHeight(heads.size)
        val usable = PAGE_H - 2 * MARGIN

        val out = ByteArrayOutputStream()
        PDDocument().use { doc ->
            if (copyHeight * COPY_LABELS.size + GAP * (COPY_LABELS.size - 1) <= usable) {
                val page = PDPage(PDRectangle(PAGE_W, PAGE_H))
                doc.addPage(page)
                PDPageContentStream(doc, page).use { stream ->
                    var y = MARGIN
                    COPY_LABELS.forEachIndexed { i, label ->
                        drawCopy(stream, regular, bold, MARGIN, y, PAGE_W - MARGIN, header, structure, heads, label)
                        y += copyHeight
                        if (i < COPY_LABELS.lastIndex) {
                            drawDashedLine(stream, MARGIN, y + GAP / 2, PAGE_W - MARGIN)
                            y += GAP
                        }
                    }
                }
            } else {
                COPY_LABELS.forEach { label ->
                    val page = PDPage(PDRectangle(PAGE_W, PAGE_H))
                    doc.addPage(page)
                    PDPageContentStream(doc, page).use { stream ->
                        drawCopy(stream, regular, bold, MARGIN, MARGIN, PAGE_W - MARGIN, header, structure, heads, label)
                    }
                }
            }
            doc.save(out)
        }
        return out.toByteArray()
    }

    // Must track drawCopy's actual draw sequence exactly (worst case: both optional note lines
    // present): 9(offset)+11(title)+10(subtitle)+28(4 field rows)+3(gap)=61 pre-grid, +2*ROW_H
    // (head+total rows)=20, +6(post-grid gap), +8(due date)+7(late fine)+7(payment note)+6(pre-box
    // gap), +32(24pt box + 8pt label) = 147 fixed, plus ROW_H per fee-head body row, plus a 13pt
    // safety margin for font-metric rounding.
    private fun estimateCopyHeight(headCount: Int): Float = 160f + headCount * ROW_H

    private fun drawCopy(
        stream: PDPageContentStream,
        regular: PDFont,
        bold: PDFont,
        x0: Float,
        y0: Float,
        x1: Float,
        header: FeeChallanHeader,
        structure: SessionFeeStructure,
        heads: List<FeeHead>,
        copyLabel: String,
    ) {
        fun text(str: String, x: Float, yTop: Float, font: PDFont, size: Float, white: Boolean = false) {
            stream.beginText()
            stream.setFont(font, size)
            if (white) stream.setNonStrokingColor(1f, 1f, 1f)
            stream.newLineAtOffset(x, PAGE_H - yTop)
            stream.showText(str)
            stream.endText()
            if (white) stream.setNonStrokingColor(0f, 0f, 0f)
        }
        fun textRight(str: String, rightX: Float, yTop: Float, font: PDFont, size: Float, white: Boolean = false) {
            text(str, rightX - font.getStringWidth(str) / 1000f * size, yTop, font, size, white)
        }
        fun strokeRect(x: Float, top: Float, w: Float, h: Float) {
            stream.setStrokingColor(0.47f, 0.47f, 0.47f)
            stream.setLineWidth(0.6f)
            stream.addRect(x, PAGE_H - top - h, w, h)
            stream.stroke()
        }
        fun fillRect(x: Float, top: Float, w: Float, h: Float) {
            stream.setNonStrokingColor(0.12f, 0.12f, 0.12f)
            stream.addRect(x, PAGE_H - top - h, w, h)
            stream.fill()
        }
        fun line(xa: Float, ya: Float, xb: Float, yb: Float) {
            stream.setStrokingColor(0.47f, 0.47f, 0.47f)
            stream.setLineWidth(0.6f)
            stream.moveTo(xa, PAGE_H - ya)
            stream.lineTo(xb, PAGE_H - yb)
            stream.stroke()
        }

        var y = y0 + 9f
        text("GGC-MBD", x0, y, bold, 11f)
        textRight(copyLabel, x1, y, bold, 7f)
        y += 11f
        text("Fee Challan", x0, y, regular, 8f)
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
            text("${pair[0].first}: ${pair[0].second}", x0, y, regular, 7f)
            if (pair.size > 1) text("${pair[1].first}: ${pair[1].second}", x0 + halfW, y, regular, 7f)
            y += 7f
        }
        y += 3f

        val amountColX = x1 - 55f
        fillRect(x0, y, x1 - x0, ROW_H)
        text("Fee Head", x0 + 3f, y + ROW_H - 2.5f, bold, 7f, white = true)
        text("Amount (Rs)", amountColX, y + ROW_H - 2.5f, bold, 7f, white = true)
        strokeRect(x0, y, x1 - x0, ROW_H)
        line(amountColX - 3f, y, amountColX - 3f, y + ROW_H)
        y += ROW_H
        heads.forEach { head ->
            strokeRect(x0, y, x1 - x0, ROW_H)
            line(amountColX - 3f, y, amountColX - 3f, y + ROW_H)
            text(head.label, x0 + 3f, y + ROW_H - 2.5f, regular, 7f)
            textRight("%.2f".format(head.amount), x1 - 3f, y + ROW_H - 2.5f, regular, 7f)
            y += ROW_H
        }
        fillRect(x0, y, x1 - x0, ROW_H)
        strokeRect(x0, y, x1 - x0, ROW_H)
        text("Total", x0 + 3f, y + ROW_H - 2.5f, bold, 7f, white = true)
        textRight("%.2f".format(structure.totalAmount), x1 - 3f, y + ROW_H - 2.5f, bold, 7f, white = true)
        y += ROW_H + 6f

        text("Due date: ${structure.dueDate?.takeIf { it.isNotBlank() } ?: "Not set"}", x0, y, regular, 7f)
        y += 8f
        structure.lateFineNote?.takeIf { it.isNotBlank() }?.let {
            text("Late fine: $it", x0, y, regular, 6.5f)
            y += 7f
        }
        structure.paymentNote?.takeIf { it.isNotBlank() }?.let {
            text(it, x0, y, regular, 6.5f)
            y += 7f
        }
        y += 6f

        val boxW = (x1 - x0) / 2 - 6f
        val boxH = 24f
        strokeRect(x0, y, boxW, boxH)
        text("Accountant Signature", x0 + 4f, y + boxH + 8f, regular, 6.5f)
        strokeRect(x1 - boxW, y, boxW, boxH)
        text("Stamp", x1 - boxW + 4f, y + boxH + 8f, regular, 6.5f)
    }

    private fun drawDashedLine(stream: PDPageContentStream, x0: Float, y: Float, x1: Float) {
        stream.setStrokingColor(0.6f, 0.6f, 0.6f)
        stream.setLineWidth(0.8f)
        var x = x0
        while (x < x1) {
            val segEnd = minOf(x + 4f, x1)
            stream.moveTo(x, PAGE_H - y)
            stream.lineTo(segEnd, PAGE_H - y)
            stream.stroke()
            x += 7f
        }
    }
}
