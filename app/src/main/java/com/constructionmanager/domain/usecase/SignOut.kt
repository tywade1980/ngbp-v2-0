package com.constructionmanager.domain.usecase

import com.constructionmanager.data.assistant.ChatHistoryStore
import com.constructionmanager.data.caroline.CarolineConnector
import com.constructionmanager.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * "Sign Out" in Settings (audit B8: it only navigated; the Firebase session and the demo flag stayed, and the next
 * launch opened the dashboard again). Now it ends the account session, unpairs this phone from Caroline (revoked on the
 * server when reachable), and clears the chat kept on the phone. Each step runs even if an earlier one fails, and the
 * result says what did not happen.
 */
class SignOut(
    private val logoutAccount: suspend () -> Unit,
    private val unpairCaroline: suspend () -> CarolineConnector.SignOutReport,
    private val clearChat: () -> Unit
) {
    @Inject
    constructor(auth: AuthRepository, connector: CarolineConnector, chat: ChatHistoryStore) :
        this({ auth.logout() }, { connector.signOut() }, { chat.clear() })

    data class Report(val accountSignedOut: Boolean, val carolineRevoked: Boolean, val problems: List<String>)

    suspend fun run(): Report {
        val problems = mutableListOf<String>()
        val account = try {
            logoutAccount(); true
        } catch (e: Exception) {
            problems += "account sign-out failed (${e.message ?: e.javaClass.simpleName})"; false
        }
        val caroline = try {
            val rep = unpairCaroline()
            if (!rep.serverRevoked && rep.serverMessage != null) problems += "Caroline's server was not told (${rep.serverMessage}); revoke this phone in the Office"
            rep.serverRevoked
        } catch (e: Exception) {
            problems += "unpairing failed (${e.message ?: e.javaClass.simpleName})"; false
        }
        try {
            clearChat()
        } catch (e: Exception) {
            problems += "chat on this phone was not cleared"
        }
        return Report(account, caroline, problems)
    }
}
