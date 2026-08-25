package com.mbd.cmsadmin.di

import android.content.Context
import androidx.room.Room
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.MIGRATION_18_19
import com.mbd.cmscommon.data.local.MIGRATION_19_20
import com.mbd.cmscommon.data.local.MIGRATION_20_21
import com.mbd.cmscommon.data.local.MIGRATION_21_22
import com.mbd.cmscommon.data.local.MIGRATION_22_23
import com.mbd.cmscommon.data.local.MIGRATION_23_24
import com.mbd.cmscommon.data.local.MIGRATION_24_25
import com.mbd.cmscommon.data.local.MIGRATION_25_26
import com.mbd.cmscommon.data.local.MIGRATION_26_27
import com.mbd.cmscommon.data.local.MIGRATION_27_28
import com.mbd.cmscommon.data.local.MIGRATION_28_29
import com.mbd.cmscommon.data.local.MIGRATION_29_30
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
            .addMigrations(MIGRATION_18_19, MIGRATION_19_20, MIGRATION_20_21, MIGRATION_21_22, MIGRATION_22_23, MIGRATION_23_24, MIGRATION_24_25, MIGRATION_25_26, MIGRATION_26_27, MIGRATION_27_28, MIGRATION_28_29, MIGRATION_29_30)
            // Schema v5 (Supabase cutover) drops the legacy tables; destructive fallback wipes any
            // stale pre-cutover local cache rather than risking id-mismatch confusion after upgrade.
            .fallbackToDestructiveMigration()
            .build()
}
