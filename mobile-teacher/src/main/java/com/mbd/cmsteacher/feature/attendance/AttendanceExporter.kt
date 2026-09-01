package com.mbd.cmsteacher.feature.attendance

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.util.Locale

object AttendanceExporter {

    private fun letter(status: AttendanceStatus?): String = when (status) {
        AttendanceStatus.PRESENT -> "P"
        AttendanceStatus.ABSENT -> "A"
        AttendanceStatus.LEAVE -> "L"
        null -> ""
    }

    private fun totals(byDate: Map<LocalDate, DailyAttendanceMark>?): StudentTotals {
        val values = byDate?.values.orEmpty()
        return StudentTotals(
            present = values.count { it.status == AttendanceStatus.PRESENT },
            absent = values.count { it.status == AttendanceStatus.ABSENT },
            leave = values.count { it.status == AttendanceStatus.LEAVE },
            late = values.count { it.isLate },
        )
    }

    private fun creditText(meta: ExportMeta): String = meta.creditHours?.let { " ($it cr)" } ?: ""

    private fun sessionText(meta: ExportMeta): String = listOfNotNull(
        meta.sessionLabel.takeIf { it.isNotBlank() },
        meta.deptId.takeIf { it.isNotBlank() }?.uppercase(Locale.ROOT),
        meta.shift.takeIf { it.isNotBlank() },
        if (meta.semester > 0) "Sem ${meta.semester}" else null,
    ).joinToString(" · ")

    private fun csv(v: String): String =
        if (!v.contains(',') && !v.contains('"') && !v.contains('\n') && !v.contains('\r')) v else "\"${v.replace("\"", "\"\"")}\""

    private fun fileName(course: String, month: String, ext: String): String =
        "attendance_${course}_$month".replace(Regex("[^A-Za-z0-9_]"), "_") + ".$ext"

    private fun share(context: Context, file: File, mime: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export attendance").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    fun exportCsv(
        context: Context,
        meta: ExportMeta,
        courseCode: String,
        monthLabel: String,
        days: List<LocalDate>,
        roster: List<SessionStudent>,
        marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    ) {
        val sb = StringBuilder()
        sb.append("Attendance Report\n")
        sb.append("Subject,${csv("$courseCode — ${meta.subjectName}${creditText(meta)}")}\n")
        sb.append("Teacher,${csv(meta.teacherName)}\n")
        sb.append("Session,${csv(sessionText(meta))}\n")
        val timeslotText = meta.timeslots.joinToString("; ").ifBlank { "—" }
        sb.append("Timeslot,${csv(timeslotText)}\n")
        sb.append("Students,${roster.size}\n")
        sb.append("Month,${csv(monthLabel)}\n")
        sb.append("\n")
        sb.append("Roll,Name")
        days.forEach { sb.append(",").append(it.dayOfMonth) }
        sb.append(",Present,Absent,Leave,Present%,Late%\n")

        roster.forEach { student ->
            val byDate = marks[student.rollNumber]
            val studentTotals = totals(byDate)
            sb.append(csv(student.rollNumber)).append(",").append(csv(student.name))
            days.forEach { day -> sb.append(",").append(letter(byDate?.get(day)?.status)) }
            sb.append(",${studentTotals.present},${studentTotals.absent},${studentTotals.leave},${studentTotals.percent}%,${studentTotals.latePercent}%\n")
        }

        val file = File(context.cacheDir, fileName(courseCode, monthLabel, "csv"))
        file.writeText(sb.toString())
        share(context, file, "text/csv")
    }

    fun exportPdf(
        context: Context,
        meta: ExportMeta,
        courseCode: String,
        monthLabel: String,
        roster: List<SessionStudent>,
        marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    ) {
        val doc = PdfDocument()
        val margin = 40f
        val pageW = 595
        val pageH = 842
        val rowH = 20f

        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true }
        val metaPaint = Paint().apply { textSize = 10f }
        val headerPaint = Paint().apply { textSize = 11f; isFakeBoldText = true }
        val bodyPaint = Paint().apply { textSize = 11f }
        val cols = floatArrayOf(margin, 80f + margin, 270f + margin, 315f + margin, 360f + margin, 410f + margin, 470f + margin)

        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
        var canvas = page.canvas
        var y = margin + 24f

        fun drawHeader() {
            canvas.drawText("Roll", cols[0], y, headerPaint)
            canvas.drawText("Name", cols[1], y, headerPaint)
            canvas.drawText("P", cols[2], y, headerPaint)
            canvas.drawText("A", cols[3], y, headerPaint)
            canvas.drawText("L", cols[4], y, headerPaint)
            canvas.drawText("Pres%", cols[5], y, headerPaint)
            canvas.drawText("Late%", cols[6], y, headerPaint)
            y += rowH
        }

        canvas.drawText("Attendance — $courseCode ${meta.subjectName}${creditText(meta)}", margin, y, titlePaint)
        y += 18f
        val teacherLine = "Teacher: ${meta.teacherName.ifBlank { "—" }}"
        val sessionLine = "Session: ${sessionText(meta).ifBlank { "—" }}"
        val timeslotLine = "Timeslot: ${meta.timeslots.joinToString("; ").ifBlank { "—" }}"
        val countsLine = "Students: ${roster.size}          Month: $monthLabel"
        listOf(teacherLine, sessionLine, timeslotLine, countsLine).forEach { line ->
            canvas.drawText(line, margin, y, metaPaint)
            y += 14f
        }
        y += 12f
        drawHeader()

        roster.forEach { student ->
            if (y > pageH - margin) {
                doc.finishPage(page)
                pageNo += 1
                page = doc.startPage(PdfDocument.PageInfo.Builder(pageW, pageH, pageNo).create())
                canvas = page.canvas
                y = margin + 24f
                drawHeader()
            }
            val studentTotals = totals(marks[student.rollNumber])
            canvas.drawText(student.rollNumber, cols[0], y, bodyPaint)
            canvas.drawText(student.name.take(28), cols[1], y, bodyPaint)
            canvas.drawText(studentTotals.present.toString(), cols[2], y, bodyPaint)
            canvas.drawText(studentTotals.absent.toString(), cols[3], y, bodyPaint)
            canvas.drawText(studentTotals.leave.toString(), cols[4], y, bodyPaint)
            canvas.drawText("${studentTotals.percent}%", cols[5], y, bodyPaint)
            canvas.drawText("${studentTotals.latePercent}%", cols[6], y, bodyPaint)
            y += rowH
        }

        doc.finishPage(page)
        val file = File(context.cacheDir, fileName(courseCode, monthLabel, "pdf"))
        FileOutputStream(file).use { doc.writeTo(it) }
        doc.close()
        share(context, file, "application/pdf")
    }
}
