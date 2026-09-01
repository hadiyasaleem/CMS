package com.mbd.cmsadmin.di

import android.content.Context
import androidx.room.Room
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.CMS_DATABASE_MIGRATIONS
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Only this app's concrete Room instance lives here now — every DAO getter is provided generically
 * from the shared `CmsDatabase` base by `mobile-shared`'s `DaoModule` (see its doc comment).
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAdminDatabase(@ApplicationContext context: Context): CmsDatabase =
        Room.databaseBuilder(context, AdminDatabase::class.java, "cms_admin.db")
            .addMigrations(*CMS_DATABASE_MIGRATIONS)
            // Schema v5 (Supabase cutover) drops the legacy tables; destructive fallback wipes any
            // stale pre-cutover local cache rather than risking id-mismatch confusion after upgrade.
            .fallbackToDestructiveMigration()
            .build()
}
