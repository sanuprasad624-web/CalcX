package com.example.math.renderer

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Native Android Canvas Renderer for Mathematical Expressions.
 * Renders high-precision vector graphics, fractional bars, dynamic radical vinculums,
 * scaling delimiters, and matrix brackets onto Jetpack Compose DrawScope.
 */
class MathCanvasRenderer(
    private val textMeasurer: TextMeasurer,
    private val fontFamily: FontFamily = FontFamily.Default
) {

    fun render(
        drawScope: DrawScope,
        box: MathLayoutBox,
        originX: Float = 0f,
        originY: Float = 0f,
        color: Color,
        debugMode: Boolean = false
    ) {
        renderBox(drawScope, box, originX, originY, color, debugMode)
    }

    private fun renderBox(
        drawScope: DrawScope,
        box: MathLayoutBox,
        offsetX: Float,
        offsetY: Float,
        color: Color,
        debugMode: Boolean
    ) {
        val curX = offsetX + box.x
        val curY = offsetY + box.y

        // Debug visualization: bounding box & baseline
        if (debugMode) {
            drawScope.drawRect(
                color = Color(0x3300BCD4),
                topLeft = Offset(curX, curY),
                size = Size(box.width, box.height),
                style = Stroke(width = 1f)
            )
            // Baseline in subtle red/coral
            val baselineY = curY + box.baseline
            drawScope.drawLine(
                color = Color(0x88FF5252),
                start = Offset(curX, baselineY),
                end = Offset(curX + box.width, baselineY),
                strokeWidth = 1f
            )
        }

        when (box.style) {
            BoxStyle.TEXT -> {
                val txt = box.text
                if (!txt.isNullOrEmpty()) {
                    val isItalic = (box.node as? VariableNode)?.isItalic == true ||
                            (box.node as? TextNode)?.isItalic == true
                    val isBold = (box.node as? TextNode)?.isBold == true ||
                            (box.node as? OperatorNode)?.op in listOf("=", "≠", "≤", "≥")

                    val textStyle = TextStyle(
                        fontSize = box.fontSizeSp.sp,
                        fontFamily = fontFamily,
                        fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        color = color
                    )
                    drawScope.drawText(
                        textMeasurer = textMeasurer,
                        text = AnnotatedString(txt),
                        topLeft = Offset(curX, curY),
                        style = textStyle
                    )
                }
            }

            BoxStyle.FRACTION -> {
                val data = box.customData as? FractionData
                if (data != null) {
                    val barY = curY + data.barY
                    // Draw clean horizontal vinculum (fraction bar)
                    drawScope.drawLine(
                        color = color,
                        start = Offset(curX, barY),
                        end = Offset(curX + data.barWidth, barY),
                        strokeWidth = data.barThickness,
                        cap = StrokeCap.Round
                    )
                }
                // Render numerator and denominator children
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }

            BoxStyle.RADICAL -> {
                val data = box.customData as? RadicalData
                if (data != null) {
                    val contentBox = data.contentBox
                    val surdW = data.surdWidth
                    val thick = data.vinculumThickness

                    val topY = curY + (contentBox.y - thick - 2f)
                    val bottomY = curY + box.height - 2f
                    val hookY = bottomY - ((bottomY - topY) * 0.45f)
                    val dipY = bottomY - 1f

                    val path = Path().apply {
                        // Left hook
                        moveTo(curX + (surdW * 0.15f), hookY)
                        lineTo(curX + (surdW * 0.35f), hookY - (3f * box.fontScale))
                        // Downward dip
                        lineTo(curX + (surdW * 0.55f), dipY)
                        // Diagonal rise to top vinculum
                        lineTo(curX + surdW, topY)
                        // Vinculum covering exact width of child content
                        lineTo(curX + box.width - 1f, topY)
                    }

                    drawScope.drawPath(
                        path = path,
                        color = color,
                        style = Stroke(
                            width = thick,
                            cap = StrokeCap.Square,
                            join = StrokeJoin.Miter
                        )
                    )
                }
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }

            BoxStyle.DELIMITED, BoxStyle.ABS -> {
                val data = box.customData as? DelimData
                if (data != null) {
                    val strokeW = (1.8f * box.fontScale).coerceAtLeast(1.2f)
                    val delimH = box.height
                    val delimTop = curY
                    val delimBottom = curY + delimH

                    // Draw left delimiter
                    drawDelimiter(
                        drawScope = drawScope,
                        type = data.leftDelim,
                        x = curX + strokeW,
                        top = delimTop,
                        bottom = delimBottom,
                        width = 8f * box.fontScale,
                        color = color,
                        strokeWidth = strokeW,
                        isLeft = true
                    )

                    // Draw right delimiter
                    val rightX = curX + box.width - (8f * box.fontScale) - strokeW
                    drawDelimiter(
                        drawScope = drawScope,
                        type = data.rightDelim,
                        x = rightX,
                        top = delimTop,
                        bottom = delimBottom,
                        width = 8f * box.fontScale,
                        color = color,
                        strokeWidth = strokeW,
                        isLeft = false
                    )
                }
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }

            BoxStyle.MATRIX -> {
                val data = box.customData as? MatrixData
                if (data != null) {
                    val strokeW = (2f * box.fontScale).coerceAtLeast(1.4f)
                    val tickLen = 6f * box.fontScale
                    val top = curY + 2f
                    val bottom = curY + box.height - 2f

                    if (data.isDeterminant) {
                        // Vertical bar on left & right
                        drawScope.drawLine(
                            color = color,
                            start = Offset(curX + strokeW, top),
                            end = Offset(curX + strokeW, bottom),
                            strokeWidth = strokeW
                        )
                        drawScope.drawLine(
                            color = color,
                            start = Offset(curX + box.width - strokeW, top),
                            end = Offset(curX + box.width - strokeW, bottom),
                            strokeWidth = strokeW
                        )
                    } else {
                        // Square bracket on left: ┌ | └
                        val leftX = curX + strokeW
                        val leftPath = Path().apply {
                            moveTo(leftX + tickLen, top)
                            lineTo(leftX, top)
                            lineTo(leftX, bottom)
                            lineTo(leftX + tickLen, bottom)
                        }
                        drawScope.drawPath(
                            path = leftPath,
                            color = color,
                            style = Stroke(width = strokeW, cap = StrokeCap.Square)
                        )

                        // Square bracket on right: ┐ | ┘
                        val rightX = curX + box.width - strokeW
                        val rightPath = Path().apply {
                            moveTo(rightX - tickLen, top)
                            lineTo(rightX, top)
                            lineTo(rightX, bottom)
                            lineTo(rightX - tickLen, bottom)
                        }
                        drawScope.drawPath(
                            path = rightPath,
                            color = color,
                            style = Stroke(width = strokeW, cap = StrokeCap.Square)
                        )
                    }
                }
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }

            BoxStyle.VECTOR -> {
                val data = box.customData as? VectorData
                if (data != null) {
                    val arrowY = curY + (2f * box.fontScale)
                    val strokeW = (1.5f * box.fontScale).coerceAtLeast(1.0f)
                    val arrowW = box.width - (2f * box.fontScale)
                    val startX = curX + (1f * box.fontScale)
                    val endX = startX + arrowW

                    // Main vector shaft
                    drawScope.drawLine(
                        color = color,
                        start = Offset(startX, arrowY),
                        end = Offset(endX, arrowY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    // Arrow head: >
                    val headSize = 3.5f * box.fontScale
                    drawScope.drawLine(
                        color = color,
                        start = Offset(endX - headSize, arrowY - headSize),
                        end = Offset(endX, arrowY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                    drawScope.drawLine(
                        color = color,
                        start = Offset(endX - headSize, arrowY + headSize),
                        end = Offset(endX, arrowY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }

            BoxStyle.ROW, BoxStyle.BIG_OP, BoxStyle.LIMIT -> {
                for (child in box.children) {
                    renderBox(drawScope, child, curX, curY, color, debugMode)
                }
            }
        }
    }

    private fun drawDelimiter(
        drawScope: DrawScope,
        type: DelimType,
        x: Float,
        top: Float,
        bottom: Float,
        width: Float,
        color: Color,
        strokeWidth: Float,
        isLeft: Boolean
    ) {
        when (type) {
            DelimType.BAR, DelimType.DOUBLE_BAR -> {
                val drawX = if (isLeft) x + (width / 2f) else x + (width / 2f)
                drawScope.drawLine(
                    color = color,
                    start = Offset(drawX, top),
                    end = Offset(drawX, bottom),
                    strokeWidth = strokeWidth
                )
                if (type == DelimType.DOUBLE_BAR) {
                    val secondX = drawX + (strokeWidth * 1.8f)
                    drawScope.drawLine(
                        color = color,
                        start = Offset(secondX, top),
                        end = Offset(secondX, bottom),
                        strokeWidth = strokeWidth
                    )
                }
            }
            DelimType.BRACKET, DelimType.BRACKET_CLOSE -> {
                val tick = width * 0.7f
                val path = Path().apply {
                    if (isLeft) {
                        moveTo(x + tick, top)
                        lineTo(x, top)
                        lineTo(x, bottom)
                        lineTo(x + tick, bottom)
                    } else {
                        moveTo(x, top)
                        lineTo(x + tick, top)
                        lineTo(x + tick, bottom)
                        lineTo(x, bottom)
                    }
                }
                drawScope.drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Square))
            }
            DelimType.PAREN, DelimType.PAREN_CLOSE -> {
                val h = bottom - top
                val bowW = (width * 1.1f).coerceAtLeast(6f * strokeWidth)
                val path = Path().apply {
                    if (isLeft) {
                        moveTo(x + bowW, top + 1f)
                        cubicTo(
                            x - (bowW * 0.2f), top + h * 0.25f,
                            x - (bowW * 0.2f), bottom - h * 0.25f,
                            x + bowW, bottom - 1f
                        )
                    } else {
                        moveTo(x, top + 1f)
                        cubicTo(
                            x + bowW * 1.2f, top + h * 0.25f,
                            x + bowW * 1.2f, bottom - h * 0.25f,
                            x, bottom - 1f
                        )
                    }
                }
                drawScope.drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            DelimType.BRACE, DelimType.BRACE_CLOSE -> {
                val midY = (top + bottom) / 2f
                val tipX = if (isLeft) x else x + width
                val baseEdgeX = if (isLeft) x + width else x
                val path = Path().apply {
                    moveTo(baseEdgeX, top)
                    quadraticTo(baseEdgeX, (top + midY) / 2f, tipX, midY)
                    quadraticTo(baseEdgeX, (midY + bottom) / 2f, baseEdgeX, bottom)
                }
                drawScope.drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            DelimType.ANGLE, DelimType.ANGLE_CLOSE -> {
                val midY = (top + bottom) / 2f
                val tipX = if (isLeft) x else x + width
                val baseX = if (isLeft) x + width else x
                val path = Path().apply {
                    moveTo(baseX, top)
                    lineTo(tipX, midY)
                    lineTo(baseX, bottom)
                }
                drawScope.drawPath(path = path, color = color, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
        }
    }
}
