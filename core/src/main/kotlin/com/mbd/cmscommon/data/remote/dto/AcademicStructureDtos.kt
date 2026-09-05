package com.mbd.cmscommon.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AcademicSessionDto(
    val sessionId: String? = null,
    val deptId: String? = null,
    val startYear: Int = 0,
    val endYear: Int = 0,
    val shift: String? = null,
    val programName: String? = null,
    val inchargeEmail: String? = null,
    val maxStudents: Int = 0,
    val currentSemester: Int = 0,
    val isActive: Boolean = true,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class SemesterSubjectDto(
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val name: String? = null,
    val creditHours: Int = 0,
    val subjectType: String? = null,
    val isElective: Boolean = false,
    val outline: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class SemesterTermDto(
    val sessionId: String? = null,
    val semester: Int = 0,
    val startDate: String? = null,
    val endDate: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class SessionStudentDto(
    val sessionId: String? = null,
    val rollNumber: String? = null,
    val name: String? = null,
    val linkedEmail: String? = null,
    val gpa: Double? = null,
    val cgpa: Double? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class StudentProfileDto(
    val sessionId: String? = null,
    val rollNumber: String? = null,
    val name: String? = null,
    val universityRollNo: String? = null,
    val registrationNo: String? = null,
    val fatherName: String? = null,
    val guardianName: String? = null,
    val cnicBform: String? = null,
    val dob: String? = null,
    val gender: String? = null,
    val phone: String? = null,
    val guardianPhone: String? = null,
    val personalEmail: String? = null,
    val currentAddress: String? = null,
    val permanentAddress: String? = null,
    val bloodGroup: String? = null,
    val domicile: String? = null,
    val religion: String? = null,
    val admissionDate: String? = null,
    val enrollmentStatus: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactRelation: String? = null,
    val emergencyContactPhone: String? = null,
    val specialNeeds: String? = null,
    val isCr: Boolean = false,
    val isGr: Boolean = false,
    val linkedEmail: String? = null,
    val gpa: Double? = null,
    val cgpa: Double? = null,
    val photoPath: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class TimetablePeriodDto(
    val id: String? = null,
    @SerialName("primary_session_id") val sessionId: String? = null,
    val day: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val periodType: String? = null,
    val courseCode: String? = null,
    val subjectName: String? = null,
    val creditHours: Int? = null,
    val teacherEmail: String? = null,
    val teacherName: String? = null,
    val roomNo: String? = null,
    val building: String? = null,
    val notes: String? = null,
    val effectiveFrom: String? = null,
    val effectiveTo: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class AttendanceRowDto(
    val entityId: Long? = null,
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val date: String? = null,
    val rollNumber: String? = null,
    val status: String? = null,
    val teacherEmail: String? = null,
    val isLate: Boolean = false,
    val remark: String? = null,
    val lectureTopic: String? = null,
    val recordedAt: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class AttendanceSummaryRowDto(
    val sessionId: String? = null,
    val courseCode: String? = null,
    val rollNumber: String? = null,
    val present: Int = 0,
    val absent: Int = 0,
    val leave: Int = 0,
)

@Serializable
data class MarkRowDto(
    val sessionId: String? = null,
    val semester: Int = 0,
    val courseCode: String? = null,
    val examType: String? = null,
    val rollNumber: String? = null,
    val score: Int? = null,
    val maxMarks: Int = 0,
    val wasAbsent: Boolean = false,
    val examDate: String? = null,
    val remarks: String? = null,
    val teacherEmail: String? = null,
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class SemesterGpaDto(
    val sessionId: String? = null,
    val rollNumber: String? = null,
    val semester: Int = 0,
    val gpa: Double = 0.0,
    val cgpa: Double = 0.0,
    val termLabel: String? = null,
    val resultStatus: String? = null,
    val classPosition: Int? = null,
    val remarks: String? = null,
    val supplyCourses: List<String> = emptyList(),
    val createdAt: String? = null,
    val createdBy: String? = null,
    val updatedAt: String? = null,
    val updatedBy: String? = null,
    val isDeleted: Boolean = false,
    val deletedAt: String? = null,
    val deletedBy: String? = null,
)

@Serializable
data class RecordResultParams(
    val session: String,
    val roll: String,
    val semester: Int,
    val gpa: Double,
    val cgpa: Double,
    val termLabel: String?,
    val result: String,
    val classPosition: Int?,
    val remarks: String?,
)
