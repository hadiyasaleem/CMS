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

    init {
        _loading.value = false
    }

    fun refresh() = launch {
        try {
            _loading.value = true
            repository.sync()
        } finally {
            _loading.value = false
        }
    }

    fun create(email: String, password: String) = launch {
        try {
            _creating.value = true
            _createdEmail.value = null

            val normalizedEmail = FieldValidators.normalizeEmail(email)
            requireValid(FieldValidators.emailError(normalizedEmail, required = true) == null) {
                "Enter a valid administrator email address."
            }
            requireValid(administrators.value.none { it.email.trim().equals(normalizedEmail, ignoreCase = true) }) {
                "An administrator with this email already exists."
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
}
