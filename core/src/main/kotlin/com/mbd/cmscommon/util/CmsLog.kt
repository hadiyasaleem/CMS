package com.mbd.cmscommon.util

import java.io.PrintWriter
import java.io.StringWriter
import java.util.ArrayDeque

/**
 * Process-wide central-logging facade. A plain `object` (not DI-injected) because `:core`
 * controllers are constructed by hand with no dependency-injection graph available to them —
 * this is reachable from anywhere, including `ScreenController`, without touching 40 constructors.
 *
 * Safety properties, all load-bearing:
 * 1. Every public function is wrapped in `try { … } catch (t: Throwable) {}` — the logger must
 *    never itself throw, or a logging bug could crash the very code path it was meant to record.
 * 2. A [ThreadLocal] re-entrancy guard: if [install]ing sink's own write path calls back into
 *    [CmsLog] (directly or via an exception it throws), that inner call is dropped rather than
 *    recursing. This is the crash-loop backstop.
 * 3. A small bounded queue buffers records logged before [install] runs (e.g. very early startup
 *    failures) and is drained once a sink is installed.
 *
 * No lock is held while invoking the sink, so a slow or blocking sink cannot deadlock callers.
 */
object CmsLog {
    private const val PRE_INSTALL_CAPACITY = 50

    @Volatile private var sink: LogSink? = null
    private val pending = ArrayDeque<LogRecord>()
    private val inLogger = ThreadLocal.withInitial { false }

    fun install(newSink: LogSink) {
        try {
            val drain: List<LogRecord>
            synchronized(pending) {
                sink = newSink
                drain = pending.toList()
                pending.clear()
            }
            drain.forEach { record -> dispatch(record) { newSink.write(record) } }
        } catch (t: Throwable) {
            // Installing the logger must never itself crash the app.
        }
    }

    /** Records an unexpected, non-fatal failure. Fire-and-forget. */
    fun critical(tag: String, message: String, error: Throwable? = null) {
        record(Severity.CRITICAL, kindOf(error), tag, message, error, blocking = false)
    }

    /** Records an uncaught crash. Persists synchronously — the process may be about to die. */
    fun crash(tag: String, error: Throwable) {
        record(Severity.CRITICAL, ErrorKind.UNEXPECTED, tag, error.message ?: error::class.java.name, error, blocking = true)
    }

    private fun kindOf(error: Throwable?): ErrorKind =
        error?.let { runCatching { ErrorClassifier.classify(it).kind }.getOrNull() } ?: ErrorKind.UNEXPECTED

    private fun record(severity: Severity, kind: ErrorKind, tag: String, message: String, error: Throwable?, blocking: Boolean) {
        try {
            if (inLogger.get()) return
            val logRecord = LogRecord(
                occurredAtMillis = System.currentTimeMillis(),
                severity = severity.name,
                kind = kind.name,
                tag = tag,
                message = message,
                stackTrace = error?.let(::stackTraceOf),
                accountEmail = LogContext.accountEmail,
                appId = LogContext.appId,
                appVersion = LogContext.appVersion,
                platform = LogContext.platform,
                deviceInfo = LogContext.deviceInfo,
            )
            val currentSink = sink
            if (currentSink == null) {
                synchronized(pending) {
                    if (pending.size >= PRE_INSTALL_CAPACITY) pending.poll()
                    pending.offer(logRecord)
                }
                return
            }
            dispatch(logRecord) { if (blocking) currentSink.writeBlocking(logRecord) else currentSink.write(logRecord) }
        } catch (t: Throwable) {
            // Never let a logging failure propagate.
        }
    }

    private fun dispatch(record: LogRecord, body: () -> Unit) {
        try {
            inLogger.set(true)
            body()
        } catch (t: Throwable) {
            // The sink misbehaved; drop this record rather than recurse into it.
        } finally {
            inLogger.set(false)
        }
    }

    private fun stackTraceOf(error: Throwable): String {
        val writer = StringWriter()
        error.printStackTrace(PrintWriter(writer))
        return writer.toString().take(8000)
    }
}
