package com.example.math

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

data class MathResult(
    val value: Double,
    val fraction: Fraction? = null,
    val exactText: String = "",
    val error: String? = null
) {
    val isSuccess: Boolean get() = error == null

    fun formattedDecimal(precision: Int = 8): String {
        if (error != null) return error
        if (value.isNaN()) return "Undefined for real numbers"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Check if value is very close to an integer
        val rounded = Math.round(value)
        if (abs(value - rounded) < 1e-12) {
            return rounded.toString()
        }

        // Check if scientific notation is appropriate (|value| >= 1e12 or 0 < |value| < 1e-6)
        val absVal = abs(value)
        if (absVal != 0.0 && (absVal >= 1e11 || absVal < 1e-5)) {
            return toScientificString(precision)
        }

        return try {
            val bd = BigDecimal(value).setScale(precision, RoundingMode.HALF_UP)
            val str = bd.stripTrailingZeros().toPlainString()
            if (str == "-0") "0" else str
        } catch (_: Exception) {
            val df = DecimalFormat("#.########")
            df.format(value)
        }
    }

    fun toScientificString(precision: Int = 6): String {
        if (value == 0.0) return "0"
        val exponent = floor(log10(abs(value))).toInt()
        val mantissa = value / 10.0.pow(exponent.toDouble())
        val bd = BigDecimal(mantissa).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros()
        val expSuperscript = toSuperscript(exponent)
        return "${bd.toPlainString()} × 10$expSuperscript"
    }

    fun toEngineeringString(precision: Int = 4): String {
        if (value == 0.0) return "0"
        var exp = floor(log10(abs(value))).toInt()
        val rem = exp % 3
        val adjExp = if (rem < 0) exp - (3 + rem) else exp - rem
        val mantissa = value / 10.0.pow(adjExp.toDouble())
        val bd = BigDecimal(mantissa).setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros()
        return "${bd.toPlainString()} × 10${toSuperscript(adjExp)}"
    }

    companion object {
        fun error(msg: String): MathResult = MathResult(Double.NaN, error = msg)

        private fun toSuperscript(number: Int): String {
            val chars = number.toString()
            val sb = StringBuilder()
            for (c in chars) {
                sb.append(when (c) {
                    '-' -> '⁻'
                    '0' -> '⁰'
                    '1' -> '¹'
                    '2' -> '²'
                    '3' -> '³'
                    '4' -> '⁴'
                    '5' -> '⁵'
                    '6' -> '⁶'
                    '7' -> '⁷'
                    '8' -> '⁸'
                    '9' -> '⁹'
                    else -> c
                })
            }
            return sb.toString()
        }
    }
}
