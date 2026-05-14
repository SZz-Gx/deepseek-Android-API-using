# DeepSeek Chat ProGuard Rules

# --- API 数据类（Moshi codegen + KotlinJsonAdapterFactory 反射） ---
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keep class com.example.deepseekchat.data.api.** { *; }
-keepclassmembers class com.example.deepseekchat.data.api.** { <fields>; }
-keep class com.example.deepseekchat.data.local.ChatSession { *; }
-keepclassmembers class com.example.deepseekchat.data.local.ChatSession { <fields>; }

# Kotlin 反射元数据（ChatHistoryStore 使用 Moshi KotlinJsonAdapterFactory 需要）
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata { *; }

# Moshi adapter 工厂
-keep class com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }
