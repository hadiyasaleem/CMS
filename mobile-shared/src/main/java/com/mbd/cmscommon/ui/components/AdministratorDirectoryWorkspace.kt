package com.mbd.cmscommon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.vector.ImageVector
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

data class AdministratorSummary(
    val label: String,
    val value: String,
    val detail: String,
    val icon: ImageVector,
)

@Composable
fun AdministratorDirectoryWorkspace(
    administrators: List<AdministratorAccount>,
    currentAccountKey: String?,
    loading: Boolean,
    creating: Boolean,
    createdEmail: String?,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onCreate: (String, String) -> Unit,
    onConsumeCreated: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(AdministratorFilter.ALL) }
    var sort by remember { mutableStateOf(AdministratorSort.EMAIL) }
    var showCreateDialog by remember { mutableStateOf(false) }

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

    val summaries = listOf(
        AdministratorSummary("Total admins", directory.accounts.size.toString(), "Unique full-access accounts", Icons.Outlined.Groups),
        AdministratorSummary("Active", directory.activeCount.toString(), "${directory.unavailableCount} unavailable", Icons.Outlined.CheckCircle),
        AdministratorSummary("Recent", directory.recentlyActiveCount.toString(), "Signed in within 30 days", Icons.AutoMirrored.Outlined.Login),
        AdministratorSummary("Pending use", directory.neverSignedInCount.toString(), "Have never signed in", Icons.Outlined.History),
    )

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = { CmsFab(onClick = { showCreateDialog = true }, contentDescription = "Add administrator") },
    ) { padding ->
        RefreshBox(isRefreshing = loading, onRefresh = onRefresh, modifier = Modifier.padding(padding)) {
            LazyColumn(Modifier.fillMaxWidth(), contentPadding = PaddingValues(16.dp)) {
                item { AdministratorHero(directory.accounts.size) }

                if (!createdEmail.isNullOrBlank()) {
                    item {
                        Spacer(Modifier.height(12.dp))
                        AdministratorCreatedBanner(createdEmail, onConsumeCreated)
                    }
                }

                item {
                    Spacer(Modifier.height(12.dp))
                    SecurityNotice()
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        summaries.forEach { summary -> AdministratorSummaryCard(summary, Modifier.weight(1f)) }
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    AdministratorDirectoryControls(
                        query = query,
                        onQueryChange = { query = it },
                        filter = filter,
                        onFilterChange = { filter = it },
                        sort = sort,
                        onSortChange = { sort = it },
                        visibleCount = visibleAdministrators.size,
                        totalCount = directory.accounts.size,
                    )
                }

                item { Spacer(Modifier.height(12.dp)) }

                if (!loading && directory.accounts.isEmpty()) {
                    item { AdministratorEmptyState(filtered = false, onAdd = { showCreateDialog = true }, onClearFilters = {}) }
                } else if (visibleAdministrators.isEmpty()) {
                    item {
                        AdministratorEmptyState(
                            filtered = true,
                            onAdd = { showCreateDialog = true },
                            onClearFilters = { query = ""; filter = AdministratorFilter.ALL },
                        )
                    }
                } else {
                    items(visibleAdministrators, key = { it.id }) { account ->
                        val isCurrent = currentKey != null && (account.id == currentKey || account.email.equals(currentKey, ignoreCase = true))
                        Spacer(Modifier.height(10.dp))
                        AdministratorCard(account, isCurrent, now)
                    }
                }

                item { Spacer(Modifier.height(88.dp)) }
            }
        }
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

    if (!errorMessage.isNullOrBlank()) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Something went wrong") },
            text = { Text(errorMessage, color = CmsTheme.colors.accent) },
            confirmButton = { TextButton(onClick = onClearError) { Text("OK") } },
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
            Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null, tint = ModInk)
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
            Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = ModSuccess)
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Administrator created", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(email, color = ModMuted, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDismiss) { Icon(Icons.Filled.Close, contentDescription = "Dismiss") }
        }
    }
}

@Composable
private fun AdministratorSummaryCard(summary: AdministratorSummary, modifier: Modifier = Modifier) {
    CmsCard(modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(summary.icon, contentDescription = null, tint = CmsTheme.colors.accent, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(10.dp))
            Text(summary.value, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
            Text(summary.label.uppercase(Locale.ROOT), color = ModMuted, style = CmsTextStyles.eyebrow)
            Text(summary.detail, color = ModMuted, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AdministratorDirectoryControls(
    query: String,
    onQueryChange: (String) -> Unit,
    filter: AdministratorFilter,
    onFilterChange: (AdministratorFilter) -> Unit,
    sort: AdministratorSort,
    onSortChange: (AdministratorSort) -> Unit,
    visibleCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by email") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
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
                CmsChip(option.label, selected = filter == option, onClick = { onFilterChange(option) })
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
                CmsChip(option.label, selected = sort == option, onClick = { onSortChange(option) })
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("Showing $visibleCount of $totalCount accounts", color = ModMuted, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun AdministratorCard(account: AdministratorAccount, isCurrent: Boolean, now: Instant, modifier: Modifier = Modifier) {
    val active = account.status.equals("ACTIVE", ignoreCase = true)
    CmsCard(modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarInitials(account.email.substringBefore('@'), size = 42)
                Spacer(Modifier.size(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(account.email, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(14.dp))
            Row {
                val status = account.status.ifBlank { "UNKNOWN" }.uppercase(Locale.ROOT)
                StatusBadge(status, if (active) BadgeTone.Success else BadgeTone.Neutral)
                Spacer(Modifier.size(8.dp))
                StatusBadge(if (isCurrent) "YOU" else "FULL ACCESS", if (isCurrent) BadgeTone.Navy else BadgeTone.Warning)
            }
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = ModTrack)
            Spacer(Modifier.height(13.dp))
            AdministratorDetailRow(Icons.AutoMirrored.Outlined.Login, "Last sign-in", relativeActivity(account.lastLoginAt, now))
            Spacer(Modifier.height(9.dp))
            AdministratorDetailRow(Icons.Outlined.Security, "Scope", "College-wide administration")
        }
    }
}

@Composable
private fun AdministratorDetailRow(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ModMuted, modifier = Modifier.size(16.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, modifier = Modifier.weight(1f), color = ModMuted, style = MaterialTheme.typography.bodySmall)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun AdministratorEmptyState(filtered: Boolean, onAdd: () -> Unit, onClearFilters: () -> Unit, modifier: Modifier = Modifier) {
    CmsCard(modifier.fillMaxWidth()) {
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
        text = {
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
                            Icon(if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility, contentDescription = "Toggle password visibility")
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
        },
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
