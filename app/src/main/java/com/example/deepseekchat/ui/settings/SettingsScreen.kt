package com.example.deepseekchat.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.data.local.ApiConfig
import com.example.deepseekchat.data.local.ApiConfigStore
import com.example.deepseekchat.data.local.BackgroundStore
import com.example.deepseekchat.data.local.BalanceManager
import com.example.deepseekchat.data.local.SecurePreferences
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onModelChanged: (String) -> Unit, onSystemPromptChanged: (String) -> Unit,
    onMaxContextRoundsChanged: (Int) -> Unit, onInputPriceMissChanged: (BigDecimal) -> Unit,
    onInputPriceHitChanged: (BigDecimal) -> Unit, onOutputPriceChanged: (BigDecimal) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToVersionHistory: (() -> Unit)? = null, onNavigateToApiConfigs: (() -> Unit)? = null,
    showApiConfigs: Boolean = false
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var apiKey by remember { mutableStateOf(SecurePreferences.apiKey) }
    var model by remember { mutableStateOf("deepseek-chat") }
    var systemPrompt by remember { mutableStateOf("") }
    var maxContextRounds by remember { mutableStateOf("20") }
    var inputPriceMiss by remember { mutableStateOf("1") }
    var inputPriceHit by remember { mutableStateOf("0.02") }
    var outputPrice by remember { mutableStateOf("2") }

    var configs by remember { mutableStateOf(ApiConfigStore.getAll()) }
    var activeConfigId by remember { mutableStateOf(ApiConfigStore.getActiveId()) }
    var showAddConfigDialog by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<ApiConfig?>(null) }

    val bgPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri -> if (uri != null) { BackgroundStore.setBackground(uri); scope.launch { snackbarHostState.showSnackbar("背景图已设置") } } }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(title = { Text(if (showApiConfigs) "API 配置管理" else "设置", fontWeight = FontWeight.SemiBold) }, navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }, actions = { if (showApiConfigs) { IconButton(onClick = { showAddConfigDialog = true }) { Icon(Icons.Default.Add, "添加配置") } } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)) }
    ) { paddingValues ->
        if (showApiConfigs) {
            if (configs.isEmpty()) { Column(Modifier.fillMaxSize().padding(paddingValues).padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) { Spacer(Modifier.height(80.dp)); Icon(Icons.Default.Info, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)); Spacer(Modifier.height(16.dp)); Text("暂无 API 配置", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("点击右上角 + 添加", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) } }
            else { LazyColumn(Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp, vertical = 8.dp)) { items(configs) { c -> ApiConfigCard(c, c.id == activeConfigId, { ApiConfigStore.setActive(c.id); activeConfigId = ApiConfigStore.getActiveId(); configs = ApiConfigStore.getAll(); val a = ApiConfigStore.getActive(); if (a != null) { apiKey = a.apiKey; model = a.model; SecurePreferences.apiKey = a.apiKey; onModelChanged(a.model); onInputPriceMissChanged(a.inputPriceMiss.toBigDecimal()); onInputPriceHitChanged(a.inputPriceHit.toBigDecimal()); onOutputPriceChanged(a.outputPrice.toBigDecimal()) }; scope.launch { snackbarHostState.showSnackbar("已切换到: ${c.name}") } }, { editingConfig = c }, { ApiConfigStore.delete(c.id); configs = ApiConfigStore.getAll(); activeConfigId = ApiConfigStore.getActiveId() }, configs.size > 1); Spacer(Modifier.height(8.dp)) } } }
        } else {
            Column(Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()).padding(16.dp)) {
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("API 配置", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = apiKey, onValueChange = { apiKey = it }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = model, onValueChange = { model = it }, label = { Text("模型") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = systemPrompt, onValueChange = { systemPrompt = it }, label = { Text("系统提示词") }, maxLines = 3, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(12.dp)); OutlinedTextField(value = maxContextRounds, onValueChange = { maxContextRounds = it.filter { c -> c.isDigit() } }, label = { Text("上下文轮数上限") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth()) } }
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("定价配置（元/百万 tokens）", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Text("deepseek-chat 官方定价\n缓存命中 ¥0.02 自 2026/4/26 起生效", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = inputPriceMiss, onValueChange = { inputPriceMiss = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("输入（缓存未命中）") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = inputPriceHit, onValueChange = { inputPriceHit = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("输入（缓存命中）") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = outputPrice, onValueChange = { outputPrice = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("输出") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth()) } }
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Text("平台余额", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp)); Text("累计花费依据 API 返回的真实 token 用量计算。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text("累计花费", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant); Text("¥${BalanceManager.formatAmount(BalanceManager.totalCost)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }; OutlinedButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://platform.deepseek.com/usage"))) }) { Text("查看平台余额 →") } } } }
                Spacer(Modifier.height(16.dp))
                Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Wallpaper, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.width(8.dp)); Text("聊天背景", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) }; Spacer(Modifier.height(8.dp)); Text("从相册选择图片作为聊天背景", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant); Spacer(Modifier.height(12.dp)); Row { OutlinedButton(onClick = { bgPickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) { Icon(Icons.Default.Image, null, Modifier.size(18.dp)); Spacer(Modifier.width(4.dp)); Text("选择图片") }; Spacer(Modifier.width(8.dp)); OutlinedButton(onClick = { BackgroundStore.clearBackground(); scope.launch { snackbarHostState.showSnackbar("背景图已清除") } }, modifier = Modifier.weight(1f), enabled = BackgroundStore.isEnabled) { Text("清除背景") } } } }
                Spacer(Modifier.height(16.dp))
                if (onNavigateToVersionHistory != null) { Card(Modifier.fillMaxWidth()) { Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.History, null, Modifier.size(24.dp), tint = MaterialTheme.colorScheme.primary); Spacer(Modifier.weight(1f)); Text("版本历史", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); OutlinedButton(onClick = onNavigateToVersionHistory) { Text("查看 →") } } }; Spacer(Modifier.height(16.dp)) }
                if (onNavigateToApiConfigs != null) { Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Text("API 配置管理", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold); Spacer(Modifier.weight(1f)); OutlinedButton(onClick = onNavigateToApiConfigs) { Text("管理 →") } }; Spacer(Modifier.height(4.dp)); val a = ApiConfigStore.getActive(); if (a != null) Text("当前: ${a.name} (${a.model})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } }; Spacer(Modifier.height(16.dp)) }
                Button(onClick = { scope.launch { SecurePreferences.apiKey = apiKey; onModelChanged(model); onSystemPromptChanged(systemPrompt); onMaxContextRoundsChanged(maxContextRounds.toIntOrNull() ?: 20); onInputPriceMissChanged(inputPriceMiss.toBigDecimalOrNull() ?: BigDecimal.ONE); onInputPriceHitChanged(inputPriceHit.toBigDecimalOrNull() ?: BigDecimal("0.02")); onOutputPriceChanged(outputPrice.toBigDecimalOrNull() ?: BigDecimal("2")); snackbarHostState.showSnackbar("设置已保存") } }, Modifier.fillMaxWidth()) { Text("保存设置") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { inputPriceMiss = "1"; inputPriceHit = "0.02"; outputPrice = "2"; maxContextRounds = "20"; model = "deepseek-chat"; scope.launch { snackbarHostState.showSnackbar("已恢复默认定价") } }, Modifier.fillMaxWidth()) { Text("恢复默认定价") }
                Spacer(Modifier.height(48.dp))
            }
        }
    }
    if (showAddConfigDialog) {
        var n by remember { mutableStateOf("") }; var k by remember { mutableStateOf("") }; var m by remember { mutableStateOf("deepseek-chat") }
        AlertDialog(onDismissRequest = { showAddConfigDialog = false }, title = { Text("添加 API 配置") }, text = { Column { OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("配置名称") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = k, onValueChange = { k = it }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = m, onValueChange = { m = it }, label = { Text("模型") }, singleLine = true, modifier = Modifier.fillMaxWidth()) } },
            confirmButton = { Button(onClick = { ApiConfigStore.add(n.trim(), k.trim(), m.trim()); configs = ApiConfigStore.getAll(); activeConfigId = ApiConfigStore.getActiveId(); showAddConfigDialog = false; scope.launch { snackbarHostState.showSnackbar("已添加: $n") } }, enabled = n.isNotBlank() && k.isNotBlank()) { Text("保存") } },
            dismissButton = { TextButton(onClick = { showAddConfigDialog = false }) { Text("取消") } })
    }
    editingConfig?.let { c ->
        var n by remember { mutableStateOf(c.name) }; var k by remember { mutableStateOf(c.apiKey) }; var m by remember { mutableStateOf(c.model) }
        AlertDialog(onDismissRequest = { editingConfig = null }, title = { Text("编辑 API 配置") }, text = { Column { OutlinedTextField(value = n, onValueChange = { n = it }, label = { Text("配置名称") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = k, onValueChange = { k = it }, label = { Text("API Key") }, singleLine = true, modifier = Modifier.fillMaxWidth()); Spacer(Modifier.height(8.dp)); OutlinedTextField(value = m, onValueChange = { m = it }, label = { Text("模型") }, singleLine = true, modifier = Modifier.fillMaxWidth()) } },
            confirmButton = { Button(onClick = { ApiConfigStore.update(c.id, name = n.trim(), apiKey = k.trim(), model = m.trim()); configs = ApiConfigStore.getAll(); editingConfig = null; scope.launch { snackbarHostState.showSnackbar("已更新: $n") } }, enabled = n.isNotBlank() && k.isNotBlank()) { Text("保存") } },
            dismissButton = { TextButton(onClick = { editingConfig = null }) { Text("取消") } })
    }
}

@Composable
private fun ApiConfigCard(c: ApiConfig, isActive: Boolean, onActivate: () -> Unit, onEdit: () -> Unit, onDelete: () -> Unit, canDelete: Boolean) {
    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(16.dp)) { Row(verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Row(verticalAlignment = Alignment.CenterVertically) { Text(c.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold); if (isActive) { Spacer(Modifier.padding(start = 8.dp)); Text("当前", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) } }; Spacer(Modifier.height(4.dp)); Text("模型: ${c.model}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis); Text("Key: ${c.apiKey.take(8)}…${c.apiKey.takeLast(4)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }; if (!isActive) OutlinedButton(onClick = onActivate, modifier = Modifier.padding(end = 4.dp)) { Text("切换") }; IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Edit, "编辑", modifier = Modifier.size(18.dp)) }; if (canDelete) IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) { Icon(Icons.Default.Delete, "删除", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(18.dp)) } } } }
}
