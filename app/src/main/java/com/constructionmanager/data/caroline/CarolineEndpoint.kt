package com.constructionmanager.data.caroline

/**
 * Where Caroline's Office API lives, and how a typed address is checked.
 *
 * One base URL for everything the app asks of Caroline. The default is the VM (Caddy -> caroline-office), so a fresh
 * install needs no typing. Only https is accepted: Android refuses cleartext at this targetSdk anyway (audit B9), and
 * the device token must never cross the network in the clear.
 */
object CarolineEndpoint {
    const val DEFAULT_BASE_URL = "https://136-108-70-191.nip.io/api/"

    sealed class Checked {
        data class Ok(val url: String) : Checked()
        data class Refused(val reason: String) : Checked()
    }

    /**
     * "136-108-70-191.nip.io"            -> https://136-108-70-191.nip.io/api/
     * "https://example.com/api"          -> https://example.com/api/
     * any http:// address              -> Refused (no cleartext)
     * ""                                 -> Refused
     */
    fun normalize(input: String): Checked {
        val t = input.trim()
        if (t.isEmpty()) return Checked.Refused("Type the address of Caroline's server.")
        val lower = t.lowercase()
        if (lower.startsWith("http://")) {
            return Checked.Refused("Only https:// addresses are allowed: the phone's key must not travel unencrypted.")
        }
        if (lower.contains("://") && !lower.startsWith("https://")) {
            return Checked.Refused("Only https:// addresses are allowed.")
        }
        var url = if (lower.startsWith("https://")) t else "https://$t"
        val hostAndPath = url.removePrefix("https://").removePrefix("HTTPS://")
        val host = hostAndPath.substringBefore('/')
        if (host.isBlank() || host.contains(' ') || !host.contains('.')) {
            return Checked.Refused("That does not look like a server address: \"$t\".")
        }
        if (hostAndPath.substringAfter('/', "").isEmpty()) url = url.trimEnd('/') + "/api"
        if (!url.endsWith("/")) url += "/"
        return Checked.Ok(url)
    }
}
