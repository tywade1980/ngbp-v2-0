package com.constructionmanager.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Guards on files the build reads (run from the app module directory, as Gradle runs unit tests). They check that the
 * fixes stay in place: a re-added permission, service or fallback turns these red.
 */
class ManifestAndConfigTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun `only the permissions the app uses are declared (B14)`() {
        val declared = Regex("""<uses-permission\s+android:name="android\.permission\.([A-Z_]+)"""").findAll(manifest).map { it.groupValues[1] }.toSet()
        assertEquals(
            setOf(
                "INTERNET",
                "CAMERA",                                         // job photos (PhotoDocumentationScreen, JobRoomScreen)
                "REQUEST_INSTALL_PACKAGES",                       // OTA self-update (ApkInstaller)
                "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION", // Site fix (LocationReader, 0.5.0)
                "ACCESS_BACKGROUND_LOCATION",                     // a fix every 15 minutes with the app closed (LocationSync, 0.7.3)
                "REQUEST_IGNORE_BATTERY_OPTIMIZATIONS",           // keeps that job from being put to sleep (LocationSync, 0.7.3)
                "READ_CALL_LOG",                                  // call log sync (CallLogSync, 0.7.1)
                "READ_MEDIA_AUDIO", "READ_EXTERNAL_STORAGE",      // call recordings (RecordingsSync, 0.7.1; the latter to API 32)
            ),
            declared,
        )
    }

    @Test
    fun `call screening is gone (Tyler, Oct 8)`() {
        assertFalse(manifest.contains("CallScreeningService"))
        assertFalse(manifest.contains("BIND_SCREENING_SERVICE"))
        val code = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.map { it.readText() }.joinToString("\n")
        assertFalse(code.contains("ROLE_CALL_SCREENING"))
        assertFalse(code.contains("CallScreeningService"))
    }

    @Test
    fun `no cleartext and no emulator defaults (B9)`() {
        assertFalse(manifest.contains("usesCleartextTraffic=\"true\""))
        val code = File("src/main/java").walkTopDown().filter { it.extension == "kt" }.map { it.readText() }.joinToString("\n")
        assertFalse(code.contains("10.0.2.2"))
        assertFalse(Regex("\"http://[A-Za-z0-9]").containsMatchIn(code))   // a cleartext URL with a host
    }

    @Test
    fun `the local database never wipes itself on a schema change (B15)`() {
        val module = File("src/main/java/com/constructionmanager/di/DatabaseModule.kt").readText()
        assertFalse(module.contains(".fallbackToDestructiveMigration()"))
    }

    @Test
    fun `backup never carries the device token or the database WAL (B17)`() {
        listOf("src/main/res/xml/backup_rules.xml", "src/main/res/xml/data_extraction_rules.xml").forEach {
            val x = File(it).readText()
            assertTrue(it, x.contains("caroline_connector.xml"))
            assertTrue(it, x.contains("construction_manager_database-wal"))
            assertTrue(it, x.contains("construction_manager_prefs.xml"))
        }
    }
}
