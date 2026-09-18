package com.example.engine

import com.example.math.Fraction
import java.text.DecimalFormat
import kotlin.math.*

sealed interface EquationResult {
    data class Linear(
        val x: Double,
        val exactX: String,
        val steps: List<String>
    ) : EquationResult

    data class Quadratic(
        val a: Double,
        val b: Double,
        val c: Double,
        val discriminant: Double,
        val natureOfRoots: String,
        val root1Display: String,
        val root2Display: String,
        val root1Decimal: Double?,
        val root2Decimal: Double?,
        val steps: List<String>
    ) : EquationResult

    data class LinearSystem2(
        val x: Double,
        val y: Double,
        val det: Double,
        val steps: List<String>
    ) : EquationResult

    data class LinearSystem3(
        val x: Double,
        val y: Double,
        val z: Double,
        val det: Double,
        val steps: List<String>
    ) : EquationResult

    data class Error(val message: String) : EquationResult
}

object EquationSolverEngine {
    private val df = DecimalFormat("#.######")

    fun solveLinear(a: Double, b: Double): EquationResult {
        if (a == 0.0) {
            return if (b == 0.0) {
                EquationResult.Error("Infinite solutions (0 = 0)")
            } else {
                EquationResult.Error("No solution (inconsistent equation)")
            }
        }
        val x = -b / a
        val fraction = Fraction.fromDouble(x)
        val steps = listOf(
            "${df.format(a)}x + ${df.format(b)} = 0",
            "${df.format(a)}x = ${df.format(-b)}",
            "x = ${df.format(-b)} / ${df.format(a)} = ${df.format(x)}"
        )
        return EquationResult.Linear(x, fraction.toProperString(), steps)
    }

    fun solveQuadratic(a: Double, b: Double, c: Double): EquationResult {
        if (a == 0.0) {
            return solveLinear(b, c)
        }
        val d = b * b - 4 * a * c
        val steps = mutableListOf<String>()
        steps.add("Standard form: (${df.format(a)})x² + (${df.format(b)})x + (${df.format(c)}) = 0")
        steps.add("Discriminant Δ = b² - 4ac = (${df.format(b)})² - 4(${df.format(a)})(${df.format(c)}) = ${df.format(d)}")

        return when {
            d > 1e-12 -> {
                val sqrtD = sqrt(d)
                val r1 = (-b + sqrtD) / (2 * a)
                val r2 = (-b - sqrtD) / (2 * a)
                val nature = "Real and distinct roots (Δ > 0)"
                steps.add("Roots x = (-b ± √Δ) / 2a")
                steps.add("x₁ = (${df.format(-b)} + ${df.format(sqrtD)}) / ${df.format(2 * a)} = ${df.format(r1)}")
                steps.add("x₂ = (${df.format(-b)} - ${df.format(sqrtD)}) / ${df.format(2 * a)} = ${df.format(r2)}")

                val exact1 = Fraction.fromDouble(r1).toProperString()
                val exact2 = Fraction.fromDouble(r2).toProperString()

                EquationResult.Quadratic(
                    a, b, c, d, nature,
                    root1Display = "x₁ = ${df.format(r1)} ($exact1)",
                    root2Display = "x₂ = ${df.format(r2)} ($exact2)",
                    root1Decimal = r1,
                    root2Decimal = r2,
                    steps = steps
                )
            }
            abs(d) <= 1e-12 -> {
                val r = -b / (2 * a)
                val nature = "Real and equal roots (Δ = 0)"
                steps.add("Roots x = -b / 2a = ${df.format(-b)} / ${df.format(2 * a)} = ${df.format(r)}")
                val exact = Fraction.fromDouble(r).toProperString()
                EquationResult.Quadratic(
                    a, b, c, d, nature,
                    root1Display = "x = ${df.format(r)} ($exact)",
                    root2Display = "x = ${df.format(r)} ($exact)",
                    root1Decimal = r,
                    root2Decimal = r,
                    steps = steps
                )
            }
            else -> {
                val realPart = -b / (2 * a)
                val imagPart = sqrt(-d) / (2 * abs(a))
                val nature = "Complex conjugate roots (Δ < 0)"
                steps.add("Roots x = -b/2a ± i(√|Δ|/2a)")
                val r1Str = "${df.format(realPart)} + ${df.format(imagPart)}i"
                val r2Str = "${df.format(realPart)} - ${df.format(imagPart)}i"
                EquationResult.Quadratic(
                    a, b, c, d, nature,
                    root1Display = "x₁ = $r1Str",
                    root2Display = "x₂ = $r2Str",
                    root1Decimal = null,
                    root2Decimal = null,
                    steps = steps
                )
            }
        }
    }

    fun solve2x2System(
        a1: Double, b1: Double, c1: Double,
        a2: Double, b2: Double, c2: Double
    ): EquationResult {
        // a1 x + b1 y = c1
        // a2 x + b2 y = c2
        val det = a1 * b2 - a2 * b1
        if (abs(det) < 1e-12) {
            val detX = c1 * b2 - c2 * b1
            return if (abs(detX) < 1e-12) {
                EquationResult.Error("Infinite solutions (dependent equations)")
            } else {
                EquationResult.Error("No solution (parallel inconsistent lines)")
            }
        }
        val detX = c1 * b2 - c2 * b1
        val detY = a1 * c2 - a2 * c1
        val x = detX / det
        val y = detY / det

        val steps = listOf(
            "Equation 1: ${df.format(a1)}x + ${df.format(b1)}y = ${df.format(c1)}",
            "Equation 2: ${df.format(a2)}x + ${df.format(b2)}y = ${df.format(c2)}",
            "Determinant D = (${df.format(a1)})(${df.format(b2)}) - (${df.format(a2)})(${df.format(b1)}) = ${df.format(det)}",
            "D_x = (${df.format(c1)})(${df.format(b2)}) - (${df.format(c2)})(${df.format(b1)}) = ${df.format(detX)}",
            "D_y = (${df.format(a1)})(${df.format(c2)}) - (${df.format(a2)})(${df.format(c1)}) = ${df.format(detY)}",
            "x = D_x / D = ${df.format(x)},  y = D_y / D = ${df.format(y)}"
        )
        return EquationResult.LinearSystem2(x, y, det, steps)
    }

    fun solve3x3System(
        a: DoubleArray, b: DoubleArray, c: DoubleArray, d: DoubleArray
    ): EquationResult {
        // a[i]x + b[i]y + c[i]z = d[i] for i = 0, 1, 2
        fun det3(m0: DoubleArray, m1: DoubleArray, m2: DoubleArray): Double {
            return m0[0] * (m1[1] * m2[2] - m1[2] * m2[1]) -
                    m0[1] * (m1[0] * m2[2] - m1[2] * m2[0]) +
                    m0[2] * (m1[0] * m2[1] - m1[1] * m2[0])
        }

        val detD = det3(
            doubleArrayOf(a[0], b[0], c[0]),
            doubleArrayOf(a[1], b[1], c[1]),
            doubleArrayOf(a[2], b[2], c[2])
        )

        if (abs(detD) < 1e-12) {
            return EquationResult.Error("System has no unique solution (determinant = 0)")
        }

        val detX = det3(
            doubleArrayOf(d[0], b[0], c[0]),
            doubleArrayOf(d[1], b[1], c[1]),
            doubleArrayOf(d[2], b[2], c[2])
        )

        val detY = det3(
            doubleArrayOf(a[0], d[0], c[0]),
            doubleArrayOf(a[1], d[1], c[1]),
            doubleArrayOf(a[2], d[2], c[2])
        )

        val detZ = det3(
            doubleArrayOf(a[0], b[0], d[0]),
            doubleArrayOf(a[1], b[1], d[1]),
            doubleArrayOf(a[2], b[2], d[2])
        )

        val x = detX / detD
        val y = detY / detD
        val z = detZ / detD

        val steps = listOf(
            "Determinant D = ${df.format(detD)}",
            "Cramer's Rule: D_x = ${df.format(detX)}, D_y = ${df.format(detY)}, D_z = ${df.format(detZ)}",
            "x = D_x / D = ${df.format(x)}",
            "y = D_y / D = ${df.format(y)}",
            "z = D_z / D = ${df.format(z)}"
        )
        return EquationResult.LinearSystem3(x, y, z, detD, steps)
    }
}
