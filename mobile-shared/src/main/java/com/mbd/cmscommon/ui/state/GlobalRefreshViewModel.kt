package com.mbd.cmscommon.ui.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.data.sync.AdminDataBootstrapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Backs the refresh button shared by all three mobile apps (admin/student/teacher). Despite its
 * name, [AdminDataBootstrapper] resyncs every table used by any role, not just admin's — it's the
 * same routine that seeds the local cache on first launch. Using it here (instead of the narrower,
 * admin-reference-only [com.mbd.cmscommon.data.sync.SyncEngine]) means a student or teacher pulling
 * to refresh actually gets their attendance/marks/fees/notifications updated, not just departments
 * and sessions. Every sync it calls is checkpoint-based (`updated_at >= last checkpoint`), so this
 * stays cheap on repeat calls rather than re-fetching everything each time.
 */
@HiltViewModel
class GlobalRefreshViewModel @Inject constructor(
    private val dataBootstrapper: AdminDataBootstrapper,
) : ViewModel() {

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _refreshVersion = MutableStateFlow(0)
    val refreshVersion: StateFlow<Int> = _refreshVersion.asStateFlow()

    private val _tasksCompleted = MutableStateFlow(0)
    val tasksCompleted: StateFlow<Int> = _tasksCompleted.asStateFlow()

    val totalTasks: Int = AdminDataBootstrapper.TOTAL_SYNC_TASKS

    fun refresh() {
        if (_refreshing.value) return
        viewModelScope.launch {
            _tasksCompleted.value = 0
            _refreshing.value = true
            try {
                // onTaskDone fires from whichever sync task's own coroutine finishes it, so several
                // can land concurrently -- update() does an atomic read-modify-write, a plain
                // `_tasksCompleted.value += 1` here would drop updates under that concurrency.
                dataBootstrapper.refreshAll(onTaskDone = { _tasksCompleted.update { it + 1 } })
            } finally {
                _refreshVersion.value += 1
                _refreshing.value = false
            }
        }
    }
}
