package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.CanonicalMathResult
import com.example.math.calculus.Expr
import com.example.math.renderer.*
import java.util.Locale

/**
 * Primary Mathematical View for CALCX powered by high-performance native Compose Canvas.
 * Converts input, AST, or formula strings into properly rendered mathematical notation.
 * Zero raw LaTeX syntax or programming characters (_ or ^) shown to the user.
 * Runs 100% natively in Jetpack Compose with zero WebView/MESA GPU overhead.
 */
@Composable
fun MathView(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    debug: Boolean = false,
    scrollable: Boolean = true,
    isDisplayMode: Boolean = true
) {
    val node = remember(latex) {
        try {
            UniversalMathParser.parse(latex)
        } catch (e: Exception) {
            TextNode(ReadableMathFallback.toReadableUnicode(latex))
        }
    }

    val accessibleText = remember(latex) {
        ReadableMathFallback.toReadableUnicode(latex)
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = accessibleText }
            .testTag("math_view"),
        contentAlignment = Alignment.CenterStart
    ) {
        NativeMathCanvas(
            node = node,
            fontSizeSp = fontSize.value,
            color = color,
            debug = debug,
            scrollable = scrollable
        )
    }
}

/**
 * Centralized MathRenderer component requested by user specification.
 * Acts as the centralized pipeline to render any mathematical equation, solution step, or formula.
 */
@Composable
fun MathRenderer(
    formula: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    scrollable: Boolean = true
) {
    MathView(
        latex = formula,
        modifier = modifier,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        scrollable = scrollable
    )
}

/**
 * Centralized renderMath Composable function.
 */
@Composable
fun renderMath(
    formula: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    MathView(
        latex = formula,
        modifier = modifier,
        fontSize = fontSize,
        color = color
    )
}

/**
 * Overload for rendering Canonical MathNode AST directly via native Canvas.
 */
@Composable
fun MathView(
    node: MathNode,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    debug: Boolean = false,
    scrollable: Boolean = true,
    isDisplayMode: Boolean = true
) {
    val accessibleText = remember(node) {
        ReadableMathFallback.toReadableUnicode(node.toLatex())
    }

    Box(
        modifier = modifier
            .semantics { contentDescription = accessibleText }
            .testTag("math_view"),
        contentAlignment = Alignment.CenterStart
    ) {
        NativeMathCanvas(
            node = node,
            fontSizeSp = fontSize.value,
            color = color,
            debug = debug,
            scrollable = scrollable
        )
    }
}

/**
 * Overload for rendering calculus AST (Expr) directly with native Canvas.
 */
@Composable
fun MathView(
    expr: Expr,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    debug: Boolean = false,
    scrollable: Boolean = true,
    isDisplayMode: Boolean = true
) {
    val node = remember(expr) {
        UniversalMathParser.fromExpr(expr)
    }
    MathView(
        node = node,
        modifier = modifier,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        debug = debug,
        scrollable = scrollable,
        isDisplayMode = isDisplayMode
    )
}

/**
 * Overload for rendering CanonicalMathResult directly with native Canvas.
 */
@Composable
fun MathView(
    result: CanonicalMathResult,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.Normal,
    debug: Boolean = false,
    scrollable: Boolean = true,
    isDisplayMode: Boolean = true
) {
    val rawLatex = if (result.latex.isNotBlank()) {
        result.latex
    } else {
        result.primaryDisplay
    }
    val fullLatex = if (result.unit != null && !rawLatex.endsWith(result.unit)) {
        "$rawLatex \\text{ ${result.unit}}"
    } else {
        rawLatex
    }
    MathView(
        latex = fullLatex,
        modifier = modifier,
        fontSize = fontSize,
        color = color,
        fontWeight = fontWeight,
        debug = debug,
        scrollable = scrollable,
        isDisplayMode = isDisplayMode
    )
}

/**
 * Pure Jetpack Compose Native Math Canvas implementation.
 * Renders mathematical expressions using vector graphics (Canvas DrawScope).
 * Instant 60/120 FPS performance, baseline alignment, zero GPU rendernode requirements.
 */
@Composable
private fun NativeMathCanvas(
    node: MathNode,
    fontSizeSp: Float,
    color: Color,
    debug: Boolean = false,
    scrollable: Boolean = true
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current

    val layoutBox = remember(node, fontSizeSp, textMeasurer) {
        try {
            val engine = MathLayoutEngine(textMeasurer, baseFontSizeSp = fontSizeSp)
            engine.layout(node)
        } catch (e: Exception) {
            val fallbackNode = TextNode(ReadableMathFallback.toReadableUnicode(node.toLatex()))
            MathLayoutEngine(textMeasurer, baseFontSizeSp = fontSizeSp).layout(fallbackNode)
        }
    }

    val widthDp = with(density) { (layoutBox.width + 8f).toDp() }
    val heightDp = with(density) { (layoutBox.height + 8f).toDp().coerceAtLeast((fontSizeSp * 1.3f).dp) }

    val scrollState = rememberScrollState()
    val scrollModifier = if (scrollable) Modifier.horizontalScroll(scrollState) else Modifier

    Box(
        modifier = Modifier
            .then(scrollModifier)
            .padding(horizontal = 2.dp, vertical = 2.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .width(widthDp)
                .height(heightDp)
        ) {
            try {
                val renderer = MathCanvasRenderer(textMeasurer)
                renderer.render(
                    drawScope = this,
                    box = layoutBox,
                    originX = 4f,
                    originY = 4f,
                    color = color,
                    debugMode = debug
                )
            } catch (_: Exception) {}
        }
    }
}

/**
 * Standardized High-Fidelity Math Result Card.
 * Adheres to the full solution rendering pipeline:
 * INPUT → FORMULA → SUBSTITUTION → SIMPLIFICATION → FINAL ANSWER
 * Each mathematical step rendered using KaTeX.
 */
@Composable
fun MathResultCard(
    result: CanonicalMathResult,
    title: String,
    calculationText: String = "",
    formulaLatex: String = "",
    substitutionLatex: String = "",
    simplificationLatex: String = "",
    onSaveToNotebook: ((title: String, calc: String, res: String) -> Unit)? = null,
    modifier: Modifier = Modifier,
    showStepsInitially: Boolean = true
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(showStepsInitially) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("math_result_card"),
        colors = CardDefaults.cardColors(
            containerColor = if (result.isError)
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (result.isError) Icons.Default.Warning else Icons.Default.Functions,
                        contentDescription = null,
                        tint = if (result.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (result.isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Actions: Copy & Share
                Row {
                    IconButton(
                        onClick = {
                            val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val copyText = if (result.latex.isNotEmpty()) result.latex else result.primaryDisplay
                            clip.setPrimaryClip(ClipData.newPlainText("CALCX Result", copyText))
                            Toast.makeText(context, "Copied result to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ContentCopy,
                            contentDescription = "Copy Result",
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "$title: ${result.primaryDisplay}")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Math Result"))
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share Result",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Calculation Context / Input (if available)
            if (calculationText.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Input: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        MathView(
                            latex = calculationText,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Optional explicit Formula Line
            if (formulaLatex.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Formula: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        MathView(
                            latex = formulaLatex,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Optional explicit Substitution Line
            if (substitutionLatex.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Substituted: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        MathView(
                            latex = substitutionLatex,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Optional explicit Simplification Line
            if (simplificationLatex.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simplified: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        MathView(
                            latex = simplificationLatex,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // PRIMARY FINAL ANSWER DISPLAY (KaTeX Rendered)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                    .padding(14.dp)
            ) {
                if (result.isError) {
                    Text(
                        text = result.errorMessage ?: "Calculation Error",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Column {
                        // Render exact / primary mathematical form using KaTeX Engine
                        MathView(
                            result = result,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Secondary Decimal Form (if different from primary)
                        val sec = result.secondaryDisplay
                        if (sec != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = sec,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Action Row: Save to Notebook & Toggle Steps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onSaveToNotebook != null && !result.isError) {
                    FilledTonalButton(
                        onClick = {
                            val resString = if (result.exactString != null) "${result.exactString} ≈ ${result.decimalString ?: ""}" else result.primaryDisplay
                            onSaveToNotebook(title, calculationText, resString)
                            Toast.makeText(context, "Saved to Notebook!", Toast.LENGTH_SHORT).show()
                        },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Note", style = MaterialTheme.typography.labelMedium)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                if (result.steps.isNotEmpty()) {
                    TextButton(
                        onClick = { isExpanded = !isExpanded },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isExpanded) "Hide Steps" else "Show Steps (${result.steps.size})",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Step-by-Step Breakdown Accordion
            AnimatedVisibility(
                visible = isExpanded && result.steps.isNotEmpty(),
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Step-by-Step Solution",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    result.steps.forEachIndexed { index, step ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            MathView(
                                latex = step,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
