# ComposeWebview

Kotlin/JVM WebView using [wry](https://github.com/tauri-apps/wry) (Rust) with Gobley/UniFFI bindings, plus a Compose Desktop wrapper.

## Modules

- `wrywebview` - Rust + Gobley bindings.
- `wrywebview-compose` - Compose `WryWebView` composable.
- `demo` - sample Compose app.

## Usage (JVM)

1) Add the Compose wrapper module:

```kotlin
dependencies {
    implementation(project(":wrywebview-compose"))
}
```

2) Enable native access for the JVM process (required by JNA):

```kotlin
compose.desktop {
    application {
        jvmArgs += "--enable-native-access=ALL-UNNAMED"
    }
}
```

3) Use the composable:

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kdroidfilter.composewebview.WryWebView

@Composable
fun App() {
    WryWebView(
        url = "https://example.com",
        modifier = Modifier.fillMaxSize(),
    )
}
```

Stateful usage (navigation, loading, current URL):

```kotlin
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.kdroidfilter.composewebview.WryWebView
import io.github.kdroidfilter.composewebview.rememberWebViewState

@Composable
fun App() {
    val state = rememberWebViewState("https://example.com")

    // Use state.canGoBack, state.canGoForward, state.isLoading, state.currentUrl
    // and trigger state.goBack(), state.goForward(), state.reload(), state.loadUrl(...)
    WryWebView(state = state, modifier = Modifier.fillMaxSize())
}
```

## Run the demo

```shell
./gradlew :demo:run
```

## Requirements

- Rust toolchain (`rustup` installed).
