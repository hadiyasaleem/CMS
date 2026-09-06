package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.FeeHead
import com.mbd.cmscommon.domain.model.FeeType
import com.mbd.cmscommon.domain.model.SessionFeeStructure
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModGround
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import java.util.Locale

private val FeeGreen = ModSuccess
private val FeeGold = ModWarn
private val FeeRed = ModAccent

@Composable
fun SessionFeeWorkspace(
    sessionId: String,
    session: AcademicSession?,
    structure: SessionFeeStructure?,
    loading: Boolean,
    saving: Boolean,
    saved: Boolean,
    errorMessage: String?,
    onSave: (FeeType, List<FeeHead>, String, String, String, String) -> Unit,
    onConsumeSaved: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var initialized by remember { mutableStateOf(false) }
    var cadence by remember { mutableStateOf(FeeType.ANNUAL) }
    var heads by remember { mutableStateOf(listOf<FeeHead>()) }
    var academicYear by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("") }
    var lateFineNote by remember { mutableStateOf("") }
    var paymentNote by remember { mutableStateOf("") }
    var addingHead by remember { mutableStateOf(false) }
    var editingIndex by remember { mutableStateOf(-1) }
    var pendingRemoveIndex by remember { mutableStateOf(-1) }

    if (!initialized && structure != null) {
        cadence = structure.cadence
        heads = structure.heads
        academicYear = structure.academicYear ?: ""
        dueDate = structure.dueDate ?: ""
        lateFineNote = structure.lateFineNote ?: ""
        paymentNote = structure.paymentNote ?: ""
        initialized = true
    }

    val total = heads.sumOf { it.amount }
    val average = if (heads.isEmpty()) 0.0 else total / heads.size
    val dirty = structure == null || cadence != structure.cadence || heads != structure.heads ||
        academicYear != (structure.academicYear ?: "") || dueDate != (structure.dueDate ?: "") ||
        lateFineNote != (structure.lateFineNote ?: "") || paymentNote != (structure.paymentNote ?: "")

    val validationError = when {
        heads.isEmpty() -> "Add at least one fee head."
        heads.any { it.label.isBlank() } -> "Every fee head needs a label."
        heads.any { it.amount <= 0.0 } -> "Every fee amount must be greater than zero."
        heads.map { it.label.trim().lowercase(Locale.ROOT) }.distinct().size != heads.size -> "Fee-head labels must be unique."
        else -> null
    }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { FeeHero(session) }

        if (!errorMessage.isNullOrBlank()) {
            item { FeeNotice(errorMessage, FeeRed, "Dismiss", onClearError) }
        }
        if (saved) {
            item { FeeNotice("Fee structure saved", FeeGreen, "Dismiss", onConsumeSaved) }
        }

        item { FeeSummaryCard(total, average, cadence, onCadenceChange = { cadence = it }) }

        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Fee heads", modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                TextButton(onClick = { addingHead = true }) { Text("Add fee head") }
            }
        }

        if (heads.isEmpty()) {
            item { FeeHeadsEmptyState { addingHead = true } }
        } else {
            itemsIndexed(heads) { index, head ->
                FeeHeadCard(
                    head = head,
                    canMoveEarlier = index > 0,
                    canMoveLater = index < heads.lastIndex,
                    onMoveEarlier = { heads = heads.toMutableList().apply { add(index - 1, removeAt(index)) } },
                    onMoveLater = { heads = heads.toMutableList().apply { add(index + 1, removeAt(index)) } },
                    onEdit = { editingIndex = index },
                    onRemove = { pendingRemoveIndex = index },
                )
            }
        }

        item {
            PaymentDetailsCard(
                academicYear = academicYear, onAcademicYear = { academicYear = it },
                dueDate = dueDate, onDueDate = { dueDate = it },
                lateFineNote = lateFineNote, onLateFineNote = { lateFineNote = it },
                paymentNote = paymentNote, onPaymentNote = { paymentNote = it },
            )
        }

        item {
            FeeSaveCard(
                dirty = dirty,
                saving = saving,
                validationError = validationError,
                onSave = { onSave(cadence, heads, academicYear.trim(), dueDate.trim(), lateFineNote.trim(), paymentNote.trim()) },
            )
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (addingHead || editingIndex >= 0) {
        val existing = if (editingIndex >= 0) heads.getOrNull(editingIndex) else null
        FeeHeadEditorDialog(
            existing = existing,
            existingLabels = heads.filterIndexed { i, _ -> i != editingIndex }.map { it.label.trim().lowercase(Locale.ROOT) }.toSet(),
            onDismiss = { addingHead = false; editingIndex = -1 },
            onSave = { label, amount ->
                heads = if (editingIndex >= 0) {
                    heads.toMutableList().apply { this[editingIndex] = FeeHead(label, amount) }
                } else {
                    heads + FeeHead(label, amount)
                }
                addingHead = false
                editingIndex = -1
            },
        )
    }

    if (pendingRemoveIndex >= 0) {
        val head = heads.getOrNull(pendingRemoveIndex)
        if (head != null) {
            ConfirmDestructiveActionDialog(
                title = "Remove fee head",
                dependentSummary = "Removes the Rs ${head.amount} \"${head.label}\" charge from this structure.",
                onConfirm = { heads = heads.filterIndexed { i, _ -> i != pendingRemoveIndex }; pendingRemoveIndex = -1 },
                onDismiss = { pendingRemoveIndex = -1 },
            )
        }
    }
}

@Composable
private fun FeeHero(session: AcademicSession?) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("SESSION FEES", color = FeeGold, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Session fee structure", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(session?.label ?: "Session", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun FeeSummaryCard(total: Double, average: Double, cadence: FeeType, onCadenceChange: (FeeType) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeeMetric("Total", "Rs $total", Modifier.weight(1f))
                FeeMetric("Average", "Rs %.0f".format(average), Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Text("COLLECTION CADENCE", color = ModMuted, style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                CmsChip("Annual collection", selected = cadence == FeeType.ANNUAL, onClick = { onCadenceChange(FeeType.ANNUAL) })
                CmsChip("Per-semester collection", selected = cadence == FeeType.SEMESTER, onClick = { onCadenceChange(FeeType.SEMESTER) })
            }
        }
    }
}

@Composable
private fun FeeMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModGround) {
        Column(Modifier.padding(14.dp)) {
            Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
private fun FeeHeadCard(
    head: FeeHead,
    canMoveEarlier: Boolean,
    canMoveLater: Boolean,
    onMoveEarlier: () -> Unit,
    onMoveLater: () -> Unit,
    onEdit: () -> Unit,
    onRemove: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(head.label, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text("Rs ${head.amount}", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onMoveEarlier, enabled = canMoveEarlier) { Text("Up") }
            TextButton(onClick = onMoveLater, enabled = canMoveLater) { Text("Down") }
            TextButton(onClick = onEdit) { Text("Edit") }
            TextButton(onClick = onRemove) { Text("Remove", color = CmsTheme.colors.accent) }
        }
    }
}

@Composable
private fun FeeHeadsEmptyState(onAdd: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No fee heads configured", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Add tuition, laboratory, library, or other session charges.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = "Add fee head", onClick = onAdd)
        }
    }
}

@Composable
private fun PaymentDetailsCard(
    academicYear: String, onAcademicYear: (String) -> Unit,
    dueDate: String, onDueDate: (String) -> Unit,
    lateFineNote: String, onLateFineNote: (String) -> Unit,
    paymentNote: String, onPaymentNote: (String) -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("Payment details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = academicYear, onValueChange = onAcademicYear, label = { Text("Academic year (optional)") }, placeholder = { Text("2026-2027") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            CmsDateField(value = dueDate, onValueChange = onDueDate, label = "Due date", optional = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = lateFineNote, onValueChange = onLateFineNote, label = { Text("Late fine note (optional)") }, placeholder = { Text("Rs 50/day after due date") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(value = paymentNote, onValueChange = onPaymentNote, label = { Text("Payment instructions (optional)") }, placeholder = { Text("Payable at the college accounts office") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
        }
    }
}

@Composable
private fun FeeSaveCard(dirty: Boolean, saving: Boolean, validationError: String?, onSave: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text(if (dirty) "Unsaved fee changes" else "Fee structure is up to date", color = if (dirty) FeeGold else FeeGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(if (dirty) "Save to publish these details to students." else "No pending changes.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            if (validationError != null) {
                Spacer(Modifier.height(6.dp))
                Text(validationError, color = FeeRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            CmsPrimaryButton(text = if (saving) "Saving..." else "Save structure", onClick = onSave, enabled = dirty && validationError == null && !saving)
        }
    }
}

@Composable
private fun FeeNotice(message: String, color: Color, action: String, onAction: () -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(1.dp, color.copy(alpha = 0.25f))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = color, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onAction) { Text(action, color = color) }
        }
    }
}

@Composable
private fun FeeHeadEditorDialog(existing: FeeHead?, existingLabels: Set<String>, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var amount by remember { mutableStateOf(existing?.amount?.toString() ?: "") }

    val parsedAmount = amount.toDoubleOrNull()
    val duplicate = label.trim().lowercase(Locale.ROOT) in existingLabels
    val error = when {
        label.isBlank() -> null
        duplicate -> "This fee-head label already exists."
        parsedAmount == null || parsedAmount <= 0.0 -> "Amount must be greater than zero."
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Fee head" else "Edit fee head", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("Fee head") }, placeholder = { Text("Tuition fee") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Rs)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = FeeRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedAmount?.let { onSave(label.trim(), it) } }, enabled = label.isNotBlank() && error == null) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
