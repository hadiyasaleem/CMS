package com.mbd.cmscommon.controller

data class TimetableDraftValidation(
    val timeError: String? = null,
    val overlapError: String? = null,
    val dateError: String? = null,
) {
    val firstError: String? get() = timeError ?: dateError ?: overlapError
    val isValid: Boolean get() = firstError == null
}
