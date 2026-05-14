# DeepSeek Chat 🤖

一款基于 DeepSeek API 的 Android 聊天客户端，采用 Jetpack Compose 构建。

![Platform](https://img.shields.io/badge/platform-Android-3DDC84?logo=android)
![Min SDK](https://img.shields.io/badge/minSdk-26-brightgreen)
![Target SDK](https://img.shields.io/badge/targetSdk-34-brightgreen)
![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-7F52FF?logo=kotlin)
![Compose BOM](https://img.shields.io/badge/Compose_BOM-2024.12.01-blue)
![License](https://img.shields.io/badge/license-MIT-green)

---

## ✨ 功能特性

| 功能                | 说明                                                        |
| ------------------- | ----------------------------------------------------------- |
| 💬 **流式对话**      | SSE 实时流式响应，逐字显示 AI 回复                          |
| 📜 **Markdown 渲染** | 支持标题、粗体、斜体、代码块、列表、引用等                  |
| 📂 **多会话管理**    | 多个对话独立存储，可切换、删除、重命名                      |
| 📎 **文件导入**      | 导入 .txt / .md / .json 等文本文件到输入框                  |
| 📤 **导出对话**      | 一键导出为 Markdown 格式分享                                |
| 💰 **费用计算器**    | 基于 DeepSeek 官方定价公式实时计算每次对话花费              |
| 🎨 **聊天背景**      | 支持从相册自定义聊天背景图片                                |
| 🔑 **多 API 配置**   | 保存多组 API Key，一键切换（工作/个人账号）                 |
| 📋 **版本历史**      | 自定义版本名称存档，记录每次更新                            |
| 🖼️ **图片预留**      | 图片按钮已就位（DeepSeek 多模态推出后即可用）               |
| 🌙 **深色模式**      | 跟随系统主题自动切换                                        |
| 🔒 **加密存储**      | API Key 使用 AES-256-GCM 加密，密钥由 Android Keystore 保管 |

---

## 📱 兼容性

| 项目                  | 支持版本                                                     |
| --------------------- | ------------------------------------------------------------ |
| **最低 Android 版本** | **Android 8.0 (API 26)**                                     |
| **目标 Android 版本** | Android 14 (API 34)                                          |
| **兼容版本**          | Android 8.0 ~ Android 16（含 15、16）                        |
| **已验证机型**        | 小米 15 (Android 16 / HyperOS 2.0)、小米 Pad 5               |
| **国内 ROM**          | 小米 HyperOS、华为 HarmonyOS、OPPO ColorOS、vivo OriginOS、荣耀 MagicOS 等均兼容 |

---

## 🚀 快速开始

### 1. 获取 API Key

1. 前往 [DeepSeek Platform](https://platform.deepseek.com/) 注册账号
2. 在 API Keys 页面创建新的 API Key
3. 复制并保存好你的 Key

### 2. 下载安装

从 [Releases](../../releases) 页面下载最新 APK，或自行构建。

### 3. 配置

1. 打开应用，点击右上角 ⚙️ 进入设置
2. 填写 API Key（格式 `sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`）
3. 默认模型为 `deepseek-chat`，可按需修改
4. 点击「保存设置」即可开始对话

> **注意**：API Key 加密存储在本地，不会上传到任何服务器。

---

## 🔧 自行构建

### 环境要求

| 工具           | 版本                      |
| -------------- | ------------------------- |
| JDK            | 21 或 17                  |
| Android Studio | Ladybug (2024.3+) 或更高  |
| Android SDK    | API 34 + 36.x build-tools |
| Gradle         | 9.0.0 (Wrapper 自动下载)  |

### 构建步骤

```bash
# 1. 克隆仓库
git clone https://github.com/你的用户名/DeepSeekChat.git
cd DeepSeekChat

# 2. 确保 local.properties 配置正确
# sdk.dir=C\:\\Users\\你的用户名\\AppData\\Local\\Android\\Sdk

# 3. 构建 Debug APK
gradlew.bat assembleDebug

# 4. APK 输出路径
# app/build/outputs/apk/debug/app-debug.apk

# 5. 安装到设备
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 📁 项目结构

```
DeepSeekChat/
├── app/
│   └── src/main/java/com/example/deepseekchat/
│       ├── data/
│       │   ├── api/                    ← 网络层 (Retrofit + Moshi)
│       │   │   ├── DeepSeekApiService.kt
│       │   │   ├── Models.kt
│       │   │   └── RetrofitClient.kt
│       │   ├── local/                  ← 本地存储
│       │   │   ├── SecurePreferences.kt  (加密 API Key)
│       │   │   ├── SessionManager.kt     (会话管理)
│       │   │   ├── ChatHistoryStore.kt   (消息历史)
│       │   │   ├── BalanceManager.kt     (余额记录)
│       │   │   ├── ApiConfigStore.kt     (多 API 配置)
│       │   │   ├── VersionHistoryStore.kt (版本历史)
│       │   │   └── BackgroundStore.kt    (背景图片)
│       │   └── repository/
│       │       └── ChatRepository.kt    ← 核心：SSE 流式解析
│       ├── ui/
│       │   ├── chat/
│       │   │   ├── ChatScreen.kt        ← 主聊天界面
│       │   │   └── ChatViewModel.kt     ← 聊天逻辑
│       │   ├── settings/
│       │   │   └── SettingsScreen.kt    ← 设置界面
│       │   ├── components/
│       │   │   ├── ChatBubble.kt        ← 聊天气泡
│       │   │   ├── MarkdownText.kt      ← Markdown 渲染
│       │   │   ├── SessionPicker.kt     ← 会话切换弹窗
│       │   │   ├── TokenUsageBar.kt     ← Token 费用条
│       │   │   └── ApiCallPanel.kt      ← API 调用板
│       │   └── theme/
│       │       └── Theme.kt, Color.kt, Type.kt
│       └── utils/
│           ├── CostCalculator.kt        ← 计费公式
│           └── NetworkUtils.kt          ← 网络监测
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── libs.versions.toml               ← 版本目录
```

---

## 🧰 技术栈

| 组件         | 选型                                                  |
| ------------ | ----------------------------------------------------- |
| **UI**       | Jetpack Compose + Material 3                          |
| **导航**     | Navigation Compose                                    |
| **网络**     | Retrofit 2 + OkHttp 4 + Moshi                         |
| **流式解析** | OkHttp 直接请求，手动解析 SSE                         |
| **加密存储** | AndroidX Security Crypto (EncryptedSharedPreferences) |
| **代码生成** | KSP + Moshi Codegen                                   |
| **构建**     | Gradle KTS + Version Catalog                          |
| **最低兼容** | JDK 17+                                               |

---

## 💡 常见问题

<details>
<summary><b>应用闪退或无法打开</b></summary>


1. 确保 Android 版本 ≥ 8.0 (API 26)
2. 若从旧版本升级，建议先卸载再安装
3. 小米 / HyperOS 用户请确认已关闭「安全守护」或允许安装未知来源应用
   </details>

<details>
<summary><b>API Key 安全吗？</b></summary>


API Key 使用 `EncryptedSharedPreferences` 存储在本地，
加密密钥由 Android Keystore 硬件保管，应用不会将 Key 上传到任何服务器。
</details>

<details>
<summary><b>费用怎么计算？</b></summary>


应用根据 DeepSeek 官方定价公式实时计算每次对话的 token 费用，
支持自定义输入/输出单价。可在设置中调整。
</details>

<details>
<summary><b>如何导入文件？</b></summary>


在聊天输入框左侧点击 📎 文件图标，选择 .txt / .md / .json 等文件，
内容会自动填入输入框。
</details>

---

## 📜 许可证

[MIT License](LICENSE)

```
MIT License

Copyright (c) 2026 DeepSeekChat

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
```

---

## 🙏 致谢

- [DeepSeek](https://deepseek.com/) — 提供强大的 AI API
- [Jetpack Compose](https://developer.android.com/jetpack/compose) — 声明式 UI 框架
- [Square](https://square.github.io/) — Retrofit / OkHttp / Moshi 等优秀库
