package com.mbd.cmscommon.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordChangeRulesTest {
    private val strong = "Newpass#2026"

    @Test
    fun aValidChangePasses() {
        assertNull(FieldValidators.passwordChangeError("Oldpass#2025", strong, strong))
    }

    @Test
    fun theCurrentPasswordIsRequired() {
        assertEquals("Enter your current password.", FieldValidators.passwordChangeError("", strong, strong))
    }

    @Test
    fun theNewPasswordMustMeetTheSameRulesAsRegistration() {
        assertEquals(
            "Use 8 or more characters with uppercase, lowercase, number, and symbol.",
            FieldValidators.passwordChangeError("Oldpass#2025", "weak", "weak"),
        )
    }

    @Test
    fun theNewPasswordMustDifferFromTheCurrentOne() {
        assertEquals(
            "The new password must be different from your current one.",
            FieldValidators.passwordChangeError(strong, strong, strong),
        )
    }

    @Test
    fun theRetypedPasswordMustMatch() {
        assertEquals("Passwords do not match.", FieldValidators.passwordChangeError("Oldpass#2025", strong, "Other#2026x"))
        assertEquals("Confirm the password.", FieldValidators.passwordChangeError("Oldpass#2025", strong, ""))
    }

    @Test
    fun aWrongCurrentPasswordIsShownAsSuch() {
        val message = ErrorClassifier.classify(CmsException.Validation("Your current password is incorrect.", "currentPassword")).userMessage
        assertEquals("Your current password is incorrect.", message)
    }
}
