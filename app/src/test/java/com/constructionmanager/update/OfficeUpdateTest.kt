package com.constructionmanager.update

import com.google.gson.JsonParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OfficeUpdateTest {
    private val pkg = "com.wadecustomcarpentry.hocc"
    private fun answer(code: Int, p: String = pkg) = JsonParser.parseString(
        """{"package":"$p","caroline_number":"+16146121962","latest":{"version_code":$code,"version_name":"0.7.6",
           "size":23594989,"sha256":"e9afd4ed","notes":"Everything in one update.","download":"/api/hocc/app/apk"},"latest_problem":null}"""
    )

    @Test fun aNewerOfficeBuildIsOffered() {
        val r = OfficeUpdate.read(answer(33), currentVersionCode = 31, expectedPackage = pkg)
        assertTrue(r.newer)
        val u = r.update!!
        assertEquals(33, u.versionCode); assertEquals("0.7.6", u.versionName); assertEquals(23594989L, u.sizeBytes)
        assertTrue(u.fromOffice); assertEquals("e9afd4ed", u.sha256)
    }

    @Test fun theSameBuildIsUpToDate() {
        val r = OfficeUpdate.read(answer(33), currentVersionCode = 33, expectedPackage = pkg)
        assertFalse(r.newer)
    }

    @Test fun anotherAppsBuildIsNeverOffered() {
        assertNull(OfficeUpdate.read(answer(99, "com.example.other"), 1, pkg).update)
    }

    @Test fun noBuildPublishedOrNoAnswerOffersNothing() {
        assertNull(OfficeUpdate.read(JsonParser.parseString("""{"package":"$pkg","latest":null}"""), 1, pkg).update)
        assertNull(OfficeUpdate.read(null, 1, pkg).update)
    }
}
