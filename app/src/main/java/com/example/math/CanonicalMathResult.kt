package com.example.math

import java.math.BigInteger
import kotlin.math.abs

/**
 * Canonical single source of truth representation for mathematical results in CALCX.
 * Prevents duplicated UI rendering by packaging exact, decimal, unit, and LaTeX representations
 * into one authoritative structure.
 */
data class CanonicalMathResult(
    val latex: String,
    val displayString: String,
    val decimalValue: Double? = null,
    val decimalString: String? = null,
    val exactString: String? = null,
    val unit: String? = null,
    val isExact: Boolean = true,
    val steps: List<String> = emptyList(),
    val isError: Boolean = false,
    val errorMessage: String? = null
) {
    /**
     * Primary display text for the single rendered result.
     * Guaranteed never to produce duplicate parallel outputs.
     */
    val primaryDisplay: String
        get() {
            if (isError) return errorMessage ?: "Error"
            return if (isExact && !exactString.isNullOrEmpty()) {
                if (unit != null) "$exactString $unit" else exactString
            } else if (!decimalString.isNullOrEmpty()) {
                if (unit != null) "$decimalString $unit" else decimalString
            } else {
                if (unit != null) "$displayString $unit" else displayString
            }
        }

    /**
     * Secondary representation, shown ONLY when explicitly relevant (e.g. user in Exact mode can see ≈ decimal).
     */
    val secondaryDisplay: String?
        get() {
            if (isError) return null
            if (isExact && !exactString.isNullOrEmpty() && !decimalString.isNullOrEmpty() && exactString != decimalString) {
                return "≈ $decimalString${if (unit != null) " $unit" else ""}"
            }
            if (!isExact && !exactString.isNullOrEmpty() && exactString != decimalString) {
                return "Exact: $exactString${if (unit != null) " $unit" else ""}"
            }
            return null
        }

    companion object {
        fun fromValue(value: Double, fraction: Fraction? = null, unit: String? = null): CanonicalMathResult {
            if (value.isNaN()) return error("Undefined")
            if (value.isInfinite()) return error(if (value > 0) "Infinity" else "-Infinity")

            val rounded = Math.round(value)
            val isInt = abs(value - rounded) < 1e-12
            val decStr = if (isInt) rounded.toString() else String.format(java.util.Locale.US, "%.8f", value).trimEnd('0').trimEnd('.')

            val exactStr = when {
                fraction != null && fraction.denominator != BigInteger.ONE -> fraction.toProperString()
                isInt -> rounded.toString()
                else -> null
            }

            val latex = when {
                fraction != null && fraction.denominator != BigInteger.ONE -> {
                    val simp = fraction.simplified()
                    val sign = if (simp.numerator < BigInteger.ZERO) "-" else ""
                    val absNum = simp.numerator.abs()
                    "$sign\\frac{$absNum}{${simp.denominator}}"
                }
                isInt -> rounded.toString()
                else -> decStr
            }

            val display = exactStr ?: decStr

            return CanonicalMathResult(
                latex = if (unit != null) "$latex\\text{ $unit}" else latex,
                displayString = display,
                decimalValue = value,
                decimalString = decStr,
                exactString = exactStr,
                unit = unit,
                isExact = exactStr != null
            )
        }

        fun fromString(text: String, unit: String? = null): CanonicalMathResult {
            return CanonicalMathResult(
                latex = text,
                displayString = text,
                decimalString = text,
                unit = unit,
                isExact = false
            )
        }

        fun error(msg: String): CanonicalMathResult {
            return CanonicalMathResult(
                latex = "\\text{Error: $msg}",
                displayString = msg,
                isError = true,
                errorMessage = msg
            )
        }
    }
}
