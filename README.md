# DeepSeek Chat 🤖

一款基于 DeepSeek API 的 Android 聊天客户端，采用 Jetpack Compose 构建。

![Platform](https://img.shields.io/badge/platform-Android-3DDC84) ![minSdk](https://img.shields.io/badge/minSdk-26-brightgreen) ![targetSdk](https://img.shields.io/badge/targetSdk-34-brightgreen) ![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-purple) ![License](https://img.shields.io/badge/license-MIT-green)

---

## ✨ 功能

| 功能 | 技术方案 |
|------|---------|
| 🌐 **非流式 API** | DeepSeek Chat API，完整响应，零丢字 |
| 📐 **数学公式** | WebView + KaTeX 引擎，支持 $$LaTeX$$ |
| 📊 **表格 / 代码块** | marked.js 渲染，GFM 标准 |
| 📎 **文件附件** | 元宝风格文件卡片，内容送入 API 但对话界面仅显示文件名 |
| 🧠 **思考过程** | DeepSeek-R1 推理过程折叠显示（灰字 + 展开/收起） |
| 💬 **多会话** | 独立对话存储、切换、删除 |
| 🔑 **多 API** | 多组 Key + 模型 + 定价，一键切换 |
| 💰 **费用计算** | 每次调用实时计费，基于 API 返回 token 用量 |
| 🎨 **背景图** | 相册选图，深色/浅色自动适配 |
| 📤 **导出** | Markdown 格式分享 |
| 🔒 **加密** | API Key 使用 AES-256-GCM 硬件加密 |
| 🌙 **深色模式** | 跟随系统 |

## 📱 兼容性

| | |
|---|---|
| **最低** | Android 8.0 (API 26) |
| **目标** | Android 14 (API 34) |
| **兼容** | Android 8 ~ 16 |
| **已验证** | 小米 15、Pad 5、HyperOS 2 |

## 🚀 构建

```bash
git clone https://github.com/SZz-Gx/DeepSeekChat.git
cd DeepSeekChat
# 配置 local.properties: sdk.dir=...
gradlew assembleDebug
# APK → app/build/outputs/apk/debug/app-debug.apk
```

要求: JDK 21、Android SDK 34+。

## 🏗️ 架构

```
ChatScreen → ChatViewModel → ChatRepository → DeepSeek API
                    ↕
         MarkdownText (WebView+KaTeX)
```

- **消息渲染**: 流式等待时纯文本 → 响应完成后 WebView 硬渲染
- **公式**: KaTeX CDN (`katex@0.16.11`)
- **Markdown**: marked.js (`marked@13.0.3`)

## 📜 License

MIT
