package com.example.math

import java.math.BigDecimal
import java.math.BigInteger
import java.math.MathContext
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Exact rational number representation supporting arbitrary-precision fractions,
 * mixed fractions, arithmetic, simplification, and conversion to/from decimals.
 */
data class Fraction(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Comparable<Fraction> {

    init {
        require(denominator != BigInteger.ZERO) { "Denominator cannot be zero" }
    }

    val isNegative: Boolean get() = (numerator.signum() * denominator.signum()) < 0

    val absNumerator: BigInteger get() = numerator.abs()
    val absDenominator: BigInteger get() = denominator.abs()

    fun simplified(): Fraction {
        if (numerator == BigInteger.ZERO) return ZERO
        val gcd = numerator.gcd(denominator)
        var num = numerator.divide(gcd)
        var den = denominator.divide(gcd)
        if (den.signum() < 0) {
            num = num.negate()
            den = den.negate()
        }
        return Fraction(num, den)
    }

    operator fun plus(other: Fraction): Fraction {
        val num = this.numerator.multiply(other.denominator).add(other.numerator.multiply(this.denominator))
        val den = this.denominator.multiply(other.denominator)
        return Fraction(num, den).simplified()
    }

    operator fun minus(other: Fraction): Fraction {
        val num = this.numerator.multiply(other.denominator).subtract(other.numerator.multiply(this.denominator))
        val den = this.denominator.multiply(other.denominator)
        return Fraction(num, den).simplified()
    }

    operator fun times(other: Fraction): Fraction {
        val num = this.numerator.multiply(other.numerator)
        val den = this.denominator.multiply(other.denominator)
        return Fraction(num, den).simplified()
    }

    operator fun div(other: Fraction): Fraction {
        require(other.numerator != BigInteger.ZERO) { "Division by zero" }
        val num = this.numerator.multiply(other.denominator)
        val den = this.denominator.multiply(other.numerator)
        return Fraction(num, den).simplified()
    }

    operator fun unaryMinus(): Fraction = Fraction(numerator.negate(), denominator)

    fun toDouble(): Double {
        return numerator.toDouble() / denominator.toDouble()
    }

    fun toBigDecimal(scale: Int = 12): BigDecimal {
        return BigDecimal(numerator).divide(
            BigDecimal(denominator),
            MathContext(scale, RoundingMode.HALF_UP)
        )
    }

    fun toProperString(): String {
        val simp = simplified()
        if (simp.denominator == BigInteger.ONE) {
            return simp.numerator.toString()
        }
        return "${simp.numerator}/${simp.denominator}"
    }

    override fun toString(): String = toProperString()

    fun toMixedString(): String {
        val simp = simplified()
        if (simp.denominator == BigInteger.ONE) {
            return simp.numerator.toString()
        }
        val whole = simp.numerator.divide(simp.denominator)
        val rem = simp.numerator.remainder(simp.denominator).abs()
        return if (whole == BigInteger.ZERO) {
            "${simp.numerator}/${simp.denominator}"
        } else {
            "$whole ${rem}/${simp.denominator}"
        }
    }

    override fun compareTo(other: Fraction): Int {
        val left = this.numerator.multiply(other.denominator)
        val right = other.numerator.multiply(this.denominator)
        return left.compareTo(right)
    }

    companion object {
        val ZERO = Fraction(BigInteger.ZERO, BigInteger.ONE)
        val ONE = Fraction(BigInteger.ONE, BigInteger.ONE)

        fun of(n: Long, d: Long = 1L): Fraction {
            return Fraction(BigInteger.valueOf(n), BigInteger.valueOf(d)).simplified()
        }

        fun fromDouble(value: Double, maxDenominator: Long = 1000000L): Fraction {
            if (value.isNaN() || value.isInfinite()) return ZERO
            val sign = if (value < 0) -1 else 1
            val x = abs(value)
            var m00 = 1L
            var m01 = 0L
            var m10 = 0L
            var m11 = 1L

            var currentX = x
            while (m10 * (currentX.toLong()) + m11 <= maxDenominator) {
                val a = currentX.toLong()
                val t0 = m00 * a + m01
                m01 = m00
                m00 = t0

                val t1 = m10 * a + m11
                m11 = m10
                m10 = t1

                val rem = currentX - a
                if (rem < 1e-12) break
                currentX = 1.0 / rem
            }

            return Fraction(
                BigInteger.valueOf(sign * m00),
                BigInteger.valueOf(m10)
            ).simplified()
        }
    }
}
