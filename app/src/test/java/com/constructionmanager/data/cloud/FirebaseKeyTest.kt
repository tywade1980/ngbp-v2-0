package com.constructionmanager.data.cloud

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Audit B12: the Firebase key is not in the source; the build supplies it, and its absence is visible. */
class FirebaseKeyTest {
    @Test
    fun `no key means not configured, with a placeholder Firebase accepts`() {
        assertFalse(FirebaseKey.usable(""))
        assertFalse(FirebaseKey.usable(null))
        assertEquals(FirebaseKey.NOT_CONFIGURED, FirebaseKey.orPlaceholder(" "))
        assertTrue(FirebaseKey.usable("k-from-ci"))
        assertEquals("k-from-ci", FirebaseKey.orPlaceholder(" k-from-ci "))
    }

    @Test
    fun `no Google API key shape anywhere in the app source`() {
        val src = File("src/main")
        assertTrue("run from the app module directory", src.isDirectory)
        val hits = src.walkTopDown().filter { it.isFile }.filter { Regex("AIza[0-9A-Za-z_-]{20,}").containsMatchIn(it.readText()) }.toList()
        assertEquals(emptyList<File>(), hits)
    }
}
