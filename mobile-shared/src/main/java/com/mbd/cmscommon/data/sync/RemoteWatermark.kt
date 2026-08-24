package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.remote.PgTime

fun <T> Iterable<T>.maxRemoteUpdatedAt(current: String?, updatedAtOf: (T) -> String?): String? {
    val candidates = mapNotNull { updatedAtOf(it)?.let { value -> value to PgTime.parseOrEpoch(value) } }
    var best = current?.let { it to PgTime.parseOrEpoch(it) }
    for (candidate in candidates) {
        if (best == null || candidate.second > best.second) {
            best = candidate
        }
    }
    return best?.first
}
