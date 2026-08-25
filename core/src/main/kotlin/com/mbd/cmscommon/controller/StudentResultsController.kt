package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.SemesterGpa
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StudentResultsController(
    private val sessionId: String,
    private val rollNumber: String,
    private val marksRepository: SessionMarksRepository,
    scope: CoroutineScope,
) : ScreenController(scope) {

    private val _results = MutableStateFlow<List<SemesterGpa>>(emptyList())
    val results: StateFlow<List<SemesterGpa>> = _results.asStateFlow()

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        clearError()
        _loading.value = true
        launch {
            try {
                _results.value = marksRepository.getSemesterGpa(sessionId, rollNumber)
            } finally {
                _loading.value = false
            }
        }
    }
}
