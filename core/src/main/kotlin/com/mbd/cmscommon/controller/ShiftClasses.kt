package com.mbd.cmscommon.controller

import com.mbd.cmscommon.domain.model.AcademicSession
import com.mbd.cmscommon.domain.model.Datesheet
import com.mbd.cmscommon.domain.model.Session
import com.mbd.cmscommon.domain.model.SessionPeriod
import com.mbd.cmscommon.domain.repository.AcademicSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The Morning/Evening tab shown: the one picked when the session runs it, otherwise the session's first shift.
 * Timetables, datesheets and fees have no combined view -- each shift is its own grid.
 */
fun shiftTab(session: AcademicSession?, picked: Session?): Session =
    picked?.takeIf { session == null || session.runs(it) } ?: session?.shifts?.firstOrNull() ?: Session.MORNING

/** The tabs: only the shifts the session runs (a single-shift session gets one tab). */
fun shiftTabs(session: AcademicSession?): List<Session> = session?.shifts ?: listOf(Session.MORNING)

/** One shift's periods; null keeps every shift (e.g. a student whose roster row hasn't synced yet). */
fun periodsForShift(periods: List<SessionPeriod>, shift: Session?): List<SessionPeriod> =
    if (shift == null) periods else periods.filter { it.shift == shift }

/** "2022–2026 (E)": the session a period belongs to, with its shift, for schedules that mix sessions. */
fun periodSessionLabel(session: AcademicSession?, period: SessionPeriod): String =
    "${session?.label ?: period.sessionId} (${period.shift.shortLabel})"

/**
 * A class is one shift of a session: "IT_2022@EVENING". A bare session id means the whole session, which is
 * what older callers (and sessions whose shift is unknown) pass.
 */
fun shiftClassKey(sessionId: String, shift: Session?): String = if (shift == null) sessionId else "$sessionId@${shift.name}"

fun parseShiftClassKey(key: String): Pair<String, Session?> {
    val at = key.lastIndexOf('@')
    if (at < 0) return key to null
    val shift = Session.entries.firstOrNull { it.name == key.substring(at + 1) } ?: return key to null
    return key.substring(0, at) to shift
}

/** One picker entry per shift the session runs, e.g. ("IT_2022@MORNING", "Information Technology 2022–2026 (M)"). */
fun shiftClassOptions(sessions: List<AcademicSession>, label: (AcademicSession) -> String): List<Pair<String, String>> =
    sessions.flatMap { session ->
        session.shifts.map { shift ->
            val suffix = if (session.shifts.size > 1) " (${shift.shortLabel})" else " (${shift.label})"
            shiftClassKey(session.sessionId, shift) to label(session) + suffix
        }
    }

/** A student's own datesheet: their session's semester and shift, published only. */
fun studentDatesheet(sheets: List<Datesheet>, sessionId: String, semester: Int?, shift: Session?): Datesheet? =
    sheets.firstOrNull { it.sessionId == sessionId && it.semester == semester && it.published && (shift == null || it.shift == shift) }

/** A student's shift from their roster row; null until the roster has synced. */
fun AcademicSessionRepository.observeShiftOf(sessionId: String, rollNumber: String): Flow<Session?> =
    observeStudents(sessionId)
        .map { students -> students.firstOrNull { it.rollNumber.equals(rollNumber, ignoreCase = true) }?.shift }
        .distinctUntilChanged()
