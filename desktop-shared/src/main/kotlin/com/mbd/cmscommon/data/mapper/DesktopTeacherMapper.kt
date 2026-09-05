package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.TeacherStatus

/**
 * Direct DTO<->Domain mapping for the desktop apps — copied near-verbatim from mobile's
 * TeacherMapper.dtoToDomain/domainToDto (which are already direct, Entity-free there too).
 */
object DesktopTeacherMapper {
    private fun String?.emptyToNull(): String? = if (this == null || isBlank()) null else this

    fun dtoToDomain(dto: TeacherDto): Teacher {
        val permissions = TeacherPermissions(
            canApproveLinkRequests = dto.canApproveLinkRequests,
            canEditTimetable = dto.canEditTimetable,
            canSendNotifications = dto.canSendNotifications,
            canManageDatesheets = dto.canManageDatesheets,
        )
        val status = runCatching { TeacherStatus.valueOf(dto.status ?: "") }.getOrDefault(TeacherStatus.ACTIVE)
        return Teacher(
            teacherId = dto.email ?: "",
            name = dto.name ?: "",
            email = dto.email ?: "",
            phone = dto.phone.emptyToNull(),
            deptId = dto.deptId.emptyToNull(),
            designation = dto.designation.emptyToNull(),
            qualification = dto.qualification.emptyToNull(),
            specialization = dto.specialization.emptyToNull(),
            officeRoom = dto.officeRoom.emptyToNull(),
            gender = dto.gender.emptyToNull(),
            authUid = dto.authUid.emptyToNull(),
            isAdmin = dto.isAdmin,
            isHod = dto.isHod,
            photoPath = dto.photoPath.emptyToNull(),
            permissions = permissions,
            status = status,
            isActive = dto.isActive,
            createdAt = PgTime.parseOrEpoch(dto.createdAt),
            createdBy = dto.createdBy ?: "",
            updatedAt = PgTime.parseOrEpoch(dto.updatedAt),
            updatedBy = dto.updatedBy ?: "",
        )
    }

    fun domainToDto(domain: Teacher): TeacherDto = TeacherDto(
        email = domain.email.ifBlank { domain.teacherId },
        name = domain.name,
        phone = domain.phone,
        deptId = domain.deptId,
        designation = domain.designation,
        qualification = domain.qualification,
        specialization = domain.specialization,
        officeRoom = domain.officeRoom,
        gender = domain.gender,
        authUid = domain.authUid,
        isAdmin = domain.isAdmin,
        isHod = domain.isHod,
        photoPath = domain.photoPath,
        canApproveLinkRequests = domain.permissions.canApproveLinkRequests,
        canEditTimetable = domain.permissions.canEditTimetable,
        canSendNotifications = domain.permissions.canSendNotifications,
        canManageDatesheets = domain.permissions.canManageDatesheets,
        status = domain.status.name,
        isActive = domain.isActive,
        createdBy = domain.createdBy,
        updatedBy = domain.updatedBy,
    )
}
