package io.github.kdroidfilter.webview.demo

import io.github.kdroidfilter.webview.request.RequestInterceptor
import io.github.kdroidfilter.webview.request.WebRequest
import io.github.kdroidfilter.webview.request.WebRequestInterceptResult
import io.github.kdroidfilter.webview.web.WebViewNavigator
import java.net.URLEncoder

internal class DemoRequestInterceptor(
    private val enabled: () -> Boolean,
    private val onLog: (String) -> Unit,
) : RequestInterceptor {
    override fun onInterceptUrlRequest(
        request: WebRequest,
        navigator: WebViewNavigator,
    ): WebRequestInterceptResult {
        if (!enabled()) return WebRequestInterceptResult.Allow

        val url = request.url
        if (url.contains("blocked", ignoreCase = true)) {
            onLog("interceptor: reject url=$url")
            return WebRequestInterceptResult.Reject
        }

        if (url == "https://example.com" || url == "https://www.example.com") {
            val rewritten = "https://httpbin.org/anything?from=interceptor&original=" + uriEncode(url)
            onLog("interceptor: rewrite $url -> $rewritten")
            val modified =
                request.copy(
                    url = rewritten,
                    headers = request.headers.toMutableMap().apply { put("X-Intercepted", "true") },
                )
            return WebRequestInterceptResult.Modify(modified)
        }

        request.headers["X-Intercepted"] = "true"
        onLog("interceptor: allow url=$url")
        return WebRequestInterceptResult.Allow
    }
}

private fun uriEncode(value: String): String =
    URLEncoder.encode(value, Charsets.UTF_8)

