package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.PoolSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterTermEntity
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.data.remote.dto.StudentProfileDto
import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.CourseCategory
import com.mbd.cmscommon.domain.model.PeriodType
import com.mbd.cmscommon.domain.model.PoolSubject
import com.mbd.cmscommon.domain.model.SemesterSubject
import com.mbd.cmscommon.domain.model.SemesterTerm
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.model.SessionStudent
import com.mbd.cmscommon.domain.model.ShiftMode
import com.mbd.cmscommon.domain.model.ProgramType
import com.mbd.cmscommon.domain.model.parseShift
import com.mbd.cmscommon.domain.model.parseShiftMode
import com.mbd.cmscommon.domain.model.parseProgramType
import com.mbd.cmscommon.domain.model.SubjectType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object AcademicStructureMapper {
    private val profileJson = Json { ignoreUnknownKeys = true }

    fun sessionEntityToDomain(e: AcademicSessionEntity): AcademicSession = AcademicSession(
        sessionId = e.sessionId,
        deptId = e.deptId,
        startYear = e.startYear,
        endYear = e.endYear,
        shiftMode = parseShiftMode(e.shiftMode) ?: ShiftMode.MORNING,
        currentSemester = e.currentSemester,
        isActive = e.isActive,
        programType = parseProgramType(e.programType) ?: ProgramType.BS,
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
        shiftMode = s.shiftMode.name,
        currentSemester = s.currentSemester,
        isActive = s.isActive,
        programType = s.programType.name,
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
        courseType = runCatching { CourseCategory.valueOf(e.courseType) }.getOrDefault(CourseCategory.MAJOR),
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
        courseType = s.courseType.name,
        isElective = s.isElective,
        outline = s.outline,
        createdAt = s.createdAt.toEpochMilli(),
        createdBy = s.createdBy,
        updatedAt = s.updatedAt.toEpochMilli(),
        updatedBy = s.updatedBy,
    )

    fun poolEntityToDomain(e: PoolSubjectEntity): PoolSubject = PoolSubject(
        courseCode = e.courseCode,
        name = e.name,
        creditHours = e.creditHours,
        subjectType = runCatching { SubjectType.valueOf(e.subjectType) }.getOrDefault(SubjectType.THEORY),
        courseType = runCatching { CourseCategory.valueOf(e.courseType) }.getOrDefault(CourseCategory.MAJOR),
        outline = e.outline,
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun poolDomainToEntity(p: PoolSubject): PoolSubjectEntity = PoolSubjectEntity(
        courseCode = p.courseCode,
        name = p.name,
        creditHours = p.creditHours,
        subjectType = p.subjectType.name,
        courseType = p.courseType.name,
        outline = p.outline,
        createdAt = p.createdAt.toEpochMilli(),
        createdBy = p.createdBy,
        updatedAt = p.updatedAt.toEpochMilli(),
        updatedBy = p.updatedBy,
    )

    fun termEntityToDomain(e: SemesterTermEntity): SemesterTerm = SemesterTerm(
        sessionId = e.sessionId,
        semester = e.semester,
        startDate = e.startDate?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        endDate = e.endDate?.let { raw -> runCatching { LocalDate.parse(raw) }.getOrNull() },
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun studentEntityToDomain(e: SessionStudentEntity): SessionStudent = SessionStudent(
        id = e.id,
        sessionId = e.sessionId,
        deptId = e.deptId,
        rollNumber = e.rollNumber,
        name = e.name,
        shift = parseShift(e.shift) ?: Session.MORNING,
        linkedEmail = e.linkedEmail ?: "",
        gpa = e.gpa,
        cgpa = e.cgpa,
        photoPath = e.profileJson?.let { encoded -> runCatching { profileJson.decodeFromString<StudentProfileDto>(encoded).photoPath }.getOrNull() },
        createdAt = Instant.ofEpochMilli(e.createdAt),
        createdBy = e.createdBy,
        updatedAt = Instant.ofEpochMilli(e.updatedAt),
        updatedBy = e.updatedBy,
    )

    fun periodEntityToDomain(e: SessionPeriodEntity): SessionPeriod = SessionPeriod(
        // The true remote period id -- for a shadow row (a merged lecture as seen from a linked session)
        // this differs from the local Room row's own [SessionPeriodEntity.id], which is a synthetic key.
        id = e.remotePeriodId,
        sessionId = e.sessionId,
        shift = parseShift(e.shift) ?: Session.MORNING,
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
        linkedSessionIds = e.linkedSessionIds.split(',').filter { it.isNotBlank() }.toSet(),
        isOwnRow = e.id == e.remotePeriodId,
    )
}
