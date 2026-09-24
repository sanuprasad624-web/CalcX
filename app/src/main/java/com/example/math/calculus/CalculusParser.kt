package com.example.math.calculus

import java.util.Locale

class CalculusParser(private val input: String) {

    private var pos = 0
    private val text: String

    init {
        // Strip common prefixes like "y =", "f(x) =", "y=", "f(x)="
        var cleaned = input.trim()
        val lower = cleaned.lowercase(Locale.US)
        if (lower.startsWith("y=") || lower.startsWith("y =")) {
            cleaned = cleaned.substring(cleaned.indexOf('=') + 1).trim()
        } else if (lower.startsWith("f(x)=") || lower.startsWith("f(x) =") ||
            lower.startsWith("g(x)=") || lower.startsWith("g(x) =")) {
            cleaned = cleaned.substring(cleaned.indexOf('=') + 1).trim()
        }
        text = cleaned
    }

    fun parse(): Expr {
        if (text.isBlank()) return Constant(0.0)
        pos = 0
        val expr = parseAddSub()
        return expr.simplify()
    }

    private fun parseAddSub(): Expr {
        var left = parseMulDiv()
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            val ch = text[pos]
            if (ch == '+') {
                pos++
                val right = parseMulDiv()
                left = Add(left, right)
            } else if (ch == '-') {
                pos++
                val right = parseMulDiv()
                left = Sub(left, right)
            } else {
                break
            }
        }
        return left
    }

    private fun parseMulDiv(): Expr {
        var left = parsePower()
        while (pos < text.length) {
            skipWhitespace()
            if (pos >= text.length) break
            val ch = text[pos]
            if (ch == '*' || ch == '×' || ch == '·') {
                pos++
                val right = parsePower()
                left = Mul(left, right)
            } else if (ch == '/' || ch == '÷') {
                pos++
                val right = parsePower()
                left = Div(left, right)
            } else if (isImplicitMulNext()) {
                // Implicit multiplication: e.g. 2x, 2(x+1), x sin(x)
                val right = parsePower()
                left = Mul(left, right)
            } else {
                break
            }
        }
        return left
    }

    private fun isImplicitMulNext(): Boolean {
        if (pos >= text.length) return false
        val ch = text[pos]
        // If next is digit or decimal point, not implicit multiplication unless following a closed paren or variable
        return ch == '(' || ch == '|' || ch.isLetter() || ch == 'π' || ch == 'θ'
    }

    private fun parsePower(): Expr {
        var left = parseUnary()
        skipWhitespace()
        if (pos < text.length && text[pos] == '^') {
            pos++
            val right = parsePower() // Right-associative: 2^3^4 = 2^(3^4)
            left = Pow(left, right)
        }
        return left
    }

    private fun parseUnary(): Expr {
        skipWhitespace()
        if (pos < text.length) {
            val ch = text[pos]
            if (ch == '-') {
                pos++
                return Neg(parseUnary())
            } else if (ch == '+') {
                pos++
                return parseUnary()
            }
        }
        return parsePrimary()
    }

    private fun parsePrimary(): Expr {
        skipWhitespace()
        if (pos >= text.length) return Constant(0.0)

        val ch = text[pos]

        // Parentheses
        if (ch == '(') {
            pos++
            val expr = parseAddSub()
            skipWhitespace()
            if (pos < text.length && text[pos] == ')') {
                pos++
            }
            return expr
        }

        // Absolute value |expr|
        if (ch == '|') {
            pos++
            val expr = parseAddSub()
            skipWhitespace()
            if (pos < text.length && text[pos] == '|') {
                pos++
            }
            return Func("abs", expr)
        }

        // Numbers
        if (ch.isDigit() || ch == '.') {
            val start = pos
            var seenDot = (ch == '.')
            pos++
            while (pos < text.length) {
                val c = text[pos]
                if (c == '.') {
                    if (seenDot) break
                    seenDot = true
                    pos++
                } else if (c.isDigit()) {
                    pos++
                } else {
                    break
                }
            }
            val numStr = text.substring(start, pos)
            val v = numStr.toDoubleOrNull() ?: 0.0
            return Constant(v)
        }

        // Unicode Pi
        if (ch == 'π') {
            pos++
            return Variable("pi")
        }
        if (ch == 'θ') {
            pos++
            return Variable("theta")
        }

        // Identifier: variable or function name
        if (ch.isLetter() || ch == 'π' || ch == 'θ') {
            val start = pos
            while (pos < text.length && (text[pos].isLetter() || text[pos].isDigit() || text[pos] == '_')) {
                pos++
            }
            var name = text.substring(start, pos)
            skipWhitespace()

            // Check if function name is followed by inverse/power superscript like sin^-1 or sin^{-1} or sin^2
            if (pos < text.length && text[pos] == '^') {
                pos++
                skipWhitespace()
                var powerStr = ""
                if (pos < text.length && text[pos] == '{') {
                    pos++
                    val pStart = pos
                    while (pos < text.length && text[pos] != '}') pos++
                    powerStr = text.substring(pStart, pos)
                    if (pos < text.length && text[pos] == '}') pos++
                } else if (pos < text.length && (text[pos] == '-' || text[pos] == '+')) {
                    val pStart = pos
                    pos++
                    while (pos < text.length && text[pos].isDigit()) pos++
                    powerStr = text.substring(pStart, pos)
                } else if (pos < text.length && text[pos].isDigit()) {
                    val pStart = pos
                    while (pos < text.length && text[pos].isDigit()) pos++
                    powerStr = text.substring(pStart, pos)
                }

                if (powerStr == "-1") {
                    name = when (name.lowercase(Locale.US)) {
                        "sin" -> "asin"
                        "cos" -> "acos"
                        "tan" -> "atan"
                        "cot" -> "acot"
                        "sec" -> "asec"
                        "csc", "cosec" -> "acsc"
                        else -> "${name}^-1"
                    }
                } else if (powerStr.isNotEmpty()) {
                    // e.g. sin^2(x) -> (sin(x))^2
                    val pVal = powerStr.toDoubleOrNull()
                    skipWhitespace()
                    if (pos < text.length && text[pos] == '(') {
                        pos++
                        val arg = parseAddSub()
                        skipWhitespace()
                        if (pos < text.length && text[pos] == ')') pos++
                        val baseFunc = Func(name, arg)
                        return if (pVal != null) Pow(baseFunc, Constant(pVal)) else baseFunc
                    }
                }
            }

            skipWhitespace()

            // Check if it's a function followed by '('
            if (pos < text.length && text[pos] == '(') {
                pos++
                val arg = parseAddSub()
                skipWhitespace()
                if (pos < text.length && text[pos] == ')') {
                    pos++
                }
                return Func(name, arg)
            }

            // Normalization of known function names if argument follows without parens (e.g. sin x -> sin(x))
            val lower = name.lowercase(Locale.US)
            val isKnownFunc = lower in listOf(
                "sin", "cos", "tan", "cot", "sec", "csc", "cosec",
                "asin", "acos", "atan", "acot", "asec", "acsc", "acosec",
                "arcsin", "arccos", "arctan", "arccot", "arcsec", "arccsc", "arccosec",
                "sinh", "cosh", "tanh", "coth", "sech", "csch",
                "ln", "log", "log10", "exp", "sqrt", "cbrt", "abs"
            )

            if (isKnownFunc && pos < text.length && (text[pos].isLetter() || text[pos] == '(' || text[pos].isDigit())) {
                val arg = parsePower()
                return Func(name, arg)
            }

            // Special single-argument functions without paren if supported, or common constants
            if (lower == "pi") return Variable("pi")
            if (lower == "e" && (pos >= text.length || text[pos] != '(')) {
                // If it's e^x
                if (pos < text.length && text[pos] == '^') {
                    return Constant(Math.E)
                }
                return Constant(Math.E)
            }

            return Variable(name)
        }

        pos++
        return Constant(0.0)
    }

    private fun skipWhitespace() {
        while (pos < text.length && text[pos].isWhitespace()) {
            pos++
        }
    }

    companion object {
        fun parse(input: String): Expr {
            return CalculusParser(input).parse()
        }
    }
}
