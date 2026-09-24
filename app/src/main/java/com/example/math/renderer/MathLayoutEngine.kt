package com.example.math.renderer

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.max

enum class BoxStyle {
    TEXT,
    FRACTION,
    RADICAL,
    DELIMITED,
    ABS,
    MATRIX,
    BIG_OP,
    LIMIT,
    VECTOR,
    ROW
}

data class MathLayoutBox(
    var x: Float = 0f,
    var y: Float = 0f,
    val width: Float,
    val height: Float,
    val baseline: Float, // Distance from y (top of box) to the baseline
    val node: MathNode,
    val children: List<MathLayoutBox> = emptyList(),
    val text: String? = null,
    val fontSizeSp: Float,
    val fontScale: Float = 1f,
    val style: BoxStyle = BoxStyle.TEXT,
    val customData: Any? = null
) {
    val ascent: Float get() = baseline
    val descent: Float get() = height - baseline
}

data class DelimData(
    val leftDelim: DelimType,
    val rightDelim: DelimType,
    val innerBox: MathLayoutBox
)

data class RadicalData(
    val contentBox: MathLayoutBox,
    val indexBox: MathLayoutBox?,
    val surdWidth: Float,
    val vinculumThickness: Float
)

data class FractionData(
    val numBox: MathLayoutBox,
    val denBox: MathLayoutBox,
    val barThickness: Float,
    val barY: Float,
    val barWidth: Float
)

data class BigOpData(
    val type: BigOpType,
    val opBox: MathLayoutBox,
    val lowerBox: MathLayoutBox?,
    val upperBox: MathLayoutBox?,
    val bodyBox: MathLayoutBox
)

data class MatrixData(
    val rows: Int,
    val cols: Int,
    val isDeterminant: Boolean,
    val colWidths: List<Float>,
    val rowHeights: List<Float>,
    val cellBoxes: List<List<MathLayoutBox>>
)

data class VectorData(
    val baseBox: MathLayoutBox,
    val arrowHeight: Float
)

/**
 * High-performance, deterministic Mathematical Layout Engine.
 * Converts Canonical MathNode AST into precise 2D layout geometry anchored on baselines.
 */
class MathLayoutEngine(
    private val textMeasurer: TextMeasurer,
    private val baseFontSizeSp: Float = 22f,
    private val fontFamily: FontFamily = FontFamily.Default
) {
    private val emToPx: Float get() = baseFontSizeSp * 1.5f // Reference em scale

    fun layout(node: MathNode, scale: Float = 1f): MathLayoutBox {
        return measureNode(node, scale)
    }

    private fun measureNode(node: MathNode, scale: Float): MathLayoutBox {
        return when (node) {
            is NumberNode -> measureText(node.text, node, scale, isItalic = false, isBold = false)
            is VariableNode -> measureVariable(node, scale)
            is SymbolNode -> measureText(node.symbol, node, scale, isItalic = false, isBold = false)
            is OperatorNode -> measureOperator(node, scale)
            is TextNode -> measureText(node.text, node, scale, isItalic = node.isItalic, isBold = node.isBold)
            is ScientificNode -> measureScientific(node, scale)
            is FractionNode -> measureFraction(node, scale)
            is PowerNode -> measurePower(node, scale)
            is SubscriptNode -> measureSubscript(node, scale)
            is PowerSubscriptNode -> measurePowerSubscript(node, scale)
            is RadicalNode -> measureRadical(node, scale)
            is DelimitedNode -> measureDelimited(node, scale)
            is AbsNode -> measureAbs(node, scale)
            is FunctionNode -> measureFunction(node, scale)
            is DerivativeNode -> measureDerivative(node, scale)
            is IntegralNode -> measureIntegral(node, scale)
            is LimitNode -> measureLimit(node, scale)
            is BigOperatorNode -> measureBigOp(node, scale)
            is MatrixNode -> measureMatrix(node, scale)
            is VectorNode -> measureVector(node, scale)
            is ComplexNode -> measureComplex(node, scale)
            is ChemNode -> measureChem(node, scale)
            is UnitNode -> measureUnit(node, scale)
            is RowNode -> measureRow(node.children, node, scale)
        }
    }

    private fun measureText(
        text: String,
        node: MathNode,
        scale: Float,
        isItalic: Boolean = false,
        isBold: Boolean = false
    ): MathLayoutBox {
        val curFontSize = (baseFontSizeSp * scale).coerceAtLeast(10f)
        val style = TextStyle(
            fontSize = curFontSize.sp,
            fontFamily = fontFamily,
            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal
        )
        val layoutRes = textMeasurer.measure(
            text = AnnotatedString(text),
            style = style
        )
        val w = layoutRes.size.width.toFloat()
        val h = layoutRes.size.height.toFloat()
        val baseline = if (layoutRes.firstBaseline > 0) layoutRes.firstBaseline else h * 0.78f

        return MathLayoutBox(
            width = w,
            height = h,
            baseline = baseline,
            node = node,
            text = text,
            fontSizeSp = curFontSize,
            fontScale = scale,
            style = BoxStyle.TEXT
        )
    }

    private fun measureVariable(node: VariableNode, scale: Float): MathLayoutBox {
        val baseBox = measureText(node.name, node, scale, isItalic = node.isItalic, isBold = false)
        if (node.isVector) {
            // Draw vector arrow above
            val arrowH = 4f * scale
            val gap = 2f * scale
            val totalH = baseBox.height + arrowH + gap
            val totalBaseline = baseBox.baseline + arrowH + gap
            baseBox.y = arrowH + gap
            return MathLayoutBox(
                width = baseBox.width + 2f * scale,
                height = totalH,
                baseline = totalBaseline,
                node = node,
                children = listOf(baseBox),
                fontSizeSp = baseBox.fontSizeSp,
                fontScale = scale,
                style = BoxStyle.VECTOR,
                customData = VectorData(baseBox, arrowH)
            )
        }
        if (node.primes > 0) {
            val primeStr = "′".repeat(node.primes)
            val primeBox = measureText(primeStr, node, scale * 0.75f, isItalic = false)
            val children = listOf(baseBox, primeBox)
            return alignRow(children, node, scale)
        }
        if (node.subscript != null) {
            val subBox = measureNode(node.subscript, scale * 0.72f)
            return layoutSubscriptBox(baseBox, subBox, node, scale)
        }
        return baseBox
    }

    private fun measureOperator(node: OperatorNode, scale: Float): MathLayoutBox {
        val opBox = measureText(node.op, node, scale, isItalic = false, isBold = false)
        // Add symmetrical horizontal spacing around binary operators for readability
        val isSpaced = node.op in listOf("+", "-", "±", "∓", "=", "≠", "<", ">", "≤", "≥", "≈", "·", "×", "→", "⟹")
        val hPadding = if (isSpaced) (5f * scale) else (1.5f * scale)
        val w = opBox.width + (hPadding * 2f)
        opBox.x = hPadding
        return MathLayoutBox(
            width = w,
            height = opBox.height,
            baseline = opBox.baseline,
            node = node,
            children = listOf(opBox),
            fontSizeSp = opBox.fontSizeSp,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }

    private fun measureScientific(node: ScientificNode, scale: Float): MathLayoutBox {
        val mantissaBox = measureText(node.mantissa, node, scale)
        val timesBox = measureText(" × 10", node, scale)
        val expBox = measureText(node.exponent, node, scale * 0.72f)
        val powerBox = layoutPowerBox(timesBox, expBox, node, scale)
        return alignRow(listOf(mantissaBox, powerBox), node, scale)
    }

    private fun measureFraction(node: FractionNode, scale: Float): MathLayoutBox {
        // Nested fractions scale down progressively (display style -> text style -> script style)
        val childScale = (scale * 0.88f).coerceAtLeast(0.55f)
        val numBox = measureNode(node.numerator, childScale)
        val denBox = measureNode(node.denominator, childScale)

        val barThickness = (1.8f * scale).coerceAtLeast(1.2f)
        val vPadding = 4f * scale
        val hPadding = 6f * scale
        val totalW = max(numBox.width, denBox.width) + (hPadding * 2f)

        // Center numerator horizontally above bar
        numBox.x = (totalW - numBox.width) / 2f
        numBox.y = 0f

        val barY = numBox.height + vPadding

        // Center denominator horizontally below bar
        denBox.x = (totalW - denBox.width) / 2f
        denBox.y = barY + barThickness + vPadding

        val totalH = denBox.y + denBox.height
        // Baseline sits right at the fraction bar (adjusted for math symbol center)
        val baseline = barY + (barThickness / 2f) + (4f * scale)

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(numBox, denBox),
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.FRACTION,
            customData = FractionData(numBox, denBox, barThickness, barY, totalW)
        )
    }

    private fun measurePower(node: PowerNode, scale: Float): MathLayoutBox {
        val baseBox = measureNode(node.base, scale)
        val expBox = measureNode(node.exponent, (scale * 0.72f).coerceAtLeast(0.5f))
        return layoutPowerBox(baseBox, expBox, node, scale)
    }

    private fun layoutPowerBox(baseBox: MathLayoutBox, expBox: MathLayoutBox, node: MathNode, scale: Float): MathLayoutBox {
        val hGap = 1.5f * scale
        val totalW = baseBox.width + hGap + expBox.width

        // Superscript baseline raised above base baseline
        val shiftUp = baseBox.ascent * 0.65f
        val expBaselineDesired = baseBox.baseline - shiftUp

        val totalAscent = max(baseBox.ascent, shiftUp + expBox.height)
        val totalDescent = baseBox.descent
        val totalH = totalAscent + totalDescent
        val baseline = totalAscent

        baseBox.x = 0f
        baseBox.y = baseline - baseBox.baseline

        expBox.x = baseBox.width + hGap
        expBox.y = (baseline - expBaselineDesired - expBox.baseline).coerceAtLeast(0f)

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(baseBox, expBox),
            fontSizeSp = baseBox.fontSizeSp,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }

    private fun measureSubscript(node: SubscriptNode, scale: Float): MathLayoutBox {
        val baseBox = measureNode(node.base, scale)
        val subBox = measureNode(node.sub, (scale * 0.72f).coerceAtLeast(0.5f))
        return layoutSubscriptBox(baseBox, subBox, node, scale)
    }

    private fun layoutSubscriptBox(baseBox: MathLayoutBox, subBox: MathLayoutBox, node: MathNode, scale: Float): MathLayoutBox {
        val hGap = 1.5f * scale
        val totalW = baseBox.width + hGap + subBox.width

        val shiftDown = baseBox.descent + (subBox.ascent * 0.4f)
        val totalAscent = baseBox.ascent
        val totalDescent = max(baseBox.descent, shiftDown + subBox.descent)
        val totalH = totalAscent + totalDescent
        val baseline = totalAscent

        baseBox.x = 0f
        baseBox.y = baseline - baseBox.baseline

        subBox.x = baseBox.width + hGap
        subBox.y = baseline + (baseBox.descent * 0.5f)

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(baseBox, subBox),
            fontSizeSp = baseBox.fontSizeSp,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }

    private fun measurePowerSubscript(node: PowerSubscriptNode, scale: Float): MathLayoutBox {
        val baseBox = measureNode(node.base, scale)
        val subBox = node.sub?.let { measureNode(it, (scale * 0.72f).coerceAtLeast(0.5f)) }
        val supBox = node.sup?.let { measureNode(it, (scale * 0.72f).coerceAtLeast(0.5f)) }

        val scriptW = max(subBox?.width ?: 0f, supBox?.width ?: 0f)
        val hGap = 1.5f * scale
        val totalW = baseBox.width + hGap + scriptW

        val shiftUp = baseBox.ascent * 0.65f
        val supH = supBox?.height ?: 0f
        val subH = subBox?.height ?: 0f

        val totalAscent = max(baseBox.ascent, if (supBox != null) shiftUp + supH else baseBox.ascent)
        val totalDescent = max(baseBox.descent, if (subBox != null) baseBox.descent + (subH * 0.7f) else baseBox.descent)
        val totalH = totalAscent + totalDescent
        val baseline = totalAscent

        baseBox.x = 0f
        baseBox.y = baseline - baseBox.baseline

        val children = mutableListOf(baseBox)

        if (supBox != null) {
            supBox.x = baseBox.width + hGap
            supBox.y = (baseline - shiftUp - supBox.baseline).coerceAtLeast(0f)
            children.add(supBox)
        }
        if (subBox != null) {
            subBox.x = baseBox.width + hGap
            subBox.y = baseline + (baseBox.descent * 0.4f)
            children.add(subBox)
        }

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = children,
            fontSizeSp = baseBox.fontSizeSp,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }

    private fun measureRadical(node: RadicalNode, scale: Float): MathLayoutBox {
        val contentBox = measureNode(node.content, scale)
        val indexBox = node.index?.let { measureNode(it, (scale * 0.58f).coerceAtLeast(0.45f)) }

        val surdWidth = (14f * scale).coerceAtLeast(10f)
        val vinculumThickness = (1.8f * scale).coerceAtLeast(1.2f)
        val topPadding = 3f * scale
        val bottomPadding = 2f * scale
        val hPadding = 3f * scale

        val contentX = surdWidth + hPadding
        val contentY = topPadding + vinculumThickness + topPadding
        contentBox.x = contentX
        contentBox.y = contentY

        val totalW = contentX + contentBox.width + hPadding
        val totalH = contentY + contentBox.height + bottomPadding
        val baseline = contentY + contentBox.baseline

        val children = mutableListOf(contentBox)
        if (indexBox != null) {
            indexBox.x = 2f * scale
            indexBox.y = 0f
            children.add(indexBox)
        }

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = children,
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.RADICAL,
            customData = RadicalData(contentBox, indexBox, surdWidth, vinculumThickness)
        )
    }

    private fun measureDelimited(node: DelimitedNode, scale: Float): MathLayoutBox {
        val innerBox = measureNode(node.inner, scale)
        // Delimiters adapt dynamically to height of enclosed expression
        val delimWidth = (8f * scale).coerceAtLeast(6f)
        val totalW = innerBox.width + (delimWidth * 2f) + (4f * scale)
        val totalH = innerBox.height + (4f * scale)
        val baseline = innerBox.baseline + (2f * scale)

        innerBox.x = delimWidth + (2f * scale)
        innerBox.y = 2f * scale

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(innerBox),
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.DELIMITED,
            customData = DelimData(node.leftDelim, node.rightDelim, innerBox)
        )
    }

    private fun measureAbs(node: AbsNode, scale: Float): MathLayoutBox {
        val innerBox = measureNode(node.inner, scale)
        val barWidth = 4f * scale
        val totalW = innerBox.width + (barWidth * 2f) + (6f * scale)
        val totalH = innerBox.height + (4f * scale)
        val baseline = innerBox.baseline + (2f * scale)

        innerBox.x = barWidth + (3f * scale)
        innerBox.y = 2f * scale

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(innerBox),
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.ABS,
            customData = DelimData(DelimType.BAR, DelimType.BAR, innerBox)
        )
    }

    private fun measureFunction(node: FunctionNode, scale: Float): MathLayoutBox {
        val rawName = node.name.lowercase(java.util.Locale.US)
        val (baseName, autoPower) = when (rawName) {
            "asin", "arcsin" -> Pair("sin", NumberNode("-1"))
            "acos", "arccos" -> Pair("cos", NumberNode("-1"))
            "atan", "arctan" -> Pair("tan", NumberNode("-1"))
            "acot", "arccot" -> Pair("cot", NumberNode("-1"))
            "asec", "arcsec" -> Pair("sec", NumberNode("-1"))
            "acsc", "arccsc" -> Pair("csc", NumberNode("-1"))
            "acosec", "arccosec" -> Pair("cosec", NumberNode("-1"))
            else -> Pair(node.name, node.power)
        }

        val funcNameBox = measureText(baseName, node, scale, isItalic = false, isBold = false)
        var nameUnit = funcNameBox
        if (node.base != null) {
            val baseBox = measureNode(node.base, scale * 0.7f)
            nameUnit = layoutSubscriptBox(nameUnit, baseBox, node, scale)
        }
        val effectivePower = autoPower ?: node.power
        if (effectivePower != null) {
            val powBox = measureNode(effectivePower, scale * 0.7f)
            nameUnit = layoutPowerBox(nameUnit, powBox, node, scale)
        }

        val argBox = if (node.argument is DelimitedNode) {
            measureNode(node.argument, scale)
        } else {
            // Auto wrap in scaling parentheses
            measureDelimited(DelimitedNode(node.argument), scale)
        }

        return alignRow(listOf(nameUnit, argBox), node, scale)
    }

    private fun measureDerivative(node: DerivativeNode, scale: Float): MathLayoutBox {
        val d = if (node.isPartial) "∂" else "d"
        val numStr = if (node.order > 1) "$d^${node.order}${node.functionName ?: ""}" else "$d${node.functionName ?: ""}"
        val denStr = if (node.order > 1) "$d${node.variable}^${node.order}" else "$d${node.variable}"

        val numNode = UniversalMathParser.parse(numStr)
        val denNode = UniversalMathParser.parse(denStr)
        val fracBox = measureFraction(FractionNode(numNode, denNode, isDerivativeStyle = true), scale)

        if (node.expr != null) {
            val exprBox = measureDelimited(DelimitedNode(node.expr), scale)
            return alignRow(listOf(fracBox, exprBox), node, scale)
        }
        return fracBox
    }

    private fun measureIntegral(node: IntegralNode, scale: Float): MathLayoutBox {
        val intSymBox = measureText("∫", node, scale * 1.35f, isItalic = false)
        val lowerBox = node.lowerLimit?.let { measureNode(it, scale * 0.65f) }
        val upperBox = node.upperLimit?.let { measureNode(it, scale * 0.65f) }

        // Limits positioned at upper and lower right of integral symbol
        val limitsW = max(lowerBox?.width ?: 0f, upperBox?.width ?: 0f)
        val opW = intSymBox.width + limitsW + (2f * scale)
        val totalAscent = max(intSymBox.ascent, (upperBox?.height ?: 0f) + (intSymBox.ascent * 0.5f))
        val totalDescent = max(intSymBox.descent, (lowerBox?.height ?: 0f) + (intSymBox.descent * 0.5f))
        val totalH = totalAscent + totalDescent
        val baseline = totalAscent

        intSymBox.x = 0f
        intSymBox.y = baseline - intSymBox.baseline

        val children = mutableListOf(intSymBox)
        if (upperBox != null) {
            upperBox.x = intSymBox.width
            upperBox.y = 0f
            children.add(upperBox)
        }
        if (lowerBox != null) {
            lowerBox.x = intSymBox.width - (2f * scale)
            lowerBox.y = totalH - lowerBox.height
            children.add(lowerBox)
        }

        val opCompoundBox = MathLayoutBox(
            width = opW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = children,
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.ROW
        )

        val integrandBox = measureNode(node.integrand, scale)
        val dVarBox = measureText(" d${node.variable}", node, scale, isItalic = false)

        return alignRow(listOf(opCompoundBox, integrandBox, dVarBox), node, scale)
    }

    private fun measureLimit(node: LimitNode, scale: Float): MathLayoutBox {
        val limBox = measureText("lim", node, scale, isItalic = false, isBold = true)
        val subText = "${node.variable} → "
        val subLeft = measureText(subText, node, scale * 0.65f)
        val targetBox = measureNode(node.target, scale * 0.65f)
        val subRow = alignRow(listOf(subLeft, targetBox), node, scale * 0.65f)

        val colW = max(limBox.width, subRow.width)
        val vGap = 2f * scale
        val totalH = limBox.height + vGap + subRow.height
        val baseline = limBox.baseline

        limBox.x = (colW - limBox.width) / 2f
        limBox.y = 0f

        subRow.x = (colW - subRow.width) / 2f
        subRow.y = limBox.height + vGap

        val limitHeaderBox = MathLayoutBox(
            width = colW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = listOf(limBox, subRow),
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.LIMIT
        )

        val exprBox = measureNode(node.expr, scale)
        return alignRow(listOf(limitHeaderBox, exprBox), node, scale)
    }

    private fun measureBigOp(node: BigOperatorNode, scale: Float): MathLayoutBox {
        val opBox = measureText(node.type.symbol, node, scale * 1.3f, isItalic = false, isBold = true)
        val lowerBox = node.lower?.let { measureNode(it, scale * 0.65f) }
        val upperBox = node.upper?.let { measureNode(it, scale * 0.65f) }

        val colW = maxOf(opBox.width, lowerBox?.width ?: 0f, upperBox?.width ?: 0f)
        val vGap = 2f * scale
        val upperH = upperBox?.height ?: 0f
        val lowerH = lowerBox?.height ?: 0f
        val totalH = upperH + vGap + opBox.height + vGap + lowerH
        val baseline = upperH + vGap + opBox.baseline

        val opChildren = mutableListOf<MathLayoutBox>()
        if (upperBox != null) {
            upperBox.x = (colW - upperBox.width) / 2f
            upperBox.y = 0f
            opChildren.add(upperBox)
        }

        opBox.x = (colW - opBox.width) / 2f
        opBox.y = upperH + vGap
        opChildren.add(opBox)

        if (lowerBox != null) {
            lowerBox.x = (colW - lowerBox.width) / 2f
            lowerBox.y = upperH + vGap + opBox.height + vGap
            opChildren.add(lowerBox)
        }

        val opComposite = MathLayoutBox(
            width = colW + (4f * scale),
            height = totalH,
            baseline = baseline,
            node = node,
            children = opChildren,
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.BIG_OP
        )

        val bodyBox = measureNode(node.body, scale)
        return alignRow(listOf(opComposite, bodyBox), node, scale)
    }

    private fun measureMatrix(node: MatrixNode, scale: Float): MathLayoutBox {
        val cellScale = (scale * 0.85f).coerceAtLeast(0.6f)
        val cellBoxes = node.cells.map { row ->
            row.map { measureNode(it, cellScale) }
        }

        val colCount = node.cols
        val rowCount = node.rows

        val colWidths = MutableList(colCount) { 0f }
        val rowHeights = MutableList(rowCount) { 0f }

        for (r in 0 until rowCount) {
            for (c in 0 until colCount) {
                val b = cellBoxes[r][c]
                colWidths[c] = max(colWidths[c], b.width)
                rowHeights[r] = max(rowHeights[r], b.height)
            }
        }

        val hSpacing = 12f * scale
        val vSpacing = 8f * scale
        val delimWidth = 8f * scale

        var curY = vSpacing
        for (r in 0 until rowCount) {
            var curX = delimWidth + hSpacing
            for (c in 0 until colCount) {
                val b = cellBoxes[r][c]
                // Center cell inside its grid slot
                b.x = curX + ((colWidths[c] - b.width) / 2f)
                b.y = curY + ((rowHeights[r] - b.height) / 2f)
                curX += colWidths[c] + hSpacing
            }
            curY += rowHeights[r] + vSpacing
        }

        val innerW = colWidths.sum() + (hSpacing * (colCount + 1))
        val totalW = innerW + (delimWidth * 2f)
        val totalH = curY
        val baseline = totalH / 2f + (4f * scale)

        val allFlatCells = cellBoxes.flatten()

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = allFlatCells,
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.MATRIX,
            customData = MatrixData(rowCount, colCount, node.isDeterminant, colWidths, rowHeights, cellBoxes)
        )
    }

    private fun measureVector(node: VectorNode, scale: Float): MathLayoutBox {
        if (node.components != null) {
            val colMat = MatrixNode(
                rows = node.components.size,
                cols = 1,
                cells = node.components.map { listOf(it) }
            )
            return measureMatrix(colMat, scale)
        }
        val name = node.name ?: "v"
        return measureVariable(VariableNode(name = name, isVector = true), scale)
    }

    private fun measureComplex(node: ComplexNode, scale: Float): MathLayoutBox {
        val realBox = measureNode(node.real, scale)
        val signBox = measureOperator(OperatorNode(node.sign), scale)
        val imagBox = measureNode(node.imag, scale)
        val iBox = measureText("i", node, scale, isItalic = true)
        return alignRow(listOf(realBox, signBox, imagBox, iBox), node, scale)
    }

    private fun measureChem(node: ChemNode, scale: Float): MathLayoutBox {
        val children = mutableListOf<MathLayoutBox>()
        for (p in node.parts) {
            when (p) {
                is ChemPart.Element -> {
                    val elemBox = measureText(p.symbol, node, scale, isItalic = false)
                    val subBox = p.count?.let { measureText(it.toString(), node, scale * 0.7f, isItalic = false) }
                    val supBox = p.charge?.let { measureText(it, node, scale * 0.7f, isItalic = false) }
                    if (subBox != null || supBox != null) {
                        val scriptBox = layoutPowerSubscriptBox(elemBox, subBox, supBox, node, scale)
                        children.add(scriptBox)
                    } else {
                        children.add(elemBox)
                    }
                }
                is ChemPart.Group -> {
                    val groupInner = measureChem(ChemNode(p.parts), scale)
                    val parenBox = measureDelimited(DelimitedNode(ChemNode(p.parts)), scale)
                    val subBox = p.count?.let { measureText(it.toString(), node, scale * 0.7f, isItalic = false) }
                    val supBox = p.charge?.let { measureText(it, node, scale * 0.7f, isItalic = false) }
                    if (subBox != null || supBox != null) {
                        children.add(layoutPowerSubscriptBox(parenBox, subBox, supBox, node, scale))
                    } else {
                        children.add(parenBox)
                    }
                }
            }
        }
        return alignRow(children, node, scale)
    }

    private fun layoutPowerSubscriptBox(
        baseBox: MathLayoutBox,
        subBox: MathLayoutBox?,
        supBox: MathLayoutBox?,
        node: MathNode,
        scale: Float
    ): MathLayoutBox {
        val scriptW = max(subBox?.width ?: 0f, supBox?.width ?: 0f)
        val totalW = baseBox.width + scriptW
        val totalAscent = max(baseBox.ascent, (supBox?.height ?: 0f) + (baseBox.ascent * 0.4f))
        val totalDescent = max(baseBox.descent, (subBox?.height ?: 0f) + (baseBox.descent * 0.4f))
        val totalH = totalAscent + totalDescent
        val baseline = totalAscent

        baseBox.x = 0f
        baseBox.y = baseline - baseBox.baseline

        val children = mutableListOf(baseBox)
        if (supBox != null) {
            supBox.x = baseBox.width
            supBox.y = 0f
            children.add(supBox)
        }
        if (subBox != null) {
            subBox.x = baseBox.width
            subBox.y = totalH - subBox.height
            children.add(subBox)
        }

        return MathLayoutBox(
            width = totalW,
            height = totalH,
            baseline = baseline,
            node = node,
            children = children,
            fontSizeSp = baseBox.fontSizeSp,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }

    private fun measureUnit(node: UnitNode, scale: Float): MathLayoutBox {
        val valBox = measureNode(node.value, scale)
        val unitBox = measureText(" " + node.unitText, node, scale * 0.9f, isItalic = false)
        return alignRow(listOf(valBox, unitBox), node, scale)
    }

    private fun measureRow(nodes: List<MathNode>, parentNode: MathNode, scale: Float): MathLayoutBox {
        val children = nodes.map { measureNode(it, scale) }
        return alignRow(children, parentNode, scale)
    }

    /**
     * CRITICAL: Aligns children along a single common mathematical baseline.
     */
    private fun alignRow(children: List<MathLayoutBox>, parentNode: MathNode, scale: Float): MathLayoutBox {
        if (children.isEmpty()) return measureText("", parentNode, scale)
        if (children.size == 1) return children[0]

        var maxAscent = 0f
        var maxDescent = 0f

        for (child in children) {
            maxAscent = max(maxAscent, child.ascent)
            maxDescent = max(maxDescent, child.descent)
        }

        val totalH = maxAscent + maxDescent
        val baseline = maxAscent

        var curX = 0f
        for (child in children) {
            child.x = curX
            // Align child baseline to row baseline:
            child.y = baseline - child.baseline
            curX += child.width
        }

        return MathLayoutBox(
            width = curX,
            height = totalH,
            baseline = baseline,
            node = parentNode,
            children = children,
            fontSizeSp = baseFontSizeSp * scale,
            fontScale = scale,
            style = BoxStyle.ROW
        )
    }
}
