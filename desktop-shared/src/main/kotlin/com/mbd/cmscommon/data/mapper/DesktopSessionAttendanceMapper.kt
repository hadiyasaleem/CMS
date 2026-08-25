package com.mbd.cmscommon.data.mapper

import com.mbd.cmscommon.data.remote.dto.AttendanceRowDto
import com.mbd.cmscommon.domain.model.AttendanceEntry
import com.mbd.cmscommon.domain.model.AttendanceStatus
import com.mbd.cmscommon.domain.model.DailyAttendanceMark
import java.time.LocalDate

/**
 * Direct DTO<->Domain mapping for the desktop apps (no local Room cache, so there is no
 * Entity intermediate here — just the same field logic mobile's dtoToEntity+entityToDomain
 * pair does, composed into one step). [AttendanceRowDto] is the raw per-student/per-day row;
 * it maps 1:1 to [DailyAttendanceMark], while [AttendanceTally] totals are aggregated by the
 * repository from cached rows (there's no single-row equivalent to aggregate from).
 */
object DesktopSessionAttendanceMapper {
    fun dtoToDomain(dto: AttendanceRowDto): DailyAttendanceMark = DailyAttendanceMark(
        rollNumber = dto.rollNumber ?: "",
        date = runCatching { LocalDate.parse(dto.date) }.getOrDefault(LocalDate.EPOCH),
        status = runCatching { AttendanceStatus.valueOf(dto.status ?: "") }.getOrDefault(AttendanceStatus.PRESENT),
        isLate = dto.isLate,
        remark = dto.remark,
        lectureTopic = dto.lectureTopic,
    )

    fun entryToDto(
        sessionId: String,
        semester: Int,
        courseCode: String,
        date: LocalDate,
        rollNumber: String,
        entry: AttendanceEntry,
        teacherEmail: String,
        lectureTopic: String?,
    ): AttendanceRowDto = AttendanceRowDto(
        sessionId = sessionId,
        semester = semester,
        courseCode = courseCode,
        date = date.toString(),
        rollNumber = rollNumber,
        status = entry.status.name,
        teacherEmail = teacherEmail,
        isLate = entry.isLate,
        remark = entry.remark?.trim()?.takeIf { it.isNotBlank() },
        lectureTopic = lectureTopic?.trim()?.takeIf { it.isNotBlank() },
    )
}
