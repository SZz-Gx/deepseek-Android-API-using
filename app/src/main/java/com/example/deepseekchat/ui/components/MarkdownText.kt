package com.example.deepseekchat.ui.components

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MarkdownText(content: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val bg = if (isDark) "#1E1E1E" else "#FFFFFF"
    val tc = if (isDark) "#E0E0E0" else "#2D2D2D"
    val cb = if (isDark) "#2D2D2D" else "#F5F5F5"
    val ic = if (isDark) "#333" else "#EEE"
    val tb = if (isDark) "#555" else "#CCC"
    val th = if (isDark) "#333" else "#F0F0F0"

    // 安全转义：反引号和反斜杠，保留 $ 和换行给 KaTeX
    val safe = content
        .replace("\\", "\\\\")
        .replace("`", "\\`")
        .replace("\r", "")

    val html = remember(content, isDark) {
        """<!DOCTYPE html><html><head><meta name='viewport' content='width=device-width,initial-scale=1'>
<link rel='stylesheet' href='https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.css'>
<style>
*{margin:0;padding:0}body{font:14px/1.7 sans-serif;color:$tc;background:$bg;padding:6px 2px;-webkit-user-select:text;user-select:text;word-wrap:break-word}
pre{background:$cb;padding:10px;border-radius:6px;overflow-x:auto}
code{font:13px monospace;background:$ic;padding:2px 5px;border-radius:3px}
pre code{background:transparent;padding:0}
table{border-collapse:collapse;width:100%;margin:8px 0}
th,td{border:1px solid $tb;padding:5px 8px;text-align:left}th{background:$th}
blockquote{border-left:3px solid $tb;margin:8px 0;padding:4px 12px;color:$tc;opacity:0.7}
a{color:#4D6BFE}img{max-width:100%}
</style></head><body><div id='md'></div>
<script>
const m=document.getElementById('md');
m.textContent=`$safe`;
</script>
<script src='https://cdn.jsdelivr.net/npm/marked@13.0.3/marked.min.js'></script>
<script src='https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/katex.min.js'></script>
<script src='https://cdn.jsdelivr.net/npm/katex@0.16.11/dist/contrib/auto-render.min.js'></script>
<script>
marked.setOptions({breaks:true,gfm:true});
m.innerHTML=marked.parse(m.textContent);
renderMathInElement(m,{delimiters:[{left:'\$\$',right:'\$\$',display:true},{left:'\$',right:'\$',display:false}]});
</script></body></html>"""
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                settings.setSupportZoom(false)
                webViewClient = WebViewClient()
                isVerticalScrollBarEnabled = false
            }
        },
        update = { it.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null) },
        modifier = modifier
    )
}
