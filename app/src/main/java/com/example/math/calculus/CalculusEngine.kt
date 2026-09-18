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
     * Optionally evaluate at [atPoint].
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

            steps.add("Given expression: f($variable) = ${ast.toDisplayString()}")

            var current = ast
            for (i in 1..order) {
                val next = current.differentiate(variable).simplify()
                val orderLabel = when (i) {
                    1 -> "1st derivative f'($variable)"
                    2 -> "2nd derivative f''($variable)"
                    3 -> "3rd derivative f'''($variable)"
                    else -> "${i}th derivative"
                }
                steps.add("Differentiating: $orderLabel = ${next.toDisplayString()}")
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
                steps.add("Evaluated at $variable = $atPoint: f^($order)($atPoint) = $ptValueExact (≈ ${String.format(java.util.Locale.US, "%.6f", v)})")
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
     * Compute indefinite integral: ∫ f(x) dx
     */
    fun integrateIndefinite(
        expressionStr: String,
        variable: String = "x"
    ): IndefiniteIntegralResult {
        try {
            val ast = CalculusParser.parse(expressionStr)
            val steps = mutableListOf<String>()
            steps.add("Integrand: f($variable) = ${ast.toDisplayString()}")

            val antiderivative = integrateSymbolic(ast, variable, steps)

            if (antiderivative != null) {
                val simp = antiderivative.simplify()
                val latex = "${simp.toLatex()} + C"
                val display = "${simp.toDisplayString()} + C"
                steps.add("Antiderivative: F($variable) = ${simp.toDisplayString()} + C")

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
                return IndefiniteIntegralResult(
                    originalExpr = expressionStr,
                    variable = variable,
                    antiderivativeExpr = null,
                    resultLatex = "\\int \\left(${ast.toLatex()}\\right)\\,d$variable",
                    resultDisplay = "∫ (${ast.toDisplayString()}) d$variable",
                    steps = listOf("No elementary closed-form symbolic antiderivative found. Definite integration via numerical quadrature is supported."),
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
     * Symbolic integration recursive matcher
     */
    private fun integrateSymbolic(expr: Expr, v: String, steps: MutableList<String>): Expr? {
        when (expr) {
            is Constant -> {
                // ∫ k dx = k * x
                steps.add("Constant rule: ∫ ${expr.toDisplayString()} d$v = ${expr.toDisplayString()}$v")
                return Mul(expr, Variable(v))
            }
            is Variable -> {
                if (expr.name == v) {
                    // ∫ x dx = x^2 / 2
                    steps.add("Power rule on $v: ∫ $v d$v = $v² / 2")
                    return Div(Pow(expr, Constant(2.0)), Constant(2.0))
                } else {
                    // Constant variable w.r.t v
                    steps.add("Treating ${expr.name} as constant w.r.t $v: ∫ ${expr.name} d$v = ${expr.name}$v")
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
                    val rInt = integrateSymbolic(expr.right, v, steps) ?: return null
                    return Mul(expr.left, rInt)
                }
                // If right is constant
                if (expr.right is Constant) {
                    val lInt = integrateSymbolic(expr.left, v, steps) ?: return null
                    return Mul(expr.right, lInt)
                }
                return null
            }
            is Div -> {
                // f(x) / k = (1/k) * f(x)
                if (expr.right is Constant && abs(expr.right.value) > 1e-15) {
                    val lInt = integrateSymbolic(expr.left, v, steps) ?: return null
                    return Div(lInt, expr.right)
                }
                // 1 / x -> ln|x|
                if (expr.left is Constant && expr.left.isOne && expr.right is Variable && expr.right.name == v) {
                    steps.add("Reciprocal rule: ∫ (1/$v) d$v = ln|$v|")
                    return Func("ln", Func("abs", expr.right))
                }
                // k / x -> k * ln|x|
                if (expr.left is Constant && expr.right is Variable && expr.right.name == v) {
                    steps.add("Reciprocal rule: ∫ (${expr.left.toDisplayString()}/$v) d$v = ${expr.left.toDisplayString()}·ln|$v|")
                    return Mul(expr.left, Func("ln", Func("abs", expr.right)))
                }
                return null
            }
            is Pow -> {
                // Power rule: x^n dx = x^(n+1) / (n+1)
                if (expr.base is Variable && expr.base.name == v && expr.exp is Constant) {
                    val n = expr.exp.value
                    if (abs(n - (-1.0)) < 1e-12) {
                        steps.add("∫ $v⁻¹ d$v = ln|$v|")
                        return Func("ln", Func("abs", expr.base))
                    }
                    val nPlus1 = n + 1
                    steps.add("Power rule: ∫ $v^$n d$v = ($v^$nPlus1) / $nPlus1")
                    return Div(Pow(expr.base, Constant(nPlus1)), Constant(nPlus1))
                }
                // e^x dx = e^x
                if (expr.base is Constant && abs(expr.base.value - Math.E) < 1e-6 && expr.exp is Variable && expr.exp.name == v) {
                    steps.add("Exponential rule: ∫ e^$v d$v = e^$v")
                    return expr
                }
                return null
            }
            is Func -> {
                if (expr.arg is Variable && expr.arg.name == v) {
                    return when (expr.name.lowercase()) {
                        "sin" -> {
                            steps.add("Trig rule: ∫ sin($v) d$v = -cos($v)")
                            Neg(Func("cos", expr.arg))
                        }
                        "cos" -> {
                            steps.add("Trig rule: ∫ cos($v) d$v = sin($v)")
                            Func("sin", expr.arg)
                        }
                        "exp" -> {
                            steps.add("Exponential rule: ∫ exp($v) d$v = exp($v)")
                            expr
                        }
                        "sinh" -> Func("cosh", expr.arg)
                        "cosh" -> Func("sinh", expr.arg)
                        else -> null
                    }
                }
                return null
            }
        }
    }

    /**
     * Compute definite integral: ∫[a, b] f(x) dx
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
            steps.add("Definite integral: ∫_{$lower}^{$upper} (${ast.toDisplayString()}) d$variable")

            // Try symbolic first
            val dummySteps = mutableListOf<String>()
            val antiderivative = integrateSymbolic(ast, variable, dummySteps)

            if (antiderivative != null) {
                val F_b = antiderivative.eval(mapOf(variable to upper))
                val F_a = antiderivative.eval(mapOf(variable to lower))
                val exactVal = F_b - F_a

                if (!exactVal.isNaN() && !exactVal.isInfinite()) {
                    steps.add("Found antiderivative: F($variable) = ${antiderivative.simplify().toDisplayString()}")
                    steps.add("By Fundamental Theorem: F($upper) - F($lower)")
                    steps.add("F($upper) = ${String.format(java.util.Locale.US, "%.6f", F_b)}, F($lower) = ${String.format(java.util.Locale.US, "%.6f", F_a)}")
                    steps.add("Result = ${String.format(java.util.Locale.US, "%.6f", exactVal)}")

                    val frac = Fraction.fromDouble(exactVal)
                    val exactFormatted = if (frac.denominator.toLong() in 1..1000) {
                        frac.toProperString()
                    } else {
                        String.format(java.util.Locale.US, "%.6f", exactVal).trimEnd('0').trimEnd('.')
                    }

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
            steps.add("Evaluating numerically via Adaptive Simpson's 1/3 Quadrature rule.")
            val numVal = adaptiveSimpson(ast, lower, upper, variable, tolerance = 1e-7, maxDepth = 15)
            steps.add("Numerical evaluation result = ${String.format(java.util.Locale.US, "%.8f", numVal)}")

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
}
