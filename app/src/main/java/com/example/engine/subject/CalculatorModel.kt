package com.example.engine.subject

import kotlin.math.*

enum class SubjectType(val displayName: String) {
    PHYSICS("Physics"),
    CHEMISTRY("Chemistry"),
    MATHEMATICS("Mathematics")
}

data class CalculationField(
    val id: String,
    val label: String,
    val defaultValue: String = "",
    val placeholder: String = "",
    val unit: String = "",
    val allowedUnits: List<String> = emptyList(),
    val description: String = "",
    val isRequired: Boolean = true
)

data class CalculationStep(
    val title: String,
    val latex: String,
    val explanation: String = ""
)

data class CalculationExecutionResult(
    val title: String,
    val formulaLatex: String,
    val substitutionLatex: String,
    val exactResultLatex: String,
    val numericResult: String,
    val unit: String,
    val steps: List<CalculationStep> = emptyList(),
    val isVerified: Boolean = false,
    val verificationNote: String = "",
    val assumptions: String = "",
    val conditions: String = "",
    val graphPoints: List<Pair<Double, Double>>? = null,
    val graphXLabel: String = "x",
    val graphYLabel: String = "y"
)

data class CalculatorDefinition(
    val id: String,
    val subject: SubjectType,
    val chapter: String,
    val topic: String,
    val name: String,
    val description: String,
    val formulaLatex: String,
    val inputs: List<CalculationField>,
    val assumptions: String = "",
    val conditions: String = "",
    val searchKeywords: List<String> = emptyList(),
    val execute: (Map<String, String>, Map<String, String>) -> Result<CalculationExecutionResult>
)

object NumberFormatter {
    fun formatDouble(value: Double, maxDecimals: Int = 5): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "∞" else "-∞"
        if (abs(value - round(value)) < 1e-9 && abs(value) < 1e12) {
            return round(value).toLong().toString()
        }
        val absVal = abs(value)
        if ((absVal in 1e-4..1e7) || absVal == 0.0) {
            val rounded = (value * 10.0.pow(maxDecimals)).roundToLong() / 10.0.pow(maxDecimals)
            var str = rounded.toString()
            if (str.contains(".")) {
                str = str.trimEnd('0').trimEnd('.')
            }
            return str
        }
        // Scientific notation
        val exp = floor(log10(absVal)).toInt()
        val mantissa = value / 10.0.pow(exp)
        val roundedMantissa = (mantissa * 1000.0).roundToLong() / 1000.0
        return "${roundedMantissa} × 10^{$exp}"
    }

    fun toFractionString(value: Double, maxDenominator: Int = 1000): String? {
        if (value.isNaN() || value.isInfinite() || abs(value) > 1e6) return null
        val sign = if (value < 0) "-" else ""
        val absVal = abs(value)
        val intPart = floor(absVal).toLong()
        val fracPart = absVal - intPart

        if (fracPart < 1e-7) return "$sign$intPart"
        if (1.0 - fracPart < 1e-7) return "$sign${intPart + 1}"

        var bestP = 1L
        var bestQ = 1L
        var minDiff = Double.MAX_VALUE

        for (q in 1..maxDenominator) {
            val p = round(fracPart * q).toLong()
            val diff = abs(fracPart - p.toDouble() / q.toDouble())
            if (diff < minDiff) {
                minDiff = diff
                bestP = p
                bestQ = q.toLong()
            }
            if (diff < 1e-9) break
        }

        if (minDiff < 1e-6) {
            val totalNumerator = intPart * bestQ + bestP
            return if (bestQ == 1L) "$sign$totalNumerator" else "\\frac{$sign$totalNumerator}{$bestQ}"
        }
        return null
    }
}
