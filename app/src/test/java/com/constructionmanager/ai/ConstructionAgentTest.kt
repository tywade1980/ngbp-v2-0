package com.constructionmanager.ai

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConstructionAgentTest {
    @Test
    fun `questions that only contain a create word do not create anything (B4)`() {
        listOf(
            "what's the address of the project",
            "any news on the project?",
            "what's the start date of the project",
            "how do I add trim to a door?"
        ).forEach { assertFalse(it, ConstructionAgent.isCreateCommand(it)) }
    }

    @Test
    fun `commands still create`() {
        listOf(
            "create a project called Maple Kitchen",
            "Caroline, add a material called 2x4 at \$4",
            "please add a worker named Sam, electrician at \$45/hr",
            "new project called Enki Court deck",
            "can you start a project for the Reyes family"
        ).forEach { assertTrue(it, ConstructionAgent.isCreateCommand(it)) }
    }

    @Test
    fun `the reply never says synced unless the cloud write confirmed (B3)`() {
        assertTrue(ConstructionAgent.cloudSentence(Result.success(Unit)).contains("copied to the cloud"))
        val failed = ConstructionAgent.cloudSentence(Result.failure(IllegalStateException("PERMISSION_DENIED")))
        assertTrue(failed.contains("failed") && failed.contains("PERMISSION_DENIED"))
        val timedOut = ConstructionAgent.cloudSentence(null)
        assertTrue(timedOut.contains("did not confirm"))
        listOf(failed, timedOut).forEach { assertFalse(it.contains("synced")) }
    }

    @Test
    fun `a worker whose only record failed is reported as not added (B3)`() {
        val bad = ConstructionAgent.workerSentence("Sam", "electrician", "\$45", Result.failure(RuntimeException("offline")))
        assertTrue(bad.contains("NOT added"))
        assertFalse(bad.contains("✅"))
        val unknown = ConstructionAgent.workerSentence("Sam", "electrician", null, null)
        assertTrue(unknown.contains("not confirmed"))
        assertTrue(ConstructionAgent.workerSentence("Sam", "electrician", null, Result.success(Unit)).startsWith("✅ Added Sam"))
    }
}
