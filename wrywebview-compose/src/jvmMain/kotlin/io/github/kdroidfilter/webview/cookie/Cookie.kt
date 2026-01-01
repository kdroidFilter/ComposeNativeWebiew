package io.github.kdroidfilter.webview.cookie

/**
 * Cookie data class (API compatibility with compose-webview-multiplatform).
 */
data class Cookie(
    val name: String,
    val value: String,
    val domain: String? = null,
    val path: String? = null,
    val expiresDate: Long? = null,
    val isSessionOnly: Boolean = false,
    val sameSite: HTTPCookieSameSitePolicy? = null,
    val isSecure: Boolean? = null,
    val isHttpOnly: Boolean? = null,
    val maxAge: Long? = null,
) {
    enum class HTTPCookieSameSitePolicy {
        NONE,
        LAX,
        STRICT,
    }

    override fun toString(): String {
        var cookieValue = "$name=$value"

        if (path != null) cookieValue += "; Path=$path"
        if (domain != null) cookieValue += "; Domain=$domain"
        if (expiresDate != null) cookieValue += "; Expires=" + getCookieExpirationDate(expiresDate)
        if (maxAge != null) cookieValue += "; Max-Age=$maxAge"
        if (isSecure == true) cookieValue += "; Secure"
        if (isHttpOnly == true) cookieValue += "; HttpOnly"
        if (sameSite != null) cookieValue += "; SameSite=$sameSite"

        return "$cookieValue;"
    }
}

fun getCookieExpirationDate(expiresDate: Long): String {
    val sdf =
        java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", java.util.Locale.US).apply {
            timeZone = java.util.TimeZone.getTimeZone("GMT")
        }
    return sdf.format(java.util.Date(expiresDate))
}

