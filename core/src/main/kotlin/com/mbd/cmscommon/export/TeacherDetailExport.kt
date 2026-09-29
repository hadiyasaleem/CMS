package com.mbd.cmscommon.export

import com.mbd.cmscommon.controller.MasterGrid
import com.mbd.cmscommon.controller.TeacherWorkloadStats
import com.mbd.cmscommon.controller.formatWeeklyMinutes
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.teacher.ResolvedAssignment
import java.util.Locale

private fun titleCased(raw: String): String = raw.lowercase(Locale.ROOT).replace('_', ' ').replaceFirstChar { it.uppercase() }

private fun profileRows(teacher: Teacher, department: Department?): List<List<String>> = listOf(
    listOf("Name", teacher.name),
    listOf("Email", teacher.email),
    listOf("Phone", teacher.phone?.takeIf { it.isNotBlank() } ?: "-"),
    listOf("Department", department?.name ?: teacher.deptId.orEmpty().ifBlank { "-" }),
    listOf("Designation", teacher.designation?.takeIf { it.isNotBlank() } ?: "-"),
    listOf("Qualification", teacher.qualification?.takeIf { it.isNotBlank() } ?: "-"),
    listOf("Office room", teacher.officeRoom?.takeIf { it.isNotBlank() } ?: "-"),
    listOf("Status", titleCased(teacher.status.name)),
    listOf("Role", listOfNotNull("Admin access".takeIf { teacher.isAdmin }, "HOD".takeIf { teacher.isHod }).joinToString(", ").ifBlank { "Teacher" }),
)

private fun statsRows(stats: TeacherWorkloadStats): List<List<String>> = listOf(
    listOf("Periods per week", stats.totalPeriods.toString()),
    listOf("Weekly teaching time", formatWeeklyMinutes(stats.totalWeeklyMinutes)),
    listOf("Distinct subjects", stats.distinctSubjects.toString()),
    listOf("Distinct classes", stats.distinctClasses.toString()),
    listOf("Departments taught in", stats.distinctDepartments.toString()),
    listOf("Teaching days per week", stats.teachingDays.toString()),
    listOf("Busiest day", stats.busiestDay?.let { titleCased(it.name) } ?: "-"),
)

private fun classesRows(assignments: List<ResolvedAssignment>): List<List<String>> =
    assignments.sortedBy { it.courseCode }.map { listOf(it.courseCode, it.subjectLabel, it.sessionLabel, it.shift.ifBlank { "-" }) }

/** Profile + computed workload stats + the classes a teacher is assigned, with no timetable grids --
 * for the detail screen's "Export stats" button. */
fun teacherStatsExport(teacher: Teacher, department: Department?, stats: TeacherWorkloadStats, assignments: List<ResolvedAssignment>): ExportDocument = ExportDocument(
    fileBase = "teacher_${teacher.teacherId}_stats",
    title = listOf("Teacher Profile & Stats", teacher.name),
    sections = listOf(
        ExportSection("Profile", listOf("Field", "Value"), profileRows(teacher, department)),
        ExportSection("Workload", listOf("Metric", "Value"), statsRows(stats)),
        ExportSection("Classes", listOf("Course", "Subject", "Session", "Shift"), classesRows(assignments)),
    ),
)

/** Everything on the teacher detail screen in one document: profile + stats + classes, followed by
 * one printed-style grid section per semester+shift the teacher has periods in -- the detail screen's
 * "Export all" button. [breakSlots], keyed by grid title, carries each grid's own break column through. */
fun teacherDetailExport(
    teacher: Teacher,
    department: Department?,
    stats: TeacherWorkloadStats,
    assignments: List<ResolvedAssignment>,
    grids: List<MasterGrid>,
    breakSlots: Map<String, Pair<String, String>?> = emptyMap(),
): ExportDocument {
    val statsDoc = teacherStatsExport(teacher, department, stats, assignments)
    val gridSections = grids.map { grid -> ExportSection(grid.title, emptyList(), emptyList(), grid = masterGridLayout(grid, breakSlots[grid.title])) }
    return ExportDocument(
        fileBase = "teacher_${teacher.teacherId}_full",
        title = statsDoc.title,
        sections = statsDoc.sections + gridSections,
    )
}
