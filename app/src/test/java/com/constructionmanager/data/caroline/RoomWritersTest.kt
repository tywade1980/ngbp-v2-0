package com.constructionmanager.data.caroline

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.tls.HandshakeCertificates
import okhttp3.tls.HeldCertificate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.net.InetAddress

/**
 * The phone's delete / archive / restore writes (HOCC 0.7.4, Tyler Nov 6 2026). A delete is a write like any other: sent ONCE
 * with this phone's token, never retried (a lost answer must not delete twice), and a refusal comes back in the office's own
 * words so the screen can show why (an anchored job's delete names Archive).
 */
class RoomWritersTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient
    private lateinit var settings: FakeSettings

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
        settings = FakeSettings(baseUrl = server.url("/api/").toString(), deviceToken = "cdt_phone", deviceId = "dev_1")
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private fun writers() = RoomWriters(CarolineConnector(client, settings, FakeOutbox()), client, settings)

    private fun json(code: Int, body: String) = MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")

    @Test
    fun `delete goes once to the door with the phone's token and returns the trash words`() = runBlocking<Unit> {
        server.enqueue(json(200, """{"ok":true,"deleted":7,"trash_id":3,"say":"Deleted - it is in the trash and can be restored."}"""))
        val r = writers().delete("tasks/7")
        assertTrue(r is ConnectorResult.Ok)
        val req = server.takeRequest()
        assertEquals("DELETE", req.method)
        assertEquals("/api/tasks/7", req.path)
        assertEquals("Bearer cdt_phone", req.getHeader("Authorization"))
        assertEquals("Deleted - it is in the trash and can be restored.", (r as ConnectorResult.Ok).value.asJsonObject.get("say").asString)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a refused delete carries the office's reason and is not retried`() = runBlocking<Unit> {
        server.enqueue(json(422, """{"detail":"this job is anchored - signed work. Its only exit is Archive."}"""))
        val r = writers().delete("hub/projects/12")
        assertTrue(r is ConnectorResult.Failed)
        val e = (r as ConnectorResult.Failed).error
        assertTrue(e is ConnectorError.Http)
        assertTrue(e.message.contains("Archive"))
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `a server error on a delete is reported, never sent a second time`() = runBlocking<Unit> {
        server.enqueue(json(500, """{"detail":"boom"}"""))
        server.enqueue(json(200, """{"ok":true}"""))
        val r = writers().delete("hours/9")
        assertTrue(r is ConnectorResult.Failed)
        assertEquals(1, server.requestCount)
    }

    @Test
    fun `restore and archive are posts to their doors`() = runBlocking<Unit> {
        server.enqueue(json(200, """{"ok":true,"restored":3,"say":"Restored: Caulk tub."}"""))
        server.enqueue(json(200, """{"ok":true,"say":"Archived - kept on the record under Archived."}"""))
        val w = writers()
        assertTrue(w.post("trash/3/restore", com.google.gson.JsonObject()) is ConnectorResult.Ok)
        assertTrue(w.post("projects/12/archive", com.google.gson.JsonObject().apply { addProperty("reason", "") }) is ConnectorResult.Ok)
        val a = server.takeRequest(); val b = server.takeRequest()
        assertEquals("POST" to "/api/trash/3/restore", a.method to a.path)
        assertEquals("POST" to "/api/projects/12/archive", b.method to b.path)
    }
}
