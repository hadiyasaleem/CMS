package com.mbd.cmscommon.domain.model

import java.time.Instant

data class Datesheet(
    val id: String,
    val sessionId: String,
    val semester: Int,
    val defaultStartTime: String? = null,
    val defaultEndTime: String? = null,
    val defaultBuildingId: String? = null,
    val published: Boolean = false,
    val instructions: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

data class DatesheetSlot(
    val id: String,
    val datesheetId: String,
    val courseCode: String,
    val subjectName: String,
    val examDate: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val buildingId: String? = null,
    val building: String? = null,
    val roomId: String? = null,
    val roomNo: String? = null,
    val invigilatorEmail: String? = null,
    override val createdAt: Instant = Instant.EPOCH,
    override val createdBy: String? = null,
    override val updatedAt: Instant = Instant.EPOCH,
    override val updatedBy: String? = null,
) : BaseEntity()

/** A paper's effective start time: its own override, or its datesheet's default. */
fun DatesheetSlot.resolvedStartTime(sheet: Datesheet): String? = startTime ?: sheet.defaultStartTime

/** A paper's effective end time: its own override, or its datesheet's default. */
fun DatesheetSlot.resolvedEndTime(sheet: Datesheet): String? = endTime ?: sheet.defaultEndTime

/** A paper's effective building: its own override, or its datesheet's default. */
fun DatesheetSlot.resolvedBuildingId(sheet: Datesheet): String? = buildingId ?: sheet.defaultBuildingId

/** A paper counts as scheduled once it has a date, an effective time, and an effective building. */
fun DatesheetSlot.isScheduled(sheet: Datesheet): Boolean =
    !examDate.isNullOrBlank() && resolvedStartTime(sheet) != null && resolvedEndTime(sheet) != null && resolvedBuildingId(sheet) != null

/** "Mid Term · Information Technology 2023–2027 Morning · Semester 3" */
fun datesheetLabel(sheet: Datesheet, session: AcademicSession?, department: Department?): String {
    val sessionLabel = session?.let { "${it.label} ${it.shift.name.lowercase().replaceFirstChar(Char::uppercase)}" } ?: sheet.sessionId
    val deptLabel = department?.name?.let { "$it " } ?: ""
    return "Mid Term · $deptLabel$sessionLabel · Semester ${sheet.semester}"
}
