package com.mbd.cmsstudent

import android.app.Application
import android.os.Build
import com.mbd.cmscommon.util.CmsCrashHandlerInstaller
import com.mbd.cmscommon.util.LogSink
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class StudentApplication : Application() {

    @Inject lateinit var logSink: LogSink

    override fun onCreate() {
        super.onCreate()
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
