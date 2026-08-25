package com.mbd.cmsadmin.feature.administrators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.AdministratorsController
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AdministratorsViewModel @Inject constructor(
    repository: AdministratorRepository,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = AdministratorsController(
        repository = repository,
        currentAccountKey = sessionManager.accountKey,
        scope = viewModelScope,
    )

    val currentAccountKey = controller.currentAccountKey
    val administrators = controller.administrators
    val loading = controller.loading
    val creating = controller.creating
    val createdEmail = controller.createdEmail
    val error = controller.error

    fun refresh() = controller.refresh()
    fun create(email: String, password: String) = controller.create(email, password)
    fun consumeCreated() = controller.consumeCreated()
    fun clearError() = controller.clearError()
}
