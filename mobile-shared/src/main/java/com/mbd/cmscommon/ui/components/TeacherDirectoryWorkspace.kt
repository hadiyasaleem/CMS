package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.controller.TeacherAccountDraft
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModAccent
import com.mbd.cmscommon.ui.theme.ModWarn
import com.mbd.cmscommon.util.FieldValidators

private val TeacherGold = ModWarn
private val TeacherRed = ModAccent

enum class TeacherFilter(val label: String) {
    ALL("All faculty"),
    ACTIVE("Active"),
    UNASSIGNED("No classes"),
    INCOMPLETE("Incomplete profile"),
}

enum class TeacherSort(val label: String) {
    NAME("Name"),
    CLASSES("Most classes"),
    NEWEST("Newest"),
}

@Composable
fun TeacherDirectoryWorkspace(
    teachers: List<Teacher>,
    departments: List<Department>,
    assignments: Map<String, List<ResolvedAssignment>>,
    loading: Boolean,
    busy: Boolean,
    busyTeacherId: String?,
    notice: String?,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreate: (TeacherAccountDraft) -> Unit,
    onUpdate: (Teacher, TeacherAccountDraft) -> Unit,
    onSetStatus: (Teacher, TeacherStatus) -> Unit,
    onDelete: (Teacher) -> Unit,
    onPickPhoto: (onPicked: (ImageBitmap) -> Unit) -> Unit,
    onUploadCroppedPhoto: (Teacher, ImageBitmap) -> Unit,
    onLoadPhoto: suspend (String) -> ImageBitmap?,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(TeacherFilter.ALL) }
    var deptFilter by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf(TeacherSort.NAME) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
    var pendingStatus by remember { mutableStateOf<Pair<Teacher, TeacherStatus>?>(null) }
    var pendingDelete by remember { mutableStateOf<Teacher?>(null) }

    fun completeness(teacher: Teacher): Int {
        val fields = listOf(teacher.deptId, teacher.designation, teacher.qualification, teacher.specialization, teacher.officeRoom, teacher.phone)
        return ((fields.count { !it.isNullOrBlank() }) * 100) / fields.size
    }

    val filtered = teachers.filter { teacher ->
        val teacherAssignments = assignments[teacher.teacherId].orEmpty()
        val matchesQuery = query.isBlank() || teacher.name.contains(query, ignoreCase = true) || teacher.email.contains(query, ignoreCase = true)
        val matchesFilter = when (filter) {
            TeacherFilter.ALL -> true
            TeacherFilter.ACTIVE -> teacher.status == TeacherStatus.ACTIVE
            TeacherFilter.UNASSIGNED -> teacherAssignments.isEmpty()
            TeacherFilter.INCOMPLETE -> completeness(teacher) < 100
        }
        val matchesDept = deptFilter == null || teacher.deptId == deptFilter
        matchesQuery && matchesFilter && matchesDept
    }

    val visible = when (sort) {
        TeacherSort.NAME -> filtered.sortedBy { it.name.lowercase() }
        TeacherSort.CLASSES -> filtered.sortedByDescending { assignments[it.teacherId]?.size ?: 0 }
        TeacherSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
    }

    Box(modifier.fillMaxSize()) {
        CardGrid(Modifier.fillMaxWidth()) {
            fullSpanItem { TeacherSummaryCard(teachers.size, teachers.count { it.status == TeacherStatus.ACTIVE }, assignments.values.sumOf { it.size }, teachers.count { completeness(it) < 100 }) }

            fullSpanItem {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by name or email", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("SHOW", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TeacherFilter.entries.forEach { option -> CmsChip(option.label, selected = filter == option, onClick = { filter = option }) }
                        DepartmentFilterChip(departments, deptFilter, onSelect = { deptFilter = it })
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("SORT", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TeacherSort.entries.forEach { option -> CmsChip(option.label, selected = sort == option, onClick = { sort = option }) }
                    }
                }
            }

            when {
                loading -> fullSpanItems(3) { SkeletonRow() }
                teachers.isEmpty() -> fullSpanItem { TeacherEmptyState(filtered = false, onAdd = { showCreateDialog = true }, onClearFilters = {}) }
                visible.isEmpty() -> fullSpanItem {
                    TeacherEmptyState(
                        filtered = true,
                        onAdd = {},
                        onClearFilters = { query = ""; filter = TeacherFilter.ALL; deptFilter = null },
                    )
                }
                else -> items(visible, key = { it.teacherId }) { teacher ->
                    val dept = departments.firstOrNull { it.deptId == teacher.deptId }
                    TeacherCard(
                        teacher = teacher,
                        department = dept,
                        assignments = assignments[teacher.teacherId].orEmpty(),
                        completeness = completeness(teacher),
                        busy = busyTeacherId == teacher.teacherId,
                        onEdit = { editingTeacher = teacher },
                        onRequestStatus = { status -> pendingStatus = teacher to status },
                        onRequestDelete = { pendingDelete = teacher },
                        onLoadPhoto = onLoadPhoto,
                    )
                }
            }

            fullSpanItem { Spacer(Modifier.height(72.dp)) }
        }
        CmsFab(
            onClick = { showCreateDialog = true },
            contentDescription = "Add teacher",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showCreateDialog) {
        TeacherActionDialog(
            title = "Create teacher",
            existing = null,
            departments = departments,
            busy = busy,
            onDismiss = { showCreateDialog = false },
            onConfirm = { draft -> onCreate(draft); showCreateDialog = false },
            onPickPhoto = null,
            onSavePhoto = null,
            photoBusy = false,
            onLoadPhoto = onLoadPhoto,
        )
    }

    editingTeacher?.let { teacher ->
        TeacherActionDialog(
            title = "Manage ${teacher.name}",
            existing = teacher,
            departments = departments,
            busy = busy,
            onDismiss = { editingTeacher = null },
            onConfirm = { draft -> onUpdate(teacher, draft); editingTeacher = null },
            onPickPhoto = onPickPhoto,
            onSavePhoto = { bitmap -> onUploadCroppedPhoto(teacher, bitmap) },
            photoBusy = busy || busyTeacherId == teacher.teacherId,
            onLoadPhoto = onLoadPhoto,
        )
    }

    pendingStatus?.let { (teacher, status) ->
        val label = when (status) {
            TeacherStatus.DISABLED -> "Disable account"
            TeacherStatus.BANNED -> "Ban account"
            TeacherStatus.ACTIVE -> "Reactivate"
        }
        AlertDialog(
            onDismissRequest = { pendingStatus = null },
            title = { Text(label, style = MaterialTheme.typography.headlineSmall) },
            text = { Text("Current status: ${teacher.status}. This changes ${teacher.name}'s sign-in access.") },
            confirmButton = { TextButton(onClick = { onSetStatus(teacher, status); pendingStatus = null }) { Text(label) } },
            dismissButton = { TextButton(onClick = { pendingStatus = null }) { Text("Cancel") } },
        )
    }

    pendingDelete?.let { teacher ->
        ConfirmDestructiveActionDialog(
            title = "Remove teacher",
            dependentSummary = "Removes ${teacher.name}'s faculty account and revokes access.",
            onConfirm = { onDelete(teacher); pendingDelete = null },
            onDismiss = { pendingDelete = null },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Something went wrong", style = MaterialTheme.typography.headlineSmall) },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = onClearError) { Text("OK") } },
        )
    }

    if (!notice.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onConsumeNotice,
            title = { Text("Success", style = MaterialTheme.typography.headlineSmall) },
            text = { Text(notice) },
            confirmButton = { TextButton(onClick = onConsumeNotice) { Text("OK") } },
        )
    }
}

@Composable
private fun TeacherSummaryCard(total: Int, active: Int, classes: Int, incomplete: Int) {
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TeacherMetric("Faculty", total.toString(), Modifier.weight(1f))
            TeacherMetric("Active", active.toString(), Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TeacherMetric("Classes", classes.toString(), Modifier.weight(1f))
            TeacherMetric("Incomplete", incomplete.toString(), Modifier.weight(1f), alert = incomplete > 0)
        }
    }
}

@Composable
private fun DepartmentFilterChip(departments: List<Department>, selectedDeptId: String?, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = departments.firstOrNull { it.deptId == selectedDeptId }?.name

    Box {
        Surface(
            modifier = Modifier.clickable { expanded = true },
            shape = RectangleShape,
            color = if (selectedDeptId != null) CmsTheme.colors.ink else MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = if (selectedDeptId != null) CmsTheme.colors.onInk else MaterialTheme.colorScheme.onSurface,
            border = if (selectedDeptId != null) null else BorderStroke(2.dp, CmsTheme.colors.rule),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(selectedName ?: "All departments", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 240.dp)) {
            DropdownMenuItem(text = { Text("All departments") }, onClick = { onSelect(null); expanded = false })
            departments.sortedBy { it.name }.forEach { dept ->
                DropdownMenuItem(text = { Text(dept.name) }, onClick = { onSelect(dept.deptId); expanded = false })
            }
        }
    }
}

/**
 * Shows the teacher's uploaded photo (downloaded lazily via [onLoadPhoto]), falling back to
 * initials. [cacheKey] should change whenever the underlying photo might have (e.g. the record's
 * updatedAt) so a re-upload under the same storage path is picked up instead of showing the stale
 * cached image indefinitely.
 */
@Composable
private fun TeacherAvatar(name: String, photoPath: String?, size: Int, onLoadPhoto: suspend (String) -> ImageBitmap?, modifier: Modifier = Modifier, cacheKey: Any = Unit) {
    if (photoPath.isNullOrBlank()) {
        AvatarInitials(name, modifier, size)
        return
    }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, photoPath, cacheKey) {
        value = runCatching { onLoadPhoto(photoPath) }.getOrNull()
    }
    val current = bitmap
    if (current != null) {
        Image(
            bitmap = current,
            contentDescription = name,
            modifier = modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        AvatarInitials(name, modifier, size)
    }
}

/**
 * Like [TeacherAvatar], but sized entirely by [modifier] (e.g. `fillMaxWidth().aspectRatio(1f)`)
 * instead of a fixed dp size -- for the card header, where the avatar should grow to fill the
 * card's width as a perfect circle rather than sit at a small fixed size next to empty space.
 */
@Composable
private fun TeacherCardAvatar(name: String, photoPath: String?, onLoadPhoto: suspend (String) -> ImageBitmap?, modifier: Modifier = Modifier, cacheKey: Any = Unit) {
    val initials = name.trim().split(" ").filter { it.isNotEmpty() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "?" }

    if (photoPath.isNullOrBlank()) {
        Box(modifier.clip(CircleShape).background(CmsTheme.colors.accent), contentAlignment = Alignment.Center) {
            Text(initials, color = CmsTheme.colors.onInk, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
        }
        return
    }
    val bitmap by produceState<ImageBitmap?>(initialValue = null, photoPath, cacheKey) {
        value = runCatching { onLoadPhoto(photoPath) }.getOrNull()
    }
    val current = bitmap
    if (current != null) {
        Image(bitmap = current, contentDescription = name, modifier = modifier.clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Box(modifier.clip(CircleShape).background(CmsTheme.colors.accent), contentAlignment = Alignment.Center) {
            Text(initials, color = CmsTheme.colors.onInk, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun TeacherMetric(label: String, value: String, modifier: Modifier = Modifier, alert: Boolean = false) {
    Surface(modifier = modifier, shape = RoundedCornerShape(14.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(14.dp)) {
            Text(value, color = if (alert) TeacherRed else ModInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Text(label.uppercase(), color = ModMuted, style = CmsTextStyles.eyebrow)
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun TeacherCard(
    teacher: Teacher,
    department: Department?,
    assignments: List<ResolvedAssignment>,
    completeness: Int,
    busy: Boolean,
    onEdit: () -> Unit,
    onRequestStatus: (TeacherStatus) -> Unit,
    onRequestDelete: () -> Unit,
    onLoadPhoto: suspend (String) -> ImageBitmap?,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.clickable(onClick = onEdit), shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(16.dp)) {
            TeacherCardAvatar(
                name = teacher.name,
                photoPath = teacher.photoPath,
                onLoadPhoto = onLoadPhoto,
                cacheKey = teacher.updatedAt,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
            )
            Spacer(Modifier.height(8.dp))
            Text(teacher.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                department?.name ?: "Department not assigned",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            TeacherContactLine(teacher)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(teacher.status.name, if (teacher.status == TeacherStatus.ACTIVE) BadgeTone.Success else BadgeTone.Error)
                StatusBadge("${assignments.size} classes", BadgeTone.Neutral)
                StatusBadge("$completeness% profile", if (completeness == 100) BadgeTone.Success else BadgeTone.Warning)
            }
            if (assignments.isEmpty()) {
                Spacer(Modifier.height(4.dp))
                Text("No timetable classes assigned", color = ModMuted, style = MaterialTheme.typography.bodySmall)
            }
            if (completeness < 100) {
                Spacer(Modifier.height(4.dp))
                Text("Contact and specialization not completed", color = TeacherGold, style = MaterialTheme.typography.bodySmall)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Box {
                    IconButton(onClick = { menuExpanded = true }, enabled = !busy) { Icon(Icons.Filled.MoreVert, contentDescription = "More") }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        when (teacher.status) {
                            TeacherStatus.ACTIVE -> DropdownMenuItem(text = { Text("Disable") }, onClick = { menuExpanded = false; onRequestStatus(TeacherStatus.DISABLED) })
                            else -> DropdownMenuItem(text = { Text("Reactivate") }, onClick = { menuExpanded = false; onRequestStatus(TeacherStatus.ACTIVE) })
                        }
                        DropdownMenuItem(
                            text = { Text("Remove", color = CmsTheme.colors.accent) },
                            onClick = { menuExpanded = false; onRequestDelete() },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TeacherContactLine(teacher: Teacher) {
    Text(
        listOfNotNull(teacher.email, teacher.phone?.takeIf { it.isNotBlank() }).joinToString(" · "),
        color = ModMuted,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun TeacherEmptyState(filtered: Boolean, onAdd: () -> Unit, onClearFilters: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (filtered) "No matching teachers" else "No teachers found", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try another search or filter." else "Add the first faculty account.",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(text = if (filtered) "Clear filters" else "Add teacher", onClick = if (filtered) onClearFilters else onAdd)
        }
    }
}

@Composable
private fun TeacherActionDialog(
    title: String,
    existing: Teacher?,
    departments: List<Department>,
    busy: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (TeacherAccountDraft) -> Unit,
    onPickPhoto: ((onPicked: (ImageBitmap) -> Unit) -> Unit)?,
    onSavePhoto: ((ImageBitmap) -> Unit)?,
    photoBusy: Boolean,
    onLoadPhoto: suspend (String) -> ImageBitmap?,
) {
    var pendingCrop by remember { mutableStateOf<ImageBitmap?>(null) }
    var pendingPhoto by remember { mutableStateOf<ImageBitmap?>(null) }
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var deptId by remember { mutableStateOf(existing?.deptId ?: "") }
    var designation by remember { mutableStateOf(existing?.designation ?: "") }
    var qualification by remember { mutableStateOf(existing?.qualification ?: "") }
    var specialization by remember { mutableStateOf(existing?.specialization ?: "") }
    var officeRoom by remember { mutableStateOf(existing?.officeRoom ?: "") }
    var gender by remember { mutableStateOf(existing?.gender ?: "") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var permissions by remember { mutableStateOf(existing?.permissions ?: TeacherPermissions()) }
    var isAdmin by remember { mutableStateOf(existing?.isAdmin ?: false) }

    val nameError = FieldValidators.nameError(name, "Teacher name")
    val emailError = FieldValidators.emailError(email)
    val passwordError = if (existing == null) FieldValidators.passwordError(password) else null
    val valid = nameError == null && emailError == null && (existing != null || passwordError == null)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState())) {
                if (existing == null) {
                    Text("Create the sign-in account and complete the initial faculty profile in one step.", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(10.dp))
                }
                if (existing != null && onPickPhoto != null) {
                    Box(Modifier.padding(bottom = 4.dp), contentAlignment = Alignment.Center) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            val pending = pendingPhoto
                            if (pending != null) {
                                Image(
                                    bitmap = pending,
                                    contentDescription = existing.name,
                                    modifier = Modifier.size(72.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop,
                                )
                            } else {
                                TeacherAvatar(existing.name, existing.photoPath, size = 72, onLoadPhoto = onLoadPhoto, cacheKey = existing.updatedAt)
                            }
                            Surface(
                                modifier = Modifier.clickable(enabled = !photoBusy) { onPickPhoto { bitmap -> pendingCrop = bitmap } },
                                shape = CircleShape,
                                color = CmsTheme.colors.accent,
                            ) {
                                Box(Modifier.size(26.dp), contentAlignment = Alignment.Center) {
                                    if (photoBusy) {
                                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CmsTheme.colors.onInk)
                                    } else {
                                        Icon(Icons.Filled.PhotoCamera, contentDescription = "Change photo", tint = CmsTheme.colors.onInk, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    if (pendingPhoto != null) {
                        Text(
                            "New photo ready -- tap Save changes to upload it.",
                            color = ModMuted,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 10.dp),
                        )
                    }
                }
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Teacher name") }, isError = name.isNotBlank() && nameError != null, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, enabled = existing == null, isError = email.isNotBlank() && emailError != null, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (existing == null) {
                    Spacer(Modifier.height(10.dp))
                    Text("TEMPORARY CREDENTIAL", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Temporary password") },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                CmsEntityPicker(
                    label = "Department",
                    selectedId = deptId.ifBlank { null },
                    options = departments.map { CmsEntityOption(it.deptId, it.name) },
                    onSelected = { deptId = it ?: "" },
                    optional = true,
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = designation, onValueChange = { designation = it }, label = { Text("Designation") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = qualification, onValueChange = { qualification = it }, label = { Text("Qualification") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = specialization, onValueChange = { specialization = it }, label = { Text("Specialization") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(value = officeRoom, onValueChange = { officeRoom = it }, label = { Text("Office") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(10.dp))
                Text("GENDER", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("MALE", "FEMALE", "OTHER").forEach { option -> CmsChip(option, selected = gender == option, onClick = { gender = option }) }
                }
                Spacer(Modifier.height(10.dp))
                Text("DELEGATED PERMISSIONS", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                PermissionEditor(permissions, onChange = { permissions = it })
                Spacer(Modifier.height(10.dp))
                Text("ACCOUNT ACCESS", color = ModMuted, style = CmsTextStyles.eyebrow)
                Spacer(Modifier.height(6.dp))
                PermissionRow("Admin access (grants full admin rights)", isAdmin) { isAdmin = it }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        TeacherAccountDraft(
                            name.trim(), email.trim(), phone.trim(), deptId, designation.trim(),
                            qualification.trim(), specialization.trim(), officeRoom.trim(), gender,
                            password, permissions, isAdmin,
                        ),
                    )
                    pendingPhoto?.let { onSavePhoto?.invoke(it) }
                },
                enabled = valid && !busy,
            ) { Text(if (busy) (if (existing == null) "Creating" else "Saving") else if (existing == null) "Create teacher" else "Save changes") }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )

    pendingCrop?.let { source ->
        PhotoCropDialog(
            source = source,
            onCancel = { pendingCrop = null },
            onCropped = { cropped -> pendingCrop = null; pendingPhoto = cropped },
        )
    }
}

@Composable
private fun PermissionEditor(permissions: TeacherPermissions, onChange: (TeacherPermissions) -> Unit) {
    Column {
        PermissionRow("Approve link requests", permissions.canApproveLinkRequests) { onChange(permissions.copy(canApproveLinkRequests = it)) }
        PermissionRow("Edit timetable", permissions.canEditTimetable) { onChange(permissions.copy(canEditTimetable = it)) }
        PermissionRow("Send notifications", permissions.canSendNotifications) { onChange(permissions.copy(canSendNotifications = it)) }
        PermissionRow("Manage datesheets", permissions.canManageDatesheets) { onChange(permissions.copy(canManageDatesheets = it)) }
    }
}

@Composable
private fun PermissionRow(label: String, checked: Boolean, onToggle: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onToggle)
    }
}
