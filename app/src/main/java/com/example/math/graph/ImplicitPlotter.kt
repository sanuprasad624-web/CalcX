package com.example.math.graph

import androidx.compose.ui.geometry.Offset
import com.example.math.calculus.Expr
import kotlin.math.abs

data class LineSegment(val start: Offset, val end: Offset)

data class PointOfInterest(
    val x: Double,
    val y: Double,
    val label: String
)

object ImplicitPlotter {

    /**
     * Solves and generates line segments for F(x, y) = 0 using Marching Squares.
     */
    fun plotImplicit(
        expr: Expr,
        parameters: Map<String, Double>,
        viewport: GraphViewport,
        width: Float,
        height: Float,
        isInteractive: Boolean = false
    ): List<LineSegment> {
        val segments = mutableListOf<LineSegment>()
        val gridSize = if (isInteractive) 45 else 85
        val nx = gridSize
        val ny = gridSize

        val dxMath = viewport.rangeX / nx
        val dyMath = viewport.rangeY / ny

        val values = Array(nx + 1) { DoubleArray(ny + 1) }
        val vars = parameters.toMutableMap()

        // 1. Evaluate grid
        for (i in 0..nx) {
            val x = viewport.minX + i * dxMath
            vars["x"] = x
            for (j in 0..ny) {
                val y = viewport.minY + j * dyMath
                vars["y"] = y
                values[i][j] = try {
                    val v = expr.eval(vars)
                    if (v.isNaN() || v.isInfinite()) 0.0 else v
                } catch (_: Exception) {
                    0.0
                }
            }
        }

        fun toScreen(mathX: Double, mathY: Double): Offset {
            val sx = ((mathX - viewport.minX) / viewport.rangeX * width).toFloat()
            val sy = ((viewport.maxY - mathY) / viewport.rangeY * height).toFloat()
            return Offset(sx, sy)
        }

        // Interpolate zero crossing on edge
        fun interp(v1: Double, v2: Double, pos1: Double, pos2: Double): Double {
            val denom = v2 - v1
            if (abs(denom) < 1e-12) return (pos1 + pos2) / 2.0
            val t = (-v1) / denom
            return pos1 + t.coerceIn(0.0, 1.0) * (pos2 - pos1)
        }

        // 2. March through cells
        for (i in 0 until nx) {
            val x0 = viewport.minX + i * dxMath
            val x1 = x0 + dxMath
            for (j in 0 until ny) {
                val y0 = viewport.minY + j * dyMath
                val y1 = y0 + dyMath

                val v0 = values[i][j]         // Bottom-left
                val v1 = values[i + 1][j]     // Bottom-right
                val v2 = values[i + 1][j + 1] // Top-right
                val v3 = values[i][j + 1]     // Top-left

                var caseIndex = 0
                if (v0 > 0) caseIndex = caseIndex or 1
                if (v1 > 0) caseIndex = caseIndex or 2
                if (v2 > 0) caseIndex = caseIndex or 4
                if (v3 > 0) caseIndex = caseIndex or 8

                if (caseIndex == 0 || caseIndex == 15) continue

                // Edge zero-crossings
                // Edge 0: bottom (x0, y0) to (x1, y0)
                val e0 = { toScreen(interp(v0, v1, x0, x1), y0) }
                // Edge 1: right (x1, y0) to (x1, y1)
                val e1 = { toScreen(x1, interp(v1, v2, y0, y1)) }
                // Edge 2: top (x0, y1) to (x1, y1)
                val e2 = { toScreen(interp(v3, v2, x0, x1), y1) }
                // Edge 3: left (x0, y0) to (x0, y1)
                val e3 = { toScreen(x0, interp(v0, v3, y0, y1)) }

                when (caseIndex) {
                    1, 14 -> segments.add(LineSegment(e0(), e3()))
                    2, 13 -> segments.add(LineSegment(e0(), e1()))
                    3, 12 -> segments.add(LineSegment(e3(), e1()))
                    4, 11 -> segments.add(LineSegment(e1(), e2()))
                    5 -> {
                        segments.add(LineSegment(e0(), e3()))
                        segments.add(LineSegment(e1(), e2()))
                    }
                    6, 9 -> segments.add(LineSegment(e0(), e2()))
                    7, 8 -> segments.add(LineSegment(e3(), e2()))
                    10 -> {
                        segments.add(LineSegment(e0(), e1()))
                        segments.add(LineSegment(e3(), e2()))
                    }
                }
            }
        }

        return segments
    }

    /**
     * Finds key points of interest (e.g. intercepts with axes) for implicit or explicit curves.
     */
    fun findPointsOfInterest(
        expr: Expr,
        parameters: Map<String, Double>,
        viewport: GraphViewport
    ): List<PointOfInterest> {
        val points = mutableListOf<PointOfInterest>()
        val vars = parameters.toMutableMap()

        // 1. X-intercepts (y = 0): search along X-axis
        vars["y"] = 0.0
        val numSamples = 200
        val stepX = viewport.rangeX / numSamples
        var prevX = viewport.minX
        vars["x"] = prevX
        var prevV = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

        for (i in 1..numSamples) {
            val curX = viewport.minX + i * stepX
            vars["x"] = curX
            val curV = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

            if (!prevV.isNaN() && !curV.isNaN() && (prevV * curV <= 0.0)) {
                // Bisection to refine root
                var lo = prevX
                var hi = curX
                for (k in 0..12) {
                    val mid = (lo + hi) / 2.0
                    vars["x"] = mid
                    val midV = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
                    if (midV * prevV <= 0) {
                        hi = mid
                    } else {
                        lo = mid
                    }
                }
                val rootX = (lo + hi) / 2.0
                points.add(PointOfInterest(rootX, 0.0, "(${String.format(java.util.Locale.US, "%.3f", rootX)}, 0)"))
            }
            prevX = curX
            prevV = curV
        }

        // 2. Y-intercepts (x = 0): search along Y-axis
        vars["x"] = 0.0
        val stepY = viewport.rangeY / numSamples
        var prevY = viewport.minY
        vars["y"] = prevY
        var prevVY = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

        for (j in 1..numSamples) {
            val curY = viewport.minY + j * stepY
            vars["y"] = curY
            val curVY = try { expr.eval(vars) } catch (_: Exception) { Double.NaN }

            if (!prevVY.isNaN() && !curVY.isNaN() && (prevVY * curVY <= 0.0)) {
                var lo = prevY
                var hi = curY
                for (k in 0..12) {
                    val mid = (lo + hi) / 2.0
                    vars["y"] = mid
                    val midV = try { expr.eval(vars) } catch (_: Exception) { 0.0 }
                    if (midV * prevVY <= 0) {
                        hi = mid
                    } else {
                        lo = mid
                    }
                }
                val rootY = (lo + hi) / 2.0
                points.add(PointOfInterest(0.0, rootY, "(0, ${String.format(java.util.Locale.US, "%.3f", rootY)})"))
            }
            prevY = curY
            prevVY = curVY
        }

        return points
    }
}
