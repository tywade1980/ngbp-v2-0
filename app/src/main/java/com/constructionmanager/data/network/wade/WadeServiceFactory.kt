package com.constructionmanager.data.network.wade

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Builds the Wade service clients against the currently-configured base URLs.
 *
 * Base URLs are user-editable at runtime (see [WadeBackendConfig]); Retrofit pins its
 * base URL at construction time, so this factory caches a client per URL and rebuilds
 * transparently whenever the configuration changes.
 */
@Singleton
class WadeServiceFactory @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val config: WadeBackendConfig
) {
    private val cache = mutableMapOf<String, Any>()

    fun orchestrator(): OrchestratorApi =
        service(config.orchestratorUrl, OrchestratorApi::class.java)

    fun memory(): MemoryApi =
        service(config.memoryUrl, MemoryApi::class.java)

    fun caroline(): CarolineApi =
        service(config.carolineUrl, CarolineApi::class.java)

    @Suppress("UNCHECKED_CAST")
    @Synchronized
    private fun <T> service(baseUrl: String, clazz: Class<T>): T {
        val key = "${clazz.name}@$baseUrl"
        cache[key]?.let { return it as T }
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val created = retrofit.create(clazz)
        cache[key] = created as Any
        return created
    }
}
