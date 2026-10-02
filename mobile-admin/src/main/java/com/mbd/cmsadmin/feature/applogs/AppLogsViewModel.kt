package com.mbd.cmsadmin.feature.applogs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.AppLogsController
import com.mbd.cmscommon.domain.repository.AppLogRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AppLogsViewModel @Inject constructor(
    repository: AppLogRepository,
) : ViewModel() {
    private val controller = AppLogsController(repository, viewModelScope)

    val logs = controller.logs
    val loading = controller.loading
    val error = controller.error

    fun refresh() = controller.refresh()
    fun clearError() = controller.clearError()
}
