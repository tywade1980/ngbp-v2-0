package com.constructionmanager.domain.usecase

import com.constructionmanager.data.caroline.CarolineConnector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Audit B8: Sign Out must end the session, unpair from Caroline and clear the chat - and say what failed. */
class SignOutTest {
    @Test
    fun `every step runs and a clean run reports no problems`() = runBlocking<Unit> {
        val done = mutableListOf<String>()
        val r = SignOut(
            logoutAccount = { done += "account" },
            unpairCaroline = { done += "caroline"; CarolineConnector.SignOutReport(true, null, 0) },
            clearChat = { done += "chat" }
        ).run()
        assertEquals(listOf("account", "caroline", "chat"), done)
        assertTrue(r.accountSignedOut && r.carolineRevoked && r.problems.isEmpty())
    }

    @Test
    fun `a failing step does not stop the others, and is reported`() = runBlocking<Unit> {
        val done = mutableListOf<String>()
        val r = SignOut(
            logoutAccount = { throw IllegalStateException("no network") },
            unpairCaroline = { done += "caroline"; CarolineConnector.SignOutReport(false, "Can't reach Caroline's server", 2) },
            clearChat = { done += "chat" }
        ).run()
        assertEquals(listOf("caroline", "chat"), done)
        assertFalse(r.accountSignedOut)
        assertEquals(2, r.problems.size)
        assertTrue(r.problems[1].contains("revoke this phone in the Office"))
    }
}
