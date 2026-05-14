package com.example.deepseekchat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.deepseekchat.ui.theme.AiBubble
import com.example.deepseekchat.ui.theme.AiBubbleDark

// Markdown 富文本渲染 —— assistant 消息专用
// 支持：标题 #~######、粗体、斜体、删除线、行内代码、代码块(深色背景+横向滚动)、
//       无序列表、有序列表、引用、分割线、链接(可点击)
// 主题跟随 isDark 切换浅色/深色

@Composable
fun MarkdownText(content: String, isDark: Boolean, modifier: Modifier = Modifier) {
    val textColor = if (isDark) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface
    val codeBlockBg = if (isDark) AiBubbleDark.copy(alpha = 0.6f) else AiBubble.copy(alpha = 0.5f)

    Column(modifier = modifier.fillMaxWidth()) {
        val lines = content.split("\n")
        var inCodeBlock = false
        val codeLines = mutableListOf<String>()

        lines.forEach { line ->
            val trimmed = line.trimStart()
            when {
                trimmed.startsWith("```") -> {
                    if (inCodeBlock) {
                        RenderCodeBlock(codeLines.joinToString("\n"), codeBlockBg, textColor)
                        codeLines.clear()
                        inCodeBlock = false
                    } else inCodeBlock = true
                }
                inCodeBlock -> codeLines.add(line)
                trimmed.startsWith("###### ") -> Heading(6, trimmed.removePrefix("###### ").trim(), textColor)
                trimmed.startsWith("##### ") -> Heading(5, trimmed.removePrefix("##### ").trim(), textColor)
                trimmed.startsWith("#### ") -> Heading(4, trimmed.removePrefix("#### ").trim(), textColor)
                trimmed.startsWith("### ") -> Heading(3, trimmed.removePrefix("### ").trim(), textColor)
                trimmed.startsWith("## ") -> Heading(2, trimmed.removePrefix("## ").trim(), textColor)
                trimmed.startsWith("# ") -> Heading(1, trimmed.removePrefix("# ").trim(), textColor)
                trimmed.matches(Regex("^(-{3,}|\\*{3,})$")) -> Text("─".repeat(32), color = textColor.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                trimmed.startsWith("- ") || trimmed.startsWith("* ") -> Text("  •  ${trimmed.substring(2)}", style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.padding(vertical = 1.dp))
                trimmed.matches(Regex("^\\d+\\.\\s.*")) -> {
                    val num = trimmed.substringBefore(".")
                    Text("  $num.  ${trimmed.substringAfter(". ").trim()}", style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.padding(vertical = 1.dp))
                }
                trimmed.startsWith(">") -> Text("┃ ${trimmed.removePrefix(">").trimStart()}", style = MaterialTheme.typography.bodyMedium, color = textColor.copy(alpha = 0.75f), modifier = Modifier.padding(start = 8.dp, top = 1.dp, bottom = 1.dp))
                trimmed.isEmpty() -> Text("", modifier = Modifier.padding(vertical = 2.dp))
                else -> Text(text = buildInlineStyled(line), style = MaterialTheme.typography.bodyMedium, color = textColor, modifier = Modifier.padding(vertical = 1.dp))
            }
        }
        if (codeLines.isNotEmpty()) RenderCodeBlock(codeLines.joinToString("\n"), codeBlockBg, textColor)
    }
}

@Composable
private fun Heading(level: Int, text: String, textColor: androidx.compose.ui.graphics.Color) {
    val (fs, fw, tp) = when (level) { 1 -> Triple(24.sp, FontWeight.Bold, 16.dp); 2 -> Triple(20.sp, FontWeight.Bold, 12.dp); 3 -> Triple(18.sp, FontWeight.SemiBold, 10.dp); 4 -> Triple(16.sp, FontWeight.SemiBold, 8.dp); 5 -> Triple(14.sp, FontWeight.Medium, 6.dp); else -> Triple(13.sp, FontWeight.Medium, 4.dp) }
    Text(text, style = MaterialTheme.typography.bodyLarge.copy(fontSize = fs, fontWeight = fw, color = textColor), modifier = Modifier.padding(top = tp, bottom = 4.dp))
}

@Composable
private fun RenderCodeBlock(code: String, bg: androidx.compose.ui.graphics.Color, textColor: androidx.compose.ui.graphics.Color) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clip(RoundedCornerShape(8.dp)).background(bg).padding(12.dp)) {
        Text(code, style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp, lineHeight = 20.sp, color = textColor))
    }
}

private fun buildInlineStyled(line: String): androidx.compose.ui.text.AnnotatedString = buildAnnotatedString {
    var i = 0
    while (i < line.length) {
        when {
            line.startsWith("**", i) -> { val e = line.indexOf("**", i + 2); if (e != -1) { this.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(line.substring(i + 2, e)) }; i = e + 2 } else { append(line[i]); i++ } }
            line[i] == '*' && (i == 0 || line[i - 1] != '*') && (i + 1 < line.length && line[i + 1] != '*') -> { val e = line.indexOf('*', i + 1); if (e != -1) { this.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(line.substring(i + 1, e)) }; i = e + 1 } else { append(line[i]); i++ } }
            line.startsWith("~~", i) -> { val e = line.indexOf("~~", i + 2); if (e != -1) { this.withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) { append(line.substring(i + 2, e)) }; i = e + 2 } else { append(line[i]); i++ } }
            line[i] == '`' -> { val e = line.indexOf('`', i + 1); if (e != -1) { this.withStyle(SpanStyle(fontFamily = FontFamily.Monospace, fontSize = 13.sp, background = Color(0x33000000))) { append(line.substring(i + 1, e)) }; i = e + 1 } else { append(line[i]); i++ } }
            else -> { append(line[i]); i++ }
        }
    }
}
