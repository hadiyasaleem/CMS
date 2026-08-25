package com.mbd.cmsteacher.feature.auth

import com.mbd.cmscommon.auth.SessionManager
import com.mbd.cmscommon.domain.model.UserRole
import com.mbd.cmscommon.domain.repository.UserRepository
import com.mbd.cmscommon.ui.auth.RoleLoginViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    sessionManager: SessionManager,
    userRepository: UserRepository,
) : RoleLoginViewModel(sessionManager, userRepository) {

    override val wrongRoleMessage = "This account is not a Teacher account"

    override fun isAccepted(role: UserRole) = role is UserRole.Teacher
}
