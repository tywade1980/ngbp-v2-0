package com.constructionmanager.data.network.wade

import com.google.gson.annotations.SerializedName

/* ----------------------------- Orchestrator ----------------------------- */

data class ChatRequest(
    @SerializedName("agent_name") val agentName: String = "caroline",
    @SerializedName("message") val message: String,
    @SerializedName("session_id") val sessionId: String
)

data class ChatResponse(
    @SerializedName("agent_name") val agentName: String? = null,
    @SerializedName("response") val response: String? = null,
    @SerializedName("session_id") val sessionId: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null
)

data class EstimateRequest(
    @SerializedName("client_name") val clientName: String,
    @SerializedName("project_type") val projectType: String,
    @SerializedName("notes") val notes: String = ""
)

data class EstimateResponse(
    @SerializedName("client_name") val clientName: String? = null,
    @SerializedName("project_type") val projectType: String? = null,
    @SerializedName("estimate_total") val estimateTotal: Double? = null,
    @SerializedName("summary") val summary: String? = null,
    @SerializedName("line_items") val lineItems: List<Map<String, Any>>? = null
)

data class BriefingResponse(
    @SerializedName("briefing") val briefing: String? = null,
    @SerializedName("generated_at") val generatedAt: String? = null
)

data class AgentInfo(
    @SerializedName("name") val name: String? = null,
    @SerializedName("role") val role: String? = null,
    @SerializedName("status") val status: String? = null
)

/* -------------------------------- Memory (mem0) -------------------------- */

data class MemoryMessage(
    @SerializedName("role") val role: String,
    @SerializedName("content") val content: String
)

data class AddMemoryRequest(
    @SerializedName("messages") val messages: List<MemoryMessage>,
    @SerializedName("user_id") val userId: String
)

data class SearchMemoryRequest(
    @SerializedName("query") val query: String,
    @SerializedName("user_id") val userId: String
)

data class MemoryItem(
    @SerializedName("id") val id: String? = null,
    @SerializedName("memory") val memory: String? = null,
    @SerializedName("score") val score: Double? = null
)

data class MemorySearchResponse(
    @SerializedName("results") val results: List<MemoryItem>? = null
)

/* ------------------------------ Caroline (calls) ------------------------ */

data class ScreenRequest(
    @SerializedName("call_sid") val callSid: String,
    @SerializedName("caller_number") val callerNumber: String,
    @SerializedName("caller_name") val callerName: String? = null
)

data class ScreeningDecision(
    @SerializedName("call_sid") val callSid: String? = null,
    @SerializedName("decision") val decision: String? = null, // allow|block|voicemail|transfer
    @SerializedName("reason") val reason: String? = null
)

data class CallRecord(
    @SerializedName("call_sid") val callSid: String? = null,
    @SerializedName("caller_number") val callerNumber: String? = null,
    @SerializedName("caller_name") val callerName: String? = null,
    @SerializedName("intent") val intent: String? = null,
    @SerializedName("transcript") val transcript: String? = null,
    @SerializedName("status") val status: String? = null
)
