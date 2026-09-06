package com.mbd.cmscommon.util

/**
 * Process-wide context attached to every [LogRecord]. Populated once at app startup (appId,
 * appVersion, platform, deviceInfo) and refreshed by [com.mbd.cmscommon.auth.SessionManager] on
 * sign-in/sign-out (accountEmail). Plain `@Volatile` vars, not DI-injected, so it is reachable
 * from the DI-less `:core` controllers via [CmsLog].
 */
object LogContext {
    @Volatile var accountEmail: String? = null
    @Volatile var appId: String? = null
    @Volatile var appVersion: String? = null
    @Volatile var platform: String? = null
    @Volatile var deviceInfo: String? = null
}
