package com.example.math.graph

import androidx.compose.ui.graphics.Color
import com.example.math.calculus.CalculusParser
import com.example.math.calculus.Expr
import kotlin.math.abs

enum class InequalityType {
    NONE,
    LESS_THAN,       // < (strict, dashed boundary, shaded below / left)
    LESS_EQUAL,      // <= (inclusive, solid boundary, shaded below / left)
    GREATER_THAN,    // > (strict, dashed boundary, shaded above / right)
    GREATER_EQUAL    // >= (inclusive, solid boundary, shaded above / right)
}

data class GraphFunction(
    val id: String,
    val expressionText: String,
    val color: Color,
    val displayLatex: String = expressionText,
    val isVisible: Boolean = true,
    val parsedExpr: Expr? = null,
    val isImplicit: Boolean = false,
    val implicitExpr: Expr? = null,
    val isXFunctionOfY: Boolean = false,
    val isVerticalLine: Boolean = false,
    val verticalX: Double = 0.0,
    val isParametric: Boolean = false,
    val parametricX: Expr? = null,
    val parametricY: Expr? = null,
    val isPolar: Boolean = false,
    val tMin: Double = 0.0,
    val tMax: Double = 2 * Math.PI,
    val inequalityType: InequalityType = InequalityType.NONE,
    val showDerivative: Boolean = false,
    val showTangent: Boolean = false,
    val tangentX: Double = 1.0,
    val showIntegralArea: Boolean = false,
    val integralA: Double = 0.0,
    val integralB: Double = 2.0,
    val error: String? = null
) {
    companion object {
        // Desmos canonical color palette: Red, Blue, Green, Purple, Orange, Black, Teal, Pink
        val PALETTE = listOf(
            Color(0xFFC74440), // Desmos Red
            Color(0xFF2D70B3), // Desmos Blue
            Color(0xFF388C46), // Desmos Green
            Color(0xFF6042A6), // Desmos Purple
            Color(0xFFFA7E19), // Desmos Orange
            Color(0xFF000000), // Desmos Black
            Color(0xFF00A5B5), // Teal
            Color(0xFFD63384)  // Pink
        )

        fun createFromLatex(id: String, latex: String, colorIndex: Int = 0): GraphFunction {
            val color = PALETTE[colorIndex % PALETTE.size]
            val trimmedLatex = latex.trim()

            if (trimmedLatex.isBlank()) {
                return GraphFunction(
                    id = id,
                    expressionText = "",
                    color = color,
                    displayLatex = "",
                    parsedExpr = null,
                    error = null
                )
            }

            val evaluatable = com.example.math.calculus.LatexToExprConverter.convertToEvaluatableString(trimmedLatex)

            return try {
                // 0. Parametric point curve: (f(t), g(t))
                if (evaluatable.startsWith("(") && evaluatable.endsWith(")") && evaluatable.contains(",")) {
                    val inner = evaluatable.substring(1, evaluatable.length - 1)
                    val commaIdx = findTopLevelComma(inner)
                    if (commaIdx != -1) {
                        val partX = inner.substring(0, commaIdx).trim()
                        val partY = inner.substring(commaIdx + 1).trim()
                        val exprX = CalculusParser.parse(partX)
                        val exprY = CalculusParser.parse(partY)
                        return GraphFunction(
                            id = id,
                            expressionText = trimmedLatex,
                            color = color,
                            displayLatex = trimmedLatex,
                            isParametric = true,
                            parametricX = exprX,
                            parametricY = exprY,
                            tMin = 0.0,
                            tMax = 2 * Math.PI,
                            error = null
                        )
                    }
                }

                // Determine operator
                val op: String?
                val ineqType: InequalityType
                when {
                    evaluatable.contains("<=") -> { op = "<="; ineqType = InequalityType.LESS_EQUAL }
                    evaluatable.contains(">=") -> { op = ">="; ineqType = InequalityType.GREATER_EQUAL }
                    evaluatable.contains("<") -> { op = "<"; ineqType = InequalityType.LESS_THAN }
                    evaluatable.contains(">") -> { op = ">"; ineqType = InequalityType.GREATER_THAN }
                    evaluatable.contains("=") -> { op = "="; ineqType = InequalityType.NONE }
                    else -> { op = null; ineqType = InequalityType.NONE }
                }

                if (op != null) {
                    val parts = evaluatable.split(op)
                    if (parts.size == 2) {
                        var lhs = parts[0].trim()
                        while (lhs.startsWith("(") && lhs.endsWith(")")) {
                            lhs = lhs.substring(1, lhs.length - 1).trim()
                        }
                        var rhs = parts[1].trim()
                        while (rhs.startsWith("(") && rhs.endsWith(")")) {
                            rhs = rhs.substring(1, rhs.length - 1).trim()
                        }

                        val leftExpr = CalculusParser.parse(lhs)
                        val rightExpr = CalculusParser.parse(rhs)
                        val lhsVars = leftExpr.extractVariables()
                        val rhsVars = rightExpr.extractVariables()

                        val lhsIsY = (lhs == "y" || (lhsVars == setOf("y") && leftExpr is com.example.math.calculus.Variable))
                        val rhsIsY = (rhs == "y" || (rhsVars == setOf("y") && rightExpr is com.example.math.calculus.Variable))
                        val lhsIsX = (lhs == "x" || (lhsVars == setOf("x") && leftExpr is com.example.math.calculus.Variable))
                        val rhsIsX = (rhs == "x" || (rhsVars == setOf("x") && rightExpr is com.example.math.calculus.Variable))
                        val lhsIsR = (lhs == "r" || (lhsVars == setOf("r") && leftExpr is com.example.math.calculus.Variable))
                        val rhsIsR = (rhs == "r" || (rhsVars == setOf("r") && rightExpr is com.example.math.calculus.Variable))

                        val reversedIneq = when (ineqType) {
                            InequalityType.LESS_THAN -> InequalityType.GREATER_THAN
                            InequalityType.LESS_EQUAL -> InequalityType.GREATER_EQUAL
                            InequalityType.GREATER_THAN -> InequalityType.LESS_THAN
                            InequalityType.GREATER_EQUAL -> InequalityType.LESS_EQUAL
                            InequalityType.NONE -> InequalityType.NONE
                        }

                        // 1. Polar equations: r = f(theta), r = f(t), r <= f(theta), etc.
                        if (lhsIsR && "r" !in rhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = rightExpr,
                                isPolar = true,
                                inequalityType = ineqType,
                                error = null
                            )
                        } else if (rhsIsR && "r" !in lhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = leftExpr,
                                isPolar = true,
                                inequalityType = reversedIneq,
                                error = null
                            )
                        }
                        // 2. Explicit in y: y <= f(x), y = f(x)
                        else if (lhsIsY && "y" !in rhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = rightExpr,
                                isImplicit = false,
                                inequalityType = ineqType,
                                error = null
                            )
                        }
                        // 3. Reverse explicit in y: f(x) >= y -> y <= f(x)
                        else if (rhsIsY && "y" !in lhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = leftExpr,
                                isImplicit = false,
                                inequalityType = reversedIneq,
                                error = null
                            )
                        }
                        // 4. Vertical line: x = c, x <= c (RHS is constant expression)
                        else if (lhsIsX && rhsVars.isEmpty()) {
                            val constVal = try { rightExpr.eval(emptyMap()) } catch (_: Exception) { 0.0 }
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = rightExpr,
                                isVerticalLine = true,
                                verticalX = constVal,
                                inequalityType = ineqType,
                                error = null
                            )
                        } else if (rhsIsX && lhsVars.isEmpty()) {
                            val constVal = try { leftExpr.eval(emptyMap()) } catch (_: Exception) { 0.0 }
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = leftExpr,
                                isVerticalLine = true,
                                verticalX = constVal,
                                inequalityType = reversedIneq,
                                error = null
                            )
                        }
                        // 5. Horizontal curves / waves: x = f(y) or x = f(t) (where RHS has y or t or theta, but NOT x)
                        else if (lhsIsX && ("y" in rhsVars || "t" in rhsVars || "θ" in rhsVars || "theta" in rhsVars) && "x" !in rhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = rightExpr,
                                isXFunctionOfY = true,
                                inequalityType = ineqType,
                                error = null
                            )
                        } else if (rhsIsX && ("y" in lhsVars || "t" in lhsVars || "θ" in lhsVars || "theta" in lhsVars) && "x" !in lhsVars) {
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = leftExpr,
                                isXFunctionOfY = true,
                                inequalityType = reversedIneq,
                                error = null
                            )
                        }
                        // 6. Isolated sqrt(y): sqrt(y) = f(x)
                        else if ((lhs == "sqrt(y)" || lhs == "(sqrt(y))" || lhs == "y^(1/2)" || lhs == "y^(0.5)") && "y" !in rhsVars) {
                            val squared = com.example.math.calculus.Pow(rightExpr, com.example.math.calculus.Constant(2.0))
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = squared,
                                isImplicit = false,
                                inequalityType = ineqType,
                                error = null
                            )
                        } else if ((rhs == "sqrt(y)" || rhs == "(sqrt(y))" || rhs == "y^(1/2)" || rhs == "y^(0.5)") && "y" !in lhsVars) {
                            val squared = com.example.math.calculus.Pow(leftExpr, com.example.math.calculus.Constant(2.0))
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = squared,
                                isImplicit = false,
                                inequalityType = reversedIneq,
                                error = null
                            )
                        }
                        // 7. General Implicit 2D Curves / Relations (circles, hyperbolas, ellipses, conics, inequalities)
                        else {
                            val diff = com.example.math.calculus.Sub(leftExpr, rightExpr).simplify()
                            GraphFunction(
                                id = id,
                                expressionText = trimmedLatex,
                                color = color,
                                displayLatex = trimmedLatex,
                                parsedExpr = diff,
                                isImplicit = true,
                                implicitExpr = diff,
                                inequalityType = ineqType,
                                error = null
                            )
                        }
                    } else {
                        val parsed = CalculusParser.parse(evaluatable)
                        val varsInExpr = parsed.extractVariables()
                        val isPolar = ("theta" in varsInExpr || "θ" in varsInExpr)
                        GraphFunction(
                            id = id,
                            expressionText = trimmedLatex,
                            color = color,
                            displayLatex = trimmedLatex,
                            parsedExpr = parsed,
                            isPolar = isPolar,
                            error = null
                        )
                    }
                } else {
                    val parsed = CalculusParser.parse(evaluatable)
                    val varsInExpr = parsed.extractVariables()
                    val isPolar = ("theta" in varsInExpr || "θ" in varsInExpr)
                    GraphFunction(
                        id = id,
                        expressionText = trimmedLatex,
                        color = color,
                        displayLatex = trimmedLatex,
                        parsedExpr = parsed,
                        isPolar = isPolar,
                        error = null
                    )
                }
            } catch (e: Exception) {
                GraphFunction(
                    id = id,
                    expressionText = trimmedLatex,
                    color = color,
                    displayLatex = trimmedLatex,
                    parsedExpr = null,
                    error = e.message ?: "Invalid syntax"
                )
            }
        }

        private fun findTopLevelComma(str: String): Int {
            var depth = 0
            for (i in str.indices) {
                when (str[i]) {
                    '(', '{', '[' -> depth++
                    ')', '}', ']' -> depth--
                    ',' -> if (depth == 0) return i
                }
            }
            return -1
        }

        fun create(id: String, expr: String, colorIndex: Int = 0): GraphFunction {
            return createFromLatex(id, expr, colorIndex)
        }
    }
}

/**
 * Mathematical 2D Viewport with 1:1 Aspect Ratio guarantee and precise focal zoom.
 */
data class GraphViewport(
    val minX: Double = -10.0,
    val maxX: Double = 10.0,
    val minY: Double = -10.0,
    val maxY: Double = 10.0
) {
    val rangeX: Double get() = (maxX - minX).coerceIn(MIN_RANGE, MAX_RANGE)
    val rangeY: Double get() = (maxY - minY).coerceIn(MIN_RANGE, MAX_RANGE)
    val centerX: Double get() = (minX + maxX) / 2.0
    val centerY: Double get() = (minY + maxY) / 2.0

    /**
     * Enforces equal 1:1 aspect ratio based on canvas dimensions so 1 math unit on X
     * corresponds to the exact same screen pixel length as 1 unit on Y.
     */
    fun withAspectRatio(screenWidth: Float, screenHeight: Float): GraphViewport {
        if (screenWidth <= 0f || screenHeight <= 0f) return this
        val targetRangeY = (rangeX / screenWidth.toDouble()) * screenHeight.toDouble()
        val cy = centerY
        val halfH = (targetRangeY / 2.0).coerceIn(MIN_RANGE / 2.0, MAX_RANGE / 2.0)
        return copy(
            minY = (cy - halfH).coerceIn(-MAX_COORD, MAX_COORD),
            maxY = (cy + halfH).coerceIn(-MAX_COORD, MAX_COORD)
        )
    }

    /**
     * Converts mathematical coordinates (worldX, worldY) to screen pixel coordinates.
     */
    fun toScreenX(worldX: Double, screenWidth: Float): Float {
        if (rangeX <= 0.0 || screenWidth <= 0f) return 0f
        return (((worldX - minX) / rangeX) * screenWidth).toFloat()
    }

    fun toScreenY(worldY: Double, screenHeight: Float): Float {
        if (rangeY <= 0.0 || screenHeight <= 0f) return 0f
        // World Y increases upwards, Screen Y increases downwards
        return (((maxY - worldY) / rangeY) * screenHeight).toFloat()
    }

    /**
     * Converts screen pixel coordinates to mathematical world coordinates.
     */
    fun toWorldX(screenX: Float, screenWidth: Float): Double {
        if (screenWidth <= 0f) return centerX
        return minX + (screenX.toDouble() / screenWidth.toDouble()) * rangeX
    }

    fun toWorldY(screenY: Float, screenHeight: Float): Double {
        if (screenHeight <= 0f) return centerY
        return maxY - (screenY.toDouble() / screenHeight.toDouble()) * rangeY
    }

    /**
     * Delta-based panning from initial gesture anchor (prevents accumulated drift).
     */
    fun panFromStart(
        startViewport: GraphViewport,
        totalPanPixelsX: Float,
        totalPanPixelsY: Float,
        screenWidth: Float,
        screenHeight: Float
    ): GraphViewport {
        if (screenWidth <= 0f || screenHeight <= 0f) return this
        val worldDeltaX = (totalPanPixelsX.toDouble() / screenWidth.toDouble()) * startViewport.rangeX
        val worldDeltaY = -(totalPanPixelsY.toDouble() / screenHeight.toDouble()) * startViewport.rangeY

        val newMinX = (startViewport.minX - worldDeltaX).coerceIn(-MAX_COORD, MAX_COORD)
        val newMaxX = (startViewport.maxX - worldDeltaX).coerceIn(-MAX_COORD, MAX_COORD)
        val newMinY = (startViewport.minY - worldDeltaY).coerceIn(-MAX_COORD, MAX_COORD)
        val newMaxY = (startViewport.maxY - worldDeltaY).coerceIn(-MAX_COORD, MAX_COORD)

        return copy(minX = newMinX, maxX = newMaxX, minY = newMinY, maxY = newMaxY)
    }

    fun panByPixels(panPixelsX: Float, panPixelsY: Float, screenWidth: Float, screenHeight: Float): GraphViewport {
        if (screenWidth <= 0f || screenHeight <= 0f) return this
        val worldDeltaX = (panPixelsX.toDouble() / screenWidth.toDouble()) * rangeX
        val worldDeltaY = -(panPixelsY.toDouble() / screenHeight.toDouble()) * rangeY

        val newMinX = (minX - worldDeltaX).coerceIn(-MAX_COORD, MAX_COORD)
        val newMaxX = (maxX - worldDeltaX).coerceIn(-MAX_COORD, MAX_COORD)
        val newMinY = (minY - worldDeltaY).coerceIn(-MAX_COORD, MAX_COORD)
        val newMaxY = (maxY - worldDeltaY).coerceIn(-MAX_COORD, MAX_COORD)

        return copy(minX = newMinX, maxX = newMaxX, minY = newMinY, maxY = newMaxY)
    }

    /**
     * Focal-point zoom (keeps the point under the gesture spatially fixed on screen).
     */
    fun zoomAroundScreenPoint(
        focalScreenX: Float,
        focalScreenY: Float,
        zoomFactor: Double,
        screenWidth: Float,
        screenHeight: Float
    ): GraphViewport {
        if (screenWidth <= 0f || screenHeight <= 0f || zoomFactor <= 0.0) return this

        val worldFocalX = toWorldX(focalScreenX, screenWidth)
        val worldFocalY = toWorldY(focalScreenY, screenHeight)

        val rawNewRangeX = rangeX / zoomFactor
        val rawNewRangeY = rangeY / zoomFactor

        val newRangeX = rawNewRangeX.coerceIn(MIN_RANGE, MAX_RANGE)
        val newRangeY = rawNewRangeY.coerceIn(MIN_RANGE, MAX_RANGE)

        val fx = (focalScreenX.toDouble() / screenWidth.toDouble()).coerceIn(0.0, 1.0)
        val fy = (focalScreenY.toDouble() / screenHeight.toDouble()).coerceIn(0.0, 1.0)

        val newMinX = worldFocalX - fx * newRangeX
        val newMaxX = newMinX + newRangeX

        val newMaxY = worldFocalY + fy * newRangeY
        val newMinY = newMaxY - newRangeY

        return copy(
            minX = newMinX.coerceIn(-MAX_COORD, MAX_COORD),
            maxX = newMaxX.coerceIn(-MAX_COORD, MAX_COORD),
            minY = newMinY.coerceIn(-MAX_COORD, MAX_COORD),
            maxY = newMaxY.coerceIn(-MAX_COORD, MAX_COORD)
        )
    }

    fun zoom(factor: Double): GraphViewport {
        val newRangeX = (rangeX * factor).coerceIn(MIN_RANGE, MAX_RANGE)
        val newRangeY = (rangeY * factor).coerceIn(MIN_RANGE, MAX_RANGE)
        val cx = centerX
        val cy = centerY
        val halfW = newRangeX / 2.0
        val halfH = newRangeY / 2.0
        return copy(
            minX = (cx - halfW).coerceIn(-MAX_COORD, MAX_COORD),
            maxX = (cx + halfW).coerceIn(-MAX_COORD, MAX_COORD),
            minY = (cy - halfH).coerceIn(-MAX_COORD, MAX_COORD),
            maxY = (cy + halfH).coerceIn(-MAX_COORD, MAX_COORD)
        )
    }

    fun reset(): GraphViewport = GraphViewport(-10.0, 10.0, -10.0, 10.0)

    companion object {
        const val MIN_RANGE = 1e-6
        const val MAX_RANGE = 1e7
        const val MAX_COORD = 1e8
    }
}

data class GraphSlider(
    val name: String,
    val value: Float,
    val min: Float = -10f,
    val max: Float = 10f,
    val step: Float = 0.1f
)

data class TableRow(
    val x: Double,
    val values: Map<String, Double?>
)
