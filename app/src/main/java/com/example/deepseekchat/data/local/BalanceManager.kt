package com.example.deepseekchat.data.local

import android.content.Context
import java.math.BigDecimal
import java.math.RoundingMode

// 累计花费追踪（基于 API 返回的真实 token 用量计算）
// 不再维护本地伪余额 —— 用户通过 "查看平台余额" 链接跳转 DeepSeek 控制台

object BalanceManager {
    private const val PREFS_FILE = "deepseek_balance_prefs"
    private const val KEY_TOTAL_COST = "total_cost"

    private lateinit var prefs: android.content.SharedPreferences
    private var cachedTotalCost: BigDecimal = BigDecimal.ZERO

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        cachedTotalCost = BigDecimal(prefs.getString(KEY_TOTAL_COST, "0") ?: "0")
    }

    val totalCost: BigDecimal get() = cachedTotalCost

    fun addCost(cost: BigDecimal) {
        if (cost <= BigDecimal.ZERO) return
        synchronized(this) {
            cachedTotalCost += cost
            prefs.edit().putString(KEY_TOTAL_COST, cachedTotalCost.toPlainString()).apply()
        }
    }

    fun formatAmount(amount: BigDecimal): String {
        return amount.setScale(4, RoundingMode.HALF_UP).toPlainString()
    }
}
