package com.mbd.cmscommon.util

import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

object FieldValidators {
    private val emailPattern = Regex("^[A-Za-z0-9.!#\$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+\$")
    private val departmentCodePattern = Regex("^[A-Z][A-Z0-9]{1,9}\$")
    private val rollNumberPattern = Regex("^([A-Z][A-Z0-9]{1,9})-(\\d{2})-(\\d{2,3})\$")
    private val courseCodePattern = Regex("^[A-Z][A-Z0-9]{1,9}(?:-[A-Z0-9]{1,8})?\$")
    private val academicYearPattern = Regex("^(\\d{4})-(\\d{4})\$")

    fun normalizeEmail(value: String): String = value.trim().lowercase(Locale.ROOT)

    fun emailError(value: String, required: Boolean = true): String? {
        val normalized = normalizeEmail(value)
        if (normalized.isEmpty()) return if (required) "Email address is required." else null
        return if (emailPattern.matches(normalized)) null else "Enter a valid email address."
    }

    fun passwordRules(value: String): List<PasswordRule> = listOf(
        PasswordRule("8+ characters", value.length >= 8),
        PasswordRule("Uppercase", value.any { it.isUpperCase() }),
        PasswordRule("Lowercase", value.any { it.isLowerCase() }),
        PasswordRule("Number", value.any { it.isDigit() }),
        PasswordRule("Symbol", value.any { !it.isLetterOrDigit() && !it.isWhitespace() }),
        PasswordRule("No spaces", value.none { it.isWhitespace() }),
    )

    fun passwordError(value: String): String? {
        if (value.isEmpty()) return "Password is required."
        return if (passwordRules(value).any { !it.passed }) {
            "Use 8 or more characters with uppercase, lowercase, number, and symbol."
        } else {
            null
        }
    }

    fun passwordConfirmationError(password: String, confirmation: String): String? {
        if (confirmation.isEmpty()) return "Confirm the password."
        return if (confirmation == password) null else "Passwords do not match."
    }

    fun nameError(value: String, label: String = "Name", required: Boolean = true, maxLength: Int = 100): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "$label is required." else null
        if (clean.length < 2) return "$label must contain at least 2 characters."
        if (clean.length > maxLength) return "$label must not exceed $maxLength characters."
        if (clean.any { it.isDigit() }) return "$label cannot contain numbers."
        return null
    }

    fun departmentCodeError(value: String): String? {
        val clean = value.trim().uppercase(Locale.ROOT)
        if (clean.isEmpty()) return "Department code is required."
        return if (departmentCodePattern.matches(clean)) null else "Use 2-10 uppercase letters or numbers, beginning with a letter."
    }

    fun courseCodeError(value: String): String? {
        val clean = value.trim().uppercase(Locale.ROOT)
        if (clean.isEmpty()) return "Course code is required."
        return if (courseCodePattern.matches(clean)) null else "Use a code such as IT-301."
    }

    fun normalizeRollNumber(value: String): String = value.trim().uppercase(Locale.ROOT)

    fun rollNumberError(value: String, departmentCode: String? = null, startYear: Int? = null): String? {
        val clean = normalizeRollNumber(value)
        val match = rollNumberPattern.matchEntire(clean) ?: return "Use department-year-roll format, for example IT-21-09."

        val expectedDept = departmentCode?.trim()?.uppercase(Locale.ROOT)?.takeIf { it.isNotBlank() }
        if (expectedDept != null && match.groupValues[1] != expectedDept) {
            return "Roll number must begin with $expectedDept for this department."
        }

        val expectedYear = startYear?.let { (it % 100).toString().padStart(2, '0') }
        if (expectedYear != null && match.groupValues[2] != expectedYear) {
            return "Roll number year must be $expectedYear for this intake."
        }

        if (match.groupValues[3].toIntOrNull() == 0) {
            return "Class roll must be greater than zero."
        }
        return null
    }

    fun cnicError(value: String, required: Boolean = false): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "CNIC / B-Form is required." else null
        val digits = clean.filter { it.isDigit() }
        val validChars = clean.all { it.isDigit() || it == '-' || it == ' ' }
        return if (digits.length == 13 && validChars) null else "CNIC / B-Form must contain exactly 13 digits."
    }

    fun phoneError(value: String, required: Boolean = false, label: String = "Phone number"): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "$label is required." else null
        val digitCount = clean.count { it.isDigit() }
        val validChars = clean.all { it.isDigit() || it in "+-() " }
        if (digitCount in 10..15 && validChars) return null
        return "$label must contain 10-15 digits."
    }

    fun isoDateError(value: String, required: Boolean, label: String, earliest: LocalDate? = null, latest: LocalDate? = null): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "$label is required." else null
        val parsed = runCatching { LocalDate.parse(clean) }.getOrNull() ?: return "Choose a valid $label."
        if (earliest != null && parsed.isBefore(earliest)) return "$label cannot be before $earliest."
        if (latest != null && parsed.isAfter(latest)) return "$label cannot be after $latest."
        return null
    }

    fun timeError(value: String, required: Boolean = true, label: String = "Time"): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "$label is required." else null
        return if (runCatching { LocalTime.parse(clean) }.isSuccess) null else "Use 24-hour time in HH:mm format."
    }

    fun academicYearError(value: String, required: Boolean = false): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "Academic year is required." else null
        val match = academicYearPattern.matchEntire(clean) ?: return "Use academic year format YYYY-YYYY."
        val start = match.groupValues[1].toInt()
        val end = match.groupValues[2].toInt()
        return if (end == start + 1) null else "Academic year must cover consecutive years."
    }

    fun positiveDecimalError(value: String, label: String = "Amount", maximum: Double? = null): String? {
        val parsed = value.trim().toDoubleOrNull() ?: return "$label must be a valid number."
        if (parsed <= 0.0) return "$label must be greater than zero."
        if (maximum != null && parsed > maximum) return "$label must not exceed $maximum."
        return null
    }

    fun textError(value: String, label: String, required: Boolean = true, minLength: Int = 1, maxLength: Int): String? {
        val clean = value.trim()
        if (clean.isEmpty()) return if (required) "$label is required." else null
        if (clean.length < minLength) return "$label must contain at least $minLength characters."
        if (clean.length > maxLength) return "$label must not exceed $maxLength characters."
        return null
    }
}

/**
 * Throws a typed [CmsException.Validation] if this (a `*Error(...)` validator result) is non-null.
 * Replaces the previous pattern of `validator(x)?.let { throw IllegalArgumentException(it) }`,
 * which controllers relied on [ErrorClassifier]'s string-sniffing fallback to recognise as
 * validation. `field` is optional context carried for logging only — it is never shown to the user.
 */
fun String?.orThrowValidation(field: String? = null) {
    if (this != null) throw CmsException.Validation(this, field)
}

/** Throws a typed [CmsException.Validation] with [message] when [condition] is false. */
fun requireValid(condition: Boolean, field: String? = null, message: () -> String) {
    if (!condition) throw CmsException.Validation(message(), field)
}
