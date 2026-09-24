package com.example.math.renderer

import com.example.math.calculus.*

/**
 * Delimiter types for brackets and delimiters.
 */
enum class DelimType(val glyph: String) {
    PAREN("("),
    PAREN_CLOSE(")"),
    BRACKET("["),
    BRACKET_CLOSE("]"),
    BRACE("{"),
    BRACE_CLOSE("}"),
    BAR("|"),
    DOUBLE_BAR("‖"),
    ANGLE("⟨"),
    ANGLE_CLOSE("⟩")
}

/**
 * Big operator symbols for calculus and discrete math.
 */
enum class BigOpType(val symbol: String, val accessibleName: String) {
    SUM("∑", "Summation"),
    PRODUCT("∏", "Product"),
    INTEGRAL("∫", "Integral"),
    CONTOUR_INTEGRAL("∮", "Contour Integral")
}

/**
 * Chemical formula element/group component.
 */
sealed interface ChemPart {
    data class Element(val symbol: String, val count: Int? = null, val charge: String? = null) : ChemPart
    data class Group(val parts: List<ChemPart>, val count: Int? = null, val charge: String? = null) : ChemPart
}

/**
 * Canonical Mathematical AST Node.
 * Single source of truth for all mathematical expressions and results in CALCX.
 */
sealed interface MathNode {
    fun toAccessibleText(): String
    fun toLatex(): String
    fun toPlainText(): String
}

/**
 * Numeric constant, decimal, integer.
 */
data class NumberNode(val text: String) : MathNode {
    override fun toAccessibleText(): String = text
    override fun toLatex(): String = text
    override fun toPlainText(): String = text
}

/**
 * Scientific notation, e.g. 6.022 × 10²³.
 */
data class ScientificNode(val mantissa: String, val exponent: String) : MathNode {
    override fun toAccessibleText(): String = "$mantissa times 10 to the power of $exponent"
    override fun toLatex(): String = "$mantissa \\times 10^{$exponent}"
    override fun toPlainText(): String = "$mantissa × 10^$exponent"
}

/**
 * Variable identifier, e.g. x, y, θ, with optional subscript, primes, or vector arrow.
 */
data class VariableNode(
    val name: String,
    val isItalic: Boolean = true,
    val subscript: MathNode? = null,
    val primes: Int = 0,
    val isVector: Boolean = false
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        if (isVector) append("vector ")
        append(name)
        if (primes > 0) append(" prime".repeat(primes))
        if (subscript != null) append(" sub ${subscript.toAccessibleText()}")
    }

    override fun toLatex(): String = buildString {
        if (isVector) append("\\vec{")
        append(name)
        if (isVector) append("}")
        if (primes > 0) append("'".repeat(primes))
        if (subscript != null) append("_{${subscript.toLatex()}}")
    }

    override fun toPlainText(): String = buildString {
        if (isVector) append("→")
        append(name)
        if (primes > 0) append("'".repeat(primes))
        if (subscript != null) append("_(${subscript.toPlainText()})")
    }
}

/**
 * Well-known mathematical or physical constants.
 */
data class SymbolNode(val symbol: String, val accessibleName: String, val latexCommand: String? = null) : MathNode {
    override fun toAccessibleText(): String = accessibleName
    override fun toLatex(): String = latexCommand ?: symbol
    override fun toPlainText(): String = symbol
}

/**
 * Mathematical operator, e.g. +, -, *, ·, ×, ÷, =, ≠, <, >, ≤, ≥, ≈, ±, ∓.
 */
data class OperatorNode(val op: String) : MathNode {
    override fun toAccessibleText(): String = when (op) {
        "+" -> "plus"
        "-" -> "minus"
        "±" -> "plus or minus"
        "∓" -> "minus or plus"
        "·", "×", "*" -> "times"
        "÷", "/" -> "divided by"
        "=" -> "equals"
        "≠" -> "does not equal"
        "<" -> "less than"
        ">" -> "greater than"
        "≤", "<=" -> "less than or equal to"
        "≥", ">=" -> "greater than or equal to"
        "≈" -> "approximately equal to"
        "→", "⟶" -> "approaches"
        "⟹" -> "implies"
        else -> op
    }

    override fun toLatex(): String = when (op) {
        "·" -> "\\cdot"
        "×" -> "\\times"
        "÷" -> "\\div"
        "±" -> "\\pm"
        "∓" -> "\\mp"
        "≤", "<=" -> "\\le"
        "≥", ">=" -> "\\ge"
        "≠" -> "\\neq"
        "≈" -> "\\approx"
        "→", "⟶" -> "\\to"
        "⟹" -> "\\implies"
        else -> op
    }

    override fun toPlainText(): String = op
}

/**
 * Vertical fraction with numerator above horizontal fraction bar and denominator below.
 */
data class FractionNode(
    val numerator: MathNode,
    val denominator: MathNode,
    val isDerivativeStyle: Boolean = false
) : MathNode {
    override fun toAccessibleText(): String =
        "fraction with numerator ${numerator.toAccessibleText()} over denominator ${denominator.toAccessibleText()}"

    override fun toLatex(): String = "\\frac{${numerator.toLatex()}}{${denominator.toLatex()}}"

    override fun toPlainText(): String = "(${numerator.toPlainText()})/(${denominator.toPlainText()})"
}

/**
 * Power/superscript node, e.g. x², e^(x+1).
 */
data class PowerNode(val base: MathNode, val exponent: MathNode) : MathNode {
    override fun toAccessibleText(): String =
        "${base.toAccessibleText()} to the power of ${exponent.toAccessibleText()}"

    override fun toLatex(): String = "${base.toLatex()}^{${exponent.toLatex()}}"

    override fun toPlainText(): String = "${base.toPlainText()}^(${exponent.toPlainText()})"
}

/**
 * Subscript node, e.g. a₁, x₀.
 */
data class SubscriptNode(val base: MathNode, val sub: MathNode) : MathNode {
    override fun toAccessibleText(): String =
        "${base.toAccessibleText()} sub ${sub.toAccessibleText()}"

    override fun toLatex(): String = "${base.toLatex()}_{${sub.toLatex()}}"

    override fun toPlainText(): String = "${base.toPlainText()}_(${sub.toPlainText()})"
}

/**
 * Tandem power and subscript node, e.g. x_1^2, sin^2(x).
 */
data class PowerSubscriptNode(
    val base: MathNode,
    val sub: MathNode? = null,
    val sup: MathNode? = null
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        append(base.toAccessibleText())
        if (sub != null) append(" sub ${sub.toAccessibleText()}")
        if (sup != null) append(" to the power of ${sup.toAccessibleText()}")
    }

    override fun toLatex(): String = buildString {
        append(base.toLatex())
        if (sub != null) append("_{${sub.toLatex()}}")
        if (sup != null) append("^{${sup.toLatex()}}")
    }

    override fun toPlainText(): String = buildString {
        append(base.toPlainText())
        if (sub != null) append("_${sub.toPlainText()}")
        if (sup != null) append("^${sup.toPlainText()}")
    }
}

/**
 * Radical / Root node with dynamic vinculum extending over child content, e.g. √x, ³√(x²+1).
 */
data class RadicalNode(val content: MathNode, val index: MathNode? = null) : MathNode {
    override fun toAccessibleText(): String = if (index != null) {
        "${index.toAccessibleText()} root of ${content.toAccessibleText()}"
    } else {
        "square root of ${content.toAccessibleText()}"
    }

    override fun toLatex(): String = if (index != null) {
        "\\sqrt[${index.toLatex()}]{${content.toLatex()}}"
    } else {
        "\\sqrt{${content.toLatex()}}"
    }

    override fun toPlainText(): String = if (index != null) {
        "${index.toPlainText()}√(${content.toPlainText()})"
    } else {
        "√(${content.toPlainText()})"
    }
}

/**
 * Delimited expression with scaling parentheses, brackets, or braces.
 */
data class DelimitedNode(
    val inner: MathNode,
    val leftDelim: DelimType = DelimType.PAREN,
    val rightDelim: DelimType = DelimType.PAREN_CLOSE
) : MathNode {
    override fun toAccessibleText(): String = "quantity ${inner.toAccessibleText()}"
    override fun toLatex(): String = "\\left${leftDelim.glyph}${inner.toLatex()}\\right${rightDelim.glyph}"
    override fun toPlainText(): String = "${leftDelim.glyph}${inner.toPlainText()}${rightDelim.glyph}"
}

/**
 * Absolute value with dynamically scaled vertical bars, e.g. |x + 1|.
 */
data class AbsNode(val inner: MathNode) : MathNode {
    override fun toAccessibleText(): String = "absolute value of ${inner.toAccessibleText()}"
    override fun toLatex(): String = "\\left|${inner.toLatex()}\\right|"
    override fun toPlainText(): String = "|${inner.toPlainText()}|"
}

/**
 * Function call node, e.g. sin(x), log₂(x), ln(x), tan⁻¹(x).
 */
data class FunctionNode(
    val name: String,
    val argument: MathNode,
    val power: MathNode? = null,
    val base: MathNode? = null
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        append(name)
        if (base != null) append(" base ${base.toAccessibleText()}")
        if (power != null) append(" to the power of ${power.toAccessibleText()}")
        append(" of ${argument.toAccessibleText()}")
    }

    override fun toLatex(): String = buildString {
        when (name.lowercase()) {
            "asin", "arcsin" -> append("\\sin^{-1}")
            "acos", "arccos" -> append("\\cos^{-1}")
            "atan", "arctan" -> append("\\tan^{-1}")
            "acot", "arccot" -> append("\\cot^{-1}")
            "asec", "arcsec" -> append("\\sec^{-1}")
            "acsc", "arccsc", "acosec", "arccosec" -> append("\\csc^{-1}")
            "sin", "cos", "tan", "cot", "sec", "csc", "sinh", "cosh", "tanh", "coth", "sech", "csch", "ln", "exp" -> append("\\$name")
            "cosec" -> append("\\operatorname{cosec}")
            "log", "log10" -> append("\\log")
            else -> append("\\text{$name}")
        }
        if (base != null) append("_{${base.toLatex()}}")
        if (power != null && !name.lowercase().startsWith("a") && !name.lowercase().startsWith("arc")) {
            append("^{${power.toLatex()}}")
        }
        append("\\left(${argument.toLatex()}\\right)")
    }

    override fun toPlainText(): String = buildString {
        val displayName = when (name.lowercase()) {
            "asin", "arcsin" -> "sin⁻¹"
            "acos", "arccos" -> "cos⁻¹"
            "atan", "arctan" -> "tan⁻¹"
            "acot", "arccot" -> "cot⁻¹"
            "asec", "arcsec" -> "sec⁻¹"
            "acsc", "arccsc", "acosec", "arccosec" -> "csc⁻¹"
            else -> name
        }
        append(displayName)
        if (base != null) append("_${base.toPlainText()}")
        if (power != null && !name.lowercase().startsWith("a") && !name.lowercase().startsWith("arc")) {
            append("^(${power.toPlainText()})")
        }
        append("(${argument.toPlainText()})")
    }
}

/**
 * Calculus derivative notation, e.g. d/dx (x²), d²y/dx², ∂f/∂x.
 */
data class DerivativeNode(
    val expr: MathNode?,
    val variable: String = "x",
    val order: Int = 1,
    val isPartial: Boolean = false,
    val functionName: String? = null
) : MathNode {
    private val dSym = if (isPartial) "∂" else "d"

    override fun toAccessibleText(): String = buildString {
        if (order == 1) append("derivative ")
        else append("${order}th derivative ")
        if (functionName != null) append("of $functionName ")
        append("with respect to $variable")
        if (expr != null) append(" of ${expr.toAccessibleText()}")
    }

    override fun toLatex(): String = buildString {
        val num = if (order > 1) "\\frac{$dSym^$order" else "\\frac{$dSym"
        val func = functionName ?: ""
        val den = if (order > 1) "{$dSym $variable^$order}" else "{$dSym $variable}"
        append("$num $func}$den")
        if (expr != null) append("\\left(${expr.toLatex()}\\right)")
    }

    override fun toPlainText(): String = buildString {
        val num = if (order > 1) "$dSym^$order${functionName ?: ""}" else "$dSym${functionName ?: ""}"
        val den = if (order > 1) "$dSym$variable^$order" else "$dSym$variable"
        append("$num/$den")
        if (expr != null) append("(${expr.toPlainText()})")
    }
}

/**
 * Calculus integral notation, e.g. ∫ f(x) dx, ∫₀¹ x² dx.
 */
data class IntegralNode(
    val integrand: MathNode,
    val variable: String = "x",
    val lowerLimit: MathNode? = null,
    val upperLimit: MathNode? = null
) : MathNode {
    val isDefinite: Boolean get() = lowerLimit != null || upperLimit != null

    override fun toAccessibleText(): String = buildString {
        if (isDefinite) {
            append("definite integral from ")
            append(lowerLimit?.toAccessibleText() ?: "negative infinity")
            append(" to ")
            append(upperLimit?.toAccessibleText() ?: "infinity")
            append(" of ")
        } else {
            append("indefinite integral of ")
        }
        append(integrand.toAccessibleText())
        append(" d $variable")
    }

    override fun toLatex(): String = buildString {
        append("\\int")
        if (lowerLimit != null) append("_{${lowerLimit.toLatex()}}")
        if (upperLimit != null) append("^{${upperLimit.toLatex()}}")
        append(" ${integrand.toLatex()} \\, d$variable")
    }

    override fun toPlainText(): String = buildString {
        append("∫")
        if (lowerLimit != null) append("_{${lowerLimit.toPlainText()}}")
        if (upperLimit != null) append("^{${upperLimit.toPlainText()}}")
        append(" (${integrand.toPlainText()}) d$variable")
    }
}

/**
 * Limit notation, e.g. lim_{x → a⁺} f(x).
 */
data class LimitNode(
    val variable: String = "x",
    val target: MathNode,
    val expr: MathNode,
    val direction: String? = null // "+", "-", or null
) : MathNode {
    override fun toAccessibleText(): String =
        "limit as $variable approaches ${target.toAccessibleText()}${if (direction != null) " from $direction" else ""} of ${expr.toAccessibleText()}"

    override fun toLatex(): String =
        "\\lim_{$variable \\to ${target.toLatex()}${if (direction != null) "^{$direction}" else ""}} ${expr.toLatex()}"

    override fun toPlainText(): String =
        "lim($variable → ${target.toPlainText()}${direction ?: ""}) ${expr.toPlainText()}"
}

/**
 * Big operators like Summation ∑ and Product ∏ with upper and lower limits.
 */
data class BigOperatorNode(
    val type: BigOpType,
    val lower: MathNode? = null,
    val upper: MathNode? = null,
    val body: MathNode
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        append("${type.accessibleName} ")
        if (lower != null) append("from ${lower.toAccessibleText()} ")
        if (upper != null) append("to ${upper.toAccessibleText()} ")
        append("of ${body.toAccessibleText()}")
    }

    override fun toLatex(): String = buildString {
        append(when (type) {
            BigOpType.SUM -> "\\sum"
            BigOpType.PRODUCT -> "\\prod"
            BigOpType.INTEGRAL -> "\\int"
            BigOpType.CONTOUR_INTEGRAL -> "\\oint"
        })
        if (lower != null) append("_{${lower.toLatex()}}")
        if (upper != null) append("^{${upper.toLatex()}}")
        append(" ${body.toLatex()}")
    }

    override fun toPlainText(): String = buildString {
        append(type.symbol)
        if (lower != null) append("_{${lower.toPlainText()}}")
        if (upper != null) append("^{${upper.toPlainText()}}")
        append(" (${body.toPlainText()})")
    }
}

/**
 * Matrix or Determinant node with nxm cells and brackets.
 */
data class MatrixNode(
    val rows: Int,
    val cols: Int,
    val cells: List<List<MathNode>>,
    val isDeterminant: Boolean = false
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        if (isDeterminant) append("determinant of ")
        append("$rows by $cols matrix: ")
        cells.forEachIndexed { r, row ->
            append("row ${r + 1}: ")
            row.forEachIndexed { c, cell ->
                append(cell.toAccessibleText())
                if (c < row.size - 1) append(", ")
            }
            append("; ")
        }
    }

    override fun toLatex(): String = buildString {
        val env = if (isDeterminant) "vmatrix" else "bmatrix"
        append("\\begin{$env}\n")
        cells.forEachIndexed { r, row ->
            append(row.joinToString(" & ") { it.toLatex() })
            if (r < cells.size - 1) append(" \\\\\n")
        }
        append("\n\\end{$env}")
    }

    override fun toPlainText(): String = buildString {
        val open = if (isDeterminant) "|" else "["
        val close = if (isDeterminant) "|" else "]"
        append(open)
        cells.forEachIndexed { r, row ->
            append(row.joinToString(" ") { it.toPlainText() })
            if (r < cells.size - 1) append("; ")
        }
        append(close)
    }
}

/**
 * Vector representation, e.g. v⃗ or column vector.
 */
data class VectorNode(
    val name: String? = null,
    val components: List<MathNode>? = null
) : MathNode {
    override fun toAccessibleText(): String = buildString {
        if (name != null) append("vector $name ")
        if (components != null) {
            append("with components (${components.joinToString(", ") { it.toAccessibleText() }})")
        }
    }

    override fun toLatex(): String = buildString {
        if (name != null) append("\\vec{$name}")
        if (components != null) {
            append("\\begin{pmatrix}")
            append(components.joinToString(" \\\\ ") { it.toLatex() })
            append("\\end{pmatrix}")
        }
    }

    override fun toPlainText(): String = buildString {
        if (name != null) append("→$name")
        if (components != null) {
            append("⟨${components.joinToString(", ") { it.toPlainText() }}⟩")
        }
    }
}

/**
 * Complex number node: a + bi.
 */
data class ComplexNode(
    val real: MathNode,
    val imag: MathNode,
    val sign: String = "+"
) : MathNode {
    override fun toAccessibleText(): String =
        "${real.toAccessibleText()} $sign ${imag.toAccessibleText()} i"

    override fun toLatex(): String =
        "${real.toLatex()} $sign ${imag.toLatex()} i"

    override fun toPlainText(): String =
        "${real.toPlainText()} $sign ${imag.toPlainText()}i"
}

/**
 * Chemical formula node, e.g. H₂SO₄, KMnO₄, SO₄²⁻.
 */
data class ChemNode(val parts: List<ChemPart>) : MathNode {
    override fun toAccessibleText(): String = buildString {
        fun describe(list: List<ChemPart>) {
            list.forEach { p ->
                when (p) {
                    is ChemPart.Element -> {
                        append(p.symbol)
                        if (p.count != null) append(p.count)
                        if (p.charge != null) append(" ${p.charge}")
                        append(" ")
                    }
                    is ChemPart.Group -> {
                        append("group ")
                        describe(p.parts)
                        if (p.count != null) append("sub ${p.count} ")
                        if (p.charge != null) append("charge ${p.charge} ")
                    }
                }
            }
        }
        describe(parts)
    }.trim()

    override fun toLatex(): String = buildString {
        fun format(list: List<ChemPart>) {
            list.forEach { p ->
                when (p) {
                    is ChemPart.Element -> {
                        append("\\text{${p.symbol}}")
                        if (p.count != null) append("_{${p.count}}")
                        if (p.charge != null) append("^{${p.charge}}")
                    }
                    is ChemPart.Group -> {
                        append("(")
                        format(p.parts)
                        append(")")
                        if (p.count != null) append("_{${p.count}}")
                        if (p.charge != null) append("^{${p.charge}}")
                    }
                }
            }
        }
        format(parts)
    }

    override fun toPlainText(): String = buildString {
        fun format(list: List<ChemPart>) {
            list.forEach { p ->
                when (p) {
                    is ChemPart.Element -> {
                        append(p.symbol)
                        if (p.count != null) append(toSubscriptDigits(p.count))
                        if (p.charge != null) append(toSuperscriptChars(p.charge))
                    }
                    is ChemPart.Group -> {
                        append("(")
                        format(p.parts)
                        append(")")
                        if (p.count != null) append(toSubscriptDigits(p.count))
                        if (p.charge != null) append(toSuperscriptChars(p.charge))
                    }
                }
            }
        }
        format(parts)
    }

    companion object {
        fun toSubscriptDigits(num: Int): String {
            val subMap = mapOf('0' to '₀', '1' to '₁', '2' to '₂', '3' to '₃', '4' to '₄',
                '5' to '₅', '6' to '₆', '7' to '₇', '8' to '₈', '9' to '₉')
            return num.toString().map { subMap[it] ?: it }.joinToString("")
        }

        fun toSuperscriptChars(text: String): String {
            val supMap = mapOf('0' to '⁰', '1' to '¹', '2' to '²', '3' to '³', '4' to '⁴',
                '5' to '⁵', '6' to '⁶', '7' to '⁷', '8' to '⁸', '9' to '⁹', '+' to '⁺', '-' to '⁻')
            return text.map { supMap[it] ?: it }.joinToString("")
        }
    }
}

/**
 * Value with mathematical/physical units, e.g. 9.8 m/s², 20 J.
 */
data class UnitNode(val value: MathNode, val unitText: String) : MathNode {
    override fun toAccessibleText(): String = "${value.toAccessibleText()} $unitText"
    override fun toLatex(): String = "${value.toLatex()} \\text{ $unitText}"
    override fun toPlainText(): String = "${value.toPlainText()} $unitText"
}

/**
 * Horizontal sequence of nodes sitting on a shared baseline.
 */
data class RowNode(val children: List<MathNode>) : MathNode {
    override fun toAccessibleText(): String = children.joinToString(" ") { it.toAccessibleText() }
    override fun toLatex(): String = children.joinToString(" ") { it.toLatex() }
    override fun toPlainText(): String = children.joinToString(" ") { it.toPlainText() }
}

/**
 * Plain text / label node.
 */
data class TextNode(
    val text: String,
    val isItalic: Boolean = false,
    val isBold: Boolean = false
) : MathNode {
    override fun toAccessibleText(): String = text
    override fun toLatex(): String = "\\text{$text}"
    override fun toPlainText(): String = text
}
