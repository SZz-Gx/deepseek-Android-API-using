package com.example.deepseekchat.utils

import com.example.deepseekchat.data.api.Usage
import java.math.BigDecimal
import java.math.RoundingMode

// DeepSeek 官方计费公式
// 输入（缓存命中）: ¥0.02/百万tokens
// 输入（缓存未命中）: ¥1/百万tokens
// 输出: ¥2/百万tokens
// 定价依据: https://api-docs.deepseek.com/zh-cn/quick_start/pricing

object CostCalculator {
    private val ONE_MILLION = BigDecimal("1000000")

    fun calculate(
        usage: Usage,
        inputPriceMiss: BigDecimal = BigDecimal("1"),
        inputPriceHit: BigDecimal = BigDecimal("0.02"),
        outputPrice: BigDecimal = BigDecimal("2")
    ): BigDecimal {
        val missCost = BigDecimal(usage.promptCacheMissTokens)
            .divide(ONE_MILLION, 10, RoundingMode.HALF_UP)
            .multiply(inputPriceMiss)
        val hitCost = BigDecimal(usage.promptCacheHitTokens)
            .divide(ONE_MILLION, 10, RoundingMode.HALF_UP)
            .multiply(inputPriceHit)
        val outputCost = BigDecimal(usage.completionTokens)
            .divide(ONE_MILLION, 10, RoundingMode.HALF_UP)
            .multiply(outputPrice)
        return (missCost + hitCost + outputCost).setScale(4, RoundingMode.HALF_UP)
    }

    fun estimate(
        promptTokens: Int,
        completionTokens: Int,
        inputPriceMiss: BigDecimal = BigDecimal("1"),
        outputPrice: BigDecimal = BigDecimal("2")
    ): BigDecimal {
        val inputCost = BigDecimal(promptTokens)
            .divide(ONE_MILLION, 10, RoundingMode.HALF_UP)
            .multiply(inputPriceMiss)
        val outputCost = BigDecimal(completionTokens)
            .divide(ONE_MILLION, 10, RoundingMode.HALF_UP)
            .multiply(outputPrice)
        return (inputCost + outputCost).setScale(4, RoundingMode.HALF_UP)
    }
}
