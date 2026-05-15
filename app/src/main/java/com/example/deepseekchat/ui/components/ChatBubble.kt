package com.example.deepseekchat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.ui.theme.AiBubble
import com.example.deepseekchat.ui.theme.AiBubbleDark
import com.example.deepseekchat.ui.theme.AiBubbleText
import com.example.deepseekchat.ui.theme.AiBubbleTextDark
import com.example.deepseekchat.ui.theme.UserBubble
import com.example.deepseekchat.ui.theme.UserBubbleDark
import com.example.deepseekchat.ui.theme.UserBubbleText
import com.example.deepseekchat.ui.theme.UserBubbleTextDark

@Composable
fun ChatBubble(
    role: String, content: String, isDark: Boolean,
    onCopy: () -> Unit, onDelete: () -> Unit, onResend: (() -> Unit)? = null,
    isStreaming: Boolean = false
) {
    when (role) {
        "system" -> {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            }
        }
        else -> {
            val isUser = role == "user"
            val hasReasoning = !isUser && content.startsWith("【思考】")
            val reasoningText: String
            val answerText: String
            if (hasReasoning) {
                val parts = content.split("\n【回答】\n", limit = 2)
                reasoningText = parts[0].removePrefix("【思考】")
                answerText = if (parts.size > 1) parts[1] else ""
            } else {
                reasoningText = ""
                answerText = content
            }

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isUser) 16.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 16.dp))
                        .background(when { isUser && isDark -> UserBubbleDark; isUser -> UserBubble; !isUser && isDark -> AiBubbleDark; else -> AiBubble })
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            if (isUser) "你" else "DeepSeek",
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                            color = when { isUser && isDark -> UserBubbleTextDark.copy(alpha = 0.7f); isUser -> UserBubbleText.copy(alpha = 0.7f); !isUser && isDark -> AiBubbleTextDark.copy(alpha = 0.7f); else -> AiBubbleText.copy(alpha = 0.7f) }
                        )

                        // 思考过程
                        if (hasReasoning && reasoningText.isNotBlank()) {
                            var expanded by remember { mutableStateOf(false) }
                            Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, null, Modifier.size(16.dp), tint = if (isDark) AiBubbleTextDark.copy(alpha = 0.7f) else AiBubbleText.copy(alpha = 0.7f))
                                Text(" 思考过程", style = MaterialTheme.typography.labelSmall, color = if (isDark) AiBubbleTextDark.copy(alpha = 0.7f) else AiBubbleText.copy(alpha = 0.7f))
                                Spacer(Modifier.weight(1f))
                                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null, Modifier.size(18.dp), tint = if (isDark) AiBubbleTextDark.copy(alpha = 0.5f) else AiBubbleText.copy(alpha = 0.5f))
                            }
                            AnimatedVisibility(visible = expanded) {
                                Text(reasoningText, style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic), color = (if (isDark) AiBubbleTextDark else AiBubbleText).copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 6.dp))
                            }
                        }

                        // 附件展示（元宝风格文件卡片）
                        val displayContent = if (isUser) answerText else if (answerText.isBlank()) content else answerText
                        val attachFile = displayContent.substringAfter("【附件: ", "").substringBefore("\n")
                        val attachPrefix = "【附件: $attachFile\n"
                        val userOnlyText = if (displayContent.startsWith(attachPrefix)) {
                            displayContent.substringAfter("\n---\n")
                        } else displayContent

                        if (attachFile.isNotBlank()) {
                            Row(Modifier.fillMaxWidth().padding(bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDark) androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AttachFile, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(attachFile, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }

                        // 回答内容
                        if (isUser) {
                            Text(userOnlyText, style = MaterialTheme.typography.bodyMedium, color = if (isDark) UserBubbleTextDark else UserBubbleText)
                        } else if (isStreaming) {
                            Text(userOnlyText, style = MaterialTheme.typography.bodyMedium, color = if (isDark) AiBubbleTextDark else AiBubbleText)
                        } else {
                            MarkdownText(userOnlyText, isDark)
                        }

                        // 操作按钮（流式时不显示）
                        if (!isStreaming) {
                            val iconTint = if (isUser && isDark) UserBubbleTextDark.copy(alpha = 0.45f)
                                else if (isUser) UserBubbleText.copy(alpha = 0.45f)
                                else if (isDark) AiBubbleTextDark.copy(alpha = 0.45f)
                                else AiBubbleText.copy(alpha = 0.45f)
                            val iconSize = 16.dp
                            Row(Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.ContentCopy, "复制", tint = iconTint, modifier = Modifier.size(iconSize)) }
                                if (onResend != null) { Spacer(Modifier.width(2.dp)); IconButton(onClick = onResend, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Refresh, "重发", tint = iconTint, modifier = Modifier.size(iconSize)) } }
                                Spacer(Modifier.width(2.dp))
                                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.Delete, "删除", tint = iconTint, modifier = Modifier.size(iconSize)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
