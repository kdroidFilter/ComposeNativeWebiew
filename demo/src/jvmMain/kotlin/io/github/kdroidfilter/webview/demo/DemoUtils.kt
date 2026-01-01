package io.github.kdroidfilter.webview.demo

import java.net.URI

internal fun normalizeUrl(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return "about:blank"
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) return trimmed
    return "https://$trimmed"
}

internal fun hostFromUrl(url: String): String? =
    runCatching { URI(url).host }.getOrNull()

