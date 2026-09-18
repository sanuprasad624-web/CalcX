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
    val rangeX: Double get() = maxX - minX
    val rangeY: Double get() = maxY - minY

    fun pan(dxMath: Double, dyMath: Double): GraphViewport {
        return copy(
            minX = minX - dxMath,
            maxX = maxX - dxMath,
            minY = minY - dyMath,
            maxY = maxY - dyMath
        )
    }

    fun zoom(factor: Double): GraphViewport {
        val centerX = (minX + maxX) / 2.0
        val centerY = (minY + maxY) / 2.0
        val halfW = (rangeX / 2.0) * factor
        val halfH = (rangeY / 2.0) * factor
        return copy(
            minX = centerX - halfW,
            maxX = centerX + halfW,
            minY = centerY - halfH,
            maxY = centerY + halfH
        )
    }

    fun reset(): GraphViewport = GraphViewport(-10.0, 10.0, -10.0, 10.0)
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
