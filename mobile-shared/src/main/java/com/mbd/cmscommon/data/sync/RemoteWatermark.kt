package com.mbd.cmscommon.data.sync

import com.mbd.cmscommon.data.remote.PgTime

fun <T> Iterable<T>.maxRemoteUpdatedAt(current: String, updatedAtOf: (T) -> String?): String {
    val candidates = mapNotNull { updatedAtOf(it)?.let { value -> value to PgTime.parseOrEpoch(value) } }
    var best = current to PgTime.parseOrEpoch(current)
    for (candidate in candidates) {
        if (candidate.second > best.second) {
            best = candidate
        }
    }
    return best.first
}
