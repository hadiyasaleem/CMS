package com.mbd.cmsstudent.di

import android.content.Context
import androidx.room.Room
import com.mbd.cmscommon.data.local.CmsDatabase
import com.mbd.cmscommon.data.local.CMS_DATABASE_MIGRATIONS
import com.mbd.cmscommon.di.MobileCmsDatabase
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
    fun provideStudentDatabase(@ApplicationContext context: Context): CmsDatabase =
        Room.databaseBuilder(context, MobileCmsDatabase::class.java, "cms_student.db")
            .addMigrations(*CMS_DATABASE_MIGRATIONS)
            .fallbackToDestructiveMigration()
            .build()
}
