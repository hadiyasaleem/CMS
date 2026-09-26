package com.mbd.cmscommon.domain.model

/*
 * Roll-number blocks per shift. Mirrors the database's roll_block_error() (see the
 * 20260926120000_session_shift_consolidation migration), which is the authority; the app checks first only
 * to show a friendly message and to suggest the next free roll number.
 *
 * A roll number's serial is its trailing number (IT-22-09 -> 9). Morning uses serials 1..morningCapacity;
 * Evening uses the serials above it. morningCapacity is max_students for a single-shift session and half of
 * it for a BOTH session, so a 100-seat BOTH intake numbers Morning 01-50 and Evening 51 onward.
 */

private val TRAILING_SERIAL = Regex("""(\d+)\s*$""")

/** The trailing serial of [roll] (IT-22-09 -> 9), or null when it has none (or an absurdly long one). */
fun rollSerial(roll: String): Int? =
    TRAILING_SERIAL.find(roll)?.groupValues?.get(1)?.takeIf { it.length <= 6 }?.toInt()

/** Highest Morning serial for a session with [mode] and [maxStudents]. */
fun morningCapacity(mode: ShiftMode, maxStudents: Int): Int =
    if (mode == ShiftMode.BOTH) maxStudents / 2 else maxStudents

fun morningCapacity(session: AcademicSession): Int = morningCapacity(session.shiftMode, session.maxStudents)

/** The serials [shift] may use; Evening's block is open-ended (the session cap limits the head count). */
fun rollBlock(mode: ShiftMode, maxStudents: Int, shift: Session): IntRange {
    val cap = morningCapacity(mode, maxStudents)
    return when (shift) {
        Session.MORNING -> 1..cap
        Session.EVENING -> (cap + 1)..MAX_SERIAL
    }
}

/**
 * Why [roll] cannot be used for a [shift] student in a session with [mode] / [maxStudents], or null when it
 * can. Same checks and wording as the database, plus "this session has no such shift".
 */
fun rollBlockError(mode: ShiftMode, maxStudents: Int, shift: Session, roll: String): String? {
    if (!mode.allows(shift)) return "This session does not run a ${shift.label} shift."
    val serial = rollSerial(roll) ?: return "Roll number $roll must end with a serial number, e.g. IT-22-09."
    val cap = morningCapacity(mode, maxStudents)
    return when (shift) {
        Session.MORNING -> if (serial < 1 || serial > cap) "Morning roll numbers use serials 1-$cap; $roll is outside that range." else null
        Session.EVENING -> if (serial <= cap) "Evening roll numbers start after serial $cap; $roll is in the Morning range." else null
    }
}

fun rollBlockError(session: AcademicSession, shift: Session, roll: String): String? =
    rollBlockError(session.shiftMode, session.maxStudents, shift, roll)

/** "IT-22-" for department code IT and intake year 2022: the prefix new roll numbers are built on. */
fun rollPrefix(deptCode: String, startYear: Int): String =
    "${deptCode.trim().uppercase()}-${(startYear % 100).toString().padStart(2, '0')}-"

/**
 * The next free roll number for [shift]: one past the highest serial already used in that shift's block
 * (gaps are not reused, matching how colleges number intakes), or the block's first serial when the shift
 * has no students yet. Serials keep at least two digits (IT-22-09). Returns null when the Morning block is
 * full. [existingRolls] should be every roll number already in the session, both shifts.
 */
fun nextRollFor(
    mode: ShiftMode,
    maxStudents: Int,
    shift: Session,
    existingRolls: Collection<String>,
    prefix: String,
): String? {
    if (!mode.allows(shift)) return null
    val block = rollBlock(mode, maxStudents, shift)
    val highest = existingRolls.mapNotNull { rollSerial(it) }.filter { it in block }.maxOrNull()
    val next = if (highest == null) block.first else highest + 1
    if (next !in block) return null
    return prefix + next.toString().padStart(2, '0')
}

fun nextRollFor(session: AcademicSession, shift: Session, existingRolls: Collection<String>, prefix: String): String? =
    nextRollFor(session.shiftMode, session.maxStudents, shift, existingRolls, prefix)

/**
 * The shift a roll number's serial places it in for this session: the only shift of a single-shift session,
 * otherwise Morning up to the Morning capacity and Evening above it. Null when the roll has no serial.
 */
fun shiftForRoll(session: AcademicSession, roll: String): Session? {
    if (session.shiftMode != ShiftMode.BOTH) return session.shifts.single()
    val serial = rollSerial(roll) ?: return null
    return if (serial <= morningCapacity(session)) Session.MORNING else Session.EVENING
}

/** Upper bound for an Evening serial: the database rejects serials longer than six digits. */
private const val MAX_SERIAL = 999_999
