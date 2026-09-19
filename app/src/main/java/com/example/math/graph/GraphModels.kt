package com.example.math.graph

import androidx.compose.ui.graphics.Color
import com.example.math.calculus.CalculusParser
import com.example.math.calculus.Expr

data class GraphFunction(
    val id: String,
    val expressionText: String,
    val color: Color,
    val isVisible: Boolean = true,
    val parsedExpr: Expr? = null,
    val isImplicit: Boolean = false,
    val implicitExpr: Expr? = null,
    val showDerivative: Boolean = false,
    val showTangent: Boolean = false,
    val tangentX: Double = 1.0,
    val showIntegralArea: Boolean = false,
    val integralA: Double = 0.0,
    val integralB: Double = 2.0,
    val error: String? = null
) {
    companion object {
        // Desmos canonical color palette: Red, Blue, Green, Purple, Orange, Black
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

        fun create(id: String, expr: String, colorIndex: Int = 0): GraphFunction {
            val color = PALETTE[colorIndex % PALETTE.size]
            val trimmed = expr.trim()
            if (trimmed.isEmpty()) {
                return GraphFunction(
                    id = id,
                    expressionText = expr,
                    color = color,
                    parsedExpr = null,
                    error = null
                )
            }

            return try {
                if (trimmed.contains("=")) {
                    val parts = trimmed.split("=")
                    if (parts.size == 2) {
                        val lhs = parts[0].trim()
                        val rhs = parts[1].trim()
                        if (lhs == "y" && !rhs.contains("y")) {
                            // Explicit function y = f(x)
                            val parsed = CalculusParser.parse(rhs)
                            GraphFunction(
                                id = id,
                                expressionText = expr,
                                color = color,
                                parsedExpr = parsed,
                                isImplicit = false,
                                error = null
                            )
                        } else {
                            // Implicit equation F(x, y) = lhs - rhs = 0 (e.g. x^2 + y^2 = 5)
                            val leftExpr = CalculusParser.parse(lhs)
                            val rightExpr = CalculusParser.parse(rhs)
                            val diff = com.example.math.calculus.Sub(leftExpr, rightExpr).simplify()
                            GraphFunction(
                                id = id,
                                expressionText = expr,
                                color = color,
                                parsedExpr = diff,
                                isImplicit = true,
                                implicitExpr = diff,
                                error = null
                            )
                        }
                    } else {
                        val parsed = CalculusParser.parse(trimmed)
                        GraphFunction(id = id, expressionText = expr, color = color, parsedExpr = parsed, error = null)
                    }
                } else {
                    val parsed = CalculusParser.parse(trimmed)
                    GraphFunction(id = id, expressionText = expr, color = color, parsedExpr = parsed, error = null)
                }
            } catch (e: Exception) {
                GraphFunction(
                    id = id,
                    expressionText = expr,
                    color = color,
                    parsedExpr = null,
                    error = e.message ?: "Invalid syntax"
                )
            }
        }
    }
}

data class GraphViewport(
    val minX: Double = -10.0,
    val maxX: Double = 10.0,
    val minY: Double = -10.0,
    val maxY: Double = 10.0
) {
    val rangeX: Double get() = (maxX - minX).coerceAtLeast(MIN_RANGE)
    val rangeY: Double get() = (maxY - minY).coerceAtLeast(MIN_RANGE)
    val centerX: Double get() = (minX + maxX) / 2.0
    val centerY: Double get() = (minY + maxY) / 2.0

    /**
     * Converts mathematical coordinates (worldX, worldY) to screen pixel coordinates.
     */
    fun toScreenX(worldX: Double, screenWidth: Float): Float {
        if (rangeX <= 0.0 || screenWidth <= 0f) return 0f
        return (((worldX - minX) / rangeX) * screenWidth).toFloat()
    }

    fun toScreenY(worldY: Double, screenHeight: Float): Float {
        if (rangeY <= 0.0 || screenHeight <= 0f) return 0f
        // World Y increases upwards, Screen Y increases downwards!
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
        // Screen top (0) is maxY, screen bottom (height) is minY
        return maxY - (screenY.toDouble() / screenHeight.toDouble()) * rangeY
    }

    /**
     * Pan the viewport by screen pixel deltas (panPixelsX, panPixelsY).
     * Moving finger RIGHT (panPixelsX > 0) should reveal content on the left, so minX/maxX decrease.
     * Moving finger DOWN (panPixelsY > 0) should reveal content above, so minY/maxY increase.
     */
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
     * Zoom around a screen focal point (focalScreenX, focalScreenY) by [zoomFactor].
     * If zoomFactor > 1.0, we zoom IN (range shrinks).
     * If zoomFactor < 1.0, we zoom OUT (range expands).
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

    fun pan(dxMath: Double, dyMath: Double): GraphViewport {
        return copy(
            minX = (minX - dxMath).coerceIn(-MAX_COORD, MAX_COORD),
            maxX = (maxX - dxMath).coerceIn(-MAX_COORD, MAX_COORD),
            minY = (minY - dyMath).coerceIn(-MAX_COORD, MAX_COORD),
            maxY = (maxY - dyMath).coerceIn(-MAX_COORD, MAX_COORD)
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
        const val MIN_RANGE = 1e-7
        const val MAX_RANGE = 1e8
        const val MAX_COORD = 1e9
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
    val values: Map<String, Double?> // funcId -> evaluated y
)
