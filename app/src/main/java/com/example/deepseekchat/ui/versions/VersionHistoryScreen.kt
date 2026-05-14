package com.example.deepseekchat.ui.versions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.data.local.VersionEntry
import com.example.deepseekchat.data.local.VersionHistoryStore
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryScreen(onNavigateBack: () -> Unit) {
    var versions by remember { mutableStateOf(VersionHistoryStore.getAll()) }
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }

    Scaffold(topBar = { TopAppBar(title = { Text("版本历史", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }, actions = { IconButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, "添加版本") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }) { paddingValues ->
        if (versions.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(80.dp)); Text("暂无版本记录", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("点击右上角 + 添加第一个版本", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp, vertical = 8.dp)) {
                items(versions) { entry ->
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) { Text(entry.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(4.dp)); Text(dateFormat.format(Date(entry.createdAt)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                                IconButton(onClick = { VersionHistoryStore.delete(entry.id); versions = VersionHistoryStore.getAll() }, modifier = Modifier.size(36.dp)) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp)) }
                            }
                            if (entry.description.isNotBlank()) { Spacer(Modifier.height(8.dp)); Text(entry.description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
    if (showAddDialog) {
        var name by remember { mutableStateOf("") }; var desc by remember { mutableStateOf("") }
        AlertDialog(onDismissRequest = { showAddDialog = false }, title = { Text("添加版本记录") }, text = { Column { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("版本名称") }, placeholder = { Text("如：v2.0.0-beta1") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("修改说明（可选）") }, maxLines = 4, modifier = Modifier.fillMaxWidth()) } },
            confirmButton = { Button(onClick = { VersionHistoryStore.add(name.trim(), desc.trim()); versions = VersionHistoryStore.getAll(); showAddDialog = false }, enabled = name.isNotBlank()) { Text("保存") } },
            dismissButton = { TextButton(onClick = { showAddDialog = false }) { Text("取消") } })
    }
}
