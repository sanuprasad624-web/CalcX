package com.example.ui.components.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.graph.GraphFunction

@Composable
fun DesmosExpressionRow(
    index: Int,
    function: GraphFunction,
    isFocused: Boolean,
    cursorPos: Int,
    onRowClick: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
    onUpdateFunction: (GraphFunction) -> Unit,
    modifier: Modifier = Modifier
) {
    var showColorMenu by remember { mutableStateOf(false) }
    var showCalculusOptions by remember { mutableStateOf(false) }

    val activeBlue = Color(0xFF2563EB)
    val unfocusedStrip = Color(0xFFCBD5E1)
    val borderColor = if (isFocused) activeBlue.copy(alpha = 0.5f) else Color(0xFFE2E8F0)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onRowClick() }
            .testTag("expression_row_$index"),
        color = Color.White,
        tonalElevation = if (isFocused) 2.dp else 0.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .border(width = 0.5.dp, color = borderColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Row Index strip (Blue when focused, Gray when not)
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .fillMaxHeight()
                        .background(if (isFocused) activeBlue else unfocusedStrip),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$index",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // 2. Circular Colored Badge with Sine Wave icon (matching Screenshot 3)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (function.isVisible) function.color else Color.Transparent
                        )
                        .border(
                            width = 2.dp,
                            color = function.color,
                            shape = CircleShape
                        )
                        .clickable { onToggleVisibility() }
                        .testTag("row_color_badge_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    // Draw smooth wave ~ inside badge
                    Canvas(modifier = Modifier.size(20.dp)) {
                        val w = size.width
                        val h = size.height
                        val path = Path().apply {
                            moveTo(2f, h * 0.5f)
                            cubicTo(
                                w * 0.25f, h * 0.15f,
                                w * 0.35f, h * 0.15f,
                                w * 0.5f, h * 0.5f
                            )
                            cubicTo(
                                w * 0.65f, h * 0.85f,
                                w * 0.75f, h * 0.85f,
                                w - 2f, h * 0.5f
                            )
                        }
                        drawPath(
                            path = path,
                            color = if (function.isVisible) Color.White else function.color,
                            style = Stroke(width = 2.2.dp.toPx())
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // 3. Mathematical Expression Display (Desmos styled format)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (function.expressionText.isEmpty()) {
                        if (isFocused) {
                            // Blinking cursor in empty row
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(22.dp)
                                        .background(activeBlue)
                                )
                            }
                        } else {
                            Text(
                                text = "Type an equation, e.g. x² + y² = 5",
                                color = Color(0xFF94A3B8),
                                fontSize = 15.sp,
                                fontStyle = FontStyle.Italic
                            )
                        }
                    } else {
                        // Render styled math string with cursor
                        val formattedMath = formatMathStringWithCursor(
                            raw = function.expressionText,
                            isFocused = isFocused,
                            cursorPos = cursorPos.coerceIn(0, function.expressionText.length)
                        )
                        Text(
                            text = formattedMath,
                            fontSize = 17.sp,
                            color = Color(0xFF0F172A),
                            lineHeight = 22.sp
                        )
                    }
                }

                // 4. Error or Calculus Overlay Menu Indicator
                if (function.error != null) {
                    IconButton(
                        onClick = { /* show error */ },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = function.error,
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = { showCalculusOptions = !showCalculusOptions },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 5. Delete / Clear '✕' Button (matching Screenshot 1 & 3)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_row_$index")
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear or delete row",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))
            }

            // --- CALCULUS / CURVE OPTIONS EXPANDABLE PANEL ---
            AnimatedVisibility(visible = showCalculusOptions) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF8FAFC)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Color:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                            GraphFunction.PALETTE.forEach { color ->
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .border(
                                            width = if (function.color == color) 2.dp else 0.dp,
                                            color = Color.Black,
                                            shape = CircleShape
                                        )
                                        .clickable {
                                            onUpdateFunction(function.copy(color = color))
                                        }
                                )
                            }
                        }

                        if (!function.isImplicit) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = function.showDerivative,
                                        onCheckedChange = { onUpdateFunction(function.copy(showDerivative = it)) }
                                    )
                                    Text("d/dx f'(x)", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = function.showTangent,
                                        onCheckedChange = { onUpdateFunction(function.copy(showTangent = it)) }
                                    )
                                    Text("Tangent", fontSize = 12.sp)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = function.showIntegralArea,
                                        onCheckedChange = { onUpdateFunction(function.copy(showIntegralArea = it)) }
                                    )
                                    Text("∫ Area", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Transforms raw string like "x^2 + y^2 = 5" into formatted mathematical AnnotatedString
 * with superscripts, italic variable glyphs, and insertion cursor.
 */
fun formatMathStringWithCursor(
    raw: String,
    isFocused: Boolean,
    cursorPos: Int
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < raw.length) {
            if (isFocused && i == cursorPos) {
                pushStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Black))
                append("|")
                pop()
            }

            val ch = raw[i]
            if (ch == '^') {
                // Superscript next character or parenthesized group
                i++
                if (i < raw.length && raw[i] == '{') {
                    i++
                    val start = i
                    while (i < raw.length && raw[i] != '}') i++
                    val supText = raw.substring(start, i)
                    pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    append(supText)
                    pop()
                    if (i < raw.length && raw[i] == '}') i++
                } else if (i < raw.length) {
                    pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 12.sp, fontWeight = FontWeight.Bold))
                    append(raw[i])
                    pop()
                    i++
                }
            } else if (ch == '*' || ch == '·') {
                append(" · ")
                i++
            } else if (ch == '=') {
                append(" = ")
                i++
            } else if (ch == '+') {
                append(" + ")
                i++
            } else if (ch == '-') {
                append(" − ")
                i++
            } else if (ch == 'x' || ch == 'y' || ch == 'a' || ch == 'b' || ch == 't') {
                pushStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.Medium))
                append(ch)
                pop()
                i++
            } else {
                append(ch)
                i++
            }
        }

        if (isFocused && cursorPos >= raw.length) {
            pushStyle(SpanStyle(color = Color(0xFF2563EB), fontWeight = FontWeight.Black))
            append("|")
            pop()
        }
    }
}
