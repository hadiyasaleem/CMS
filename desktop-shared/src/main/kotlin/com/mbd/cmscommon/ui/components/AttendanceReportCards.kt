package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AttendanceMonthRate
import com.mbd.cmscommon.domain.model.AttendanceStudentSummary
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.attendanceStudentSummaries
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AttendanceStudentReportCards(
    marks: List<DailyAttendanceMark>,
    roster: List<SessionStudent>,
    months: List<YearMonth> = emptyList(),
    modifier: Modifier = Modifier,
) {
    val summaries = attendanceStudentSummaries(marks, roster, months)
    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(summaries, key = { it.rollNumber }) { student -> AttendanceStudentReportCard(student) }
    }
}

@Composable
private fun AttendanceStudentReportCard(student: AttendanceStudentSummary, modifier: Modifier = Modifier) {
    val risk = student.belowTarget
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, if (risk) Color(0xFFE7B9B1) else Color(0xFFE3E0D9)),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f)) {
                    Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Roll ${student.rollNumber}", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (risk) Color(0xFFFFECE9) else Color(0xFFE8F2EA),
                ) {
                    Text(
                        student.percentage?.let { "$it%" } ?: "--",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        color = if (risk) Color(0xFF9A3E38) else Color(0xFF2F6B4F),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { (student.percentage ?: 0) / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = if (risk) Color(0xFFB9574F) else Color(0xFF3E7A5C),
                trackColor = Color(0xFFEDEAE4),
            )
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AttendanceCount("PRESENT", student.present, Color(0xFF2F6B4F))
                AttendanceCount("ABSENT", student.absent, Color(0xFF9A3E38))
                AttendanceCount("LEAVE", student.leave, Color(0xFF9A651B))
                AttendanceCount("LATE", student.late, Color(0xFF24577A))
            }
            if (student.monthlyRates.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    student.monthlyRates.forEach { rate -> MonthRatePill(rate) }
                }
            }
        }
    }
}

@Composable
private fun AttendanceCount(label: String, value: Int, color: Color) {
    Column {
        Text(value.toString(), color = color, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label, color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun MonthRatePill(rate: AttendanceMonthRate) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFF5F3EE)) {
        val displayName = rate.month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
        val percentage = rate.percentage?.let { "$it%" } ?: "--"
        Text("$displayName $percentage", modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
    }
}
