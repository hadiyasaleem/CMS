package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.Fine
import com.mbd.cmscommon.domain.model.StudentProfile
import com.mbd.cmscommon.util.FieldValidators
import java.time.LocalDate
import java.util.Locale
import kotlin.math.roundToInt

data class StudentProfileSnapshot(
    val completionPercent: Int,
    val validGpa: Double?,
    val validCgpa: Double?,
    val validFines: List<Fine>,
    val fineTotal: Double,
)

fun studentProfileSnapshot(profile: StudentProfile, fines: List<Fine>): StudentProfileSnapshot {
    val completed = listOf(
        profile.name.isNotBlank(),
        hasValue(profile.fatherName),
        validCnic(profile.cnicBform),
        validDate(profile.dob),
        profile.gender?.uppercase(Locale.ROOT) in setOf("MALE", "FEMALE", "OTHER"),
        validPhone(profile.phone),
        validPhone(profile.guardianPhone),
        validEmail(profile.personalEmail),
        hasValue(profile.currentAddress),
        hasValue(profile.permanentAddress),
        hasValue(profile.universityRollNo),
        hasValue(profile.registrationNo),
        validDate(profile.admissionDate),
        hasValue(profile.emergencyContactName),
        hasValue(profile.emergencyContactRelation),
        validPhone(profile.emergencyContactPhone),
        hasValue(profile.bloodGroup),
        hasValue(profile.domicile),
    )
    val validFines = fines.filter { it.amount > 0.0 && it.reason.isNotBlank() }
    val completionPercent = ((completed.count { it } * 100f) / completed.size).roundToInt()

    return StudentProfileSnapshot(
        completionPercent = completionPercent,
        validGpa = validAcademicGrade(profile.gpa),
        validCgpa = validAcademicGrade(profile.cgpa),
        validFines = validFines,
        fineTotal = validFines.sumOf { it.amount },
    )
}

fun validateStudentProfile(profile: StudentProfile): String? {
    FieldValidators.nameError(profile.name, "Full name")?.let { return it }
    FieldValidators.nameError(profile.fatherName ?: "", "Father's name", required = false)?.let { return it }
    FieldValidators.nameError(profile.guardianName ?: "", "Guardian's name", required = false)?.let { return it }

    if (hasValue(profile.gender) && profile.gender?.uppercase(Locale.ROOT) !in setOf("MALE", "FEMALE", "OTHER")) {
        return "Choose a valid gender."
    }
    if (hasValue(profile.bloodGroup) &&
        profile.bloodGroup?.uppercase(Locale.ROOT) !in setOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    ) {
        return "Choose a valid blood group."
    }
    if (profile.enrollmentStatus.uppercase(Locale.ROOT) !in setOf("ACTIVE", "PROMOTED", "REPEATED", "WITHDRAWN", "GRADUATED")) {
        return "Choose a valid enrollment status."
    }

    FieldValidators.cnicError(profile.cnicBform ?: "")?.let { return it }
    FieldValidators.isoDateError(profile.dob ?: "", false, "date of birth", LocalDate.of(1900, 1, 1), LocalDate.now())
        ?.let { return "Choose a valid date of birth." }
    FieldValidators.isoDateError(profile.admissionDate ?: "", false, "admission date", LocalDate.of(1950, 1, 1), LocalDate.now())
        ?.let { return "Choose a valid admission date." }
    FieldValidators.phoneError(profile.phone ?: "")?.let { return it }
    FieldValidators.phoneError(profile.guardianPhone ?: "", label = "Guardian phone")?.let { return it }
    FieldValidators.phoneError(profile.emergencyContactPhone ?: "", label = "Emergency phone")?.let { return it }
    FieldValidators.emailError(profile.personalEmail ?: "", required = false)?.let { return it }
    if (FieldValidators.textError(profile.domicile ?: "", "Domicile", required = false, maxLength = 80) != null) {
        return "Domicile must not exceed 80 characters."
    }
    if (FieldValidators.textError(profile.religion ?: "", "Religion", required = false, maxLength = 50) != null) {
        return "Religion must not exceed 50 characters."
    }
    if (FieldValidators.textError(profile.universityRollNo ?: "", "University roll number", required = false, maxLength = 40) != null) {
        return "University roll number must not exceed 40 characters."
    }
    if (FieldValidators.textError(profile.registrationNo ?: "", "Registration number", required = false, maxLength = 40) != null) {
        return "Registration number must not exceed 40 characters."
    }
    if (FieldValidators.textError(profile.currentAddress ?: "", "Current address", required = false, maxLength = 300) != null) {
        return "Current address must not exceed 300 characters."
    }
    if (FieldValidators.textError(profile.permanentAddress ?: "", "Permanent address", required = false, maxLength = 300) != null) {
        return "Permanent address must not exceed 300 characters."
    }
    FieldValidators.nameError(profile.emergencyContactName ?: "", "Emergency contact name", required = false)?.let { return it }
    if (FieldValidators.textError(profile.emergencyContactRelation ?: "", "Emergency contact relation", required = false, maxLength = 50) != null) {
        return "Emergency contact relation must not exceed 50 characters."
    }
    if (FieldValidators.textError(profile.specialNeeds ?: "", "Special needs", required = false, maxLength = 500) != null) {
        return "Special needs must not exceed 500 characters."
    }
    return null
}

private fun hasValue(value: String?): Boolean = !value.isNullOrBlank()

private fun validCnic(value: String?): Boolean = value != null && value.count { it.isDigit() } == 13

private fun validDate(value: String?): Boolean {
    val trimmed = value?.trim() ?: return false
    return runCatching { LocalDate.parse(trimmed) }.isSuccess
}

private fun validPhone(value: String?): Boolean = value != null && value.count { it.isDigit() } >= 10

private fun validEmail(value: String?): Boolean {
    val clean = value?.trim().orEmpty()
    return clean.contains('@') && clean.substringAfter('@').contains('.')
}
