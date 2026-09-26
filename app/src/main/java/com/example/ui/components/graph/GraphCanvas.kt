package com.example.ui.components.graph

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.calculus.Expr
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
    val density = LocalDensity.current

    // Desmos styling colors
    val canvasBg = Color(0xFFFFFFFF)
    val majorGridColor = Color(0xFFE2E8F0)
    val minorGridColor = Color(0xFFF1F5F9)
    val axisColor = Color(0xFF334155)

    var isInteracting by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf<IntSize?>(null) }

    // Trace / Inspector tooltip state
    var activeTooltip by remember { mutableStateOf<Pair<PointOfInterest, Offset>?>(null) }
    var traceCrosshair by remember { mutableStateOf<Offset?>(null) }
    var traceCoords by remember { mutableStateOf<Pair<Double, Double>?>(null) }

    // Reusable text paints for axis numbers
    val axisTextPaint = remember(density) {
        Paint().apply {
            color = android.graphics.Color.rgb(0x33, 0x41, 0x55)
            textSize = with(density) { 11.sp.toPx() }
            isAntiAlias = true
            typeface = Typeface.DEFAULT
        }
    }

    // Points of Interest cache (evaluated when settled or expression changes)
    var pointsOfInterest by remember { mutableStateOf<List<Pair<GraphFunction, List<PointOfInterest>>>>(emptyList()) }

    LaunchedEffect(functions, viewport, parameters, isInteracting) {
        if (!isInteracting) {
            val result = mutableListOf<Pair<GraphFunction, List<PointOfInterest>>>()
            for (fn in functions) {
                if (!fn.isVisible || fn.parsedExpr == null) continue
                val pois = findRootsAndIntercepts(fn.parsedExpr, parameters, viewport)
                result.add(Pair(fn, pois))
            }
            pointsOfInterest = result
        }
    }

    var interactionResetTrigger by remember { mutableIntStateOf(0) }
    LaunchedEffect(interactionResetTrigger) {
        if (isInteracting) {
            delay(120)
            isInteracting = false
        }
    }

    val currentViewportState by rememberUpdatedState(viewport)
    val onViewportChangeState by rememberUpdatedState(onViewportChange)

    // Ensure 1:1 Aspect ratio on initial canvas measurement and resize
    LaunchedEffect(canvasSize) {
        canvasSize?.let { sz ->
            if (sz.width > 0 && sz.height > 0) {
                val adjusted = currentViewportState.withAspectRatio(sz.width.toFloat(), sz.height.toFloat())
                if (adjusted != currentViewportState) {
                    onViewportChangeState(adjusted)
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(canvasBg)
            .onSizeChanged { canvasSize = it }
            .testTag("graph_canvas_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("graph_canvas")
                // Start-anchored 1:1 pan and focal-point pinch zoom + drag-to-trace on curves
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val firstDown = awaitFirstDown(requireUnconsumed = false)
                        val startViewport = currentViewportState
                        var currentBaseViewport = startViewport
                        var startCentroid: Offset? = null
                        var lastActivePointerIds = emptySet<androidx.compose.ui.input.pointer.PointerId>()
                        var previousSpan: Float? = null

                        val screenWidth = size.width.toFloat()
                        val screenHeight = size.height.toFloat()

                        // Check if touch down is near any curve or POI to initiate curve trace mode
                        val startTrace = if (screenWidth > 0f && screenHeight > 0f) {
                            findClosestCurvePoint(firstDown.position, currentViewportState, screenWidth, screenHeight, functions, parameters, pointsOfInterest)
                        } else null

                        var isTracing = (startTrace != null && startTrace.screenDist < 54f)
                        if (isTracing && startTrace != null) {
                            activeTooltip = Pair(PointOfInterest(startTrace.mathX, startTrace.mathY, startTrace.label), Offset(startTrace.screenX, startTrace.screenY))
                            traceCrosshair = Offset(startTrace.screenX, startTrace.screenY)
                            traceCoords = Pair(startTrace.mathX, startTrace.mathY)
                        }

                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.any { it.isConsumed }
                            if (canceled) break

                            val pressedChanges = event.changes.filter { it.pressed }
                            val pointerCount = pressedChanges.size
                            if (pointerCount == 0) break

                            val currentPointerIds = pressedChanges.map { it.id }.toSet()
                            val currentWidth = size.width.toFloat()
                            val currentHeight = size.height.toFloat()

                            if (currentWidth > 0f && currentHeight > 0f) {
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
                                } else null

                                val pointersChanged = currentPointerIds != lastActivePointerIds

                                if (pointerCount >= 2) {
                                    isTracing = false
                                }

                                if (isTracing && pointerCount == 1) {
                                    // Slide / drag along curve: update trace point in real-time
                                    val trace = findClosestCurvePoint(currentCentroid, currentBaseViewport, currentWidth, currentHeight, functions, parameters, pointsOfInterest)
                                    if (trace != null) {
                                        activeTooltip = Pair(PointOfInterest(trace.mathX, trace.mathY, trace.label), Offset(trace.screenX, trace.screenY))
                                        traceCrosshair = Offset(trace.screenX, trace.screenY)
                                        traceCoords = Pair(trace.mathX, trace.mathY)
                                    }
                                } else {
                                    isInteracting = true
                                    if (pointersChanged || startCentroid == null) {
                                        startCentroid = currentCentroid
                                        currentBaseViewport = currentViewportState
                                        previousSpan = currentSpan
                                        lastActivePointerIds = currentPointerIds
                                    } else {
                                        val deltaX = currentCentroid.x - startCentroid.x
                                        val deltaY = currentCentroid.y - startCentroid.y

                                        // 1. Separate Zoom Step with 0.015 (1.5%) Deadzone
                                        val prevSpan = previousSpan
                                        if (currentSpan != null && prevSpan != null && prevSpan > 8f && currentSpan > 8f) {
                                            val zoomFactor = (currentSpan / prevSpan).toDouble()
                                            if (zoomFactor in 0.5..2.0 && abs(zoomFactor - 1.0) > 0.015) {
                                                currentBaseViewport = currentBaseViewport.zoomAroundScreenPoint(
                                                    focalScreenX = currentCentroid.x,
                                                    focalScreenY = currentCentroid.y,
                                                    zoomFactor = zoomFactor,
                                                    screenWidth = currentWidth,
                                                    screenHeight = currentHeight
                                                )
                                            }
                                        }

                                        // 2. Separate Pan Step computed from start anchor
                                        var newVp = currentBaseViewport.panFromStart(
                                            startViewport = currentBaseViewport,
                                            totalPanPixelsX = deltaX,
                                            totalPanPixelsY = deltaY,
                                            screenWidth = currentWidth,
                                            screenHeight = currentHeight
                                        )

                                        // 3. Enforce 1:1 Aspect Ratio on Viewport
                                        newVp = newVp.withAspectRatio(currentWidth, currentHeight)

                                        if (newVp != currentViewportState) {
                                            onViewportChangeState(newVp)
                                        }

                                        previousSpan = currentSpan
                                    }
                                }
                            }

                            event.changes.forEach {
                                if (it.positionChange() != Offset.Zero) {
                                    it.consume()
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        interactionResetTrigger++
                    }
                }
                // Tap & trace inspector
                .pointerInput(functions, viewport, parameters) {
                    detectTapGestures(
                        onTap = { offset ->
                            val screenWidth = size.width.toFloat()
                            val screenHeight = size.height.toFloat()
                            if (screenWidth <= 0f || screenHeight <= 0f) return@detectTapGestures

                            val trace = findClosestCurvePoint(offset, viewport, screenWidth, screenHeight, functions, parameters, pointsOfInterest)
                            if (trace != null && trace.screenDist < 54f) {
                                activeTooltip = Pair(PointOfInterest(trace.mathX, trace.mathY, trace.label), Offset(trace.screenX, trace.screenY))
                                traceCrosshair = Offset(trace.screenX, trace.screenY)
                                traceCoords = Pair(trace.mathX, trace.mathY)
                            } else {
                                activeTooltip = null
                                traceCrosshair = null
                                traceCoords = null
                            }
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            if (width <= 0f || height <= 0f) return@Canvas

            // 1. Draw Desmos Grid & Nice Numbers on Axes
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

            // 2. Draw Main Coordinate Axes (x = 0, y = 0)
            if (showAxes) {
                val originX = viewport.toScreenX(0.0, width)
                val originY = viewport.toScreenY(0.0, height)

                if (originX in -2f..width + 2f) {
                    drawLine(
                        color = axisColor,
                        start = Offset(originX, 0f),
                        end = Offset(originX, height),
                        strokeWidth = 1.4.dp.toPx()
                    )
                }

                if (originY in -2f..height + 2f) {
                    drawLine(
                        color = axisColor,
                        start = Offset(0f, originY),
                        end = Offset(width, originY),
                        strokeWidth = 1.4.dp.toPx()
                    )
                }
            }

            // 3. Draw Functions & Curves using GraphPathEngine
            val vars = parameters.toMutableMap()

            functions.forEach { fn ->
                if (!fn.isVisible || fn.parsedExpr == null) return@forEach
                val expr = fn.parsedExpr

                if (fn.isVerticalLine) {
                    // Vertical line & vertical inequality (e.g. x <= 3, x > -2)
                    drawVerticalLineAndShading(fn, viewport, width, height)
                } else if (fn.isPolar) {
                    // Polar curve r = f(theta) / r = f(t) (e.g. r = 3 sin(5 theta), r = 1 - cos(theta))
                    val curvePath = GraphPathEngine.buildPolarPath(
                        expr = expr,
                        parameters = vars,
                        viewport = viewport,
                        width = width,
                        height = height,
                        isInteractive = isInteracting
                    )
                    val isStrict = (fn.inequalityType == com.example.math.graph.InequalityType.LESS_THAN || fn.inequalityType == com.example.math.graph.InequalityType.GREATER_THAN)
                    drawPath(
                        path = curvePath,
                        color = fn.color,
                        style = Stroke(
                            width = 2.8.dp.toPx(),
                            pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                        )
                    )
                } else if (fn.isXFunctionOfY) {
                    // Horizontal function x = f(y) or x = f(t) (e.g. x = 3 sin(5t), x = y^2 - 4)
                    if (fn.inequalityType != com.example.math.graph.InequalityType.NONE) {
                        drawHorizontalInequalityShading(expr, fn.inequalityType, vars, viewport, width, height, fn.color)
                    }

                    val curvePath = GraphPathEngine.buildXFunctionOfYPath(
                        expr = expr,
                        parameters = vars,
                        viewport = viewport,
                        width = width,
                        height = height,
                        isInteractive = isInteracting
                    )
                    val isStrict = (fn.inequalityType == com.example.math.graph.InequalityType.LESS_THAN || fn.inequalityType == com.example.math.graph.InequalityType.GREATER_THAN)
                    drawPath(
                        path = curvePath,
                        color = fn.color,
                        style = Stroke(
                            width = 2.8.dp.toPx(),
                            pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                        )
                    )
                } else if (fn.isParametric && fn.parametricX != null && fn.parametricY != null) {
                    // Parametric curve: (f(t), g(t))
                    val curvePath = GraphPathEngine.buildParametricPath(
                        exprX = fn.parametricX,
                        exprY = fn.parametricY,
                        tMin = fn.tMin,
                        tMax = fn.tMax,
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
                } else if (fn.isImplicit) {
                    // A. Implicit 2D inequality region shading (e.g. x^2 + y^2 <= 9)
                    if (fn.inequalityType != com.example.math.graph.InequalityType.NONE) {
                        val rects = ImplicitPlotter.plotImplicitShading(
                            expr = expr,
                            inequalityType = fn.inequalityType,
                            parameters = parameters,
                            viewport = viewport,
                            width = width,
                            height = height,
                            isInteractive = isInteracting
                        )
                        for (r in rects) {
                            drawRect(
                                color = fn.color.copy(alpha = 0.22f),
                                topLeft = r.topLeft,
                                size = r.size
                            )
                        }
                    }

                    // B. Implicit boundary curves (circles, ellipses, hyperbolas, general conics)
                    val isStrict = (fn.inequalityType == com.example.math.graph.InequalityType.LESS_THAN || fn.inequalityType == com.example.math.graph.InequalityType.GREATER_THAN)
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
                            strokeWidth = 2.8.dp.toPx(),
                            pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                        )
                    }
                } else {
                    // Explicit functions y = f(x) or inequalities y <= f(x), y >= f(x), y < f(x), y > f(x)
                    // A. Inequality Region Shading
                    if (fn.inequalityType != com.example.math.graph.InequalityType.NONE) {
                        drawInequalityShading(expr, fn.inequalityType, vars, viewport, width, height, fn.color)
                    }

                    // B. Shaded Definite Integral Region & Live Area Display
                    if (fn.showIntegralArea) {
                        drawIntegralShading(fn, vars, viewport, width, height)
                    }

                    // C. Main Function Curve (high-density adaptive sampling + pen-up asymptote break)
                    val curvePath = GraphPathEngine.buildFunctionPath(
                        expr = expr,
                        parameters = vars,
                        viewport = viewport,
                        width = width,
                        height = height,
                        isInteractive = isInteracting
                    )
                    val isStrict = (fn.inequalityType == com.example.math.graph.InequalityType.LESS_THAN || fn.inequalityType == com.example.math.graph.InequalityType.GREATER_THAN)
                    drawPath(
                        path = curvePath,
                        color = fn.color,
                        style = Stroke(
                            width = 2.8.dp.toPx(),
                            pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
                        )
                    )

                    // D. Derivative Overlay f'(x)
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

                    // E. Tangent Line Overlay
                    if (fn.showTangent) {
                        drawTangentLine(expr, vars, fn.tangentX, viewport, width, height, fn.color)
                    }
                }
            }

            // 4. Draw Points of Interest (Roots & Intercepts)
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

            // 5. Draw Active Trace Crosshair & Coordinate Indicator
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

        // Active point tooltip (density-aware positioning clamped safely inside canvas bounds)
        activeTooltip?.let { (poi, screenOffset) ->
            val screenW = canvasSize?.width?.toFloat() ?: 1000f
            val screenH = canvasSize?.height?.toFloat() ?: 1000f
            val posX = (screenOffset.x - 30f).coerceIn(12f, (screenW - 140f).coerceAtLeast(12f))
            val posY = (screenOffset.y - 48f).coerceIn(12f, (screenH - 50f).coerceAtLeast(12f))

            Surface(
                modifier = Modifier
                    .offset(
                        x = with(density) { posX.toDp() },
                        y = with(density) { posY.toDp() }
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
 * Draws the Desmos-style square grid lines and labels numbers on the axes with independent nice number step scaling per axis.
 */
private fun DrawScope.drawDesmosGridAndLabels(
    viewport: GraphViewport,
    width: Float,
    height: Float,
    majorColor: Color,
    minorColor: Color,
    textPaint: Paint
) {
    val majorStepX = calculateNiceStep(viewport.rangeX)
    val majorStepY = calculateNiceStep(viewport.rangeY)
    val minorStepX = (majorStepX / 5.0).coerceAtLeast(1e-6)
    val minorStepY = (majorStepY / 5.0).coerceAtLeast(1e-6)

    val originX = viewport.toScreenX(0.0, width)
    val originY = viewport.toScreenY(0.0, height)

    // Minor Vertical Grid Lines (X)
    val startMinorX = floor(viewport.minX / minorStepX) * minorStepX
    var curMinorX = startMinorX
    var minorXCount = 0
    while (curMinorX <= viewport.maxX + minorStepX * 0.5 && minorXCount < 150) {
        val sx = viewport.toScreenX(curMinorX, width)
        if (sx in 0f..width && abs(sx - originX) > 1.5f) {
            drawLine(
                color = minorColor,
                start = Offset(sx, 0f),
                end = Offset(sx, height),
                strokeWidth = 0.5.dp.toPx()
            )
        }
        curMinorX += minorStepX
        minorXCount++
    }

    // Minor Horizontal Grid Lines (Y)
    val startMinorY = floor(viewport.minY / minorStepY) * minorStepY
    var curMinorY = startMinorY
    var minorYCount = 0
    while (curMinorY <= viewport.maxY + minorStepY * 0.5 && minorYCount < 150) {
        val sy = viewport.toScreenY(curMinorY, height)
        if (sy in 0f..height && abs(sy - originY) > 1.5f) {
            drawLine(
                color = minorColor,
                start = Offset(0f, sy),
                end = Offset(width, sy),
                strokeWidth = 0.5.dp.toPx()
            )
        }
        curMinorY += minorStepY
        minorYCount++
    }

    // Major Vertical Grid Lines (X)
    val startMajorX = floor(viewport.minX / majorStepX) * majorStepX
    var curMajorX = startMajorX
    var majorXCount = 0
    while (curMajorX <= viewport.maxX + majorStepX * 0.5 && majorXCount < 50) {
        val sx = viewport.toScreenX(curMajorX, width)
        if (sx in 0f..width && abs(sx - originX) > 1.5f) {
            drawLine(
                color = majorColor,
                start = Offset(sx, 0f),
                end = Offset(sx, height),
                strokeWidth = 0.9.dp.toPx()
            )
        }
        curMajorX += majorStepX
        majorXCount++
    }

    // Major Horizontal Grid Lines (Y)
    val startMajorY = floor(viewport.minY / majorStepY) * majorStepY
    var curMajorY = startMajorY
    var majorYCount = 0
    while (curMajorY <= viewport.maxY + majorStepY * 0.5 && majorYCount < 50) {
        val sy = viewport.toScreenY(curMajorY, height)
        if (sy in 0f..height && abs(sy - originY) > 1.5f) {
            drawLine(
                color = majorColor,
                start = Offset(0f, sy),
                end = Offset(width, sy),
                strokeWidth = 0.9.dp.toPx()
            )
        }
        curMajorY += majorStepY
        majorYCount++
    }

    // Numbers on X and Y axes
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

    // X-axis numbers
    textPaint.textAlign = Paint.Align.CENTER
    curMajorX = startMajorX
    var numXCount = 0
    while (curMajorX <= viewport.maxX + majorStepX * 0.5 && numXCount < 50) {
        if (abs(curMajorX) > 1e-9) {
            val sx = viewport.toScreenX(curMajorX, width)
            if (sx in 10f..(width - 10f)) {
                val label = formatAxisNumber(curMajorX, majorStepX)
                drawContext.canvas.nativeCanvas.drawText(label, sx, labelYForXAxis, textPaint)
            }
        }
        curMajorX += majorStepX
        numXCount++
    }

    // Y-axis numbers
    textPaint.textAlign = Paint.Align.RIGHT
    curMajorY = startMajorY
    var numYCount = 0
    while (curMajorY <= viewport.maxY + majorStepY * 0.5 && numYCount < 50) {
        if (abs(curMajorY) > 1e-9) {
            val sy = viewport.toScreenY(curMajorY, height)
            if (sy in 15f..(height - 15f)) {
                val label = formatAxisNumber(curMajorY, majorStepY)
                drawContext.canvas.nativeCanvas.drawText(label, labelXForYAxis, sy + 4.dp.toPx(), textPaint)
            }
        }
        curMajorY += majorStepY
        numYCount++
    }

    // Origin "0" (avoid duplicate zeros)
    if (originX in 20f..(width - 20f) && originY in 20f..(height - 20f)) {
        textPaint.textAlign = Paint.Align.RIGHT
        drawContext.canvas.nativeCanvas.drawText("0", originX - 6.dp.toPx(), originY + 15.dp.toPx(), textPaint)
    }
}

/**
 * Nice number algorithm: rounds step to 1, 2, or 5 * 10^k.
 */
private fun calculateNiceStep(range: Double): Double {
    val safeRange = if (range.isNaN() || range.isInfinite() || range <= 0.0) 20.0 else range
    val roughStep = (safeRange / 5.5).coerceIn(1e-6, 1e8)
    val exponent = floor(log10(roughStep))
    val fraction = roughStep / 10.0.pow(exponent)
    val niceFraction = when {
        fraction < 1.5 -> 1.0
        fraction < 3.5 -> 2.0
        fraction < 7.5 -> 5.0
        else -> 10.0
    }
    return (niceFraction * 10.0.pow(exponent)).coerceIn(1e-6, 1e8)
}

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

private fun formatPiOrNumber(value: Double): String {
    if (abs(value) < 1e-10) return "0"
    val pi = Math.PI

    // Check common fractions of pi: k*pi/d for d in [1, 2, 3, 4, 6, 8, 12]
    val denominators = intArrayOf(1, 2, 3, 4, 6, 8, 12)
    for (d in denominators) {
        val exactMultiple = (value * d) / pi
        val roundK = Math.round(exactMultiple)
        val diff = abs(value - (roundK * pi / d))

        val tolerance = 0.006 * (1.0 + abs(roundK).toDouble() / d)
        if (diff < tolerance && roundK != 0L) {
            val g = gcd(abs(roundK), d.toLong())
            val num = roundK / g
            val den = d / g
            val sign = if (num < 0) "-" else ""
            val absNum = abs(num)

            return when {
                den == 1L && absNum == 1L -> "${sign}π"
                den == 1L -> "${num}π"
                absNum == 1L -> "${sign}π/$den"
                else -> "${sign}${absNum}π/$den"
            }
        }
    }

    val rounded = Math.round(value)
    if (abs(value - rounded) < 1e-5) return rounded.toString()
    return String.format(Locale.US, "%.3f", value).trimEnd('0').trimEnd('.')
}

private fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)

private fun formatCoord(value: Double): String {
    return formatPiOrNumber(value)
}

private data class TraceResult(
    val mathX: Double,
    val mathY: Double,
    val screenX: Float,
    val screenY: Float,
    val label: String,
    val screenDist: Float
)

private fun findClosestCurvePoint(
    touchPos: Offset,
    viewport: GraphViewport,
    screenWidth: Float,
    screenHeight: Float,
    functions: List<GraphFunction>,
    parameters: Map<String, Double>,
    pointsOfInterest: List<Pair<GraphFunction, List<PointOfInterest>>>
): TraceResult? {
    val mathX = viewport.toWorldX(touchPos.x, screenWidth)
    val mathY = viewport.toWorldY(touchPos.y, screenHeight)

    // 1. Check Points of Interest first with snap radius
    var closestPoi: PointOfInterest? = null
    var minPoiDist = Float.MAX_VALUE
    for ((_, pois) in pointsOfInterest) {
        for (poi in pois) {
            val sx = viewport.toScreenX(poi.x, screenWidth)
            val sy = viewport.toScreenY(poi.y, screenHeight)
            val dist = kotlin.math.hypot(sx - touchPos.x, sy - touchPos.y)
            if (dist < 36f && dist < minPoiDist) {
                minPoiDist = dist
                closestPoi = poi
            }
        }
    }

    if (closestPoi != null) {
        val sx = viewport.toScreenX(closestPoi.x, screenWidth)
        val sy = viewport.toScreenY(closestPoi.y, screenHeight)
        val label = "(${formatPiOrNumber(closestPoi.x)}, ${formatPiOrNumber(closestPoi.y)})"
        return TraceResult(closestPoi.x, closestPoi.y, sx, sy, label, minPoiDist)
    }

    var bestResult: TraceResult? = null
    var minScreenDist = Float.MAX_VALUE

    for (fn in functions) {
        if (!fn.isVisible || fn.parsedExpr == null) continue
        val expr = fn.parsedExpr
        val vars = parameters.toMutableMap()

        when {
            // A. Vertical line x = c
            fn.isVerticalLine -> {
                val cx = fn.verticalX
                val cy = mathY.coerceIn(viewport.minY, viewport.maxY)
                val sx = viewport.toScreenX(cx, screenWidth)
                val sy = viewport.toScreenY(cy, screenHeight)
                val dist = abs(sx - touchPos.x)
                if (dist < minScreenDist) {
                    minScreenDist = dist
                    bestResult = TraceResult(cx, cy, sx, sy, "(${formatPiOrNumber(cx)}, ${formatPiOrNumber(cy)})", dist)
                }
            }

            // B. Horizontal wave x = f(y)
            fn.isXFunctionOfY -> {
                vars["y"] = mathY
                vars["t"] = mathY
                vars["theta"] = mathY
                vars["θ"] = mathY
                val cx = try {
                    val v = expr.eval(vars)
                    if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
                } catch (_: Exception) { null }

                if (cx != null) {
                    val sx = viewport.toScreenX(cx, screenWidth)
                    val sy = viewport.toScreenY(mathY, screenHeight)
                    val dist = abs(sx - touchPos.x)
                    if (dist < minScreenDist) {
                        minScreenDist = dist
                        bestResult = TraceResult(cx, mathY, sx, sy, "(${formatPiOrNumber(cx)}, ${formatPiOrNumber(mathY)})", dist)
                    }
                }
            }

            // C. Polar curve r = f(theta)
            fn.isPolar -> {
                val theta0 = atan2(mathY, mathX).let { if (it < 0) it + 2 * Math.PI else it }
                val thetas = mutableListOf<Double>()
                for (k in 0..6) {
                    thetas.add(theta0 + k * 2 * Math.PI)
                    thetas.add(theta0 + Math.PI + k * 2 * Math.PI)
                }
                for (th in thetas) {
                    vars["t"] = th
                    vars["theta"] = th
                    vars["θ"] = th
                    val r = try {
                        val v = expr.eval(vars)
                        if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
                    } catch (_: Exception) { null }

                    if (r != null) {
                        val px = r * kotlin.math.cos(th)
                        val py = r * kotlin.math.sin(th)
                        val sx = viewport.toScreenX(px, screenWidth)
                        val sy = viewport.toScreenY(py, screenHeight)
                        val dist = kotlin.math.hypot(sx - touchPos.x, sy - touchPos.y)
                        if (dist < minScreenDist) {
                            minScreenDist = dist
                            bestResult = TraceResult(px, py, sx, sy, "(${formatPiOrNumber(px)}, ${formatPiOrNumber(py)})", dist)
                        }
                    }
                }
            }

            // D. Parametric (x(t), y(t))
            fn.isParametric && fn.parametricX != null && fn.parametricY != null -> {
                val tMin = fn.tMin
                val tMax = fn.tMax
                val samples = 120
                val dt = (tMax - tMin) / samples
                for (i in 0..samples) {
                    val t = tMin + i * dt
                    vars["t"] = t
                    vars["theta"] = t
                    vars["θ"] = t
                    val px = try { val v = fn.parametricX.eval(vars); if (v.isNaN() || v.isInfinite()) null else v } catch (_: Exception) { null }
                    val py = try { val v = fn.parametricY.eval(vars); if (v.isNaN() || v.isInfinite()) null else v } catch (_: Exception) { null }
                    if (px != null && py != null) {
                        val sx = viewport.toScreenX(px, screenWidth)
                        val sy = viewport.toScreenY(py, screenHeight)
                        val dist = kotlin.math.hypot(sx - touchPos.x, sy - touchPos.y)
                        if (dist < minScreenDist) {
                            minScreenDist = dist
                            bestResult = TraceResult(px, py, sx, sy, "(${formatPiOrNumber(px)}, ${formatPiOrNumber(py)})", dist)
                        }
                    }
                }
            }

            // E. Implicit curves (e.g. x^2 + y^2 = 25)
            fn.isImplicit -> {
                val deltaY = viewport.rangeY * 0.08
                val yLow = mathY - deltaY
                val yHigh = mathY + deltaY
                vars["x"] = mathX
                vars["y"] = yLow
                val valLow = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }
                vars["y"] = yHigh
                val valHigh = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

                if (!valLow.isNaN() && !valHigh.isNaN() && valLow * valHigh <= 0) {
                    var l = yLow
                    var h = yHigh
                    for (step in 0..10) {
                        val m = (l + h) * 0.5
                        vars["y"] = m
                        val vm = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
                        if (vm * valLow <= 0) h = m else l = m
                    }
                    val foundY = (l + h) * 0.5
                    val sx = viewport.toScreenX(mathX, screenWidth)
                    val sy = viewport.toScreenY(foundY, screenHeight)
                    val dist = abs(sy - touchPos.y)
                    if (dist < minScreenDist) {
                        minScreenDist = dist
                        bestResult = TraceResult(mathX, foundY, sx, sy, "(${formatPiOrNumber(mathX)}, ${formatPiOrNumber(foundY)})", dist)
                    }
                }
            }

            // F. Explicit function y = f(x)
            else -> {
                vars["x"] = mathX
                vars["t"] = mathX
                vars["theta"] = mathX
                vars["θ"] = mathX
                val cy = try {
                    val v = expr.eval(vars)
                    if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
                } catch (_: Exception) { null }

                if (cy != null) {
                    val sx = viewport.toScreenX(mathX, screenWidth)
                    val sy = viewport.toScreenY(cy, screenHeight)
                    val dist = abs(sy - touchPos.y)
                    if (dist < minScreenDist) {
                        minScreenDist = dist
                        bestResult = TraceResult(mathX, cy, sx, sy, "(${formatPiOrNumber(mathX)}, ${formatPiOrNumber(cy)})", dist)
                    }
                }
            }
        }
    }

    return bestResult
}

private fun DrawScope.drawInequalityShading(
    expr: Expr,
    inequalityType: com.example.math.graph.InequalityType,
    vars: MutableMap<String, Double>,
    viewport: GraphViewport,
    width: Float,
    height: Float,
    color: Color
) {
    if (inequalityType == com.example.math.graph.InequalityType.NONE) return

    val isLessThan = (inequalityType == com.example.math.graph.InequalityType.LESS_THAN || inequalityType == com.example.math.graph.InequalityType.LESS_EQUAL)
    val evaluator = { v: Map<String, Double> -> expr.eval(v) }
    val numSamples = min(500, (width / 2.0).toInt().coerceAtLeast(60))
    val dxMath = viewport.rangeX / numSamples

    val screenClipMargin = 50f
    val baseScreenY = if (isLessThan) height + screenClipMargin else -screenClipMargin

    val shadePath = Path()
    var segmentOpen = false
    var segStartX = 0f

    for (i in 0..numSamples) {
        val mathX = viewport.minX + i * dxMath
        vars["x"] = mathX
        val mathY = try {
            val v = evaluator(vars)
            if (v.isNaN() || v.isInfinite()) null else v
        } catch (_: Exception) {
            null
        }

        if (mathY == null) {
            if (segmentOpen) {
                val prevScreenX = viewport.toScreenX(viewport.minX + (i - 1) * dxMath, width)
                shadePath.lineTo(prevScreenX, baseScreenY)
                shadePath.lineTo(segStartX, baseScreenY)
                shadePath.close()
                segmentOpen = false
            }
            continue
        }

        val screenX = viewport.toScreenX(mathX, width)
        val screenY = viewport.toScreenY(mathY, height).coerceIn(-height, height * 2)

        if (!segmentOpen) {
            shadePath.moveTo(screenX, baseScreenY)
            shadePath.lineTo(screenX, screenY)
            segStartX = screenX
            segmentOpen = true
        } else {
            shadePath.lineTo(screenX, screenY)
        }
    }

    if (segmentOpen) {
        val lastScreenX = viewport.toScreenX(viewport.maxX, width)
        shadePath.lineTo(lastScreenX, baseScreenY)
        shadePath.lineTo(segStartX, baseScreenY)
        shadePath.close()
    }

    drawPath(path = shadePath, color = color.copy(alpha = 0.22f))
}

private fun DrawScope.drawHorizontalInequalityShading(
    expr: Expr,
    inequalityType: com.example.math.graph.InequalityType,
    vars: MutableMap<String, Double>,
    viewport: GraphViewport,
    width: Float,
    height: Float,
    color: Color
) {
    if (inequalityType == com.example.math.graph.InequalityType.NONE) return

    val isLessThan = (inequalityType == com.example.math.graph.InequalityType.LESS_THAN || inequalityType == com.example.math.graph.InequalityType.LESS_EQUAL)
    val evaluator = { v: Map<String, Double> -> expr.eval(v) }
    val numSamples = min(500, (height / 2.0).toInt().coerceAtLeast(60))
    val dyMath = viewport.rangeY / numSamples

    val screenClipMargin = 50f
    val baseScreenX = if (isLessThan) -screenClipMargin else width + screenClipMargin

    val shadePath = Path()
    var segmentOpen = false
    var segStartY = 0f

    for (i in 0..numSamples) {
        val mathY = viewport.minY + i * dyMath
        vars["y"] = mathY
        vars["t"] = mathY
        vars["theta"] = mathY
        vars["θ"] = mathY

        val mathX = try {
            val v = evaluator(vars)
            if (v.isNaN() || v.isInfinite()) null else v
        } catch (_: Exception) {
            null
        }

        if (mathX == null) {
            if (segmentOpen) {
                val prevScreenY = viewport.toScreenY(viewport.minY + (i - 1) * dyMath, height)
                shadePath.lineTo(baseScreenX, prevScreenY)
                shadePath.lineTo(baseScreenX, segStartY)
                shadePath.close()
                segmentOpen = false
            }
            continue
        }

        val screenX = viewport.toScreenX(mathX, width).coerceIn(-width, width * 2)
        val screenY = viewport.toScreenY(mathY, height)

        if (!segmentOpen) {
            shadePath.moveTo(baseScreenX, screenY)
            shadePath.lineTo(screenX, screenY)
            segStartY = screenY
            segmentOpen = true
        } else {
            shadePath.lineTo(screenX, screenY)
        }
    }

    if (segmentOpen) {
        val lastScreenY = viewport.toScreenY(viewport.maxY, height)
        shadePath.lineTo(baseScreenX, lastScreenY)
        shadePath.lineTo(baseScreenX, segStartY)
        shadePath.close()
    }

    drawPath(path = shadePath, color = color.copy(alpha = 0.22f))
}

private fun DrawScope.drawVerticalLineAndShading(
    fn: GraphFunction,
    viewport: GraphViewport,
    width: Float,
    height: Float
) {
    val xVal = fn.verticalX
    val screenX = viewport.toScreenX(xVal, width)

    // 1. Shading if inequality
    if (fn.inequalityType != com.example.math.graph.InequalityType.NONE) {
        val (left, right) = when (fn.inequalityType) {
            com.example.math.graph.InequalityType.LESS_THAN, com.example.math.graph.InequalityType.LESS_EQUAL -> Pair(0f, screenX.coerceIn(0f, width))
            com.example.math.graph.InequalityType.GREATER_THAN, com.example.math.graph.InequalityType.GREATER_EQUAL -> Pair(screenX.coerceIn(0f, width), width)
            com.example.math.graph.InequalityType.NONE -> Pair(0f, 0f)
        }
        if (right > left) {
            drawRect(
                color = fn.color.copy(alpha = 0.22f),
                topLeft = Offset(left, 0f),
                size = androidx.compose.ui.geometry.Size(right - left, height)
            )
        }
    }

    // 2. Line boundary
    if (screenX in -2f..width + 2f) {
        val isStrict = (fn.inequalityType == com.example.math.graph.InequalityType.LESS_THAN || fn.inequalityType == com.example.math.graph.InequalityType.GREATER_THAN)
        drawLine(
            color = fn.color,
            start = Offset(screenX, 0f),
            end = Offset(screenX, height),
            strokeWidth = 2.8.dp.toPx(),
            pathEffect = if (isStrict) PathEffect.dashPathEffect(floatArrayOf(14f, 10f)) else null
        )
    }
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
    expr: Expr,
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

/**
 * Live root-finding and intercept detection using bisection.
 */
private fun findRootsAndIntercepts(
    expr: Expr,
    parameters: Map<String, Double>,
    viewport: GraphViewport
): List<PointOfInterest> {
    val pois = mutableListOf<PointOfInterest>()
    val vars = parameters.toMutableMap()

    // 1. Y-intercept (x = 0)
    if (0.0 in viewport.minX..viewport.maxX) {
        vars["x"] = 0.0
        try {
            val y0 = expr.eval(vars)
            if (!y0.isNaN() && !y0.isInfinite() && y0 in viewport.minY..viewport.maxY) {
                pois.add(PointOfInterest(0.0, y0, "(0, ${formatCoord(y0)})"))
            }
        } catch (_: Exception) {}
    }

    // 2. X-intercepts (Roots where f(x) = 0) via bisection
    val steps = 80
    val dx = viewport.rangeX / steps
    var prevX = viewport.minX
    vars["x"] = prevX
    var prevY = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

    for (i in 1..steps) {
        val currX = viewport.minX + i * dx
        vars["x"] = currX
        val currY = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

        if (!prevY.isNaN() && !currY.isNaN() && (prevY * currY <= 0) && abs(currY - prevY) < viewport.rangeY * 0.8) {
            // Bisect to refine root
            var low = prevX
            var high = currX
            for (iter in 0..12) {
                val mid = (low + high) * 0.5
                vars["x"] = mid
                val midY = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
                if (midY * prevY <= 0) {
                    high = mid
                } else {
                    low = mid
                }
            }
            val rootX = (low + high) * 0.5
            pois.add(PointOfInterest(rootX, 0.0, "(${formatCoord(rootX)}, 0)"))
        }

        prevX = currX
        prevY = currY
    }

    return pois
}
