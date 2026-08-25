package com.mbd.cmsdesktop.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.mbd.cmscommon.controller.AdministratorsController
import com.mbd.cmscommon.domain.repository.AdministratorRepository
import com.mbd.cmscommon.ui.components.AdministratorDirectoryWorkspace

@Composable
fun AdministratorsScreen(repository: AdministratorRepository, currentAccountKey: String?) {
    val scope = rememberCoroutineScope()
    val controller = remember(repository, currentAccountKey) {
        AdministratorsController(repository, currentAccountKey, scope)
    }
    val administrators by controller.administrators.collectAsState()
    val loading by controller.loading.collectAsState()
    val creating by controller.creating.collectAsState()
    val createdEmail by controller.createdEmail.collectAsState()
    val errorMessage by controller.error.collectAsState()

    AdministratorDirectoryWorkspace(
        administrators = administrators,
        currentAccountKey = currentAccountKey,
        loading = loading,
        creating = creating,
        createdEmail = createdEmail,
        errorMessage = errorMessage,
        onRefresh = controller::refresh,
        onCreate = controller::create,
        onConsumeCreated = controller::consumeCreated,
        onClearError = controller::clearError,
    )
}
