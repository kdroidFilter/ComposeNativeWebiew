package io.github.kdroidfilter.webview.util

/**
 * Lightweight logger used by the compat API layer.
 *
 * This project is JVM/Desktop-only; keep logging simple and avoid extra deps.
 */
internal object KLogger {
    private var minSeverity: KLogSeverity = KLogSeverity.Info

    fun setMinSeverity(severity: KLogSeverity) {
        minSeverity = severity
    }

    fun d(tag: String? = null, msg: () -> String) = log(KLogSeverity.Debug, tag, null, msg)

    fun i(tag: String? = null, msg: () -> String) = log(KLogSeverity.Info, tag, null, msg)

    fun w(tag: String? = null, msg: () -> String) = log(KLogSeverity.Warn, tag, null, msg)

    fun e(t: Throwable? = null, tag: String? = null, msg: () -> String) = log(KLogSeverity.Error, tag, t, msg)

    private fun log(severity: KLogSeverity, tag: String?, t: Throwable?, msg: () -> String) {
        if (severity.ordinal < minSeverity.ordinal) return
        val prefix = buildString {
            append("[ComposeWebView]")
            if (!tag.isNullOrBlank()) append("[$tag]")
            append(" ")
        }
        if (t != null) {
            System.err.println(prefix + msg())
            t.printStackTrace()
        } else {
            val out = if (severity.ordinal >= KLogSeverity.Warn.ordinal) System.err else System.out
            out.println(prefix + msg())
        }
    }
}

enum class KLogSeverity {
    Verbose,
    Debug,
    Info,
    Warn,
    Error,
    Assert,
}

