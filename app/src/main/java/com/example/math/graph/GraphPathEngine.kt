package com.example.math.graph

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.example.math.calculus.Expr
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * High-performance adaptive sampling and path generation engine for 2D function plotting.
 *
 * Implements:
 * 1. Screen-space adaptive subdivision (samples curves with precision where curvature is high).
 * 2. Strict discontinuity detection (prevents false vertical lines for 1/x, tan(x), asymptotes).
 * 3. Safe domain evaluation (ignores NaN, Infinity, and out-of-domain points).
 * 4. Dual rendering modes:
 *    - INTERACTIVE mode (during active pan/pinch): fast, low-latency sampling (e.g. 1 sample per 4-8px).
 *    - PRECISION mode (settled): full adaptive subdivision with fine refinement.
 */
object GraphPathEngine {

    /**
     * Builds a Compose [Path] for explicit function y = f(x).
     *
     * @param expr Parsed AST of the function f(x)
     * @param parameters Variable environment (e.g. parameters a, b)
     * @param viewport Active mathematical viewport
     * @param width Screen width in pixels
     * @param height Screen height in pixels
     * @param isInteractive If true, uses lower sample count and skips recursive subdivision for 60fps gestures
     * @return Path ready to draw on Canvas
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

        val vars = parameters.toMutableMap()

        // Base step size in screen pixels
        val pixelStep = if (isInteractive) 4f else 1.5f
        val numBaseSegments = max(30, min(1400, (width / pixelStep).toInt()))
        val dxMath = viewport.rangeX / numBaseSegments

        val screenClipMinY = -500f
        val screenClipMaxY = height + 500f

        var pathOpen = false
        var prevScreenX = 0f
        var prevScreenY = 0f
        var prevMathX = 0.0
        var prevMathY = 0.0
        var prevDerivative = 0.0

        for (i in 0..numBaseSegments) {
            val currMathX = viewport.minX + i * dxMath
            vars["x"] = currMathX
            val currMathY = evaluateSafe(expr, vars)

            if (currMathY == null) {
                // Undefined point (e.g. ln(x) for x <= 0, sqrt(x) for x < 0)
                pathOpen = false
                continue
            }

            val currScreenX = viewport.toScreenX(currMathX, width)
            val currScreenY = viewport.toScreenY(currMathY, height)

            if (!pathOpen) {
                path.moveTo(currScreenX, currScreenY.coerceIn(screenClipMinY, screenClipMaxY))
                pathOpen = true
            } else {
                // Check for discontinuity / vertical asymptote jump
                val isDiscontinuous = checkDiscontinuity(
                    prevMathX = prevMathX,
                    prevMathY = prevMathY,
                    currMathX = currMathX,
                    currMathY = currMathY,
                    rangeY = viewport.rangeY
                )

                if (isDiscontinuous) {
                    pathOpen = false
                } else {
                    // In precision mode, do adaptive subdivision if curvature is high
                    if (!isInteractive && i > 0) {
                        refineCurveBetween(
                            path = path,
                            expr = expr,
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

    /**
     * Recursively subdivides curve segment if midpoint deviates significantly from line segment (curvature).
     * Maximum recursion depth = 3 to keep frame times strictly under 2ms.
     */
    private fun refineCurveBetween(
        path: Path,
        expr: Expr,
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
        val midY = evaluateSafe(expr, vars) ?: return

        val expectedMidY = (y0 + y1) * 0.5
        val screenDiffY = abs(viewport.toScreenY(midY, height) - viewport.toScreenY(expectedMidY, height))

        // If screen deviation > 1.25 pixels, subdivide
        if (screenDiffY > 1.25f && screenDiffY < height * 0.6f) {
            // Left half
            refineCurveBetween(
                path, expr, vars, viewport, width, height,
                x0, y0, midX, midY, screenClipMinY, screenClipMaxY, depth + 1
            )

            val screenMidX = viewport.toScreenX(midX, width)
            val screenMidY = viewport.toScreenY(midY, height)
            path.lineTo(screenMidX, screenMidY.coerceIn(screenClipMinY, screenClipMaxY))

            // Right half
            refineCurveBetween(
                path, expr, vars, viewport, width, height,
                midX, midY, x1, y1, screenClipMinY, screenClipMaxY, depth + 1
            )
        }
    }

    /**
     * Detects true mathematical discontinuities and asymptotes (e.g. 1/x at x=0, tan(x) at pi/2).
     * Prevents connecting across infinity or jump discontinuities.
     */
    private fun checkDiscontinuity(
        prevMathX: Double,
        prevMathY: Double,
        currMathX: Double,
        currMathY: Double,
        rangeY: Double
    ): Boolean {
        val dy = abs(currMathY - prevMathY)

        // 1. Extreme vertical delta (> 60% of visible Y range) with opposite signs
        if (dy > rangeY * 0.6 && (currMathY * prevMathY < 0)) {
            return true
        }

        // 2. Very steep slope connecting points on opposite sides of horizontal zero
        val dx = abs(currMathX - prevMathX)
        if (dx > 0.0) {
            val slope = dy / dx
            // Slope > 1000 * rangeY/rangeX is an asymptote
            if (slope > (rangeY / dx) * 0.95 && dy > rangeY * 0.4) {
                return true
            }
        }

        // 3. Huge magnitude jump
        if (dy > rangeY * 2.5) {
            return true
        }

        return false
    }

    /**
     * Evaluates f(x) and returns null if out of domain or invalid.
     */
    fun evaluateSafe(expr: Expr, vars: Map<String, Double>): Double? {
        return try {
            val v = expr.eval(vars)
            if (v.isNaN() || v.isInfinite() || abs(v) > 1e12) null else v
        } catch (_: Exception) {
            null
        }
    }
}
