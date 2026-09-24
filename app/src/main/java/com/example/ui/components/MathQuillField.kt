package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.util.Log
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Controller for programmatic operations on a MathQuill MathField.
 */
class MathQuillController {
    internal var webView: WebView? = null
    internal var isLoaded: Boolean = false
    internal var queuedCommands = mutableListOf<String>()

    private fun execJs(js: String) {
        val wv = webView
        if (wv != null && isLoaded) {
            wv.post {
                wv.evaluateJavascript(js, null)
            }
        } else {
            queuedCommands.add(js)
        }
    }

    internal fun flushQueue() {
        val wv = webView ?: return
        isLoaded = true
        while (queuedCommands.isNotEmpty()) {
            val cmd = queuedCommands.removeAt(0)
            wv.evaluateJavascript(cmd, null)
        }
    }

    fun setLatex(latex: String) {
        val escaped = escapeJs(latex)
        execJs("setLatex('$escaped');")
    }

    fun insertTypedText(text: String) {
        val escaped = escapeJs(text)
        execJs("insertTypedText('$escaped');")
    }

    fun insertCmd(cmd: String) {
        val escaped = escapeJs(cmd)
        execJs("insertCmd('$escaped');")
    }

    fun insertWrite(latex: String) {
        val escaped = escapeJs(latex)
        execJs("insertWrite('$escaped');")
    }

    fun insertFunction(fn: String) {
        val escaped = escapeJs(fn)
        execJs("insertFunction('$escaped');")
    }

    fun handleBackspace() {
        execJs("handleBackspace();")
    }

    fun moveCursorLeft() {
        execJs("moveCursorLeft();")
    }

    fun moveCursorRight() {
        execJs("moveCursorRight();")
    }

    fun moveCursorUp() {
        execJs("moveCursorUp();")
    }

    fun moveCursorDown() {
        execJs("moveCursorDown();")
    }

    fun moveToNextSlot() {
        execJs("moveToNextSlot();")
    }

    fun clear() {
        execJs("clearField();")
    }

    fun focus() {
        execJs("focusField();")
    }

    fun blur() {
        execJs("blurField();")
    }

    fun setFontSize(sizePx: Int) {
        execJs("setFontSize($sizePx);")
    }

    fun setTextColor(colorHex: String) {
        execJs("setTextColor('$colorHex');")
    }

    private fun escapeJs(str: String): String {
        return str
            .replace("\\", "\\\\")
            .replace("'", "\\'")
            .replace("\n", "\\n")
            .replace("\r", "")
    }
}

/**
 * High-performance interactive MathQuill MathField Composable.
 * Features built-in structured function templates, empty slots, slot-navigation (Tab/Arrows),
 * and native backspace unwrapping.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MathQuillField(
    latex: String,
    onLatexChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFocused: Boolean = false,
    controller: MathQuillController? = null,
    onEnter: (() -> Unit)? = null,
    onFocused: (() -> Unit)? = null,
    fontSizePx: Int = 20,
    textColor: Color = Color(0xFF0F172A)
) {
    val context = LocalContext.current
    val activeController = controller ?: remember { MathQuillController() }

    val currentOnLatexChange by rememberUpdatedState(onLatexChange)
    val currentOnEnter by rememberUpdatedState(onEnter)
    val currentOnFocused by rememberUpdatedState(onFocused)

    // Bridge instance
    val bridge = remember {
        object {
            @JavascriptInterface
            fun onLatexChange(newLatex: String) {
                currentOnLatexChange(newLatex)
            }

            @JavascriptInterface
            fun onEnter() {
                currentOnEnter?.invoke()
            }

            @JavascriptInterface
            fun onFocus() {
                currentOnFocused?.invoke()
            }

            @JavascriptInterface
            fun onMoveOutOf(direction: String) {
                // slot boundary reached
            }

            @JavascriptInterface
            fun onError(err: String) {
                Log.e("MathQuillField", "JS Error: $err")
            }
        }
    }

    // Keep controller updated with focus
    LaunchedEffect(isFocused) {
        if (isFocused) {
            activeController.focus()
        }
    }

    // Update latex if changed from outside
    var lastKnownLatex by remember { mutableStateOf(latex) }
    LaunchedEffect(latex) {
        if (latex != lastKnownLatex) {
            lastKnownLatex = latex
            activeController.setLatex(latex)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setBackgroundColor(AndroidColor.TRANSPARENT)
                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    allowFileAccess = true
                    allowContentAccess = true
                    allowFileAccessFromFileURLs = true
                    allowUniversalAccessFromFileURLs = true
                    databaseEnabled = true
                    cacheMode = WebSettings.LOAD_NO_CACHE
                    useWideViewPort = false
                    loadWithOverviewMode = true
                }

                addJavascriptInterface(bridge, "AndroidBridge")

                webChromeClient = object : WebChromeClient() {
                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        Log.d("MathQuillJS", "${consoleMessage?.message()} -- line ${consoleMessage?.lineNumber()}")
                        return true
                    }
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        activeController.webView = this@apply
                        activeController.flushQueue()
                        activeController.setFontSize(fontSizePx)
                        activeController.setTextColor(String.format("#%06X", 0xFFFFFF and textColor.toArgb()))
                        if (latex.isNotEmpty()) {
                            activeController.setLatex(latex)
                        }
                        if (isFocused) {
                            activeController.focus()
                        }
                    }
                }

                loadUrl("file:///android_asset/mathquill/mathquill-input.html")
            }
        },
        update = { webView ->
            activeController.webView = webView
        },
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
    )
}
