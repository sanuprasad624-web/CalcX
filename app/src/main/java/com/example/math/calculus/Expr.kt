package com.example.math.calculus

import com.example.math.Fraction
import java.math.BigInteger
import kotlin.math.*

sealed interface Expr {
    fun eval(vars: Map<String, Double> = emptyMap()): Double
    fun differentiate(v: String = "x"): Expr
    fun simplify(): Expr
    fun toLatex(): String
    fun toDisplayString(): String
}

data class Constant(val value: Double, val fraction: Fraction? = null) : Expr {
    override fun eval(vars: Map<String, Double>): Double = value

    override fun differentiate(v: String): Expr = Constant(0.0)

    override fun simplify(): Expr = this

    override fun toLatex(): String {
        if (fraction != null && fraction.denominator != BigInteger.ONE) {
            val simp = fraction.simplified()
            val sign = if (simp.numerator < BigInteger.ZERO) "-" else ""
            val absNum = simp.numerator.abs()
            return "$sign\\frac{$absNum}{${simp.denominator}}"
        }
        val rounded = Math.round(value)
        return if (abs(value - rounded) < 1e-10) {
            rounded.toString()
        } else {
            val formatted = String.format(java.util.Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
            formatted
        }
    }

    override fun toDisplayString(): String {
        if (fraction != null && fraction.denominator != BigInteger.ONE) {
            return fraction.toProperString()
        }
        val rounded = Math.round(value)
        return if (abs(value - rounded) < 1e-10) {
            rounded.toString()
        } else {
            String.format(java.util.Locale.US, "%.6f", value).trimEnd('0').trimEnd('.')
        }
    }

    val isZero: Boolean get() = abs(value) < 1e-12
    val isOne: Boolean get() = abs(value - 1.0) < 1e-12
    val isMinusOne: Boolean get() = abs(value + 1.0) < 1e-12
}

data class Variable(val name: String = "x") : Expr {
    override fun eval(vars: Map<String, Double>): Double {
        return vars[name] ?: when (name.lowercase()) {
            "pi" -> Math.PI
            "e" -> Math.E
            else -> 0.0
        }
    }

    override fun differentiate(v: String): Expr {
        return if (name == v) Constant(1.0) else Constant(0.0)
    }

    override fun simplify(): Expr = this

    override fun toLatex(): String = when (name.lowercase()) {
        "pi" -> "\\pi"
        "theta" -> "\\theta"
        "alpha" -> "\\alpha"
        "beta" -> "\\beta"
        else -> name
    }

    override fun toDisplayString(): String = when (name.lowercase()) {
        "pi" -> "π"
        "theta" -> "θ"
        "alpha" -> "α"
        "beta" -> "β"
        else -> name
    }
}

data class Add(val left: Expr, val right: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double = left.eval(vars) + right.eval(vars)

    override fun differentiate(v: String): Expr = Add(left.differentiate(v), right.differentiate(v)).simplify()

    override fun simplify(): Expr {
        val l = left.simplify()
        val r = right.simplify()
        if (l is Constant && l.isZero) return r
        if (r is Constant && r.isZero) return l
        if (l is Constant && r is Constant) {
            return Constant(l.value + r.value)
        }
        return Add(l, r)
    }

    override fun toLatex(): String = "${left.toLatex()} + ${right.toLatex()}"
    override fun toDisplayString(): String = "${left.toDisplayString()} + ${right.toDisplayString()}"
}

data class Sub(val left: Expr, val right: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double = left.eval(vars) - right.eval(vars)

    override fun differentiate(v: String): Expr = Sub(left.differentiate(v), right.differentiate(v)).simplify()

    override fun simplify(): Expr {
        val l = left.simplify()
        val r = right.simplify()
        if (r is Constant && r.isZero) return l
        if (l is Constant && l.isZero) return Neg(r).simplify()
        if (l is Constant && r is Constant) {
            return Constant(l.value - r.value)
        }
        return Sub(l, r)
    }

    override fun toLatex(): String {
        val rightStr = if (right is Add || right is Sub) "\\left(${right.toLatex()}\\right)" else right.toLatex()
        return "${left.toLatex()} - $rightStr"
    }

    override fun toDisplayString(): String {
        val rightStr = if (right is Add || right is Sub) "(${right.toDisplayString()})" else right.toDisplayString()
        return "${left.toDisplayString()} - $rightStr"
    }
}

data class Mul(val left: Expr, val right: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double = left.eval(vars) * right.eval(vars)

    // Product Rule: (u*v)' = u'*v + u*v'
    override fun differentiate(v: String): Expr {
        val du = left.differentiate(v)
        val dv = right.differentiate(v)
        return Add(Mul(du, right), Mul(left, dv)).simplify()
    }

    override fun simplify(): Expr {
        val l = left.simplify()
        val r = right.simplify()
        if (l is Constant && l.isZero) return Constant(0.0)
        if (r is Constant && r.isZero) return Constant(0.0)
        if (l is Constant && l.isOne) return r
        if (r is Constant && r.isOne) return l
        if (l is Constant && l.isMinusOne) return Neg(r).simplify()
        if (r is Constant && r.isMinusOne) return Neg(l).simplify()
        if (l is Constant && r is Constant) {
            return Constant(l.value * r.value)
        }
        return Mul(l, r)
    }

    override fun toLatex(): String {
        val lStr = if (left is Add || left is Sub) "\\left(${left.toLatex()}\\right)" else left.toLatex()
        val rStr = if (right is Add || right is Sub) "\\left(${right.toLatex()}\\right)" else right.toLatex()
        return if (left is Constant && right is Variable) {
            "$lStr ${right.toLatex()}"
        } else if (left is Constant && right is Func) {
            "$lStr ${right.toLatex()}"
        } else {
            "$lStr \\cdot $rStr"
        }
    }

    override fun toDisplayString(): String {
        val lStr = if (left is Add || left is Sub) "(${left.toDisplayString()})" else left.toDisplayString()
        val rStr = if (right is Add || right is Sub) "(${right.toDisplayString()})" else right.toDisplayString()
        return if (left is Constant && (right is Variable || right is Func)) {
            "$lStr${right.toDisplayString()}"
        } else {
            "$lStr · $rStr"
        }
    }
}

data class Div(val left: Expr, val right: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double {
        val den = right.eval(vars)
        if (abs(den) < 1e-15) throw ArithmeticException("Division by zero")
        return left.eval(vars) / den
    }

    // Quotient Rule: (u/v)' = (u'*v - u*v') / v^2
    override fun differentiate(v: String): Expr {
        val du = left.differentiate(v)
        val dv = right.differentiate(v)
        val num = Sub(Mul(du, right), Mul(left, dv))
        val den = Pow(right, Constant(2.0))
        return Div(num, den).simplify()
    }

    override fun simplify(): Expr {
        val l = left.simplify()
        val r = right.simplify()
        if (l is Constant && l.isZero) return Constant(0.0)
        if (r is Constant && r.isOne) return l
        if (l is Constant && r is Constant && abs(r.value) > 1e-15) {
            return Constant(l.value / r.value)
        }
        return Div(l, r)
    }

    override fun toLatex(): String = "\\frac{${left.toLatex()}}{${right.toLatex()}}"
    override fun toDisplayString(): String {
        val lStr = if (left is Add || left is Sub) "(${left.toDisplayString()})" else left.toDisplayString()
        val rStr = if (right is Add || right is Sub || right is Mul || right is Div) "(${right.toDisplayString()})" else right.toDisplayString()
        return "$lStr / $rStr"
    }
}

data class Pow(val base: Expr, val exp: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double {
        val b = base.eval(vars)
        val e = exp.eval(vars)
        return b.pow(e)
    }

    // General Power Rule: (u^v)' = u^v * (v' * ln(u) + v * u' / u)
    override fun differentiate(v: String): Expr {
        val u = base
        val w = exp
        val du = u.differentiate(v)
        val dw = w.differentiate(v)

        // Case 1: exponent is constant: (u^n)' = n * u^(n-1) * u'
        if (w is Constant) {
            val n = w.value
            val newExp = Constant(n - 1)
            return Mul(Mul(w, Pow(u, newExp)), du).simplify()
        }
        // Case 2: base is constant a^w: (a^w)' = a^w * ln(a) * w'
        if (u is Constant) {
            return Mul(Mul(this, Func("ln", u)), dw).simplify()
        }
        // Case 3: general (u^w)'
        val part1 = Mul(dw, Func("ln", u))
        val part2 = Div(Mul(w, du), u)
        return Mul(this, Add(part1, part2)).simplify()
    }

    override fun simplify(): Expr {
        val b = base.simplify()
        val e = exp.simplify()
        if (e is Constant && e.isZero) return Constant(1.0)
        if (e is Constant && e.isOne) return b
        if (b is Constant && (b.isZero || b.isOne)) return b
        if (b is Constant && e is Constant) {
            return Constant(b.value.pow(e.value))
        }
        return Pow(b, e)
    }

    override fun toLatex(): String {
        val baseStr = if (base is Add || base is Sub || base is Mul || base is Div || base is Neg) {
            "\\left(${base.toLatex()}\\right)"
        } else {
            base.toLatex()
        }
        return "$baseStr^{${exp.toLatex()}}"
    }

    override fun toDisplayString(): String {
        val baseStr = if (base is Add || base is Sub || base is Mul || base is Div || base is Neg) {
            "(${base.toDisplayString()})"
        } else {
            base.toDisplayString()
        }
        if (exp is Constant && exp.value == 2.0) return "$baseStr²"
        if (exp is Constant && exp.value == 3.0) return "$baseStr³"
        return "$baseStr^(${exp.toDisplayString()})"
    }
}

data class Neg(val inner: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double = -inner.eval(vars)

    override fun differentiate(v: String): Expr = Neg(inner.differentiate(v)).simplify()

    override fun simplify(): Expr {
        val inn = inner.simplify()
        if (inn is Constant) return Constant(-inn.value)
        if (inn is Neg) return inn.inner
        return Neg(inn)
    }

    override fun toLatex(): String {
        val inStr = if (inner is Add || inner is Sub) "\\left(${inner.toLatex()}\\right)" else inner.toLatex()
        return "-$inStr"
    }

    override fun toDisplayString(): String {
        val inStr = if (inner is Add || inner is Sub) "(${inner.toDisplayString()})" else inner.toDisplayString()
        return "-$inStr"
    }
}

data class Func(val name: String, val arg: Expr) : Expr {
    override fun eval(vars: Map<String, Double>): Double {
        val a = arg.eval(vars)
        return when (name.lowercase()) {
            "sin" -> sin(a)
            "cos" -> cos(a)
            "tan" -> tan(a)
            "asin" -> asin(a)
            "acos" -> acos(a)
            "atan" -> atan(a)
            "sinh" -> sinh(a)
            "cosh" -> cosh(a)
            "tanh" -> tanh(a)
            "ln" -> ln(a)
            "log", "log10" -> log10(a)
            "exp" -> exp(a)
            "sqrt" -> sqrt(a)
            "abs" -> abs(a)
            else -> throw UnsupportedOperationException("Unknown function $name")
        }
    }

    override fun differentiate(v: String): Expr {
        val du = arg.differentiate(v)
        val derivFunc = when (name.lowercase()) {
            "sin" -> Func("cos", arg)
            "cos" -> Neg(Func("sin", arg))
            "tan" -> Pow(Func("sec", arg), Constant(2.0)) // or 1 + tan^2(arg) -> Div(1, Pow(cos(arg), 2))
            "sec" -> Mul(Func("sec", arg), Func("tan", arg))
            "asin" -> Div(Constant(1.0), Func("sqrt", Sub(Constant(1.0), Pow(arg, Constant(2.0)))))
            "acos" -> Neg(Div(Constant(1.0), Func("sqrt", Sub(Constant(1.0), Pow(arg, Constant(2.0))))))
            "atan" -> Div(Constant(1.0), Add(Constant(1.0), Pow(arg, Constant(2.0))))
            "sinh" -> Func("cosh", arg)
            "cosh" -> Func("sinh", arg)
            "tanh" -> Div(Constant(1.0), Pow(Func("cosh", arg), Constant(2.0)))
            "ln" -> Div(Constant(1.0), arg)
            "log", "log10" -> Div(Constant(1.0), Mul(arg, Constant(ln(10.0))))
            "exp" -> Func("exp", arg)
            "sqrt" -> Div(Constant(1.0), Mul(Constant(2.0), Func("sqrt", arg)))
            "abs" -> Div(arg, Func("abs", arg))
            else -> throw UnsupportedOperationException("Derivative for $name is not supported")
        }
        return Mul(derivFunc, du).simplify()
    }

    override fun simplify(): Expr {
        val s = arg.simplify()
        if (s is Constant) {
            try {
                val v = eval(emptyMap())
                if (!v.isNaN() && !v.isInfinite()) {
                    val r = Math.round(v)
                    if (abs(v - r) < 1e-12) return Constant(r.toDouble())
                }
            } catch (_: Exception) {}
        }
        return Func(name, s)
    }

    override fun toLatex(): String = when (name.lowercase()) {
        "sqrt" -> "\\sqrt{${arg.toLatex()}}"
        "sin", "cos", "tan", "sinh", "cosh", "tanh", "ln", "exp" -> "\\$name\\left(${arg.toLatex()}\\right)"
        "asin" -> "\\arcsin\\left(${arg.toLatex()}\\right)"
        "acos" -> "\\arccos\\left(${arg.toLatex()}\\right)"
        "atan" -> "\\arctan\\left(${arg.toLatex()}\\right)"
        "log", "log10" -> "\\log_{10}\\left(${arg.toLatex()}\\right)"
        "abs" -> "\\left|${arg.toLatex()}\\right|"
        else -> "$name\\left(${arg.toLatex()}\\right)"
    }

    override fun toDisplayString(): String = when (name.lowercase()) {
        "sqrt" -> "√(${arg.toDisplayString()})"
        "abs" -> "|${arg.toDisplayString()}|"
        else -> "$name(${arg.toDisplayString()})"
    }
}
