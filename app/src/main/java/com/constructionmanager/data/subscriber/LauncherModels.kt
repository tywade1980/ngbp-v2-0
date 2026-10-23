package com.constructionmanager.data.subscriber

/**
 * The subscriber intake and launch record, mirroring the platform launcher's
 * own shapes field for field (subscriber/subscribe_service.py on the host):
 * the app collects exactly what the subscribe page collects — no payment,
 * anywhere — and reads back exactly what the status page shows.
 */
data class SubscriberIntake(
    val businessName: String,
    val ownerName: String,
    val ownerCell: String,
    val email: String,
    val areaCode: String,
    val timezone: String = "",
    val tradeBlurb: String = "",
    val greeting: String = "",
    val legalName: String = "",
    val ein: String = "",
    val entityType: String = "LLC",
    val companyType: String = "private",
    val street: String = "",
    val city: String = "",
    val state: String = "",
    val zip: String = "",
    val website: String = "",
    val industry: String = "REAL ESTATE"
) {
    /** The wire shape: the launcher's intake keys, verbatim (snake_case). */
    fun toWire(): Map<String, String> = linkedMapOf(
        "business_name" to businessName,
        "owner_name" to ownerName,
        "owner_cell" to ownerCell,
        "email" to email,
        "area_code" to areaCode,
        "timezone" to timezone,
        "trade_blurb" to tradeBlurb,
        "greeting" to greeting,
        "legal_name" to legalName,
        "ein" to ein,
        "entity_type" to entityType,
        "company_type" to companyType,
        "street" to street,
        "city" to city,
        "state" to state,
        "zip" to zip,
        "website" to website,
        "industry" to industry
    )

    companion object {
        /** The launcher's industry list (subscriber/subscribe_service.py INDUSTRIES). */
        val INDUSTRIES = listOf(
            "REAL ESTATE", "CONSTRUCTION", "HOME SERVICES", "LEGAL", "HEALTHCARE",
            "FINANCIAL", "INSURANCE", "RETAIL", "HOSPITALITY", "EDUCATION", "OTHER"
        )

        /** The business types the launcher's validator accepts, verbatim. */
        val ENTITY_TYPES = listOf(
            "LLC", "Corporation", "Partnership", "Sole Proprietorship", "Non-Profit", "Government"
        )

        val COMPANY_TYPES = listOf("private", "public", "sole_proprietor", "non_profit", "government")
    }
}

/** POST /subscribe/api accepted: the launch exists and the pipeline is running it. */
data class LaunchStarted(val token: String, val statusPath: String?)

data class LaunchStep(
    val key: String,
    val label: String,
    val status: String,
    val error: String?,
    val reasons: List<String>
) {
    val isDone: Boolean get() = status == "done" || status == "created"
    val isBlocked: Boolean get() = status == "blocked"
    val isFailed: Boolean get() = status == "failed"
    val isWaiting: Boolean get() = status == "waiting_for_vm"
}

/** What a finished launch hands the subscriber (the status page's confirmation block). */
data class LaunchConfirmation(
    val businessNumber: String,
    val crmUrl: String,
    val appToken: String?,
    val voice: String?,
    val texting: String?,
    val facilitation: String?,
    val admin: String?
)

data class LaunchStatus(
    val token: String,
    val slug: String,
    val status: String,
    val businessName: String,
    val steps: List<LaunchStep>,
    val confirmation: LaunchConfirmation?
) {
    val isDone: Boolean get() = status == "done" && confirmation != null

    /** Preflight refused the launch before any spend: the platform (not the subscriber) is unfinished. */
    val isBlockedAtPreflight: Boolean get() = status == "blocked_preflight"

    val isTerminal: Boolean get() = status == "done" || status == "torn_down"
}
