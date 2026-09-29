package com.mbd.cmscommon.util

import java.io.FileNotFoundException
import java.io.IOException
import java.util.zip.ZipException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAndFileErrorsTest {

    private fun classify(message: String) = ErrorClassifier.classify(RuntimeException(message), "Couldn't sign in.")

    // ---- auth, by GoTrue error code / message ----

    @Test
    fun wrongCredentialsAreExplained() {
        val c = classify("Invalid login credentials")
        assertEquals(ErrorKind.AUTH, c.kind)
        assertEquals("The email or password is incorrect.", c.userMessage)
    }

    @Test
    fun anUnconfirmedEmailSaysWhereToLook() {
        val c = classify("Email not confirmed")
        assertEquals(ErrorKind.AUTH, c.kind)
        assertTrue(c.userMessage.startsWith("Confirm your email address before signing in."))
        assertTrue(c.userMessage.contains("spam"))
    }

    @Test
    fun aBannedAccountSaysItIsDisabled() {
        assertEquals("This account has been disabled. Contact the college administrator.", classify("User banned").userMessage)
        assertEquals("This account has been disabled. Contact the college administrator.", classify("code: user_banned").userMessage)
    }

    @Test
    fun anExistingAccountSuggestsSigningInOrResetting() {
        val c = classify("User already registered")
        assertEquals(ErrorKind.CONFLICT, c.kind)
        assertEquals("An account with this email already exists. Try signing in, or use Forgot password.", c.userMessage)
    }

    @Test
    fun disabledSignupsAreExplained() {
        assertEquals("New sign-ups are turned off right now. Contact the college administrator.", classify("Signups not allowed for this instance").userMessage)
    }

    @Test
    fun emailAndRequestRateLimitsAreTold_apart() {
        val email = classify("Email rate limit exceeded")
        assertEquals(ErrorKind.NETWORK, email.kind)
        assertEquals("Too many emails were requested recently. Wait a while before asking for another.", email.userMessage)

        assertEquals("Too many attempts. Wait a minute and try again.", classify("over_request_rate_limit").userMessage)
    }

    @Test
    fun anExpiredLinkSaysToRequestANewOne() {
        assertEquals(
            "That link or code has expired or was already used. Request a new one.",
            classify("Email link is invalid or has expired").userMessage,
        )
        assertEquals(
            "That link or code has expired or was already used. Request a new one.",
            classify("Token has expired or is invalid").userMessage,
        )
    }

    @Test
    fun reusingTheCurrentPasswordIsExplained() {
        assertEquals(
            "Choose a password that is different from your current one.",
            classify("New password should be different from the old password.").userMessage,
        )
    }

    @Test
    fun weakPasswordReasonsAreListed() {
        val (kind, message) = AuthErrorMessages.messageFor("weak_password", "Password is too weak", listOf("length", "pwned"))!!
        assertEquals(ErrorKind.VALIDATION, kind)
        assertEquals("That password is too weak. It must be longer, not be a password that has leaked in a data breach.", message)
        assertEquals("Choose a stronger password and try again.", AuthErrorMessages.messageFor("weak_password", "")!!.second)
    }

    @Test
    fun sessionProblemsAskToSignInAgain() {
        listOf("bad_jwt", "session_expired", "refresh_token_not_found", "refresh_token_already_used").forEach {
            assertEquals("Your session has expired. Sign in again.", AuthErrorMessages.messageFor(it, "")!!.second)
        }
    }

    @Test
    fun anInvalidOrUnknownEmailIsDistinguished() {
        assertEquals("That email address isn't valid. Check it and try again.", classify("Unable to validate email address: invalid format").userMessage)
        assertEquals("No account exists for that email address.", classify("User not found").userMessage)
    }

    @Test
    fun anUnrelatedFailureIsNotMistakenForAnAuthOutcome() {
        assertNull(AuthErrorMessages.messageFor(null, "some completely unrelated backend failure"))
        val c = classify("some completely unrelated backend failure")
        assertEquals(ErrorKind.UNEXPECTED, c.kind)
        assertTrue(c.userMessage.startsWith("Couldn't sign in. (Ref "))
    }

    // ---- file reads ----

    @Test
    fun aDamagedSpreadsheetIsExplained() {
        assertEquals(
            "This spreadsheet isn't a valid Excel (.xlsx) file. It may be damaged, or saved in another format.",
            FileReadErrors.describe(ZipException("invalid END header"), "spreadsheet"),
        )
    }

    @Test
    fun aMissingOrUnreadableFileSaysWhy() {
        assertTrue(FileReadErrors.describe(FileNotFoundException("/x"), "photo").startsWith("That photo can no longer be found."))
        assertTrue(FileReadErrors.describe(SecurityException("denied"), "file").startsWith("The app isn't allowed to read that file."))
        assertEquals("That file is too large to open.", FileReadErrors.describe(OutOfMemoryError(), "file"))
        assertTrue(FileReadErrors.describe(IOException("The process cannot access the file"), "file").contains("open in another program"))
    }

    @Test
    fun aParserMessageIsPassedThroughButInternalsAreNot() {
        assertEquals(
            "Couldn't find a worksheet in this Excel file.",
            FileReadErrors.describe(IllegalArgumentException("Couldn't find a worksheet in this Excel file.")),
        )
        val leaky = FileReadErrors.describe(IllegalStateException("failed at content://media/external/file/12 {x}"))
        assertFalse(leaky.contains("content://"))
        assertEquals("Couldn't read that file.", leaky)
    }

    @Test
    fun asCmsExceptionKeepsTheOriginalAsTheCause() {
        val original = FileNotFoundException("x")
        val wrapped = FileReadErrors.asCmsException(original, "photo")
        assertTrue(wrapped is CmsException.Validation)
        assertEquals(original, wrapped.cause)
        val typed = CmsException.Conflict("already")
        assertEquals(typed, FileReadErrors.asCmsException(typed))
    }

    @Test
    fun exportFailuresSayWhatWentWrongWithTheFile() {
        assertTrue(FileReadErrors.describeWrite(IOException("The process cannot access the file because it is being used"), "PDF").contains("open in another program"))
        assertEquals(
            "There isn't enough free space to save the Excel (.xlsx) file. Free some space and try again.",
            FileReadErrors.describeWrite(IOException("No space left on device"), "Excel (.xlsx)"),
        )
        assertEquals("The app isn't allowed to save there. Choose a different folder and try again.", FileReadErrors.describeWrite(SecurityException("x"), "PDF"))
        assertEquals("Couldn't create the PDF file.", FileReadErrors.describeWrite(RuntimeException("boom"), "PDF"))
    }
}
