package com.constructionmanager.data.subscriber

import com.google.gson.JsonParser
import com.constructionmanager.ui.screens.onboarding.OnboardingViewModel
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

/** The launcher client against a local https server (MockWebServer with a throwaway certificate). */
class LauncherClientTest {
    private lateinit var server: MockWebServer
    private lateinit var client: OkHttpClient

    private fun launcher() = LauncherClient(client, server.url("/").toString().trimEnd('/'))

    private fun intake() = SubscriberIntake(
        businessName = "Acme Realty", ownerName = "Jane Doe", ownerCell = "6145550123",
        email = "jane@acme.example", areaCode = "614", legalName = "Acme Realty LLC",
        ein = "12-3456789", street = "1 Main St", city = "Columbus", state = "OH", zip = "43215"
    )

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
    }

    @After
    fun tearDown() {
        runCatching { server.shutdown() }
    }

    private fun json(code: Int, body: String) =
        MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")

    @Test
    fun `intake wire keys are exactly the launcher's intake keys`() {
        val keys = intake().toWire().keys
        assertEquals(
            setOf(
                "business_name", "owner_name", "owner_cell", "email", "area_code", "timezone",
                "trade_blurb", "greeting", "legal_name", "ein", "entity_type", "company_type",
                "street", "city", "state", "zip", "website", "industry"
            ),
            keys
        )
    }

    @Test
    fun `submit posts the intake as JSON and returns the launch token`() = runBlocking<Unit> {
        server.enqueue(json(200, """{"ok":true,"token":"tok-abc","status_path":"/subscribe/api/status/tok-abc"}"""))
        val r = launcher().submit(intake())
        assertTrue(r is LauncherResult.Ok)
        assertEquals("tok-abc", (r as LauncherResult.Ok).value.token)
        val req = server.takeRequest()
        assertEquals("POST", req.method)
        assertEquals("/subscribe/api", req.path)
        assertNull("the platform door takes no auth header", req.getHeader("Authorization"))
        val body = JsonParser.parseString(req.body.readUtf8()).asJsonObject
        assertEquals("Acme Realty", body.get("business_name").asString)
        assertEquals("12-3456789", body.get("ein").asString)
        assertEquals("REAL ESTATE", body.get("industry").asString)
    }

    @Test
    fun `submit surfaces the launcher's own field errors`() = runBlocking<Unit> {
        server.enqueue(json(422, """{"ok":false,"errors":["business name is required","EIN must be 9 digits"]}"""))
        val r = launcher().submit(intake())
        assertTrue(r is LauncherResult.Failed)
        val msg = (r as LauncherResult.Failed).message
        assertTrue(msg.contains("business name is required"))
        assertTrue(msg.contains("EIN must be 9 digits"))
    }

    @Test
    fun `status reads a finished launch with its confirmation`() = runBlocking<Unit> {
        server.enqueue(
            json(
                200,
                """{"ok":true,"launch":{"token":"tok-abc","slug":"acme-realty","status":"done",
                "business_name":"Acme Realty",
                "steps":[{"key":"preflight","label":"Checking the platform","status":"done","error":null,"reasons":[]},
                         {"key":"vm","label":"Your computer (VM) and system","status":"done","error":null,"reasons":[]}],
                "confirmation":{"business_number":"+16145550100","crm_url":"https://1-2-3-4.nip.io/app",
                "app_token":"secret","voice":"live","texting":"registration submitted",
                "facilitation":"Tell your Caroline","admin":"Tailscale pending"}}}"""
            )
        )
        val r = launcher().status("tok-abc")
        assertTrue(r is LauncherResult.Ok)
        val s = (r as LauncherResult.Ok).value
        assertTrue(s.isDone)
        assertFalse(s.isBlockedAtPreflight)
        assertEquals("+16145550100", s.confirmation?.businessNumber)
        assertEquals("https://1-2-3-4.nip.io/app", s.confirmation?.crmUrl)
        assertEquals(2, s.steps.size)
        assertTrue(s.steps.all { it.isDone })
        assertEquals("/subscribe/api/status/tok-abc", server.takeRequest().path)
    }

    @Test
    fun `status reads a launch blocked at preflight with the platform's reasons`() = runBlocking<Unit> {
        server.enqueue(
            json(
                200,
                """{"ok":true,"launch":{"token":"tok-abc","slug":"acme-realty","status":"blocked_preflight",
                "business_name":"Acme Realty",
                "steps":[{"key":"preflight","label":"Checking the platform","status":"blocked","error":null,
                          "reasons":["the platform's VM permission is not granted yet"]}],
                "confirmation":null}}"""
            )
        )
        val r = launcher().status("tok-abc")
        assertTrue(r is LauncherResult.Ok)
        val s = (r as LauncherResult.Ok).value
        assertTrue(s.isBlockedAtPreflight)
        assertNull(s.confirmation)
        assertEquals(listOf("the platform's VM permission is not granted yet"), s.steps[0].reasons)
    }

    @Test
    fun `status of an unknown token is a plain failure`() = runBlocking<Unit> {
        server.enqueue(json(404, """{"ok":false,"error":"no such launch"}"""))
        val r = launcher().status("nope")
        assertTrue(r is LauncherResult.Failed)
    }

    @Test
    fun `office URL is derived from the tenant's own CRM URL`() {
        assertEquals("https://1-2-3-4.nip.io/api/", OnboardingViewModel.officeUrlFor("https://1-2-3-4.nip.io/app"))
        assertNull(OnboardingViewModel.officeUrlFor("not a url"))
    }
}
