package com.mbd.cmscommon.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.data.sync.SyncEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class GlobalRefreshViewModel @Inject constructor(
    private val syncEngine: SyncEngine,
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshVersion = MutableStateFlow(0)
    val refreshVersion: StateFlow<Int> = _refreshVersion.asStateFlow()

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _refreshing.value = true
            try {
                syncEngine.refreshAdminReferenceData()
            } finally {
                _refreshVersion.value += 1
                _refreshing.value = false
            }
        }
    }
}
