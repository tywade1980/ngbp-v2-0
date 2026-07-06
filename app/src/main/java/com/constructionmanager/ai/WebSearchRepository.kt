package com.constructionmanager.ai

import com.constructionmanager.data.network.wade.WadeBackendConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * In-app web search via Tavily (free tier). Tavily returns a synthesized `answer` string plus
 * source results, so it's usable directly in chat without a model to summarize. The API key is
 * user-supplied in the assistant's backend settings; with no key, web search is simply disabled
 * and the assistant falls back to its bundled knowledge.
 */
@Singleton
class WebSearchRepository @Inject constructor(
    private val config: WadeBackendConfig
) {
    private val client = OkHttpClient.Builder()
        .callTimeout(20, TimeUnit.SECONDS)
        .build()

    fun isConfigured(): Boolean = config.webSearchApiKey.isNotBlank()

    /** Heuristic: does this message look like an explicit web lookup? */
    fun isSearchQuery(message: String): Boolean {
        val m = message.lowercase().trim()
        return m.startsWith("search ") || m.startsWith("look up ") || m.startsWith("google ") ||
            m.contains("on the web") || m.contains("online") || m.contains("latest") ||
            m.contains("current price")
    }

    suspend fun search(query: String): Result<String> = withContext(Dispatchers.IO) {
        val key = config.webSearchApiKey
        if (key.isBlank()) {
            return@withContext Result.failure(IllegalStateException("No web search key configured"))
        }
        runCatching {
            val payload = JSONObject()
                .put("query", cleanQuery(query))
                .put("max_results", 5)
                .put("include_answer", "basic")
                .put("search_depth", "basic")
                .toString()
            val request = Request.Builder()
                .url("https://api.tavily.com/search")
                .addHeader("Authorization", "Bearer $key")
                .addHeader("Content-Type", "application/json")
                .post(payload.toRequestBody(JSON_MEDIA))
                .build()
            client.newCall(request).execute().use { resp ->
                val bodyText = resp.body?.string().orEmpty()
                if (!resp.isSuccessful) error("Search failed (HTTP ${resp.code})")
                format(JSONObject(bodyText))
            }
        }
    }

    private fun cleanQuery(q: String): String =
        q.replaceFirst(Regex("^(search|look up|google)\\s+", RegexOption.IGNORE_CASE), "").trim().ifBlank { q }

    private fun format(json: JSONObject): String {
        val answer = json.optString("answer").takeIf { it.isNotBlank() }
        val results: JSONArray = json.optJSONArray("results") ?: JSONArray()
        val sb = StringBuilder()
        if (answer != null) sb.append(answer).append("\n")
        val n = minOf(results.length(), 3)
        if (n > 0) {
            sb.append("\nSources:\n")
            for (i in 0 until n) {
                val r = results.optJSONObject(i) ?: continue
                val title = r.optString("title").ifBlank { "result" }
                val url = r.optString("url")
                sb.append("• ").append(title).append(" — ").append(url).append("\n")
            }
        }
        return sb.toString().trim().ifBlank { "No useful results found." }
    }

    private companion object {
        val JSON_MEDIA = "application/json".toMediaType()
    }
}
