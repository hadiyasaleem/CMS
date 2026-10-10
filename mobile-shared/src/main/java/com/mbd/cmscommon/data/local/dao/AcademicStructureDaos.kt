package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.PoolSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
import com.mbd.cmscommon.data.local.entity.SemesterTermEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceRowEntity
import com.mbd.cmscommon.data.local.entity.SessionAttendanceTallyEntity
import com.mbd.cmscommon.data.local.entity.SessionMarkEntity
import com.mbd.cmscommon.data.local.entity.SessionPeriodEntity
import com.mbd.cmscommon.data.local.entity.SessionStudentEntity
import com.mbd.cmscommon.data.local.entity.StudentSemesterGpaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicSessionDao {
    @Query("SELECT * FROM academic_sessions WHERE sessionId = :id LIMIT 1")
    suspend fun getById(id: String): AcademicSessionEntity?

    @Query("SELECT * FROM academic_sessions WHERE deptId = :deptId AND isDeleted = 0")
    fun observeSessionsForDept(deptId: String): Flow<List<AcademicSessionEntity>>

    @Query("SELECT * FROM academic_sessions WHERE isDeleted = 0")
    fun observeAllSessions(): Flow<List<AcademicSessionEntity>>

    @Query("SELECT * FROM academic_sessions WHERE isDeleted = 0")
    suspend fun getAllActive(): List<AcademicSessionEntity>

    @Query("SELECT * FROM academic_sessions WHERE sessionId = :sessionId LIMIT 1")
    fun observeSession(sessionId: String): Flow<AcademicSessionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: AcademicSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<AcademicSessionEntity>)

    @Query("DELETE FROM academic_sessions WHERE sessionId = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM academic_sessions WHERE sessionId IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM academic_sessions WHERE deptId = :deptId")
    suspend fun deleteForDept(deptId: String)

    @Query("UPDATE academic_sessions SET currentSemester = :semester WHERE sessionId = :sessionId")
    suspend fun setCurrentSemester(sessionId: String, semester: Int)

    @Query("UPDATE academic_sessions SET isActive = :isActive WHERE sessionId = :sessionId")
    suspend fun setActive(sessionId: String, isActive: Boolean)

    suspend fun applyDelta(upserts: List<AcademicSessionEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface SemesterSubjectDao {
    @Query("SELECT * FROM semester_subjects WHERE sessionId = :sessionId AND semester = :semester AND isDeleted = 0")
    fun observeSemesterSubjects(sessionId: String, semester: Int): Flow<List<SemesterSubjectEntity>>

    @Query("SELECT * FROM semester_subjects WHERE sessionId = :sessionId AND isDeleted = 0")
    fun observeSessionSubjects(sessionId: String): Flow<List<SemesterSubjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SemesterSubjectEntity>)

    @Query("DELETE FROM semester_subjects WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM semester_subjects WHERE sessionId = :sessionId AND semester = :semester AND courseCode = :courseCode")
    suspend fun deleteByCourseCode(sessionId: String, semester: Int, courseCode: String)

    @Query("DELETE FROM semester_subjects WHERE sessionId = :sessionId AND semester = :semester")
    suspend fun deleteForSemester(sessionId: String, semester: Int)

    @Query("DELETE FROM semester_subjects WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    suspend fun applyDelta(upserts: List<SemesterSubjectEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface PoolSubjectDao {
    @Query("SELECT * FROM subject_pool WHERE isDeleted = 0 ORDER BY courseCode")
    fun observeAll(): Flow<List<PoolSubjectEntity>>

    @Query("SELECT * FROM subject_pool WHERE courseCode = :courseCode LIMIT 1")
    suspend fun getByCode(courseCode: String): PoolSubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<PoolSubjectEntity>)

    @Query("DELETE FROM subject_pool WHERE courseCode IN (:codes)")
    suspend fun deleteByCodes(codes: List<String>)

    suspend fun applyDelta(upserts: List<PoolSubjectEntity>, deletedCodes: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedCodes.isNotEmpty()) deleteByCodes(deletedCodes)
    }
}

@Dao
interface SemesterTermDao {
    @Query("SELECT * FROM semester_terms WHERE sessionId = :sessionId AND semester = :semester AND isDeleted = 0 LIMIT 1")
    suspend fun getForSemester(sessionId: String, semester: Int): SemesterTermEntity?

    @Query("SELECT * FROM semester_terms WHERE sessionId = :sessionId AND isDeleted = 0")
    suspend fun getForSession(sessionId: String): List<SemesterTermEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SemesterTermEntity>)

    @Query("DELETE FROM semester_terms WHERE sessionId = :sessionId AND semester = :semester")
    suspend fun deleteForSemester(sessionId: String, semester: Int)

    suspend fun applyDelta(upserts: List<SemesterTermEntity>, deleted: List<Pair<String, Int>>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        deleted.forEach { (sessionId, semester) -> deleteForSemester(sessionId, semester) }
    }
}

@Dao
interface SessionStudentDao {
    @Query("SELECT COUNT(*) FROM session_students WHERE sessionId = :sessionId AND isDeleted = 0")
    suspend fun countForSession(sessionId: String): Int

    @Query("SELECT COUNT(*) FROM session_students WHERE sessionId = :sessionId AND shift = :shift AND isDeleted = 0")
    suspend fun countForSessionShift(sessionId: String, shift: String): Int

    @Query("SELECT * FROM session_students WHERE sessionId = :sessionId AND isDeleted = 0")
    fun observeForSession(sessionId: String): Flow<List<SessionStudentEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM session_students
        WHERE isDeleted = 0
        AND sessionId IN (SELECT sessionId FROM academic_sessions WHERE isActive = 1 AND isDeleted = 0)
        """,
    )
    fun observeActiveSessionStudentCount(): Flow<Int>

    @Query("SELECT * FROM session_students WHERE isDeleted = 0")
    suspend fun getAllActive(): List<SessionStudentEntity>

    @Query("SELECT * FROM session_students WHERE isDeleted = 0")
    fun observeAllActive(): Flow<List<SessionStudentEntity>>

    @Query("SELECT * FROM session_students WHERE sessionId = :sessionId AND rollNumber = :rollNumber LIMIT 1")
    suspend fun findByRoll(sessionId: String, rollNumber: String): SessionStudentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(student: SessionStudentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SessionStudentEntity>)

    @Query("DELETE FROM session_students WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM session_students WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM session_students WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    suspend fun applyDelta(upserts: List<SessionStudentEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface SessionPeriodDao {
    // Excludes shadow rows (id != remotePeriodId): a cross-session/teacher-wide view must see a merged
    // lecture exactly once, under its own owning row, not once more per linked session.
    @Query("SELECT * FROM session_periods WHERE isDeleted = 0 AND id = remotePeriodId")
    fun observeAll(): Flow<List<SessionPeriodEntity>>

    // Per-session views DO include shadow rows: a session merely linked to another session's lecture must
    // still see it occupying that slot on its own grid.
    @Query("SELECT * FROM session_periods WHERE sessionId = :sessionId AND day = :day AND isDeleted = 0")
    fun observeForSessionDay(sessionId: String, day: String): Flow<List<SessionPeriodEntity>>

    @Query("SELECT * FROM session_periods WHERE sessionId = :sessionId AND isDeleted = 0")
    fun observeForSession(sessionId: String): Flow<List<SessionPeriodEntity>>

    // A teacher teaches a period as its main teacher or as one of its co-teachers. :teacherId != ''
    // guards the co-teacher LIKE below: padded as ',' || coTeacherIds || ',', a period with NO
    // co-teachers becomes exactly ",," -- which a blank :teacherId's pattern ('%,,%') trivially
    // matches, so every such period in the whole college would otherwise show up as this teacher's own.
    @Query(
        "SELECT * FROM session_periods WHERE :teacherId != '' AND (teacherId = :teacherId OR (',' || coTeacherIds || ',') LIKE '%,' || :teacherId || ',%') " +
            "AND isDeleted = 0 AND id = remotePeriodId",
    )
    fun observeForTeacher(teacherId: String): Flow<List<SessionPeriodEntity>>

    @Query("SELECT * FROM session_periods WHERE day = :day AND isDeleted = 0 AND id = remotePeriodId")
    fun observeForDay(day: String): Flow<List<SessionPeriodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SessionPeriodEntity>)

    @Query("DELETE FROM session_periods WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM session_periods WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM session_periods WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    @Query("DELETE FROM session_periods WHERE sessionId = :sessionId AND day = :day")
    suspend fun deleteForSessionDay(sessionId: String, day: String)

    @Query("DELETE FROM session_periods WHERE sessionId = :sessionId AND shift = :shift AND day = :day AND startTime = :startTime")
    suspend fun deleteForSlot(sessionId: String, shift: String, day: String, startTime: String?)

    /** Removes a period's own row AND every shadow row of it (one per session merged into it), by the true
     * remote period id -- used when a merged lecture is fully removed, not just unmerged from one session. */
    @Query("DELETE FROM session_periods WHERE remotePeriodId = :remotePeriodId")
    suspend fun deleteAllRowsForRemotePeriod(remotePeriodId: String)

    /** Updates the linked-session CSV on a period's own (primary) row without touching its other fields. */
    @Query("UPDATE session_periods SET linkedSessionIds = :csv WHERE remotePeriodId = :remotePeriodId AND id = remotePeriodId")
    suspend fun setLinkedSessionIds(remotePeriodId: String, csv: String)

    /** Drops [sessionId]'s shadow rows that are no longer current (the lecture was unmerged elsewhere since
     * this session last synced), keeping only [keepIds]. Never touches [sessionId]'s own (primary) rows. */
    @Query("DELETE FROM session_periods WHERE sessionId = :sessionId AND id != remotePeriodId AND id NOT IN (:keepIds)")
    suspend fun deleteStaleShadowsForSession(sessionId: String, keepIds: List<String>)

    /** Every session that currently has a merge chip or a shadow row locally, so a global sync can revisit
     * and clear any that are no longer actually merged. */
    @Query(
        "SELECT sessionId FROM session_periods WHERE (linkedSessionIds != '' AND id = remotePeriodId) OR id != remotePeriodId GROUP BY sessionId",
    )
    suspend fun getSessionIdsWithLinks(): List<String>

    suspend fun applyDelta(upserts: List<SessionPeriodEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface SessionAttendanceDao {
    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId AND courseCode = :courseCode AND date = :date LIMIT 1")
    suspend fun getMarkedOn(sessionId: String, courseCode: String, date: String): SessionAttendanceRowEntity?

    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId AND courseCode = :courseCode AND date BETWEEN :from AND :to")
    suspend fun getRowsBetween(sessionId: String, courseCode: String, from: String, to: String): List<SessionAttendanceRowEntity>

    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId AND semester = :semester")
    suspend fun getRowsForSemester(sessionId: String, semester: Int): List<SessionAttendanceRowEntity>

    @Query("SELECT * FROM session_attendance_rows")
    suspend fun getAllRows(): List<SessionAttendanceRowEntity>

    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId AND courseCode = :courseCode")
    fun observeTalliesFor(sessionId: String, courseCode: String): Flow<List<SessionAttendanceRowEntity>>

    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId")
    fun observeTalliesForSession(sessionId: String): Flow<List<SessionAttendanceRowEntity>>

    @Query("SELECT * FROM session_attendance_rows WHERE sessionId = :sessionId AND rollNumber = :rollNumber")
    fun observeTalliesForStudent(sessionId: String, rollNumber: String): Flow<List<SessionAttendanceRowEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRows(items: List<SessionAttendanceRowEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SessionAttendanceTallyEntity>)

    @Query("DELETE FROM session_attendance_rows WHERE id IN (:ids)")
    suspend fun deleteRowsByIds(ids: List<String>)

    @Query("DELETE FROM session_attendance_rows WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    @Query("DELETE FROM session_attendance_rows WHERE sessionId = :sessionId AND courseCode = :courseCode")
    suspend fun deleteFor(sessionId: String, courseCode: String)

    suspend fun applyRowDelta(upserts: List<SessionAttendanceRowEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertRows(upserts)
        if (deletedIds.isNotEmpty()) deleteRowsByIds(deletedIds)
    }
}

@Dao
interface SessionMarkDao {
    @Query("SELECT * FROM session_marks WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): SessionMarkEntity?

    @Query("SELECT * FROM session_marks WHERE sessionId = :sessionId AND courseCode = :courseCode AND examType = :examType")
    fun observeScores(sessionId: String, courseCode: String, examType: String): Flow<List<SessionMarkEntity>>

    @Query("SELECT * FROM session_marks WHERE sessionId = :sessionId AND rollNumber = :rollNumber")
    fun observeForStudent(sessionId: String, rollNumber: String): Flow<List<SessionMarkEntity>>

    @Query("SELECT * FROM session_marks")
    suspend fun getAllRows(): List<SessionMarkEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SessionMarkEntity>)

    @Query("DELETE FROM session_marks WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    @Query("DELETE FROM session_marks WHERE sessionId = :sessionId AND courseCode = :courseCode AND examType = :examType")
    suspend fun deleteFor(sessionId: String, courseCode: String, examType: String)

    @Query("DELETE FROM session_marks WHERE sessionId = :sessionId")
    suspend fun deleteForSession(sessionId: String)

    suspend fun applyDelta(upserts: List<SessionMarkEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface StudentSemesterGpaDao {
    @Query("SELECT * FROM student_semester_gpa WHERE sessionId = :sessionId AND semester = :semester")
    suspend fun getForSemester(sessionId: String, semester: Int): List<StudentSemesterGpaEntity>

    @Query("SELECT * FROM student_semester_gpa WHERE sessionId = :sessionId AND rollNumber = :rollNumber ORDER BY semester")
    suspend fun getForStudent(sessionId: String, rollNumber: String): List<StudentSemesterGpaEntity>

    @Query("SELECT * FROM student_semester_gpa")
    suspend fun getAllRows(): List<StudentSemesterGpaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StudentSemesterGpaEntity>)

    @Query("DELETE FROM student_semester_gpa WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<StudentSemesterGpaEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
