package com.mbd.cmscommon.controller

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenControllerLaunchTest {

    private class Probe : ScreenController(CoroutineScope(Dispatchers.Unconfined)) {
        fun run(action: String?, block: suspend () -> Unit) = launch(action, block)
        fun runUnlabelled(block: suspend () -> Unit) = launch(block = block)
    }

    @Test
    fun anUnrecognisedFailureNamesTheActionAndCarriesAReference() {
        val probe = Probe()
        probe.run("save the teacher") { throw RuntimeException("totally unexpected") }
        val message = probe.error.value!!
        assertTrue(message, message.startsWith("Couldn't save the teacher. (Ref "))
    }

    @Test
    fun aClassifiedFailureShowsItsSpecificMessageWhateverTheAction() {
        val probe = Probe()
        probe.run("save the teacher") { throw com.mbd.cmscommon.util.CmsException.Conflict("Roll number IT-22-01 is already in this session.") }
        assertEquals("Roll number IT-22-01 is already in this session.", probe.error.value)
    }

    @Test
    fun launchWithoutALabelStillWorksAndUsesTheDefaultWording() {
        val probe = Probe()
        probe.runUnlabelled { throw RuntimeException("totally unexpected") }
        assertTrue(probe.error.value!!.startsWith("Something went wrong. Please try again. (Ref "))
    }

    @Test
    fun successLeavesNoError() {
        val probe = Probe()
        probe.run("save the teacher") { }
        assertNull(probe.error.value)
    }
}
