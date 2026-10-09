package com.constructionmanager.data.caroline

/** What came of one request to Caroline. Every failure carries a sentence a person can read. */
sealed class ConnectorResult<out T> {
    data class Ok<T>(val value: T) : ConnectorResult<T>()

    /** Not sent yet: no connection. It is in the outbox under [clientMsgId] and goes out, in order, when the phone is back. */
    data class Queued(val clientMsgId: String) : ConnectorResult<Nothing>()

    data class Failed(val error: ConnectorError) : ConnectorResult<Nothing>()

    fun valueOrNull(): T? = (this as? Ok<T>)?.value
}

sealed class ConnectorError(val message: String) {
    /** No token on this phone, or the server no longer knows it (revoked / signed out). */
    class NotPaired(message: String = "This phone is not paired with Caroline. Pair it with a code from the Office.") :
        ConnectorError(message)

    class Forbidden(detail: String) : ConnectorError(detail)

    class Http(val code: Int, detail: String) : ConnectorError(detail)

    class Network(detail: String) : ConnectorError(detail)

    class BadResponse(detail: String) : ConnectorError(detail)

    override fun toString(): String = "${this::class.simpleName}: $message"
}
