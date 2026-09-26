package com.mbd.cmscommon.teacher

data class ResolvedAssignment(
    val sessionId: String,
    val sessionLabel: String,
    val courseCode: String,
    val subjectLabel: String,
    /** Structured parts of [sessionLabel], used by the class filters. Blank when unknown. */
    val deptName: String = "",
    val sessionName: String = "",
    val shift: String = "",
)

/** Optional class filters: a null field means "any", and only the chosen fields narrow the list. */
data class AssignmentFilter(
    val dept: String? = null,
    val session: String? = null,
    val shift: String? = null,
) {
    val isEmpty: Boolean get() = dept == null && session == null && shift == null
    fun matches(a: ResolvedAssignment): Boolean =
        (dept == null || a.deptName == dept) && (session == null || a.sessionName == session) && (shift == null || a.shift == shift)
}

fun List<ResolvedAssignment>.filtered(filter: AssignmentFilter): List<ResolvedAssignment> =
    if (filter.isEmpty) this else filter { filter.matches(it) }

fun List<ResolvedAssignment>.distinctDepts(): List<String> = map { it.deptName }.filter { it.isNotBlank() }.distinct().sorted()
fun List<ResolvedAssignment>.distinctSessions(): List<String> = map { it.sessionName }.filter { it.isNotBlank() }.distinct().sorted()
fun List<ResolvedAssignment>.distinctShifts(): List<String> = map { it.shift }.filter { it.isNotBlank() }.distinct().sorted()
