package com.constructionmanager.data.database

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Reads a timestamp column back, whatever shape an earlier write gave it (audit B0).
 *
 * SeedData wrote `Clock.System.now().toString()` - an Instant ("2026-10-08T23:05:12.345Z") - into a column the
 * repository read with `LocalDateTime.parse`, which refuses the trailing "Z". Every install has those rows, so every read
 * of the catalog threw and the Reports screen crashed. Both shapes are read here; anything else is an error, loudly.
 */
object StoredTime {
    fun toLocalDateTime(raw: String, zone: TimeZone = TimeZone.currentSystemDefault()): LocalDateTime {
        val t = raw.trim()
        val hasOffset = t.endsWith("Z") || t.endsWith("z") || OFFSET.containsMatchIn(t)
        return if (hasOffset) Instant.parse(t).toLocalDateTime(zone) else LocalDateTime.parse(t)
    }

    private val OFFSET = Regex("""[+-]\d{2}:\d{2}$""")
}
