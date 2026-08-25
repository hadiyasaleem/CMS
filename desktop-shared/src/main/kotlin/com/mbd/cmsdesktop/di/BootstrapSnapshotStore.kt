package com.mbd.cmsdesktop.di

import java.util.prefs.Preferences
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks whether a role's one-time reference-data bootstrap (e.g. [com.mbd.cmscommon.data.sync.AdminDataBootstrapper])
 * has already completed for a given uid, so a relaunch doesn't re-download everything before the
 * splash screen dismisses. Backed by [Preferences] (per desktop app, keyed by `cms.desktop.appId`) —
 * desktop has no Room/DataStore to persist this in instead.
 */
@Singleton
class BootstrapSnapshotStore @Inject constructor() {
    private val prefs: Preferences by lazy {
        Preferences.userRoot().node("com/mbd/cmsdesktop/${System.getProperty("cms.desktop.appId", "app")}/bootstrap")
    }

    fun isBootstrapComplete(scope: String, uid: String): Boolean = prefs.getBoolean(key(scope, uid), false)

    fun markBootstrapComplete(scope: String, uid: String) {
        prefs.putBoolean(key(scope, uid), true)
    }

    private fun key(scope: String, uid: String) = "$scope:$uid"
}
