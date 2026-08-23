package com.mbd.cmsadmin.feature.auth

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

    override val wrongRoleMessage = "This account is not an Admin account"

    override fun isAccepted(role: UserRole?) = role is UserRole.Admin

    // First-run bootstrap: after a fresh Firestore, the single designated admin has no
    // users/{email} doc yet and the rules forbid self-promotion for anyone else. The one
    // whitelisted admin email (mirrored in firestore.rules) provisions its own ADMIN doc here,
    // once, then re-resolves.
    override suspend fun afterRoleResolved(accountKey: String, role: UserRole?): UserRole? {
        if (role == null && accountKey == ADMIN_BOOTSTRAP_EMAIL) {
            userRepository.provisionAdmin(accountKey)
            return userRepository.resolveRole(accountKey)
        }
        return role
    }

    companion object {
        /** The single designated bootstrap admin email — must match the allowance in firestore.rules. */
        const val ADMIN_BOOTSTRAP_EMAIL = "admin@example.com"
    }
}
