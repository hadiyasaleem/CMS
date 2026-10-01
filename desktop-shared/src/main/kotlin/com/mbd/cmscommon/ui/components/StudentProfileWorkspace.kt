package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.Camera
import com.mbd.cmscommon.controller.rollBlockHint
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AcademicSession
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
private val GENDERS = listOf("MALE", "FEMALE")
private val ENROLLMENTS = listOf("ACTIVE", "GRADUATED", "PROMOTED", "REPEATED", "WITHDRAWN")
private val BLOOD_GROUPS = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

@Composable
fun StudentProfileWorkspace(
    loadedProfile: StudentProfile,
    session: AcademicSession?,
    saveOutcome: Outcome<Unit>?,
    errorMessage: String?,
    onSave: (StudentProfile) -> Unit,
    onDelink: () -> Unit,
    onClearError: () -> Unit,
    onPickPhoto: (onPicked: (ImageBitmap) -> Unit) -> Unit,
    onSavePhoto: (ImageBitmap) -> Unit,
    photoBusy: Boolean,
    onLoadPhoto: suspend (String) -> ImageBitmap?,
    modifier: Modifier = Modifier,
) {
    var profile by remember(loadedProfile.rollNumber) { mutableStateOf(loadedProfile) }
    var pendingCrop by remember { mutableStateOf<ImageBitmap?>(null) }
    // Tracks whether the last save-in-flight was a delink, so its success/failure shows right next
    // to the Delink button (which sits below the fold) instead of only in the generic notice at the
    // top of this list -- the local `profile` edit buffer also needs to be told about the delink
    // directly, since it isn't resynced from `loadedProfile` (that would clobber in-progress edits).
    var delinkAttempted by remember { mutableStateOf(false) }

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


    val listState = rememberLazyListState()
    WithVerticalScrollbar(listState) {
    LazyColumn(modifier.fillMaxWidth(), state = listState, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            StudentProfileHero(
                profile = profile,
                session = session,
                completion = completion,
                photoBusy = photoBusy,
                onLoadPhoto = onLoadPhoto,
                onPickPhotoClick = { onPickPhoto { bitmap -> pendingCrop = bitmap } },
            )
        }

        if (!errorMessage.isNullOrBlank()) {
            item { CmsNotice(errorMessage, tone = NoticeTone.Error, onDismiss = onClearError) }
        }
        when (saveOutcome) {
            is Outcome.Success -> item { CmsNotice("Profile saved.", tone = NoticeTone.Success) }
            is Outcome.Error -> item { CmsNotice(saveOutcome.message, tone = NoticeTone.Error) }
            else -> {}
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
                session = session,
                delinkOutcome = if (delinkAttempted) saveOutcome else null,
                onToggleCr = { profile = profile.copy(isCr = !profile.isCr) },
                onToggleGr = { profile = profile.copy(isGr = !profile.isGr) },
                onDelink = {
                    // Optimistic local update: the controller's own StateFlow already clears
                    // linkedEmail, but this screen edits a local copy so in-progress field edits
                    // aren't clobbered by every upstream emission -- so the delink has to be applied
                    // to that local copy directly, or the card keeps showing the old linked email.
                    profile = profile.copy(linkedEmail = "")
                    delinkAttempted = true
                    onDelink()
                },
            )
        }

        item {
            ProfileSaveCard(
                dirty = dirty,
                saving = saveOutcome is Outcome.Loading,
                errors = validationErrors,
                onSave = { delinkAttempted = false; onSave(profile) },
                onReset = { profile = loadedProfile },
            )
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
    }

    pendingCrop?.let { source ->
        PhotoCropDialog(
            source = source,
            onCancel = { pendingCrop = null },
            onCropped = { cropped -> pendingCrop = null; onSavePhoto(cropped) },
        )
    }
}

@Composable
private fun StudentProfileHero(
    profile: StudentProfile,
    session: AcademicSession?,
    completion: Int,
    photoBusy: Boolean,
    onLoadPhoto: suspend (String) -> ImageBitmap?,
    onPickPhotoClick: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(18.dp), color = ModInk) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.BottomEnd) {
                ProfilePhotoAvatar(profile.name, profile.photoPath, size = 52, onLoadPhoto = onLoadPhoto, cacheKey = profile.updatedAt)
                Surface(
                    modifier = Modifier.clickable(enabled = !photoBusy, onClick = onPickPhotoClick),
                    shape = CircleShape,
                    color = CmsTheme.colors.accent,
                ) {
                    Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                        if (photoBusy) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CmsTheme.colors.onInk)
                        } else {
                            Icon(TablerIcons.Camera, contentDescription = "Change photo", tint = CmsTheme.colors.onInk, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AcademicAndRolesCard(
    profile: StudentProfile,
    session: AcademicSession?,
    delinkOutcome: Outcome<Unit>?,
    onToggleCr: () -> Unit,
    onToggleGr: () -> Unit,
    onDelink: () -> Unit,
) {
    var confirmDelink by remember { mutableStateOf(false) }

    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            Text("Academic standing & class roles", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Grades are read-only and update from recorded results.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(10.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AcademicMetric("GPA", profile.gpa?.let { "%.2f".format(it) } ?: "--")
                AcademicMetric("CGPA", profile.cgpa?.let { "%.2f".format(it) } ?: "--")
                AcademicMetric("Account", if (profile.linkedEmail.isNotBlank()) "Linked" else "Not linked")
                AcademicMetric("Shift", profile.shift.label)
            }
            // The roll number's serial decides the shift, so it isn't edited here.
            rollBlockHint(session)?.let { hint ->
                Spacer(Modifier.height(4.dp))
                Text("Shift follows the roll number. $hint", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (profile.linkedEmail.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(profile.linkedEmail, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    TextButton(onClick = { confirmDelink = true }) { Text("Delink account", color = CmsTheme.colors.accent) }
                }
            }
            when (delinkOutcome) {
                is Outcome.Success -> {
                    Spacer(Modifier.height(6.dp))
                    CmsNotice("Account delinked.", tone = NoticeTone.Success)
                }
                is Outcome.Error -> {
                    Spacer(Modifier.height(6.dp))
                    CmsNotice(delinkOutcome.message, tone = NoticeTone.Error)
                }
                else -> {}
            }
            Spacer(Modifier.height(10.dp))
            Text("CLASS REPRESENTATIVE ROLES", color = ModMuted, style = CmsTextStyles.eyebrow)
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Class Rep (CR) · ${profile.shift.label}", modifier = Modifier.weight(1f))
                Switch(checked = profile.isCr, onCheckedChange = { onToggleCr() })
            }
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Girls' Rep (GR) · ${profile.shift.label}", modifier = Modifier.weight(1f))
                Switch(checked = profile.isGr, onCheckedChange = { onToggleGr() })
            }
        }
    }

    if (confirmDelink) {
        ConfirmDestructiveActionDialog(
            title = "Delink account",
            dependentSummary = "Removes ${profile.linkedEmail}'s access to this student record. They'll need a new approved link request to regain access.",
            confirmLabel = "Delink",
            showUndoWarning = false,
            onConfirm = { onDelink(); confirmDelink = false },
            onDismiss = { confirmDelink = false },
        )
    }
}

@Composable
private fun AcademicMetric(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
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

