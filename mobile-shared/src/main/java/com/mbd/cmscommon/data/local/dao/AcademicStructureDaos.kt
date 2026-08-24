package com.mbd.cmscommon.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmscommon.data.local.entity.AcademicSessionEntity
import com.mbd.cmscommon.data.local.entity.SemesterSubjectEntity
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

    suspend fun applyDelta(upserts: List<AcademicSessionEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}

@Dao
interface SemesterSubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<SemesterSubjectEntity>)

    @Query("DELETE FROM semester_subjects WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

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
interface SessionStudentDao {
    @Query("SELECT COUNT(*) FROM session_students WHERE sessionId = :sessionId AND isDeleted = 0")
    suspend fun countForSession(sessionId: String): Int

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
    @Query("SELECT * FROM session_periods WHERE sessionId = :sessionId AND day = :day AND isDeleted = 0")
    fun observeForSessionDay(sessionId: String, day: String): Flow<List<SessionPeriodEntity>>

    @Query("SELECT * FROM session_periods WHERE sessionId = :sessionId AND isDeleted = 0")
    fun observeForSession(sessionId: String): Flow<List<SessionPeriodEntity>>

    @Query("SELECT * FROM session_periods WHERE teacherId = :teacherId AND isDeleted = 0")
    fun observeForTeacher(teacherId: String): Flow<List<SessionPeriodEntity>>

    @Query("SELECT * FROM session_periods WHERE day = :day AND isDeleted = 0")
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
    @Query("SELECT * FROM session_marks WHERE sessionId = :sessionId AND courseCode = :courseCode AND examType = :examType")
    fun observeScores(sessionId: String, courseCode: String, examType: String): Flow<List<SessionMarkEntity>>

    @Query("SELECT * FROM session_marks WHERE sessionId = :sessionId AND rollNumber = :rollNumber")
    fun observeForStudent(sessionId: String, rollNumber: String): Flow<List<SessionMarkEntity>>

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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<StudentSemesterGpaEntity>)

    @Query("DELETE FROM student_semester_gpa WHERE id IN (:ids)")
    suspend fun deleteByIds(ids: List<String>)

    suspend fun applyDelta(upserts: List<StudentSemesterGpaEntity>, deletedIds: List<String>) {
        if (upserts.isNotEmpty()) upsertAll(upserts)
        if (deletedIds.isNotEmpty()) deleteByIds(deletedIds)
    }
}
