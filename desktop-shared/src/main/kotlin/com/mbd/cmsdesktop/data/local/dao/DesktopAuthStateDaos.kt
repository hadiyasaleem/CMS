package com.mbd.cmsdesktop.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthCodeVerifierEntity
import com.mbd.cmsdesktop.data.local.entity.DesktopAuthSessionEntity

@Dao
interface DesktopAuthSessionDao {
    @Query("SELECT * FROM desktop_auth_session WHERE id = 'default' LIMIT 1")
    suspend fun get(): DesktopAuthSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(session: DesktopAuthSessionEntity)

    @Query("DELETE FROM desktop_auth_session WHERE id = 'default'")
    suspend fun delete()
}

@Dao
interface DesktopAuthCodeVerifierDao {
    @Query("SELECT value FROM desktop_auth_code_verifier WHERE id = 'default' LIMIT 1")
    suspend fun get(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(verifier: DesktopAuthCodeVerifierEntity)

    @Query("DELETE FROM desktop_auth_code_verifier WHERE id = 'default'")
    suspend fun delete()
}
