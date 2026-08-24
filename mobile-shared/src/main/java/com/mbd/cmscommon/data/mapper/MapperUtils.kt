package com.mbd.cmscommon.data.mapper

internal fun String?.emptyToNull(): String? = if (this == null || isBlank()) null else this
