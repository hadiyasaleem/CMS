package com.mbd.cmscommon.domain.model

/*
 * Roll numbers are free-form -- a student's shift is whatever is stored on their row, not something
 * derived from their roll number. The only capacity rule left is a flat per-shift seat cap, mirrored by
 * the database's fn_enforce_roster_cap (see the relax_roll_number_shift_rule migration).
 */

/** Seats available in one shift of [session]: half of max_students for a session running both shifts,
 * all of it for a single-shift session. */
fun shiftCapacity(session: AcademicSession): Int =
    if (session.shiftMode == ShiftMode.BOTH) session.maxStudents / 2 else session.maxStudents

/** "IT-25-" for department code IT and intake year 2025: a starting hint for a new roll number, not a
 * requirement -- the serial after it can be anything. */
fun rollPrefix(deptCode: String, startYear: Int): String =
    "${deptCode.trim().uppercase()}-${(startYear % 100).toString().padStart(2, '0')}-"
