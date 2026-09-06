package com.mbd.cmscommon.util

/**
 * Where [CmsLog] persists a [LogRecord]. Implemented by `RoomLogSink` in mobile-shared/
 * desktop-shared, which buffers to a local Room table and flushes to Supabase during sync.
 *
 * Implementations must never throw and must never block the caller for long — [CmsLog] already
 * wraps every call in a try/catch as a backstop, but a well-behaved sink should not rely on that.
 */
interface LogSink {
    /** Fire-and-forget persist; safe to call from a hot path. */
    fun write(record: LogRecord)

    /**
     * Synchronous best-effort persist for the crash path, where the process is about to die and
     * an async write would be lost. Implementations should apply a short internal timeout rather
     * than block indefinitely.
     */
    fun writeBlocking(record: LogRecord)
}
