package com.example.deepseekchat.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.data.local.ApiConfig

@Composable
fun ApiCallPanel(configs: List<ApiConfig>, activeId: String, onSelect: (String) -> Unit, onAdd: (String, String, String) -> Unit, onDelete: (String) -> Unit, onDismiss: () -> Unit) {
    var showAddForm by remember { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text("API 调用板", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); IconButton(onClick = { showAddForm = !showAddForm }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Add, "添加配置", tint = MaterialTheme.colorScheme.primary) } } },
        text = {
            if (showAddForm) {
                var name by remember { mutableStateOf("") }; var key by remember { mutableStateOf("") }; var model by remember { mutableStateOf("deepseek-chat") }
                Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("配置名称") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = key, onValueChange = { key = it }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("模型") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp))
                    Row { TextButton(onClick = { showAddForm = false }, modifier = Modifier.weight(1f)) { Text("取消") }; Button(onClick = { onAdd(name.trim(), key.trim(), model.trim()); showAddForm = false }, enabled = name.isNotBlank() && key.isNotBlank(), modifier = Modifier.weight(1f)) { Text("保存") } }
                }
            } else {
                Column { HorizontalDivider(); Spacer(Modifier.height(8.dp))
                    if (configs.isEmpty()) { Text("暂无保存的 API 配置", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); Text("点击 + 添加你的第一个 API 配置", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                    else { LazyColumn(Modifier.height(320.dp)) { items(configs) { c -> Row(Modifier.fillMaxWidth().clickable { onSelect(c.id) }.padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { RadioButton(selected = c.id == activeId, onClick = { onSelect(c.id) }); Spacer(Modifier.width(8.dp)); Column(Modifier.weight(1f)) { Text(c.name, style = MaterialTheme.typography.bodyMedium, fontWeight = if (c.id == activeId) FontWeight.SemiBold else FontWeight.Normal, maxLines = 1, overflow = TextOverflow.Ellipsis); Text(c.model, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }; if (configs.size > 1) { IconButton(onClick = { onDelete(c.id) }, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) } } } } } }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("关闭") } })
}
