package com.constructionmanager.di

import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** Audit B11: bodies (chat text, call records) were logged in release builds. */
class HttpLoggingTest {
    @Test
    fun `release builds get no logging interceptor at all`() {
        assertEquals(HttpLoggingInterceptor.Level.NONE, HttpLogging.levelFor(false))
        assertNull(HttpLogging.interceptorFor(false))
    }

    @Test
    fun `debug builds log bodies`() {
        assertEquals(HttpLoggingInterceptor.Level.BODY, HttpLogging.interceptorFor(true)!!.level)
    }
}
