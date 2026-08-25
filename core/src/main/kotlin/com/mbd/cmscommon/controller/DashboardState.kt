package com.mbd.cmscommon.controller

data class DashboardState(
    val students: Int = 0,
    val teachers: Int = 0,
    val departments: Int = 0,
    val pendingRequests: Int = 0,
    val activeSessions: Int = 0,
)
