package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.StudentRecord
import com.mbd.cmscommon.export.ExportDocument
import com.mbd.cmscommon.export.ExportFormat
import com.mbd.cmscommon.export.studentRecordExport
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModWarn
import java.util.Locale

private fun recordPretty(raw: String): String = raw.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
private fun recordNum(value: Double?): String = value?.let { "%.2f".format(Locale.ENGLISH, it) } ?: "-"

@Composable
fun StudentRecordWorkspace(
    rollNumber: String,
    record: StudentRecord?,
    loading: Boolean,
    notFound: Boolean,
    errorMessage: String?,
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onRetry: () -> Unit,
    onClearError: () -> Unit,
    onExport: (ExportDocument, ExportFormat) -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(ModGround),
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { RecordHero(record, rollNumber, onBack, onEditProfile, onExport) }
        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, actionLabel = "Retry", onAction = onRetry, onDismiss = onClearError) }
        }
        when {
            loading && record == null -> items(4) { SkeletonRow() }
            notFound -> item { RecordCard("NOT FOUND") { Text("This student is not in the local roster. Refresh rosters and try again.", color = ModMuted) } }
            record != null -> {
                item { ProfileCard(record) }
                item { AttendanceCard(record) }
                item { MarksCard(record) }
                item { ResultsCard(record) }
                item { FeesCard(record) }
            }
        }
        item { Spacer(Modifier.height(72.dp)) }
    }
    }
}

@Composable
private fun RecordHero(record: StudentRecord?, rollNumber: String, onBack: () -> Unit, onEditProfile: () -> Unit, onExport: (ExportDocument, ExportFormat) -> Unit) {
    val p = record?.profile
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.fillMaxWidth().padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("‹ Students", color = CmsTheme.colors.onInk) }
                Spacer(Modifier.weight(1f))
                if (record != null) {
                    TextButton(onClick = onEditProfile) { Text("Edit profile", color = CmsTheme.colors.onInk) }
                    ExportMenuButton(onExport = { format -> onExport(studentRecordExport(record), format) }, tint = ModWarn)
                }
            }
            Column(Modifier.padding(start = 12.dp)) {
                Text("STUDENT RECORD", color = ModWarn, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Text(p?.name ?: "Roll $rollNumber", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Spacer(Modifier.height(4.dp))
                val subtitle = listOfNotNull(
                    p?.rollNumber ?: rollNumber,
                    record?.department?.name,
                    record?.session?.let { "${it.label} · ${p?.shift?.label ?: it.shiftMode.label} · Semester ${it.currentSemester}" },
                ).joinToString(" · ")
                Text(subtitle, color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
                if (p != null) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusBadge(p.enrollmentStatus.uppercase(), if (p.enrollmentStatus.equals("ACTIVE", ignoreCase = true)) BadgeTone.Success else BadgeTone.Neutral)
                        if (p.isCr) StatusBadge("CR", BadgeTone.Neutral)
                        if (p.isGr) StatusBadge("GR", BadgeTone.Neutral)
                        StatusBadge(if (p.linkedEmail.isBlank()) "NOT LINKED" else "ACCOUNT LINKED", if (p.linkedEmail.isBlank()) BadgeTone.Neutral else BadgeTone.Success)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordCard(title: String, trailing: String? = null, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), color = ModMuted, style = CmsTextStyles.eyebrow)
                if (trailing != null) Text(trailing, color = ModMuted, style = MaterialTheme.typography.labelSmall)
            }
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun FieldLine(label: String, value: String?) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, modifier = Modifier.width(140.dp), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value?.takeIf { it.isNotBlank() } ?: "-", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ProfileCard(record: StudentRecord) {
    val p = record.profile
    RecordCard("PROFILE", trailing = "${record.snapshot.completionPercent}% complete") {
        FieldLine("University roll", p.universityRollNo)
        FieldLine("Registration no", p.registrationNo)
        FieldLine("Father name", p.fatherName)
        FieldLine("Guardian", p.guardianName)
        FieldLine("CNIC / B-Form", p.cnicBform)
        FieldLine("Date of birth", p.dob)
        FieldLine("Gender", p.gender)
        FieldLine("Blood group", p.bloodGroup)
        FieldLine("Religion", p.religion)
        FieldLine("Domicile", p.domicile)
        FieldLine("Phone", p.phone)
        FieldLine("Guardian phone", p.guardianPhone)
        FieldLine("Personal email", p.personalEmail)
        FieldLine("Current address", p.currentAddress)
        FieldLine("Permanent address", p.permanentAddress)
        FieldLine("Admission date", p.admissionDate)
        FieldLine("Emergency contact", listOfNotNull(p.emergencyContactName, p.emergencyContactRelation?.let { "($it)" }, p.emergencyContactPhone).joinToString(" "))
        FieldLine("Special needs", p.specialNeeds)
        FieldLine("Student account", p.linkedEmail.ifBlank { "Not linked" })
        FieldLine("GPA / CGPA", "${recordNum(record.snapshot.validGpa)} / ${recordNum(record.snapshot.validCgpa)}")
    }
}

private fun percentColor(present: Int, total: Int): Color = when {
    total == 0 -> ModMuted
    present * 100 / total < 65 -> ModAccent
    else -> ModSuccess
}

@Composable
private fun AttendanceCard(record: StudentRecord) {
    val present = record.attendance.sumOf { it.present }
    val total = record.attendance.sumOf { it.total }
    RecordCard("ATTENDANCE", trailing = if (total == 0) "No classes recorded" else "${present * 100 / total}% overall") {
        if (record.attendance.isEmpty()) {
            Text("No attendance recorded yet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
        record.attendance.sortedBy { it.courseCode }.forEach { t ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t.courseCode, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(record.subjectNames[t.courseCode].orEmpty(), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                Text("P ${t.present} · A ${t.absent} · L ${t.leave}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.width(10.dp))
                Text(if (t.total == 0) "-" else "${t.present * 100 / t.total}%", color = percentColor(t.present, t.total), fontWeight = FontWeight.Bold)
            }
            HorizontalDivider(color = ModTrack)
        }
    }
}

@Composable
private fun MarksCard(record: StudentRecord) {
    RecordCard("MARKS") {
        if (record.marks.isEmpty()) {
            Text("No marks recorded yet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
        record.marks.groupBy { it.courseCode }.toSortedMap().forEach { (course, scores) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(course, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    Text(record.subjectNames[course].orEmpty(), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    scores.sortedBy { it.examType }.joinToString("   ") { s ->
                        "${recordPretty(s.examType.name)} " + if (s.wasAbsent) "Absent" else "${s.score}/${s.maxMarks}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HorizontalDivider(color = ModTrack)
        }
    }
}

@Composable
private fun ResultsCard(record: StudentRecord) {
    RecordCard("SEMESTER RESULTS") {
        if (record.results.isEmpty()) {
            Text("No semester results recorded yet.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        }
        record.results.forEach { r ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Semester ${r.semester}", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text("GPA ${recordNum(r.gpa)} · CGPA ${recordNum(r.cgpa)} · ${recordPretty(r.resultStatus)}", style = MaterialTheme.typography.bodySmall)
            }
            if (r.supplyCourses.isNotEmpty()) Text("Supply: ${r.supplyCourses.joinToString(", ")}", color = ModAccent, style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(color = ModTrack)
        }
    }
}

@Composable
private fun FeesCard(record: StudentRecord) {
    val structure = record.feeStructure
    RecordCard("FEES") {
        if (structure == null) {
            Text("No fee structure set for this session.", color = ModMuted, style = MaterialTheme.typography.bodyMedium)
        } else {
            structure.heads.forEach { FieldLine(it.label, "PKR %,.0f".format(Locale.ENGLISH, it.amount)) }
            FieldLine("Total (${recordPretty(structure.cadence.name)})", "PKR %,.0f".format(Locale.ENGLISH, structure.heads.sumOf { it.amount }))
            structure.dueDate?.let { FieldLine("Due date", it) }
        }
    }
}
