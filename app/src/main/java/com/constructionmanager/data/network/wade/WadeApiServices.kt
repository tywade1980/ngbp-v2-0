package com.constructionmanager.data.network.wade

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Orchestrator: chat, reasoning and WCC construction tools (unified-agentic-ai-foundation). */
interface OrchestratorApi {
    @GET("health")
    suspend fun health(): Map<String, Any>

    @GET("agents")
    suspend fun agents(): List<AgentInfo>

    @POST("chat")
    suspend fun chat(@Body request: ChatRequest): ChatResponse

    @POST("wcc/estimate")
    suspend fun estimate(@Body request: EstimateRequest): EstimateResponse

    @GET("wcc/briefing")
    suspend fun briefing(): BriefingResponse

    @GET("wcc/pricebook/{term}")
    suspend fun pricebook(@Path("term") term: String): List<Map<String, Any>>
}

/** Memory layer (mem0). */
interface MemoryApi {
    @POST("memories")
    suspend fun add(@Body request: AddMemoryRequest): Map<String, Any>

    @POST("search")
    suspend fun search(@Body request: SearchMemoryRequest): MemorySearchResponse
}

/** Caroline receptionist / call screening (telephony_voice_ai). */
interface CarolineApi {
    @POST("screen")
    suspend fun screen(@Body request: ScreenRequest): ScreeningDecision

    @GET("calls")
    suspend fun calls(): List<CallRecord>
}
