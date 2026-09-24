package com.example.math.renderer

import com.example.math.CanonicalMathResult
import com.example.math.calculus.*
import java.math.BigInteger
import java.util.Locale

/**
 * Universal Mathematical Parser.
 * Transforms raw calculator expressions, LaTeX strings, calculus AST (Expr),
 * chemistry formulas, and formatted results into the single Canonical MathNode AST.
 */
object UniversalMathParser {

    /**
     * Parse from any string input (LaTeX, calculator expression, chemical formula, or unit).
     */
    fun parse(input: String): MathNode {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return NumberNode("0")

        // 1. Check for chemical formula if applicable
        if (isChemicalFormula(trimmed)) {
            val chem = parseChemicalFormula(trimmed)
            if (chem != null) return chem
        }

        // 2. Check for matrix bracket string like "[1 2; 3 4]"
        if (isMatrixLiteral(trimmed)) {
            val mat = parseMatrixLiteral(trimmed)
            if (mat != null) return mat
        }

        // 3. Check for unit suffix like "9.8 m/s²" or "20 J"
        val unitMatch = extractUnit(trimmed)
        if (unitMatch != null) {
            val (valStr, unit) = unitMatch
            return UnitNode(parse(valStr), unit)
        }

        // 4. Try LaTeX parsing if input contains backslashes or typical LaTeX tokens
        if (trimmed.contains('\\') || trimmed.contains('{') || trimmed.contains('}')) {
            try {
                return LatexParser(trimmed).parse()
            } catch (_: Exception) {
                // Fall through to general expression parser
            }
        }

        // 5. Parse general math expression / calculus syntax
        return try {
            GeneralMathParser(trimmed).parse()
        } catch (_: Exception) {
            // Safe fallback
            TextNode(trimmed)
        }
    }

    /**
     * Convert calculus engine AST (Expr) directly to Canonical MathNode without string roundtrips!
     */
    fun fromExpr(expr: Expr): MathNode {
        return when (expr) {
            is Constant -> {
                if (expr.fraction != null && expr.fraction.denominator != BigInteger.ONE) {
                    val simp = expr.fraction.simplified()
                    val sign = simp.numerator < BigInteger.ZERO
                    val num = simp.numerator.abs().toString()
                    val den = simp.denominator.toString()
                    val frac = FractionNode(NumberNode(num), NumberNode(den))
                    if (sign) RowNode(listOf(OperatorNode("-"), frac)) else frac
                } else {
                    val rounded = Math.round(expr.value)
                    if (kotlin.math.abs(expr.value - rounded) < 1e-10) {
                        NumberNode(rounded.toString())
                    } else {
                        val formatted = String.format(Locale.US, "%.6f", expr.value).trimEnd('0').trimEnd('.')
                        NumberNode(formatted)
                    }
                }
            }
            is Variable -> {
                when (expr.name.lowercase(Locale.US)) {
                    "pi" -> SymbolNode("π", "pi", "\\pi")
                    "theta" -> SymbolNode("θ", "theta", "\\theta")
                    "alpha" -> SymbolNode("α", "alpha", "\\alpha")
                    "beta" -> SymbolNode("β", "beta", "\\beta")
                    "gamma" -> SymbolNode("γ", "gamma", "\\gamma")
                    "infinity", "inf" -> SymbolNode("∞", "infinity", "\\infty")
                    else -> VariableNode(expr.name)
                }
            }
            is Add -> RowNode(listOf(fromExpr(expr.left), OperatorNode("+"), fromExpr(expr.right)))
            is Sub -> RowNode(listOf(fromExpr(expr.left), OperatorNode("-"), fromExpr(expr.right)))
            is Mul -> {
                // If left is Constant and right is Variable/Func, omit explicit times
                if (expr.left is Constant && (expr.right is Variable || expr.right is Func)) {
                    RowNode(listOf(fromExpr(expr.left), fromExpr(expr.right)))
                } else {
                    RowNode(listOf(fromExpr(expr.left), OperatorNode("·"), fromExpr(expr.right)))
                }
            }
            is Div -> FractionNode(fromExpr(expr.left), fromExpr(expr.right))
            is Pow -> {
                val baseNode = fromExpr(expr.base)
                val expNode = fromExpr(expr.exp)
                PowerNode(baseNode, expNode)
            }
            is Neg -> RowNode(listOf(OperatorNode("-"), fromExpr(expr.inner)))
            is Func -> {
                val argNode = fromExpr(expr.arg)
                when (expr.name.lowercase(Locale.US)) {
                    "sqrt" -> RadicalNode(argNode)
                    "abs" -> AbsNode(argNode)
                    "asin", "arcsin", "sin^-1", "sin^{-1}" -> FunctionNode("sin", argNode, power = NumberNode("-1"))
                    "acos", "arccos", "cos^-1", "cos^{-1}" -> FunctionNode("cos", argNode, power = NumberNode("-1"))
                    "atan", "arctan", "tan^-1", "tan^{-1}" -> FunctionNode("tan", argNode, power = NumberNode("-1"))
                    "acot", "arccot", "cot^-1", "cot^{-1}" -> FunctionNode("cot", argNode, power = NumberNode("-1"))
                    "asec", "arcsec", "sec^-1", "sec^{-1}" -> FunctionNode("sec", argNode, power = NumberNode("-1"))
                    "acsc", "arccsc", "csc^-1", "csc^{-1}" -> FunctionNode("csc", argNode, power = NumberNode("-1"))
                    "acosec", "arccosec", "cosec^-1", "cosec^{-1}" -> FunctionNode("cosec", argNode, power = NumberNode("-1"))
                    else -> FunctionNode(expr.name, argNode)
                }
            }
        }
    }

    /**
     * Convert CanonicalMathResult into a MathNode.
     */
    fun fromCanonicalResult(result: CanonicalMathResult): MathNode {
        if (result.isError) {
            return TextNode(result.errorMessage ?: "Error")
        }
        val baseNode = if (result.latex.isNotBlank()) {
            parse(result.latex)
        } else {
            parse(result.primaryDisplay)
        }
        return if (result.unit != null) {
            UnitNode(baseNode, result.unit)
        } else {
            baseNode
        }
    }

    // --- Helpers ---

    private fun isChemicalFormula(s: String): Boolean {
        // e.g. H2SO4, KMnO4, NaCl, CH3CH2OH, NH4+, SO4^2-, Fe2(SO4)3
        if (s.length < 2) return false
        val chemRegex = Regex("^[A-Z][a-z0-9()_{}^+-]+$")
        if (!chemRegex.matches(s)) return false
        // Ensure it contains known chemical letters and at least one lowercase or digit or charge
        val hasChemElements = s.contains(Regex("[A-Z][a-z]?"))
        val hasNotOperators = !s.contains(Regex("[*/=]")) && !s.startsWith("sin") && !s.startsWith("cos")
        return hasChemElements && hasNotOperators && (s.contains(Regex("[0-9]")) || s.contains('+') || s.contains('-'))
    }

    private fun parseChemicalFormula(s: String): ChemNode? {
        try {
            val parts = mutableListOf<ChemPart>()
            var i = 0
            while (i < s.length) {
                val ch = s[i]
                if (ch == '(') {
                    // Group
                    i++
                    var depth = 1
                    val start = i
                    while (i < s.length && depth > 0) {
                        if (s[i] == '(') depth++
                        else if (s[i] == ')') depth--
                        i++
                    }
                    val groupStr = s.substring(start, i - 1)
                    val innerParts = parseChemicalFormula(groupStr)?.parts ?: listOf(ChemPart.Element(groupStr))
                    // Check count
                    val count = readIntDigits(s, i)
                    if (count != null) {
                        i += count.toString().length
                    }
                    parts.add(ChemPart.Group(innerParts, count))
                } else if (ch.isUpperCase()) {
                    val start = i
                    i++
                    while (i < s.length && s[i].isLowerCase()) i++
                    val symbol = s.substring(start, i)
                    // Check subscript count
                    val count = readIntDigits(s, i)
                    if (count != null) {
                        i += count.toString().length
                    }
                    // Check charge (e.g. ^2-, +, 2+, -)
                    var charge: String? = null
                    if (i < s.length && (s[i] == '^' || s[i] == '+' || s[i] == '-')) {
                        if (s[i] == '^') i++
                        val cStart = i
                        while (i < s.length && (s[i].isDigit() || s[i] == '+' || s[i] == '-')) i++
                        charge = s.substring(cStart, i)
                    }
                    parts.add(ChemPart.Element(symbol, count, charge))
                } else {
                    i++
                }
            }
            return if (parts.isNotEmpty()) ChemNode(parts) else null
        } catch (_: Exception) {
            return null
        }
    }

    private fun readIntDigits(s: String, startIndex: Int): Int? {
        var idx = startIndex
        if (idx < s.length && s[idx] == '_') idx++
        if (idx < s.length && s[idx] == '{') {
            idx++
            val start = idx
            while (idx < s.length && s[idx] != '}') idx++
            return s.substring(start, idx).toIntOrNull()
        }
        val start = idx
        while (idx < s.length && s[idx].isDigit()) idx++
        return if (idx > start) s.substring(start, idx).toIntOrNull() else null
    }

    private fun isMatrixLiteral(s: String): Boolean {
        return (s.startsWith("[") && s.endsWith("]") && s.contains(";")) ||
                (s.startsWith("det(") && s.endsWith(")")) ||
                s.contains("\\begin{matrix}") ||
                s.contains("\\begin{bmatrix}") ||
                s.contains("\\begin{vmatrix}")
    }

    private fun parseMatrixLiteral(s: String): MathNode? {
        try {
            val isDet = s.startsWith("det(")
            val inner = if (isDet) s.removePrefix("det(").removeSuffix(")") else s
            if (inner.startsWith("[") && inner.endsWith("]")) {
                val body = inner.substring(1, inner.length - 1).trim()
                val rows = body.split(';').map { it.trim() }.filter { it.isNotEmpty() }
                val cells = rows.map { rowStr ->
                    rowStr.split(Regex("[,\\s]+")).filter { it.isNotEmpty() }.map { parse(it) }
                }
                if (cells.isNotEmpty()) {
                    return MatrixNode(
                        rows = cells.size,
                        cols = cells[0].size,
                        cells = cells,
                        isDeterminant = isDet
                    )
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun extractUnit(s: String): Pair<String, String>? {
        // e.g. "9.8 m/s²", "20 J", "3.2 × 10⁵ Pa", "1.0e-6 C"
        val knownUnits = listOf("m/s²", "m/s", "km/h", "m/s^2", "g/mol", "kg/m³", "N/m", "N", "J", "W", "Pa", "C", "V", "A", "Hz", "rad/s", "K", "°C", "mol", "m", "s", "kg", "g")
        for (u in knownUnits) {
            if (s.endsWith(" $u") || s.endsWith("\\text{ $u}")) {
                val valPart = s.removeSuffix(" $u").removeSuffix("\\text{ $u}").trim()
                if (valPart.isNotEmpty()) return Pair(valPart, u)
            }
        }
        return null
    }
}

/**
 * LaTeX Parser: transforms LaTeX commands into canonical MathNode AST.
 */
private class LatexParser(private val text: String) {
    private var pos = 0

    fun parse(): MathNode {
        pos = 0
        val nodes = mutableListOf<MathNode>()
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            val node = parseItem()
            if (node != null) nodes.add(node)
        }
        return when {
            nodes.isEmpty() -> NumberNode("0")
            nodes.size == 1 -> nodes[0]
            else -> RowNode(nodes)
        }
    }

    private fun parseItem(): MathNode? {
        skipWhitespace()
        if (pos >= text.length) return null
        val ch = text[pos]

        var base: MathNode = when {
            ch == '\\' -> parseCommand()
            ch == '{' -> {
                pos++
                val content = parseGroup('}')
                content
            }
            ch == '(' || ch == '[' -> {
                val open = ch
                val close = if (open == '(') ')' else ']'
                pos++
                val inner = parseUntil(close)
                if (pos < text.length && text[pos] == close) pos++
                DelimitedNode(
                    inner,
                    if (open == '(') DelimType.PAREN else DelimType.BRACKET,
                    if (close == ')') DelimType.PAREN_CLOSE else DelimType.BRACKET_CLOSE
                )
            }
            ch == '|' -> {
                pos++
                val inner = parseUntil('|')
                if (pos < text.length && text[pos] == '|') pos++
                AbsNode(inner)
            }
            ch.isDigit() || (ch == '.' && pos + 1 < text.length && text[pos + 1].isDigit()) -> {
                parseNumber()
            }
            ch in "+-=<>≠≤≥≈±∓·×÷" -> {
                pos++
                OperatorNode(ch.toString())
            }
            ch.isLetter() -> {
                pos++
                VariableNode(ch.toString())
            }
            else -> {
                pos++
                OperatorNode(ch.toString())
            }
        }

        // Check for superscript ^ or subscript _
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            if (text[pos] == '^') {
                pos++
                val sup = parseScriptArg()
                base = PowerNode(base, sup)
            } else if (text[pos] == '_') {
                pos++
                val sub = parseScriptArg()
                base = SubscriptNode(base, sub)
            } else {
                break
            }
        }

        return base
    }

    private fun parseScriptArg(): MathNode {
        skipWhitespace()
        if (pos >= text.length) return NumberNode("")
        return if (text[pos] == '{') {
            pos++
            parseGroup('}')
        } else {
            val ch = text[pos++]
            if (ch == '\\') parseCommand() else VariableNode(ch.toString())
        }
    }

    private fun parseCommand(): MathNode {
        pos++ // skip '\\'
        val start = pos
        while (pos < text.length && text[pos].isLetter()) pos++
        val cmd = text.substring(start, pos)

        return when (cmd) {
            "frac", "dfrac", "tfrac" -> {
                val num = parseRequiredArg()
                val den = parseRequiredArg()
                FractionNode(num, den)
            }
            "sqrt" -> {
                val index = parseOptionalArg()
                val content = parseRequiredArg()
                RadicalNode(content, index)
            }
            "cdot" -> OperatorNode("·")
            "times" -> OperatorNode("×")
            "div" -> OperatorNode("÷")
            "pm" -> OperatorNode("±")
            "mp" -> OperatorNode("∓")
            "le", "leq" -> OperatorNode("≤")
            "ge", "geq" -> OperatorNode("≥")
            "ne", "neq" -> OperatorNode("≠")
            "approx" -> OperatorNode("≈")
            "equiv" -> OperatorNode("≡")
            "cong" -> OperatorNode("≅")
            "sim" -> OperatorNode("∼")
            "propto" -> OperatorNode("∝")
            "parallel" -> OperatorNode("∥")
            "perp" -> OperatorNode("⊥")
            "angle" -> OperatorNode("∠")
            "therefore" -> OperatorNode("∴")
            "because" -> OperatorNode("∵")
            "circ", "degree" -> OperatorNode("°")
            "to", "rightarrow", "longrightarrow" -> OperatorNode("→")
            "leftarrow", "longleftarrow" -> OperatorNode("←")
            "leftrightarrow" -> OperatorNode("↔")
            "implies", "Longrightarrow" -> OperatorNode("⟹")
            "iff", "Longleftrightarrow" -> OperatorNode("⟺")
            "in" -> OperatorNode("∈")
            "notin" -> OperatorNode("∉")
            "subset" -> OperatorNode("⊂")
            "subseteq" -> OperatorNode("⊆")
            "cap" -> OperatorNode("∩")
            "cup" -> OperatorNode("∪")
            "forall" -> OperatorNode("∀")
            "exists" -> OperatorNode("∃")
            "nabla" -> SymbolNode("∇", "nabla", "\\nabla")
            "pi" -> SymbolNode("π", "pi", "\\pi")
            "theta" -> SymbolNode("θ", "theta", "\\theta")
            "alpha" -> SymbolNode("α", "alpha", "\\alpha")
            "beta" -> SymbolNode("β", "beta", "\\beta")
            "gamma" -> SymbolNode("γ", "gamma", "\\gamma")
            "lambda" -> SymbolNode("λ", "lambda", "\\lambda")
            "mu" -> SymbolNode("μ", "mu", "\\mu")
            "sigma" -> SymbolNode("σ", "sigma", "\\sigma")
            "omega" -> SymbolNode("ω", "omega", "\\omega")
            "rho" -> SymbolNode("ρ", "rho", "\\rho")
            "tau" -> SymbolNode("τ", "tau", "\\tau")
            "phi", "varphi" -> SymbolNode("φ", "phi", "\\phi")
            "psi" -> SymbolNode("ψ", "psi", "\\psi")
            "eta" -> SymbolNode("η", "eta", "\\eta")
            "nu" -> SymbolNode("ν", "nu", "\\nu")
            "xi" -> SymbolNode("ξ", "xi", "\\xi")
            "zeta" -> SymbolNode("ζ", "zeta", "\\zeta")
            "kappa" -> SymbolNode("κ", "kappa", "\\kappa")
            "chi" -> SymbolNode("χ", "chi", "\\chi")
            "epsilon", "varepsilon" -> SymbolNode("ε", "epsilon", "\\epsilon")
            "Delta" -> SymbolNode("Δ", "delta", "\\Delta")
            "Gamma" -> SymbolNode("Γ", "Gamma", "\\Gamma")
            "Lambda" -> SymbolNode("Λ", "Lambda", "\\Lambda")
            "Sigma" -> SymbolNode("Σ", "Sigma", "\\Sigma")
            "Omega" -> SymbolNode("Ω", "Omega", "\\Omega")
            "Phi" -> SymbolNode("Φ", "Phi", "\\Phi")
            "Psi" -> SymbolNode("Ψ", "Psi", "\\Psi")
            "Theta" -> SymbolNode("Θ", "Theta", "\\Theta")
            "infty" -> SymbolNode("∞", "infinity", "\\infty")
            "partial" -> SymbolNode("∂", "partial", "\\partial")
            "int" -> parseIntegral()
            "sum" -> parseBigOp(BigOpType.SUM)
            "prod" -> parseBigOp(BigOpType.PRODUCT)
            "lim" -> parseLimit()
            "vec", "hat", "bar", "overline" -> {
                val v = parseRequiredArg()
                VectorNode(name = v.toPlainText())
            }
            "mathbf", "mathrm", "operatorname", "mathit", "boldsymbol", "text" -> {
                val str = parseRawGroup()
                if (str.isNotEmpty()) TextNode(str) else parseRequiredArg()
            }
            "quad", "qquad" -> OperatorNode("  ")
            "vert", "Vert" -> OperatorNode("|")
            "Box", "square" -> TextNode("⬚")
            "color", "textcolor" -> {
                parseRawGroup() // consume color hex/name
                parseItem() ?: NumberNode("")
            }
            "phantom" -> {
                parseRawGroup() // consume content
                OperatorNode("")
            }
            "limits", "nolimits", "displaystyle", "textstyle", "scriptstyle" -> {
                parseItem() ?: NumberNode("")
            }
            "right" -> {
                skipWhitespace()
                if (pos < text.length && text[pos] in "()[]{}|.") {
                    pos++
                }
                OperatorNode("")
            }
            "left" -> {
                skipWhitespace()
                val delimCh = if (pos < text.length) text[pos++] else '('
                val inner = parseUntilRight()
                when (delimCh) {
                    '|' -> AbsNode(inner)
                    '[' -> DelimitedNode(inner, DelimType.BRACKET, DelimType.BRACKET_CLOSE)
                    '{' -> DelimitedNode(inner, DelimType.BRACE, DelimType.BRACE_CLOSE)
                    else -> DelimitedNode(inner, DelimType.PAREN, DelimType.PAREN_CLOSE)
                }
            }
            "begin" -> parseEnvironment()
            "text" -> {
                val str = parseRawGroup()
                TextNode(str)
            }
            "sin", "cos", "tan", "asin", "acos", "atan", "arcsin", "arccos", "arctan",
            "cot", "sec", "csc", "cosec", "acot", "asec", "acsc", "acosec", "arccot", "arcsec", "arccsc", "arccosec",
            "sinh", "cosh", "tanh", "coth", "sech", "csch", "ln", "log", "log10", "exp" -> {
                // Check if followed by power (e.g. \sin^2(x) or \sin^{-1}(x)) or base (e.g. \log_2(x))
                var power: MathNode? = null
                var base: MathNode? = null
                skipWhitespace()
                while (pos < text.length && (text[pos] == '^' || text[pos] == '_')) {
                    if (text[pos] == '^') {
                        pos++
                        power = parseScriptArg()
                    } else if (text[pos] == '_') {
                        pos++
                        base = parseScriptArg()
                    }
                    skipWhitespace()
                }
                val arg = parseItem() ?: NumberNode("0")
                val normCmd = when (cmd.lowercase(Locale.US)) {
                    "arcsin", "asin" -> { power = NumberNode("-1"); "sin" }
                    "arccos", "acos" -> { power = NumberNode("-1"); "cos" }
                    "arctan", "atan" -> { power = NumberNode("-1"); "tan" }
                    "arccot", "acot" -> { power = NumberNode("-1"); "cot" }
                    "arcsec", "asec" -> { power = NumberNode("-1"); "sec" }
                    "arccsc", "acsc" -> { power = NumberNode("-1"); "csc" }
                    "arccosec", "acosec" -> { power = NumberNode("-1"); "cosec" }
                    else -> cmd
                }
                FunctionNode(normCmd, arg, power, base)
            }
            else -> VariableNode(cmd)
        }
    }

    private fun parseIntegral(): MathNode {
        var lower: MathNode? = null
        var upper: MathNode? = null
        skipWhitespace()
        while (pos < text.length && (text[pos] == '_' || text[pos] == '^')) {
            if (text[pos] == '_') {
                pos++
                lower = parseScriptArg()
            } else if (text[pos] == '^') {
                pos++
                upper = parseScriptArg()
            }
            skipWhitespace()
        }
        val integrand = parseItem() ?: NumberNode("0")
        return IntegralNode(integrand = integrand, lowerLimit = lower, upperLimit = upper)
    }

    private fun parseBigOp(type: BigOpType): MathNode {
        var lower: MathNode? = null
        var upper: MathNode? = null
        skipWhitespace()
        while (pos < text.length && (text[pos] == '_' || text[pos] == '^')) {
            if (text[pos] == '_') {
                pos++
                lower = parseScriptArg()
            } else if (text[pos] == '^') {
                pos++
                upper = parseScriptArg()
            }
            skipWhitespace()
        }
        val body = parseItem() ?: NumberNode("0")
        return BigOperatorNode(type = type, lower = lower, upper = upper, body = body)
    }

    private fun parseLimit(): MathNode {
        var variable = "x"
        var target: MathNode = NumberNode("0")
        skipWhitespace()
        if (pos < text.length && text[pos] == '_') {
            pos++
            val subNode = parseScriptArg()
            val subText = subNode.toPlainText()
            if (subText.contains("→") || subText.contains("to")) {
                val parts = subText.split(Regex("→|to"))
                variable = parts[0].trim()
                target = UniversalMathParser.parse(parts.getOrElse(1) { "0" })
            } else {
                target = subNode
            }
        }
        val expr = parseItem() ?: NumberNode("0")
        return LimitNode(variable = variable, target = target, expr = expr)
    }

    private fun parseEnvironment(): MathNode {
        val envName = parseRawGroup().trim()
        val isDet = envName == "vmatrix" || envName == "Vmatrix"
        val endTag = "\\end{$envName}"
        val endIdx = text.indexOf(endTag, pos)
        val body = if (endIdx != -1) {
            val b = text.substring(pos, endIdx)
            pos = endIdx + endTag.length
            b
        } else {
            val b = text.substring(pos)
            pos = text.length
            b
        }
        val rows = body.split("\\\\").map { it.trim() }.filter { it.isNotEmpty() }
        val cells = rows.map { rowStr ->
            rowStr.split("&").map { UniversalMathParser.parse(it.trim()) }
        }
        return MatrixNode(
            rows = cells.size,
            cols = cells.firstOrNull()?.size ?: 0,
            cells = cells,
            isDeterminant = isDet
        )
    }

    private fun parseUntilRight(): MathNode {
        var depth = 1
        val start = pos
        var endPos = text.length

        var scan = pos
        while (scan < text.length && depth > 0) {
            if (text.startsWith("\\left", scan)) {
                depth++
                scan += 5
            } else if (text.startsWith("\\right", scan)) {
                depth--
                if (depth == 0) {
                    endPos = scan
                    scan += 6 // skip "\\right"
                    skipWhitespace()
                    if (scan < text.length && text[scan] in "()[]{}|.") {
                        scan++
                    }
                    pos = scan
                    break
                } else {
                    scan += 6
                }
            } else {
                scan++
            }
        }

        if (depth > 0) {
            pos = text.length
        }

        val content = text.substring(start, endPos)
        return UniversalMathParser.parse(content)
    }

    private fun parseUntil(delimiter: Char): MathNode {
        val start = pos
        var depth = 0
        while (pos < text.length) {
            val c = text[pos]
            if (c == '{' || c == '(' || c == '[') depth++
            else if (c == '}' || c == ')' || c == ']') depth--
            if (depth < 0 || (depth == 0 && c == delimiter)) break
            pos++
        }
        val sub = text.substring(start, pos)
        return UniversalMathParser.parse(sub)
    }

    private fun parseRequiredArg(): MathNode {
        skipWhitespace()
        if (pos >= text.length) return NumberNode("0")
        return if (text[pos] == '{') {
            pos++
            parseGroup('}')
        } else {
            parseItem() ?: NumberNode("0")
        }
    }

    private fun parseOptionalArg(): MathNode? {
        skipWhitespace()
        if (pos < text.length && text[pos] == '[') {
            pos++
            return parseGroup(']')
        }
        return null
    }

    private fun parseGroup(closeChar: Char): MathNode {
        val start = pos
        var depth = 1
        while (pos < text.length && depth > 0) {
            if (text[pos] == '{' && closeChar == '}') depth++
            else if (text[pos] == closeChar) depth--
            pos++
        }
        val content = text.substring(start, pos - 1)
        return UniversalMathParser.parse(content)
    }

    private fun parseRawGroup(): String {
        skipWhitespace()
        if (pos >= text.length || text[pos] != '{') return ""
        pos++
        val start = pos
        var depth = 1
        while (pos < text.length && depth > 0) {
            if (text[pos] == '{') depth++
            else if (text[pos] == '}') depth--
            pos++
        }
        return text.substring(start, pos - 1)
    }

    private fun parseNumber(): MathNode {
        val start = pos
        while (pos < text.length && (text[pos].isDigit() || text[pos] == '.')) pos++
        val numStr = text.substring(start, pos)
        // Check for scientific exponent e.g. 1.2e-4
        if (pos < text.length && (text[pos] == 'e' || text[pos] == 'E')) {
            pos++
            val expStart = pos
            if (pos < text.length && (text[pos] == '+' || text[pos] == '-')) pos++
            while (pos < text.length && text[pos].isDigit()) pos++
            val expStr = text.substring(expStart, pos)
            return ScientificNode(numStr, expStr)
        }
        return NumberNode(numStr)
    }

    private fun skipWhitespace() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }
}

/**
 * General Math Parser for infix strings, expressions, calculus shorthand, powers, and roots.
 */
private class GeneralMathParser(private val text: String) {
    private var pos = 0

    fun parse(): MathNode {
        pos = 0
        // Check scientific notation: 6.022 * 10^23 or 6.022 × 10²³
        val sciRegex = Regex("^([0-9.]+)\\s*(?:[*×·]|\\\\times)\\s*10\\s*\\^\\s*\\{?([+-]?[0-9]+)\\}?$")
        val sciMatch = sciRegex.matchEntire(text)
        if (sciMatch != null) {
            return ScientificNode(sciMatch.groupValues[1], sciMatch.groupValues[2])
        }

        // Check derivative notation: d^2y/dx^2, d/dx(f(x))
        val derivRegex = Regex("^d(?:\\^([0-9]+))?([a-zA-Z])?/d([a-zA-Z])(?:\\^([0-9]+))?(?:\\s*\\((.*)\\))?$")
        val dMatch = derivRegex.matchEntire(text)
        if (dMatch != null) {
            val order = dMatch.groupValues[1].toIntOrNull() ?: 1
            val funcName = dMatch.groupValues[2].ifEmpty { null }
            val v = dMatch.groupValues[3].ifEmpty { "x" }
            val exprStr = dMatch.groupValues[5]
            val innerNode = if (exprStr.isNotEmpty()) UniversalMathParser.parse(exprStr) else null
            return DerivativeNode(expr = innerNode, variable = v, order = order, functionName = funcName)
        }

        // Check definite integral: int_0^1 x^2 dx or ∫₀¹ x² dx
        if (text.startsWith("∫") || text.startsWith("int")) {
            val intRegex = Regex("^(?:∫|int)(?:_([a-zA-Z0-9.]+))?(?:\\^([a-zA-Z0-9.]+))?\\s*(.*?)(?:\\s*d([a-zA-Z]))?$")
            val intMatch = intRegex.matchEntire(text)
            if (intMatch != null) {
                val lower = intMatch.groupValues[1].ifEmpty { null }?.let { UniversalMathParser.parse(it) }
                val upper = intMatch.groupValues[2].ifEmpty { null }?.let { UniversalMathParser.parse(it) }
                val bodyStr = intMatch.groupValues[3].ifEmpty { "x" }
                val varName = intMatch.groupValues[4].ifEmpty { "x" }
                return IntegralNode(
                    integrand = UniversalMathParser.parse(bodyStr),
                    variable = varName,
                    lowerLimit = lower,
                    upperLimit = upper
                )
            }
        }

        // Check limit: lim x->0 sin(x)/x
        if (text.startsWith("lim")) {
            val limRegex = Regex("^lim(?:_\\{?|\\s+)([a-zA-Z])\\s*(?:->|→|to)\\s*([a-zA-Z0-9.∞]+)\\}?\\s+(.*)$")
            val limMatch = limRegex.matchEntire(text)
            if (limMatch != null) {
                val v = limMatch.groupValues[1]
                val targetStr = limMatch.groupValues[2]
                val bodyStr = limMatch.groupValues[3]
                return LimitNode(
                    variable = v,
                    target = UniversalMathParser.parse(targetStr),
                    expr = UniversalMathParser.parse(bodyStr)
                )
            }
        }

        // Check summation: sum_{i=1}^n i or ∑ i=1 to n i
        if (text.startsWith("sum") || text.startsWith("∑")) {
            val sumRegex = Regex("^(?:sum|∑)(?:_\\{?|\\s*)([a-zA-Z0-9=]+)(?:\\^\\{?|\\s+to\\s+)([a-zA-Z0-9]+)\\}?\\s+(.*)$")
            val sumMatch = sumRegex.matchEntire(text)
            if (sumMatch != null) {
                val lowerStr = sumMatch.groupValues[1]
                val upperStr = sumMatch.groupValues[2]
                val bodyStr = sumMatch.groupValues[3]
                return BigOperatorNode(
                    type = BigOpType.SUM,
                    lower = UniversalMathParser.parse(lowerStr),
                    upper = UniversalMathParser.parse(upperStr),
                    body = UniversalMathParser.parse(bodyStr)
                )
            }
        }

        return parseExpression()
    }

    private fun parseExpression(): MathNode {
        var left = parseTerm()
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            val ch = text[pos]
            if (ch == '+' || ch == '-' || ch == '±' || ch == '∓') {
                pos++
                val right = parseTerm()
                left = RowNode(listOf(left, OperatorNode(ch.toString()), right))
            } else if (ch in "=<≠≤≥≈") {
                pos++
                val right = parseExpression()
                left = RowNode(listOf(left, OperatorNode(ch.toString()), right))
            } else {
                break
            }
        }
        return left
    }

    private fun parseTerm(): MathNode {
        var left = parseFactor()
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            val ch = text[pos]
            if (ch == '*' || ch == '×' || ch == '·') {
                pos++
                val right = parseFactor()
                left = RowNode(listOf(left, OperatorNode(ch.toString()), right))
            } else if (ch == '/' || ch == '÷') {
                pos++
                val right = parseFactor()
                // True vertical fraction
                left = FractionNode(left, right)
            } else if (isImplicitMul()) {
                val right = parseFactor()
                left = RowNode(listOf(left, right))
            } else {
                break
            }
        }
        return left
    }

    private fun isImplicitMul(): Boolean {
        if (pos >= text.length) return false
        val ch = text[pos]
        return ch.isLetter() || ch == '(' || ch == '|' || ch == '√' || ch == 'π' || ch == 'θ'
    }

    private fun parseFactor(): MathNode {
        var base = parseUnary()
        skipWhitespace()
        if (pos < text.length && text[pos] == '^') {
            pos++
            val exp = parseFactor()
            base = PowerNode(base, exp)
        } else if (pos < text.length && text[pos] == '_') {
            pos++
            val sub = parseFactor()
            base = SubscriptNode(base, sub)
        }
        return base
    }

    private fun parseUnary(): MathNode {
        skipWhitespace()
        if (pos < text.length) {
            val ch = text[pos]
            if (ch == '-') {
                pos++
                return RowNode(listOf(OperatorNode("-"), parseUnary()))
            }
            if (ch == '+') {
                pos++
                return parseUnary()
            }
        }
        return parsePrimary()
    }

    private fun parsePrimary(): MathNode {
        skipWhitespace()
        if (pos >= text.length) return NumberNode("0")
        val ch = text[pos]

        // Parentheses
        if (ch == '(') {
            pos++
            val inner = parseUntil(')')
            if (pos < text.length && text[pos] == ')') pos++
            return DelimitedNode(inner, DelimType.PAREN, DelimType.PAREN_CLOSE)
        }

        // Absolute value
        if (ch == '|') {
            pos++
            val inner = parseUntil('|')
            if (pos < text.length && text[pos] == '|') pos++
            return AbsNode(inner)
        }

        // Radical symbol √
        if (ch == '√') {
            pos++
            val inner = parsePrimary()
            return RadicalNode(inner)
        }

        // Numbers
        if (ch.isDigit() || (ch == '.' && pos + 1 < text.length && text[pos + 1].isDigit())) {
            val start = pos
            while (pos < text.length && (text[pos].isDigit() || text[pos] == '.')) pos++
            val numStr = text.substring(start, pos)
            // Check scientific exponent: 1e-4
            if (pos < text.length && (text[pos] == 'e' || text[pos] == 'E')) {
                pos++
                val expStart = pos
                if (pos < text.length && (text[pos] == '+' || text[pos] == '-')) pos++
                while (pos < text.length && text[pos].isDigit()) pos++
                val expStr = text.substring(expStart, pos)
                return ScientificNode(numStr, expStr)
            }
            return NumberNode(numStr)
        }

        // Functions and identifiers
        if (ch.isLetter() || ch == 'π' || ch == 'θ' || ch == 'α' || ch == 'β' || ch == 'γ' || ch == '∞') {
            val start = pos
            while (pos < text.length && (text[pos].isLetter() || text[pos].isDigit())) pos++
            val ident = text.substring(start, pos)

            when (ident.lowercase(Locale.US)) {
                "pi", "π" -> return SymbolNode("π", "pi", "\\pi")
                "theta", "θ" -> return SymbolNode("θ", "theta", "\\theta")
                "alpha", "α" -> return SymbolNode("α", "alpha", "\\alpha")
                "beta", "β" -> return SymbolNode("β", "beta", "\\beta")
                "gamma", "γ" -> return if (ident == "Gamma" || ident == "Γ") SymbolNode("Γ", "Gamma", "\\Gamma") else SymbolNode("γ", "gamma", "\\gamma")
                "delta", "δ" -> return if (ident == "Delta" || ident == "Δ") SymbolNode("Δ", "Delta", "\\Delta") else SymbolNode("δ", "delta", "\\delta")
                "lambda", "λ" -> return if (ident == "Lambda" || ident == "Λ") SymbolNode("Λ", "Lambda", "\\Lambda") else SymbolNode("λ", "lambda", "\\lambda")
                "sigma", "σ" -> return if (ident == "Sigma" || ident == "Σ") SymbolNode("Σ", "Sigma", "\\Sigma") else SymbolNode("σ", "sigma", "\\sigma")
                "omega", "ω" -> return if (ident == "Omega" || ident == "Ω") SymbolNode("Ω", "Omega", "\\Omega") else SymbolNode("ω", "omega", "\\omega")
                "mu", "μ" -> return SymbolNode("μ", "mu", "\\mu")
                "phi", "ϕ", "φ" -> return if (ident == "Phi" || ident == "Φ") SymbolNode("Φ", "Phi", "\\Phi") else SymbolNode("φ", "phi", "\\phi")
                "infinity", "inf", "∞" -> return SymbolNode("∞", "infinity", "\\infty")
                "sqrt" -> {
                    val arg = parsePrimary()
                    return RadicalNode(arg)
                }
                "cbrt" -> {
                    val arg = parsePrimary()
                    return RadicalNode(arg, NumberNode("3"))
                }
                "abs" -> {
                    val arg = parsePrimary()
                    return AbsNode(arg)
                }
                "sin", "cos", "tan", "asin", "acos", "atan", "arcsin", "arccos", "arctan",
                "cot", "sec", "csc", "cosec", "acot", "asec", "acsc", "acosec", "arccot", "arcsec", "arccsc", "arccosec",
                "sinh", "cosh", "tanh", "coth", "sech", "csch", "ln", "log", "log10", "exp" -> {
                    // Check power: sin^2(x) or sin^-1(x)
                    var power: MathNode? = null
                    var base: MathNode? = null
                    skipWhitespace()
                    if (pos < text.length && text[pos] == '^') {
                        pos++
                        power = parseFactor()
                    } else if (pos < text.length && text[pos] == '_') {
                        pos++
                        base = parseFactor()
                    }
                    val arg = parsePrimary()
                    val normIdent = when (ident.lowercase(Locale.US)) {
                        "arcsin", "asin" -> { power = NumberNode("-1"); "sin" }
                        "arccos", "acos" -> { power = NumberNode("-1"); "cos" }
                        "arctan", "atan" -> { power = NumberNode("-1"); "tan" }
                        "arccot", "acot" -> { power = NumberNode("-1"); "cot" }
                        "arcsec", "asec" -> { power = NumberNode("-1"); "sec" }
                        "arccsc", "acsc" -> { power = NumberNode("-1"); "csc" }
                        "arccosec", "acosec" -> { power = NumberNode("-1"); "cosec" }
                        else -> ident
                    }
                    return FunctionNode(normIdent, arg, power, base)
                }
                else -> {
                    // Check subscript: a1 or x0
                    if (ident.length > 1 && ident[0].isLetter() && ident.substring(1).all { it.isDigit() }) {
                        return SubscriptNode(VariableNode(ident.take(1)), NumberNode(ident.substring(1)))
                    }
                    return VariableNode(ident)
                }
            }
        }

        // Fallback single character
        pos++
        return VariableNode(ch.toString())
    }

    private fun parseUntil(delimiter: Char): MathNode {
        val start = pos
        var depth = 0
        while (pos < text.length) {
            val c = text[pos]
            if (c == '(' || c == '[' || c == '{') depth++
            else if (c == ')' || c == ']' || c == '}') depth--
            if (depth < 0 || (depth == 0 && c == delimiter)) break
            pos++
        }
        val sub = text.substring(start, pos)
        return UniversalMathParser.parse(sub)
    }

    private fun skipWhitespace() {
        while (pos < text.length && text[pos].isWhitespace()) pos++
    }
}
