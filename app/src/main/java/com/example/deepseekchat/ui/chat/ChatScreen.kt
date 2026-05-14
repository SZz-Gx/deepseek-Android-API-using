package com.example.deepseekchat.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Api
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.deepseekchat.data.local.ApiConfigStore
import com.example.deepseekchat.data.local.BackgroundStore
import com.example.deepseekchat.ui.components.ApiCallPanel
import com.example.deepseekchat.ui.components.ChatBubble
import com.example.deepseekchat.ui.components.SessionPicker
import com.example.deepseekchat.ui.components.TokenUsageBar
import com.example.deepseekchat.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, onNavigateToSettings: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val lastUsage by viewModel.lastUsage.collectAsState()
    val lastCost by viewModel.lastCost.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var showClearDialog by remember { mutableStateOf(false) }
    var showSessionPicker by remember { mutableStateOf(false) }
    var showApiPanel by remember { mutableStateOf(false) }
    var showImageUnsupported by remember { mutableStateOf(false) }

    // 背景图
    var bgUri by remember { mutableStateOf(BackgroundStore.backgroundUri) }
    var bgEnabled by remember { mutableStateOf(BackgroundStore.isEnabled) }
    var bgBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(bgUri, bgEnabled) {
        if (bgEnabled && bgUri.isNotBlank()) {
            try {
                val bmp = withContext(Dispatchers.IO) {
                    val uri = Uri.parse(bgUri)
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                }
                bgBitmap = bmp
            } catch (_: Exception) { bgBitmap = null }
        } else { bgBitmap = null }
    }

    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { BufferedReader(InputStreamReader(it)).readText() } ?: ""
                }
                if (text.isNotBlank()) {
                    inputText = inputText.ifEmpty { "" } + text
                    snackbarHostState.showSnackbar("已导入: ${getFileName(context, uri)}")
                }
            } catch (e: Exception) { snackbarHostState.showSnackbar("读取失败: ${e.message}") }
        }
    }

    LaunchedEffect(Unit) { viewModel.snackbar.collect { snackbarHostState.showSnackbar(it) } }
    LaunchedEffect(Unit) { NetworkUtils.observeNetworkState(context).collect { isOnline -> if (!isOnline) snackbarHostState.showSnackbar("网络连接已断开") } }
    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }
    val lastContent = messages.lastOrNull()?.content ?: ""
    LaunchedEffect(lastContent) { if (isLoading && messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    if (showClearDialog) {
        AlertDialog(onDismissRequest = { showClearDialog = false }, title = { Text("清空对话") }, text = { Text("确定要清空全部对话历史吗？费用数据将保留。") },
            confirmButton = { TextButton(onClick = { viewModel.clearChat(); showClearDialog = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showClearDialog = false }) { Text("取消") } })
    }
    if (showSessionPicker) {
        SessionPicker(sessions = sessions, currentSessionId = viewModel.currentSessionIdProp,
            onSelect = { viewModel.switchToSession(it); showSessionPicker = false },
            onDelete = { viewModel.deleteSession(it) }, onNew = { viewModel.newSession(); showSessionPicker = false },
            onDismiss = { showSessionPicker = false })
    }
    if (showApiPanel) {
        val configs = ApiConfigStore.getAll()
        val activeId = ApiConfigStore.getActiveId()
        ApiCallPanel(configs = configs, activeId = activeId,
            onSelect = { id -> ApiConfigStore.setActive(id); val a = ApiConfigStore.getActive(); if (a != null) { viewModel.model = a.model; viewModel.inputPriceMiss = a.inputPriceMiss.toBigDecimal(); viewModel.inputPriceHit = a.inputPriceHit.toBigDecimal(); viewModel.outputPrice = a.outputPrice.toBigDecimal() }; showApiPanel = false; scope.launch { snackbarHostState.showSnackbar("已切换") } },
            onAdd = { n, k, m -> ApiConfigStore.add(n, k, m); scope.launch { snackbarHostState.showSnackbar("已添加: $n") } },
            onDelete = { ApiConfigStore.delete(it); scope.launch { snackbarHostState.showSnackbar("已删除") } },
            onDismiss = { showApiPanel = false })
    }
    if (showImageUnsupported) {
        AlertDialog(onDismissRequest = { showImageUnsupported = false }, title = { Text("暂不支持") },
            text = { Text("DeepSeek 多模态功能尚未推出，图片识别将在未来版本中支持。") },
            confirmButton = { TextButton(onClick = { showImageUnsupported = false }) { Text("知道了") } })
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { TopAppBar(
            title = { Text(viewModel.currentSessionTitle, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            actions = {
                IconButton(onClick = { showApiPanel = true }) { Icon(Icons.Default.Api, "API 调用板") }
                IconButton(onClick = { showSessionPicker = true }) { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "会话列表") }
                IconButton(onClick = {
                    val md = viewModel.exportToMarkdown()
                    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, md); putExtra(Intent.EXTRA_SUBJECT, viewModel.currentSessionTitle) }, "导出对话"))
                }) { Icon(Icons.Default.Share, "导出") }
                IconButton(onClick = { showClearDialog = true }) { Icon(Icons.Default.Clear, "清空对话") }
                IconButton(onClick = onNavigateToSettings) { Icon(Icons.Default.Settings, "设置") }
            }) },
        bottomBar = {
            Column(Modifier.imePadding()) {
                TokenUsageBar(usage = lastUsage, lastCost = lastCost, isDark = isDark)
                Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.Bottom) {
                    IconButton(onClick = { showImageUnsupported = true }, modifier = Modifier.size(40.dp), enabled = !isLoading) { Icon(Icons.Default.Image, "图片", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    IconButton(onClick = { filePickerLauncher.launch(arrayOf("text/*", "*/*")) }, modifier = Modifier.size(40.dp), enabled = !isLoading) { Icon(Icons.Default.AttachFile, "导入文件", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp)) }
                    OutlinedTextField(value = inputText, onValueChange = { inputText = it }, modifier = Modifier.weight(1f), placeholder = { Text("输入消息…") }, maxLines = 4, shape = RoundedCornerShape(20.dp), enabled = !isLoading)
                    Spacer(Modifier.width(4.dp))
                    if (isLoading) CircularProgressIndicator(Modifier.size(48.dp).padding(8.dp), strokeWidth = 2.dp)
                    else IconButton(onClick = { if (inputText.isNotBlank()) { viewModel.sendMessage(inputText); inputText = "" } }, modifier = Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Filled.Send, "发送", tint = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    ) { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            val bmp = bgBitmap
            if (bmp != null && bgEnabled) {
                androidx.compose.foundation.Image(painter = BitmapPainter(bmp.asImageBitmap()), contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = if (isDark) 0.25f else 0.12f)
            }
            if (messages.isEmpty() && !isLoading) {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AutoAwesome, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        Spacer(Modifier.height(20.dp))
                        Text("开始与 DeepSeek 对话", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                        Spacer(Modifier.height(8.dp))
                        Text("API Key 需在设置中配置", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                        val ac = ApiConfigStore.getActive()
                        if (ac != null) { Spacer(Modifier.height(12.dp)); Text("当前 API: ${ac.name} (${ac.model})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)) }
                    }
                }
            } else {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    itemsIndexed(messages) { index, message ->
                        ChatBubble(role = message.role, content = message.content, isDark = isDark,
                            onCopy = { (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText("message", message.content)) },
                            onDelete = { viewModel.deleteRound(index) },
                            onResend = if (message.role == "user") { { viewModel.resendLast() } } else null)
                    }
                    if (isLoading) {
                        item { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { Row(verticalAlignment = Alignment.CenterVertically) { CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("DeepSeek 正在思考…", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } } }
                    }
                }
            }
        }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "未知文件"
    try { context.contentResolver.query(uri, null, null, null, null)?.use { val i = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME); if (i >= 0 && it.moveToFirst()) name = it.getString(i) } } catch (_: Exception) {}
    return name
}
