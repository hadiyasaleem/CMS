package com.mbd.cmscommon.util

object StudentIdCodec {
    fun sessionIdOf(studentId: String): String = studentId.substringBeforeLast('_')

    fun rollOf(studentId: String): String = studentId.substringAfterLast('_')

    fun deptIdOf(sessionId: String): String =
        sessionId.substringBeforeLast('_').substringBeforeLast('_')
}
