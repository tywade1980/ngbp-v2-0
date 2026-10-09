package com.constructionmanager.data.caroline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId

class CarolineEndpointTest {
    private fun ok(s: String) = (CarolineEndpoint.normalize(s) as CarolineEndpoint.Checked.Ok).url

    @Test
    fun `the default is the VM over https (audit B9)`() {
        assertEquals("https://136-108-70-191.nip.io/api/", CarolineEndpoint.DEFAULT_BASE_URL)
    }

    @Test
    fun `a bare host gets https and the api path`() {
        assertEquals("https://136-108-70-191.nip.io/api/", ok("136-108-70-191.nip.io"))
        assertEquals("https://136-108-70-191.nip.io/api/", ok("  https://136-108-70-191.nip.io/ "))
        assertEquals("https://example.com/api/", ok("https://example.com/api"))
    }

    @Test
    fun `cleartext and junk are refused, never rewritten`() {
        assertTrue(CarolineEndpoint.normalize("http://136-108-70-191.nip.io/api/") is CarolineEndpoint.Checked.Refused)
        assertTrue(CarolineEndpoint.normalize("ftp://x.com") is CarolineEndpoint.Checked.Refused)
        assertTrue(CarolineEndpoint.normalize("") is CarolineEndpoint.Checked.Refused)
        assertTrue(CarolineEndpoint.normalize("not a host") is CarolineEndpoint.Checked.Refused)
        assertTrue(CarolineEndpoint.normalize("10.0.2.2:8000") is CarolineEndpoint.Checked.Ok) // typed on purpose: https only
    }

    @Test
    fun `server times are shown in the phone's zone, not as UTC`() {
        val ny = ZoneId.of("America/New_York")
        assertEquals("Thu Oct 8, 9:05 PM", serverTimeToLocal("2026-10-09 01:05:12.345678+00:00", ny))
        assertEquals("Thu Oct 8, 9:05 PM", serverTimeToLocal("2026-10-09T01:05:12+00:00", ny))
        assertNull(serverTimeToLocal("yesterday", ny))
        assertNull(serverTimeToLocal(null, ny))
    }
}
