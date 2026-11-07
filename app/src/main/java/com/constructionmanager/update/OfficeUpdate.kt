package com.constructionmanager.update

import com.google.gson.JsonElement
import com.google.gson.JsonObject

/**
 * The Office's GET hocc/app answer ({package, latest: {version_code, version_name, notes, size, sha256}}) read as an
 * update for Settings > App Updates. Nov 7 2026: that screen only read ota/update.json on GitHub, which still says
 * build 1, so it never saw a single 0.7.x build; the builds are published on the Office. Returns null when the
 * answer names no build; [newer] is false when the phone already has it.
 */
object OfficeUpdate {
    data class Read(val update: AvailableUpdate?, val newer: Boolean)

    fun read(answer: JsonElement?, currentVersionCode: Int, expectedPackage: String? = null): Read {
        val o = answer?.takeIf { it.isJsonObject }?.asJsonObject ?: return Read(null, false)
        val pkg = o.text("package")
        if (expectedPackage != null && pkg != null && pkg != expectedPackage) return Read(null, false)
        val latest = o.get("latest")?.takeIf { it.isJsonObject }?.asJsonObject ?: return Read(null, false)
        val code = latest.number("version_code")?.toInt() ?: return Read(null, false)
        val update = AvailableUpdate(
            versionCode = code,
            versionName = latest.text("version_name") ?: code.toString(),
            releaseNotes = latest.text("notes") ?: "",
            apkUrl = "",
            mandatory = false,
            sizeBytes = latest.number("size")?.toLong() ?: 0L,
            source = UpdateSource.SELF_HOSTED,
            fromOffice = true,
            sha256 = latest.text("sha256")
        )
        return Read(update, code > currentVersionCode)
    }

    private fun JsonObject.text(k: String): String? =
        get(k)?.takeIf { it.isJsonPrimitive }?.asString?.takeIf { it.isNotBlank() }

    private fun JsonObject.number(k: String): Double? =
        get(k)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asDouble
}
