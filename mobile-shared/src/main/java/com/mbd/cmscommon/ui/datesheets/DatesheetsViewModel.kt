package com.mbd.cmscommon.ui.datesheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.TeacherDatesheetController
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Teacher-only "My Datesheet" screen support: no filters, pre-loaded with the teacher's own
 * subjects' published exam slots. */
@HiltViewModel
class DatesheetsViewModel @Inject constructor(
    datesheetRepository: DatesheetRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val controller = TeacherDatesheetController(datesheetRepository, assignmentsProvider, viewModelScope)

    val identityKey: String? get() = sessionManager.accountKey
}
