package io.github.kdroidfilter.webview.demo

import io.github.kdroidfilter.webview.cookie.Cookie
import io.github.kdroidfilter.webview.cookie.CookieManager
import io.github.kdroidfilter.webview.jsbridge.IJsMessageHandler
import io.github.kdroidfilter.webview.jsbridge.JsMessage
import io.github.kdroidfilter.webview.web.WebViewNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val demoJson = Json { ignoreUnknownKeys = true }

private fun jsonStringField(
    raw: String,
    key: String,
): String? =
    runCatching {
        demoJson.parseToJsonElement(raw).jsonObject[key]?.jsonPrimitive?.content
    }.getOrNull()

internal class EchoHandler(
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "echo"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val text = jsonStringField(message.params, "text") ?: message.params
        onLog("jsbridge: echo text=${text.take(120)}")
        callback(text)
    }
}

internal class AppInfoHandler(
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "appInfo"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val info =
            buildString {
                append("{")
                append("\"os\":\"").append(System.getProperty("os.name")).append("\",")
                append("\"arch\":\"").append(System.getProperty("os.arch")).append("\",")
                append("\"java\":\"").append(System.getProperty("java.version")).append("\"")
                append("}")
            }
        onLog("jsbridge: appInfo")
        callback(info)
    }
}

internal class NavigateHandler(
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "navigate"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val url = normalizeUrl(jsonStringField(message.params, "url") ?: "")
        onLog("jsbridge: navigate url=$url")
        navigator?.loadUrl(url)
    }
}

internal class SetCookieHandler(
    private val scope: CoroutineScope,
    private val cookieManager: CookieManager,
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "setCookie"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val url = normalizeUrl(jsonStringField(message.params, "url") ?: "")
        val name = jsonStringField(message.params, "name") ?: "demo_cookie"
        val value = jsonStringField(message.params, "value") ?: "from_js"

        val cookie =
            Cookie(
                name = name,
                value = value,
                domain = hostFromUrl(url),
                path = "/",
                isSessionOnly = true,
                isSecure = url.startsWith("https://"),
                isHttpOnly = false,
                sameSite = Cookie.HTTPCookieSameSitePolicy.LAX,
            )

        scope.launch {
            cookieManager.setCookie(url, cookie)
            onLog("jsbridge: setCookie url=$url name=$name")
            callback("ok")
        }
    }
}

internal class GetCookiesHandler(
    private val scope: CoroutineScope,
    private val cookieManager: CookieManager,
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "getCookies"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        val url = normalizeUrl(jsonStringField(message.params, "url") ?: "")
        scope.launch {
            val cookies = cookieManager.getCookies(url)
            onLog("jsbridge: getCookies url=$url count=${cookies.size}")
            callback(cookies.joinToString(prefix = "[", postfix = "]") { it.toString() })
        }
    }
}

internal class CustomHandler(
    private val onLog: (String) -> Unit,
) : IJsMessageHandler {
    override fun methodName(): String = "custom"

    override fun handle(
        message: JsMessage,
        navigator: WebViewNavigator?,
        callback: (String) -> Unit,
    ) {
        onLog("jsbridge: custom params=${message.params.take(160)}")
        callback(message.params)
    }
}

