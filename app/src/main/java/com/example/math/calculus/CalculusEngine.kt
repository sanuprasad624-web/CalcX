package com.example.math.calculus

import com.example.math.Fraction
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.*

data class DifferentiationResult(
    val originalExpr: String,
    val variable: String,
    val order: Int,
    val derivativeExpr: Expr,
    val derivativeLatex: String,
    val derivativeDisplay: String,
    val evaluatedAtPoint: Double? = null,
    val pointValueExact: String? = null,
    val pointValueDecimal: Double? = null,
    val steps: List<String> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class IndefiniteIntegralResult(
    val originalExpr: String,
    val variable: String,
    val antiderivativeExpr: Expr?,
    val resultLatex: String,
    val resultDisplay: String,
    val steps: List<String> = emptyList(),
    val isSymbolic: Boolean = true,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

data class DefiniteIntegralResult(
    val originalExpr: String,
    val variable: String,
    val lowerLimit: Double,
    val upperLimit: Double,
    val value: Double,
    val formattedDecimal: String,
    val exactResult: String? = null,
    val isExact: Boolean = false,
    val method: String = "Exact (Fundamental Theorem)",
    val steps: List<String> = emptyList(),
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)

object CalculusEngine {

    /**
     * Differentiate f(x) with respect to variable [variable] up to order [order].
     * Generates comprehensive, multi-step mathematical derivations.
     */
    fun differentiate(
        expressionStr: String,
        variable: String = "x",
        order: Int = 1,
        atPoint: Double? = null
    ): DifferentiationResult {
        try {
            val ast = CalculusParser.parse(expressionStr)
            val steps = mutableListOf<String>()

            steps.add("\\text{Given function: } f($variable) = ${ast.toLatex()}")

            var current = ast
            for (i in 1..order) {
                val orderLabel = when (i) {
                    1 -> "f'($variable) = \\frac{d}{d$variable}\\left[ f($variable) \\right]"
                    2 -> "f''($variable) = \\frac{d^2}{d$variable^2}\\left[ f($variable) \\right]"
                    3 -> "f'''($variable) = \\frac{d^3}{d$variable^3}\\left[ f($variable) \\right]"
                    else -> "f^{($i)}($variable) = \\frac{d^$i}{d$variable^$i}\\left[ f($variable) \\right]"
                }

                if (order > 1) {
                    steps.add("\\textbf{Step } $i: \\text{Computing } $orderLabel")
                }

                // Generate detailed derivation steps for this differentiation pass
                val passSteps = generateDiffStepsForExpr(current, variable)
                steps.addAll(passSteps)

                val next = current.differentiate(variable).simplify()
                steps.add("\\textbf{Simplified derivative: } $orderLabel = ${next.toLatex()}")
                current = next
            }

            val finalExpr = current
            val latex = finalExpr.toLatex()
            val display = finalExpr.toDisplayString()

            var ptValueExact: String? = null
            var ptValueDec: Double? = null

            if (atPoint != null) {
                val v = finalExpr.eval(mapOf(variable to atPoint))
                ptValueDec = v
                val rounded = Math.round(v)
                ptValueExact = if (abs(v - rounded) < 1e-10) {
                    rounded.toString()
                } else {
                    val frac = Fraction.fromDouble(v)
                    if (frac.denominator.toLong() in 2..1000) {
                        frac.toProperString()
                    } else {
                        String.format(java.util.Locale.US, "%.6f", v).trimEnd('0').trimEnd('.')
                    }
                }
                steps.add("\\textbf{Evaluation at } $variable = $atPoint:")
                steps.add("f^{($order)}($atPoint) = ${formatSubstitution(finalExpr, variable, atPoint)} = $ptValueExact \\; (\\approx ${String.format(java.util.Locale.US, "%.6f", v)})")
            }

            return DifferentiationResult(
                originalExpr = expressionStr,
                variable = variable,
                order = order,
                derivativeExpr = finalExpr,
                derivativeLatex = latex,
                derivativeDisplay = display,
                evaluatedAtPoint = atPoint,
                pointValueExact = ptValueExact,
                pointValueDecimal = ptValueDec,
                steps = steps,
                isSuccess = true
            )
        } catch (e: Exception) {
            return DifferentiationResult(
                originalExpr = expressionStr,
                variable = variable,
                order = order,
                derivativeExpr = Constant(0.0),
                derivativeLatex = "",
                derivativeDisplay = "",
                isSuccess = false,
                errorMessage = e.message ?: "Failed to compute derivative"
            )
        }
    }

    /**
     * Helper to recursively break down differentiation steps of an expression AST.
     */
    private fun generateDiffStepsForExpr(expr: Expr, v: String): List<String> {
        val steps = mutableListOf<String>()
        val terms = extractAddSubTerms(expr)

        if (terms.size > 1) {
            val termsExpansion = terms.joinToString(" ") { (isPos, t) ->
                val sign = if (isPos) "+" else "-"
                "$sign \\frac{d}{d$v}\\left[ ${t.toLatex()} \\right]"
            }.removePrefix("+ ")

            steps.add("\\text{Apply Sum/Difference Rule: } \\frac{d}{d$v}\\left[ ${expr.toLatex()} \\right] = $termsExpansion")

            val termDerivatives = mutableListOf<String>()
            terms.forEachIndexed { idx, (isPos, t) ->
                val termStep = explainSingleTermDerivative(t, v)
                steps.add("\\text{Term } ${idx + 1}: $termStep")
                val dt = t.differentiate(v).simplify().toLatex()
                val sign = if (isPos) "+" else "-"
                termDerivatives.add("$sign $dt")
            }

            val unsimpCombined = termDerivatives.joinToString(" ").removePrefix("+ ")
            steps.add("\\text{Combine derivatives: } \\frac{d}{d$v}\\left[ ${expr.toLatex()} \\right] = $unsimpCombined")
        } else {
            steps.add(explainSingleTermDerivative(expr, v))
        }

        return steps
    }

    /**
     * Explains the differentiation rule applied to a single term.
     */
    private fun explainSingleTermDerivative(expr: Expr, v: String): String {
        return when (expr) {
            is Constant -> {
                "\\text{Constant Rule: } \\frac{d}{d$v}\\left[ ${expr.toLatex()} \\right] = 0"
            }
            is Variable -> {
                if (expr.name == v) {
                    "\\text{Linear Power Rule: } \\frac{d}{d$v}\\left[ $v \\right] = 1"
                } else {
                    "\\text{Treating } ${expr.name} \\text{ as constant: } \\frac{d}{d$v}\\left[ ${expr.name} \\right] = 0"
                }
            }
            is Pow -> {
                if (expr.base is Variable && expr.base.name == v && expr.exp is Constant) {
                    val n = expr.exp.value
                    val nMinus1 = n - 1
                    val nMinus1Str = if (nMinus1 == 1.0) "" else if (abs(nMinus1 - Math.round(nMinus1)) < 1e-9) "^{${Math.round(nMinus1)}}" else "^{$nMinus1}"
                    val nStr = if (abs(n - Math.round(n)) < 1e-9) "${Math.round(n)}" else "$n"
                    "\\text{Power Rule } \\left(\\frac{d}{d$v}[$v^n] = n $v^{n-1}\\right): \\frac{d}{d$v}\\left[ $v^{${expr.exp.toLatex()}} \\right] = $nStr $v$nMinus1Str"
                } else if (expr.base is Constant && expr.exp is Variable && expr.exp.name == v) {
                    "\\text{Exponential Rule } \\left(\\frac{d}{d$v}[a^$v] = a^$v \\ln(a)\\right): \\frac{d}{d$v}\\left[ ${expr.base.toLatex()}^$v \\right] = ${expr.base.toLatex()}^$v \\ln(${expr.base.toLatex()})"
                } else {
                    val u = expr.base.toLatex()
                    val du = expr.base.differentiate(v).simplify().toLatex()
                    "\\text{Chain & Power Rule: } \\frac{d}{d$v}\\left[ \\left($u\\right)^{${expr.exp.toLatex()}} \\right] = ${expr.exp.toLatex()} \\left($u\\right)^{${expr.exp.toLatex()}-1} \\cdot \\left($du\\right)"
                }
            }
            is Mul -> {
                if (expr.left is Constant) {
                    val c = expr.left.toLatex()
                    val r = expr.right.toLatex()
                    val dr = expr.right.differentiate(v).simplify().toLatex()
                    "\\text{Constant Multiple Rule: } \\frac{d}{d$v}\\left[ $c \\cdot $r \\right] = $c \\cdot \\frac{d}{d$v}\\left[ $r \\right] = $c \\left($dr\\right)"
                } else if (expr.right is Constant) {
                    val c = expr.right.toLatex()
                    val l = expr.left.toLatex()
                    val dl = expr.left.differentiate(v).simplify().toLatex()
                    "\\text{Constant Multiple Rule: } \\frac{d}{d$v}\\left[ $l \\cdot $c \\right] = $c \\cdot \\frac{d}{d$v}\\left[ $l \\right] = $c \\left($dl\\right)"
                } else {
                    val u = expr.left.toLatex()
                    val w = expr.right.toLatex()
                    val du = expr.left.differentiate(v).simplify().toLatex()
                    val dw = expr.right.differentiate(v).simplify().toLatex()
                    "\\text{Product Rule } \\left(\\frac{d}{d$v}[u \\cdot w] = u'w + uw'\\right): \\frac{d}{d$v}\\left[ ($u)($w) \\right] = \\left($du\\right)($w) + ($u)\\left($dw\\right)"
                }
            }
            is Div -> {
                if (expr.right is Constant) {
                    val c = expr.right.toLatex()
                    val l = expr.left.toLatex()
                    val dl = expr.left.differentiate(v).simplify().toLatex()
                    "\\text{Constant Divisor Rule: } \\frac{d}{d$v}\\left[ \\frac{$l}{$c} \\right] = \\frac{1}{$c} \\frac{d}{d$v}\\left[ $l \\right] = \\frac{$dl}{$c}"
                } else {
                    val u = expr.left.toLatex()
                    val w = expr.right.toLatex()
                    val du = expr.left.differentiate(v).simplify().toLatex()
                    val dw = expr.right.differentiate(v).simplify().toLatex()
                    "\\text{Quotient Rule } \\left(\\frac{d}{d$v}\\left[\\frac{u}{w}\\right] = \\frac{u'w - uw'}{w^2}\\right): \\frac{d}{d$v}\\left[ \\frac{$u}{$w} \\right] = \\frac{\\left($du\\right)($w) - ($u)\\left($dw\\right)}{\\left($w\\right)^2}"
                }
            }
            is Func -> {
                val argLatex = expr.arg.toLatex()
                val dArg = expr.arg.differentiate(v).simplify()
                val dArgLatex = dArg.toLatex()
                val isSimpleVar = expr.arg is Variable && expr.arg.name == v

                when (expr.name.lowercase()) {
                    "sin" -> {
                        if (isSimpleVar) "\\text{Trig Rule: } \\frac{d}{d$v}\\left[ \\sin($v) \\right] = \\cos($v)"
                        else "\\text{Chain Rule with Sine: } \\frac{d}{d$v}\\left[ \\sin($argLatex) \\right] = \\cos($argLatex) \\cdot \\frac{d}{d$v}[$argLatex] = \\cos($argLatex) \\cdot ($dArgLatex)"
                    }
                    "cos" -> {
                        if (isSimpleVar) "\\text{Trig Rule: } \\frac{d}{d$v}\\left[ \\cos($v) \\right] = -\\sin($v)"
                        else "\\text{Chain Rule with Cosine: } \\frac{d}{d$v}\\left[ \\cos($argLatex) \\right] = -\\sin($argLatex) \\cdot \\frac{d}{d$v}[$argLatex] = -\\sin($argLatex) \\cdot ($dArgLatex)"
                    }
                    "tan" -> {
                        if (isSimpleVar) "\\text{Trig Rule: } \\frac{d}{d$v}\\left[ \\tan($v) \\right] = \\sec^2($v)"
                        else "\\text{Chain Rule with Tangent: } \\frac{d}{d$v}\\left[ \\tan($argLatex) \\right] = \\sec^2($argLatex) \\cdot ($dArgLatex)"
                    }
                    "ln" -> {
                        if (isSimpleVar) "\\text{Log Rule: } \\frac{d}{d$v}\\left[ \\ln($v) \\right] = \\frac{1}{$v}"
                        else "\\text{Chain Rule with Natural Log: } \\frac{d}{d$v}\\left[ \\ln($argLatex) \\right] = \\frac{1}{$argLatex} \\cdot ($dArgLatex)"
                    }
                    "exp" -> {
                        if (isSimpleVar) "\\text{Exponential Rule: } \\frac{d}{d$v}\\left[ e^{$v} \\right] = e^{$v}"
                        else "\\text{Chain Rule with Exponential: } \\frac{d}{d$v}\\left[ e^{$argLatex} \\right] = e^{$argLatex} \\cdot ($dArgLatex)"
                    }
                    "sqrt" -> {
                        if (isSimpleVar) "\\text{Square Root Rule: } \\frac{d}{d$v}\\left[ \\sqrt{$v} \\right] = \\frac{1}{2\\sqrt{$v}}"
                        else "\\text{Chain Rule with Square Root: } \\frac{d}{d$v}\\left[ \\sqrt{$argLatex} \\right] = \\frac{1}{2\\sqrt{$argLatex}} \\cdot ($dArgLatex)"
                    }
                    else -> "\\text{Function Rule: } \\frac{d}{d$v}\\left[ ${expr.name}($argLatex) \\right] = ${expr.name}'($argLatex) \\cdot ($dArgLatex)"
                }
            }
            is Neg -> {
                val inStep = explainSingleTermDerivative(expr.inner, v)
                "\\text{Negation Rule: } \\frac{d}{d$v}\\left[ -${expr.inner.toLatex()} \\right] = -\\frac{d}{d$v}\\left[ ${expr.inner.toLatex()} \\right] \\implies $inStep"
            }
            else -> "\\frac{d}{d$v}\\left[ ${expr.toLatex()} \\right] = ${expr.differentiate(v).simplify().toLatex()}"
        }
    }

    /**
     * Compute indefinite integral: ∫ f(x) dx with full step-by-step mathematical reasoning.
     */
    fun integrateIndefinite(
        expressionStr: String,
        variable: String = "x"
    ): IndefiniteIntegralResult {
        try {
            val ast = CalculusParser.parse(expressionStr)
            val steps = mutableListOf<String>()
            steps.add("\\text{Integrand: } f($variable) = ${ast.toLatex()}")
            steps.add("\\text{Set up integral: } I = \\int \\left( ${ast.toLatex()} \\right)\\,d$variable")

            val terms = extractAddSubTerms(ast)
            val antiderivative: Expr?

            if (terms.size > 1) {
                val expansionStr = terms.joinToString(" ") { (isPos, t) ->
                    val sign = if (isPos) "+" else "-"
                    "$sign \\int \\left(${t.toLatex()}\\right)\\,d$variable"
                }.removePrefix("+ ")

                steps.add("\\text{Apply Linearity / Sum Rule: } I = $expansionStr")

                val integratedTerms = mutableListOf<Expr>()
                var allSucceeded = true

                terms.forEachIndexed { idx, (isPos, t) ->
                    val termSteps = mutableListOf<String>()
                    val res = integrateSymbolic(t, variable, termSteps)
                    if (res != null) {
                        termSteps.forEach { steps.add("\\text{Term } ${idx + 1}: $it") }
                        val termRes = if (isPos) res else Neg(res)
                        integratedTerms.add(termRes)
                    } else {
                        allSucceeded = false
                    }
                }

                if (allSucceeded) {
                    var combined: Expr = integratedTerms.first()
                    for (k in 1 until integratedTerms.size) {
                        val t = integratedTerms[k]
                        combined = if (t is Neg) Sub(combined, t.inner) else Add(combined, t)
                    }
                    antiderivative = combined.simplify()
                } else {
                    antiderivative = integrateSymbolic(ast, variable, steps)
                }
            } else {
                antiderivative = integrateSymbolic(ast, variable, steps)
            }

            if (antiderivative != null) {
                val simp = antiderivative.simplify()
                val latex = "${simp.toLatex()} + C"
                val display = "${simp.toDisplayString()} + C"
                steps.add("\\textbf{Combine integrated terms: } F($variable) = ${simp.toLatex()}")
                steps.add("\\textbf{Final Antiderivative: } \\int f($variable)\\,d$variable = $latex")

                return IndefiniteIntegralResult(
                    originalExpr = expressionStr,
                    variable = variable,
                    antiderivativeExpr = simp,
                    resultLatex = latex,
                    resultDisplay = display,
                    steps = steps,
                    isSymbolic = true,
                    isSuccess = true
                )
            } else {
                steps.add("\\text{Note: No elementary closed-form symbolic antiderivative found in terms of standard algebraic/trigonometric functions.}")
                steps.add("\\text{Definite integration via adaptive Simpson's 1/3 numerical quadrature is supported.}")

                return IndefiniteIntegralResult(
                    originalExpr = expressionStr,
                    variable = variable,
                    antiderivativeExpr = null,
                    resultLatex = "\\int \\left(${ast.toLatex()}\\right)\\,d$variable",
                    resultDisplay = "∫ (${ast.toDisplayString()}) d$variable",
                    steps = steps,
                    isSymbolic = false,
                    isSuccess = true
                )
            }
        } catch (e: Exception) {
            return IndefiniteIntegralResult(
                originalExpr = expressionStr,
                variable = variable,
                antiderivativeExpr = null,
                resultLatex = "",
                resultDisplay = "",
                isSuccess = false,
                errorMessage = e.message ?: "Failed to integrate expression"
            )
        }
    }

    /**
     * Symbolic integration recursive matcher with detailed step explanations.
     */
    private fun integrateSymbolic(expr: Expr, v: String, steps: MutableList<String>): Expr? {
        when (expr) {
            is Constant -> {
                // ∫ k dx = k * x
                steps.add("\\text{Constant Rule: } \\int ${expr.toLatex()}\\,d$v = ${expr.toLatex()} $v")
                return Mul(expr, Variable(v))
            }
            is Variable -> {
                if (expr.name == v) {
                    // ∫ x dx = x^2 / 2
                    steps.add("\\text{Power Rule: } \\int $v\\,d$v = \\frac{$v^2}{2}")
                    return Div(Pow(expr, Constant(2.0)), Constant(2.0))
                } else {
                    steps.add("\\text{Treating } ${expr.name} \\text{ as constant: } \\int ${expr.name}\\,d$v = ${expr.name} $v")
                    return Mul(expr, Variable(v))
                }
            }
            is Add -> {
                val leftInt = integrateSymbolic(expr.left, v, steps) ?: return null
                val rightInt = integrateSymbolic(expr.right, v, steps) ?: return null
                return Add(leftInt, rightInt)
            }
            is Sub -> {
                val leftInt = integrateSymbolic(expr.left, v, steps) ?: return null
                val rightInt = integrateSymbolic(expr.right, v, steps) ?: return null
                return Sub(leftInt, rightInt)
            }
            is Neg -> {
                val innerInt = integrateSymbolic(expr.inner, v, steps) ?: return null
                return Neg(innerInt)
            }
            is Mul -> {
                // If left is constant: ∫ k * f(x) dx = k * ∫ f(x) dx
                if (expr.left is Constant) {
                    val c = expr.left.toLatex()
                    val r = expr.right.toLatex()
                    val dummy = mutableListOf<String>()
                    val rInt = integrateSymbolic(expr.right, v, dummy) ?: return null
                    steps.add("\\text{Constant Multiple Rule: } \\int $c \\cdot $r\\,d$v = $c \\int $r\\,d$v = $c \\left(${rInt.toLatex()}\\right)")
                    return Mul(expr.left, rInt)
                }
                // If right is constant
                if (expr.right is Constant) {
                    val c = expr.right.toLatex()
                    val l = expr.left.toLatex()
                    val dummy = mutableListOf<String>()
                    val lInt = integrateSymbolic(expr.left, v, dummy) ?: return null
                    steps.add("\\text{Constant Multiple Rule: } \\int $l \\cdot $c\\,d$v = $c \\int $l\\,d$v = $c \\left(${lInt.toLatex()}\\right)")
                    return Mul(expr.right, lInt)
                }

                // Integration by parts patterns:
                // x * sin(x) -> -x cos(x) + sin(x)
                if (expr.left is Variable && expr.left.name == v && expr.right is Func && expr.right.arg is Variable && expr.right.arg.name == v) {
                    when (expr.right.name.lowercase()) {
                        "sin" -> {
                            steps.add("\\text{Integration by Parts } \\left(\\int u\\,dv = uv - \\int v\\,du\\right):")
                            steps.add("\\text{Let } u = $v \\implies du = d$v, \\quad dv = \\sin($v)d$v \\implies v = -\\cos($v)")
                            steps.add("\\int $v \\sin($v)\\,d$v = ($v)(-\\cos $v) - \\int (-\\cos $v)d$v = -$v\\cos($v) + \\sin($v)")
                            return Add(Neg(Mul(Variable(v), Func("cos", Variable(v)))), Func("sin", Variable(v)))
                        }
                        "cos" -> {
                            steps.add("\\text{Integration by Parts } \\left(\\int u\\,dv = uv - \\int v\\,du\\right):")
                            steps.add("\\text{Let } u = $v \\implies du = d$v, \\quad dv = \\cos($v)d$v \\implies v = \\sin($v)")
                            steps.add("\\int $v \\cos($v)\\,d$v = ($v)(\\sin $v) - \\int \\sin($v)d$v = $v\\sin($v) + \\cos($v)")
                            return Add(Mul(Variable(v), Func("sin", Variable(v))), Func("cos", Variable(v)))
                        }
                        "exp" -> {
                            steps.add("\\text{Integration by Parts } \\left(\\int u\\,dv = uv - \\int v\\,du\\right):")
                            steps.add("\\text{Let } u = $v \\implies du = d$v, \\quad dv = e^{$v}d$v \\implies v = e^{$v}")
                            steps.add("\\int $v e^{$v}\\,d$v = $v e^{$v} - \\int e^{$v}d$v = $v e^{$v} - e^{$v}")
                            return Sub(Mul(Variable(v), Func("exp", Variable(v))), Func("exp", Variable(v)))
                        }
                    }
                }
                return null
            }
            is Div -> {
                // f(x) / k = (1/k) * f(x)
                if (expr.right is Constant && abs(expr.right.value) > 1e-15) {
                    val c = expr.right.toLatex()
                    val l = expr.left.toLatex()
                    val dummy = mutableListOf<String>()
                    val lInt = integrateSymbolic(expr.left, v, dummy) ?: return null
                    steps.add("\\text{Constant Divisor Rule: } \\int \\frac{$l}{$c}\\,d$v = \\frac{1}{$c} \\int $l\\,d$v = \\frac{${lInt.toLatex()}}{$c}")
                    return Div(lInt, expr.right)
                }
                // 1 / x -> ln|x|
                if (expr.left is Constant && expr.left.isOne && expr.right is Variable && expr.right.name == v) {
                    steps.add("\\text{Reciprocal Rule: } \\int \\frac{1}{$v}\\,d$v = \\ln|$v|")
                    return Func("ln", Func("abs", expr.right))
                }
                // k / x -> k * ln|x|
                if (expr.left is Constant && expr.right is Variable && expr.right.name == v) {
                    steps.add("\\text{Reciprocal Rule: } \\int \\frac{${expr.left.toLatex()}}{$v}\\,d$v = ${expr.left.toLatex()} \\ln|$v|")
                    return Mul(expr.left, Func("ln", Func("abs", expr.right)))
                }
                return null
            }
            is Pow -> {
                // Power rule: x^n dx = x^(n+1) / (n+1)
                if (expr.base is Variable && expr.base.name == v && expr.exp is Constant) {
                    val n = expr.exp.value
                    if (abs(n - (-1.0)) < 1e-12) {
                        steps.add("\\text{Power Rule } (n = -1): \\int $v^{-1}\\,d$v = \\ln|$v|")
                        return Func("ln", Func("abs", expr.base))
                    }
                    val nPlus1 = n + 1
                    val nPlus1Str = if (abs(nPlus1 - Math.round(nPlus1)) < 1e-9) "${Math.round(nPlus1)}" else "$nPlus1"
                    steps.add("\\text{Power Rule } \\left(\\int $v^n\\,d$v = \\frac{$v^{n+1}}{n+1}\\right): \\int $v^{${expr.exp.toLatex()}}\\,d$v = \\frac{$v^{$nPlus1Str}}{$nPlus1Str}")
                    return Div(Pow(expr.base, Constant(nPlus1)), Constant(nPlus1))
                }
                // e^x dx = e^x
                if (expr.base is Constant && abs(expr.base.value - Math.E) < 1e-6 && expr.exp is Variable && expr.exp.name == v) {
                    steps.add("\\text{Exponential Rule: } \\int e^{$v}\\,d$v = e^{$v}")
                    return expr
                }
                return null
            }
            is Func -> {
                if (expr.arg is Variable && expr.arg.name == v) {
                    return when (expr.name.lowercase()) {
                        "sin" -> {
                            steps.add("\\text{Trigonometric Rule: } \\int \\sin($v)\\,d$v = -\\cos($v)")
                            Neg(Func("cos", expr.arg))
                        }
                        "cos" -> {
                            steps.add("\\text{Trigonometric Rule: } \\int \\cos($v)\\,d$v = \\sin($v)")
                            Func("sin", expr.arg)
                        }
                        "exp" -> {
                            steps.add("\\text{Exponential Rule: } \\int e^{$v}\\,d$v = e^{$v}")
                            expr
                        }
                        "sinh" -> {
                            steps.add("\\text{Hyperbolic Rule: } \\int \\sinh($v)\\,d$v = \\cosh($v)")
                            Func("cosh", expr.arg)
                        }
                        "cosh" -> {
                            steps.add("\\text{Hyperbolic Rule: } \\int \\cosh($v)\\,d$v = \\sinh($v)")
                            Func("sinh", expr.arg)
                        }
                        else -> null
                    }
                }
                return null
            }
        }
    }

    /**
     * Compute definite integral: ∫[a, b] f(x) dx with full step-by-step mathematical reasoning.
     */
    fun integrateDefinite(
        expressionStr: String,
        lower: Double,
        upper: Double,
        variable: String = "x"
    ): DefiniteIntegralResult {
        try {
            val ast = CalculusParser.parse(expressionStr)
            val steps = mutableListOf<String>()
            val lowerFormatted = formatLimit(lower)
            val upperFormatted = formatLimit(upper)

            steps.add("\\textbf{Definite Integral: } I = \\int_{$lowerFormatted}^{$upperFormatted} \\left(${ast.toLatex()}\\right)\\,d$variable")

            // Try symbolic first
            val dummySteps = mutableListOf<String>()
            val antiderivative = integrateSymbolic(ast, variable, dummySteps)

            if (antiderivative != null) {
                val F = antiderivative.simplify()
                val F_b = F.eval(mapOf(variable to upper))
                val F_a = F.eval(mapOf(variable to lower))
                val exactVal = F_b - F_a

                if (!exactVal.isNaN() && !exactVal.isInfinite()) {
                    steps.add("\\textbf{Step 1 - Find Antiderivative: } F($variable) = \\int \\left(${ast.toLatex()}\\right)\\,d$variable = ${F.toLatex()}")
                    steps.add("\\textbf{Step 2 - Fundamental Theorem of Calculus: } \\int_{a}^{b} f($variable)\\,d$variable = \\left[ F($variable) \\right]_{a}^{b} = F(b) - F(a)")

                    val subB = formatSubstitution(F, variable, upper)
                    val subA = formatSubstitution(F, variable, lower)
                    steps.add("\\textbf{Step 3 - Evaluate at Upper Limit } $variable = $upperFormatted:")
                    steps.add("F($upperFormatted) = $subB = ${String.format(java.util.Locale.US, "%.6f", F_b)}")

                    steps.add("\\textbf{Step 4 - Evaluate at Lower Limit } $variable = $lowerFormatted:")
                    steps.add("F($lowerFormatted) = $subA = ${String.format(java.util.Locale.US, "%.6f", F_a)}")

                    steps.add("\\textbf{Step 5 - Subtract: } F($upperFormatted) - F($lowerFormatted) = (${String.format(java.util.Locale.US, "%.6f", F_b)}) - (${String.format(java.util.Locale.US, "%.6f", F_a)}) = ${String.format(java.util.Locale.US, "%.6f", exactVal)}")

                    val frac = Fraction.fromDouble(exactVal)
                    val exactFormatted = if (frac.denominator.toLong() in 1..1000) {
                        frac.toProperString()
                    } else {
                        String.format(java.util.Locale.US, "%.6f", exactVal).trimEnd('0').trimEnd('.')
                    }

                    steps.add("\\textbf{Final Result: } I = $exactFormatted \\quad (\\approx ${String.format(java.util.Locale.US, "%.6f", exactVal)})")

                    return DefiniteIntegralResult(
                        originalExpr = expressionStr,
                        variable = variable,
                        lowerLimit = lower,
                        upperLimit = upper,
                        value = exactVal,
                        formattedDecimal = String.format(java.util.Locale.US, "%.6f", exactVal).trimEnd('0').trimEnd('.'),
                        exactResult = exactFormatted,
                        isExact = true,
                        method = "Exact (Fundamental Theorem)",
                        steps = steps,
                        isSuccess = true
                    )
                }
            }

            // High precision numerical integration: Adaptive Simpson's 1/3 Rule
            steps.add("\\text{Symbolic antiderivative is non-elementary. Evaluating numerically via Adaptive Simpson's 1/3 Quadrature Rule.}")
            steps.add("\\textbf{Formula: } S = \\frac{h}{3}\\left[ f(x_0) + 4\\sum_{i\\text{ odd}} f(x_i) + 2\\sum_{i\\text{ even}} f(x_i) + f(x_n) \\right]")
            val numVal = adaptiveSimpson(ast, lower, upper, variable, tolerance = 1e-7, maxDepth = 15)
            steps.add("\\textbf{Numerical Quadrature Result: } I \\approx ${String.format(java.util.Locale.US, "%.8f", numVal)}")

            return DefiniteIntegralResult(
                originalExpr = expressionStr,
                variable = variable,
                lowerLimit = lower,
                upperLimit = upper,
                value = numVal,
                formattedDecimal = String.format(java.util.Locale.US, "%.6f", numVal).trimEnd('0').trimEnd('.'),
                exactResult = null,
                isExact = false,
                method = "Numerical (Adaptive Simpson)",
                steps = steps,
                isSuccess = true
            )
        } catch (e: Exception) {
            return DefiniteIntegralResult(
                originalExpr = expressionStr,
                variable = variable,
                lowerLimit = lower,
                upperLimit = upper,
                value = Double.NaN,
                formattedDecimal = "Error",
                isSuccess = false,
                errorMessage = e.message ?: "Failed to evaluate definite integral"
            )
        }
    }

    /**
     * Adaptive Simpson's 1/3 Rule with recursion depth control
     */
    fun adaptiveSimpson(
        expr: Expr,
        a: Double,
        b: Double,
        variable: String = "x",
        tolerance: Double = 1e-7,
        maxDepth: Int = 15
    ): Double {
        val f = { x: Double ->
            val v = expr.eval(mapOf(variable to x))
            if (v.isNaN() || v.isInfinite()) 0.0 else v
        }

        fun simpson(fa: Double, fb: Double, fc: Double, left: Double, right: Double): Double {
            return (right - left) / 6.0 * (fa + 4.0 * fc + fb)
        }

        fun recursive(
            left: Double,
            right: Double,
            tol: Double,
            fa: Double,
            fb: Double,
            fc: Double,
            s: Double,
            depth: Int
        ): Double {
            val c = (left + right) / 2.0
            val d = (left + c) / 2.0
            val e = (c + right) / 2.0
            val fd = f(d)
            val fe = f(e)

            val sLeft = simpson(fa, fc, fd, left, c)
            val sRight = simpson(fc, fb, fe, c, right)
            val sTotal = sLeft + sRight

            if (depth >= maxDepth || abs(sTotal - s) <= 15.0 * tol) {
                return sTotal + (sTotal - s) / 15.0
            }

            return recursive(left, c, tol / 2.0, fa, fc, fd, sLeft, depth + 1) +
                    recursive(c, right, tol / 2.0, fc, fb, fe, sRight, depth + 1)
        }

        val fa = f(a)
        val fb = f(b)
        val c = (a + b) / 2.0
        val fc = f(c)
        val s0 = simpson(fa, fb, fc, a, b)

        return recursive(a, b, tolerance, fa, fb, fc, s0, 0)
    }

    /**
     * Extracts individual terms joined by + or - at the root level.
     */
    private fun extractAddSubTerms(expr: Expr): List<Pair<Boolean, Expr>> {
        val result = mutableListOf<Pair<Boolean, Expr>>()
        fun collect(e: Expr, isPositive: Boolean) {
            when (e) {
                is Add -> {
                    collect(e.left, isPositive)
                    collect(e.right, isPositive)
                }
                is Sub -> {
                    collect(e.left, isPositive)
                    collect(e.right, !isPositive)
                }
                is Neg -> {
                    collect(e.inner, !isPositive)
                }
                else -> {
                    result.add(Pair(isPositive, e))
                }
            }
        }
        collect(expr, true)
        return result
    }

    private fun formatLimit(v: Double): String {
        return when {
            abs(v - Math.PI) < 1e-5 -> "\\pi"
            abs(v - (Math.PI / 2)) < 1e-5 -> "\\frac{\\pi}{2}"
            abs(v - (2 * Math.PI)) < 1e-5 -> "2\\pi"
            abs(v - Math.round(v)) < 1e-9 -> Math.round(v).toString()
            else -> String.format(java.util.Locale.US, "%.4f", v).trimEnd('0').trimEnd('.')
        }
    }

    private fun formatSubstitution(expr: Expr, variable: String, point: Double): String {
        val ptStr = if (abs(point - Math.round(point)) < 1e-9) Math.round(point).toString() else String.format(java.util.Locale.US, "%.4f", point)
        val originalLatex = expr.toLatex()
        return originalLatex.replace(variable, "($ptStr)")
    }
}
