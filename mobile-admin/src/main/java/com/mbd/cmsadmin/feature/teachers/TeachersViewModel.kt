package com.mbd.cmsadmin.feature.teachers

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.controller.TeacherAccountDraft
import com.mbd.cmscommon.controller.TeachersController
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TeachersViewModel @Inject constructor(
    private val teacherRepository: TeacherRepository,
    departmentRepository: DepartmentRepository,
    assignmentsProvider: TeacherAssignmentsProvider,
    sessionManager: SessionManager,
) : ViewModel() {
    private val controller = TeachersController(
        teacherRepository = teacherRepository,
        departmentRepository = departmentRepository,
        assignmentsProvider = assignmentsProvider,
        editedBy = sessionManager.accountKey.orEmpty(),
        scope = viewModelScope,
    )

    val teachers = controller.teachers
    val departments = controller.departments
    val assignments = controller.assignments
    val loading = controller.loading
    val creating = controller.creating
    val busyTeacherId = controller.busyTeacherId
    val notice = controller.notice
    val error = controller.error

    fun refresh() = controller.refresh()
    fun createTeacher(draft: TeacherAccountDraft) = controller.createTeacher(draft)
    fun updateTeacher(teacher: Teacher, draft: TeacherAccountDraft) = controller.updateTeacher(teacher, draft)
    fun setStatus(teacher: Teacher, status: TeacherStatus) = controller.setStatus(teacher, status)
    fun deleteTeacher(teacher: Teacher) = controller.deleteTeacher(teacher)
    fun uploadPhoto(teacher: Teacher, imageBytes: ByteArray, mimeType: String) = controller.uploadPhoto(teacher, imageBytes, mimeType)
    suspend fun loadPhoto(photoPath: String): ImageBitmap? {
        val bytes = teacherRepository.downloadPhoto(photoPath) ?: return null
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    }
    fun consumeNotice() = controller.consumeNotice()
    fun clearError() = controller.clearError()
}
