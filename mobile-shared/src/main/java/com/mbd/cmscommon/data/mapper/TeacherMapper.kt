package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.local.entity.TeacherEntity
import com.mbd.cmscommon.data.remote.PgTime
import com.mbd.cmscommon.data.remote.dto.TeacherDto
import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherPermissions
import com.mbd.cmscommon.domain.model.TeacherStatus
import java.time.Instant

object TeacherMapper {
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
            archivedAt = PgTime.parse(dto.archivedAt),
            entityId = dto.entityId ?: 0L,
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

    fun domainToEntity(domain: Teacher): TeacherEntity = TeacherEntity(
        teacherId = domain.teacherId,
        entityId = domain.entityId,
        name = domain.name,
        email = domain.email,
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
        archivedAt = domain.archivedAt?.toEpochMilli(),
        createdAt = domain.createdAt.toEpochMilli(),
        createdBy = domain.createdBy,
        updatedAt = domain.updatedAt.toEpochMilli(),
        updatedBy = domain.updatedBy,
    )

    fun entityToDomain(entity: TeacherEntity): Teacher {
        val permissions = TeacherPermissions(
            canApproveLinkRequests = entity.canApproveLinkRequests,
            canEditTimetable = entity.canEditTimetable,
            canSendNotifications = entity.canSendNotifications,
            canManageDatesheets = entity.canManageDatesheets,
        )
        val status = runCatching { TeacherStatus.valueOf(entity.status) }.getOrDefault(TeacherStatus.ACTIVE)
        return Teacher(
            teacherId = entity.teacherId,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
            deptId = entity.deptId,
            designation = entity.designation,
            qualification = entity.qualification,
            specialization = entity.specialization,
            officeRoom = entity.officeRoom,
            gender = entity.gender,
            authUid = entity.authUid,
            isAdmin = entity.isAdmin,
            isHod = entity.isHod,
            photoPath = entity.photoPath,
            permissions = permissions,
            status = status,
            isActive = entity.isActive,
            archivedAt = entity.archivedAt?.let { Instant.ofEpochMilli(it) },
            entityId = entity.entityId,
            createdAt = Instant.ofEpochMilli(entity.createdAt),
            createdBy = entity.createdBy,
            updatedAt = Instant.ofEpochMilli(entity.updatedAt),
            updatedBy = entity.updatedBy,
        )
    }

    fun dtoToEntity(dto: TeacherDto): TeacherEntity = domainToEntity(dtoToDomain(dto)).copy(
        isDeleted = dto.isDeleted,
        deletedAt = PgTime.parse(dto.deletedAt)?.toEpochMilli(),
        deletedBy = dto.deletedBy,
    )
}
