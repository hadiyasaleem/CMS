package com.mbd.cmscommon.auth

import com.mbd.cmscommon.util.userMessageLogged
import kotlin.coroutines.cancellation.CancellationException

/** Changes the signed-in user's password; null on success, otherwise the sentence to show (wrong current password, offline, ...). */
suspend fun SessionManager.changePasswordMessage(currentPassword: String, newPassword: String): String? =
    try {
        changePassword(currentPassword, newPassword)
        null
    } catch (c: CancellationException) {
        throw c
    } catch (t: Throwable) {
        t.userMessageLogged("SessionManager.changePassword", "Couldn't change your password.")
    }
