package com.example

import com.example.math.renderer.MathExpressionNormalizer
import com.example.math.renderer.ReadableMathFallback
import com.example.math.renderer.UniversalMathParser
import org.junit.Assert.*
import org.junit.Test

/**
 * Verification test suite for CALCX KaTeX Math Rendering Upgrade.
 * Validates the pipeline across all mathematical domains:
 * powers, subscripts, fractions, roots, calculus, linear algebra, Greek symbols, physics, chemistry.
 */
class KatexMathRenderingTest {

    @Test
    fun testSubscriptsAndPowers() {
        // x_1 -> x_{1}
        val subKatex = MathExpressionNormalizer.normalizeToKatex("x_1")
        assertTrue("Subscript should contain _{1}", subKatex.contains("_{1}"))

        // x^2 -> x^{2}
        val powKatex = MathExpressionNormalizer.normalizeToKatex("x^2")
        assertTrue("Power should contain ^{2}", powKatex.contains("^{2}"))

        // a_i -> a_{i}
        val subIndexKatex = MathExpressionNormalizer.normalizeToKatex("a_i")
        assertTrue("Subscript index should contain _{i}", subIndexKatex.contains("_{i}"))
    }

    @Test
    fun testFractionsAndRoots() {
        // (a+b)/c -> \frac{a+b}{c}
        val fracKatex = MathExpressionNormalizer.normalizeToKatex("(a+b)/c")
        assertTrue("Fraction should contain \\frac", fracKatex.contains("\\frac"))

        // sqrt(x) -> \sqrt{x}
        val sqrtKatex = MathExpressionNormalizer.normalizeToKatex("sqrt(x)")
        assertTrue("Square root should contain \\sqrt", sqrtKatex.contains("\\sqrt"))
    }

    @Test
    fun testCalculusDerivativesIntegralsSummations() {
        // d/dx(x^2)
        val derivKatex = MathExpressionNormalizer.normalizeToKatex("d/dx(x^2)")
        assertTrue("Derivative should contain \\frac{d}{dx}", derivKatex.contains("\\frac{d}{dx}"))

        // ∫x² dx or int(x^2)
        val intKatex = MathExpressionNormalizer.normalizeToKatex("int_0^1 x^2 dx")
        assertTrue("Integral should contain \\int", intKatex.contains("\\int"))

        // Summation: sum_{i=1}^n i
        val sumKatex = MathExpressionNormalizer.normalizeToKatex("sum_{i=1}^n i")
        assertTrue("Summation should contain \\sum", sumKatex.contains("\\sum"))

        // Limit: lim x->0 sin(x)/x
        val limKatex = MathExpressionNormalizer.normalizeToKatex("lim_{x \\to 0} \\frac{\\sin(x)}{x}")
        assertTrue("Limit should contain \\lim", limKatex.contains("\\lim"))
    }

    @Test
    fun testMatricesAndVectors() {
        // Matrix [1 2; 3 4]
        val matKatex = MathExpressionNormalizer.normalizeToKatex("[1 2; 3 4]")
        assertTrue("Matrix should contain \\begin{bmatrix} or \\begin{matrix}", matKatex.contains("bmatrix") || matKatex.contains("matrix"))

        // Vector \vec{v}
        val vecKatex = MathExpressionNormalizer.normalizeToKatex("\\vec{v}")
        assertTrue("Vector should contain \\vec", vecKatex.contains("\\vec"))
    }

    @Test
    fun testGreekSymbols() {
        val greekKatex = MathExpressionNormalizer.normalizeToKatex("theta + Delta x + alpha + beta + pi")
        assertTrue("Should contain \\theta", greekKatex.contains("\\theta"))
        assertTrue("Should contain \\Delta", greekKatex.contains("\\Delta"))
        assertTrue("Should contain \\alpha", greekKatex.contains("\\alpha"))
        assertTrue("Should contain \\beta", greekKatex.contains("\\beta"))
        assertTrue("Should contain \\pi", greekKatex.contains("\\pi"))
    }

    @Test
    fun testPhysicsAndChemistryFormulas() {
        // Physics step substitution
        val physKatex = MathExpressionNormalizer.normalizeToKatex("v = 10 + 2_×_5")
        assertFalse("Should not contain raw _×_", physKatex.contains("_×_"))
        assertTrue("Should contain \\times", physKatex.contains("\\times"))

        // Chemical formula
        val chemNode = UniversalMathParser.parse("H2SO4")
        val chemKatex = chemNode.toLatex()
        assertTrue("Chemical formula should contain H and SO4", chemKatex.contains("H") && chemKatex.contains("SO"))
    }

    @Test
    fun testSafeReadableFallbackNeverDisplaysRawLatex() {
        val fallback = ReadableMathFallback.toReadableUnicode("\\frac{a+b}{c} + x^{2} + a_{1} + \\sqrt{x} + \\theta")
        assertFalse("Fallback must not contain \\frac", fallback.contains("\\frac"))
        assertFalse("Fallback must not contain \\sqrt", fallback.contains("\\sqrt"))
        assertFalse("Fallback must not contain \\theta", fallback.contains("\\theta"))
        assertTrue("Fallback contains unicode power ²", fallback.contains("²"))
        assertTrue("Fallback contains unicode subscript ₁", fallback.contains("₁"))
        assertTrue("Fallback contains greek symbol θ", fallback.contains("θ"))
        assertTrue("Fallback contains square root √", fallback.contains("√"))
    }
}
