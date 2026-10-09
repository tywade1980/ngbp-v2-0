package com.constructionmanager.di

import android.content.Context
import android.content.SharedPreferences
import com.constructionmanager.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("construction_manager_prefs", Context.MODE_PRIVATE)
    }

    /**
     * The shared HTTP client (the Caroline connector and the update check build on it). Bodies are logged only in debug
     * builds (audit B11: chat text and call records reached logcat in release builds), and the Authorization header is
     * never written to the log in any build.
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        HttpLogging.interceptorFor(BuildConfig.DEBUG)?.let { builder.addInterceptor(it) }
        return builder.build()
    }
}

/** Kept apart from Hilt so a JVM test can check it. */
object HttpLogging {
    fun levelFor(debugBuild: Boolean): HttpLoggingInterceptor.Level =
        if (debugBuild) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE

    /** null in a release build: no logging interceptor at all. */
    fun interceptorFor(debugBuild: Boolean): HttpLoggingInterceptor? {
        val level = levelFor(debugBuild)
        if (level == HttpLoggingInterceptor.Level.NONE) return null
        return HttpLoggingInterceptor().apply {
            this.level = level
            redactHeader("Authorization")
        }
    }
}
