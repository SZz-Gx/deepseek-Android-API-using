package com.example.deepseekchat.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class ApiConfig(
    val id: String,
    val name: String,
    val apiKey: String,
    val model: String = "deepseek-chat",
    val inputPriceMiss: String = "1",
    val inputPriceHit: String = "0.02",
    val outputPrice: String = "2",
    val createdAt: Long = System.currentTimeMillis()
)

object ApiConfigStore {
    private const val PREFS_FILE = "deepseek_api_configs"
    private const val KEY_CONFIGS = "configs_json"
    private const val KEY_ACTIVE = "active_config_id"

    private lateinit var prefs: android.content.SharedPreferences
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ApiConfig::class.java)
    private val listAdapter = moshi.adapter<List<ApiConfig>>(listType)

    private var cachedConfigs: List<ApiConfig> = emptyList()
    private var activeConfigId: String = ""

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        cachedConfigs = loadFromPrefs()
        activeConfigId = prefs.getString(KEY_ACTIVE, "") ?: ""

        try {
            if (cachedConfigs.isEmpty()) {
                val oldKey = SecurePreferences.apiKey
                if (oldKey.isNotBlank()) {
                    val config = ApiConfig(
                        id = UUID.randomUUID().toString(),
                        name = "默认配置",
                        apiKey = oldKey
                    )
                    cachedConfigs = listOf(config)
                    activeConfigId = config.id
                    saveToPrefs()
                }
            }
        } catch (_: Exception) {
            cachedConfigs = emptyList()
        }
    }

    fun getAll(): List<ApiConfig> = cachedConfigs
    fun getActive(): ApiConfig? = cachedConfigs.find { it.id == activeConfigId }
    fun getActiveId(): String = activeConfigId

    fun setActive(id: String) {
        if (cachedConfigs.any { it.id == id }) {
            activeConfigId = id
            prefs.edit().putString(KEY_ACTIVE, id).apply()
            getActive()?.let { SecurePreferences.apiKey = it.apiKey }
        }
    }

    fun add(name: String, apiKey: String, model: String = "deepseek-chat"): ApiConfig {
        val config = ApiConfig(id = UUID.randomUUID().toString(), name = name, apiKey = apiKey, model = model)
        cachedConfigs = listOf(config) + cachedConfigs
        saveToPrefs()
        return config
    }

    fun delete(id: String) {
        cachedConfigs = cachedConfigs.filter { it.id != id }
        if (activeConfigId == id) {
            activeConfigId = cachedConfigs.firstOrNull()?.id ?: ""
            prefs.edit().putString(KEY_ACTIVE, activeConfigId).apply()
            getActive()?.let { SecurePreferences.apiKey = it.apiKey }
        }
        saveToPrefs()
    }

    fun update(id: String, name: String? = null, apiKey: String? = null, model: String? = null) {
        cachedConfigs = cachedConfigs.map {
            if (it.id == id) it.copy(
                name = name ?: it.name,
                apiKey = apiKey ?: it.apiKey,
                model = model ?: it.model
            ) else it
        }
        saveToPrefs()
        if (id == activeConfigId) {
            getActive()?.let { SecurePreferences.apiKey = it.apiKey }
        }
    }

    private fun loadFromPrefs(): List<ApiConfig> {
        val json = prefs.getString(KEY_CONFIGS, null) ?: return emptyList()
        return try { listAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    private fun saveToPrefs() {
        prefs.edit().putString(KEY_CONFIGS, listAdapter.toJson(cachedConfigs)).apply()
    }
}
