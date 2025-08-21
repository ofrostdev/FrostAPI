package com.github.ofrostdev.api.utils.common

object RomanNumber {
    private val SYMBOLS = arrayOf("M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I")
    private val NUMBERS = intArrayOf(1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1)

    fun numural(number: Int): String {
        for (i in NUMBERS.indices) {
            if (number >= NUMBERS[i]) {
                return SYMBOLS[i] + numural(number - NUMBERS[i])
            }
        }
        return ""
    }

    fun unnumural(number: String): Int {
        for (i in SYMBOLS.indices) {
            if (number.startsWith(SYMBOLS[i])) {
                return NUMBERS[i] + unnumural(number.replaceFirst(SYMBOLS[i].toRegex(), ""))
            }
        }
        return 0
    }
}