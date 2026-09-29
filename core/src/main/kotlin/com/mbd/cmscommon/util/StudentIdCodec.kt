package com.mbd.cmscommon.util

object StudentIdCodec {
    fun sessionIdOf(studentId: String): String = studentId.substringBeforeLast('_')

    fun rollOf(studentId: String): String = studentId.substringAfterLast('_')

    /** Session ids are "{deptId}_{startYear}" (BS) or "{deptId}_{startYear}_ma" (MA Replacement). */
    fun deptIdOf(sessionId: String): String = sessionId.substringBefore('_')
}
