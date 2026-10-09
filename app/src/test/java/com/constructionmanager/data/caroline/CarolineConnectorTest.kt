package com.constructionmanager.data.caroline

import com.google.gson.JsonParser
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetAddress
import java.util.concurrent.TimeUnit

class FakeSettings(
    override var baseUrl: String = "",
    override var deviceToken: String? = null,
    override var deviceId: String? = null
) : ConnectorSettings

class FakeOutbox(private val max: Int = 200) : Outbox {
    val list = mutableListOf<OutboxItem>()
    override fun items() = list.toList()
    override fun add(item: OutboxItem): Boolean {
        if (list.any { it.clientMsgId == item.clientMsgId }) return true
        if (list.size >= max) return false
        list += item; return true
    }
    override fun remove(clientMsgId: String) { list.removeAll { it.clientMsgId == clientMsgId } }
    override fun clear(): Int { val n = list.size; list.clear(); return n }
}

/** The connector against a local https server (MockWebServer with a throwaway certificate). */
class CarolineConnectorTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var settings: FakeSettings
    private lateinit var outbox: FakeOutbox
    private val sleeps = mutableListOf<Long>()
    private var ids = 0

    private fun connector() = CarolineConnector(client, settings, outbox, sleep = { sleeps += it }, clock = { 1000L }, newId = { "id-${++ids}-0000-0000-0000" })

    @Before
    fun setUp() {
        val cert = HeldCertificate.Builder()
            .addSubjectAlternativeName(InetAddress.getByName("localhost").canonicalHostName)
            .addSubjectAlternativeName("localhost")
            .addSubjectAlternativeName("127.0.0.1")
            .build()
        val serverCerts = HandshakeCertificates.Builder().heldCertificate(cert).build()
        val clientCerts = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        server = MockWebServer()
        server.useHttps(serverCerts.sslSocketFactory(), false)
        server.start()
        client = OkHttpClient.Builder().sslSocketFactory(clientCerts.sslSocketFactory(), clientCerts.trustManager).build()
        settings = FakeSettings(baseUrl = server.url("/api/").toString())
        outbox = FakeOutbox()
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private fun json(code: Int, body: String) = MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")

    @Test
    fun `pair keeps the token and sends no Authorization header`() = runBlocking<Unit> {
        server.enqueue(json(200, """{"device_id":"dev_0123456789abcdef","token":"cdt_secret","tenant":"wcc","label":"Fold7"}"""))
        val r = connector().pair(" abcd-efgh ", "Fold7", "SM-F966U", "16", "1.0")
        assertTrue(r is ConnectorResult.Ok)
        assertEquals("cdt_secret", settings.deviceToken)
        assertEquals("dev_0123456789abcdef", settings.deviceId)
        val req = server.takeRequest()
        assertEquals("/api/pair", req.path)
        assertNull(req.getHeader("Authorization"))
        val body = JsonParser().parse(req.body.readUtf8()).asJsonObject
        assertEquals("abcd-efgh", body.get("pair_code").asString)
        assertEquals("SM-F966U", body.getAsJsonObject("device").get("model").asString)
    }

    @Test
    fun `pair refusal is reported with the server's words and saves nothing`() = runBlocking<Unit> {
        server.enqueue(json(409, """{"detail":"that pairing code was already used; make a new one in the Office"}"""))
        val r = connector().pair("ABCD-EFGH", "", null, null, null)
        assertTrue(r is ConnectorResult.Failed)
        assertTrue((r as ConnectorResult.Failed).error.message.contains("already used"))
        assertNull(settings.deviceToken)
    }

    @Test
    fun `pair is not retried - the code works once`() = runBlocking<Unit> {
        server.enqueue(json(503, """{"detail":"busy"}"""))
        server.enqueue(json(200, """{"device_id":"dev_0123456789abcdef","token":"cdt_x"}"""))
        val r = connector().pair("ABCD-EFGH", "", null, null, null)
        assertTrue(r is ConnectorResult.Failed)
        assertEquals(1, server.requestCount)
        assertNull(settings.deviceToken)
    }

    @Test
    fun `pair answer without a token is a failure, not a pairing`() = runBlocking<Unit> {
        server.enqueue(json(200, """{"device_id":"dev_0123456789abcdef"}"""))
        val r = connector().pair("ABCD-EFGH", "", null, null, null)
        assertTrue(r is ConnectorResult.Failed && r.error is ConnectorError.BadResponse)
        assertNull(settings.deviceToken)
    }

    @Test
    fun `device calls carry the token in the header and never in the URL`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        server.enqueue(json(200, """[{"id":"call-1","direction":"inbound","party_number":"+16145550101","outcome":"completed"}]"""))
        val r = connector().calls(20)
        assertEquals("+16145550101", (r as ConnectorResult.Ok).value.single().partyNumber)
        val req = server.takeRequest()
        assertEquals("Bearer cdt_secret", req.getHeader("Authorization"))
        assertEquals("/api/calls?limit=20", req.path)
        assertFalse(req.path!!.contains("cdt_secret"))
    }

    @Test
    fun `not paired means no request at all`() = runBlocking<Unit> {
        val r = connector().briefing()
        assertTrue(r is ConnectorResult.Failed && r.error is ConnectorError.NotPaired)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `a 401 on a device route forgets the token and says not paired`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_revoked"; settings.deviceId = "dev_0123456789abcdef"
        server.enqueue(json(401, """{"detail":"this phone is not paired (or was signed out)"}"""))
        val r = connector().whoAmI()
        assertTrue(r is ConnectorResult.Failed && r.error is ConnectorError.NotPaired)
        assertNull(settings.deviceToken)
        assertNull(settings.deviceId)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `server errors are retried with backoff, then succeed`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        server.enqueue(json(503, "{}"))
        server.enqueue(json(502, "{}"))
        server.enqueue(json(200, """{"text":"Two calls today."}"""))
        val r = connector().briefing()
        assertEquals("Two calls today.", (r as ConnectorResult.Ok).value.text)
        assertEquals(3, server.requestCount)
        assertEquals(listOf(500L, 1000L), sleeps)
    }

    @Test
    fun `a 403 is not retried and keeps the server's reason`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        server.enqueue(json(403, """{"detail":"a paired phone cannot use this route"}"""))
        val r = connector().calls()
        assertTrue(r is ConnectorResult.Failed && r.error is ConnectorError.Forbidden)
        assertEquals("a paired phone cannot use this route", (r as ConnectorResult.Failed).error.message)
        assertEquals(1, server.requestCount)
        assertEquals("cdt_secret", settings.deviceToken)
    }

    @Test
    fun `offline text goes to the outbox with its id, and the flush resends that same id`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        val base = settings.baseUrl
        server.shutdown()                                   // nothing listens: connection refused
        val r = connector().sendText("Order the drywall", clientMsgId = "11111111-2222-3333-4444-555555555555")
        assertTrue(r is ConnectorResult.Queued)
        assertEquals(listOf("11111111-2222-3333-4444-555555555555"), outbox.items().map { it.clientMsgId })
        assertEquals(listOf(500L, 1000L), sleeps)          // tried three times first

        // back online on a new server
        server = MockWebServer().also { it.useHttps(serverCertsFor(), false); it.start() }
        settings.baseUrl = server.url("/api/").toString()
        server.enqueue(json(200, """{"stored":true,"duplicate":false,"message_id":7,"answered":false}"""))
        val rep = connector().flushOutbox()
        assertEquals(1, rep.sent)
        assertEquals(0, rep.left)
        val body = JsonParser().parse(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("11111111-2222-3333-4444-555555555555", body.get("client_msg_id").asString)
        assertTrue(base.isNotEmpty())
    }

    @Test
    fun `a refused text is not queued`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        server.enqueue(json(422, """{"detail":[{"msg":"Value error, empty message"}]}"""))
        val r = connector().sendText(" ")
        assertTrue(r is ConnectorResult.Failed && r.error is ConnectorError.Http)
        assertEquals("Value error, empty message", (r as ConnectorResult.Failed).error.message)
        assertTrue(outbox.items().isEmpty())
    }

    @Test
    fun `flush keeps order and stops at the first network failure`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        outbox.add(OutboxItem("a", "first", 1)); outbox.add(OutboxItem("b", "second", 2))
        server.enqueue(json(200, """{"stored":true}"""))
        server.enqueue(json(503, "{}")); server.enqueue(json(503, "{}")); server.enqueue(json(503, "{}"))
        val rep = connector().flushOutbox()
        assertEquals(1, rep.sent)
        assertEquals(listOf("b"), outbox.items().map { it.clientMsgId })
        val first = JsonParser().parse(server.takeRequest().body.readUtf8()).asJsonObject
        assertEquals("a", first.get("client_msg_id").asString)
    }

    @Test
    fun `sign out revokes on the server and forgets the token`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"; settings.deviceId = "dev_0123456789abcdef"
        outbox.add(OutboxItem("a", "unsent", 1))
        server.enqueue(json(200, """{"revoked":"dev_0123456789abcdef"}"""))
        val rep = connector().signOut()
        assertTrue(rep.serverRevoked)
        assertEquals(1, rep.droppedUnsent)
        assertNull(settings.deviceToken)
        assertEquals("/api/devices/self/revoke", server.takeRequest().path)
    }

    @Test
    fun `sign out with no server still forgets the token and says the server was not told`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        server.shutdown()
        val rep = connector().signOut()
        assertFalse(rep.serverRevoked)
        assertTrue(rep.serverMessage!!.isNotBlank())
        assertNull(settings.deviceToken)
    }

    @Test
    fun `an http base address is refused before anything is sent`() = runBlocking<Unit> {
        settings.deviceToken = "cdt_secret"
        settings.baseUrl = "http://" + server.hostName + ":" + server.port + "/api/"
        val r = connector().briefing()
        assertTrue(r is ConnectorResult.Failed)
        assertEquals(0, server.requestCount)
    }

    @Test
    fun `timeouts are set on every call`() {
        val c = connector()
        val f = CarolineConnector::class.java.getDeclaredField("client").apply { isAccessible = true }
        val built = f.get(c) as OkHttpClient
        assertEquals(TimeUnit.SECONDS.toMillis(CarolineConnector.CONNECT_TIMEOUT_S).toInt(), built.connectTimeoutMillis)
        assertEquals(TimeUnit.SECONDS.toMillis(CarolineConnector.CALL_TIMEOUT_S).toInt(), built.callTimeoutMillis)
    }

    private fun serverCertsFor(): javax.net.ssl.SSLSocketFactory {
        // the client trusts only the first certificate; reuse it by rebuilding the client for the new server
        val cert = HeldCertificate.Builder()
            .addSubjectAlternativeName(InetAddress.getByName("localhost").canonicalHostName)
            .addSubjectAlternativeName("localhost")
            .addSubjectAlternativeName("127.0.0.1")
            .build()
        val clientCerts = HandshakeCertificates.Builder().addTrustedCertificate(cert.certificate).build()
        client = OkHttpClient.Builder().sslSocketFactory(clientCerts.sslSocketFactory(), clientCerts.trustManager).build()
        return HandshakeCertificates.Builder().heldCertificate(cert).build().sslSocketFactory()
    }
}
