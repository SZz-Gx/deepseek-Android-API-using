package com.example.deepseekchat.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.data.api.Usage
import com.example.deepseekchat.data.local.BalanceManager
import com.example.deepseekchat.ui.theme.TokenBarBackground
import com.example.deepseekchat.ui.theme.TokenBarBackgroundDark
import java.math.BigDecimal

// Token 用量与费用展示条（可折叠）
// "查看平台余额 →" 跳转至 DeepSeek 控制台查询真实余额

@Composable
fun TokenUsageBar(usage: Usage?, lastCost: BigDecimal, isDark: Boolean) {
    var expanded by remember { mutableStateOf(false) }
    val totalCost = BalanceManager.totalCost
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)).background(if (isDark) TokenBarBackgroundDark else TokenBarBackground)) {
        Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Token 用量", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (usage != null) Text(" · 本轮 ${usage.totalTokens} tokens", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("累计 ¥${BalanceManager.formatAmount(totalCost)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp))
            }
        }
        AnimatedVisibility(visible = expanded, enter = expandVertically(), exit = shrinkVertically()) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 8.dp)) {
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                if (usage != null) {
                    DetailRow("缓存命中", "${usage.promptCacheHitTokens} tokens")
                    DetailRow("缓存未命中", "${usage.promptCacheMissTokens} tokens")
                    DetailRow("输出", "${usage.completionTokens} tokens")
                    DetailRow("总计", "${usage.totalTokens} tokens")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                DetailRow("本轮花费", "¥${BalanceManager.formatAmount(lastCost)}", highlight = true)
                DetailRow("累计花费", "¥${BalanceManager.formatAmount(totalCost)}")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "查看平台余额 →",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.deepseek.com/usage"))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, highlight: Boolean = false, color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = if (highlight) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium) else MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = if (highlight) MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold) else MaterialTheme.typography.bodySmall, color = color)
    }
}
