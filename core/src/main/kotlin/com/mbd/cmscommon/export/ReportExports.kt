package com.mbd.cmscommon.export

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.AttendanceExportPayload
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.DatesheetSlot
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentTermAttendance
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.attendanceCounts
import com.mbd.cmscommon.domain.model.isRegisterHoliday
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val WEEKDAY = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

private fun pct(value: Int, total: Int): String = if (total == 0) "-" else "${(value * 100) / total}%"
private fun num(value: Double?, digits: Int = 2): String = value?.let { "%.${digits}f".format(Locale.ENGLISH, it) } ?: "-"
private fun letter(status: AttendanceStatus): String = status.name.take(1)
private fun titleCase(raw: String): String = raw.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
fun sessionTitle(session: AcademicSession?): String =
    session?.let { "${it.deptId.uppercase(Locale.ROOT)} ${it.label} · ${titleCase(it.shift.name)} · Semester ${it.currentSemester}" } ?: ""

fun AttendanceExportPayload.toExportDocument() = singleSectionDocument(fileBase, title, header, rows)

/** Teacher monthly register: one column per day, Sundays marked as holidays, totals at the end. */
fun attendanceRegisterExport(
    courseCode: String,
    session: AcademicSession?,
    month: YearMonth,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
): ExportDocument {
    val days = (1..month.lengthOfMonth()).map(month::atDay)
    val datesWithMarks = marks.values.flatMap { it.keys }.toSet()
    val header = listOf("Roll", "Name") +
        days.map { d -> d.dayOfMonth.toString().padStart(2, '0') + " " + d.format(WEEKDAY) + if (isRegisterHoliday(d, datesWithMarks)) " (Holiday)" else "" } +
        listOf("P", "A", "L", "Late", "%")
    val rows = roster.sortedBy { it.rollNumber }.map { student ->
        val byDate = marks[student.rollNumber].orEmpty()
        val counts = attendanceCounts(byDate.values)
        listOf(student.rollNumber, student.name) +
            days.map { d -> if (isRegisterHoliday(d, datesWithMarks)) "H" else byDate[d]?.let { letter(it.status) + if (it.isLate) "*" else "" } ?: "" } +
            listOf(counts.present.toString(), counts.absent.toString(), counts.leave.toString(), counts.late.toString(), pct(counts.present, counts.total))
    }
    return ExportDocument(
        fileBase = "attendance_${courseCode}_$month",
        title = listOfNotNull("Attendance Register", "$courseCode · ${month.format(MONTH)}", sessionTitle(session).ifBlank { null }, "P present · A absent · L leave · * late · H holiday"),
        sections = listOf(ExportSection("Register", header, rows)),
    )
}

fun termSummaryExport(
    courseCode: String,
    rollNumber: String,
    student: SessionStudent?,
    session: AcademicSession?,
    term: SemesterTerm?,
    summary: StudentTermAttendance,
): ExportDocument {
    val info = listOf(
        listOf("Name", student?.name ?: "-"),
        listOf("Roll number", student?.rollNumber ?: rollNumber),
        listOf("Session", sessionTitle(session).ifBlank { "-" }),
        listOf("Subject", courseCode),
        listOf("Term", term?.startDate?.let { "$it to ${term.endDate ?: "ongoing"}" } ?: "Dates not set"),
        listOf("Student account", student?.linkedEmail?.ifBlank { null } ?: "Not linked"),
        listOf("Overall attendance", pct(summary.overall.present, summary.overall.total)),
    )
    val monthHeader = listOf("Month", "Classes", "Present", "Absent", "Leave", "Late", "%")
    val monthRows = summary.months.map { m ->
        val c = m.tally
        listOf(m.month.format(MONTH), c.total.toString(), c.present.toString(), c.absent.toString(), c.leave.toString(), c.late.toString(), pct(c.present, c.total))
    } + listOf(listOf("Overall", summary.overall.total.toString(), summary.overall.present.toString(), summary.overall.absent.toString(), summary.overall.leave.toString(), summary.overall.late.toString(), pct(summary.overall.present, summary.overall.total)))
    return ExportDocument(
        fileBase = "attendance_${courseCode}_$rollNumber",
        title = listOf("Term Attendance Summary", "${student?.name ?: rollNumber} · $courseCode"),
        sections = listOf(ExportSection("Student", listOf("Field", "Value"), info), ExportSection("By month", monthHeader, monthRows)),
    )
}

fun marksSheetExport(
    assignment: ResolvedAssignment,
    examType: ExamType,
    roster: List<SessionStudent>,
    scores: Map<String, String>,
    absentRolls: Set<String>,
): ExportDocument {
    val header = listOf("Roll", "Name", "Score", "Out of", "Status")
    val rows = roster.sortedBy { it.rollNumber }.map { s ->
        val absent = s.rollNumber in absentRolls
        val score = scores[s.rollNumber].orEmpty()
        listOf(s.rollNumber, s.name, if (absent) "" else score, examType.maxMarks.toString(), when {
            absent -> "Absent"
            score.isBlank() -> "Not entered"
            else -> "Entered"
        })
    }
    return ExportDocument(
        fileBase = "marks_${assignment.courseCode}_${examType.name}",
        title = listOf("Marks Sheet", "${assignment.courseCode} · ${assignment.subjectLabel}", "${assignment.sessionLabel} · ${titleCase(examType.name)} (out of ${examType.maxMarks})"),
        sections = listOf(ExportSection("Marks", header, rows)),
    )
}

fun semesterResultsExport(
    sessionLabel: String,
    semester: Int,
    roster: List<SessionStudent>,
    results: Map<String, SemesterGpa>,
): ExportDocument {
    val header = listOf("Roll", "Name", "GPA", "CGPA", "Result", "Position", "Supply courses", "Remarks")
    val rows = roster.sortedBy { it.rollNumber }.map { s ->
        val r = results[s.rollNumber]
        listOf(
            s.rollNumber, s.name, num(r?.gpa), num(r?.cgpa), r?.resultStatus?.let(::titleCase) ?: "Not recorded",
            r?.classPosition?.toString() ?: "", r?.supplyCourses?.joinToString(", ").orEmpty(), r?.remarks.orEmpty(),
        )
    }
    return ExportDocument(
        fileBase = "results_${sessionLabel}_sem$semester",
        title = listOf("Semester Results", "$sessionLabel · Semester $semester"),
        sections = listOf(ExportSection("Results", header, rows)),
    )
}

fun myStudentsExport(
    assignment: ResolvedAssignment,
    roster: List<SessionStudent>,
    tallies: Map<String, AttendanceTally>,
): ExportDocument {
    val header = listOf("Roll", "Name", "Student account", "Present", "Absent", "Leave", "Attendance %")
    val rows = roster.sortedBy { it.rollNumber }.map { s ->
        val t = tallies[s.rollNumber]
        listOf(
            s.rollNumber, s.name, s.linkedEmail.ifBlank { "Not linked" },
            (t?.present ?: 0).toString(), (t?.absent ?: 0).toString(), (t?.leave ?: 0).toString(),
            t?.let { pct(it.present, it.total) } ?: "-",
        )
    }
    return ExportDocument(
        fileBase = "students_${assignment.courseCode}_${assignment.sessionLabel}",
        title = listOf("Student Roster", "${assignment.courseCode} · ${assignment.subjectLabel}", assignment.sessionLabel),
        sections = listOf(ExportSection("Students", header, rows)),
    )
}

fun sessionFeesExport(session: AcademicSession?, departmentName: String?, structure: SessionFeeStructure): ExportDocument {
    val heads = structure.heads.map { listOf(it.label, num(it.amount, 0)) } + listOf(listOf("Total", num(structure.heads.sumOf { it.amount }, 0)))
    val details = listOfNotNull(
        listOf("Cadence", titleCase(structure.cadence.name)),
        structure.academicYear?.let { listOf("Academic year", it) },
        structure.dueDate?.let { listOf("Due date", it) },
        structure.lateFineNote?.let { listOf("Late fine", it) },
        structure.paymentNote?.let { listOf("Payment", it) },
    )
    return ExportDocument(
        fileBase = "fees_${structure.sessionId}",
        title = listOfNotNull("Session Fee Structure", departmentName, sessionTitle(session).ifBlank { null }),
        sections = listOf(ExportSection("Fee heads", listOf("Head", "Amount (PKR)"), heads), ExportSection("Details", listOf("Field", "Value"), details)),
    )
}

fun timetableExport(session: AcademicSession?, periods: List<SessionPeriod>): ExportDocument {
    val dayOrder = DayOfWeek.entries
    val header = listOf("Day", "Start", "End", "Course", "Subject", "Type", "Teacher", "Room", "Effective")
    val rows = periods.sortedWith(compareBy({ dayOrder.indexOf(it.day) }, { it.startTime })).map { p ->
        listOf(
            titleCase(p.day.name), p.startTime, p.endTime, p.courseCode, p.subjectName, titleCase(p.periodType.name), p.teacherName,
            listOfNotNull(p.building, p.roomNo).joinToString(" "),
            listOfNotNull(p.effectiveFrom?.toString(), p.effectiveTo?.toString()).joinToString(" to "),
        )
    }
    return ExportDocument(
        fileBase = "timetable_${session?.sessionId ?: "session"}",
        title = listOfNotNull("Class Timetable", sessionTitle(session).ifBlank { null }),
        sections = listOf(ExportSection("Timetable", header, rows)),
    )
}

fun datesheetExport(sheetLabel: String, sheet: Datesheet, slots: List<DatesheetSlot>): ExportDocument {
    val header = listOf("Date", "Start", "End", "Course", "Subject", "Venue", "Invigilator")
    val rows = slots.sortedWith(compareBy({ it.examDate ?: "9999" }, { it.startTime ?: "" })).map { s ->
        listOf(
            s.examDate ?: "Not scheduled", s.startTime ?: sheet.defaultStartTime.orEmpty(), s.endTime ?: sheet.defaultEndTime.orEmpty(),
            s.courseCode, s.subjectName, listOfNotNull(s.building, s.roomNo).joinToString(" "), s.invigilatorEmail.orEmpty(),
        )
    }
    return ExportDocument(
        fileBase = "datesheet_${sheet.sessionId}_sem${sheet.semester}",
        title = listOfNotNull("Datesheet", sheetLabel, if (sheet.published) "Published" else "Draft", sheet.instructions?.takeIf { it.isNotBlank() }),
        sections = listOf(ExportSection("Papers", header, rows)),
    )
}

fun insightsExport(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    sessionLabel: (String) -> String,
): ExportDocument = ExportDocument(
    fileBase = "insights_${LocalDate.now()}",
    title = listOf("Insights", "Generated ${LocalDate.now()}"),
    sections = listOf(
        ExportSection(
            "Sessions",
            listOf("Session", "Department", "Shift", "Semester", "Students", "Avg CGPA", "Avg attendance %"),
            overviews.map { listOf(sessionLabel(it.sessionId), it.deptId.uppercase(Locale.ROOT), titleCase(it.shift.name), it.currentSemester.toString(), it.students.toString(), num(it.avgCgpa), num(it.avgAttendance, 1)) },
        ),
        ExportSection(
            "At-risk students",
            listOf("Session", "Roll", "Name", "CGPA", "Attendance %"),
            atRisk.map { listOf(sessionLabel(it.sessionId), it.rollNumber, it.name, num(it.cgpa), num(it.attendance, 1)) },
        ),
        ExportSection(
            "Exam statistics",
            listOf("Session", "Semester", "Course", "Exam", "Entered", "Average", "Min", "Max", "Std dev", "Out of", "Pass rate %"),
            examStats.map {
                listOf(
                    sessionLabel(it.sessionId), it.semester.toString(), it.courseCode, titleCase(it.examType.name), it.entered.toString(),
                    num(it.avgScore, 1), it.minScore?.toString() ?: "-", it.maxScore?.toString() ?: "-", num(it.stddev, 1), it.outOf.toString(), num(it.passRate, 1),
                )
            },
        ),
    ),
)

fun teacherDirectoryExport(
    teachers: List<Teacher>,
    departments: List<Department>,
    assignments: Map<String, List<ResolvedAssignment>>,
): ExportDocument {
    val deptNames = departments.associate { it.deptId to it.name }
    val header = listOf("Name", "Email", "Phone", "Department", "Designation", "Qualification", "Office", "Status", "Role", "Classes")
    val rows = teachers.sortedBy { it.name.lowercase() }.map { t ->
        val role = listOfNotNull("Admin access".takeIf { t.isAdmin }, "HOD".takeIf { t.isHod }).joinToString(", ").ifBlank { "Teacher" }
        listOf(
            t.name, t.email, t.phone.orEmpty(), t.deptId?.let { deptNames[it] ?: it }.orEmpty(), t.designation.orEmpty(), t.qualification.orEmpty(),
            t.officeRoom.orEmpty(), titleCase(t.status.name), role,
            assignments[t.teacherId].orEmpty().joinToString("; ") { "${it.courseCode} (${it.sessionLabel})" },
        )
    }
    return ExportDocument("teachers_${LocalDate.now()}", listOf("Teacher Directory", "${teachers.size} teachers · ${LocalDate.now()}"), listOf(ExportSection("Teachers", header, rows)))
}

fun studentRosterExport(session: AcademicSession?, students: List<SessionStudent>): ExportDocument {
    val header = listOf("Roll", "Name", "Student account", "GPA", "CGPA")
    val rows = students.sortedBy { it.rollNumber }.map { s ->
        listOf(s.rollNumber, s.name, s.linkedEmail.ifBlank { "Not linked" }, num(s.gpa), num(s.cgpa))
    }
    return ExportDocument(
        fileBase = "roster_${session?.sessionId ?: "session"}",
        title = listOfNotNull("Student Roster", sessionTitle(session).ifBlank { null }, "${students.size} students"),
        sections = listOf(ExportSection("Students", header, rows)),
    )
}
