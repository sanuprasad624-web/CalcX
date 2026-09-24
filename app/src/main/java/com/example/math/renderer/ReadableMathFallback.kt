package com.example.math.renderer

/**
 * Safe Mathematical Fallback Formatter.
 * When KaTeX fails or falls back, converts expressions into clean Unicode mathematical symbols.
 * NEVER displays raw LaTeX commands (\frac, \sqrt, \text) or programming syntax (_, ^) directly to the user.
 */
object ReadableMathFallback {

    private val superscriptMap = mapOf(
        '0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
        '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹',
        '+' to '⁺', '-' to '⁻', '=' to '⁼', '(' to '⁽', ')' to '⁾',
        'n' to 'ⁿ', 'i' to 'ⁱ', 'x' to 'ˣ', 'y' to 'ʸ'
    )

    private val subscriptMap = mapOf(
        '0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
        '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉',
        '+' to '₊', '-' to '₋', '=' to '₌', '(' to '₍', ')' to '₎',
        'a' to 'ₐ', 'e' to 'ₑ', 'h' to 'ₕ', 'i' to 'ᵢ', 'j' to 'ⱼ',
        'k' to 'ₖ', 'l' to 'ₗ', 'm' to 'ₘ', 'n' to 'ₙ', 'o' to 'ₒ',
        'p' to 'ₚ', 'r' to 'ᵣ', 's' to 'ₛ', 't' to 'ₜ', 'u' to 'ᵤ',
        'v' to 'ᵥ', 'x' to 'ₓ'
    )

    private val latexSymbolMap = mapOf(
        "\\alpha" to "α",
        "\\beta" to "β",
        "\\gamma" to "γ",
        "\\delta" to "δ",
        "\\Delta" to "Δ",
        "\\theta" to "θ",
        "\\Theta" to "Θ",
        "\\lambda" to "λ",
        "\\Lambda" to "Λ",
        "\\mu" to "μ",
        "\\pi" to "π",
        "\\Pi" to "Π",
        "\\sigma" to "σ",
        "\\Sigma" to "Σ",
        "\\omega" to "ω",
        "\\Omega" to "Ω",
        "\\infty" to "∞",
        "\\partial" to "∂",
        "\\int" to "∫",
        "\\sum" to "∑",
        "\\prod" to "∏",
        "\\times" to "×",
        "\\cdot" to "·",
        "\\div" to "÷",
        "\\pm" to "±",
        "\\mp" to "∓",
        "\\le" to "≤",
        "\\leq" to "≤",
        "\\ge" to "≥",
        "\\geq" to "≥",
        "\\neq" to "≠",
        "\\approx" to "≈",
        "\\to" to "→",
        "\\rightarrow" to "→",
        "\\implies" to "⟹",
        "\\left" to "",
        "\\right" to "",
        "\\," to " ",
        "\\;" to " ",
        "\\quad" to "  "
    )

    fun toReadableUnicode(latexOrExpr: String): String {
        var s = latexOrExpr.trim()
        if (s.isEmpty()) return "0"

        // Strip \left and \right delimiters cleanly
        s = s.replace(Regex("\\\\left\\s*([(\\[{|.])"), "$1")
        s = s.replace(Regex("\\\\right\\s*([)\\]}|.])"), "$1")
        s = s.replace("\\left", "")
        s = s.replace("\\right", "")
        s = s.replace("\\Box", "⬚")
        s = s.replace("\\vert", "|")
        s = s.replace(Regex("\\\\color\\{[^{}]+\\}"), "")

        // Replace \frac{a}{b} -> (a)/(b)
        s = s.replace(Regex("\\\\frac\\{([^{}]+)\\}\\{([^{}]+)\\}")) { "(${it.groupValues[1]})/(${it.groupValues[2]})" }
        s = s.replace(Regex("\\\\dfrac\\{([^{}]+)\\}\\{([^{}]+)\\}")) { "(${it.groupValues[1]})/(${it.groupValues[2]})" }

        // Replace \sqrt{a} -> √(a), \sqrt[3]{a} -> ³√(a)
        s = s.replace(Regex("\\\\sqrt\\[([^{}\\]]+)\\]\\{([^{}]+)\\}")) { "${toSuperscript(it.groupValues[1])}√(${it.groupValues[2]})" }
        s = s.replace(Regex("\\\\sqrt\\{([^{}]+)\\}")) { "√(${it.groupValues[1]})" }

        // Replace \text{...} -> ...
        s = s.replace(Regex("\\\\text\\{([^{}]+)\\}")) { it.groupValues[1] }
        s = s.replace(Regex("\\\\vec\\{([^{}]+)\\}")) { "${it.groupValues[1]}⃗" }

        // Replace symbols
        for ((cmd, sym) in latexSymbolMap) {
            s = s.replace(cmd, sym)
        }

        // Replace superscripts ^{...} or ^...
        s = s.replace(Regex("\\^\\{([^{}]+)\\}")) { toSuperscript(it.groupValues[1]) }
        s = s.replace(Regex("\\^([a-zA-Z0-9+-])")) { toSuperscript(it.groupValues[1]) }

        // Replace subscripts _{...} or _...
        s = s.replace(Regex("_\\{([^{}]+)\\}")) { toSubscript(it.groupValues[1]) }
        s = s.replace(Regex("_([a-zA-Z0-9])")) { toSubscript(it.groupValues[1]) }

        // Strip remaining stray braces or backslashes
        s = s.replace("{", "").replace("}", "").replace("\\", "")
        s = s.replace("_", " ")

        return s.trim()
    }

    private fun toSuperscript(text: String): String {
        return text.map { superscriptMap[it] ?: it }.joinToString("")
    }

    private fun toSubscript(text: String): String {
        return text.map { subscriptMap[it] ?: it }.joinToString("")
    }
}
