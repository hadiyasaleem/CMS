package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.TeacherPermissions

data class TeacherAccountDraft(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val deptId: String = "",
    val designation: String = "",
    val qualification: String = "",
    val specialization: String = "",
    val officeRoom: String = "",
    val gender: String = "",
    val password: String = "",
    val permissions: TeacherPermissions = TeacherPermissions(),
    val isAdmin: Boolean = false,
)
