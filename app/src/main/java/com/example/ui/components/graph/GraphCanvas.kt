package com.example.ui.components.graph

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.graph.GraphFunction
import com.example.math.graph.GraphPathEngine
import com.example.math.graph.GraphViewport
import com.example.math.graph.ImplicitPlotter
import com.example.math.graph.PointOfInterest
import kotlinx.coroutines.delay
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

    // Interaction state: switches to INTERACTIVE mode during gestures (lower sampling density for 60fps),
    // and settles to PRECISION mode (fine refinement) 150ms after gesture release.
    var isInteracting by remember { mutableStateOf(false) }

    // Trace / Inspector tooltip state
    var activeTooltip by remember { mutableStateOf<Pair<PointOfInterest, Offset>?>(null) }
    var traceCrosshair by remember { mutableStateOf<Offset?>(null) }
    var traceCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Reusable text paints for axis numbers
    val axisTextPaint = remember {
        Paint().apply {
            color = android.graphics.Color.rgb(0x33, 0x41, 0x55)
            textSize = 28f // ~11sp in px
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
    }

    // Points of Interest cache (evaluated when settled or expression changes, not on every touch micro-delta)
    var pointsOfInterest by remember { mutableStateOf<List<Pair<GraphFunction, List<PointOfInterest>>>>(emptyList()) }

    LaunchedEffect(functions, viewport, parameters, isInteracting) {
        if (!isInteracting) {
            val result = mutableListOf<Pair<GraphFunction, List<PointOfInterest>>>()
            for (fn in functions) {
                if (!fn.isVisible || fn.parsedExpr == null) continue
                val pois = ImplicitPlotter.findPointsOfInterest(fn.parsedExpr, parameters, viewport)
                result.add(Pair(fn, pois))
            }
            pointsOfInterest = result
        }
    }

    // Timer to reset isInteracting after touches finish
    var interactionResetTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(interactionResetTrigger) {
        if (isInteracting) {
            delay(120)
            isInteracting = false
        }
    }

    // Keep latest references for pointer gestures
    val currentViewportState by rememberUpdatedState(viewport)
    val onViewportChangeState by rememberUpdatedState(onViewportChange)

    Box(modifier = modifier.fillMaxSize().background(canvasBg).testTag("graph_canvas_container")) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("graph_canvas")
                // Professional transition-safe 2D pan and focal-point pinch zoom gesture detector
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        var currentVp = currentViewportState
                        var lastActivePointerIds = emptySet<androidx.compose.ui.input.pointer.PointerId>()
                        var previousCentroid: Offset? = null
                        var previousSpan: Float? = null

                        isInteracting = true
                        activeTooltip = null
                        traceCrosshair = null
                        traceCoords = null

                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (canceled) break

                            val pressedChanges = event.changes.filter { it.pressed }
                            val pointerCount = pressedChanges.size
                            if (pointerCount == 0) break

                            val currentPointerIds = pressedChanges.map { it.id }.toSet()
                            val screenWidth = size.width.toFloat()
                            val screenHeight = size.height.toFloat()

                            if (screenWidth > 0f && screenHeight > 0f) {
                                // Compute current centroid of all active touches
                                val currentCentroid = if (pointerCount == 1) {
                                    pressedChanges[0].position
                                } else {
                                    var sumX = 0f
                                    var sumY = 0f
                                    for (p in pressedChanges) {
                                        sumX += p.position.x
                                        sumY += p.position.y
                                    }
                                    Offset(sumX / pointerCount, sumY / pointerCount)
                                }

                                // Compute current span (distance between touches for pinch zoom)
                                val currentSpan = if (pointerCount >= 2) {
                                    if (pointerCount == 2) {
                                        (pressedChanges[0].position - pressedChanges[1].position).getDistance()
                                    } else {
                                        var sumDist = 0.0
                                        for (p in pressedChanges) {
                                            sumDist += (p.position - currentCentroid).getDistance()
                                        }
                                        ((sumDist / pointerCount) * 2.0).toFloat()
                                    }
                                } else {
                                    null
                                }

                                val pointersChanged = currentPointerIds != lastActivePointerIds

                                if (pointersChanged || previousCentroid == null) {
                                    // TRANSITION RE-ANCHOR:
                                    // When a finger touches down or lifts up (e.g. 1 -> 2 fingers or 2 -> 1 finger),
                                    // NEVER calculate delta on this frame! Re-anchor centroid and span seamlessly.
                                    previousCentroid = currentCentroid
                                    previousSpan = currentSpan
                                    lastActivePointerIds = currentPointerIds
                                } else {
                                    // STEADY STATE GESTURE:
                                    val prevCentroid = previousCentroid!!
                                    val panDelta = currentCentroid - prevCentroid
                                    var newVp = currentVp

                                    // 1. Pinch Zoom around actual gesture focal point (currentCentroid)
                                    val prevSpan = previousSpan
                                    if (currentSpan != null && prevSpan != null && prevSpan > 8f && currentSpan > 8f) {
                                        val zoomFactor = (currentSpan / prevSpan).toDouble()
                                        if (zoomFactor in 0.5..2.0 && abs(zoomFactor - 1.0) > 0.0002) {
                                            newVp = newVp.zoomAroundScreenPoint(
                                                focalScreenX = currentCentroid.x,
                                                focalScreenY = currentCentroid.y,
                                                zoomFactor = zoomFactor,
                                                screenWidth = screenWidth,
                                                screenHeight = screenHeight
                                            )
                                        }
                                    }

                                    // 2. Pan by centroid movement
                                    if (panDelta.x != 0f || panDelta.y != 0f) {
                                        newVp = newVp.panByPixels(
                                            panPixelsX = panDelta.x,
                                            panPixelsY = panDelta.y,
                                            screenWidth = screenWidth,
                                            screenHeight = screenHeight
                                        )
                                    }

                                    if (newVp != currentVp) {
                                        currentVp = newVp
                                        onViewportChangeState(newVp)
                                    }

                                    previousCentroid = currentCentroid
                                    previousSpan = currentSpan
                                }
                            }

                            // Consume position changes so parent lists/scrollables do not steal touch events
                            event.changes.forEach {
                                if (it.positionChange() != Offset.Zero) {
                                    it.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        interactionResetTrigger++
                    }
                }
                // Tap to inspect nearest Point of Interest or show coordinate readout
                .pointerInput(functions, viewport, parameters) {
                    detectTapGestures(
                        onTap = { offset ->
                            val screenWidth = size.width.toFloat()
                            val screenHeight = size.height.toFloat()
                            val mathX = viewport.toWorldX(offset.x, screenWidth)
                            val mathY = viewport.toWorldY(offset.y, screenHeight)

                            // Search cached POIs first
                            var foundPoi: PointOfInterest? = null
                            for ((_, pois) in pointsOfInterest) {
                                for (poi in pois) {
                                    val distSq = (poi.x - mathX).pow(2) + (poi.y - mathY).pow(2)
                                    val threshold = (viewport.rangeX * 0.06).pow(2)
                                    if (distSq < threshold) {
                                        foundPoi = poi
                                        break
                                    }
                                }
                                if (foundPoi != null) break
                            }

                            if (foundPoi != null) {
                                val sx = viewport.toScreenX(foundPoi.x, screenWidth)
                                val sy = viewport.toScreenY(foundPoi.y, screenHeight)
                                activeTooltip = Pair(foundPoi, Offset(sx, sy))
                                traceCrosshair = Offset(sx, sy)
                                traceCoords = Pair(foundPoi.x, foundPoi.y)
                            } else {
                                // Check if tapped near an explicit function curve to trace
                                var nearestDist = Double.MAX_VALUE
                                var bestCoord: Pair<Double, Double>? = null

                                for (fn in functions) {
                                    if (!fn.isVisible || fn.parsedExpr == null || fn.isImplicit) continue
                                    val testVars = parameters.toMutableMap()
                                    testVars["x"] = mathX
                                    val curveY = GraphPathEngine.evaluateSafe(fn.parsedExpr, testVars) ?: continue
                                    val screenDist = abs(viewport.toScreenY(curveY, screenHeight) - offset.y)
                                    if (screenDist < 48f && screenDist < nearestDist) {
                                        nearestDist = screenDist.toDouble()
                                        bestCoord = Pair(mathX, curveY)
                                    }
                                }

                                if (bestCoord != null) {
                                    val (cx, cy) = bestCoord
                                    val sx = viewport.toScreenX(cx, screenWidth)
                                    val sy = viewport.toScreenY(cy, screenHeight)
                                    val label = "(${formatCoord(cx)}, ${formatCoord(cy)})"
                                    activeTooltip = Pair(PointOfInterest(cx, cy, label), Offset(sx, sy))
                                    traceCrosshair = Offset(sx, sy)
                                    traceCoords = Pair(cx, cy)
                                } else {
                                    activeTooltip = null
                                    traceCrosshair = null
                                    traceCoords = null
                                }
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // 1. Draw Desmos Grid & Numbers on Axes
            if (showGrid) {
                drawDesmosGridAndLabels(
                    viewport = viewport,
                    width = width,
                    height = height,
                    majorColor = majorGridColor,
                    minorColor = minorGridColor,
                    textPaint = axisTextPaint
                )
            }

            // 2. Draw Main Axes (x = 0, y = 0)
            if (showAxes) {
                val originX = viewport.toScreenX(0.0, width)
                val originY = viewport.toScreenY(0.0, height)

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

            // 3. Draw Functions & Curves using GraphPathEngine
            val vars = parameters.toMutableMap()

            functions.forEach { fn ->
                if (!fn.isVisible || fn.parsedExpr == null) return@forEach
                val expr = fn.parsedExpr

                if (fn.isImplicit) {
                    // IMPLICIT CURVE (e.g. x² + y² = 5 circle / conics) via Marching Squares
                    val segments = ImplicitPlotter.plotImplicit(
                        expr = expr,
                        parameters = parameters,
                        viewport = viewport,
                        width = width,
                        height = height,
                        isInteractive = isInteracting
                    )
                    for (seg in segments) {
                        drawLine(
                            color = fn.color,
                            start = seg.start,
                            end = seg.end,
                            strokeWidth = 2.8.dp.toPx()
                        )
                    }
                } else {
                    // EXPLICIT FUNCTION y = f(x)
                    // A. Integral Area Shading
                    if (fn.showIntegralArea) {
                        drawIntegralShading(fn, vars, viewport, width, height)
                    }

                    // B. Main Function Curve with adaptive sampling & discontinuity detection
                    val curvePath = GraphPathEngine.buildFunctionPath(
                        expr = expr,
                        parameters = vars,
                        viewport = viewport,
                        width = width,
                        height = height,
                        isInteractive = isInteracting
                    )
                    drawPath(
                        path = curvePath,
                        color = fn.color,
                        style = Stroke(width = 2.8.dp.toPx())
                    )

                    // C. Derivative Overlay f'(x)
                    if (fn.showDerivative) {
                        try {
                            val derivExpr = expr.differentiate("x").simplify()
                            val derivPath = GraphPathEngine.buildFunctionPath(
                                expr = derivExpr,
                                parameters = vars,
                                viewport = viewport,
                                width = width,
                                height = height,
                                isInteractive = isInteracting
                            )
                            drawPath(
                                path = derivPath,
                                color = fn.color.copy(alpha = 0.65f),
                                style = Stroke(
                                    width = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                                )
                            )
                        } catch (_: Exception) {}
                    }

                    // D. Tangent Line Overlay
                    if (fn.showTangent) {
                        drawTangentLine(expr, vars, fn.tangentX, viewport, width, height, fn.color)
                    }
                }
            }

            // 4. Draw Points of Interest (roots & intercepts)
            if (!isInteracting) {
                for ((_, pois) in pointsOfInterest) {
                    for (poi in pois) {
                        val sx = viewport.toScreenX(poi.x, width)
                        val sy = viewport.toScreenY(poi.y, height)
                        if (sx in 0f..width && sy in 0f..height) {
                            drawCircle(color = Color(0xFF64748B), radius = 5.dp.toPx(), center = Offset(sx, sy))
                            drawCircle(color = Color.White, radius = 2.5.dp.toPx(), center = Offset(sx, sy))
                        }
                    }
                }
            }

            // 5. Draw Active Trace Crosshair if inspecting
            traceCrosshair?.let { ch ->
                drawLine(
                    color = Color(0xFF64748B).copy(alpha = 0.5f),
                    start = Offset(ch.x, 0f),
                    end = Offset(ch.x, height),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
                drawLine(
                    color = Color(0xFF64748B).copy(alpha = 0.5f),
                    start = Offset(0f, ch.y),
                    end = Offset(width, ch.y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                )
                drawCircle(color = Color(0xFF2563EB), radius = 6.dp.toPx(), center = ch)
                drawCircle(color = Color.White, radius = 3.dp.toPx(), center = ch)
            }
        }

        // Active point tooltip (e.g. "(2.236, 0)")
        activeTooltip?.let { (poi, screenOffset) ->
            Surface(
                modifier = Modifier
                    .padding(
                        start = (screenOffset.x - 40f).coerceIn(10f, 600f).dp / 2.75f,
                        top = (screenOffset.y - 45f).coerceIn(10f, 600f).dp / 2.75f
                    ),
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
 * Only renders visible lines; dynamically adjusts steps according to zoom.
 */
private fun DrawScope.drawDesmosGridAndLabels(
    viewport: GraphViewport,
    width: Float,
    height: Float,
    majorColor: Color,
    minorColor: Color,
    textPaint: Paint
) {
    val majorStep = calculateNiceStep(viewport.rangeX)
    val minorStep = majorStep / 5.0 // 5 square boxes per major step

    val originX = viewport.toScreenX(0.0, width)
    val originY = viewport.toScreenY(0.0, height)

    // 1. Draw Minor Vertical Grid Lines (X)
    val startMinorX = floor(viewport.minX / minorStep) * minorStep
    var curMinorX = startMinorX
    while (curMinorX <= viewport.maxX + minorStep * 0.5) {
        val sx = viewport.toScreenX(curMinorX, width)
        if (sx in 0f..width) {
            drawLine(
                color = minorColor,
                start = Offset(sx, 0f),
                end = Offset(sx, height),
                strokeWidth = 0.8.dp.toPx()
            )
        }
        curMinorX += minorStep
    }

    // 2. Draw Minor Horizontal Grid Lines (Y)
    val startMinorY = floor(viewport.minY / minorStep) * minorStep
    var curMinorY = startMinorY
    while (curMinorY <= viewport.maxY + minorStep * 0.5) {
        val sy = viewport.toScreenY(curMinorY, height)
        if (sy in 0f..height) {
            drawLine(
                color = minorColor,
                start = Offset(0f, sy),
                end = Offset(width, sy),
                strokeWidth = 0.8.dp.toPx()
            )
        }
        curMinorY += minorStep
    }

    // 3. Draw Major Grid Lines
    val startMajorX = floor(viewport.minX / majorStep) * majorStep
    var curMajorX = startMajorX
    while (curMajorX <= viewport.maxX + majorStep * 0.5) {
        val sx = viewport.toScreenX(curMajorX, width)
        if (sx in 0f..width) {
            drawLine(
                color = majorColor,
                start = Offset(sx, 0f),
                end = Offset(sx, height),
                strokeWidth = 1.3.dp.toPx()
            )
        }
        curMajorX += majorStep
    }

    val startMajorY = floor(viewport.minY / majorStep) * majorStep
    var curMajorY = startMajorY
    while (curMajorY <= viewport.maxY + majorStep * 0.5) {
        val sy = viewport.toScreenY(curMajorY, height)
        if (sy in 0f..height) {
            drawLine(
                color = majorColor,
                start = Offset(0f, sy),
                end = Offset(width, sy),
                strokeWidth = 1.3.dp.toPx()
            )
        }
        curMajorY += majorStep
    }

    // 4. DRAW NUMBERS ON AXES (Dynamic with zoom)
    val labelYForXAxis = when {
        originY in 24f..(height - 30f) -> originY + 16.dp.toPx()
        originY < 24f -> 20.dp.toPx()
        else -> height - 8.dp.toPx()
    }

    val labelXForYAxis = when {
        originX in 38f..(width - 15f) -> originX - 8.dp.toPx()
        originX < 38f -> 32.dp.toPx()
        else -> width - 8.dp.toPx()
    }

    // Draw X-axis numbers
    textPaint.textAlign = Paint.Align.CENTER
    curMajorX = startMajorX
    while (curMajorX <= viewport.maxX + majorStep * 0.5) {
        if (abs(curMajorX) > 1e-9) {
            val sx = viewport.toScreenX(curMajorX, width)
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
        if (abs(curMajorY) > 1e-9) {
            val sy = viewport.toScreenY(curMajorY, height)
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
 * Dynamically scales with "nice numbers": 1, 2, 5 * 10^k.
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
 * Formats axis numbers cleanly, avoiding floating-point noise.
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

private fun formatCoord(value: Double): String {
    if (abs(value) < 1e-10) return "0"
    val rounded = round(value)
    if (abs(value - rounded) < 1e-5) return rounded.toLong().toString()
    return String.format(Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
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

    val samples = 120
    val stepX = (b - a) / samples
    val path = Path()

    val screenOriginY = viewport.toScreenY(0.0, height).coerceIn(0f, height)
    path.moveTo(viewport.toScreenX(a, width), screenOriginY)

    for (i in 0..samples) {
        val x = a + i * stepX
        vars["x"] = x
        val y = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
        path.lineTo(viewport.toScreenX(x, width), viewport.toScreenY(y, height))
    }

    path.lineTo(viewport.toScreenX(b, width), screenOriginY)
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

        val x1 = viewport.minX
        val y1 = y0 + slope * (x1 - tangentX)
        val x2 = viewport.maxX
        val y2 = y0 + slope * (x2 - tangentX)

        drawLine(
            color = color.copy(alpha = 0.85f),
            start = Offset(viewport.toScreenX(x1, width), viewport.toScreenY(y1, height)),
            end = Offset(viewport.toScreenX(x2, width), viewport.toScreenY(y2, height)),
            strokeWidth = 2.dp.toPx()
        )

        drawCircle(
            color = color,
            radius = 5.dp.toPx(),
            center = Offset(viewport.toScreenX(tangentX, width), viewport.toScreenY(y0, height))
        )
    } catch (_: Exception) {}
}
