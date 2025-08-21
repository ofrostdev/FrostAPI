package com.github.ofrostdev.api.utils.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

object NumberAPI {
    private val SUFFIXES = arrayOf(
        "", "K", "M", "B", "T", "Q", "QQ", "S", "SS", "OC", "N", "D",
        "UN", "DD", "TR", "QT", "QN", "SD", "SPD", "OD", "ND",
        "VG", "UVG", "DVG", "TVG", "QVG", "QQVG", "SVG", "SSVG", "OVG", "NVG",
        "TG", "UTG", "DTG", "TTG", "QTG", "QQTG", "STG", "SSTG", "OTG", "NTG",
        "QG", "UQG", "DQG", "TQG", "QQG", "QQQG", "SQG", "SSQG", "OQG", "NQG",
        "QuG", "UQuG", "DQuG", "TQuG", "QQuG", "QQQuG", "SuG", "SSuG", "OuG", "NuG",
        "SG", "USG", "DSG", "TSG", "QSG", "QQSG", "SSG", "SSSG", "OSG", "NSG",
        "SeG", "USeG", "DSeG", "TSeG", "QSeG", "QQSeG", "SSeG", "SSSeG", "OSeG", "NSeG",
        "OG", "UOG", "DOG", "TOG", "QOG", "QQOG", "SOG", "SSOG", "OOG", "NOG",
        "NG", "UNG", "DNG", "TNG", "QNG", "QQNG", "SNG", "SSNG", "ONG", "NNG",
        "CT"
    )

    fun format(decimal: BigDecimal): String {
        var dec = decimal
        dec = dec.setScale(0, RoundingMode.DOWN)
        val zeros = dec.precision() - 1

        if (zeros < 3) return dec.setScale(0).toString()

        val k = zeros / 3
        if (k >= SUFFIXES.size) return "999.9CT"

        return dec.movePointLeft(k * 3).setScale(1, RoundingMode.DOWN).toString() + SUFFIXES[k]
    }

    fun format(number: Number): String = format(BigDecimal(number.toString()))

    fun nFormat(amount: Double): String {
        var am = amount
        am = mRound(am)
        return formatValue(am)
    }

    fun mRound(amount: Double): Double {
        val df = DecimalFormat("###.##")
        val formatted = df.format(amount).replace(",", ".")
        return formatted.toDouble()
    }

    fun round(value: Double, places: Int): Double {
        require(places >= 0) { "Casas decimais não podem ser negativas." }
        return BigDecimal(value).setScale(places, RoundingMode.HALF_UP).toDouble()
    }

    fun parse(input: String): BigDecimal {
        var zeros = 3
        for (suffix in SUFFIXES) {
            if (suffix.isNotEmpty() && input.endsWith(suffix)) {
                val numberPart = input.substring(0, input.length - suffix.length)
                try {
                    return BigDecimal(numberPart).movePointRight(zeros)
                } catch (_: NumberFormatException) {}
                zeros += 3
            }
        }
        return try {
            BigDecimal(input)
        } catch (_: NumberFormatException) {
            BigDecimal.ZERO
        }
    }

    fun isInt(input: String): Boolean = try {
        input.toInt()
        true
    } catch (_: NumberFormatException) {
        false
    }

    fun isDouble(input: String): Boolean = try {
        input.toDouble()
        true
    } catch (_: NumberFormatException) {
        false
    }

    private fun formatValue(value: Double): String {
        val isWhole = value == Math.round(value).toDouble()
        val symbols = DecimalFormatSymbols(Locale.ENGLISH)
        symbols.decimalSeparator = '.'
        val pattern = if (isWhole) "###,###" else "###,##0.00"
        return DecimalFormat(pattern, symbols).format(value)
    }
}

class NumberDSL internal constructor() {
    fun format(num: Number) = NumberAPI.format(num)
    fun nFormat(num: Double) = NumberAPI.nFormat(num)
    fun round(num: Double, places: Int) = NumberAPI.round(num, places)
    fun parse(input: String) = NumberAPI.parse(input)
    fun isInt(input: String) = NumberAPI.isInt(input)
    fun isDouble(input: String) = NumberAPI.isDouble(input)
}

fun NumberAPI.dsl(block: NumberDSL.() -> Unit) {
    NumberDSL().apply(block)
}
