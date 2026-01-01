package io.github.kdroidfilter.webview.cookie

/**
 * Cookie Manager exposing access to cookies of the WebView.
 *
 * Desktop implementation is backed by Wry's cookie APIs.
 */
interface CookieManager {
    suspend fun setCookie(
        url: String,
        cookie: Cookie,
    )

    suspend fun getCookies(url: String): List<Cookie>

    suspend fun removeAllCookies()

    suspend fun removeCookies(url: String)
}

@Suppress("FunctionName") // Builder Function (API compatibility)
fun WebViewCookieManager(): CookieManager = WryCookieManager()

