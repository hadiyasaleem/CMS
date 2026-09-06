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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.Outcome

private val ProfileGreen = ModSuccess
private val ProfileGold = ModWarn
private val ProfileRed = ModAccent
private val FINE_CATEGORIES = listOf("ATTENDANCE", "DISCIPLINARY", "EXAM", "LIBRARY", "OTHER")
private val GENDERS = listOf("MALE", "FEMALE")
private val ENROLLMENTS = listOf("ACTIVE", "GRADUATED", "PROMOTED", "REPEATED", "WITHDRAWN")
private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

@Composable
fun StudentProfileWorkspace(
    loadedProfile: StudentProfile,
    session: AcademicSession?,
    fines: List<Fine>,
    saveOutcome: Outcome<Unit>,
    errorMessage: String?,
    onSave: (StudentProfile) -> Unit,
    onIssueFine: (String, Double, String) -> Unit,
    onDeleteFine: (Fine) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var profile by remember(loadedProfile.rollNumber) { mutableStateOf(loadedProfile) }
    var showFineDialog by remember { mutableStateOf(false) }
    var pendingFineDelete by remember { mutableStateOf<Fine?>(null) }

    val dirty = profile != loadedProfile
    val nameError = FieldValidators.nameError(profile.name, "Full name")
    val phoneError = FieldValidators.phoneError(profile.phone ?: "", label = "Phone")
    val guardianPhoneError = FieldValidators.phoneError(profile.guardianPhone ?: "", label = "Guardian phone")
    val emergencyPhoneError = FieldValidators.phoneError(profile.emergencyContactPhone ?: "", label = "Emergency phone")
    val cnicError = FieldValidators.cnicError(profile.cnicBform ?: "")
    val validationErrors = listOfNotNull(nameError, phoneError, guardianPhoneError, emergencyPhoneError, cnicError)

    val requiredFields = listOf(
        profile.name, profile.fatherName ?: profile.guardianName, profile.cnicBform, profile.dob,
        profile.phone, profile.personalEmail, profile.currentAddress, profile.emergencyContactPhone,
    )
    val completion = (((requiredFields.count { !it.isNullOrBlank() }) * 100) / requiredFields.size).coerceIn(0, 100)

    val totalFines = fines.sumOf { it.amount }

    LazyColumn(modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { StudentProfileHero(profile, session, completion) }

        if (!errorMessage.isNullOrBlank()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = ProfileRed.copy(alpha = 0.1f), border = BorderStroke(1.dp, ProfileRed.copy(alpha = 0.25f))) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(errorMessage, modifier = Modifier.weight(1f), color = ProfileRed, style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = onClearError) { Text("Dismiss") }
                    }
                }
            }
        }

        item {
            ProfileSectionCard("Identity", "Legal and personal information") {
                ProfileField("Full name", profile.name, error = nameError) { profile = profile.copy(name = it) }
                ProfileField("Father's name", profile.fatherName ?: "") { profile = profile.copy(fatherName = it) }
                ProfileField("Guardian's name", profile.guardianName ?: "") { profile = profile.copy(guardianName = it) }
                ProfileField("CNIC / B-Form", profile.cnicBform ?: "", error = cnicError) { profile = profile.copy(cnicBform = it) }
                ProfileDateField("Date of birth", profile.dob ?: "") { profile = profile.copy(dob = it) }
                ProfileChipPicker("Gender", GENDERS, profile.gender) { profile = profile.copy(gender = it) }
                ProfileChipPicker("Blood group", BLOOD_GROUPS, profile.bloodGroup) { profile = profile.copy(bloodGroup = it) }
                ProfileField("Domicile", profile.domicile ?: "") { profile = profile.copy(domicile = it) }
                ProfileField("Religion", profile.religion ?: "") { profile = profile.copy(religion = it) }
            }
        }

        item {
            ProfileSectionCard("Contact", "Student and guardian communication") {
                ProfileField("Phone", profile.phone ?: "", error = phoneError) { profile = profile.copy(phone = it) }
                ProfileField("Personal email", profile.personalEmail ?: "") { profile = profile.copy(personalEmail = it) }
                ProfileField("Guardian phone", profile.guardianPhone ?: "", error = guardianPhoneError) { profile = profile.copy(guardianPhone = it) }
                ProfileField("Current address", profile.currentAddress ?: "") { profile = profile.copy(currentAddress = it) }
                ProfileField("Permanent address", profile.permanentAddress ?: "") { profile = profile.copy(permanentAddress = it) }
            }
        }

        item {
            ProfileSectionCard("Emergency & support", "Emergency contact and accommodation") {
                ProfileField("Emergency contact name", profile.emergencyContactName ?: "") { profile = profile.copy(emergencyContactName = it) }
                ProfileField("Emergency relation", profile.emergencyContactRelation ?: "") { profile = profile.copy(emergencyContactRelation = it) }
                ProfileField("Emergency phone", profile.emergencyContactPhone ?: "", error = emergencyPhoneError) { profile = profile.copy(emergencyContactPhone = it) }
                ProfileField("Special needs", profile.specialNeeds ?: "") { profile = profile.copy(specialNeeds = it) }
            }
        }

        item {
            ProfileSectionCard("University record", "Institution identifiers and enrollment") {
                ProfileField("University roll number", profile.universityRollNo ?: "") { profile = profile.copy(universityRollNo = it) }
                ProfileField("Registration number", profile.registrationNo ?: "") { profile = profile.copy(registrationNo = it) }
                ProfileDateField("Admission date", profile.admissionDate ?: "") { profile = profile.copy(admissionDate = it) }
                ProfileChipPicker("Enrollment status", ENROLLMENTS, profile.enrollmentStatus, allowClear = false) { profile = profile.copy(enrollmentStatus = it ?: profile.enrollmentStatus) }
            }
        }

        item {
            AcademicAndRolesCard(
                profile = profile,
                onToggleCr = { profile = profile.copy(isCr = !profile.isCr) },
                onToggleGr = { profile = profile.copy(isGr = !profile.isGr) },
            )
        }

        item { FinesWorkspaceCard(fines, totalFines, onAdd = { showFineDialog = true }, onDelete = { pendingFineDelete = it }) }

        item {
            ProfileSaveCard(
                dirty = dirty,
                saving = saveOutcome is Outcome.Loading,
                errors = validationErrors,
                onSave = { onSave(profile) },
                onReset = { profile = loadedProfile },
            )
        }

        item { Spacer(Modifier.height(72.dp)) }
    }

    if (showFineDialog) {
        AddProfileFineDialog(onDismiss = { showFineDialog = false }, onConfirm = { category, amount, reason -> onIssueFine(category, amount, reason); showFineDialog = false })
    }

    pendingFineDelete?.let { fine ->
        ConfirmDestructiveActionDialog(
            title = "Remove fine",
            dependentSummary = "Removes the Rs ${fine.amount} ${fine.category} record.",
            onConfirm = { onDeleteFine(fine); pendingFineDelete = null },
            onDismiss = { pendingFineDelete = null },
        )
    }
}

@Composable
private fun StudentProfileHero(profile: StudentProfile, session: AcademicSession?, completion: Int) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            AvatarInitials(profile.name, size = 52)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text("STUDENT RECORD", color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
                Text(profile.name, color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                Text("Roll ${profile.rollNumber} · ${session?.label ?: "Session"}", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodySmall)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$completion%", color = if (completion == 100) ProfileGreen else ProfileGold, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text("PROFILE COMPLETION", color = CmsTheme.colors.onInkMuted, style = CmsTextStyles.eyebrow)
            }
        }
    }
}

@Composable
private fun ProfileSectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String, error: String? = null, onChange: (String) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onChange,
            label = { Text(label) },
            isError = error != null && value.isNotBlank(),
            supportingText = { if (error != null && value.isNotBlank()) Text(error) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

@Composable
private fun ProfileDateField(label: String, value: String, onChange: (String) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        CmsDateField(value = value, onValueChange = onChange, label = label, optional = true)
    }
}

@Composable
private fun ProfileChipPicker(label: String, options: List<String>, selected: String?, allowClear: Boolean = true, onSelect: (String?) -> Unit) {
    Column(Modifier.padding(vertical = 6.dp)) {
        Text(label, color = ModMuted, style = CmsTextStyles.eyebrow)
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (allowClear) {
                CmsChip("None", selected = selected.isNullOrBlank(), onClick = { onSelect(null) })
            }
            options.forEach { option -> CmsChip(option, selected = selected == option, onClick = { onSelect(option) }) }
        }
    }
}

@Composable
private fun AcademicAndRolesCard(profile: StudentProfile, onToggleCr: () -> Unit, onToggleGr: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("Academic standing & class roles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Grades are read-only and update from recorded results.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AcademicMetric("GPA", profile.gpa?.let { "%.2f".format(it) } ?: "--")
                AcademicMetric("CGPA", profile.cgpa?.let { "%.2f".format(it) } ?: "--")
                AcademicMetric("Account", if (profile.linkedEmail.isNotBlank()) "Linked" else "Not linked")
            }
            Spacer(Modifier.height(10.dp))
            Text("CLASS REPRESENTATIVE ROLES", color = ModMuted, style = CmsTextStyles.eyebrow)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Class Rep (CR)", modifier = Modifier.weight(1f))
                Switch(checked = profile.isCr, onCheckedChange = { onToggleCr() })
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Girls' Rep (GR)", modifier = Modifier.weight(1f))
                Switch(checked = profile.isGr, onCheckedChange = { onToggleGr() })
            }
        }
    }
}

@Composable
private fun AcademicMetric(label: String, value: String) {
    Column {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
    }
}

@Composable
private fun FinesWorkspaceCard(fines: List<Fine>, total: Double, onAdd: () -> Unit, onDelete: (Fine) -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Fines", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Rs $total total", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onAdd) { Text("Fine") }
            }
            if (fines.isEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("No active fine records", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            } else {
                Spacer(Modifier.height(6.dp))
                fines.forEach { fine -> FineCard(fine, onDelete) }
            }
        }
    }
}

@Composable
private fun FineCard(fine: Fine, onDelete: (Fine) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("${fine.category} · Rs ${fine.amount}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
            Text(fine.reason, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
        TextButton(onClick = { onDelete(fine) }) { Text("Remove", color = CmsTheme.colors.accent) }
    }
}

@Composable
private fun ProfileSaveCard(dirty: Boolean, saving: Boolean, errors: List<String>, onSave: () -> Unit, onReset: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text(if (dirty) "Unsaved changes" else "Profile is up to date", color = if (dirty) ProfileGold else ProfileGreen, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
            Text(if (dirty) "Save to update the student record." else "No pending profile changes.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            if (errors.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text("Review highlighted fields", color = ProfileRed, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CmsPrimaryButton(text = if (saving) "Saving profile" else "Save profile", onClick = onSave, enabled = dirty && errors.isEmpty() && !saving)
                if (dirty) TextButton(onClick = onReset) { Text("Discard changes") }
            }
        }
    }
}

@Composable
private fun AddProfileFineDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var category by remember { mutableStateOf(FINE_CATEGORIES.first()) }
    var amount by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    val parsedAmount = amount.toDoubleOrNull()
    val error = if (amount.isNotBlank() && (parsedAmount == null || parsedAmount <= 0.0)) "Enter an amount greater than zero." else null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fine", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column {
                Text("CATEGORY", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FINE_CATEGORIES.forEach { option -> CmsChip(option, selected = category == option, onClick = { category = option }) }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (Rs)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = ProfileRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { parsedAmount?.let { onConfirm(category, it, reason.trim()) } }, enabled = parsedAmount != null && parsedAmount > 0.0) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
