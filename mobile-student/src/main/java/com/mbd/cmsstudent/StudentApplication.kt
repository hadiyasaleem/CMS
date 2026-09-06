package com.mbd.cmsstudent

import android.app.Application
import android.os.Build
import com.mbd.cmscommon.di.LogSinkEntryPoint
import com.mbd.cmscommon.util.CmsCrashHandlerInstaller
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class StudentApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        val logSink = EntryPointAccessors.fromApplication(this, LogSinkEntryPoint::class.java).logSink()
        val versionName = runCatching { packageManager.getPackageInfo(packageName, 0).versionName }.getOrNull() ?: "unknown"
        CmsCrashHandlerInstaller.install(
            sink = logSink,
            appId = packageName,
            appVersion = versionName,
            platform = "android",
            deviceInfo = "${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})",
        )
    }
}
