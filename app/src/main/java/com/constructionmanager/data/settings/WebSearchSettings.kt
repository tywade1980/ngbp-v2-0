package com.constructionmanager.data.settings

import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The Tavily key for in-app web search (user-typed; empty = web search off). Web search is a third-party service, not
 * Caroline, so it does not go through the Caroline connector. Same preference key as before, so an existing key stays.
 */
@Singleton
class WebSearchSettings @Inject constructor(private val prefs: SharedPreferences) {
    var apiKey: String
        get() = prefs.getString(KEY, "") ?: ""
        set(value) = prefs.edit { putString(KEY, value.trim()) }

    private companion object {
        const val KEY = "wade_web_search_key"
    }
}
