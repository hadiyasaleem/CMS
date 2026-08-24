package com.mbd.cmscommon.controller

data class BulkImportSummary(
    val succeeded: Int,
    val failures: List<String>,
)
