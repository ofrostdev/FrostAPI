package com.github.ofrostdev.api.utils.common

import kotlin.math.max

object ProgressBar {
    private fun repeat(symbol: String, count: Int): String {
        val sb = StringBuilder()
        for (i in 0 until count) {
            sb.append(symbol)
        }
        return sb.toString()
    }

    fun createProgressBar(
        current: Double,
        max: Double,
        totalBars: Int,
        symbol: String,
        completedColor: String,
        notCompletedColor: String?
    ): String {
        if (max == 0.0) return completedColor + repeat(symbol, totalBars)

        val percent = current / max
        val progressBars = Math.round(totalBars * percent).toInt()

        val bar = StringBuilder()
        bar.append(completedColor)
        bar.append(repeat(symbol, max(0.0, progressBars.toDouble()).toInt()))

        bar.append(notCompletedColor)
        bar.append(repeat(symbol, max(0.0, (totalBars - progressBars).toDouble()).toInt()))

        return bar.toString()
    }

    fun getPercent(current: Double, max: Double): String {
        if (max == 0.0) return "100%"
        val percent = (current / max) * 100

        return if (percent == percent.toInt().toDouble()) {
            String.format("%d%%", percent.toInt())
        } else {
            String.format("%.1f%%", percent)
        }
    }
}