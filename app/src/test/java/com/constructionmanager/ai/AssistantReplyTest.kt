package com.constructionmanager.ai

import com.constructionmanager.data.caroline.AssistTextResponse
import com.constructionmanager.data.caroline.ConnectorError
import com.constructionmanager.data.caroline.ConnectorResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** What the chat says after a send must match what happened (charter: never claim what did not happen). */
class AssistantReplyTest {
    @Test
    fun `stored but not answered is said as delivered, never as Caroline's answer`() {
        val r = AssistantRepository.replyFor(
            ConnectorResult.Ok(AssistTextResponse(stored = true, duplicate = false, answered = false, detail = "Saved to your thread with Caroline.")),
            "order drywall"
        )
        assertEquals(AssistantReply.Source.DELIVERED, r.source)
        assertEquals("Saved to your thread with Caroline.", r.text)
    }

    @Test
    fun `an answer is shown only when the server says answered and sends words`() {
        val yes = AssistantRepository.replyFor(ConnectorResult.Ok(AssistTextResponse(stored = true, answered = true, reply = "On it.")), "x")
        assertEquals(AssistantReply.Source.CAROLINE, yes.source)
        val empty = AssistantRepository.replyFor(ConnectorResult.Ok(AssistTextResponse(stored = true, answered = true, reply = " ")), "x")
        assertEquals(AssistantReply.Source.DELIVERED, empty.source)
    }

    @Test
    fun `queued says not sent yet`() {
        val r = AssistantRepository.replyFor(ConnectorResult.Queued("id"), "x")
        assertEquals(AssistantReply.Source.QUEUED, r.source)
        assertTrue(r.text.startsWith("Not sent yet"))
    }

    @Test
    fun `a failure says Caroline was not reached and why`() {
        val r = AssistantRepository.replyFor(ConnectorResult.Failed(ConnectorError.Forbidden("a paired phone cannot use this route")), "hello")
        assertEquals(AssistantReply.Source.OFFLINE, r.source)
        assertTrue(r.text.contains("Caroline was not reached: a paired phone cannot use this route"))
    }

    @Test
    fun `the offline helper no longer says Hi Tyler or offers call screening (B17, screening removed)`() {
        val t = OfflineAssistant.reply("hi caroline") + OfflineAssistant.reply("can you screen my calls")
        assertFalse(t.contains("Tyler"))
        assertFalse(t.contains("screening role"))
        assertFalse(t.contains("I've noted that"))
    }
}
