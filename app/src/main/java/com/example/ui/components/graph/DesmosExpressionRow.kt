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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.graph.GraphFunction
import com.example.ui.components.MathQuillController
import com.example.ui.components.MathQuillField
import com.example.ui.components.MathView

@Composable
fun DesmosExpressionRow(
    index: Int,
    function: GraphFunction,
    isFocused: Boolean,
    controller: MathQuillController? = null,
    onRowClick: () -> Unit,
    onToggleVisibility: () -> Unit,
    onDelete: () -> Unit,
    onUpdateLatex: (String) -> Unit,
    onUpdateFunction: (GraphFunction) -> Unit,
    onEnter: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
                    .heightIn(min = 56.dp)
                    .border(width = 0.5.dp, color = borderColor),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Row Index strip (Blue when focused, Gray when not)
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(56.dp)
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

                // 2. Circular Colored Badge with Sine Wave icon
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

                Spacer(modifier = Modifier.width(10.dp))

                // 3. Mathematical Expression Input (Live MathQuill MathField)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MathQuillField(
                        latex = function.expressionText,
                        onLatexChange = { newLatex ->
                            onUpdateLatex(newLatex)
                        },
                        isFocused = isFocused,
                        controller = controller,
                        onEnter = onEnter,
                        onFocused = onRowClick,
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    )
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

                // 5. Delete / Clear '✕' Button
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
