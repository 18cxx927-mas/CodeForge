package com.example.ui.editor

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodePrimary
import com.example.viewmodel.CodeForgeViewModel

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MonacoEditorWebView(
    viewModel: CodeForgeViewModel,
    modifier: Modifier = Modifier
) {
    var isLoaded by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    DisposableEffect(Unit) {
        viewModel.editorBridgeAction = { script ->
            webViewRef?.post {
                webViewRef?.evaluateJavascript(script, null)
            }
        }
        onDispose {
            viewModel.editorBridgeAction = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(VsCodeBg)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    webViewRef = this
                    setBackgroundColor(0xFF1E1E1E.toInt())

                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        allowFileAccess = true
                        allowContentAccess = true
                        databaseEnabled = true
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        cacheMode = WebSettings.LOAD_DEFAULT
                        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    }

                    // Use software layer rendering in WebView to prevent Mesa rendernode probe errors in virtualized emulator environments
                    setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)

                    addJavascriptInterface(
                        CodeForgeJsBridge(viewModel) {
                            post {
                                isLoaded = true
                                // If there is an active tab, send its content now
                                val activeTab = viewModel.uiState.value.activeTab
                                if (activeTab != null) {
                                    val safeContent = activeTab.content
                                        .replace("\\", "\\\\")
                                        .replace("`", "\\`")
                                        .replace("\$", "\\\$")
                                        .replace("\r", "")
                                    evaluateJavascript(
                                        "setFileContent(`$safeContent`, '${activeTab.language}', '${activeTab.path}', false);",
                                        null
                                    )
                                }
                            }
                        },
                        "CodeForgeBridge"
                    )

                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("MonacoEditor", "${consoleMessage?.message()} -- From line ${consoleMessage?.lineNumber()} of ${consoleMessage?.sourceId()}")
                            return super.onConsoleMessage(consoleMessage)
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            isLoaded = true
                        }

                        override fun onReceivedError(
                            view: WebView?,
                            errorCode: Int,
                            description: String?,
                            failingUrl: String?
                        ) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            android.util.Log.w("MonacoEditor", "WebView error: $description ($errorCode) at $failingUrl")
                        }

                        override fun onRenderProcessGone(
                            view: WebView?,
                            detail: android.webkit.RenderProcessGoneDetail?
                        ): Boolean {
                            android.util.Log.w("MonacoEditor", "WebView render process gone (crashed: ${detail?.didCrash()})")
                            return true
                        }
                    }

                    loadUrl("file:///android_asset/monaco/index.html")
                }
            },
            update = { webView ->
                webViewRef = webView
            }
        )

        if (!isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(VsCodeBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = VsCodePrimary)
            }
        }
    }
}

class CodeForgeJsBridge(
    private val viewModel: CodeForgeViewModel,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun onEditorReady() {
        onReady()
    }

    @JavascriptInterface
    fun onContentChanged(content: String) {
        viewModel.onEditorContentChanged(content)
    }

    @JavascriptInterface
    fun onCursorPositionChanged(line: Int, column: Int, selectionLength: Int) {
        viewModel.onCursorPositionChanged(line, column, selectionLength)
    }

    @JavascriptInterface
    fun onMarkersChanged(markersJson: String) {
        viewModel.onMarkersChanged(markersJson)
    }
}
