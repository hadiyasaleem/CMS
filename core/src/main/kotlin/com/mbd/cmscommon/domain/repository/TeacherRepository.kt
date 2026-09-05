package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.Teacher
import com.mbd.cmscommon.domain.model.TeacherStatus
import kotlinx.coroutines.flow.Flow

interface TeacherRepository {
    fun observeActiveTeachers(): Flow<List<Teacher>>
    fun observeTeacher(teacherId: String): Flow<Teacher>

    suspend fun getTeacher(teacherId: String): Teacher?
    suspend fun createTeacherAccount(email: String, password: String, teacher: Teacher)
    suspend fun updateTeacher(teacher: Teacher)
    suspend fun deleteTeacher(teacherId: String)
    suspend fun setStatus(teacherId: String, status: TeacherStatus)
    suspend fun resolveNameOrFallback(teacherId: String): String
    suspend fun sync()
    suspend fun syncSelf(teacherId: String)

    /** Uploads a new profile photo, stores it at photos/teachers/{teacherId}.{ext}, and records the path. */
    suspend fun uploadPhoto(teacherId: String, imageBytes: ByteArray, mimeType: String)

    /** Downloads a previously-uploaded photo's bytes, or null if it no longer exists. */
    suspend fun downloadPhoto(photoPath: String): ByteArray?
}
