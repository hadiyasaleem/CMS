package com.mbd.cmscommon.export

import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.util.clockDisplay
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.AtRiskStudent
import com.mbd.cmscommon.domain.model.AttendanceExportPayload
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.AttendanceTally
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.ExamStat
import com.mbd.cmscommon.domain.model.ExamType
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.domain.model.SessionOverview
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentTermAttendance
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.attendanceCounts
import com.mbd.cmscommon.domain.model.distinctTaughtTopics
import com.mbd.cmscommon.domain.model.isRegisterHoliday
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

private val MONTH = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)
private val WEEKDAY = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH)

private fun pct(value: Int, total: Int): String = if (total == 0) "-" else "${(value * 100) / total}%"
private fun num(value: Double?, digits: Int = 2): String = value?.let { "%.${digits}f".format(Locale.ENGLISH, it) } ?: "-"
private fun letter(status: AttendanceStatus): String = status.name.take(1)
private fun titleCase(raw: String): String = raw.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
/** "IT 2022–2026 · Evening · Semester 3"; without [shift] the session's shifts ("Morning & Evening") are shown. */
fun sessionTitle(session: AcademicSession?, shift: Session? = null): String =
    session?.let { "${it.deptId.uppercase(Locale.ROOT)} ${it.label} · ${shift?.label ?: it.shiftMode.label} · Semester ${it.currentSemester}" } ?: ""

fun AttendanceExportPayload.toExportDocument() = singleSectionDocument(fileBase, title, header, rows)

/** Names shown in the register's title block; blank values are left out. */
data class RegisterContext(
    val departmentName: String? = null,
    val subjectName: String? = null,
    val teacherName: String? = null,
)

/**
 * Teacher monthly register: one column per day, Sundays (holidays) as solid black columns, totals at
 * the end. The title carries department, semester, shift, subject, teacher, month and the lecture
 * topics the teacher entered while marking attendance that month.
 */
fun attendanceRegisterExport(
    courseCode: String,
    session: AcademicSession?,
    month: YearMonth,
    roster: List<SessionStudent>,
    marks: Map<String, Map<LocalDate, DailyAttendanceMark>>,
    context: RegisterContext = RegisterContext(),
    /** The class's shift: a register belongs to one shift's students. Null prints the session's shifts. */
    shift: Session? = null,
): ExportDocument {
    val days = (1..month.lengthOfMonth()).map(month::atDay)
    val datesWithMarks = marks.values.flatMap { it.keys }.toSet()
    val holidayColumns = days.withIndex().filter { (_, d) -> isRegisterHoliday(d, datesWithMarks) }.map { 2 + it.index }.toSet()
    val header = listOf("Roll", "Name") + days.map { it.dayOfMonth.toString().padStart(2, '0') } + listOf("P", "A", "L", "Late", "%")
    val rows = roster.sortedWith(compareBy({ it.sessionId }, { it.rollNumber })).map { student ->
        val byDate = marks[student.id].orEmpty()
        val counts = attendanceCounts(byDate.values)
        listOf(student.rollNumber, student.name) +
            days.map { d -> if (isRegisterHoliday(d, datesWithMarks)) "" else byDate[d]?.let { letter(it.status) + if (it.isLate) "*" else "" } ?: "" } +
            listOf(counts.present.toString(), counts.absent.toString(), counts.leave.toString(), counts.late.toString(), pct(counts.present, counts.total))
    }

    // A topic taught on several days is listed once, in the order it was first taught.
    val topics = distinctTaughtTopics(
        marks.values.flatMap { it.values }.filter { !it.lectureTopic.isNullOrBlank() }.sortedBy { it.date }.map { it.lectureTopic },
    )

    val subject = listOfNotNull(context.subjectName?.takeIf { it.isNotBlank() }, courseCode.takeIf { it.isNotBlank() }?.let { "($it)" }).joinToString(" ")
    val title = buildList {
        add("Attendance Register - ${month.format(MONTH)}")
        add(listOfNotNull(
            context.departmentName?.takeIf { it.isNotBlank() }?.let { "Department: $it" },
            session?.let { "Semester: ${it.currentSemester}" },
            (shift?.label ?: session?.shiftMode?.label)?.let { "Shift: $it" },
        ).joinToString(" | "))
        add(listOfNotNull(
            "Subject: $subject".takeIf { subject.isNotBlank() },
            context.teacherName?.takeIf { it.isNotBlank() }?.let { "Teacher: $it" },
        ).joinToString(" | "))
        add("P present | A absent | L leave | * late | black column = holiday")
        if (topics.isNotEmpty()) {
            addAll(wrapLine("Topics covered: " + topics.joinToString(", "), 120))
        }
    }.filter { it.isNotBlank() }

    return ExportDocument(
        fileBase = listOfNotNull("attendance", courseCode, shift?.name?.lowercase(Locale.ROOT), month.toString()).joinToString("_"),
        title = title,
        sections = listOf(ExportSection("Register", header, rows, blackColumns = holidayColumns)),
    )
}

/** Greedy word wrap so long titles/topics stay inside the PDF page width. */
internal fun wrapLine(text: String, maxChars: Int): List<String> {
    if (text.length <= maxChars) return listOf(text)
    val lines = mutableListOf<String>()
    var current = StringBuilder()
    for (word in text.split(' ')) {
        if (current.isNotEmpty() && current.length + 1 + word.length > maxChars) {
            lines += current.toString()
            current = StringBuilder()
        }
        if (current.isNotEmpty()) current.append(' ')
        current.append(word)
    }
    if (current.isNotEmpty()) lines += current.toString()
    return lines
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
    val rows = roster.sortedWith(compareBy({ it.sessionId }, { it.rollNumber })).map { s ->
        val absent = s.id in absentRolls
        val score = scores[s.id].orEmpty()
        listOf(s.rollNumber, s.name, if (absent) "" else score, examType.maxMarks.toString(), when {
            absent -> "Absent"
            score.isBlank() -> "Not entered"
            else -> "Entered"
        })
    }
    return ExportDocument(
        fileBase = listOfNotNull("marks", assignment.courseCode, assignment.classShift?.name?.lowercase(Locale.ROOT), examType.name, if (assignment.isMerged) "combined" else null).joinToString("_"),
        title = listOf("Marks Sheet", "${assignment.courseCode} · ${assignment.subjectLabel}", "${assignment.sessionLabel}${if (assignment.isMerged) " (combined)" else ""} · ${titleCase(examType.name)} (out of ${examType.maxMarks})"),
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
    val header = listOf("Roll", "Name", "Shift", "Student account", "Present", "Absent", "Leave", "Attendance %")
    val rows = roster.sortedWith(compareBy({ it.sessionId }, { it.rollNumber })).map { s ->
        val t = tallies[s.id]
        listOf(
            s.rollNumber, s.name, s.shift.label, s.linkedEmail.ifBlank { "Not linked" },
            (t?.present ?: 0).toString(), (t?.absent ?: 0).toString(), (t?.leave ?: 0).toString(),
            t?.let { pct(it.present, it.total) } ?: "-",
        )
    }
    return ExportDocument(
        fileBase = "students_${assignment.courseCode}_${assignment.sessionLabel}${if (assignment.isMerged) "_combined" else ""}",
        title = listOf("Student Roster", "${assignment.courseCode} · ${assignment.subjectLabel}", assignment.sessionLabel + if (assignment.isMerged) " (combined)" else ""),
        sections = listOf(ExportSection("Students", header, rows)),
    )
}

/** One pair of sections (heads, details) per shift -- each shift has its own plan. */
fun sessionFeesExport(session: AcademicSession?, departmentName: String?, structures: List<SessionFeeStructure>): ExportDocument {
    val ordered = structures.sortedBy { it.shift }
    val multiShift = ordered.size > 1 || (session?.shifts?.size ?: 1) > 1
    val sections = ordered.flatMap { structure ->
        val prefix = if (multiShift) "${structure.shift.label} " else ""
        val heads = structure.heads.map { listOf(it.label, num(it.amount, 0)) } + listOf(listOf("Total", num(structure.heads.sumOf { it.amount }, 0)))
        val details = listOfNotNull(
            listOf("Shift", structure.shift.label),
            listOf("Cadence", titleCase(structure.cadence.name)),
            structure.academicYear?.let { listOf("Academic year", it) },
            structure.dueDate?.let { listOf("Due date", it) },
            structure.paymentNote?.let { listOf("Payment", it) },
        )
        listOf(ExportSection("${prefix}fee heads".replaceFirstChar { it.uppercase() }, listOf("Head", "Amount (PKR)"), heads), ExportSection("${prefix}details".replaceFirstChar { it.uppercase() }, listOf("Field", "Value"), details))
    }
    return ExportDocument(
        fileBase = "fees_${session?.sessionId ?: ordered.firstOrNull()?.sessionId ?: "session"}",
        title = listOfNotNull("Session Fee Structure", departmentName, sessionTitle(session).ifBlank { null }),
        sections = sections,
    )
}

private val StudentGridDays = listOf(
    DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY,
)

/** Same day-rows x time-slot-columns grid the student app shows on screen, rather than a flat
 * per-period list: only lectures (matching the on-screen filter) are placed, one row per weekday. */
fun studentGridTimetableExport(periods: List<SessionPeriod>): ExportDocument {
    val lectures = periods.filter { it.periodType == PeriodType.LECTURE && it.courseCode.isNotBlank() }
    val timeSlots = lectures.map { clockDisplay(it.startTime) to clockDisplay(it.endTime) }.distinct().sortedBy { it.first }
    val header = listOf("Day") + timeSlots.map { "${it.first}-${it.second}" }
    val byDayAndSlot = lectures.associateBy { it.day to (clockDisplay(it.startTime) to clockDisplay(it.endTime)) }
    val rows = StudentGridDays.map { day ->
        listOf(titleCase(day.name)) + timeSlots.map { slot ->
            byDayAndSlot[day to slot]?.let { p ->
                listOfNotNull(p.subjectName, p.teacherLabel.ifBlank { null }, listOfNotNull(p.building, p.roomNo).joinToString(" ").ifBlank { null }).joinToString(" - ")
            } ?: ""
        }
    }
    return ExportDocument(
        fileBase = "my_timetable_${LocalDate.now()}",
        title = listOf("My Timetable"),
        sections = listOf(ExportSection("Timetable", header, rows)),
    )
}

fun timetableExport(session: AcademicSession?, periods: List<SessionPeriod>, shift: Session? = null): ExportDocument {
    val dayOrder = DayOfWeek.entries
    val header = listOf("Day", "Start", "End", "Course", "Subject", "Type", "Teacher", "Room (if different)", "Effective")
    // The class's usual room is stated once in the title; a period only repeats its room when it is held elsewhere.
    val usualRoom = periods.filter { it.periodType != PeriodType.BREAK }
        .mapNotNull { listOfNotNull(it.building, it.roomNo).joinToString(" ").ifBlank { null } }
        .groupingBy { it }.eachCount().let { counts -> counts.values.maxOrNull()?.let { best -> counts.entries.first { it.value == best }.key } }
    val rows = periods.sortedWith(compareBy({ dayOrder.indexOf(it.day) }, { it.startTime })).map { p ->
        listOf(
            titleCase(p.day.name), clockDisplay(p.startTime), clockDisplay(p.endTime), p.courseCode, p.subjectName, titleCase(p.periodType.name), p.teacherLabel,
            listOfNotNull(p.building, p.roomNo).joinToString(" ").takeIf { it != usualRoom }.orEmpty(),
            listOfNotNull(p.effectiveFrom?.toString(), p.effectiveTo?.toString()).joinToString(" to "),
        )
    }
    return ExportDocument(
        fileBase = listOfNotNull("timetable", session?.sessionId ?: "session", shift?.name?.lowercase(Locale.ROOT)).joinToString("_"),
        title = listOfNotNull("Class Timetable", sessionTitle(session, shift).ifBlank { null }, usualRoom?.let { "Room: $it" }),
        sections = listOf(ExportSection("Timetable", header, rows)),
    )
}

fun insightsExport(
    overviews: List<SessionOverview>,
    atRisk: List<AtRiskStudent>,
    examStats: List<ExamStat>,
    sessionLabel: (String) -> String,
    /** The active Department -> Session -> Shift filter, e.g. "Information Technology · 2022–2026 · Evening shift". */
    scopeTitle: String? = null,
): ExportDocument = ExportDocument(
    fileBase = "insights_${LocalDate.now()}",
    title = listOfNotNull("Insights", scopeTitle, "Generated ${LocalDate.now()}"),
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

fun studentRosterExport(session: AcademicSession?, students: List<SessionStudent>, shift: Session? = null): ExportDocument {
    val header = listOf("Roll", "Name", "Shift", "Student account", "GPA", "CGPA")
    val rows = students.sortedBy { it.rollNumber }.map { s ->
        listOf(s.rollNumber, s.name, s.shift.label, s.linkedEmail.ifBlank { "Not linked" }, num(s.gpa), num(s.cgpa))
    }
    return ExportDocument(
        fileBase = "roster_${session?.sessionId ?: "session"}" + (shift?.let { "_${it.name.lowercase(Locale.ROOT)}" } ?: ""),
        title = listOfNotNull(
            "Student Roster",
            sessionTitle(session).ifBlank { null },
            shift?.let { "${it.label} shift" },
            "${students.size} students",
        ),
        sections = listOf(ExportSection("Students", header, rows)),
    )
}

fun studentDirectoryExport(rows: List<com.mbd.cmscommon.controller.StudentDirectoryRow>, filtered: Boolean, scopeTitle: String? = null): ExportDocument {
    val header = listOf(
        "Roll", "Name", "Department", "Session", "Shift", "Semester", "Status", "University roll", "Registration no",
        "Father name", "Gender", "Phone", "Student account", "GPA", "CGPA",
    )
    val body = rows.map { r ->
        val p = r.profile
        listOf(
            p.rollNumber, p.name, r.departmentName ?: r.session?.deptId?.uppercase(Locale.ROOT).orEmpty(), r.session?.label.orEmpty(),
            p.shift.label, r.session?.currentSemester?.toString().orEmpty(), titleCase(p.enrollmentStatus),
            p.universityRollNo.orEmpty(), p.registrationNo.orEmpty(), p.fatherName.orEmpty(), p.gender.orEmpty(), p.phone.orEmpty(),
            p.linkedEmail.ifBlank { "Not linked" }, num(p.gpa), num(p.cgpa),
        )
    }
    return ExportDocument(
        fileBase = "students_${LocalDate.now()}",
        title = listOfNotNull("Student Directory", scopeTitle, "${rows.size} students${if (filtered) " (filtered)" else ""} · ${LocalDate.now()}"),
        sections = listOf(ExportSection("Students", header, body)),
    )
}

fun studentRecordExport(record: com.mbd.cmscommon.controller.StudentRecord): ExportDocument {
    val p = record.profile
    fun field(label: String, value: String?) = listOf(label, value?.takeIf { it.isNotBlank() } ?: "-")
    val profile = listOf(
        field("Name", p.name), field("Roll number", p.rollNumber), field("University roll", p.universityRollNo),
        field("Registration no", p.registrationNo), field("Department", record.department?.name ?: record.session?.deptId?.uppercase(Locale.ROOT)),
        field("Session", sessionTitle(record.session)), field("Shift", p.shift.label), field("Enrollment status", titleCase(p.enrollmentStatus)),
        field("Class representative", listOfNotNull("CR".takeIf { p.isCr }, "GR".takeIf { p.isGr }).joinToString(", ").ifBlank { "No" }),
        field("Father name", p.fatherName), field("Guardian", p.guardianName), field("CNIC / B-Form", p.cnicBform),
        field("Date of birth", p.dob), field("Gender", p.gender), field("Blood group", p.bloodGroup), field("Religion", p.religion),
        field("Domicile", p.domicile), field("Phone", p.phone), field("Guardian phone", p.guardianPhone), field("Personal email", p.personalEmail),
        field("Current address", p.currentAddress), field("Permanent address", p.permanentAddress), field("Admission date", p.admissionDate),
        field("Emergency contact", listOfNotNull(p.emergencyContactName, p.emergencyContactRelation?.let { "($it)" }, p.emergencyContactPhone).joinToString(" ")),
        field("Special needs", p.specialNeeds), field("Student account", p.linkedEmail.ifBlank { "Not linked" }),
        field("GPA", num(record.snapshot.validGpa)), field("CGPA", num(record.snapshot.validCgpa)),
        field("Profile completion", "${record.snapshot.completionPercent}%"),
    )
    val attendance = record.attendance.sortedBy { it.courseCode }.map { t ->
        listOf(t.courseCode, record.subjectNames[t.courseCode].orEmpty(), t.total.toString(), t.present.toString(), t.absent.toString(), t.leave.toString(), pct(t.present, t.total))
    }
    val marks = record.marks.sortedWith(compareBy({ it.courseCode }, { it.examType })).map { m ->
        listOf(m.courseCode, record.subjectNames[m.courseCode].orEmpty(), titleCase(m.examType.name), if (m.wasAbsent) "Absent" else m.score.toString(), m.maxMarks.toString(), m.remarks.orEmpty())
    }
    val results = record.results.map { r ->
        listOf(r.semester.toString(), r.termLabel.orEmpty(), num(r.gpa), num(r.cgpa), titleCase(r.resultStatus), r.classPosition?.toString().orEmpty(), r.supplyCourses.joinToString(", "))
    }
    val fees = buildList {
        record.feeStructure?.let { s ->
            s.heads.forEach { add(listOf("Fee", it.label, num(it.amount, 0), "")) }
            add(listOf("Fee", "Total (${titleCase(s.cadence.name)})", num(s.heads.sumOf { it.amount }, 0), s.dueDate?.let { "Due $it" }.orEmpty()))
        }
    }
    return ExportDocument(
        fileBase = "student_${p.rollNumber}",
        title = listOf("Student Record", "${p.name} · ${p.rollNumber}", sessionTitle(record.session), "Generated ${LocalDate.now()}"),
        sections = listOf(
            ExportSection("Profile", listOf("Field", "Value"), profile),
            ExportSection("Attendance", listOf("Course", "Subject", "Classes", "Present", "Absent", "Leave", "%"), attendance),
            ExportSection("Marks", listOf("Course", "Subject", "Exam", "Score", "Out of", "Remarks"), marks),
            ExportSection("Results", listOf("Semester", "Term", "GPA", "CGPA", "Result", "Position", "Supply courses"), results),
            ExportSection("Fees", listOf("Type", "Item", "Amount (PKR)", "Note"), fees),
        ),
    )
}

/**
 * Looks up the department, subject and teacher names for a register title. Each lookup is best-effort:
 * a missing name is simply left out of the title rather than failing the export.
 */
suspend fun resolveRegisterContext(
    session: AcademicSession?,
    courseCode: String,
    departments: com.mbd.cmscommon.domain.repository.DepartmentRepository,
    curriculum: com.mbd.cmscommon.domain.repository.CurriculumRepository,
    timetable: com.mbd.cmscommon.domain.repository.SessionTimetableRepository,
    shift: Session? = null,
): RegisterContext {
    if (session == null) return RegisterContext()
    // Best-effort enrichment: the report is still complete without the department/subject/period names, which fall back to codes.
    val department = runCatching { departments.getDepartment(session.deptId)?.name }.getOrNull()
    val subject = runCatching {
        curriculum.observeSemesterSubjects(session.sessionId, session.currentSemester).first().firstOrNull { it.courseCode == courseCode }?.name
    }.getOrNull()
    val period = runCatching {
        // The teacher of this shift's class; each shift can have its own teacher for the subject.
        timetable.observeWeek(session.sessionId).first()
            .firstOrNull { it.courseCode == courseCode && it.teacherLabel.isNotBlank() && (shift == null || it.shift == shift) }
    }.getOrNull()
    return RegisterContext(department, subject ?: period?.subjectName, period?.teacherLabel?.ifBlank { null })
}
