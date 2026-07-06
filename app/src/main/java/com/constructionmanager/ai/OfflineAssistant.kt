package com.constructionmanager.ai

/**
 * A lightweight, fully on-device fallback "Caroline" assistant.
 *
 * It keeps the master app useful when the Wade backend is disabled or unreachable:
 * it pattern-matches common construction-management intents and returns practical,
 * grounded guidance. Every reply is clearly marked so users know they are offline.
 */
object OfflineAssistant {

    fun reply(message: String): String {
        val m = message.lowercase().trim()
        val body = when {
            m.isBlank() ->
                "Tell me what you need — an estimate, a material lookup, a schedule update, or a project briefing."

            containsAny(m, "estimate", "quote", "bid", "cost", "price") ->
                "For a quick estimate I normally pull the WCC pricebook and RSMeans regional pricing. " +
                    "Give me the project type (kitchen remodel, deck, framing, etc.), rough square footage, " +
                    "and finish level, and I'll draft line items. Connect the Wade backend in settings to run the live estimator."

            containsAny(m, "schedule", "timeline", "deadline", "when") ->
                "I can sequence the standard phases — pre-construction, demolition, rough-in, inspections, " +
                    "finishes, and handover. Tell me the start date and crew size and I'll lay out a critical path."

            containsAny(m, "material", "lumber", "supplier", "order") ->
                "I track materials by project in the Materials tab. I can flag long-lead items (windows, cabinets, " +
                    "custom millwork) and suggest reorder points. Which project are we sourcing for?"

            containsAny(m, "call", "phone", "screen", "client", "lead") ->
                "Call screening runs in the Voice tab. I can qualify inbound leads, block spam, and route urgent " +
                    "client calls to you. Enable the screening role on the device to let me answer first."

            containsAny(m, "brief", "status", "update", "summary") ->
                "Here's the shape of a briefing: active projects, budget vs. actuals, anything off-schedule, and " +
                    "today's priorities. Connect the backend and I'll generate it live from your project data."

            containsAny(m, "hi", "hello", "hey", "caroline") ->
                "Hi Tyler — Caroline here. I can help with estimates, scheduling, materials, call screening, and " +
                    "daily briefings. What are we working on?"

            else ->
                "I've noted that. I can help most with estimates, scheduling, materials, briefings, and call " +
                    "screening. Connect the Wade backend in settings for full reasoning and memory."
        }
        return "$body\n\n_(offline mode — connect the Wade backend for live answers)_"
    }

    private fun containsAny(haystack: String, vararg needles: String): Boolean =
        needles.any { haystack.contains(it) }
}
