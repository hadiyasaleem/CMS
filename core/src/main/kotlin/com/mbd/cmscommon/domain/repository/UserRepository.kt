package com.mbd.cmscommon.domain.repository

import com.mbd.cmscommon.domain.model.UserRole
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Emits null when there is no signed-in user (e.g. after sign-out clears the local cache),
    // so callers such as the app-root role gate can actually leave the authenticated state.
    fun observeCurrentUserRole(): Flow<UserRole?>

    suspend fun resolveRole(uid: String): UserRole
    suspend fun getCachedRole(uid: String): UserRole
    suspend fun provisionAdmin(uid: String)
    suspend fun provisionTeacher(uid: String, teacherId: String)
    suspend fun provisionUnlinkedStudent(uid: String)
    suspend fun linkStudent(uid: String, studentId: String)
    suspend fun unlinkStudent(uid: String)
    suspend fun touchLastLogin(uid: String)
    suspend fun deleteUser(uid: String)
    suspend fun clearLocalCache()
}
