package com.mbd.cmscommon.ui.components

import compose.icons.TablerIcons
import compose.icons.tablericons.CircleCheck
import compose.icons.tablericons.Eye
import compose.icons.tablericons.EyeOff
import compose.icons.tablericons.DotsVertical
import compose.icons.tablericons.Login
import compose.icons.tablericons.Search
import compose.icons.tablericons.Shield
import compose.icons.tablericons.ShieldCheck
import compose.icons.tablericons.X
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.model.administratorDirectorySnapshot
import com.mbd.cmscommon.ui.theme.CmsTextStyles
import com.mbd.cmscommon.ui.theme.CmsTheme
import com.mbd.cmscommon.ui.theme.ModInk
import com.mbd.cmscommon.ui.theme.ModMuted
import com.mbd.cmscommon.ui.theme.ModTrack
import com.mbd.cmscommon.ui.theme.ModSurface
import com.mbd.cmscommon.ui.theme.ModSuccess
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.PasswordRule
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class AdministratorFilter(val label: String) {
    ALL("All accounts"),
    ACTIVE("Active"),
    UNAVAILABLE("Unavailable"),
    NEVER_SIGNED_IN("Never signed in"),
}

enum class AdministratorSort(val label: String) {
    EMAIL("Email"),
    RECENT_ACTIVITY("Recent activity"),
    NEWEST("Newest"),
}

@Composable
fun AdministratorDirectoryWorkspace(
    administrators: List<AdministratorAccount>,
    currentAccountKey: String?,
    loading: Boolean,
    creating: Boolean,
    createdEmail: String?,
    busyAdminKey: String?,
    notice: String?,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreate: (String, String) -> Unit,
    onConsumeCreated: () -> Unit,
    onSetStatus: (AdministratorAccount, String) -> Unit,
    onResetPassword: (AdministratorAccount, String) -> Unit,
    onDelete: (AdministratorAccount) -> Unit,
    onConsumeNotice: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AdministratorFilter.ALL) }
    var sort by remember { mutableStateOf(AdministratorSort.EMAIL) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var detailAccount by remember { mutableStateOf<AdministratorAccount?>(null) }
    var pendingStatus by remember { mutableStateOf<Pair<AdministratorAccount, String>?>(null) }
    var pendingDelete by remember { mutableStateOf<AdministratorAccount?>(null) }
    var pendingResetPassword by remember { mutableStateOf<AdministratorAccount?>(null) }

    val now = Instant.now()
    val directory = administratorDirectorySnapshot(administrators, now)
    val currentKey = currentAccountKey?.trim()

    val filtered = directory.accounts.filter { account ->
        val matchesQuery = query.isBlank() || account.email.contains(query.trim(), ignoreCase = true)
        val matchesFilter = when (filter) {
            AdministratorFilter.ALL -> true
            AdministratorFilter.ACTIVE -> account.status.equals("ACTIVE", ignoreCase = true)
            AdministratorFilter.UNAVAILABLE -> !account.status.equals("ACTIVE", ignoreCase = true)
            AdministratorFilter.NEVER_SIGNED_IN -> account.lastLoginAt == null
        }
        matchesQuery && matchesFilter
    }

    val visibleAdministrators = when (sort) {
        AdministratorSort.EMAIL -> filtered.sortedBy { it.email.lowercase(Locale.ROOT) }
        AdministratorSort.RECENT_ACTIVITY -> filtered
            .sortedByDescending { it.lastLoginAt != null }
            .let { list ->
                list.groupBy { it.lastLoginAt != null }.flatMap { (hasLogin, group) ->
                    if (hasLogin) group.sortedByDescending { it.lastLoginAt } else group
                }
            }
        AdministratorSort.NEWEST -> filtered.sortedByDescending { it.createdAt }
    }

    fun isSelf(account: AdministratorAccount): Boolean =
        currentKey != null && (account.id == currentKey || account.email.equals(currentKey, ignoreCase = true))

    Box(modifier.fillMaxSize()) {
        CardGrid(Modifier.fillMaxWidth()) {
            fullSpanItem { AdministratorHero(directory.accounts.size) }

            if (!createdEmail.isNullOrBlank()) {
                fullSpanItem { AdministratorCreatedBanner(createdEmail, onConsumeCreated) }
            }

            fullSpanItem { SecurityNotice() }

            fullSpanItem {
                Column(Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by email") },
                        leadingIcon = { Icon(TablerIcons.Search, contentDescription = null) },
                        singleLine = true,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("SHOW", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AdministratorFilter.entries.forEach { option ->
                            CmsChip(option.label, selected = filter == option, onClick = { filter = option })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("SORT", color = ModMuted, style = CmsTextStyles.eyebrow)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AdministratorSort.entries.forEach { option ->
                            CmsChip(option.label, selected = sort == option, onClick = { sort = option })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Showing ${visibleAdministrators.size} of ${directory.accounts.size} accounts", color = ModMuted, style = MaterialTheme.typography.bodySmall)
                }
            }

            when {
                loading -> fullSpanItems(3) { SkeletonRow() }
                directory.accounts.isEmpty() -> fullSpanItem {
                    AdministratorEmptyState(filtered = false, onAdd = { showCreateDialog = true }, onClearFilters = {})
                }
                visibleAdministrators.isEmpty() -> fullSpanItem {
                    AdministratorEmptyState(
                        filtered = true,
                        onAdd = { showCreateDialog = true },
                        onClearFilters = { query = ""; filter = AdministratorFilter.ALL },
                    )
                }
                else -> items(visibleAdministrators, key = { it.id }) { account ->
                    val isCurrent = isSelf(account)
                    AdministratorCard(
                        account = account,
                        isCurrent = isCurrent,
                        now = now,
                        busy = busyAdminKey == account.id,
                        onEdit = { detailAccount = account },
                        onRequestStatus = { status -> pendingStatus = account to status },
                        onRequestResetPassword = { pendingResetPassword = account },
                        onRequestDelete = { pendingDelete = account },
                    )
                }
            }

            fullSpanItem { Spacer(Modifier.height(72.dp)) }
        }
        CmsFab(
            onClick = { showCreateDialog = true },
            contentDescription = "Add administrator",
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
        )
    }

    if (showCreateDialog) {
        CreateAdministratorDialog(
            existing = administrators,
            creating = creating,
            onDismiss = { showCreateDialog = false },
            onCreate = { email, password ->
                onCreate(email, password)
                showCreateDialog = false
            },
        )
    }

    detailAccount?.let { account ->
        AdministratorDetailDialog(
            account = account,
            isCurrent = isSelf(account),
            now = now,
            busy = busyAdminKey == account.id,
            onDismiss = { detailAccount = null },
            onRequestStatus = { status -> pendingStatus = account to status },
            onRequestResetPassword = { pendingResetPassword = account },
            onRequestDelete = { pendingDelete = account },
        )
    }

    pendingStatus?.let { (account, status) ->
        val label = if (status == "ACTIVE") "Reactivate" else "Disable account"
        AlertDialog(
            onDismissRequest = { pendingStatus = null },
            title = { Text(label, style = MaterialTheme.typography.headlineSmall) },
            text = { DialogScrollBody { Text("Current status: ${account.status}. This changes ${account.email}'s sign-in access.") } },
            confirmButton = {
                TextButton(onClick = { onSetStatus(account, status); pendingStatus = null; detailAccount = null }) { Text(label) }
            },
            dismissButton = { TextButton(onClick = { pendingStatus = null }) { Text("Cancel") } },
        )
    }

    pendingDelete?.let { account ->
        ConfirmDestructiveActionDialog(
            title = "Remove administrator",
            dependentSummary = "Removes ${account.email}'s administrator account and revokes access.",
            onConfirm = { onDelete(account); pendingDelete = null; detailAccount = null },
            onDismiss = { pendingDelete = null },
        )
    }

    pendingResetPassword?.let { account ->
        AdministratorResetPasswordDialog(
            account = account,
            busy = busyAdminKey == account.id,
            onConfirm = { newPassword -> onResetPassword(account, newPassword); pendingResetPassword = null },
            onDismiss = { pendingResetPassword = null },
        )
    }

    if (!errorMessage.isNullOrBlank()) {
        CmsErrorDialog(message = errorMessage, title = "Couldn't update administrators", onDismiss = onClearError)
    }

    if (!notice.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onConsumeNotice,
            title = { Text("Success", style = MaterialTheme.typography.headlineSmall) },
            text = { DialogScrollBody { Text(notice) } },
            confirmButton = { TextButton(onClick = onConsumeNotice) { Text("OK") } },
        )
    }
}

@Composable
private fun AdministratorHero(count: Int, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = ModInk) {
        Column(Modifier.padding(20.dp)) {
            Text("ACCESS CONTROL", color = CmsTheme.colors.onInk.copy(alpha = 0.7f), style = CmsTextStyles.eyebrow)
            Spacer(Modifier.height(6.dp))
            Text("Administrator directory", color = CmsTheme.colors.onInk, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text("$count full-access accounts", color = CmsTheme.colors.onInkMuted, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SecurityNotice(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ModInk.copy(alpha = 0.08f), border = BorderStroke(1.dp, ModInk.copy(alpha = 0.2f))) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(TablerIcons.Shield, contentDescription = null, tint = ModInk)
            Spacer(Modifier.size(12.dp))
            Text(
                "Administrator accounts have full-access, college-wide permissions. Create them only for people who need this level of access.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun AdministratorCreatedBanner(email: String, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ModSuccess.copy(alpha = 0.12f), border = BorderStroke(1.dp, ModSuccess.copy(alpha = 0.35f))) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(TablerIcons.CircleCheck, contentDescription = null, tint = ModSuccess)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Administrator created", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(email, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDismiss) { Icon(TablerIcons.X, contentDescription = "Dismiss") }
        }
    }
}

/** Two-letter avatar initials derived from an email local-part, e.g. "john.doe" -> "JD". */
private fun administratorAvatarLabel(email: String): String {
    val local = email.substringBefore('@')
    val parts = local.split('.', '_', '-').filter { it.isNotBlank() }
    return if (parts.size >= 2) "${parts[0]} ${parts[1]}" else local
}

@Composable
private fun AdministratorCard(
    account: AdministratorAccount,
    isCurrent: Boolean,
    now: Instant,
    busy: Boolean,
    onEdit: () -> Unit,
    onRequestStatus: (String) -> Unit,
    onRequestResetPassword: () -> Unit,
    onRequestDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val active = account.status.equals("ACTIVE", ignoreCase = true)

    Surface(
        modifier = modifier.fillMaxWidth().clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        color = ModSurface,
        border = BorderStroke(1.dp, ModTrack),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(administratorAvatarLabel(account.email), size = 42)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(account.email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
                if (isCurrent) {
                    StatusBadge("YOU", BadgeTone.Navy)
                } else {
                    Box {
                        IconButton(onClick = { menuExpanded = true }, enabled = !busy) {
                            Icon(TablerIcons.DotsVertical, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                            if (active) {
                                DropdownMenuItem(text = { Text("Disable") }, onClick = { menuExpanded = false; onRequestStatus("DISABLED") })
                            } else {
                                DropdownMenuItem(text = { Text("Reactivate") }, onClick = { menuExpanded = false; onRequestStatus("ACTIVE") })
                            }
                            DropdownMenuItem(text = { Text("Reset password") }, onClick = { menuExpanded = false; onRequestResetPassword() })
                            DropdownMenuItem(
                                text = { Text("Remove", color = CmsTheme.colors.accent) },
                                onClick = { menuExpanded = false; onRequestDelete() },
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            val status = account.status.ifBlank { "UNKNOWN" }.uppercase(Locale.ROOT)
            StatusBadge(status, if (active) BadgeTone.Success else BadgeTone.Neutral)
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = ModTrack)
            Spacer(Modifier.height(13.dp))
            AdministratorDetailRow(TablerIcons.Login, "Last sign-in", relativeActivity(account.lastLoginAt, now))
        }
    }
}

@Composable
private fun AdministratorDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ModMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AdministratorEmptyState(filtered: Boolean, onAdd: () -> Unit, onClearFilters: () -> Unit, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = ModSurface, border = BorderStroke(1.dp, ModTrack)) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (filtered) "No matching administrators" else "No administrators found",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                if (filtered) "Try another email or account filter." else "Create an authorized full-access account.",
                color = ModMuted,
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(12.dp))
            CmsPrimaryButton(
                text = if (filtered) "Clear filters" else "Add administrator",
                onClick = if (filtered) onClearFilters else onAdd,
            )
        }
    }
}

@Composable
private fun AdministratorDetailDialog(
    account: AdministratorAccount,
    isCurrent: Boolean,
    now: Instant,
    busy: Boolean,
    onDismiss: () -> Unit,
    onRequestStatus: (String) -> Unit,
    onRequestResetPassword: () -> Unit,
    onRequestDelete: () -> Unit,
) {
    val active = account.status.equals("ACTIVE", ignoreCase = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(account.email, style = MaterialTheme.typography.headlineSmall) },
        text = {
            DialogScrollBody {
                Column {
                    StatusBadge(account.status.ifBlank { "UNKNOWN" }.uppercase(Locale.ROOT), if (active) BadgeTone.Success else BadgeTone.Neutral)
                    Spacer(Modifier.height(12.dp))
                    AdministratorDetailRow(TablerIcons.Login, "Last sign-in", relativeActivity(account.lastLoginAt, now))
                    Spacer(Modifier.height(8.dp))
                    AdministratorDetailRow(TablerIcons.ShieldCheck, "Created", formatAdministratorDate(account.createdAt))
                    if (!isCurrent) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = ModTrack)
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            CmsPrimaryButton(
                                text = if (active) "Disable account" else "Reactivate",
                                onClick = { onRequestStatus(if (active) "DISABLED" else "ACTIVE") },
                                enabled = !busy,
                            )
                            TextButton(onClick = onRequestResetPassword, enabled = !busy) { Text("Reset password") }
                            TextButton(onClick = onRequestDelete, enabled = !busy) { Text("Remove administrator", color = CmsTheme.colors.accent) }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
    )
}

@Composable
private fun AdministratorResetPasswordDialog(
    account: AdministratorAccount,
    busy: Boolean,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var newPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    val passwordError = FieldValidators.passwordError(newPassword)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset ${account.email}'s password", style = MaterialTheme.typography.headlineSmall) },
        text = {
            DialogScrollBody {
                Column {
                    Text(
                        "Set a new temporary password. Share it with ${account.email} directly -- they'll sign in with it.",
                        color = ModMuted,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New temporary password") },
                        isError = newPassword.isNotBlank() && passwordError != null,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = if (passwordVisible) "Hide password" else "Show password")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(newPassword) }, enabled = passwordError == null && !busy) {
                Text(if (busy) "Resetting" else "Reset password")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text("Cancel") } },
    )
}

@Composable
private fun CreateAdministratorDialog(
    existing: List<AdministratorAccount>,
    creating: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var accessConfirmed by remember { mutableStateOf(false) }

    val normalizedEmail = email.trim().lowercase(Locale.ROOT)
    val emailError = FieldValidators.emailError(normalizedEmail, required = false)
    val emailValid = emailError == null
    val duplicate = existing.any { it.email.equals(normalizedEmail, ignoreCase = true) }
    val passwordRules = FieldValidators.passwordRules(password)
    val passwordValid = FieldValidators.passwordError(password) == null
    val confirmationError = FieldValidators.passwordConfirmationError(password, confirmation)
    val confirmationValid = confirmationError == null
    val valid = emailValid && !duplicate && passwordValid && confirmationValid && accessConfirmed

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create full-access account", style = MaterialTheme.typography.headlineSmall) },
        text = { DialogScrollBody {
            Column {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    isError = normalizedEmail.isNotBlank() && (!emailValid || duplicate),
                    supportingText = {
                        val message = when {
                            duplicate -> "An administrator with this email already exists."
                            emailError != null && normalizedEmail.isNotBlank() -> emailError
                            else -> null
                        }
                        if (message != null) Text(message)
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(if (passwordVisible) TablerIcons.EyeOff else TablerIcons.Eye, contentDescription = "Toggle password visibility")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    passwordRules.forEach { rule: PasswordRule ->
                        StatusBadge(rule.label, if (rule.passed) BadgeTone.Success else BadgeTone.Neutral)
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmation,
                    onValueChange = { confirmation = it },
                    label = { Text("Confirm password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    isError = confirmation.isNotBlank() && !confirmationValid,
                    supportingText = { if (confirmation.isNotBlank() && confirmationError != null) Text(confirmationError) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = accessConfirmed, onCheckedChange = { accessConfirmed = it })
                    Text("I confirm this person is authorized for full administrative access.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }},
        confirmButton = {
            TextButton(onClick = { onCreate(normalizedEmail, password) }, enabled = valid && !creating) {
                if (creating) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Creating")
                } else {
                    Text("Create full-access account")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !creating) { Text("Cancel") }
        },
    )
}

private fun relativeActivity(value: Instant?, now: Instant): String {
    if (value == null) return "Never signed in"
    val days = Duration.between(value, now).toDays().coerceAtLeast(0)
    return when {
        days == 0L -> "Today"
        days == 1L -> "Yesterday"
        days in 2..29 -> "$days days ago"
        else -> formatAdministratorDate(value)
    }
}

private fun formatAdministratorDate(value: Instant?): String =
    value?.atZone(ZoneId.systemDefault())?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "Not available"
