package com.mbd.cmscommon.ui.datesheets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DatesheetsController
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.DatesheetViewerContext
import com.mbd.cmscommon.domain.model.DatesheetViewerRole
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import com.mbd.cmscommon.domain.repository.CurriculumRepository
import com.mbd.cmscommon.domain.repository.DatesheetRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.util.StudentIdCodec
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class DatesheetsViewModel @Inject constructor(
    datesheetRepository: DatesheetRepository,
    private val userRepository: UserRepository,
    private val sessionRepository: AcademicSessionRepository,
    private val curriculumRepository: CurriculumRepository,
    private val teacherRepository: TeacherRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val accountKey: String? = sessionManager.accountKey

    val controller = DatesheetsController(datesheetRepository, accountKey ?: "", viewModelScope)

    val sessions: StateFlow<List<AcademicSession>> = sessionRepository.observeAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val viewer: StateFlow<DatesheetViewerContext> = userRepository.observeCurrentUserRole()
        .map { role -> buildViewerContext(role) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DatesheetViewerContext(DatesheetViewerRole.STUDENT))

    val invigilators: StateFlow<List<Teacher>> = if (accountKey != null) {
        teacherRepository.observeTeacher(accountKey)
            .map { listOf(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    } else {
        MutableStateFlow(emptyList())
    }

    private val _subjectsBySession = MutableStateFlow<Map<String, List<SemesterSubject>>>(emptyMap())
    val subjectsBySession: StateFlow<Map<String, List<SemesterSubject>>> = _subjectsBySession.asStateFlow()

    private val loadingSubjects = mutableSetOf<String>()

    fun loadSubjects(sessionId: String) {
        if (sessionId.isBlank() || _subjectsBySession.value.containsKey(sessionId) || loadingSubjects.contains(sessionId)) return
        loadingSubjects += sessionId
        viewModelScope.launch {
            try {

                val subjects = curriculumRepository.observeSessionSubjects(sessionId)
                _subjectsBySession.value = _subjectsBySession.value + (sessionId to emptyList())
                subjects.collect { list ->
                    _subjectsBySession.value = _subjectsBySession.value + (sessionId to list)
                }
            } finally {
                loadingSubjects -= sessionId
            }
        }
    }

    private fun buildViewerContext(role: UserRole?): DatesheetViewerContext = when (role) {
        null -> DatesheetViewerContext(DatesheetViewerRole.STUDENT)
        is UserRole.Admin -> DatesheetViewerContext(DatesheetViewerRole.ADMIN, canManage = true)
        is UserRole.Teacher -> DatesheetViewerContext(DatesheetViewerRole.TEACHER, canManage = true, identityKey = role.teacherId)
        is UserRole.LinkedStudent -> DatesheetViewerContext(
            DatesheetViewerRole.STUDENT,
            sessionId = StudentIdCodec.sessionIdOf(role.studentId),
            identityKey = role.studentId,
        )
        is UserRole.UnlinkedStudent -> DatesheetViewerContext(DatesheetViewerRole.STUDENT)
    }
}
