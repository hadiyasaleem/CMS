package com.mbd.cmscommon.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.util.Outcome
import com.mbd.cmscommon.util.userMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SyncedListState<T>(
    scope: CoroutineScope,
    roomFlow: Flow<List<T>>,
    private val onRefresh: suspend () -> Unit,
) {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private val _refreshError = MutableStateFlow<String?>(null)
    val refreshError: StateFlow<String?> = _refreshError

    val items: StateFlow<Outcome<List<T>>> = roomFlow
        .map<List<T>, Outcome<List<T>>> { Outcome.Success(it) }
        .catch { throwable -> emit(Outcome.Error(throwable.userMessage("Could not load this information."), throwable)) }
        .stateIn(scope, SharingStarted.WhileSubscribed(5_000), Outcome.Loading)

    fun refresh(scope: CoroutineScope) {
        if (_isRefreshing.value) return
        scope.launch {
            _isRefreshing.value = true
            try {
                onRefresh()
                _refreshError.value = null
            } catch (t: Throwable) {
                _refreshError.value = t.userMessage("Refresh failed. Please try again.")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}

fun <T> ViewModel.syncedListState(roomFlow: Flow<List<T>>, onRefresh: suspend () -> Unit): SyncedListState<T> =
    SyncedListState(viewModelScope, roomFlow, onRefresh)
