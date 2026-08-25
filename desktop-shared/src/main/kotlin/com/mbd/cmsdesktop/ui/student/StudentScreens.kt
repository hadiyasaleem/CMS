package com.mbd.cmsdesktop.ui.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StudentAttendanceController
import com.mbd.cmscommon.controller.StudentHomeController
import com.mbd.cmscommon.controller.SubjectAttendanceRow
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionAttendanceRepository
import com.mbd.cmscommon.domain.repository.SessionFeeRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmscommon.domain.repository.SessionTimetableRepository
import com.mbd.cmscommon.ui.components.CmsCard
import com.mbd.cmscommon.ui.components.RingGauge
import com.mbd.cmscommon.ui.components.SectionHeader
import com.mbd.cmscommon.ui.theme.CmsTheme

/**
 * Superseded by the `*WorkspaceScreen`/`*Screen` files in this package (`StudentHomeWorkspaceScreen`,
 * `StudentMarksScreen`, etc.), which [StudentNavHost] actually wires up. Kept only because it's a
 * distinct real file in the decompiled build - it is not referenced from `StudentNavHost` and is not
 * part of the app's live navigation graph, so its bodies are reconstructed here at lower visual
 * fidelity than the wired screens (plain cards/text instead of the full hero-card layouts).
 */
@Composable
fun StudentHomeScreen(
    sessionId: String,
    rollNumber: String,
    sessionRepository: AcademicSessionRepository,
    attendanceRepository: SessionAttendanceRepository,
    timetableRepository: SessionTimetableRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentHomeController(sessionId, rollNumber, sessionRepository, attendanceRepository, timetableRepository, scope)
    }
    val me by controller.me.collectAsState()
    val session by controller.session.collectAsState()
    val ui by controller.ui.collectAsState()

    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { SectionHeader("Home", "GGC-MBD", "Roll $rollNumber") }
        item {
            CmsCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RingGauge(ui.overallPercent, caption = "Attendance")
                        Spacer(Modifier.height(16.dp))
                        Column(Modifier.weight(1f)) {
                            StandingRow("CGPA / GPA", me?.cgpa?.let { c -> "%.2f / %s".format(c, me?.gpa?.let { "%.2f".format(it) } ?: "-") } ?: "No CGPA yet")
                            StandingRow("Semester", session?.currentSemester?.let { "$it of 8" } ?: "-")
                            StandingRow("Subjects", ui.subjectCount.toString())
                            StandingRow("Attendance", "${ui.overallPercent.toInt()}%")
                        }
                    }
                }
            }
        }
        item {
            CmsCard(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("NEXT CLASS", color = CmsTheme.colors.accent, style = MaterialTheme.typography.labelMedium)
                    Text(ui.nextClass?.let { "${it.courseCode} - ${it.timeRange}" } ?: "No more classes today", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
private fun StandingRow(label: String, value: String, last: Boolean = false) {
    Column {
        Row(Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.bodyMedium)
        }
        if (!last) Divider(Modifier.padding(vertical = 4.dp))
    }
}

@Composable
private fun attendanceColor(percent: Float): Color = when {
    percent >= 80f -> CmsTheme.colors.success
    percent >= 75f -> Color(0xFF9A651B)
    else -> Color(0xFFB43A31)
}

@Composable
fun StudentAttendanceScreen(
    sessionId: String,
    rollNumber: String,
    attendanceRepository: SessionAttendanceRepository,
    curriculumRepository: CurriculumRepository,
) {
    val scope = rememberCoroutineScope()
    val controller = remember(sessionId, rollNumber) {
        StudentAttendanceController(sessionId, rollNumber, attendanceRepository, curriculumRepository, scope)
    }
    val rows by controller.rows.collectAsState()

    LazyColumn(contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { SectionHeader("Attendance", "GGC-MBD", "Roll $rollNumber") }
        items(rows) { row -> SubjectAttendanceCard(row) }
    }
}

@Composable
private fun SubjectAttendanceCard(row: SubjectAttendanceRow) {
    CmsCard(Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(row.subjectName, style = MaterialTheme.typography.titleSmall)
                Text("${row.present}/${row.total} lectures", color = CmsTheme.colors.muted, style = MaterialTheme.typography.bodySmall)
            }
            Text("${row.percentage.toInt()}%", color = attendanceColor(row.percentage), style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun LegacyStudentMarksScreen(
    sessionId: String,
    rollNumber: String,
    marksRepository: SessionMarksRepository,
    curriculumRepository: CurriculumRepository,
) = StudentMarksScreen(sessionId, rollNumber, marksRepository, curriculumRepository)

@Composable
fun LegacyStudentResultsScreen(
    sessionId: String,
    rollNumber: String,
    marksRepository: SessionMarksRepository,
) = StudentResultsScreen(sessionId, rollNumber, marksRepository)

@Composable
fun LegacyStudentTimetableScreen(
    sessionId: String,
    timetableRepository: SessionTimetableRepository,
) = StudentTimetableScreen(sessionId, timetableRepository)

@Composable
private fun PeriodRow(period: SessionPeriod) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Text(period.startTime, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
        Text(period.courseCode, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LegacyStudentFeeChallanScreen(
    sessionId: String,
    rollNumber: String,
    feeRepository: SessionFeeRepository,
) = StudentFeeChallanScreen(sessionId, rollNumber, feeRepository)
