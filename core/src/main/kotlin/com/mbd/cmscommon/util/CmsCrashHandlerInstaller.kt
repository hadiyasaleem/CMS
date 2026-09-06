package com.mbd.cmscommon.util

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Wires [CmsLog] up at process startup: installs the platform [LogSink], populates [LogContext],
 * and chains an uncaught-exception handler onto whatever was previously registered (Android's
 * own crash-reporting hook, if any; none on desktop). Pure JVM — reused as-is by both mobile
 * (Application.onCreate) and desktop (before `application {}` in each Main.kt).
 *
 * The crash record is persisted *synchronously* via [LogSink.writeBlocking] before delegating to
 * the previous handler, because the process may be about to die and an async write would be lost.
 * An [AtomicBoolean] guard ensures a crash inside the handler itself cannot loop.
 */
object CmsCrashHandlerInstaller {
    fun install(sink: LogSink, appId: String, appVersion: String, platform: String, deviceInfo: String) {
        CmsLog.install(sink)
        LogContext.appId = appId
        LogContext.appVersion = appVersion
        LogContext.platform = platform
        LogContext.deviceInfo = deviceInfo

        val alreadyHandling = AtomicBoolean(false)
        val previous = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            if (alreadyHandling.compareAndSet(false, true)) {
                CmsLog.crash(thread.name, throwable)
            }
            previous?.uncaughtException(thread, throwable)
        }
    }
}
