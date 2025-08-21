package com.github.ofrostdev.api.utils.common

import kotlin.math.max
import java.util.concurrent.ThreadLocalRandom

@DslMarker
annotation class MathDSLMarker

class MathUtils private constructor() {
    init {
        throw UnsupportedOperationException()
    }

    companion object {
        private val RANDOM: ThreadLocalRandom = ThreadLocalRandom.current()

        fun randomInt(min: Int, max: Int): Int = RANDOM.nextInt(min, max + 1)
        fun randomDouble(min: Double, max: Double): Double = RANDOM.nextDouble(min, max)
        fun chance(percent: Double): Boolean {
            if (percent <= 0) return false
            if (percent >= 100) return true
            return RANDOM.nextDouble(100.0) < percent
        }

        fun clamp(value: Int, min: Int, max: Int): Int = max(min, kotlin.math.min(max, value))
        fun clamp(value: Double, min: Double, max: Double): Double = max(min, kotlin.math.min(max, value))
        fun weightedRandom(weights: IntArray): Int {
            var total = 0
            for (w in weights) total += w
            val r = randomInt(1, total)
            var cumulative = 0
            for (i in weights.indices) {
                cumulative += weights[i]
                if (r <= cumulative) return i
            }
            return weights.size - 1
        }

        fun normalize(value: Double, min: Double, max: Double): Double {
            if (min == max) return 0.0
            return (value - min) / (max - min)
        }

        fun lerp(start: Double, end: Double, t: Double): Double = start + (end - start) * clamp(t, 0.0, 1.0)
        fun coinFlip(): Boolean = RANDOM.nextBoolean()

        @MathDSLMarker
        class DSL {
            fun randomInt(min: Int, max: Int) = Companion.randomInt(min, max)
            fun randomDouble(min: Double, max: Double) = Companion.randomDouble(min, max)
            fun chance(percent: Double) = Companion.chance(percent)
            fun clamp(value: Int, min: Int, max: Int) = Companion.clamp(value, min, max)
            fun clamp(value: Double, min: Double, max: Double) = Companion.clamp(value, min, max)
            fun weightedRandom(weights: IntArray) = Companion.weightedRandom(weights)
            fun normalize(value: Double, min: Double, max: Double) = Companion.normalize(value, min, max)
            fun lerp(start: Double, end: Double, t: Double) = Companion.lerp(start, end, t)
            fun coinFlip() = Companion.coinFlip()
        }

        fun dsl(block: DSL.() -> Unit): DSL {
            val dsl = DSL()
            dsl.block()
            return dsl
        }
    }
}
