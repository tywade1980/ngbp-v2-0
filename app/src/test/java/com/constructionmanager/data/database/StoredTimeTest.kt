package com.constructionmanager.data.database

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

/** Audit B0: the seed rows hold an Instant string; reading them must not throw (it crashed Reports). */
class StoredTimeTest {
    private val ny = TimeZone.of("America/New_York")

    @Test
    fun `an Instant string from the seed data is read, in the phone's zone`() {
        assertEquals(LocalDateTime(2026, 10, 8, 19, 5, 12, 345_000_000), StoredTime.toLocalDateTime("2026-10-08T23:05:12.345Z", ny))
    }

    @Test
    fun `a LocalDateTime string is read as it was written`() {
        assertEquals(LocalDateTime(2026, 10, 8, 9, 30), StoredTime.toLocalDateTime("2026-10-08T09:30", ny))
    }

    @Test
    fun `an offset string is read`() {
        assertEquals(LocalDateTime(2026, 10, 8, 19, 0), StoredTime.toLocalDateTime("2026-10-08T23:00:00+00:00", ny))
    }

    @Test
    fun `every seed row's timestamp shape is readable`() {
        // the exact shape SeedData writes: Clock.System.now().toString()
        val written = kotlinx.datetime.Clock.System.now().toString()
        StoredTime.toLocalDateTime(written, ny)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `junk still fails loudly`() {
        StoredTime.toLocalDateTime("last tuesday", ny)
    }
}
