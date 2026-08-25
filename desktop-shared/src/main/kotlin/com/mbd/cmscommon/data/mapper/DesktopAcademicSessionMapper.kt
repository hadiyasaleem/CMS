package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.AcademicSessionDto
import com.mbd.cmscommon.data.remote.dto.SessionStudentDto
import com.mbd.cmscommon.data.remote.dto.StudentProfileDto
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.StudentProfile

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step). Covers everything [com.mbd.cmscommon.domain.repository
 * .AcademicSessionRepository] moves across the wire: sessions, session-scoped students, and
 * the (network-only, never cached) student profile.
 */
object DesktopAcademicSessionMapper {
    fun sessionDtoToDomain(dto: AcademicSessionDto, fallbackDeptId: String = ""): AcademicSession {
        val shift = runCatching { Session.valueOf(dto.shift ?: "") }.getOrDefault(Session.MORNING)
        return AcademicSession(
            sessionId = dto.sessionId ?: "",
            deptId = dto.deptId ?: fallbackDeptId,
            startYear = dto.startYear,
            endYear = dto.endYear,
            shift = shift,
            currentSemester = dto.currentSemester.coerceIn(1, 8),
            isActive = dto.isActive,
            programName = dto.programName,
            inchargeEmail = dto.inchargeEmail,
            maxStudents = dto.maxStudents,
            archivedAt = PgTime.parse(dto.archivedAt),
            entityId = dto.entityId ?: 0L,
            createdAt = PgTime.parseOrEpoch(dto.createdAt),
            createdBy = dto.createdBy,
            updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
            updatedBy = dto.updatedBy,
        )
    }

    fun sessionDomainToDto(domain: AcademicSession): AcademicSessionDto = AcademicSessionDto(
        sessionId = domain.sessionId,
        deptId = domain.deptId,
        startYear = domain.startYear,
        endYear = domain.endYear,
        shift = domain.shift.name,
        currentSemester = domain.currentSemester,
        isActive = domain.isActive,
        programName = domain.programName,
        inchargeEmail = domain.inchargeEmail,
        maxStudents = domain.maxStudents,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )

    fun studentDtoToDomain(dto: SessionStudentDto, fallbackSessionId: String = "", deptId: String = ""): SessionStudent {
        val sessionId = dto.sessionId?.takeIf { it.isNotBlank() } ?: fallbackSessionId
        return SessionStudent(
            id = SessionStudent.buildId(sessionId, dto.rollNumber ?: ""),
            sessionId = sessionId,
            deptId = deptId,
            rollNumber = dto.rollNumber ?: "",
            name = dto.name ?: "",
            linkedEmail = dto.linkedEmail ?: "",
            gpa = dto.gpa,
            cgpa = dto.cgpa,
            entityId = dto.entityId ?: 0L,
            createdAt = PgTime.parseOrEpoch(dto.createdAt),
            createdBy = dto.createdBy,
            updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
            updatedBy = dto.updatedBy,
        )
    }

    fun studentDomainToDto(domain: SessionStudent): SessionStudentDto = SessionStudentDto(
        sessionId = domain.sessionId,
        rollNumber = domain.rollNumber,
        name = domain.name,
        linkedEmail = domain.linkedEmail.takeIf { it.isNotBlank() },
        gpa = domain.gpa,
        cgpa = domain.cgpa,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )

    fun profileDtoToDomain(dto: StudentProfileDto, sessionId: String, rollNumber: String): StudentProfile = StudentProfile(
        sessionId = sessionId,
        rollNumber = rollNumber,
        name = dto.name ?: "",
        universityRollNo = dto.universityRollNo,
        registrationNo = dto.registrationNo,
        fatherName = dto.fatherName,
        guardianName = dto.guardianName,
        cnicBform = dto.cnicBform,
        dob = dto.dob,
        gender = dto.gender,
        phone = dto.phone,
        guardianPhone = dto.guardianPhone,
        personalEmail = dto.personalEmail,
        currentAddress = dto.currentAddress,
        permanentAddress = dto.permanentAddress,
        bloodGroup = dto.bloodGroup,
        domicile = dto.domicile,
        religion = dto.religion,
        admissionDate = dto.admissionDate,
        enrollmentStatus = dto.enrollmentStatus ?: "ACTIVE",
        emergencyContactName = dto.emergencyContactName,
        emergencyContactRelation = dto.emergencyContactRelation,
        emergencyContactPhone = dto.emergencyContactPhone,
        specialNeeds = dto.specialNeeds,
        isCr = dto.isCr,
        isGr = dto.isGr,
        linkedEmail = dto.linkedEmail ?: "",
        gpa = dto.gpa,
        cgpa = dto.cgpa,
        entityId = dto.entityId ?: 0L,
        createdAt = PgTime.parseOrEpoch(dto.createdAt),
        createdBy = dto.createdBy,
        updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
        updatedBy = dto.updatedBy,
    )

    fun profileDomainToDto(domain: StudentProfile): StudentProfileDto = StudentProfileDto(
        sessionId = domain.sessionId,
        rollNumber = domain.rollNumber,
        name = domain.name,
        universityRollNo = domain.universityRollNo,
        registrationNo = domain.registrationNo,
        fatherName = domain.fatherName,
        guardianName = domain.guardianName,
        cnicBform = domain.cnicBform,
        dob = domain.dob,
        gender = domain.gender,
        phone = domain.phone,
        guardianPhone = domain.guardianPhone,
        personalEmail = domain.personalEmail,
        currentAddress = domain.currentAddress,
        permanentAddress = domain.permanentAddress,
        bloodGroup = domain.bloodGroup,
        domicile = domain.domicile,
        religion = domain.religion,
        admissionDate = domain.admissionDate,
        enrollmentStatus = domain.enrollmentStatus,
        emergencyContactName = domain.emergencyContactName,
        emergencyContactRelation = domain.emergencyContactRelation,
        emergencyContactPhone = domain.emergencyContactPhone,
        specialNeeds = domain.specialNeeds,
        isCr = domain.isCr,
        isGr = domain.isGr,
        linkedEmail = domain.linkedEmail,
        gpa = domain.gpa,
        cgpa = domain.cgpa,
    )
}
