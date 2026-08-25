package com.mbd.cmscommon.util

object Validators {
    fun isValidEmail(email: String): Boolean = FieldValidators.emailError(email) == null

    fun isValidPassword(password: String): Boolean = password.length >= 6

    fun isStrongPassword(password: String): Boolean = FieldValidators.passwordError(password) == null
}
