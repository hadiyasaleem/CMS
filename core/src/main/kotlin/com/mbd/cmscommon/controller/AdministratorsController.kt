package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AdministratorAccount
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class AdministratorsController(
    private val repository: AdministratorRepository,
    val currentAccountKey: String?,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val administrators: StateFlow<List<AdministratorAccount>> = repository.observeAdministrators()
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating.asStateFlow()

    private val _createdEmail = MutableStateFlow<String?>(null)
    val createdEmail: StateFlow<String?> = _createdEmail.asStateFlow()

    private val _busyAdminKey = MutableStateFlow<String?>(null)
    val busyAdminKey: StateFlow<String?> = _busyAdminKey.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        _loading.value = false
    }

    private fun isSelfAccount(account: AdministratorAccount): Boolean {
        val key = currentAccountKey?.trim() ?: return false
        return account.id == key || account.email.equals(key, ignoreCase = true)
    }

    fun refresh() = launch("refresh the administrators") {
        try {
            _loading.value = true
            repository.sync()
        } finally {
            _loading.value = false
        }
    }

    fun create(email: String, password: String) = launch("create the administrator") {
        try {
            _creating.value = true
            _createdEmail.value = null

            val normalizedEmail = FieldValidators.normalizeEmail(email)
            requireValid(FieldValidators.emailError(normalizedEmail, required = true) == null) {
                "Enter a valid administrator email address."
            }
            requireValid(administrators.value.none { it.email.trim().equals(normalizedEmail, ignoreCase = true) }) {
                "An administrator with the email $normalizedEmail already exists."
            }
            FieldValidators.passwordError(password).orThrowValidation()

            repository.createAdministrator(normalizedEmail, password)
            _createdEmail.value = normalizedEmail
        } finally {
            _creating.value = false
        }
    }

    fun consumeCreated() {
        _createdEmail.value = null
    }

    fun setStatus(account: AdministratorAccount, status: String) = launch("change the administrator's status") {
        requireValid(!isSelfAccount(account)) { "You cannot change your own account status." }
        try {
            _busyAdminKey.value = account.id
            _notice.value = null
            repository.setStatus(account.email, status)
            _notice.value = if (status == "ACTIVE") {
                "${account.email}'s account was reactivated."
            } else {
                "${account.email}'s account was disabled."
            }
        } finally {
            _busyAdminKey.value = null
        }
    }

    fun resetPassword(account: AdministratorAccount, newPassword: String) = launch("reset the administrator's password") {
        try {
            _busyAdminKey.value = account.id
            _notice.value = null
            FieldValidators.passwordError(newPassword).orThrowValidation()
            repository.resetPassword(account.email, newPassword)
            _notice.value = "${account.email}'s password was reset."
        } finally {
            _busyAdminKey.value = null
        }
    }

    fun deleteAdministrator(account: AdministratorAccount) = launch("remove the administrator") {
        requireValid(!isSelfAccount(account)) { "You cannot delete your own account." }
        try {
            _busyAdminKey.value = account.id
            _notice.value = null
            repository.deleteAdministrator(account.email)
            _notice.value = "${account.email} was removed from the administrator directory."
        } finally {
            _busyAdminKey.value = null
        }
    }

    fun consumeNotice() {
        _notice.value = null
    }
}
