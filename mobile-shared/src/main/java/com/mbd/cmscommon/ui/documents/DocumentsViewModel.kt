package com.mbd.cmscommon.ui.documents

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.DocumentsController
import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Document
import com.mbd.cmscommon.domain.model.DocumentViewerContext
import com.mbd.cmscommon.domain.model.DocumentViewerRole
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.DocumentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.util.FileOpener
import com.mbd.cmscommon.util.StudentIdCodec
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class DocumentsViewModel @Inject constructor(
    repo: DocumentRepository,
    userRepository: UserRepository,
    teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    sessionManager: SessionManager,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {

    val controller = DocumentsController(repo, viewModelScope)

    val accountKey: String = sessionManager.accountKey ?: ""

    val resolvedViewer: StateFlow<DocumentViewerContext?> = combine(
        userRepository.observeCurrentUserRole(),
        teacherRepository.observeTeacher(accountKey),
    ) { role, teacher ->
        when (role) {
            is UserRole.Admin -> DocumentViewerContext(DocumentViewerRole.ADMIN)
            is UserRole.Teacher -> DocumentViewerContext(DocumentViewerRole.TEACHER, teacher?.deptId)
            is UserRole.LinkedStudent -> {
                val sessionId = StudentIdCodec.sessionIdOf(role.studentId)
                DocumentViewerContext(DocumentViewerRole.STUDENT, StudentIdCodec.deptIdOf(sessionId))
            }
            is UserRole.UnlinkedStudent -> DocumentViewerContext(DocumentViewerRole.STUDENT)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val departments: StateFlow<List<Department>> = departmentRepository.observeActiveDepartments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun open(context: Context, document: Document) {
        controller.openDocument(document, appContext.cacheDir) { file ->
            FileOpener.open(context, file, "application/pdf")
        }
    }
}
