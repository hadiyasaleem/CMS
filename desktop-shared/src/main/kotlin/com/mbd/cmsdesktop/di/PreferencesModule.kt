package com.mbd.cmsdesktop.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.mbd.cmsdesktop.data.cache.DesktopBootstrapSnapshotStore
import dagger.Module
import dagger.Provides
import java.io.File
import javax.inject.Singleton
import okio.Path.Companion.toPath

/**
 * Desktop has no Android Context to hand `PreferenceDataStoreFactory.create` — the store is
 * file-backed directly under `%APPDATA%/CMSDesktop/<cms.desktop.appId>/cms_prefs.preferences_pb`
 * (falling back to `user.home` off Windows), keyed per app id the same way [SupabaseModule]'s auth
 * settings and [DesktopBootstrapSnapshotStore]'s cache directory are.
 */
@Module
object PreferencesModule {

    private fun baseDir(): File {
        val appId = System.getProperty("cms.desktop.appId").orEmpty().ifBlank { "shared" }
        val root = System.getenv("APPDATA") ?: System.getProperty("user.home")
        return File(root, "CMSDesktop/$appId")
    }

    @Provides
    @Singleton
    fun provideDataStore(): DataStore<Preferences> {
        val dir = baseDir()
        dir.mkdirs()
        val file = File(dir, "cms_prefs.preferences_pb")
        return PreferenceDataStoreFactory.createWithPath { file.absolutePath.toPath() }
    }

    @Provides
    @Singleton
    fun provideBootstrapSnapshotStore(): DesktopBootstrapSnapshotStore = DesktopBootstrapSnapshotStore(baseDir())
}
