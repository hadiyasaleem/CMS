package com.mbd.cmsstudent.feature.marks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.controller.StudentMarksController
import com.mbd.cmscommon.controller.StudentMarksSnapshot
import com.mbd.cmscommon.controller.studentMarksSnapshot
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.SessionMarksRepository
import com.mbd.cmsstudent.feature.common.CurrentStudentProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MyMarksViewModel @Inject constructor(
    currentStudentProvider: CurrentStudentProvider,
    private val marksRepository: SessionMarksRepository,
    private val curriculumRepository: CurriculumRepository,
) : ViewModel() {

    private var controller: StudentMarksController? = null

    val snapshot = currentStudentProvider.observeContext()
        .distinctUntilChangedBy { it?.studentId }
        .flatMapLatest { context ->
            if (context == null) {
                controller = null
                flowOf<StudentMarksSnapshot?>(null)
            } else {
                val c = StudentMarksController(context.sessionId, context.rollNumber, marksRepository, curriculumRepository, viewModelScope)
                controller = c
                c.rows.map { rows -> studentMarksSnapshot(rows) }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun refresh() {
        viewModelScope.launch { controller?.refresh() }
    }
}
