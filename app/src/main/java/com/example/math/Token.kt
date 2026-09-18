package com.example.math

sealed interface Token {
    data class Number(val value: Double, val text: String) : Token
    data class Identifier(val name: String) : Token
    data class Operator(val op: Char) : Token
    data object OpenParen : Token
    data object CloseParen : Token
    data object Comma : Token
    data object Factorial : Token
    data object Percent : Token
}

enum class AngleMode {
    DEG, RAD, GRAD
}

class Tokenizer(private val input: String) {
    private var index = 0

    fun tokenize(): List<Token> {
        val tokens = mutableListOf<Token>()
        while (index < input.length) {
            val c = input[index]
            when {
                c.isWhitespace() -> {
                    index++
                }
                c.isDigit() || c == '.' -> {
                    tokens.add(readNumber())
                }
                c.isLetter() || c == 'π' || c == '√' -> {
                    tokens.add(readIdentifierOrSpecial())
                }
                c == '(' -> {
                    tokens.add(Token.OpenParen)
                    index++
                }
                c == ')' -> {
                    tokens.add(Token.CloseParen)
                    index++
                }
                c == ',' -> {
                    tokens.add(Token.Comma)
                    index++
                }
                c == '!' -> {
                    tokens.add(Token.Factorial)
                    index++
                }
                c == '%' -> {
                    tokens.add(Token.Percent)
                    index++
                }
                c in "+-×*÷/^−" -> {
                    val normalizedOp = when (c) {
                        '×' -> '*'
                        '÷' -> '/'
                        '−' -> '-'
                        else -> c
                    }
                    tokens.add(Token.Operator(normalizedOp))
                    index++
                }
                else -> {
                    // Unknown character, skip or error
                    index++
                }
            }
        }
        return tokens
    }

    private fun readNumber(): Token.Number {
        val start = index
        var hasDot = false
        var hasExp = false

        while (index < input.length) {
            val c = input[index]
            if (c.isDigit()) {
                index++
            } else if (c == '.' && !hasDot && !hasExp) {
                hasDot = true
                index++
            } else if ((c == 'e' || c == 'E') && !hasExp && index > start) {
                // Check if next char is digit or + / -
                val nextIndex = index + 1
                if (nextIndex < input.length && (input[nextIndex].isDigit() || input[nextIndex] == '+' || input[nextIndex] == '-')) {
                    hasExp = true
                    index++
                    if (input[index] == '+' || input[index] == '-') {
                        index++
                    }
                } else {
                    break
                }
            } else {
                break
            }
        }
        val text = input.substring(start, index)
        val value = text.toDoubleOrNull() ?: 0.0
        return Token.Number(value, text)
    }

    private fun readIdentifierOrSpecial(): Token {
        val c = input[index]
        if (c == 'π') {
            index++
            return Token.Identifier("pi")
        }
        if (c == '√') {
            index++
            return Token.Identifier("sqrt")
        }

        val start = index
        while (index < input.length && (input[index].isLetter() || input[index].isDigit())) {
            index++
        }
        val name = input.substring(start, index).lowercase()
        return Token.Identifier(name)
    }
}
