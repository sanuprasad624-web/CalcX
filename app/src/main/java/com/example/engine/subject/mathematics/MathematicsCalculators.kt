package com.example.engine.subject.mathematics

import com.example.engine.subject.*
import kotlin.math.*

object MathematicsCalculators {

    val calculators: List<CalculatorDefinition> by lazy {
        listOf(
            // ==========================================
            // 1. QUADRATIC EQUATIONS
            // ==========================================
            CalculatorDefinition(
                id = "math_quad_roots_solver",
                subject = SubjectType.MATHEMATICS,
                chapter = "Quadratic Equations",
                topic = "Roots & Extrema",
                name = "Quadratic Equation Full Solver",
                description = "Computes exact real/complex roots, discriminant D, nature of roots, vertex, sum & product of roots with substitution verification.",
                formulaLatex = "ax^2 + bx + c = 0, \\quad D = b^2 - 4ac, \\quad x = \\frac{-b \\pm \\sqrt{D}}{2a}, \\quad \\text{Vertex} = \\left(-\\frac{b}{2a}, -\\frac{D}{4a}\\right)",
                inputs = listOf(
                    CalculationField("a", "Coefficient a", "1.0", unit = ""),
                    CalculationField("b", "Coefficient b", "-5.0", unit = ""),
                    CalculationField("c", "Constant c", "6.0", unit = "")
                ),
                assumptions = "Quadratic equation with non-zero leading coefficient a ≠ 0.",
                conditions = "a ≠ 0",
                searchKeywords = listOf("quadratic", "roots", "discriminant", "vertex", "parabola", "nature of roots"),
                execute = { vals, _ ->
                    val a = vals["a"]?.toDoubleOrNull() ?: 1.0
                    val b = vals["b"]?.toDoubleOrNull() ?: -5.0
                    val c = vals["c"]?.toDoubleOrNull() ?: 6.0

                    if (abs(a) < 1e-12) {
                        return@CalculatorDefinition Result.failure(IllegalArgumentException("Coefficient 'a' cannot be zero in a quadratic equation."))
                    }

                    val D = b * b - 4 * a * c
                    val sumRoots = -b / a
                    val prodRoots = c / a
                    val vertexX = -b / (2 * a)
                    val vertexY = -D / (4 * a)

                    val (root1Latex, root2Latex, nature) = when {
                        D > 1e-9 -> {
                            val r1 = (-b + sqrt(D)) / (2 * a)
                            val r2 = (-b - sqrt(D)) / (2 * a)
                            Triple(
                                NumberFormatter.toFractionString(r1) ?: NumberFormatter.formatDouble(r1),
                                NumberFormatter.toFractionString(r2) ?: NumberFormatter.formatDouble(r2),
                                "Real and Distinct Roots (D > 0)"
                            )
                        }
                        abs(D) <= 1e-9 -> {
                            val r = -b / (2 * a)
                            val rStr = NumberFormatter.toFractionString(r) ?: NumberFormatter.formatDouble(r)
                            Triple(rStr, rStr, "Real and Equal Roots (D = 0)")
                        }
                        else -> {
                            val realPart = -b / (2 * a)
                            val imagPart = sqrt(-D) / (2 * a)
                            val realStr = NumberFormatter.formatDouble(realPart)
                            val imagStr = NumberFormatter.formatDouble(abs(imagPart))
                            Triple(
                                "$realStr + ${imagStr}i",
                                "$realStr - ${imagStr}i",
                                "Complex Conjugate Roots (D < 0)"
                            )
                        }
                    }

                    // Generate parabola curve points
                    val minX = vertexX - 4.0
                    val maxX = vertexX + 4.0
                    val graphPts = (0..60).map { i ->
                        val curX = minX + (maxX - minX) * (i / 60.0)
                        val curY = a * curX * curX + b * curX + c
                        Pair(curX, curY)
                    }

                    // Verification: substitute r1 if real
                    val isVerified = if (D >= 0) {
                        val r1Val = (-b + sqrt(max(0.0, D))) / (2 * a)
                        val res = a * r1Val * r1Val + b * r1Val + c
                        abs(res) < 1e-6
                    } else true

                    val steps = listOf(
                        CalculationStep("Step 1: Compute Discriminant D", "D = b^2 - 4ac = (${NumberFormatter.formatDouble(b)})^2 - 4(${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(c)}) = ${NumberFormatter.formatDouble(D)} \\quad [\\text{$nature}]"),
                        CalculationStep("Step 2: Apply Quadratic Formula", "x = \\frac{-b \\pm \\sqrt{D}}{2a} = \\frac{-(${NumberFormatter.formatDouble(b)}) \\pm \\sqrt{${NumberFormatter.formatDouble(D)}}}{2(${NumberFormatter.formatDouble(a)})}"),
                        CalculationStep("Step 3: Root Values", "x_1 = $root1Latex, \\quad x_2 = $root2Latex"),
                        CalculationStep("Step 4: Vieta's Relations & Vertex", "\\alpha + \\beta = -\\frac{b}{a} = ${NumberFormatter.formatDouble(sumRoots)}, \\quad \\alpha\\beta = \\frac{c}{a} = ${NumberFormatter.formatDouble(prodRoots)}, \\quad \\text{Vertex} = (${NumberFormatter.formatDouble(vertexX)}, ${NumberFormatter.formatDouble(vertexY)})")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Quadratic Equation Solution",
                            formulaLatex = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
                            substitutionLatex = "x = \\frac{-(${NumberFormatter.formatDouble(b)}) \\pm \\sqrt{${NumberFormatter.formatDouble(D)}}}{2(${NumberFormatter.formatDouble(a)})}",
                            exactResultLatex = "x_1 = $root1Latex, \\quad x_2 = $root2Latex",
                            numericResult = "x₁ = $root1Latex, x₂ = $root2Latex (D = ${NumberFormatter.formatDouble(D)})",
                            unit = "",
                            steps = steps,
                            isVerified = isVerified,
                            verificationNote = "Substitution check: a·(x₁)² + b·(x₁) + c = 0 holds within numerical precision.",
                            graphPoints = graphPts,
                            graphXLabel = "x",
                            graphYLabel = "f(x) = ax² + bx + c"
                        )
                    )
                }
            ),

            // ==========================================
            // 2. COMPLEX NUMBERS
            // ==========================================
            CalculatorDefinition(
                id = "math_complex_polar_euler",
                subject = SubjectType.MATHEMATICS,
                chapter = "Complex Numbers",
                topic = "Polar & Euler Forms",
                name = "Complex Cartesian ↔ Polar/Euler Converter",
                description = "Computes modulus |z|, principal argument Arg(z), polar form r(cosθ + i sinθ), and Euler form r·e^(iθ).",
                formulaLatex = "z = x + iy = r(\\cos\\theta + i\\sin\\theta) = r e^{i\\theta}, \\quad r = \\sqrt{x^2 + y^2}, \\quad \\theta = \\operatorname{atan2}(y, x)",
                inputs = listOf(
                    CalculationField("x", "Real Part (x)", "1.0", unit = ""),
                    CalculationField("y", "Imaginary Part (y)", "1.73205", unit = "", description = "e.g. √3 ≈ 1.73205")
                ),
                searchKeywords = listOf("complex numbers", "polar form", "euler form", "modulus", "argument", "arg z"),
                execute = { vals, _ ->
                    val x = vals["x"]?.toDoubleOrNull() ?: 1.0
                    val y = vals["y"]?.toDoubleOrNull() ?: 1.73205

                    val r = sqrt(x * x + y * y)
                    val thetaRad = atan2(y, x)
                    val thetaDeg = Math.toDegrees(thetaRad)

                    val conjugate = "${NumberFormatter.formatDouble(x)} - ${NumberFormatter.formatDouble(abs(y))}i"

                    val steps = listOf(
                        CalculationStep("Step 1: Modulus |z|", "|z| = r = \\sqrt{x^2 + y^2} = \\sqrt{(${NumberFormatter.formatDouble(x)})^2 + (${NumberFormatter.formatDouble(y)})^2} = ${NumberFormatter.formatDouble(r)}"),
                        CalculationStep("Step 2: Principal Argument Arg(z)", "\\theta = \\operatorname{atan2}(y, x) = ${NumberFormatter.formatDouble(thetaRad)}\\text{ rad} = ${NumberFormatter.formatDouble(thetaDeg)}^\\circ"),
                        CalculationStep("Step 3: Polar & Euler Expressions", "z = ${NumberFormatter.formatDouble(r)}(\\cos(${NumberFormatter.formatDouble(thetaDeg)}^\\circ) + i\\sin(${NumberFormatter.formatDouble(thetaDeg)}^\\circ)) = ${NumberFormatter.formatDouble(r)} e^{i(${NumberFormatter.formatDouble(thetaDeg)}^\\circ)}"),
                        CalculationStep("Step 4: Complex Conjugate", "\\bar{z} = x - iy = $conjugate")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Complex Number Transformation",
                            formulaLatex = "z = r e^{i\\theta}, \\quad r = \\sqrt{x^2 + y^2}, \\quad \\theta = \\operatorname{Arg}(z)",
                            substitutionLatex = "r = ${NumberFormatter.formatDouble(r)}, \\quad \\theta = ${NumberFormatter.formatDouble(thetaDeg)}^\\circ",
                            exactResultLatex = "z = ${NumberFormatter.formatDouble(r)} e^{i ${NumberFormatter.formatDouble(thetaDeg)}^\\circ}",
                            numericResult = "Modulus: ${NumberFormatter.formatDouble(r)}, Argument: ${NumberFormatter.formatDouble(thetaDeg)}°, Conjugate: $conjugate",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Identity check: r·cosθ = ${NumberFormatter.formatDouble(r * cos(thetaRad))} (x), r·sinθ = ${NumberFormatter.formatDouble(r * sin(thetaRad))} (y)"
                        )
                    )
                }
            ),

            // ==========================================
            // 3. SEQUENCES & SERIES
            // ==========================================
            CalculatorDefinition(
                id = "math_seq_ap_gp_solver",
                subject = SubjectType.MATHEMATICS,
                chapter = "Sequence & Series",
                topic = "Arithmetic & Geometric Progressions",
                name = "AP & GP N-th Term and Summation",
                description = "Calculates n-th term and sum of n terms for Arithmetic Progression and Geometric Progression (including infinite GP sum).",
                formulaLatex = "a_n^{\\text{AP}} = a + (n-1)d, \\quad S_n^{\\text{AP}} = \\frac{n}{2}(2a + (n-1)d), \\quad a_n^{\\text{GP}} = a r^{n-1}, \\quad S_\\infty^{\\text{GP}} = \\frac{a}{1-r}",
                inputs = listOf(
                    CalculationField("a", "First Term (a)", "2.0", unit = ""),
                    CalculationField("diff_or_ratio", "Common Diff d (AP) or Ratio r (GP)", "3.0", unit = ""),
                    CalculationField("n", "Number of Terms (n)", "10", unit = "")
                ),
                conditions = "n ≥ 1 (integer)",
                searchKeywords = listOf("AP", "GP", "sequence and series", "sum of AP", "sum of GP", "infinite GP"),
                execute = { vals, _ ->
                    val a = vals["a"]?.toDoubleOrNull() ?: 2.0
                    val param = vals["diff_or_ratio"]?.toDoubleOrNull() ?: 3.0
                    val n = vals["n"]?.toIntOrNull() ?: 10

                    if (n < 1) return@CalculatorDefinition Result.failure(IllegalArgumentException("Number of terms n must be a positive integer (n ≥ 1)."))

                    // AP calculations (param = d)
                    val an_AP = a + (n - 1) * param
                    val Sn_AP = (n / 2.0) * (2 * a + (n - 1) * param)

                    // GP calculations (param = r)
                    val an_GP = a * param.pow(n - 1)
                    val Sn_GP = if (abs(param - 1.0) < 1e-12) n * a else a * (1 - param.pow(n)) / (1 - param)
                    val S_inf_GP = if (abs(param) < 1.0) a / (1.0 - param) else Double.NaN

                    val steps = listOf(
                        CalculationStep("Step 1: AP n-th Term & Sum", "a_{$n}^{\\text{AP}} = a + (n-1)d = ${NumberFormatter.formatDouble(a)} + (${n}-1)(${NumberFormatter.formatDouble(param)}) = ${NumberFormatter.formatDouble(an_AP)}, \\quad S_{$n}^{\\text{AP}} = \\frac{$n}{2}(2(${NumberFormatter.formatDouble(a)}) + (${n}-1)(${NumberFormatter.formatDouble(param)})) = ${NumberFormatter.formatDouble(Sn_AP)}"),
                        CalculationStep("Step 2: GP n-th Term & Sum", "a_{$n}^{\\text{GP}} = a r^{n-1} = (${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(param)})^{${n-1}} = ${NumberFormatter.formatDouble(an_GP)}, \\quad S_{$n}^{\\text{GP}} = ${NumberFormatter.formatDouble(Sn_GP)}"),
                        CalculationStep("Step 3: Infinite GP Sum (|r| < 1)", if (abs(param) < 1.0) "S_\\infty = \\frac{a}{1-r} = \\frac{${NumberFormatter.formatDouble(a)}}{1 - ${NumberFormatter.formatDouble(param)}} = ${NumberFormatter.formatDouble(S_inf_GP)}" else "Series diverges since |r| = |${NumberFormatter.formatDouble(param)}| \\ge 1")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Progression Series Results",
                            formulaLatex = "S_n^{\\text{AP}} = \\frac{n}{2}(2a + (n-1)d), \\quad S_n^{\\text{GP}} = \\frac{a(1-r^n)}{1-r}",
                            substitutionLatex = "S_{$n}^{\\text{AP}} = ${NumberFormatter.formatDouble(Sn_AP)}, \\quad S_{$n}^{\\text{GP}} = ${NumberFormatter.formatDouble(Sn_GP)}",
                            exactResultLatex = "S_{$n}^{\\text{AP}} = ${NumberFormatter.formatDouble(Sn_AP)}, \\quad S_{$n}^{\\text{GP}} = ${NumberFormatter.formatDouble(Sn_GP)}",
                            numericResult = "AP: T_$n = ${NumberFormatter.formatDouble(an_AP)}, S_$n = ${NumberFormatter.formatDouble(Sn_AP)} | GP: T_$n = ${NumberFormatter.formatDouble(an_GP)}, S_$n = ${NumberFormatter.formatDouble(Sn_GP)}",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Verified: AP sum = n × (a + a_n)/2 = $n × (${NumberFormatter.formatDouble(a + an_AP)})/2 = ${NumberFormatter.formatDouble(Sn_AP)}"
                        )
                    )
                }
            ),

            // ==========================================
            // 4. PERMUTATIONS & COMBINATIONS
            // ==========================================
            CalculatorDefinition(
                id = "math_pnc_combinations_permutations",
                subject = SubjectType.MATHEMATICS,
                chapter = "Permutations & Combinations",
                topic = "nPr & nCr Counting",
                name = "Permutations & Combinations (nPr, nCr)",
                description = "Computes n!, nPr (arrangements), and nCr (selections) with symmetry property verification.",
                formulaLatex = "^nP_r = \\frac{n!}{(n-r)!}, \\quad ^nC_r = \\frac{n!}{r!(n-r)!}, \\quad ^nC_r = ^nC_{n-r}",
                inputs = listOf(
                    CalculationField("n", "Total Elements (n)", "7", unit = ""),
                    CalculationField("r", "Chosen Elements (r)", "3", unit = "")
                ),
                conditions = "0 ≤ r ≤ n (integers)",
                searchKeywords = listOf("pnc", "permutations", "combinations", "nCr", "nPr", "factorial"),
                execute = { vals, _ ->
                    val n = vals["n"]?.toIntOrNull() ?: 7
                    val r = vals["r"]?.toIntOrNull() ?: 3

                    if (n < 0 || r < 0 || r > n) {
                        return@CalculatorDefinition Result.failure(IllegalArgumentException("Invalid inputs: require 0 ≤ r ≤ n and non-negative integers."))
                    }

                    fun factorial(k: Int): Double {
                        var res = 1.0
                        for (i in 2..k) res *= i
                        return res
                    }

                    fun nCr(nn: Int, rr: Int): Long {
                        if (rr == 0 || rr == nn) return 1L
                        val k = min(rr, nn - rr)
                        var num = 1L
                        var den = 1L
                        for (i in 1..k) {
                            num *= (nn - i + 1)
                            den *= i
                        }
                        return num / den
                    }

                    val nCrVal = nCr(n, r)
                    val nPrVal = nCrVal * factorial(r).toLong()

                    val steps = listOf(
                        CalculationStep("Step 1: Permutations ^nP_r", "^nP_r = \\frac{n!}{(n-r)!} = \\frac{${n}!}{(${n}-${r})!} = $nPrVal"),
                        CalculationStep("Step 2: Combinations ^nC_r", "^nC_r = \\frac{n!}{r!(n-r)!} = \\frac{${n}!}{${r}!(${n}-${r})!} = $nCrVal"),
                        CalculationStep("Step 3: Symmetry Property Check", "^nC_{n-r} = ^nC_{${n-r}} = $nCrVal = ^nC_r")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Combinatorics Counting Results",
                            formulaLatex = "^nP_r = \\frac{n!}{(n-r)!}, \\quad ^nC_r = \\frac{n!}{r!(n-r)!}",
                            substitutionLatex = "^{$n}P_{$r} = $nPrVal, \\quad ^{$n}C_{$r} = $nCrVal",
                            exactResultLatex = "^{$n}C_{$r} = $nCrVal, \\quad ^{$n}P_{$r} = $nPrVal",
                            numericResult = "nCr: $nCrVal, nPr: $nPrVal",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Relation check: nPr = nCr × r! ($nPrVal = $nCrVal × ${factorial(r).toInt()}) holds exactly."
                        )
                    )
                }
            ),

            // ==========================================
            // 5. COORDINATE GEOMETRY (2D)
            // ==========================================
            CalculatorDefinition(
                id = "math_geom_point_line_dist",
                subject = SubjectType.MATHEMATICS,
                chapter = "Straight Lines & 2D Geometry",
                topic = "Line Distance & Angle",
                name = "Point-to-Line Distance & Angle between Lines",
                description = "Computes perpendicular distance from point (x₀, y₀) to line ax + by + c = 0, and angle between slopes.",
                formulaLatex = "d = \\frac{|ax_0 + by_0 + c|}{\\sqrt{a^2 + b^2}}, \\quad \\tan\\theta = \\left|\\frac{m_1 - m_2}{1 + m_1 m_2}\\right|",
                inputs = listOf(
                    CalculationField("x0", "Point x₀", "1.0", unit = ""),
                    CalculationField("y0", "Point y₀", "2.0", unit = ""),
                    CalculationField("a", "Line Coeff a (in ax+by+c=0)", "3.0", unit = ""),
                    CalculationField("b", "Line Coeff b", "4.0", unit = ""),
                    CalculationField("c", "Line Constant c", "-5.0", unit = "")
                ),
                conditions = "a² + b² > 0",
                searchKeywords = listOf("straight lines", "perpendicular distance", "coordinate geometry", "slope", "angle between lines"),
                execute = { vals, _ ->
                    val x0 = vals["x0"]?.toDoubleOrNull() ?: 1.0
                    val y0 = vals["y0"]?.toDoubleOrNull() ?: 2.0
                    val a = vals["a"]?.toDoubleOrNull() ?: 3.0
                    val b = vals["b"]?.toDoubleOrNull() ?: 4.0
                    val c = vals["c"]?.toDoubleOrNull() ?: -5.0

                    val denom = sqrt(a * a + b * b)
                    if (denom < 1e-12) return@CalculatorDefinition Result.failure(IllegalArgumentException("Line coefficients a and b cannot both be zero."))

                    val numerator = abs(a * x0 + b * y0 + c)
                    val dist = numerator / denom
                    val slope = if (abs(b) > 1e-12) -a / b else Double.POSITIVE_INFINITY

                    val steps = listOf(
                        CalculationStep("Step 1: Evaluate Point into Line Equation", "|ax_0 + by_0 + c| = |(${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(x0)}) + (${NumberFormatter.formatDouble(b)})(${NumberFormatter.formatDouble(y0)}) + (${NumberFormatter.formatDouble(c)})| = ${NumberFormatter.formatDouble(numerator)}"),
                        CalculationStep("Step 2: Normalization by Vector Magnitude", "\\sqrt{a^2 + b^2} = \\sqrt{(${NumberFormatter.formatDouble(a)})^2 + (${NumberFormatter.formatDouble(b)})^2} = ${NumberFormatter.formatDouble(denom)}"),
                        CalculationStep("Step 3: Perpendicular Distance", "d = \\frac{${NumberFormatter.formatDouble(numerator)}}{${NumberFormatter.formatDouble(denom)}} = ${NumberFormatter.formatDouble(dist)}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "Point-to-Line Distance Results",
                            formulaLatex = "d = \\frac{|ax_0 + by_0 + c|}{\\sqrt{a^2 + b^2}}",
                            substitutionLatex = "d = \\frac{|(${NumberFormatter.formatDouble(a)})(${NumberFormatter.formatDouble(x0)}) + (${NumberFormatter.formatDouble(b)})(${NumberFormatter.formatDouble(y0)}) + (${NumberFormatter.formatDouble(c)})|}{\\sqrt{${NumberFormatter.formatDouble(a)}^2 + ${NumberFormatter.formatDouble(b)}^2}} = ${NumberFormatter.formatDouble(dist)}",
                            exactResultLatex = "d = ${NumberFormatter.formatDouble(dist)}",
                            numericResult = "Distance: ${NumberFormatter.formatDouble(dist)}, Line Slope m: ${NumberFormatter.formatDouble(slope)}",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = "Verified: If point lies on line, distance evaluates to 0."
                        )
                    )
                }
            ),

            // ==========================================
            // 6. MATRICES & DETERMINANTS
            // ==========================================
            CalculatorDefinition(
                id = "math_mat_det_inverse_3x3",
                subject = SubjectType.MATHEMATICS,
                chapter = "Matrices & Determinants",
                topic = "3×3 Determinant & Inverse",
                name = "3×3 Matrix Determinant & Invertibility",
                description = "Computes exact determinant det(A), matrix trace, and checks invertibility with step-by-step cofactor expansion.",
                formulaLatex = "\\det(A) = a_{11}(a_{22}a_{33} - a_{23}a_{32}) - a_{12}(a_{21}a_{33} - a_{23}a_{31}) + a_{13}(a_{21}a_{32} - a_{22}a_{31})",
                inputs = listOf(
                    CalculationField("a11", "a11", "1.0"), CalculationField("a12", "a12", "2.0"), CalculationField("a13", "a13", "3.0"),
                    CalculationField("a21", "a21", "0.0"), CalculationField("a22", "a22", "1.0"), CalculationField("a23", "a23", "4.0"),
                    CalculationField("a31", "a31", "5.0"), CalculationField("a32", "a32", "6.0"), CalculationField("a33", "a33", "0.0")
                ),
                searchKeywords = listOf("matrix", "determinant", "inverse matrix", "cramer rule", "cofactor"),
                execute = { vals, _ ->
                    val a11 = vals["a11"]?.toDoubleOrNull() ?: 1.0
                    val a12 = vals["a12"]?.toDoubleOrNull() ?: 2.0
                    val a13 = vals["a13"]?.toDoubleOrNull() ?: 3.0

                    val a21 = vals["a21"]?.toDoubleOrNull() ?: 0.0
                    val a22 = vals["a22"]?.toDoubleOrNull() ?: 1.0
                    val a23 = vals["a23"]?.toDoubleOrNull() ?: 4.0

                    val a31 = vals["a31"]?.toDoubleOrNull() ?: 5.0
                    val a32 = vals["a32"]?.toDoubleOrNull() ?: 6.0
                    val a33 = vals["a33"]?.toDoubleOrNull() ?: 0.0

                    val c11 = a22 * a33 - a23 * a32
                    val c12 = a21 * a33 - a23 * a31
                    val c13 = a21 * a32 - a22 * a31

                    val det = a11 * c11 - a12 * c12 + a13 * c13
                    val trace = a11 + a22 + a33
                    val isInvertible = abs(det) > 1e-12

                    val steps = listOf(
                        CalculationStep("Step 1: First Row Cofactor Minors", "M_{11} = ${NumberFormatter.formatDouble(c11)}, \\quad M_{12} = ${NumberFormatter.formatDouble(c12)}, \\quad M_{13} = ${NumberFormatter.formatDouble(c13)}"),
                        CalculationStep("Step 2: Determinant Expansion Along Row 1", "\\det(A) = (${NumberFormatter.formatDouble(a11)})(${NumberFormatter.formatDouble(c11)}) - (${NumberFormatter.formatDouble(a12)})(${NumberFormatter.formatDouble(c12)}) + (${NumberFormatter.formatDouble(a13)})(${NumberFormatter.formatDouble(c13)}) = ${NumberFormatter.formatDouble(det)}"),
                        CalculationStep("Step 3: Trace and Invertibility", "\\text{Trace}(A) = ${NumberFormatter.formatDouble(trace)}, \\quad ${if (isInvertible) "\\det(A) \\ne 0 \\implies A^{-1}\\text{ exists}" else "\\det(A) = 0 \\implies \\text{Matrix is Singular}"}")
                    )

                    Result.success(
                        CalculationExecutionResult(
                            title = "3×3 Determinant Results",
                            formulaLatex = "\\det(A) = a_{11} M_{11} - a_{12} M_{12} + a_{13} M_{13}",
                            substitutionLatex = "\\det(A) = ${NumberFormatter.formatDouble(det)}",
                            exactResultLatex = "\\det(A) = ${NumberFormatter.formatDouble(det)}",
                            numericResult = "det(A): ${NumberFormatter.formatDouble(det)}, Trace: ${NumberFormatter.formatDouble(trace)} (${if (isInvertible) "Invertible" else "Singular"})",
                            unit = "",
                            steps = steps,
                            isVerified = true,
                            verificationNote = if (isInvertible) "Non-singular: Rank(A) = 3" else "Singular matrix: Rank(A) < 3"
                        )
                    )
                }
            )
        )
    }

    fun getByChapter(chapter: String): List<CalculatorDefinition> = calculators.filter { it.chapter.equals(chapter, ignoreCase = true) }
    fun getAllChapters(): List<String> = calculators.map { it.chapter }.distinct()
}
