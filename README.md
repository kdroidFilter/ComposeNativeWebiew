# ComposeNativeWebView 🌐

**A lightweight Compose Multiplatform WebView** with a
**KevinnZou/compose-webview-multiplatform-inspired API**, backed by **native OS webviews** on every platform.

```text
io.github.kdroidfilter.webview.*
```

✅ Android: `android.webkit.WebView`
✅ iOS: `WKWebView`
✅ Desktop: **Wry (Rust)** via **UniFFI** → WebView2 / WKWebView / WebKitGTK

---

## Why ComposeNativeWebView? ⚡

### Native engines. No bundled Chromium.

Unlike KCEF-based solutions, **Desktop uses the OS webview**:

* **Windows**: WebView2
* **macOS**: WKWebView
* **Linux**: WebKitGTK

👉 Smaller binaries, faster startup, system-level rendering.

---

### Tiny desktop footprint 📦

Desktop ships only:

* a **minimal JVM bridge**
* **native bindings** (UniFFI + Rust)

No embedded browser runtime.

---

### Familiar Compose WebView API 🧩

Same mental model as `compose-webview-multiplatform`:

* `WebViewState`
* `WebViewNavigator`
* observable loading state
* cookies
* JS ↔ Kotlin bridge
* request interception for app-driven navigation

---

## Quick start 🚀

```kotlin
@Composable
fun App() {
  val state = rememberWebViewState("https://example.com")
  WebView(state, Modifier.fillMaxSize())
}
```

That’s it.

---

## Installation 🧩

### Dependency (all platforms)

```kotlin
dependencies {
  implementation("io.github.kdroidfilter:composenativewebview:<version>")
}
```

Same artifact for **Android, iOS, Desktop**.

---

### Desktop only: enable native access ⚠️

Wry uses native access via JNA.

```kotlin
compose.desktop {
  application {
    jvmArgs += "--enable-native-access=ALL-UNNAMED"
  }
}
```

---

## Demo app 🎮

Run the feature showcase first:

* **Desktop**: `./gradlew :demo:run`
* **Android**: `./gradlew :demo-android:installDebug`
* **iOS**: open `iosApp/iosApp.xcodeproj` in Xcode and Run

Responsive UI:

* large screens → side **Tools** panel
* phones → **bottom sheet**

---

## Core features ✨

### Content loading

* `loadUrl(url, headers)`
* `loadHtml(html)`
* `loadHtmlFile(fileName, readType)`

---

### Navigation

* `navigateBack()`, `navigateForward()`
* `reload()`, `stopLoading()`
* `canGoBack`, `canGoForward`

---

### Observable state

* `isLoading`
* `loadingState`
* `lastLoadedUrl`
* `pageTitle`

---

### Cookies 🍪

Unified cookie API:

```kotlin
state.cookieManager.setCookie(...)
state.cookieManager.getCookies(url)
state.cookieManager.removeCookies(url)
state.cookieManager.removeAllCookies()
```

---

### JavaScript

```kotlin
navigator.evaluateJavaScript("document.title = 'Hello'")
```

Desktop is currently **fire-and-forget**.

---

### JS ↔ Kotlin bridge 🌉

* injected automatically after page load
* callback-based
* works on all platforms

```js
window.kmpJsBridge.callNative("echo", {...}, callback)
```

---

### RequestInterceptor 🚦

Intercept **navigator-initiated** navigations only:

```kotlin
override fun onInterceptUrlRequest(
  request: WebRequest,
  navigator: WebViewNavigator
): WebRequestInterceptResult
```

Useful for:

* blocking URLs
* app-driven routing
* security rules

---

## WebViewState & Navigator 📘

### State creation

```kotlin
val state = rememberWebViewState(
  url = "https://example.com"
) {
  customUserAgentString = "MyApp/1.0"
}
```

Supports:

* URL
* inline HTML
* resource files

---

### Navigator

```kotlin
val navigator = rememberWebViewNavigator()
WebView(state, navigator)
```

Commands:

* `loadUrl`
* `loadHtml`
* `loadHtmlFile`
* `evaluateJavaScript`

---

## Settings ⚙️

### Custom User-Agent

```kotlin
state.webSettings.customUserAgentString = "MyApp/1.2.3"
```

Desktop note:

* applied at creation time
* changing it **recreates** the WebView (debounced)
* JS context/history may be lost

👉 Set it early.

---

### Logging

```kotlin
state.webSettings.logSeverity = KLogSeverity.Debug
```

---

## Desktop advanced 🖥️

### Access native WebView handle

```kotlin
WebView(
  state,
  navigator,
  onCreated = { native ->
    println(native.getCurrentUrl())
  }
)
```

Useful for debugging or platform-specific hooks.

---

## Project structure 🗂️

* `wrywebview/` → Rust core + UniFFI bindings
* `wrywebview-compose/` → Compose API
* `demo-shared/` → shared demo UI
* `demo/`, `demo-android/`, `iosApp/` → platform launchers

---

## Limitations ⚠️

* RequestInterceptor does **not** intercept sub-resources
* Desktop UA change recreates the WebView

---


## Credits 🙏

* API inspiration: KevinnZou/compose-webview-multiplatform
* Wry (Tauri ecosystem)
* UniFFI (Mozilla)

