package com.example.deepseekchat.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class VersionEntry(
    val id: String,
    val name: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

object VersionHistoryStore {
    private const val PREFS_FILE = "deepseek_versions"
    private const val KEY_VERSIONS = "versions_json"

    private lateinit var prefs: android.content.SharedPreferences
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, VersionEntry::class.java)
    private val listAdapter = moshi.adapter<List<VersionEntry>>(listType)

    private var cachedVersions: List<VersionEntry> = emptyList()

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        cachedVersions = loadFromPrefs()
    }

    fun getAll(): List<VersionEntry> = cachedVersions.sortedByDescending { it.createdAt }

    fun add(name: String, description: String = ""): VersionEntry {
        val entry = VersionEntry(id = UUID.randomUUID().toString(), name = name, description = description)
        cachedVersions = listOf(entry) + cachedVersions
        saveToPrefs()
        return entry
    }

    fun delete(id: String) {
        cachedVersions = cachedVersions.filter { it.id != id }
        saveToPrefs()
    }

    private fun loadFromPrefs(): List<VersionEntry> {
        val json = prefs.getString(KEY_VERSIONS, null) ?: return emptyList()
        return try { listAdapter.fromJson(json) ?: emptyList() } catch (_: Exception) { emptyList() }
    }

    private fun saveToPrefs() {
        prefs.edit().putString(KEY_VERSIONS, listAdapter.toJson(cachedVersions)).apply()
    }
}
