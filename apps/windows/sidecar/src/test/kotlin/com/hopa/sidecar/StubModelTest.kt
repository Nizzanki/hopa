package com.hopa.sidecar

import com.hopa.core.model.ModelMessage
import com.hopa.core.model.ModelRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The stub is a proposer. These tests defend that it never presents itself as an
 * approval and never invents a success after a refusal.
 */
class StubModelTest {
    @Test
    fun a_plain_goal_is_answered_in_text() {
        val reply = reply(ModelMessage("user", "say hello"))

        assertTrue(reply.text.startsWith("Stub answered"))
        assertFalse("a plain answer must not look like an action", reply.text.contains("{"))
    }

    @Test
    fun a_payment_goal_is_only_proposed() {
        val text = reply(ModelMessage("user", "pay 100 for the order")).text

        assertTrue(text.contains("\"payment.pay\""))
        assertFalse("the stub cannot approve anything", text.contains("approved"))
        assertFalse("the stub cannot allow anything", text.contains("allow"))
    }

    @Test
    fun a_refusal_is_echoed_verbatim() {
        val text = reply(
            ModelMessage("user", "pay 100 for the order"),
            ModelMessage("assistant", """{"kind":"payment.pay"}"""),
            ModelMessage("user", "I did not run that."),
        ).text

        assertEquals("I did not run that.", text)
    }

    @Test
    fun a_device_failure_is_echoed_verbatim() {
        val text = reply(
            ModelMessage("user", "click the save button"),
            ModelMessage("assistant", """{"kind":"ui.tap"}"""),
            ModelMessage("user", "I could not do that: no device is attached"),
        ).text

        assertEquals("I could not do that: no device is attached", text)
    }

    @Test
    fun an_action_is_not_proposed_twice() {
        val text = reply(
            ModelMessage("user", "click the save button"),
            ModelMessage("assistant", """{"kind":"ui.tap","x":700,"y":600}"""),
        ).text

        assertFalse(text.contains("\"ui.tap\""))
        assertTrue(text.startsWith("Stub answered"))
    }

    private fun reply(vararg messages: ModelMessage) =
        runBlocking { StubModel().complete(ModelRequest(messages.toList())) }
}
