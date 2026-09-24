package com.example.math.calculus

import java.util.Locale

/**
 * Robust LaTeX-to-Expr parser and normalizer.
 * Takes the raw LaTeX output of MathQuill (or KaTeX) and converts it into an AST (Expr)
 * for high-performance plotting and repeated evaluation.
 */
object LatexToExprConverter {

    fun convertToEvaluatableString(latex: String): String {
        var s = latex.trim()
        if (s.isEmpty()) return "0"

        // 1. Remove LaTeX formatting commands like \left, \right, \displaystyle, \mathrm, \text
        s = s.replace("\\left(", "(")
        s = s.replace("\\right)", ")")
        s = s.replace("\\left[", "[")
        s = s.replace("\\right]", "]")
        s = s.replace("\\left|", "abs(")
        s = s.replace("\\right|", ")")
        s = s.replace("\\left.", "")
        s = s.replace("\\right.", "")
        s = s.replace("\\displaystyle", "")
        s = s.replace(Regex("\\\\text\\{([^}]*)\\}"), "$1")
        s = s.replace(Regex("\\\\mathrm\\{([^}]*)\\}"), "$1")
        s = s.replace(Regex("\\\\operatorname\\{([^}]*)\\}"), "$1")

        // 2. Unpack fractions: \frac{a}{b} -> ((a)/(b))
        s = unpackFractions(s)

        // 3. Unpack nth roots and square roots: \sqrt[n]{x} -> ((x)^(1/(n))), \sqrt{x} -> (sqrt(x))
        s = unpackRoots(s)

        // 4. Inverse Trigonometric Functions: \sin^{-1}, \cos^{-1}, \tan^{-1}, etc.
        s = s.replace("\\sin^{-1}", "asin")
        s = s.replace("\\cos^{-1}", "acos")
        s = s.replace("\\tan^{-1}", "atan")
        s = s.replace("\\cot^{-1}", "acot")
        s = s.replace("\\sec^{-1}", "asec")
        s = s.replace("\\csc^{-1}", "acsc")
        s = s.replace("\\cosec^{-1}", "acsc")

        // Also check sin^-1 or sin^{-1} without backslash
        s = s.replace("sin^{-1}", "asin")
        s = s.replace("cos^{-1}", "acos")
        s = s.replace("tan^{-1}", "atan")
        s = s.replace("cot^{-1}", "acot")
        s = s.replace("sec^{-1}", "asec")
        s = s.replace("csc^{-1}", "acsc")
        s = s.replace("cosec^{-1}", "acsc")
        s = s.replace("sin^-1", "asin")
        s = s.replace("cos^-1", "acos")
        s = s.replace("tan^-1", "atan")
        s = s.replace("cot^-1", "acot")
        s = s.replace("sec^-1", "asec")
        s = s.replace("csc^-1", "acsc")

        // 5. Common trigonometric and math function commands
        val funcs = listOf("sinh", "cosh", "tanh", "coth", "sech", "csch", "sin", "cos", "tan", "cot", "sec", "csc", "cosec", "arcsin", "arccos", "arctan", "arccot", "arcsec", "arccsc", "asin", "acos", "atan", "acot", "asec", "acsc", "ln", "log", "exp", "abs", "round", "floor", "ceil")
        for (f in funcs) {
            s = s.replace("\\$f", f)
        }

        // 6. Greek letters and symbols
        s = s.replace("\\pi", "pi")
        s = s.replace("\\theta", "theta")
        s = s.replace("\\cdot", " * ")
        s = s.replace("\\times", " * ")
        s = s.replace("\\div", " / ")
        s = s.replace("\\pm", " + ")
        s = s.replace("\\le", " <= ")
        s = s.replace("\\ge", " >= ")
        s = s.replace("\\leq", " <= ")
        s = s.replace("\\geq", " >= ")
        s = s.replace("\\neq", " != ")
        s = s.replace("\\approx", " = ")

        // 7. Superscripts / powers: ^{expr} -> ^(expr)
        s = unpackPowers(s)

        // 8. Subscripts: _{sub} -> _sub
        s = s.replace(Regex("_\\{([^}]+)\\}"), "_$1")

        // 9. Remove any remaining backslashes or unnecessary spaces
        s = s.replace("\\", "")
        s = s.replace(Regex("\\s+"), " ")

        return s.trim()
    }

    private fun unpackFractions(input: String): String {
        var s = input
        while (s.contains("\\frac")) {
            val idx = s.indexOf("\\frac")
            val numStart = s.indexOf('{', idx)
            if (numStart == -1) break
            val numEnd = findMatchingBrace(s, numStart)
            if (numEnd == -1) break

            val denStart = s.indexOf('{', numEnd + 1)
            if (denStart == -1) break
            val denEnd = findMatchingBrace(s, denStart)
            if (denEnd == -1) break

            val num = s.substring(numStart + 1, numEnd)
            val den = s.substring(denStart + 1, denEnd)

            val replacement = "(($num)/($den))"
            s = s.substring(0, idx) + replacement + s.substring(denEnd + 1)
        }
        return s
    }

    private fun unpackRoots(input: String): String {
        var s = input
        // 1. \sqrt[n]{x}
        while (s.contains("\\sqrt[")) {
            val idx = s.indexOf("\\sqrt[")
            val indexEnd = s.indexOf(']', idx)
            if (indexEnd == -1) break
            val nStr = s.substring(idx + 6, indexEnd)

            val radStart = s.indexOf('{', indexEnd)
            if (radStart == -1) break
            val radEnd = findMatchingBrace(s, radStart)
            if (radEnd == -1) break

            val rad = s.substring(radStart + 1, radEnd)
            val replacement = "(($rad)^(1/($nStr)))"
            s = s.substring(0, idx) + replacement + s.substring(radEnd + 1)
        }

        // 2. \sqrt{x}
        while (s.contains("\\sqrt")) {
            val idx = s.indexOf("\\sqrt")
            val radStart = s.indexOf('{', idx)
            if (radStart == -1) break
            val radEnd = findMatchingBrace(s, radStart)
            if (radEnd == -1) break

            val rad = s.substring(radStart + 1, radEnd)
            val replacement = "sqrt($rad)"
            s = s.substring(0, idx) + replacement + s.substring(radEnd + 1)
        }
        return s
    }

    private fun unpackPowers(input: String): String {
        var s = input
        while (s.contains("^{")) {
            val idx = s.indexOf("^{")
            val end = findMatchingBrace(s, idx + 1)
            if (end == -1) break
            val inner = s.substring(idx + 2, end)
            s = s.substring(0, idx) + "^(" + inner + ")" + s.substring(end + 1)
        }
        return s
    }

    private fun findMatchingBrace(str: String, openIndex: Int): Int {
        var depth = 0
        for (i in openIndex until str.length) {
            if (str[i] == '{') depth++
            else if (str[i] == '}') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }

    /**
     * Parses a LaTeX string directly to an Expr AST.
     */
    fun parseLatexToExpr(latex: String): Expr {
        val evaluatable = convertToEvaluatableString(latex)
        return CalculusParser.parse(evaluatable)
    }
}
