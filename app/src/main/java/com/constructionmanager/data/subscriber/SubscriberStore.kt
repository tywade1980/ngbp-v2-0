package com.constructionmanager.data.subscriber

import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Where the subscriber front door keeps its state: the launch token the
 * platform issued this phone, and — once the launch lands — the binding to
 * the tenant's OWN system (their office URL on their VM, their number).
 *
 * The prefs file is excluded from backup and device transfer (res/xml):
 * a launch token or tenant binding restored onto another phone would point
 * that phone at someone else's system.
 */
interface SubscriberStore {
    /** The token of this phone's launch on the platform; null before any intake is submitted. */
    var launchToken: String?

    /** The tenant office API base URL (https://<their VM>/api/) once their launch completed. */
    var boundOfficeUrl: String?

    /** The tenant office (CRM) web URL from the launch confirmation. */
    var boundCrmUrl: String?

    var businessName: String?
    var businessNumber: String?

    /** True once the launch completed and the binding above was written. */
    var launchCompleted: Boolean

    /** "I already have a Caroline" — the owner escape hatch on the welcome pane. */
    var skipped: Boolean
}

class PrefsSubscriberStore(private val prefs: SharedPreferences) : SubscriberStore {
    override var launchToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, value) }

    override var boundOfficeUrl: String?
        get() = prefs.getString(KEY_OFFICE, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_OFFICE) else putString(KEY_OFFICE, value) }

    override var boundCrmUrl: String?
        get() = prefs.getString(KEY_CRM, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_CRM) else putString(KEY_CRM, value) }

    override var businessName: String?
        get() = prefs.getString(KEY_BIZ, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_BIZ) else putString(KEY_BIZ, value) }

    override var businessNumber: String?
        get() = prefs.getString(KEY_NUMBER, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_NUMBER) else putString(KEY_NUMBER, value) }

    override var launchCompleted: Boolean
        get() = prefs.getBoolean(KEY_DONE, false)
        set(value) = prefs.edit { putBoolean(KEY_DONE, value) }

    override var skipped: Boolean
        get() = prefs.getBoolean(KEY_SKIPPED, false)
        set(value) = prefs.edit { putBoolean(KEY_SKIPPED, value) }

    companion object {
        const val FILE = "subscriber_onboarding"
        private const val KEY_TOKEN = "launch_token"
        private const val KEY_OFFICE = "bound_office_url"
        private const val KEY_CRM = "bound_crm_url"
        private const val KEY_BIZ = "business_name"
        private const val KEY_NUMBER = "business_number"
        private const val KEY_DONE = "launch_completed"
        private const val KEY_SKIPPED = "skipped"
    }
}
