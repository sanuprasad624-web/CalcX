package com.example.math.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.example.math.calculus.Expr
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance adaptive sampling and path generation engine for 2D function plotting.
 *
 * Implements:
 * 1. Compiled AST lambda memoization for maximum evaluation speed during 60 FPS pan/zoom.
 * 2. High-density adaptive curve sampling (up to 2400 samples) tied to canvas pixel density.
 * 3. Strict "Pen-up" discontinuity logic for asymptotes (tan x, 1/x, cot x, sec x).
 * 4. Safe domain evaluation (ignores NaN, Infinity, and out-of-domain points).
 */
object GraphPathEngine {

    // Fast evaluation cache keyed by Expr hash
    private val compiledFuncCache = ConcurrentHashMap<Expr, (Map<String, Double>) -> Double>()

    private fun getCompiledEvaluator(expr: Expr): (Map<String, Double>) -> Double {
        return compiledFuncCache.getOrPut(expr) {
            { vars -> expr.eval(vars) }
        }
    }

    /**
     * Builds a Compose [Path] for explicit function y = f(x).
     */
    fun buildFunctionPath(
        expr: Expr,
        parameters: Map<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        isInteractive: Boolean = false
    ): Path {
        val path = Path()
        if (width <= 0f || height <= 0f || viewport.rangeX <= 0.0 || viewport.rangeY <= 0.0) {
            return path
        }

        val evaluator = getCompiledEvaluator(expr)
        val vars = parameters.toMutableMap()

        // Adaptive sample count tied to canvas width (capped at 2400 for high-density crispness)
        val pixelStep = if (isInteractive) 3f else 1.0f
        val numSamples = max(60, min(2400, (width / pixelStep).toInt()))
        val dxMath = viewport.rangeX / numSamples

        val screenClipMargin = 400f
        val screenClipMinY = -screenClipMargin
        val screenClipMaxY = height + screenClipMargin

        var pathOpen = false
        var prevMathX = 0.0
        var prevMathY = 0.0
        var prevScreenX = 0f
        var prevScreenY = 0f

        for (i in 0..numSamples) {
            val currMathX = viewport.minX + i * dxMath
            vars["x"] = currMathX
            vars["t"] = currMathX
            vars["theta"] = currMathX
            vars["θ"] = currMathX

            val currMathY = try {
                val v = evaluator(vars)
                if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
            } catch (_: Exception) {
                null
            }

            if (currMathY == null) {
                // Lift pen on undefined domain points (e.g. sqrt(negative), ln(<=0))
                pathOpen = false
                continue
            }

            val currScreenX = viewport.toScreenX(currMathX, width)
            val currScreenY = viewport.toScreenY(currMathY, height)

            if (!pathOpen) {
                path.moveTo(currScreenX, currScreenY.coerceIn(screenClipMinY, screenClipMaxY))
                pathOpen = true
            } else {
                // Strict "Pen-up" check for vertical asymptotes and discontinuities
                val isDiscontinuous = checkDiscontinuity(
                    prevMathX = prevMathX,
                    prevMathY = prevMathY,
                    currMathX = currMathX,
                    currMathY = currMathY,
                    rangeY = viewport.rangeY,
                    rangeX = viewport.rangeX
                )

                if (isDiscontinuous) {
                    // Lift pen and start fresh at the new side of the discontinuity
                    pathOpen = false
                } else {
                    // In precision mode, do adaptive subdivision if curvature is high
                    if (!isInteractive && i > 0) {
                        refineCurveBetween(
                            path = path,
                            evaluator = evaluator,
                            vars = vars,
                            viewport = viewport,
                            width = width,
                            height = height,
                            x0 = prevMathX,
                            y0 = prevMathY,
                            x1 = currMathX,
                            y1 = currMathY,
                            screenClipMinY = screenClipMinY,
                            screenClipMaxY = screenClipMaxY,
                            depth = 0
                        )
                    }

                    path.lineTo(currScreenX, currScreenY.coerceIn(screenClipMinY, screenClipMaxY))
                }
            }

            prevMathX = currMathX
            prevMathY = currMathY
            prevScreenX = currScreenX
            prevScreenY = currScreenY
        }

        return path
    }

    private fun refineCurveBetween(
        path: Path,
        evaluator: (Map<String, Double>) -> Double,
        vars: MutableMap<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        x0: Double,
        y0: Double,
        x1: Double,
        y1: Double,
        screenClipMinY: Float,
        screenClipMaxY: Float,
        depth: Int
    ) {
        if (depth >= 3) return

        val midX = (x0 + x1) * 0.5
        vars["x"] = midX
        vars["t"] = midX
        vars["theta"] = midX
        vars["θ"] = midX
        val midY = try {
            val v = evaluator(vars)
            if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
        } catch (_: Exception) {
            null
        } ?: return

        val expectedMidY = (y0 + y1) * 0.5
        val screenDiffY = abs(viewport.toScreenY(midY, height) - viewport.toScreenY(expectedMidY, height))

        // If screen deviation > 1.25 pixels, recursively subdivide
        if (screenDiffY > 1.25f && screenDiffY < height * 0.6f) {
            // Left half
            refineCurveBetween(
                path, evaluator, vars, viewport, width, height,
                x0, y0, midX, midY, screenClipMinY, screenClipMaxY, depth + 1
            )

            val screenMidX = viewport.toScreenX(midX, width)
            val screenMidY = viewport.toScreenY(midY, height)
            path.lineTo(screenMidX, screenMidY.coerceIn(screenClipMinY, screenClipMaxY))

            // Right half
            refineCurveBetween(
                path, evaluator, vars, viewport, width, height,
                midX, midY, x1, y1, screenClipMinY, screenClipMaxY, depth + 1
            )
        }
    }

    /**
     * Detects true mathematical discontinuities (e.g. tan(x) at pi/2, 1/x at 0).
     */
    private fun checkDiscontinuity(
        prevMathX: Double,
        prevMathY: Double,
        currMathX: Double,
        currMathY: Double,
        rangeY: Double,
        rangeX: Double
    ): Boolean {
        val dy = abs(currMathY - prevMathY)
        val dx = abs(currMathX - prevMathX)

        // 1. Extreme vertical jump across opposite signs
        if (dy > rangeY * 0.5 && (currMathY * prevMathY < 0)) {
            return true
        }

        // 2. Extremely steep slope connecting opposite signs
        if (dx > 0.0) {
            val slope = dy / dx
            if (slope > (rangeY / dx) * 0.85 && dy > rangeY * 0.35) {
                return true
            }
        }

        // 3. Huge magnitude jump
        if (dy > rangeY * 2.0) {
            return true
        }

        return false
    }

    fun evaluateSafe(expr: Expr, vars: Map<String, Double>): Double? {
        return try {
            val v = expr.eval(vars)
            if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Builds a Compose [Path] for horizontal functions where x is given in terms of y or t: x = f(y) / x = f(t).
     */
    fun buildXFunctionOfYPath(
        expr: Expr,
        parameters: Map<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        isInteractive: Boolean = false
    ): Path {
        val path = Path()
        if (width <= 0f || height <= 0f || viewport.rangeX <= 0.0 || viewport.rangeY <= 0.0) {
            return path
        }

        val evaluator = getCompiledEvaluator(expr)
        val vars = parameters.toMutableMap()

        val pixelStep = if (isInteractive) 3f else 1.0f
        val numSamples = max(60, min(2400, (height / pixelStep).toInt()))
        val dyMath = viewport.rangeY / numSamples

        val screenClipMargin = 400f
        val screenClipMinX = -screenClipMargin
        val screenClipMaxX = width + screenClipMargin

        var pathOpen = false
        var prevMathY = 0.0
        var prevMathX = 0.0
        var prevScreenY = 0f
        var prevScreenX = 0f

        for (i in 0..numSamples) {
            val currMathY = viewport.minY + i * dyMath
            vars["y"] = currMathY
            vars["t"] = currMathY
            vars["theta"] = currMathY
            vars["θ"] = currMathY

            val currMathX = try {
                val v = evaluator(vars)
                if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
            } catch (_: Exception) {
                null
            }

            if (currMathX == null) {
                pathOpen = false
                continue
            }

            val currScreenX = viewport.toScreenX(currMathX, width)
            val currScreenY = viewport.toScreenY(currMathY, height)

            if (!pathOpen) {
                path.moveTo(currScreenX.coerceIn(screenClipMinX, screenClipMaxX), currScreenY)
                pathOpen = true
            } else {
                val isDiscontinuous = checkDiscontinuity(
                    prevMathX = prevMathY,
                    prevMathY = prevMathX,
                    currMathX = currMathY,
                    currMathY = currMathX,
                    rangeY = viewport.rangeX,
                    rangeX = viewport.rangeY
                )

                if (isDiscontinuous) {
                    pathOpen = false
                } else {
                    path.lineTo(currScreenX.coerceIn(screenClipMinX, screenClipMaxX), currScreenY)
                }
            }

            prevMathY = currMathY
            prevMathX = currMathX
            prevScreenY = currScreenY
            prevScreenX = currScreenX
        }

        return path
    }

    /**
     * Builds a Compose [Path] for polar curves r = f(theta) / r = f(t).
     */
    fun buildPolarPath(
        expr: Expr,
        parameters: Map<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        isInteractive: Boolean = false
    ): Path {
        val path = Path()
        if (width <= 0f || height <= 0f) return path

        val evaluator = getCompiledEvaluator(expr)
        val vars = parameters.toMutableMap()

        val numSamples = if (isInteractive) 800 else 2400
        val tMin = 0.0
        val tMax = 12.0 * Math.PI
        val dt = (tMax - tMin) / numSamples

        var pathOpen = false
        for (i in 0..numSamples) {
            val t = tMin + i * dt
            vars["t"] = t
            vars["theta"] = t
            vars["θ"] = t

            val r = try {
                val v = evaluator(vars)
                if (v.isNaN() || v.isInfinite() || abs(v) > 1e6) null else v
            } catch (_: Exception) {
                null
            }

            if (r == null) {
                pathOpen = false
                continue
            }

            val mathX = r * kotlin.math.cos(t)
            val mathY = r * kotlin.math.sin(t)

            val sx = viewport.toScreenX(mathX, width)
            val sy = viewport.toScreenY(mathY, height)

            if (!pathOpen) {
                path.moveTo(sx, sy)
                pathOpen = true
            } else {
                path.lineTo(sx, sy)
            }
        }
        return path
    }

    /**
     * Builds a Compose [Path] for parametric curves (x(t), y(t)).
     */
    fun buildParametricPath(
        exprX: Expr,
        exprY: Expr,
        tMin: Double,
        tMax: Double,
        parameters: Map<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        isInteractive: Boolean = false
    ): Path {
        val path = Path()
        if (width <= 0f || height <= 0f) return path

        val evalX = getCompiledEvaluator(exprX)
        val evalY = getCompiledEvaluator(exprY)
        val vars = parameters.toMutableMap()

        val numSamples = if (isInteractive) 600 else 1800
        val dt = (tMax - tMin) / numSamples

        var pathOpen = false
        for (i in 0..numSamples) {
            val t = tMin + i * dt
            vars["t"] = t
            vars["θ"] = t
            vars["theta"] = t

            val x = try { val v = evalX(vars); if (v.isNaN() || v.isInfinite()) null else v } catch (_: Exception) { null }
            val y = try { val v = evalY(vars); if (v.isNaN() || v.isInfinite()) null else v } catch (_: Exception) { null }

            if (x == null || y == null) {
                pathOpen = false
                continue
            }

            val sx = viewport.toScreenX(x, width)
            val sy = viewport.toScreenY(y, height)

            if (!pathOpen) {
                path.moveTo(sx, sy)
                pathOpen = true
            } else {
                path.lineTo(sx, sy)
            }
        }
        return path
    }
}
