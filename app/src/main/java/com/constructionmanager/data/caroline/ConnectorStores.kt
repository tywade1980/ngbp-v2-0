package com.constructionmanager.data.caroline

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Where the connector keeps the server address, this phone's device token and its id. */
interface ConnectorSettings {
    var baseUrl: String
    var deviceToken: String?
    var deviceId: String?
}

/** One message waiting for the network. [clientMsgId] is also the server's duplicate key, so a resend never doubles it. */
data class OutboxItem(
    val clientMsgId: String,
    val text: String,
    val queuedAtMillis: Long
)

/** Messages typed while offline, oldest first. */
interface Outbox {
    fun items(): List<OutboxItem>
    fun add(item: OutboxItem): Boolean
    fun remove(clientMsgId: String)
    fun clear(): Int
}

/**
 * SharedPreferences-backed settings. The file is excluded from backup (res/xml/backup_rules.xml,
 * data_extraction_rules.xml): a device token restored onto another phone would be that other phone acting as this one.
 */
class PrefsConnectorSettings(private val prefs: SharedPreferences) : ConnectorSettings {
    override var baseUrl: String
        get() = prefs.getString(KEY_BASE, null) ?: CarolineEndpoint.DEFAULT_BASE_URL
        set(value) = prefs.edit { putString(KEY_BASE, value) }

    override var deviceToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_TOKEN) else putString(KEY_TOKEN, value) }

    override var deviceId: String?
        get() = prefs.getString(KEY_DEVICE, null)
        set(value) = prefs.edit { if (value == null) remove(KEY_DEVICE) else putString(KEY_DEVICE, value) }

    companion object {
        const val FILE = "caroline_connector"
        private const val KEY_BASE = "base_url"
        private const val KEY_TOKEN = "device_token"
        private const val KEY_DEVICE = "device_id"
    }
}

class PrefsOutbox(private val prefs: SharedPreferences, private val gson: Gson = Gson()) : Outbox {
    private val type = object : TypeToken<List<OutboxItem>>() {}.type

    @Synchronized
    override fun items(): List<OutboxItem> =
        prefs.getString(KEY, null)?.let { runCatching { gson.fromJson<List<OutboxItem>>(it, type) }.getOrNull() }.orEmpty()

    @Synchronized
    override fun add(item: OutboxItem): Boolean {
        val now = items()
        if (now.any { it.clientMsgId == item.clientMsgId }) return true
        if (now.size >= MAX_ITEMS) return false
        prefs.edit { putString(KEY, gson.toJson(now + item)) }
        return true
    }

    @Synchronized
    override fun remove(clientMsgId: String) {
        prefs.edit { putString(KEY, gson.toJson(items().filterNot { it.clientMsgId == clientMsgId })) }
    }

    @Synchronized
    override fun clear(): Int {
        val n = items().size
        prefs.edit { remove(KEY) }
        return n
    }

    companion object {
        const val MAX_ITEMS = 200
        private const val KEY = "outbox"
    }
}
