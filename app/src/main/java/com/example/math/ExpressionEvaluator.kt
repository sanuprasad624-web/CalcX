package com.example.math

import java.math.BigInteger
import kotlin.math.*

class ExpressionEvaluator(
    var angleMode: AngleMode = AngleMode.DEG
) {

    fun evaluate(expression: String): MathResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return MathResult(0.0, Fraction.ZERO, "0")
        }

        // Automatically balance unclosed parentheses so functions show results immediately
        val balanced = balanceParentheses(trimmed)

        return try {
            val rawTokens = Tokenizer(balanced).tokenize()
            if (rawTokens.isEmpty()) {
                return MathResult(0.0, Fraction.ZERO, "0")
            }

            // Insert implicit multiplication where needed
            val tokens = insertImplicitMultiplication(rawTokens)

            val parser = Parser(tokens, angleMode)
            val ast = parser.parse()
            val value = ast.eval()

            // Try to evaluate as exact fraction if applicable
            val fraction = try {
                ast.evalFraction()
            } catch (_: Exception) {
                null
            }

            // Check if expression is an inverse trig calculation
            val isInvTrig = trimmed.contains("asin", ignoreCase = true) ||
                    trimmed.contains("acos", ignoreCase = true) ||
                    trimmed.contains("atan", ignoreCase = true) ||
                    trimmed.contains("sin⁻¹") ||
                    trimmed.contains("cos⁻¹") ||
                    trimmed.contains("tan⁻¹")

            val piFraction = if (isInvTrig) {
                // If in DEG, convert value (degrees) to radians to get exact pi fraction
                val radVal = if (angleMode == AngleMode.DEG) (value * PI / 180.0) else value
                formatPiFraction(radVal)
            } else if (angleMode == AngleMode.RAD) {
                formatPiFraction(value)
            } else {
                null
            }

            val exactStr = when {
                piFraction != null -> piFraction
                fraction != null -> fraction.toProperString()
                abs(value - Math.round(value)) < 1e-12 -> Math.round(value).toString()
                else -> ""
            }

            MathResult(value = value, fraction = fraction, exactText = exactStr)
        } catch (e: MathException) {
            MathResult.error(e.message ?: "Invalid expression")
        } catch (e: Exception) {
            MathResult.error("Invalid expression")
        }
    }

    private fun balanceParentheses(expr: String): String {
        var openCount = 0
        for (ch in expr) {
            if (ch == '(') openCount++
            else if (ch == ')') {
                if (openCount > 0) openCount--
            }
        }
        return expr + ")".repeat(openCount)
    }

    private fun formatPiFraction(value: Double): String? {
        if (abs(value) < 1e-12) return "0"
        val ratio = value / PI
        for (d in 1..24) {
            val prod = ratio * d
            val rounded = Math.round(prod)
            if (abs(prod - rounded) < 1e-7) {
                val k = rounded.toInt()
                val g = gcd(abs(k), d)
                val num = k / g
                val den = d / g
                return when {
                    num == 0 -> "0"
                    den == 1 -> {
                        when (num) {
                            1 -> "\\pi"
                            -1 -> "-\\pi"
                            else -> "${num}\\pi"
                        }
                    }
                    num == 1 -> "\\frac{\\pi}{$den}"
                    num == -1 -> "-\\frac{\\pi}{$den}"
                    else -> "\\frac{${num}\\pi}{$den}"
                }
            }
        }
        return null
    }

    private fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

    private fun insertImplicitMultiplication(tokens: List<Token>): List<Token> {
        val result = mutableListOf<Token>()
        for (i in tokens.indices) {
            val curr = tokens[i]
            result.add(curr)
            if (i + 1 < tokens.size) {
                val next = tokens[i + 1]
                val needMul = when (curr) {
                    is Token.Number -> next is Token.OpenParen || next is Token.Identifier
                    is Token.CloseParen -> next is Token.OpenParen || next is Token.Number || next is Token.Identifier
                    is Token.Factorial -> next is Token.Number || next is Token.OpenParen || next is Token.Identifier
                    is Token.Percent -> next is Token.Number || next is Token.OpenParen || next is Token.Identifier
                    is Token.Identifier -> {
                        // If identifier is a constant (pi, e) followed by number or open paren or identifier
                        if (curr.name == "pi" || curr.name == "e") {
                            next is Token.Number || next is Token.OpenParen || next is Token.Identifier
                        } else false
                    }
                    else -> false
                }
                if (needMul) {
                    result.add(Token.Operator('*'))
                }
            }
        }
        return result
    }

    class MathException(message: String) : RuntimeException(message)

    private sealed interface ExprNode {
        fun eval(): Double
        fun evalFraction(): Fraction
    }

    private data class NumberNode(val value: Double, val rawText: String) : ExprNode {
        override fun eval(): Double = value
        override fun evalFraction(): Fraction {
            return Fraction.fromDouble(value)
        }
    }

    private data class BinaryOpNode(
        val left: ExprNode,
        val op: Char,
        val right: ExprNode
    ) : ExprNode {
        override fun eval(): Double {
            val l = left.eval()
            val r = right.eval()
            return when (op) {
                '+' -> l + r
                '-' -> l - r
                '*' -> l * r
                '/' -> {
                    if (r == 0.0) throw MathException("Division by zero")
                    l / r
                }
                '%' -> l % r
                '^' -> {
                    if (l < 0 && r % 1.0 != 0.0) throw MathException("Undefined for real numbers")
                    l.pow(r)
                }
                else -> throw MathException("Unknown operator '$op'")
            }
        }

        override fun evalFraction(): Fraction {
            val lf = left.evalFraction()
            val rf = right.evalFraction()
            return when (op) {
                '+' -> lf + rf
                '-' -> lf - rf
                '*' -> lf * rf
                '/' -> {
                    if (rf.numerator == BigInteger.ZERO) throw MathException("Division by zero")
                    lf / rf
                }
                '^' -> {
                    val rDouble = right.eval()
                    if (rDouble % 1.0 == 0.0 && abs(rDouble) <= 50) {
                        val exp = rDouble.toInt()
                        if (exp >= 0) {
                            Fraction(lf.numerator.pow(exp), lf.denominator.pow(exp)).simplified()
                        } else {
                            val posExp = -exp
                            Fraction(lf.denominator.pow(posExp), lf.numerator.pow(posExp)).simplified()
                        }
                    } else {
                        throw MathException("Non-integer fraction power")
                    }
                }
                else -> throw MathException("Cannot evaluate as exact fraction")
            }
        }
    }

    private data class UnaryOpNode(val op: Char, val operand: ExprNode) : ExprNode {
        override fun eval(): Double {
            val v = operand.eval()
            return when (op) {
                '-' -> -v
                '+' -> v
                else -> throw MathException("Unknown unary operator '$op'")
            }
        }

        override fun evalFraction(): Fraction {
            val f = operand.evalFraction()
            return when (op) {
                '-' -> -f
                '+' -> f
                else -> throw MathException("Unknown unary operator")
            }
        }
    }

    private data class FactorialNode(val operand: ExprNode) : ExprNode {
        override fun eval(): Double {
            val v = operand.eval()
            if (v < 0 || v % 1.0 != 0.0 || v > 170) {
                throw MathException(if (v < 0) "Domain error for factorial" else "Factorial overflow")
            }
            return factorial(v.toInt())
        }

        override fun evalFraction(): Fraction {
            val v = eval()
            return Fraction.of(v.toLong(), 1L)
        }

        private fun factorial(n: Int): Double {
            var res = 1.0
            for (i in 2..n) res *= i
            return res
        }
    }

    private data class PercentNode(val operand: ExprNode) : ExprNode {
        override fun eval(): Double = operand.eval() / 100.0
        override fun evalFraction(): Fraction = operand.evalFraction() / Fraction.of(100L)
    }

    private data class FunctionNode(
        val name: String,
        val args: List<ExprNode>,
        val angleMode: AngleMode
    ) : ExprNode {
        override fun eval(): Double {
            val argVals = args.map { it.eval() }
            return when (name) {
                "sin" -> {
                    val a = toRadians(argVals[0], angleMode)
                    // Check for exact multiples of 180 deg
                    if (angleMode == AngleMode.DEG && argVals[0] % 180.0 == 0.0) 0.0 else sin(a)
                }
                "cos" -> {
                    val a = toRadians(argVals[0], angleMode)
                    if (angleMode == AngleMode.DEG && (argVals[0] - 90.0) % 180.0 == 0.0) 0.0 else cos(a)
                }
                "tan" -> {
                    if (angleMode == AngleMode.DEG && (argVals[0] - 90.0) % 180.0 == 0.0) {
                        throw MathException("Undefined (tan 90°)")
                    }
                    val a = toRadians(argVals[0], angleMode)
                    tan(a)
                }
                "asin" -> {
                    val v = argVals[0]
                    if (v < -1.0 || v > 1.0) throw MathException("Domain error: asin argument must be in [-1, 1]")
                    fromRadians(asin(v), angleMode)
                }
                "acos" -> {
                    val v = argVals[0]
                    if (v < -1.0 || v > 1.0) throw MathException("Domain error: acos argument must be in [-1, 1]")
                    fromRadians(acos(v), angleMode)
                }
                "atan" -> fromRadians(atan(argVals[0]), angleMode)
                "sinh" -> sinh(argVals[0])
                "cosh" -> cosh(argVals[0])
                "tanh" -> tanh(argVals[0])
                "ln" -> {
                    val v = argVals[0]
                    if (v <= 0.0) throw MathException(if (v == 0.0) "Undefined (ln 0)" else "Undefined for real numbers")
                    ln(v)
                }
                "log", "log10" -> {
                    val v = argVals[0]
                    if (v <= 0.0) throw MathException(if (v == 0.0) "Undefined (log 0)" else "Undefined for real numbers")
                    log10(v)
                }
                "log2" -> {
                    val v = argVals[0]
                    if (v <= 0.0) throw MathException("Domain error for log2")
                    log2(v)
                }
                "exp" -> exp(argVals[0])
                "sqrt" -> {
                    val v = argVals[0]
                    if (v < 0) throw MathException("Undefined for real numbers")
                    sqrt(v)
                }
                "cbrt" -> cbrt(argVals[0])
                "abs" -> abs(argVals[0])
                "floor" -> floor(argVals[0])
                "ceil" -> ceil(argVals[0])
                "rad" -> Math.toRadians(argVals[0])
                "deg" -> Math.toDegrees(argVals[0])
                "gcd" -> {
                    if (args.size < 2) throw MathException("gcd requires 2 arguments")
                    val a = argVals[0].toLong()
                    val b = argVals[1].toLong()
                    BigInteger.valueOf(a).gcd(BigInteger.valueOf(b)).toDouble()
                }
                "lcm" -> {
                    if (args.size < 2) throw MathException("lcm requires 2 arguments")
                    val a = BigInteger.valueOf(argVals[0].toLong())
                    val b = BigInteger.valueOf(argVals[1].toLong())
                    val gcd = a.gcd(b)
                    if (gcd == BigInteger.ZERO) 0.0 else a.multiply(b).abs().divide(gcd).toDouble()
                }
                "npr" -> {
                    if (args.size < 2) throw MathException("nPr requires n and r")
                    val n = argVals[0].toLong()
                    val r = argVals[1].toLong()
                    if (n < 0 || r < 0 || r > n) throw MathException("Domain error for nPr")
                    var res = 1.0
                    for (i in 0 until r) res *= (n - i)
                    res
                }
                "ncr" -> {
                    if (args.size < 2) throw MathException("nCr requires n and r")
                    val n = argVals[0].toLong()
                    val r = min(argVals[1].toLong(), n - argVals[1].toLong())
                    if (n < 0 || r < 0 || r > n) throw MathException("Domain error for nCr")
                    var res = 1.0
                    for (i in 1..r) res = res * (n - i + 1) / i
                    res
                }
                else -> throw MathException("Unknown function '$name'")
            }
        }

        override fun evalFraction(): Fraction {
            throw MathException("Cannot evaluate function to exact fraction")
        }

        private fun toRadians(angle: Double, mode: AngleMode): Double {
            return when (mode) {
                AngleMode.RAD -> angle
                AngleMode.DEG -> Math.toRadians(angle)
                AngleMode.GRAD -> angle * (PI / 200.0)
            }
        }

        private fun fromRadians(rad: Double, mode: AngleMode): Double {
            return when (mode) {
                AngleMode.RAD -> rad
                AngleMode.DEG -> Math.toDegrees(rad)
                AngleMode.GRAD -> rad * (200.0 / PI)
            }
        }
    }

    private class Parser(
        private val tokens: List<Token>,
        private val angleMode: AngleMode
    ) {
        private var pos = 0

        private val current: Token? get() = if (pos < tokens.size) tokens[pos] else null

        fun parse(): ExprNode {
            val node = parseExpression()
            if (pos < tokens.size) {
                val rem = tokens[pos]
                if (rem is Token.CloseParen) {
                    throw MathException("Mismatched closing parenthesis")
                }
                throw MathException("Unexpected symbol near '${tokenString(rem)}'")
            }
            return node
        }

        private fun parseExpression(): ExprNode {
            var left = parseTerm()
            while (true) {
                val tok = current
                if (tok is Token.Operator && (tok.op == '+' || tok.op == '-')) {
                    pos++
                    val right = parseTerm()
                    left = BinaryOpNode(left, tok.op, right)
                } else {
                    break
                }
            }
            return left
        }

        private fun parseTerm(): ExprNode {
            var left = parsePower()
            while (true) {
                val tok = current
                if (tok is Token.Operator && (tok.op == '*' || tok.op == '/' || tok.op == '%')) {
                    pos++
                    val right = parsePower()
                    left = BinaryOpNode(left, tok.op, right)
                } else {
                    break
                }
            }
            return left
        }

        private fun parsePower(): ExprNode {
            val base = parseUnary()
            val tok = current
            return if (tok is Token.Operator && tok.op == '^') {
                pos++
                val exponent = parsePower() // right-associative
                BinaryOpNode(base, '^', exponent)
            } else {
                base
            }
        }

        private fun parseUnary(): ExprNode {
            val tok = current
            if (tok is Token.Operator && (tok.op == '+' || tok.op == '-')) {
                pos++
                val operand = parseUnary()
                return UnaryOpNode(tok.op, operand)
            }
            return parsePostfix()
        }

        private fun parsePostfix(): ExprNode {
            var node = parsePrimary()
            while (true) {
                when (current) {
                    is Token.Factorial -> {
                        pos++
                        node = FactorialNode(node)
                    }
                    is Token.Percent -> {
                        pos++
                        node = PercentNode(node)
                    }
                    else -> break
                }
            }
            return node
        }

        private fun parsePrimary(): ExprNode {
            val tok = current ?: throw MathException("Unexpected end of expression")
            return when (tok) {
                is Token.Number -> {
                    pos++
                    NumberNode(tok.value, tok.text)
                }
                is Token.Identifier -> {
                    pos++
                    when (tok.name) {
                        "pi" -> NumberNode(PI, "π")
                        "e" -> NumberNode(E, "e")
                        "phi" -> NumberNode(1.618033988749895, "φ")
                        else -> {
                            // Function call
                            if (current !is Token.OpenParen) {
                                throw MathException("Expected '(' after function '${tok.name}'")
                            }
                            pos++ // consume '('
                            val args = mutableListOf<ExprNode>()
                            if (current !is Token.CloseParen) {
                                args.add(parseExpression())
                                while (current is Token.Comma) {
                                    pos++ // consume ','
                                    args.add(parseExpression())
                                }
                            }
                            if (current !is Token.CloseParen) {
                                throw MathException("Missing closing parenthesis for '${tok.name}'")
                            }
                            pos++ // consume ')'
                            FunctionNode(tok.name, args, angleMode)
                        }
                    }
                }
                is Token.OpenParen -> {
                    pos++ // consume '('
                    val expr = parseExpression()
                    if (current !is Token.CloseParen) {
                        throw MathException("Missing closing parenthesis")
                    }
                    pos++ // consume ')'
                    expr
                }
                else -> throw MathException("Invalid expression at '${tokenString(tok)}'")
            }
        }

        private fun tokenString(tok: Token): String {
            return when (tok) {
                is Token.Number -> tok.text
                is Token.Identifier -> tok.name
                is Token.Operator -> tok.op.toString()
                Token.OpenParen -> "("
                Token.CloseParen -> ")"
                Token.Comma -> ","
                Token.Factorial -> "!"
                Token.Percent -> "%"
            }
        }
    }
}
