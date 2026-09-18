package com.example.ui.components.graph

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.graph.GraphFunction
import com.example.math.graph.GraphViewport
import com.example.math.graph.ImplicitPlotter
import com.example.math.graph.PointOfInterest
import java.util.Locale
import kotlin.math.*

@Composable
fun GraphCanvas(
    viewport: GraphViewport,
    functions: List<GraphFunction>,
    parameters: Map<String, Double>,
    showGrid: Boolean = true,
    showAxes: Boolean = true,
    onViewportChange: (GraphViewport) -> Unit,
    modifier: Modifier = Modifier
) {
    // Desmos styling colors
    val canvasBg = Color(0xFFFFFFFF)
    val majorGridColor = Color(0xFFD0D7DE)
    val minorGridColor = Color(0xFFEDF0F3)
    val axisColor = Color(0xFF1E293B)
    val primaryColor = Color(0xFF2563EB)

    // Trace / Inspector state
    var activeTooltip by remember { mutableStateOf<Pair<PointOfInterest, Offset>?>(null) }

    Box(modifier = modifier.fillMaxSize().background(canvasBg).testTag("graph_canvas_container")) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("graph_canvas")
                .pointerInput(viewport) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val rangeX = viewport.rangeX
                        val rangeY = viewport.rangeY
                        val dxMath = (pan.x / size.width) * rangeX
                        val dyMath = -(pan.y / size.height) * rangeY

                        var newMinX = viewport.minX - dxMath
                        var newMaxX = viewport.maxX - dxMath
                        var newMinY = viewport.minY - dyMath
                        var newMaxY = viewport.maxY - dyMath

                        if (zoom != 1f && zoom > 0.05f && zoom < 20f) {
                            val factor = 1.0 / zoom.toDouble()
                            val cx = (newMinX + newMaxX) / 2.0
                            val cy = (newMinY + newMaxY) / 2.0
                            val halfW = (newMaxX - newMinX) * factor / 2.0
                            val halfH = halfW * (size.height / size.width)
                            newMinX = cx - halfW
                            newMaxX = cx + halfW
                            newMinY = cy - halfH
                            newMaxY = cy + halfH
                        }
                        onViewportChange(GraphViewport(newMinX, newMaxX, newMinY, newMaxY))
                    }
                }
                .pointerInput(functions, viewport, parameters) {
                    detectTapGestures(
                        onTap = { offset ->
                            // Check if tapped near any Point of Interest or curve
                            val mathX = viewport.minX + (offset.x / size.width) * viewport.rangeX
                            val mathY = viewport.maxY - (offset.y / size.height) * viewport.rangeY

                            var found: PointOfInterest? = null
                            for (fn in functions) {
                                if (!fn.isVisible || fn.parsedExpr == null) continue
                                val pois = ImplicitPlotter.findPointsOfInterest(fn.parsedExpr, parameters, viewport)
                                for (poi in pois) {
                                    val distSq = (poi.x - mathX).pow(2) + (poi.y - mathY).pow(2)
                                    val threshold = (viewport.rangeX * 0.05).pow(2)
                                    if (distSq < threshold) {
                                        found = poi
                                        break
                                    }
                                }
                                if (found != null) break
                            }

                            if (found != null) {
                                val sx = ((found.x - viewport.minX) / viewport.rangeX * size.width).toFloat()
                                val sy = ((viewport.maxY - found.y) / viewport.rangeY * size.height).toFloat()
                                activeTooltip = Pair(found, Offset(sx, sy))
                            } else {
                                activeTooltip = null
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height

            fun toScreenX(x: Double): Float = ((x - viewport.minX) / viewport.rangeX * width).toFloat()
            fun toScreenY(y: Double): Float = ((viewport.maxY - y) / viewport.rangeY * height).toFloat()

            // 1. Draw Desmos Grid & Numbers on Axes (pinch-to-zoom dynamic tick labels)
            if (showGrid) {
                drawDesmosGridAndLabels(viewport, width, height, majorGridColor, minorGridColor, axisColor)
            }

            // 2. Draw Axes
            if (showAxes) {
                val originX = toScreenX(0.0)
                val originY = toScreenY(0.0)

                // Y-axis (x = 0)
                if (originX in -10f..width + 10f) {
                    drawLine(
                        color = axisColor,
                        start = Offset(originX, 0f),
                        end = Offset(originX, height),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }

                // X-axis (y = 0)
                if (originY in -10f..height + 10f) {
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, originY),
                        end = Offset(width, originY),
                        strokeWidth = 1.8.dp.toPx()
                    )
                }
            }

            // 3. Draw Functions & Curves
            val vars = parameters.toMutableMap()

            functions.forEach { fn ->
                if (!fn.isVisible || fn.parsedExpr == null) return@forEach
                val expr = fn.parsedExpr

                if (fn.isImplicit) {
                    // IMPLICIT CURVE (e.g. x² + y² = 5 circle / conics)
                    val segments = ImplicitPlotter.plotImplicit(expr, parameters, viewport, width, height)
                    for (seg in segments) {
                        drawLine(
                            color = fn.color,
                            start = seg.start,
                            end = seg.end,
                            strokeWidth = 2.8.dp.toPx()
                        )
                    }

                    // Draw Points of Interest (gray circle markers with white centers, as in Screenshot 3!)
                    val pois = ImplicitPlotter.findPointsOfInterest(expr, parameters, viewport)
                    for (poi in pois) {
                        val sx = toScreenX(poi.x)
                        val sy = toScreenY(poi.y)
                        if (sx in 0f..width && sy in 0f..height) {
                            drawCircle(color = Color(0xFF64748B), radius = 5.dp.toPx(), center = Offset(sx, sy))
                            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(sx, sy))
                        }
                    }
                } else {
                    // EXPLICIT FUNCTION y = f(x)
                    // A. Integral Area Shading
                    if (fn.showIntegralArea) {
                        drawIntegralShading(fn, vars, viewport, width, height)
                    }

                    // B. Main Function Curve
                    drawFunctionCurve(expr, vars, viewport, width, height, fn.color)

                    // C. Derivative Overlay f'(x)
                    if (fn.showDerivative) {
                        try {
                            val derivExpr = expr.differentiate("x").simplify()
                            drawFunctionCurve(
                                derivExpr, vars, viewport, width, height,
                                fn.color.copy(alpha = 0.65f),
                                strokeWidth = 2.dp.toPx(),
                                isDashed = true
                            )
                        } catch (_: Exception) {}
                    }

                    // D. Tangent Line Overlay
                    if (fn.showTangent) {
                        drawTangentLine(expr, vars, fn.tangentX, viewport, width, height, fn.color)
                    }

                    // Points of Interest for explicit curve (roots & y-intercept)
                    val pois = ImplicitPlotter.findPointsOfInterest(expr, parameters, viewport)
                    for (poi in pois) {
                        val sx = toScreenX(poi.x)
                        val sy = toScreenY(poi.y)
                        if (sx in 0f..width && sy in 0f..height) {
                            drawCircle(color = Color(0xFF64748B), radius = 5.dp.toPx(), center = Offset(sx, sy))
                            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(sx, sy))
                        }
                    }
                }
            }
        }

        // Active point tooltip (e.g. (2.236, 0))
        activeTooltip?.let { (poi, screenOffset) ->
            Surface(
                modifier = Modifier
                    .padding(start = (screenOffset.x - 40f).coerceAtLeast(10f).dp / 2.75f, top = (screenOffset.y - 45f).coerceAtLeast(10f).dp / 2.75f),
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF1E293B),
                shadowElevation = 4.dp
            ) {
                Text(
                    text = poi.label,
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

/**
 * Draws the Desmos-style square grid lines and labels numbers on the axes.
 * When user pinches to zoom, step dynamically adjusts so smaller numbers appear.
 */
private fun DrawScope.drawDesmosGridAndLabels(
    viewport: GraphViewport,
    width: Float,
    height: Float,
    majorColor: Color,
    minorColor: Color,
    axisTextColor: Color
) {
    val majorStep = calculateNiceStep(viewport.rangeX)
    val minorStep = majorStep / 5.0 // 5 square boxes per major step, matching Desmos

    fun toScreenX(x: Double) = ((x - viewport.minX) / viewport.rangeX * width).toFloat()
    fun toScreenY(y: Double) = ((viewport.maxY - y) / viewport.rangeY * height).toFloat()

    val originX = toScreenX(0.0)
    val originY = toScreenY(0.0)

    // 1. Draw Minor Vertical Grid Lines (X)
    val startMinorX = floor(viewport.minX / minorStep) * minorStep
    var curMinorX = startMinorX
    while (curMinorX <= viewport.maxX + minorStep) {
        val sx = toScreenX(curMinorX)
        drawLine(
            color = minorColor,
            start = Offset(sx, 0f),
            end = Offset(sx, height),
            strokeWidth = 0.8.dp.toPx()
        )
        curMinorX += minorStep
    }

    // 2. Draw Minor Horizontal Grid Lines (Y)
    val startMinorY = floor(viewport.minY / minorStep) * minorStep
    var curMinorY = startMinorY
    while (curMinorY <= viewport.maxY + minorStep) {
        val sy = toScreenY(curMinorY)
        drawLine(
            color = minorColor,
            start = Offset(0f, sy),
            end = Offset(width, sy),
            strokeWidth = 0.8.dp.toPx()
        )
        curMinorY += minorStep
    }

    // 3. Draw Major Grid Lines
    val startMajorX = floor(viewport.minX / majorStep) * majorStep
    var curMajorX = startMajorX
    while (curMajorX <= viewport.maxX + majorStep) {
        val sx = toScreenX(curMajorX)
        drawLine(
            color = majorColor,
            start = Offset(sx, 0f),
            end = Offset(sx, height),
            strokeWidth = 1.3.dp.toPx()
        )
        curMajorX += majorStep
    }

    val startMajorY = floor(viewport.minY / majorStep) * majorStep
    var curMajorY = startMajorY
    while (curMajorY <= viewport.maxY + majorStep) {
        val sy = toScreenY(curMajorY)
        drawLine(
            color = majorColor,
            start = Offset(0f, sy),
            end = Offset(width, sy),
            strokeWidth = 1.3.dp.toPx()
        )
        curMajorY += majorStep
    }

    // 4. DRAW NUMBERS ON AXES (Dynamic with zoom)
    val textPaint = Paint().apply {
        color = android.graphics.Color.rgb(0x33, 0x41, 0x55)
        textSize = 11.sp.toPx()
        isAntiAlias = true
        typeface = Typeface.DEFAULT
    }

    // Determine Y position for X-axis labels
    val labelYForXAxis = when {
        originY in 24f..(height - 30f) -> originY + 16.dp.toPx()
        originY < 24f -> 20.dp.toPx()
        else -> height - 8.dp.toPx()
    }

    // Determine X position for Y-axis labels
    val labelXForYAxis = when {
        originX in 38f..(width - 15f) -> originX - 8.dp.toPx()
        originX < 38f -> 32.dp.toPx()
        else -> width - 8.dp.toPx()
    }

    // Draw X-axis numbers
    textPaint.textAlign = Paint.Align.CENTER
    curMajorX = startMajorX
    while (curMajorX <= viewport.maxX + majorStep * 0.5) {
        if (abs(curMajorX) > 1e-9) { // Skip origin 0, drawn separately
            val sx = toScreenX(curMajorX)
            if (sx in 10f..(width - 10f)) {
                val label = formatAxisNumber(curMajorX, majorStep)
                drawContext.canvas.nativeCanvas.drawText(label, sx, labelYForXAxis, textPaint)
            }
        }
        curMajorX += majorStep
    }

    // Draw Y-axis numbers
    textPaint.textAlign = Paint.Align.RIGHT
    curMajorY = startMajorY
    while (curMajorY <= viewport.maxY + majorStep * 0.5) {
        if (abs(curMajorY) > 1e-9) { // Skip origin 0
            val sy = toScreenY(curMajorY)
            if (sy in 15f..(height - 15f)) {
                val label = formatAxisNumber(curMajorY, majorStep)
                drawContext.canvas.nativeCanvas.drawText(label, labelXForYAxis, sy + 4.dp.toPx(), textPaint)
            }
        }
        curMajorY += majorStep
    }

    // Draw origin "0"
    if (originX in 20f..(width - 20f) && originY in 20f..(height - 20f)) {
        textPaint.textAlign = Paint.Align.RIGHT
        drawContext.canvas.nativeCanvas.drawText("0", originX - 6.dp.toPx(), originY + 15.dp.toPx(), textPaint)
    }
}

/**
 * Calculates optimal major step size for grid numbers based on visible range.
 * Dynamically scales as user pinches in or out.
 */
private fun calculateNiceStep(range: Double): Double {
    val roughStep = range / 5.5
    val exponent = floor(log10(roughStep))
    val fraction = roughStep / 10.0.pow(exponent)
    val niceFraction = when {
        fraction < 1.5 -> 1.0
        fraction < 3.5 -> 2.0
        fraction < 7.5 -> 5.0
        else -> 10.0
    }
    return niceFraction * 10.0.pow(exponent)
}

/**
 * Formats axis numbers cleanly, removing unnecessary trailing zeros.
 */
private fun formatAxisNumber(value: Double, step: Double): String {
    if (abs(value) < 1e-10) return "0"
    return if (step >= 1.0 && abs(value - round(value)) < 1e-5) {
        round(value).toLong().toString()
    } else {
        val decimals = when {
            step >= 0.5 -> 1
            step >= 0.05 -> 2
            step >= 0.005 -> 3
            else -> 4
        }
        String.format(Locale.US, "%.${decimals}f", value)
            .trimEnd('0').trimEnd('.')
    }
}

private fun DrawScope.drawFunctionCurve(
    expr: com.example.math.calculus.Expr,
    vars: MutableMap<String, Double>,
    viewport: GraphViewport,
    width: Float,
    height: Float,
    color: Color,
    strokeWidth: Float = 2.8.dp.toPx(),
    isDashed: Boolean = false
) {
    val samples = (width.toInt().coerceIn(350, 1200))
    val stepX = viewport.rangeX / samples
    val path = Path()
    var pathStarted = false
    var prevY = 0.0

    for (i in 0..samples) {
        val mathX = viewport.minX + i * stepX
        vars["x"] = mathX
        val mathY = try {
            expr.eval(vars)
        } catch (_: Exception) {
            Double.NaN
        }

        if (mathY.isNaN() || mathY.isInfinite()) {
            pathStarted = false
            continue
        }

        // Asymptote / Discontinuity jump breaker (e.g. tan(x), 1/x)
        if (pathStarted && abs(mathY - prevY) > viewport.rangeY * 0.8 && (mathY * prevY < 0)) {
            pathStarted = false
        }

        val screenX = ((mathX - viewport.minX) / viewport.rangeX * width).toFloat()
        val screenY = ((viewport.maxY - mathY) / viewport.rangeY * height).toFloat()

        if (!pathStarted) {
            path.moveTo(screenX, screenY.coerceIn(-500f, height + 500f))
            pathStarted = true
        } else {
            path.lineTo(screenX, screenY.coerceIn(-500f, height + 500f))
        }
        prevY = mathY
    }

    val style = if (isDashed) {
        Stroke(width = strokeWidth, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
    } else {
        Stroke(width = strokeWidth)
    }

    drawPath(path = path, color = color, style = style)
}

private fun DrawScope.drawIntegralShading(
    fn: GraphFunction,
    vars: MutableMap<String, Double>,
    viewport: GraphViewport,
    width: Float,
    height: Float
) {
    val expr = fn.parsedExpr ?: return
    val a = fn.integralA.coerceIn(viewport.minX, viewport.maxX)
    val b = fn.integralB.coerceIn(viewport.minX, viewport.maxX)
    if (b <= a) return

    val samples = 160
    val stepX = (b - a) / samples
    val path = Path()

    fun toScreenX(x: Double) = ((x - viewport.minX) / viewport.rangeX * width).toFloat()
    fun toScreenY(y: Double) = ((viewport.maxY - y) / viewport.rangeY * height).toFloat()

    val screenOriginY = toScreenY(0.0).coerceIn(0f, height)
    path.moveTo(toScreenX(a), screenOriginY)

    for (i in 0..samples) {
        val x = a + i * stepX
        vars["x"] = x
        val y = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
        path.lineTo(toScreenX(x), toScreenY(y))
    }

    path.lineTo(toScreenX(b), screenOriginY)
    path.close()

    drawPath(path = path, color = fn.color.copy(alpha = 0.25f))
}

private fun DrawScope.drawTangentLine(
    expr: com.example.math.calculus.Expr,
    vars: MutableMap<String, Double>,
    tangentX: Double,
    viewport: GraphViewport,
    width: Float,
    height: Float,
    color: Color
) {
    try {
        vars["x"] = tangentX
        val y0 = expr.eval(vars)
        val derivExpr = expr.differentiate("x").simplify()
        val slope = derivExpr.eval(vars)

        if (y0.isNaN() || slope.isNaN() || y0.isInfinite() || slope.isInfinite()) return

        fun toScreenX(x: Double) = ((x - viewport.minX) / viewport.rangeX * width).toFloat()
        fun toScreenY(y: Double) = ((viewport.maxY - y) / viewport.rangeY * height).toFloat()

        val x1 = viewport.minX
        val y1 = y0 + slope * (x1 - tangentX)
        val x2 = viewport.maxX
        val y2 = y0 + slope * (x2 - tangentX)

        drawLine(
            color = color.copy(alpha = 0.85f),
            start = Offset(toScreenX(x1), toScreenY(y1)),
            end = Offset(toScreenX(x2), toScreenY(y2)),
            strokeWidth = 2.dp.toPx()
        )

        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = Offset(toScreenX(tangentX), toScreenY(y0))
        )
    } catch (_: Exception) {}
}
