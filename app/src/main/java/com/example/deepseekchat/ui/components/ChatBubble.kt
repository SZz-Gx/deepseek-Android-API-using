package com.example.deepseekchat.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.ui.theme.AiBubble
import com.example.deepseekchat.ui.theme.AiBubbleDark
import com.example.deepseekchat.ui.theme.AiBubbleText
import com.example.deepseekchat.ui.theme.AiBubbleTextDark
import com.example.deepseekchat.ui.theme.UserBubble
import com.example.deepseekchat.ui.theme.UserBubbleDark
import com.example.deepseekchat.ui.theme.UserBubbleText
import com.example.deepseekchat.ui.theme.UserBubbleTextDark

// 聊天气泡组件：user 右对齐蓝色，assistant 左对齐灰色（含 Markdown），system 居中
// 每个气泡底部有复制 / 删除 / 重发（仅 user 消息）图标按钮

@Composable
fun ChatBubble(
    role: String,
    content: String,
    isDark: Boolean,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    onResend: (() -> Unit)? = null
) {
    when (role) {
        "system" -> {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(text = content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(8.dp))
            }
        }
        else -> {
            val isUser = role == "user"
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = if (isUser) 16.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 16.dp))
                        .background(when { isUser && isDark -> UserBubbleDark; isUser -> UserBubble; !isUser && isDark -> AiBubbleDark; else -> AiBubble })
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column {
                        Text(
                            text = if (isUser) "你" else "DeepSeek",
                            style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                            color = when { isUser && isDark -> UserBubbleTextDark.copy(alpha = 0.7f); isUser -> UserBubbleText.copy(alpha = 0.7f); !isUser && isDark -> AiBubbleTextDark.copy(alpha = 0.7f); else -> AiBubbleText.copy(alpha = 0.7f) }
                        )
                        if (isUser) {
                            Text(text = content, style = MaterialTheme.typography.bodyMedium, color = if (isDark) UserBubbleTextDark else UserBubbleText)
                        } else {
                            MarkdownText(content = content, isDark = isDark)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val iconTint = if (isUser && isDark) UserBubbleTextDark.copy(alpha = 0.45f)
                                else if (isUser) UserBubbleText.copy(alpha = 0.45f)
                                else if (isDark) AiBubbleTextDark.copy(alpha = 0.45f)
                                else AiBubbleText.copy(alpha = 0.45f)
                            val iconSize = 16.dp

                            IconButton(onClick = onCopy, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.ContentCopy, "复制", tint = iconTint, modifier = Modifier.size(iconSize))
                            }
                            if (onResend != null) {
                                Spacer(Modifier.width(2.dp))
                                IconButton(onClick = onResend, modifier = Modifier.size(28.dp)) {
                                    Icon(Icons.Default.Refresh, "重发", tint = iconTint, modifier = Modifier.size(iconSize))
                                }
                            }
                            Spacer(Modifier.width(2.dp))
                            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Delete, "删除", tint = iconTint, modifier = Modifier.size(iconSize))
                            }
                        }
                    }
                }
            }
        }
    }
}
