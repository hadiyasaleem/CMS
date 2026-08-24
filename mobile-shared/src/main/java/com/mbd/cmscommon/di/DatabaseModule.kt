package com.mbd.cmscommon.di

import android.content.Context
import androidx.room.Room
import com.mbd.cmscommon.data.local.CMS_DATABASE_MIGRATIONS
import com.mbd.cmscommon.data.local.CmsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideCmsDatabase(@ApplicationContext context: Context): CmsDatabase =
        Room.databaseBuilder(context, CmsDatabase::class.java, "cms.db")
            .addMigrations(*CMS_DATABASE_MIGRATIONS)
            .build()
}
