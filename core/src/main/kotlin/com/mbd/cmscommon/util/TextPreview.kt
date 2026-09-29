package com.mbd.cmscommon.util

/**
 * "A, B, C and 2 more" -- a short, readable list for error messages that name the things blocking an
 * action (rooms still in a building, classes a teacher still teaches) without growing unbounded.
 */
fun List<String>.previewText(max: Int = 3): String {
    val shown = take(max)
    val hidden = size - shown.size
    return if (hidden > 0) "${shown.joinToString(", ")} and $hidden more" else shown.joinToString(", ")
}
