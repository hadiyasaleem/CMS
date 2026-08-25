package com.mbd.cmscommon.domain.model

sealed interface UserRole {
    val uid: String

    data class Admin(override val uid: String) : UserRole

    data class Teacher(
        override val uid: String,
        val teacherId: String,
        val permissions: TeacherPermissions,
    ) : UserRole

    data class LinkedStudent(override val uid: String, val studentId: String) : UserRole

    data class UnlinkedStudent(override val uid: String) : UserRole
}
