package com.example.ui.components

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.util.Log
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Native RDKit-powered 2D Chemical Structure & Reaction Diagram View.
 * Renders molecular structures from SMILES strings and reactions 100% offline via bundled RDKit WebAssembly.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RDKitStructureView(
    smiles: String,
    modifier: Modifier = Modifier,
    isReaction: Boolean = false,
    widthPx: Int = 280,
    heightPx: Int = 180,
    backgroundColor: Color = Color.White
) {
    var isLoaded by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun updateStructure(wv: WebView?) {
        val safeSmiles = smiles.replace("\\", "\\\\").replace("'", "\\'")
        if (isReaction) {
            wv?.evaluateJavascript("renderReaction('$safeSmiles', $widthPx, $heightPx);", null)
        } else {
            wv?.evaluateJavascript("renderMolecule('$safeSmiles', $widthPx, $heightPx);", null)
        }
    }

    LaunchedEffect(smiles, isReaction, isLoaded) {
        if (isLoaded && webViewRef != null) {
            updateStructure(webViewRef)
        }
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .testTag("rdkit_structure_view"),
        contentAlignment = Alignment.Center
    ) {
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
                        cacheMode = WebSettings.LOAD_NO_CACHE
                    }

                    addJavascriptInterface(
                        object {
                            @JavascriptInterface
                            fun onReady() {
                                post {
                                    isLoaded = true
                                    updateStructure(this@apply)
                                }
                            }

                            @JavascriptInterface
                            fun onError(err: String) {
                                post {
                                    errorMessage = err
                                    Log.e("RDKit", "Error: $err")
                                }
                            }
                        },
                        "AndroidBridge"
                    )

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            webViewRef = this@apply
                            postDelayed({
                                isLoaded = true
                                updateStructure(this@apply)
                            }, 50)
                        }
                    }

                    loadUrl("file:///android_asset/rdkit/rdkit-renderer.html")
                }
            },
            update = { wv ->
                webViewRef = wv
                if (isLoaded) {
                    updateStructure(wv)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (!isLoaded && errorMessage == null) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 11.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}
