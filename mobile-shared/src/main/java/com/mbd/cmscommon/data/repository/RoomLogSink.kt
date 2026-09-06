package com.mbd.cmscommon.data.repository

import com.mbd.cmscommon.data.local.dao.AppLogDao
import com.mbd.cmscommon.data.mapper.AppLogMapper
import com.mbd.cmscommon.util.LogRecord
import com.mbd.cmscommon.util.LogSink
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull

/**
 * [LogSink] that persists to the local Room `app_logs` buffer (uploaded later by
 * [AppLogRepositoryImpl] during sync). Installed into [com.mbd.cmscommon.util.CmsLog] at app
 * startup. Owns its own scope rather than reusing a screen/ViewModel scope so a record survives
 * regardless of what UI is on screen when it is logged.
 */
@Singleton
class RoomLogSink @Inject constructor(
    private val appLogDao: AppLogDao,
) : LogSink {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun write(record: LogRecord) {
        scope.launch {
            runCatching { appLogDao.insert(AppLogMapper.recordToEntity(record)) }
        }
    }

    override fun writeBlocking(record: LogRecord) {
        runCatching {
            runBlocking(Dispatchers.IO) {
                withTimeoutOrNull(WRITE_BLOCKING_TIMEOUT_MS) {
                    appLogDao.insert(AppLogMapper.recordToEntity(record))
                }
            }
        }
    }

    private companion object {
        const val WRITE_BLOCKING_TIMEOUT_MS = 2000L
    }
}
