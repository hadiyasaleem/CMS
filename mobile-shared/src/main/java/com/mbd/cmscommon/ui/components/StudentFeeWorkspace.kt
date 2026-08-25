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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.mbd.cmscommon.domain.model.FeeDueState
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.StudentFeeSnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import java.time.format.DateTimeFormatter

private val FeeCanvas = Color(0xFFF7F5F0)
private val FeeGreen = Color(0xFF2F6B4F)
private val FeeGold = Color(0xFF9A651B)
private val FeeRed = Color(0xFFB43A31)
private val FeeBlue = Color(0xFF24577A)
private val FeeDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy")

@Composable
fun StudentFeeWorkspace(
    snapshot: StudentFeeSnapshot?,
    loading: Boolean,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth().background(FeeCanvas),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item { FeeHeader() }

        when {
            loading && snapshot == null -> items(3) { SkeletonRow() }
            !errorMessage.isNullOrBlank() -> item { FeeErrorCard(errorMessage, onRetry) }
            snapshot != null -> {
                item { FeeOverview(snapshot) }
                item { FeeDueCard(snapshot) }
                if (snapshot.structure != null && snapshot.itemCount > 0) {
                    item {
                        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Fee component", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                snapshot.structure.heads.filter { it.label.isNotBlank() && it.amount >= 0.0 }.forEach { head -> FeeHeadCard(head) }
                            }
                        }
                    }
                }
                item { FeeGuidanceCards(snapshot) }
            }
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun FeeHeader() {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFF252321)) {
        Column(Modifier.padding(20.dp)) {
            Text("FINANCIALS", color = FeeGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Fee challan", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("Your session's published fee structure and payment guidance", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeeOverview(snapshot: StudentFeeSnapshot) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
            Column(Modifier.padding(14.dp)) {
                Text("Rs ${snapshot.totalAmount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("TOTAL PUBLISHED FEE", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            }
        }
        Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
            Column(Modifier.padding(14.dp)) {
                Text(snapshot.itemCount.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(if (snapshot.itemCount == 1) "COMPONENT" else "COMPONENTS", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            }
        }
    }
    snapshot.largestHead?.let { head ->
        Spacer(Modifier.height(8.dp))
        Text("Largest component: ${head.label} (Rs ${head.amount})", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun FeeDueCard(snapshot: StudentFeeSnapshot) {
    val (color, message) = when (snapshot.dueState) {
        FeeDueState.NOT_SET -> FeeBlue to "The college has not published a due date."
        FeeDueState.INVALID -> FeeGold to "The published due date could not be interpreted. Confirm it with the accounts office."
        FeeDueState.UPCOMING -> FeeGreen to "Due in ${snapshot.daysUntilDue} day(s)."
        FeeDueState.DUE_TODAY -> FeeGold to "Due today. Confirm payment timing with the accounts office."
        FeeDueState.OVERDUE -> FeeRed to "The published due date passed ${-(snapshot.daysUntilDue ?: 0)} day(s) ago."
    }
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, color.copy(alpha = 0.3f))) {
        Column(Modifier.padding(16.dp)) {
            Text("PAYMENT DUE DATE", color = Color(0xFF77716A), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text(snapshot.dueDate?.format(FeeDateFormat) ?: "Not specified", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(message, color = color, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeeHeadCard(head: FeeHead) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(head.label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text("Rs ${head.amount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun FeeGuidanceCards(snapshot: StudentFeeSnapshot) {
    Surface(shape = RoundedCornerShape(16.dp), color = Color.White, border = BorderStroke(1.dp, Color(0xFFE5E0D7))) {
        Column(Modifier.padding(16.dp)) {
            Text("Payment guidance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(snapshot.structure?.paymentNote?.takeIf { it.isNotBlank() } ?: "Not specified", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Text("Late fee policy", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            Text(snapshot.structure?.lateFineNote?.takeIf { it.isNotBlank() } ?: "Not specified", color = Color(0xFF77716A), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun FeeErrorCard(message: String, onRetry: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = FeeRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, FeeRed.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = FeeRed, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onRetry) { Text("Retry", color = FeeRed) }
        }
    }
}
