package com.example.math.renderer

import java.util.Locale
import java.util.regex.Pattern

/**
 * Expression Normalizer and Sanitizer for CALCX Math Engine.
 * Converts raw user inputs, programming syntax, calculus expressions,
 * chemical formulas, and scientific notations into standard KaTeX-compatible LaTeX.
 */
object MathExpressionNormalizer {

    private val greekSymbols = mapOf(
        "alpha" to "\\alpha",
        "beta" to "\\beta",
        "gamma" to "\\gamma",
        "Gamma" to "\\Gamma",
        "delta" to "\\delta",
        "Delta" to "\\Delta",
        "epsilon" to "\\epsilon",
        "varepsilon" to "\\varepsilon",
        "zeta" to "\\zeta",
        "eta" to "\\eta",
        "theta" to "\\theta",
        "Theta" to "\\Theta",
        "iota" to "\\iota",
        "kappa" to "\\kappa",
        "lambda" to "\\lambda",
        "Lambda" to "\\Lambda",
        "mu" to "\\mu",
        "nu" to "\\nu",
        "xi" to "\\xi",
        "Xi" to "\\Xi",
        "pi" to "\\pi",
        "Pi" to "\\Pi",
        "rho" to "\\rho",
        "sigma" to "\\sigma",
        "Sigma" to "\\Sigma",
        "tau" to "\\tau",
        "upsilon" to "\\upsilon",
        "phi" to "\\phi",
        "Phi" to "\\Phi",
        "chi" to "\\chi",
        "psi" to "\\psi",
        "Psi" to "\\Psi",
        "omega" to "\\omega",
        "Omega" to "\\Omega",
        "infty" to "\\infty",
        "infinity" to "\\infty"
    )

    /**
     * Main normalization pipeline.
     * Takes any string (raw calculator expression, AST string, LaTeX, chemistry formula, etc.)
     * and produces pristine KaTeX LaTeX.
     */
    fun normalizeToKatex(rawInput: String): String {
        val trimmed = rawInput.trim()
        if (trimmed.isEmpty()) return "0"

        // 1. If it's already well-formed LaTeX with matrices or environments, sanitize and return
        if (trimmed.startsWith("\\begin{") && trimmed.contains("\\end{")) {
            return sanitizeKatex(trimmed)
        }

        // 2. Pre-transform raw string into normalized LaTeX representation
        val transformed = transformRawStringToLatex(trimmed)

        // 3. Try parsing through UniversalMathParser to build the canonical AST
        try {
            val astNode = UniversalMathParser.parse(transformed)
            val latexFromAst = astNode.toLatex()
            if (latexFromAst.isNotBlank() && !latexFromAst.contains("NaN")) {
                return sanitizeKatex(latexFromAst)
            }
        } catch (_: Exception) {
            // Fall back to transformed string
        }

        return sanitizeKatex(transformed)
    }

    /**
     * Rule-based transformation for raw mathematical and calculator strings.
     */
    fun transformRawStringToLatex(input: String): String {
        var s = input.trim()

        // Clean up common programming operators and substitutions
        s = s.replace("_×_", " \\times ")
        s = s.replace("_*_ ", " \\times ")
        s = s.replace(" * ", " \\times ")
        s = s.replace("*", " \\cdot ")
        s = s.replace("+-", " \\pm ")
        s = s.replace("-+", " \\mp ")
        s = s.replace("<=", " \\le ")
        s = s.replace(">=", " \\ge ")
        s = s.replace("!=", " \\neq ")
        s = s.replace("~=", " \\approx ")
        s = s.replace("=>", " \\implies ")
        s = s.replace("->", " \\to ")

        // Transform inverse trigonometric functions to sin^-1, cos^-1, tan^-1, cot^-1, sec^-1, csc^-1
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arcsin|asin|sin\\^\\{-1\\}|sin\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\sin^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccos|acos|cos\\^\\{-1\\}|cos\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\cos^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arctan|atan|tan\\^\\{-1\\}|tan\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\tan^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccot|acot|cot\\^\\{-1\\}|cot\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\cot^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arcsec|asec|sec\\^\\{-1\\}|sec\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\sec^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccsc|acsc|arccosec|acosec|csc\\^\\{-1\\}|csc\\^-1|cosec\\^\\{-1\\}|cosec\\^-1)\\s*\\(([^)]+)\\)")) {
            "\\csc^{-1}\\left(${it.groupValues[1]}\\right)"
        }

        // Standalone inverse trig without parens (e.g. sin^-1 x, arcsin x)
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arcsin|asin|sin\\^\\{-1\\}|sin\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\sin^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccos|acos|cos\\^\\{-1\\}|cos\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\cos^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arctan|atan|tan\\^\\{-1\\}|tan\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\tan^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccot|acot|cot\\^\\{-1\\}|cot\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\cot^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arcsec|asec|sec\\^\\{-1\\}|sec\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\sec^{-1}\\left(${it.groupValues[1]}\\right)"
        }
        s = s.replace(Regex("(?<![a-zA-Z\\\\])(?:arccsc|acsc|arccosec|acosec|csc\\^\\{-1\\}|csc\\^-1|cosec\\^\\{-1\\}|cosec\\^-1)\\s+([a-zA-Z0-9]+)")) {
            "\\csc^{-1}\\left(${it.groupValues[1]}\\right)"
        }

        // Transform standard trig & calculus function calls with/without parens
        val stdFuncs = listOf("sin", "cos", "tan", "cot", "sec", "csc", "cosec", "sinh", "cosh", "tanh", "coth", "sech", "csch", "ln", "log", "log10", "exp")
        for (f in stdFuncs) {
            val cmd = if (f == "cosec") "\\operatorname{cosec}" else "\\$f"
            s = s.replace(Regex("(?<![a-zA-Z\\\\])$f\\s*\\(([^)]+)\\)")) {
                "$cmd\\left(${it.groupValues[1]}\\right)"
            }
        }

        // Transform summations: sum_{i=1}^n i, Σ(i=1,n) i, sum(i=1..n, i)
        s = s.replace(Regex("Σ\\(([a-zA-Z0-9=]+),([a-zA-Z0-9]+)\\)\\s*(.*)")) {
            "\\sum_{${it.groupValues[1]}}^{${it.groupValues[2]}} ${it.groupValues[3]}"
        }
        s = s.replace(Regex("(?:\\\\sum|sum|∑)_?\\{?([a-zA-Z0-9=]+)\\}?\\^?\\{?([a-zA-Z0-9]+)\\}?\\s+(.*)")) {
            "\\sum_{${it.groupValues[1]}}^{${it.groupValues[2]}} ${it.groupValues[3]}"
        }

        // Transform integrals: int_0^1 x^2 dx, ∫x² dx
        s = s.replace(Regex("(?:\\\\int|int|∫)_([a-zA-Z0-9.]+)\\^([a-zA-Z0-9.]+)\\s*(.*?)(?:\\s*d([a-zA-Z]))?")) { m ->
            val lower = m.groupValues[1]
            val upper = m.groupValues[2]
            val body = m.groupValues[3]
            val variable = m.groupValues[4].ifEmpty { "x" }
            "\\int_{$lower}^{$upper} $body \\, d$variable"
        }
        s = s.replace(Regex("(?:\\\\int|int|∫)\\s*(.*?)(?:\\s*d([a-zA-Z]))")) { m ->
            val body = m.groupValues[1]
            val variable = m.groupValues[2]
            "\\int $body \\, d$variable"
        }

        // Transform limits: lim x->0 sin(x)/x, lim_{x \to 0} ...
        s = s.replace(Regex("(?<!\\\\)lim\\s*(_\\{[^{}]+\\}|_\\w+)")) { "\\lim${it.groupValues[1]}" }
        s = s.replace(Regex("lim\\s+([a-zA-Z])\\s*(?:->|\\\\to|to)\\s*([a-zA-Z0-9]+)\\s+(.*)")) {
            "\\lim_{${it.groupValues[1]} \\to ${it.groupValues[2]}} ${it.groupValues[3]}"
        }

        // Transform d/dx(x^2) or d/dx f(x)
        s = s.replace(Regex("d/d([a-zA-Z])\\s*\\(([^)]+)\\)")) { m ->
            "\\frac{d}{d${m.groupValues[1]}}\\left(${m.groupValues[2]}\\right)"
        }
        s = s.replace(Regex("d\\^?([0-9]+)?/d([a-zA-Z])\\^?([0-9]+)?\\s*\\(([^)]+)\\)")) { m ->
            val order = m.groupValues[1].ifEmpty { "1" }
            val v = m.groupValues[2]
            val inner = m.groupValues[4]
            if (order == "1") "\\frac{d}{d$v}\\left($inner\\right)"
            else "\\frac{d^{$order}}{d$v^{$order}}\\left($inner\\right)"
        }

        // Transform sqrt(...) and cbrt(...)
        s = s.replace(Regex("cbrt\\(([^)]+)\\)")) { "\\sqrt[3]{${it.groupValues[1]}}" }
        s = s.replace(Regex("sqrt\\(([^)]+)\\)")) { "\\sqrt{${it.groupValues[1]}}" }
        s = s.replace(Regex("√\\(([^)]+)\\)")) { "\\sqrt{${it.groupValues[1]}}" }
        s = s.replace(Regex("√([a-zA-Z0-9]+)")) { "\\sqrt{${it.groupValues[1]}}" }

        // Transform fractions like (a+b)/c or (a)/(b)
        s = s.replace(Regex("\\(([^()]+)\\)/\\(([^()]+)\\)")) { "\\frac{${it.groupValues[1]}}{${it.groupValues[2]}}" }
        s = s.replace(Regex("\\(([^()]+)\\)/([a-zA-Z0-9]+)")) { "\\frac{${it.groupValues[1]}}{${it.groupValues[2]}}" }
        s = s.replace(Regex("([a-zA-Z0-9]+)/\\(([^()]+)\\)")) { "\\frac{${it.groupValues[1]}}{${it.groupValues[2]}}" }

        // Transform matrix literal [1 2; 3 4]
        if (s.startsWith("[") && s.endsWith("]") && s.contains(";")) {
            val body = s.substring(1, s.length - 1).trim()
            val rows = body.split(';').map { it.trim() }
            val formattedRows = rows.joinToString(" \\\\ ") { r ->
                r.split(Regex("[,\\s]+")).filter { it.isNotEmpty() }.joinToString(" & ")
            }
            return "\\begin{bmatrix} $formattedRows \\end{bmatrix}"
        }

        // Transform subscripts: x_1 -> x_{1}, a_i -> a_{i}, v_initial -> v_{\\text{initial}}
        s = s.replace(Regex("([a-zA-Z])_([a-zA-Z0-9]+)")) { m ->
            val v = m.groupValues[1]
            val sub = m.groupValues[2]
            if (sub.length > 1 && sub.all { it.isLetter() }) {
                "${v}_{\\text{$sub}}"
            } else {
                "${v}_{$sub}"
            }
        }

        // Transform superscripts / powers: x^2 -> x^{2}, e^(-x) -> e^{-x}
        s = s.replace(Regex("([a-zA-Z0-9)]+)\\^\\(([^)]+)\\)")) { "${it.groupValues[1]}^{${it.groupValues[2]}}" }
        s = s.replace(Regex("([a-zA-Z0-9)]+)\\^([a-zA-Z0-9]+)")) { "${it.groupValues[1]}^{${it.groupValues[2]}}" }

        // Transform Greek words into LaTeX commands (e.g. theta -> \theta, Delta -> \Delta)
        for ((word, cmd) in greekSymbols) {
            val pattern = Regex("(?<=[^a-zA-Z\\\\]|^)${Pattern.quote(word)}(?=[^a-zA-Z]|$)")
            s = s.replace(pattern, cmd)
        }

        // Transform scientific notation: 6.022e23 or 6.022E-19
        s = s.replace(Regex("([0-9.]+)[eE]([+-]?[0-9]+)")) { m ->
            "${m.groupValues[1]} \\times 10^{${m.groupValues[2]}}"
        }

        return s
    }

    /**
     * Clean and sanitize KaTeX string, ensuring balanced braces, security sanitization, and escaping issues are resolved.
     */
    fun sanitizeKatex(latex: String): String {
        var res = latex.trim()

        // 1. Security sanitization: Strip potentially dangerous raw commands without heavy dependencies
        val forbiddenCommands = listOf(
            "\\html", "\\href", "\\url", "\\includegraphics", "\\special",
            "\\write18", "\\input", "\\include", "\\openin", "\\read", "\\catcode"
        )
        for (cmd in forbiddenCommands) {
            res = res.replace(Regex("${Pattern.quote(cmd)}(?:\\{[^}]*\\}|\\[[^\\]]*\\])*"), "")
        }
        res = res.replace(Regex("(?i)<script.*?>.*?</script>"), "")
        res = res.replace(Regex("(?i)javascript:"), "")

        // 2. Normalize and clean LaTeX symbols
        res = res.replace("\\\\;", " \\; ")
        res = res.replace("\\\\ ", " ")
        res = res.replace("×", "\\times")
        res = res.replace("·", "\\cdot")
        res = res.replace("÷", "\\div")
        res = res.replace("≤", "\\le")
        res = res.replace("≥", "\\ge")
        res = res.replace("≠", "\\neq")
        res = res.replace("≈", "\\approx")
        res = res.replace("±", "\\pm")
        res = res.replace("∓", "\\mp")
        res = res.replace("→", "\\to")
        res = res.replace("⟹", "\\implies")
        res = res.replace("∞", "\\infty")
        res = res.replace("∂", "\\partial")

        // Ensure common units are wrapped in \text{} if at end
        val units = listOf("m/s^2", "m/s^{2}", "m/s²", "m/s", "km/h", "g/mol", "kg/m^3", "N/m", "N", "J", "W", "Pa", "C", "V", "A", "Hz", "rad/s", "K", "°C", "mol", "m", "s", "kg", "g")
        for (u in units) {
            if (res.endsWith(" $u") && !res.endsWith("\\text{ $u}")) {
                res = res.removeSuffix(" $u") + " \\text{ $u}"
                break
            }
        }

        return res
    }
}
