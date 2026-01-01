package io.github.kdroidfilter.webview.web

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import io.github.kdroidfilter.webview.cookie.WryCookieManager
import io.github.kdroidfilter.webview.jsbridge.JsMessage
import io.github.kdroidfilter.webview.jsbridge.WebViewJsBridge
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: () -> Unit = {},
    onDispose: () -> Unit = {},
) {
    WebView(
        state = state,
        modifier = modifier,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = { _ -> onCreated() },
        onDispose = { _ -> onDispose() },
    )
}

@Composable
fun WebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    factory: ((WebViewFactoryParam) -> NativeWebView)? = null,
) {
    val webView = state.webView

    webView?.let { wv ->
        LaunchedEffect(wv, navigator) {
            with(navigator) {
                wv.handleNavigationEvents()
            }
        }

        LaunchedEffect(wv, state) {
            snapshotFlow { state.content }.collect { content ->
                wv.loadContent(content)
            }
        }

        if (webViewJsBridge != null) {
            LaunchedEffect(wv, state) {
                val loadingStateFlow =
                    snapshotFlow { state.loadingState }.filter { it is LoadingState.Finished }
                val lastLoadedUrlFlow =
                    snapshotFlow { state.lastLoadedUrl }.filter { !it.isNullOrEmpty() }

                merge(loadingStateFlow, lastLoadedUrlFlow).collect {
                    if (state.loadingState is LoadingState.Finished) {
                        wv.injectJsBridge()
                    }
                }
            }
        }
    }

    ActualWebView(
        state = state,
        modifier = modifier,
        navigator = navigator,
        webViewJsBridge = webViewJsBridge,
        onCreated = onCreated,
        onDispose = onDispose,
        factory = factory ?: ::defaultWebViewFactory,
    )

    DisposableEffect(Unit) {
        onDispose { webViewJsBridge?.clear() }
    }
}

class WebViewFactoryParam(
    val state: WebViewState,
    val fileContent: String = "",
    val userAgent: String? = null,
)

fun defaultWebViewFactory(param: WebViewFactoryParam): NativeWebView =
    when (val content = param.state.content) {
        is WebContent.Url -> NativeWebView(content.url, param.userAgent ?: param.state.webSettings.customUserAgentString)
        else -> NativeWebView("about:blank", param.userAgent ?: param.state.webSettings.customUserAgentString)
    }

@Composable
fun ActualWebView(
    state: WebViewState,
    modifier: Modifier = Modifier,
    navigator: WebViewNavigator = rememberWebViewNavigator(),
    webViewJsBridge: WebViewJsBridge? = null,
    onCreated: (NativeWebView) -> Unit = {},
    onDispose: (NativeWebView) -> Unit = {},
    factory: (WebViewFactoryParam) -> NativeWebView = ::defaultWebViewFactory,
) {
    val currentOnDispose by rememberUpdatedState(onDispose)
    val scope = rememberCoroutineScope()

    val desiredUserAgent = state.webSettings.customUserAgentString?.trim()?.takeIf { it.isNotEmpty() }
    var effectiveUserAgent by remember { mutableStateOf(desiredUserAgent) }

    LaunchedEffect(desiredUserAgent) {
        if (desiredUserAgent == effectiveUserAgent) return@LaunchedEffect
        // Wry applies user-agent at creation time, so recreate the webview after a small debounce.
        delay(400)
        effectiveUserAgent = desiredUserAgent
    }

    key(effectiveUserAgent) {
        val nativeWebView = remember(state, factory) { factory(WebViewFactoryParam(state, userAgent = effectiveUserAgent)) }

        val desktopWebView =
            remember(nativeWebView, scope, webViewJsBridge) {
                DesktopWebView(
                    webView = nativeWebView,
                    scope = scope,
                    webViewJsBridge = webViewJsBridge,
                )
            }

        LaunchedEffect(desktopWebView) {
            state.webView = desktopWebView
            webViewJsBridge?.webView = desktopWebView
            (state.cookieManager as? WryCookieManager)?.attach(nativeWebView)
        }

        // Poll native state (URL/loading/title/nav) and drain IPC messages for JS bridge.
        LaunchedEffect(nativeWebView, state, navigator, webViewJsBridge) {
            while (true) {
                if (!nativeWebView.isReady()) {
                    if (state.loadingState !is LoadingState.Initializing) {
                        state.loadingState = LoadingState.Initializing
                    }
                    delay(50)
                    continue
                }

                val isLoading = nativeWebView.isLoading()
                state.loadingState =
                    if (isLoading) {
                        val current = state.loadingState
                        val next =
                            when (current) {
                                is LoadingState.Loading -> (current.progress + 0.02f).coerceAtMost(0.9f)
                                else -> 0.1f
                            }
                        LoadingState.Loading(next)
                    } else {
                        LoadingState.Finished
                    }

                val url = nativeWebView.getCurrentUrl()
                if (!url.isNullOrBlank()) {
                    if (!isLoading || state.lastLoadedUrl.isNullOrBlank()) {
                        state.lastLoadedUrl = url
                    }
                }

                val title = nativeWebView.getTitle()
                if (!title.isNullOrBlank()) {
                    state.pageTitle = title
                }

                navigator.canGoBack = nativeWebView.canGoBack()
                navigator.canGoForward = nativeWebView.canGoForward()

                if (webViewJsBridge != null) {
                    for (raw in nativeWebView.drainIpcMessages()) {
                        parseJsMessage(raw)?.let { webViewJsBridge.dispatch(it) }
                    }
                }

                delay(250)
            }
        }

        SwingPanel(
            modifier = modifier,
            factory = {
                onCreated(nativeWebView)
                nativeWebView
            },
        )

        DisposableEffect(nativeWebView) {
            onDispose {
                state.webView = null
                webViewJsBridge?.webView = null
                currentOnDispose(nativeWebView)
            }
        }
    }
}

private val jsBridgeJson = Json { ignoreUnknownKeys = true }

private fun parseJsMessage(raw: String): JsMessage? =
    runCatching {
        val obj = jsBridgeJson.parseToJsonElement(raw).jsonObject
        val callbackId = obj["callbackId"]?.jsonPrimitive?.content?.toIntOrNull() ?: -1
        val methodName = obj["methodName"]?.jsonPrimitive?.content ?: return null
        val params =
            obj["params"]?.jsonPrimitive?.content
                ?: obj["params"]?.toString()
                ?: ""
        JsMessage(callbackId = callbackId, methodName = methodName, params = params)
    }.getOrNull()
