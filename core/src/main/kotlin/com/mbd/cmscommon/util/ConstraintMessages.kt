package com.mbd.cmscommon.util

/**
 * Plain-words messages for database constraint violations. Everything here turns a Postgres
 * constraint/table/column name into wording an admin, teacher or student understands; internals
 * (table names, column names, SQL) never reach the user. Unknown names degrade to a generic
 * sentence rather than leaking the identifier.
 *
 * The named constraints below mirror `supabase/migrations`. Postgres' default names
 * (`<table>_pkey`, `<table>_<cols>_key`, `<table>_<col>_check`, `<table>_<col>_fkey`) are handled
 * by the table/column label fallbacks.
 */
internal object ConstraintMessages {

    /** What one row of each table is called in conversation. */
    private val TABLE_LABELS = mapOf(
        "academic_sessions" to "session",
        "app_logs" to "log entry",
        "attendance_edit_requests" to "attendance edit request",
        "buildings" to "building",
        "calendar_events" to "calendar event",
        "datesheet_slots" to "datesheet paper",
        "datesheets" to "datesheet",
        "departments" to "department",
        "documents" to "document",
        "exam_paper_submissions" to "exam paper",
        "fee_overrides" to "fee override",
        "mark_edit_requests" to "mark edit request",
        "notifications" to "notification",
        "period_sessions" to "timetable period",
        "profiles" to "account",
        "rooms" to "room",
        "semester_terms" to "semester term",
        "session_attendance" to "attendance record",
        "session_fee_heads" to "fee item",
        "session_fees" to "fee structure",
        "session_marks" to "marks record",
        "session_students" to "student",
        "session_subjects" to "subject",
        "student_link_requests" to "link request",
        "student_semester_gpa" to "semester result",
        "teachers" to "teacher",
        "timetable_periods" to "timetable period",
    )

    /** Plural, for "still has <n> things" wording. */
    private val TABLE_PLURALS = mapOf(
        "academic_sessions" to "sessions",
        "attendance_edit_requests" to "attendance edit requests",
        "buildings" to "buildings",
        "calendar_events" to "calendar events",
        "datesheet_slots" to "datesheet papers",
        "datesheets" to "datesheets",
        "departments" to "departments",
        "documents" to "documents",
        "exam_paper_submissions" to "exam papers",
        "fee_overrides" to "fee overrides",
        "mark_edit_requests" to "mark edit requests",
        "notifications" to "notifications",
        "period_sessions" to "timetable periods",
        "profiles" to "accounts",
        "rooms" to "rooms",
        "semester_terms" to "semester terms",
        "session_attendance" to "attendance records",
        "session_fee_heads" to "fee items",
        "session_fees" to "fee structures",
        "session_marks" to "marks records",
        "session_students" to "students",
        "session_subjects" to "subjects",
        "student_link_requests" to "link requests",
        "student_semester_gpa" to "semester results",
        "teachers" to "teachers",
        "timetable_periods" to "timetable periods",
    )

    /** Column names that are safe and meaningful to show, in the words users use for them. */
    private val COLUMN_LABELS = mapOf(
        "dept_id" to "Department",
        "start_year" to "Start year",
        "end_year" to "End year",
        "current_semester" to "Semester",
        "semester" to "Semester",
        "max_students" to "Student limit",
        "roll_number" to "Roll number",
        "email" to "Email",
        "name" to "Name",
        "code" to "Code",
        "room_no" to "Room number",
        "building_id" to "Building",
        "course_code" to "Subject code",
        "score" to "Score",
        "max_marks" to "Maximum marks",
        "gpa" to "GPA",
        "cgpa" to "CGPA",
        "reason" to "Reason",
        "file_size_bytes" to "File size",
        "start_time" to "Start time",
        "end_time" to "End time",
        "exam_date" to "Exam date",
        "title" to "Title",
        "message" to "Message",
        "label" to "Fee item name",
        "amount" to "Amount",
        "shift" to "Shift",
        "session_id" to "Session",
        "university_roll_no" to "University roll number",
        "registration_no" to "Registration number",
        "teacher_email" to "Teacher",
    )

    /** Key columns whose *value* is safe and useful to quote back ("roll number 22-101 is already ..."). */
    private val QUOTABLE_KEY_COLUMNS = listOf(
        "roll_number", "university_roll_no", "registration_no", "email", "room_no", "dept_id", "course_code", "label",
    )

    private val UNIQUE_BY_CONSTRAINT = mapOf(
        "academic_sessions_dept_id_start_year_program_key" to "A session for this department, intake year and program type already exists.",
        "academic_sessions_pkey" to "A session for this department and intake year already exists.",
        "one_cr_per_session_shift" to "This class already has a class representative (CR) for this shift. Remove that role from the other student first.",
        "one_gr_per_session_shift" to "This class already has a girls' representative (GR) for this shift. Remove that role from the other student first.",
        "uq_session_slot" to "This class already has a period at that day and time.",
        "uq_datesheet_session_semester_shift" to "A datesheet for this class, semester and shift already exists.",
        "uq_datesheet_slot_course" to "This subject already has a paper in this datesheet.",
        "uq_datesheet_slot_date" to "Another paper is already scheduled on that date in this datesheet.",
        "uq_exam_paper_active_slot" to "An exam paper is already uploaded for this paper. Delete it before uploading a new one.",
        "uq_attendance_edit_requests_pending_cell" to "An edit request for this attendance record is already waiting for review.",
        "rooms_building_id_room_no_key" to "This building already has a room with that number.",
    )

    fun uniqueViolation(pg: PostgresError): String {
        val constraint = pg.constraint
        val keys = pg.keyValues
        when (constraint) {
            "uq_university_roll" -> return quoted("University roll number", keys["university_roll_no"], "is already assigned to another student.")
            "uq_registration_no" -> return quoted("Registration number", keys["registration_no"], "is already assigned to another student.")
            "session_students_pkey" -> return quoted("Roll number", keys["roll_number"], "is already in this session.")
            "teachers_pkey" -> return quoted("A teacher with email", keys["email"], "already exists.")
            "departments_pkey" -> return quoted("A department with code", keys["dept_id"], "already exists.")
            "buildings_pkey" -> return "A building with that name already exists."
            "rooms_pkey" -> return "That room already exists."
        }
        constraint?.let { UNIQUE_BY_CONSTRAINT[it] }?.let { return it }

        val entity = pg.table?.let(TABLE_LABELS::get) ?: constraint?.let(::tableOfConstraint)?.let(TABLE_LABELS::get)
        val quotable = QUOTABLE_KEY_COLUMNS.firstOrNull { keys.containsKey(it) }
        return when {
            entity != null && quotable != null -> "A $entity with ${COLUMN_LABELS[quotable]?.lowercase()} ${keys[quotable]} already exists."
            entity != null -> "A $entity with the same details already exists."
            else -> "This record already exists. Check the details and try again."
        }
    }

    fun foreignKeyViolation(pg: PostgresError): String {
        val parentLabel = pg.foreignKeyParentTable?.let(TABLE_LABELS::get)
        if (pg.isBlockedByDependents) {
            val dependents = pg.table?.let(TABLE_PLURALS::get)
            return when {
                parentLabel != null && dependents != null -> "This $parentLabel can't be deleted or changed because it still has $dependents. Remove those first."
                else -> "This action cannot be completed because related records still exist."
            }
        }
        val referenced = pg.details?.let { REFERENCED_TABLE.find(it)?.groupValues?.get(1) }?.let(TABLE_LABELS::get)
        return if (referenced != null) {
            "The $referenced you selected no longer exists. Refresh and choose again."
        } else {
            "One of the items you selected no longer exists. Refresh and try again."
        }
    }

    fun checkViolation(pg: PostgresError): String {
        val constraint = pg.constraint.orEmpty()
        NAMED_CHECKS[constraint]?.let { return it }
        val entity = pg.table?.let(TABLE_LABELS::get)
        val column = columnOfCheckConstraint(constraint)?.let(COLUMN_LABELS::get)
        return when {
            column != null && entity != null -> "$column is outside the allowed range for this $entity."
            column != null -> "$column is outside the allowed range."
            else -> "One of the values is outside what's allowed. Check the details and try again."
        }
    }

    fun notNullViolation(pg: PostgresError): String {
        val column = pg.nullColumn?.let(COLUMN_LABELS::get)
        return if (column != null) "$column is required." else "A required field is missing."
    }

    fun tooLong(pg: PostgresError): String {
        val limit = TOO_LONG.find(pg.message)?.groupValues?.get(1)
        return if (limit != null) "One of the entered values is too long (at most $limit characters)." else "One of the entered values is too long."
    }

    fun invalidFormat(): String = "One of the entered values is in the wrong format. Check the details and try again."

    fun permissionDenied(pg: PostgresError): String {
        // Only an admin may change a saved score (teachers correct it through a mark edit request), so a batch that includes
        // a roll someone already saved is refused as a whole.
        if (pg.table == "session_marks") {
            return "Some of these scores were already saved and are locked. Reload the class, then request an edit for the ones that need changing."
        }
        val entity = pg.table?.let(TABLE_LABELS::get)
        return if (entity != null) {
            "You don't have permission to change $entity records."
        } else {
            "You do not have permission to perform this action."
        }
    }

    private fun quoted(prefix: String, value: String?, suffix: String): String =
        if (value.isNullOrBlank()) "$prefix $suffix".replace("  ", " ") else "$prefix $value $suffix"

    /** `session_students_pkey` -> `session_students` (longest known table that prefixes the name). */
    private fun tableOfConstraint(constraint: String): String? =
        TABLE_LABELS.keys.filter { constraint.startsWith("${it}_") }.maxByOrNull { it.length }

    /** `academic_sessions_current_semester_check` -> `current_semester`. */
    private fun columnOfCheckConstraint(constraint: String): String? {
        if (!constraint.endsWith("_check")) return null
        val table = tableOfConstraint(constraint) ?: return null
        return constraint.removePrefix("${table}_").removeSuffix("_check").takeIf { it.isNotBlank() && COLUMN_LABELS.containsKey(it) }
    }

    private val NAMED_CHECKS = mapOf(
        "academic_sessions_session_id_format" to "The session id doesn't match its department, intake year and program type.",
        "academic_sessions_current_semester_check" to "That semester isn't valid for this program (BS runs 1-8, MA Replacement runs 5-8).",
        "academic_sessions_end_year_check" to "The end year doesn't match the program length (BS is 4 years, MA Replacement is 2).",
        "academic_sessions_max_students_range" to "The student limit must be between 1 and 200.",
        "session_marks_check" to "The score must be between 0 and the maximum marks for this exam.",
        "session_marks_check1" to "The maximum marks don't match the exam type (midterm 25, sessional 15).",
        "timetable_periods_check" to "The period must end after it starts.",
        "exam_paper_submissions_file_size_bytes_check" to "The file is too large (5 MB at most).",
        "attendance_edit_requests_reason_check" to "The reason is too long (500 characters at most).",
        "student_semester_gpa_gpa_check" to "GPA must be between 0 and 4.",
        "student_semester_gpa_cgpa_check" to "CGPA must be between 0 and 4.",
        "session_attendance_late_only_present" to "A student can only be marked late when their status is Present.",
        "attendance_edit_requests_late_only_present" to "A student can only be marked late when their requested status is Present.",
    )

    /** Every constraint/index name this object words specially -- checked against a real migrated database by DatabaseScenarioMessagesTest. */
    val namedConstraints: Set<String> get() = UNIQUE_BY_CONSTRAINT.keys + NAMED_CHECKS.keys

    private val REFERENCED_TABLE = Regex("""is not present in table "([^"]+)"""")
    private val TOO_LONG = Regex("""\((\d+)\)""")
}
