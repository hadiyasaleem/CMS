package com.mbd.cmscommon.teacher

import com.mbd.cmscommon.domain.model.distinctTaughtTopics
import com.mbd.cmscommon.domain.model.outlineTopics
import com.mbd.cmscommon.domain.model.toggleTopic
import org.junit.Assert.assertEquals
import org.junit.Test

class CourseTopicsTest {
    @Test
    fun outlineIsSplitTrimmedAndDeduplicated() {
        assertEquals(listOf("Arrays", "Trees", "Graphs"), outlineTopics(" Arrays, Trees ,, arrays,Graphs "))
        assertEquals(emptyList<String>(), outlineTopics(null))
    }

    @Test
    fun toggleAddsAndRemoves() {
        assertEquals("Arrays", toggleTopic("", "Arrays"))
        assertEquals("Arrays, Trees", toggleTopic("Arrays", "Trees"))
        assertEquals("Trees", toggleTopic("Arrays, Trees", "arrays"))
    }

    @Test
    fun aTopicTaughtOnSeveralDaysIsListedOnce() {
        assertEquals(listOf("Arrays", "Trees", "Graphs"), distinctTaughtTopics(listOf("Arrays, Trees", "trees", "Arrays, Graphs", null)))
    }
}
