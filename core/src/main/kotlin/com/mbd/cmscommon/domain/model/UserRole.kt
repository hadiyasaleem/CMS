package com.mbd.cmscommon.domain.model

sealed interface UserRole {
    val uid: String

    data class Admin(override val uid: String) : UserRole

    data class Teacher(
        override val uid: String,
        val teacherId: String,
        val permissions: TeacherPermissions,
        val isAdmin: Boolean = false,
    ) : UserRole

    data class LinkedStudent(override val uid: String, val studentId: String) : UserRole

    data class UnlinkedStudent(override val uid: String) : UserRole
}

/** A full-access teacher (Teacher.isAdmin) is trusted identically to a real Admin profile at the
 * RLS layer (is_admin() in the schema checks both) -- this lets every admin-app gate treat the two
 * uniformly by normalizing to a synthetic UserRole.Admin(uid), without threading the distinction
 * through the rest of the admin app's code. */
fun UserRole?.asAdminOrDelegate(): UserRole.Admin? = when {
    this is UserRole.Admin -> this
    this is UserRole.Teacher && isAdmin -> UserRole.Admin(uid)
    else -> null
}
