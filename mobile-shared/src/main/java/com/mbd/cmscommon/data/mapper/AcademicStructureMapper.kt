package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.SubjectType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

object AcademicStructureMapper {
    fun sessionEntityToDomain(e: AcademicSessionEntity): AcademicSession = AcademicSession(
        sessionId = e.sessionId,
        deptId = e.deptId,
        startYear = e.startYear,
        endYear = e.endYear,
        shift = runCatching { Session.valueOf(e.shift) }.getOrDefault(Session.MORNING),
        currentSemester = e.currentSemester,
        isActive = e.isActive,
        programName = e.programName,
        inchargeEmail = e.inchargeEmail,
        maxStudents = e.maxStudents,
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun sessionDomainToEntity(s: AcademicSession): AcademicSessionEntity = AcademicSessionEntity(
        sessionId = s.sessionId,
        deptId = s.deptId,
        startYear = s.startYear,
        endYear = s.endYear,
        shift = s.shift.name,
        currentSemester = s.currentSemester,
        isActive = s.isActive,
        programName = s.programName,
        inchargeEmail = s.inchargeEmail,
        maxStudents = s.maxStudents,
        createdAt = s.createdAt.toEpochMilli(),
        createdBy = s.createdBy,
        updatedAt = s.updatedAt.toEpochMilli(),
        updatedBy = s.updatedBy,
    )

    fun subjectEntityToDomain(e: SemesterSubjectEntity): SemesterSubject = SemesterSubject(
        sessionId = e.sessionId,
        semester = e.semester,
        courseCode = e.courseCode,
        name = e.name,
        creditHours = e.creditHours,
        subjectType = runCatching { SubjectType.valueOf(e.subjectType) }.getOrDefault(SubjectType.THEORY),
        isElective = e.isElective,
        outline = e.outline,
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun subjectDomainToEntity(s: SemesterSubject): SemesterSubjectEntity = SemesterSubjectEntity(
        id = "${s.sessionId}_${s.semester}_${s.courseCode}",
        sessionId = s.sessionId,
        semester = s.semester,
        courseCode = s.courseCode,
        name = s.name,
        creditHours = s.creditHours,
        subjectType = s.subjectType.name,
        isElective = s.isElective,
        outline = s.outline,
        createdAt = s.createdAt.toEpochMilli(),
        createdBy = s.createdBy,
        updatedAt = s.updatedAt.toEpochMilli(),
        updatedBy = s.updatedBy,
    )

    fun studentEntityToDomain(e: SessionStudentEntity): SessionStudent = SessionStudent(
        id = e.id,
        sessionId = e.sessionId,
        deptId = e.deptId,
        rollNumber = e.rollNumber,
        name = e.name,
        linkedEmail = e.linkedEmail ?: "",
        gpa = e.gpa,
        cgpa = e.cgpa,
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun periodEntityToDomain(e: SessionPeriodEntity): SessionPeriod = SessionPeriod(
        id = e.id,
        sessionId = e.sessionId,
        day = runCatching { DayOfWeek.valueOf(e.day) }.getOrDefault(DayOfWeek.MONDAY),
        startTime = e.startTime ?: "",
        endTime = e.endTime ?: "",
        courseCode = e.courseCode ?: "",
        subjectName = e.subjectName ?: "",
        teacherId = e.teacherId ?: "",
        teacherName = e.teacherName ?: "",
        periodType = runCatching { PeriodType.valueOf(e.periodType) }.getOrDefault(PeriodType.LECTURE),
        creditHours = e.creditHours,
        roomNo = e.roomNo,
        building = e.building,
        notes = e.notes,
        effectiveFrom = e.effectiveFrom?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        effectiveTo = e.effectiveTo?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )
}
