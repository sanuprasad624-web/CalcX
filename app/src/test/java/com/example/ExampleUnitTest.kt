package com.example

import com.example.engine.*
import com.example.math.*
import com.example.math.calculus.*
import com.example.math.graph.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testFractionArithmetic() {
        val f1 = Fraction.of(1, 2)
        val f2 = Fraction.of(1, 3)

        val sum = f1 + f2
        assertEquals("5/6", sum.toString())

        val diff = f1 - f2
        assertEquals("1/6", diff.toString())

        val prod = f1 * f2
        assertEquals("1/6", prod.toString())

        val div = f1 / f2
        assertEquals("3/2", div.toString())
    }

    @Test
    fun testExpressionEvaluatorOrderOfOperations() {
        val evaluator = ExpressionEvaluator(angleMode = AngleMode.DEG)
        val result = evaluator.evaluate("2 + 3 * 4")
        assertTrue(result.isSuccess)
        assertEquals(14.0, result.value, 1e-6)
    }

    @Test
    fun testExpressionEvaluatorTrigonometry() {
        val evaluator = ExpressionEvaluator(angleMode = AngleMode.DEG)
        val resultSin30 = evaluator.evaluate("sin(30)")
        assertTrue(resultSin30.isSuccess)
        assertEquals(0.5, resultSin30.value, 1e-4)

        val resultCos60 = evaluator.evaluate("cos(60)")
        assertTrue(resultCos60.isSuccess)
        assertEquals(0.5, resultCos60.value, 1e-4)
    }

    @Test
    fun testExpressionEvaluatorImplicitMultiplication() {
        val evaluator = ExpressionEvaluator(angleMode = AngleMode.DEG)
        val result = evaluator.evaluate("2(3 + 4)")
        assertTrue(result.isSuccess)
        assertEquals(14.0, result.value, 1e-6)
    }

    @Test
    fun testEquationSolverQuadratic() {
        // x^2 - 5x + 6 = 0 -> roots are 3 and 2
        val result = EquationSolverEngine.solveQuadratic(1.0, -5.0, 6.0)
        assertTrue(result is EquationResult.Quadratic)
        val q = result as EquationResult.Quadratic
        assertEquals(3.0, q.root1Decimal ?: 0.0, 1e-6)
        assertEquals(2.0, q.root2Decimal ?: 0.0, 1e-6)
    }

    @Test
    fun testMatrixDeterminantAndInverse() {
        val mat = Matrix(
            2, 2,
            arrayOf(
                doubleArrayOf(4.0, 7.0),
                doubleArrayOf(2.0, 6.0)
            )
        )
        val det = mat.determinant()
        assertEquals(10.0, det, 1e-6)

        val inv = mat.inverse()
        val product = mat * inv
        assertEquals(1.0, product.data[0][0], 1e-6)
        assertEquals(0.0, product.data[0][1], 1e-6)
        assertEquals(0.0, product.data[1][0], 1e-6)
        assertEquals(1.0, product.data[1][1], 1e-6)
    }

    @Test
    fun testUnitConversion() {
        val mUnit = UnitItem("m", "Meter", "m", 1.0)
        val kmUnit = UnitItem("km", "Kilometer", "km", 1000.0)
        val converted = UnitConversionEngine.convert(5000.0, mUnit, kmUnit)
        assertEquals(5.0, converted, 1e-6)
    }

    @Test
    fun testProgrammerBaseConversions() {
        val bases = ProgrammerEngine.formatAllBases(255L, BitWordSize.BYTE)
        assertEquals("FF", bases["HEX"])
        assertEquals("377", bases["OCT"])
        assertEquals("1111 1111", bases["BIN"])
    }

    @Test
    fun testCalculusSymbolicDifferentiation() {
        // d/dx (x^3 - 3*x^2 + 4) -> 3*x^2 - 6*x
        val diff1 = CalculusEngine.differentiate("x^3 - 3*x^2 + 4", "x", 1, null)
        assertTrue(diff1.isSuccess)
        assertNotNull(diff1.derivativeLatex)

        // Evaluate derivative at x = 2: 3*(4) - 6*(2) = 12 - 12 = 0
        val diffAtPt = CalculusEngine.differentiate("x^3 - 3*x^2 + 4", "x", 1, 2.0)
        assertTrue(diffAtPt.isSuccess)
        assertEquals(0.0, diffAtPt.pointValueDecimal ?: -1.0, 1e-6)

        // 2nd order derivative: d^2/dx^2 (x^3) = 6x, at x = 3 is 18
        val diff2 = CalculusEngine.differentiate("x^3", "x", 2, 3.0)
        assertTrue(diff2.isSuccess)
        assertEquals(18.0, diff2.pointValueDecimal ?: -1.0, 1e-6)
    }

    @Test
    fun testCalculusTrigDifferentiation() {
        // d/dx sin(x) -> cos(x), at x = 0 is 1.0
        val diffSin = CalculusEngine.differentiate("sin(x)", "x", 1, 0.0)
        assertTrue(diffSin.isSuccess)
        assertEquals(1.0, diffSin.pointValueDecimal ?: 0.0, 1e-6)
    }

    @Test
    fun testCalculusIndefiniteIntegration() {
        // ∫ (x^2 + 2*x) dx -> 1/3 x^3 + x^2 + C
        val intResult = CalculusEngine.integrateIndefinite("x^2 + 2*x", "x")
        assertTrue(intResult.isSuccess)
        assertTrue(intResult.resultDisplay.contains("+ C"))
        assertTrue(intResult.isSymbolic)
    }

    @Test
    fun testCalculusDefiniteIntegration() {
        // ∫[0, 3] x^2 dx = [x^3 / 3]_0^3 = 27 / 3 = 9.0
        val defInt = CalculusEngine.integrateDefinite("x^2", 0.0, 3.0, "x")
        assertTrue(defInt.isSuccess)
        assertEquals(9.0, defInt.value, 1e-4)

        // ∫[0, pi] sin(x) dx = [-cos(x)]_0^pi = -(-1) - (-1) = 2.0
        val defSin = CalculusEngine.integrateDefinite("sin(x)", 0.0, Math.PI, "x")
        assertTrue(defSin.isSuccess)
        assertEquals(2.0, defSin.value, 1e-4)
    }

    @Test
    fun testCanonicalMathResult() {
        val result = CanonicalMathResult(
            latex = "\\frac{3}{2}",
            displayString = "3/2",
            decimalValue = 1.5,
            decimalString = "1.5",
            exactString = "3/2",
            isExact = true
        )
        assertEquals("\\frac{3}{2}", result.latex)
        assertEquals("3/2", result.primaryDisplay)
        assertEquals("≈ 1.5", result.secondaryDisplay)
        assertFalse(result.isError)
    }

    @Test
    fun testGraphFunctionParsing() {
        val fn = GraphFunction.create("f1", "x^2 - 4", 0)
        assertNotNull(fn.parsedExpr)
        assertNull(fn.error)
        val vars = mutableMapOf("x" to 2.0)
        assertEquals(0.0, fn.parsedExpr?.eval(vars) ?: -999.0, 1e-6)
    }

    @Test
    fun testImplicitEquationParsing() {
        // x^2 + y^2 = 5 (Circle from user's screenshot)
        val circleFn = GraphFunction.create("circle", "x^2 + y^2 = 5", 0)
        assertTrue(circleFn.isImplicit)
        assertNotNull(circleFn.parsedExpr)
        assertNull(circleFn.error)

        // At (x=1, y=2): 1^2 + 2^2 - 5 = 1 + 4 - 5 = 0
        val vars = mutableMapOf("x" to 1.0, "y" to 2.0)
        assertEquals(0.0, circleFn.parsedExpr?.eval(vars) ?: -999.0, 1e-6)
    }

    @Test
    fun testDesmosColorRotation() {
        val fn1 = GraphFunction.create("f1", "x", 0)
        val fn2 = GraphFunction.create("f2", "x^2", 1)
        val fn3 = GraphFunction.create("f3", "sin(x)", 2)

        // Each function must have a distinct color
        assertNotEquals(fn1.color, fn2.color)
        assertNotEquals(fn2.color, fn3.color)
        assertNotEquals(fn1.color, fn3.color)
        assertEquals(GraphFunction.PALETTE[0], fn1.color)
        assertEquals(GraphFunction.PALETTE[1], fn2.color)
    }
}
