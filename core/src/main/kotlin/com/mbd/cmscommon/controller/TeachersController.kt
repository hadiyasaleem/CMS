package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Department
import com.mbd.cmscommon.domain.model.Room
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import com.mbd.cmscommon.domain.repository.DepartmentRepository
import com.mbd.cmscommon.domain.repository.RoomRepository
import com.mbd.cmscommon.domain.repository.TeacherRepository
import com.mbd.cmscommon.teacher.ResolvedAssignment
import com.mbd.cmscommon.teacher.TeacherAssignmentsProvider
import com.mbd.cmscommon.util.FieldValidators
import com.mbd.cmscommon.util.orThrowValidation
import com.mbd.cmscommon.util.requireValid
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class TeachersController(
    private val teacherRepository: TeacherRepository,
    private val departmentRepository: DepartmentRepository,
    private val roomRepository: RoomRepository,
    private val assignmentsProvider: TeacherAssignmentsProvider,
    private val editedBy: String,
    scope: CoroutineScope,
) : ScreenController(scope) {

    val teachers: StateFlow<List<Teacher>> =
        teacherRepository.observeActiveTeachers().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val departments: StateFlow<List<Department>> =
        departmentRepository.observeActiveDepartments().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val rooms: StateFlow<List<Room>> =
        roomRepository.observeActiveRooms().stateIn(scope, SharingStarted.Eagerly, emptyList())

    val assignments: StateFlow<Map<String, List<ResolvedAssignment>>> = teachers
        .flatMapLatest { roster ->
            if (roster.isEmpty()) {
                flowOf(emptyMap())
            } else {
                combine(roster.map { teacher -> assignmentsProvider.observeAssignmentsFor(teacher.teacherId).map { teacher.teacherId to it } }) { it.toMap() }
            }
        }
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _loading = MutableStateFlow(true)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _creating = MutableStateFlow(false)
    val creating: StateFlow<Boolean> = _creating.asStateFlow()

    private val _busyTeacherId = MutableStateFlow<String?>(null)
    val busyTeacherId: StateFlow<String?> = _busyTeacherId.asStateFlow()

    private val _notice = MutableStateFlow<String?>(null)
    val notice: StateFlow<String?> = _notice.asStateFlow()

    init {
        _loading.value = false
    }

    fun refresh() = launch {
        _loading.value = true
        try {
            coroutineScope {
                listOf(
                    async { teacherRepository.sync() },
                    async { departmentRepository.sync() },
                    async { roomRepository.sync() },
                ).awaitAll()
            }
        } finally {
            _loading.value = false
        }
    }

    fun createTeacher(draft: TeacherAccountDraft) = launch {
        try {
            _creating.value = true
            _notice.value = null
            val normalized = validateDraft(draft, creatingAccount = true)
            requireValid(teachers.value.none { it.email.trim().equals(normalized.email, ignoreCase = true) }) {
                "A teacher account with this email already exists."
            }

            val now = Instant.now()
            teacherRepository.createTeacherAccount(
                normalized.email,
                normalized.password,
                Teacher(
                    teacherId = normalized.email,
                    name = normalized.name,
                    email = normalized.email,
                    phone = normalized.phone.nullIfBlank(),
                    deptId = normalized.deptId.nullIfBlank(),
                    designation = normalized.designation.nullIfBlank(),
                    qualification = normalized.qualification.nullIfBlank(),
                    specialization = normalized.specialization.nullIfBlank(),
                    officeRoom = normalized.officeRoom.nullIfBlank(),
                    gender = normalized.gender.nullIfBlank(),
                    permissions = normalized.permissions,
                    isAdmin = normalized.isAdmin,
                    status = TeacherStatus.ACTIVE,
                    isActive = true,
                    createdAt = now,
                    createdBy = editedBy,
                    updatedAt = now,
                    updatedBy = editedBy,
                ),
            )
            _notice.value = "${normalized.name} was added to the faculty directory."
        } finally {
            _creating.value = false
        }
    }

    fun updateTeacher(original: Teacher, draft: TeacherAccountDraft) = launch {
        try {
            _busyTeacherId.value = original.teacherId
            _notice.value = null
            val normalized = validateDraft(draft.copy(email = original.email), creatingAccount = false)
            teacherRepository.updateTeacher(
                original.copy(
                    name = normalized.name,
                    phone = normalized.phone.nullIfBlank(),
                    deptId = normalized.deptId.nullIfBlank(),
                    designation = normalized.designation.nullIfBlank(),
                    qualification = normalized.qualification.nullIfBlank(),
                    specialization = normalized.specialization.nullIfBlank(),
                    officeRoom = normalized.officeRoom.nullIfBlank(),
                    gender = normalized.gender.nullIfBlank(),
                    permissions = normalized.permissions,
                    isAdmin = normalized.isAdmin,
                    updatedAt = Instant.now(),
                    updatedBy = editedBy,
                ),
            )
            _notice.value = "${normalized.name}'s profile was updated."
        } finally {
            _busyTeacherId.value = null
        }
    }

    fun setStatus(teacher: Teacher, status: TeacherStatus) = launch {
        try {
            _busyTeacherId.value = teacher.teacherId
            _notice.value = null
            teacherRepository.setStatus(teacher.teacherId, status)
            _notice.value = when (status) {
                TeacherStatus.ACTIVE -> "${teacher.name}'s account was reactivated."
                TeacherStatus.DISABLED -> "${teacher.name}'s account was disabled."
                TeacherStatus.BANNED -> "${teacher.name}'s account was banned."
            }
        } finally {
            _busyTeacherId.value = null
        }
    }

    fun deleteTeacher(teacher: Teacher) = launch {
        try {
            _busyTeacherId.value = teacher.teacherId
            _notice.value = null
            teacherRepository.deleteTeacher(teacher.teacherId)
            _notice.value = "${teacher.name} was removed from the active faculty directory."
        } finally {
            _busyTeacherId.value = null
        }
    }

    fun uploadPhoto(teacher: Teacher, imageBytes: ByteArray, mimeType: String) = launch {
        try {
            _busyTeacherId.value = teacher.teacherId
            _notice.value = null
            teacherRepository.uploadPhoto(teacher.teacherId, imageBytes, mimeType)
            _notice.value = "${teacher.name}'s photo was updated."
        } finally {
            _busyTeacherId.value = null
        }
    }

    fun consumeNotice() {
        _notice.value = null
    }

    private fun validateDraft(draft: TeacherAccountDraft, creatingAccount: Boolean): TeacherAccountDraft {
        val normalized = draft.copy(
            name = draft.name.trim(),
            email = FieldValidators.normalizeEmail(draft.email),
            phone = draft.phone.trim(),
            deptId = draft.deptId.trim(),
            designation = draft.designation.trim(),
            qualification = draft.qualification.trim(),
            specialization = draft.specialization.trim(),
            officeRoom = draft.officeRoom.trim(),
            gender = draft.gender.trim(),
        )
        FieldValidators.nameError(normalized.name, "Teacher name").orThrowValidation()
        requireValid(FieldValidators.emailError(normalized.email) == null) { "Enter a valid teacher email address." }
        FieldValidators.phoneError(normalized.phone).orThrowValidation()
        requireValid(FieldValidators.textError(normalized.designation, "Designation", required = false, maxLength = 80) == null) {
            "Designation must not exceed 80 characters."
        }
        requireValid(FieldValidators.textError(normalized.qualification, "Qualification", required = false, maxLength = 120) == null) {
            "Qualification must not exceed 120 characters."
        }
        requireValid(FieldValidators.textError(normalized.specialization, "Specialization", required = false, maxLength = 120) == null) {
            "Specialization must not exceed 120 characters."
        }
        requireValid(FieldValidators.textError(normalized.officeRoom, "Office", required = false, maxLength = 40) == null) {
            "Office must not exceed 40 characters."
        }
        val genderValid = normalized.gender.isBlank() || normalized.gender.uppercase(Locale.ROOT) in setOf("MALE", "FEMALE", "OTHER")
        requireValid(genderValid) { "Gender must be Male, Female, or Other." }

        val deptValid = normalized.deptId.isBlank() || departments.value.any { it.deptId == normalized.deptId }
        requireValid(deptValid) { "Choose a valid department." }

        if (creatingAccount) {
            FieldValidators.passwordError(normalized.password).orThrowValidation()
        }

        return normalized.copy(gender = normalized.gender.uppercase(Locale.ROOT))
    }
}

private fun String.nullIfBlank(): String? = takeIf { it.isNotBlank() }
