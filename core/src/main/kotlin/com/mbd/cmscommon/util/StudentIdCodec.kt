package com.mbd.cmscommon.util

object StudentIdCodec {
    fun sessionIdOf(studentId: String): String = studentId.substringBeforeLast('_')

    fun rollOf(studentId: String): String = studentId.substringAfterLast('_')

    /** Session ids are "{deptId}_{startYear}" (one session per intake, both shifts). */
    fun deptIdOf(sessionId: String): String = sessionId.substringBeforeLast('_')
}
