package com.mbd.cmsdesktop.ui.parity

enum class ParityRole { ADMIN, TEACHER, STUDENT }

enum class ParityState { DEFAULT, LOADING, EMPTY, ERROR, OFFLINE, VALIDATION, DIALOG, PERMISSION }

data class ParityScenario(
    val role: ParityRole,
    val route: String,
    val states: Set<ParityState> = BASIC_STATES,
)

private val BASIC_STATES = setOf(ParityState.DEFAULT, ParityState.LOADING, ParityState.EMPTY, ParityState.ERROR)
private val FORM_STATES = BASIC_STATES + ParityState.VALIDATION + ParityState.DIALOG
private val PERMISSION_STATES = FORM_STATES + ParityState.PERMISSION

/**
 * Static per-screen QA checklist: which UI states (loading/empty/error/validation/permission/...)
 * each route is expected to support, grouped by role. Not consumed by app logic at runtime — a
 * reference list for manual/scripted parity testing against the mobile apps.
 */
object ParityScenarioRegistry {
    val scenarios: List<ParityScenario> = buildList {
        role(ParityRole.ADMIN, FORM_STATES, "auth")
        role(
            ParityRole.ADMIN, BASIC_STATES,
            "dashboard", "academics", "people", "records", "more", "profile", "master_timetable",
            "fees_picker", "attendance_records", "insights",
        )
        role(
            ParityRole.ADMIN, FORM_STATES,
            "teachers", "notifications", "calendar", "datesheets", "documents", "dept/{deptId}",
            "session/{sessionId}", "session/{sessionId}/fees", "session/{sessionId}/semester/{semester}",
            "session/{sessionId}/students", "session/{sessionId}/student/{roll}", "session/{sessionId}/timetable",
        )
        role(ParityRole.ADMIN, PERMISSION_STATES, "link_requests", "mark_edit_requests")
        role(
            ParityRole.TEACHER, FORM_STATES,
            "auth", "attendance", "attendance_history/{sessionId}/{courseCode}", "marks",
            "semester_results", "exam_paper",
        )
        role(
            ParityRole.TEACHER, BASIC_STATES,
            "home", "exams_hub", "menu_hub", "my_students", "events", "datesheets", "documents",
            "insights", "profile",
        )
        role(ParityRole.TEACHER, PERMISSION_STATES, "schedule", "notifications", "link_requests")
        role(ParityRole.STUDENT, FORM_STATES, "auth", "link_request")
        role(
            ParityRole.STUDENT, BASIC_STATES,
            "home", "attendance", "exams_hub", "marks", "results", "timetable", "more", "events",
            "datesheets", "documents", "fees", "notifications", "profile",
        )
    }

    private fun MutableList<ParityScenario>.role(role: ParityRole, states: Set<ParityState>, vararg routes: String) {
        routes.forEach { route -> add(ParityScenario(role, route, states)) }
    }
}
