package com.mbd.cmscommon.domain.model

import java.time.LocalDate

data class FeeChallanHeader(
    val studentName: String,
    val rollNumber: String,
    val fatherName: String? = null,
    val sessionLabel: String,
    val shift: String,
    val deptCode: String? = null,
    val challanNumber: String,
    val issueDate: String,
)

/** Deterministic, not persisted -- the same (session, roll, cadence) always produces the same
 * number so a re-download looks like the same document instead of minting a new one every time. */
fun feeChallanNumber(sessionId: String, rollNumber: String, cadenceLabel: String): String {
    val key = "$sessionId-$rollNumber-$cadenceLabel".uppercase()
    val checksum = (key.sumOf { it.code } % 9000) + 1000
    return "FC-${rollNumber.uppercase()}-$checksum"
}

/** Admin's placeholder header for previewing the fee structure they're editing, before any real
 * student exists to generate one from -- unmistakably not a real document (prefixed "SAMPLE"). */
fun sampleFeeChallanHeader(session: AcademicSession?, department: Department?): FeeChallanHeader {
    val sessionId = session?.sessionId ?: "sample-session"
    return FeeChallanHeader(
        studentName = "Sample Student",
        rollNumber = "${department?.code ?: "XX"}-00-00",
        fatherName = "Sample Father Name",
        sessionLabel = session?.label ?: "----–----",
        shift = session?.shift?.name ?: "MORNING",
        deptCode = department?.code,
        challanNumber = "SAMPLE-" + feeChallanNumber(sessionId, "00-00", session?.currentSemester?.toString() ?: "1"),
        issueDate = LocalDate.now().toString(),
    )
}
