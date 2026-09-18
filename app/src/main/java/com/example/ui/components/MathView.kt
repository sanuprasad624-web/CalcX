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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.CanonicalMathResult

/**
 * Native Compose Mathematical Expression Renderer.
 * Renders LaTeX-formatted mathematical syntax (fractions, roots, superscripts, subscripts, symbols)
 * directly in Jetpack Compose with pristine layout and guaranteed safe fallback.
 */
@Composable
fun MathView(
    latex: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 22.sp,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    // Normalization & Symbol Mapping
    val normalized = remember(latex) {
        normalizeLatexForDisplay(latex)
    }

    // Check if expression contains a top-level fraction to render stacked
    val fractionMatch = remember(latex) {
        parseTopLevelFraction(latex)
    }

    if (fractionMatch != null) {
        // Render stacked fraction
        Row(
            modifier = modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (fractionMatch.prefix.isNotEmpty()) {
                Text(
                    text = normalizeLatexForDisplay(fractionMatch.prefix),
                    fontSize = fontSize,
                    color = color,
                    fontWeight = fontWeight
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Numerator
                Text(
                    text = normalizeLatexForDisplay(fractionMatch.numerator),
                    fontSize = (fontSize.value * 0.85f).sp,
                    color = color,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
                // Fraction bar
                HorizontalDivider(
                    modifier = Modifier
                        .padding(vertical = 2.dp)
                        .widthIn(min = 24.dp),
                    thickness = 1.5.dp,
                    color = color
                )
                // Denominator
                Text(
                    text = normalizeLatexForDisplay(fractionMatch.denominator),
                    fontSize = (fontSize.value * 0.85f).sp,
                    color = color,
                    fontWeight = fontWeight,
                    textAlign = TextAlign.Center
                )
            }

            if (fractionMatch.suffix.isNotEmpty()) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = normalizeLatexForDisplay(fractionMatch.suffix),
                    fontSize = fontSize,
                    color = color,
                    fontWeight = fontWeight
                )
            }
        }
    } else {
        // Render formatted text with superscripts/subscripts
        val annotated = remember(normalized) {
            buildFormattedMathString(normalized)
        }
        Text(
            text = annotated,
            modifier = modifier.horizontalScroll(rememberScrollState()),
            fontSize = fontSize,
            color = color,
            fontWeight = fontWeight,
            fontFamily = FontFamily.Default
        )
    }
}

/**
 * Single Canonical Math Result Card.
 * Complies with ZERO DUPLICATE RESULTS mandate.
 * Renders exactly ONE authoritative result with optional steps and action buttons.
 */
@Composable
fun MathResultCard(
    result: CanonicalMathResult,
    title: String = "Result",
    onSaveToNotebook: ((title: String, calc: String, res: String) -> Unit)? = null,
    calculationText: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showRawLatex by remember { mutableStateOf(false) }
    var showSteps by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("math_result_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Title & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // LaTeX View Toggle Button
                    IconButton(
                        onClick = { showRawLatex = !showRawLatex },
                        modifier = Modifier.size(32.dp).testTag("toggle_latex_view")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = "Toggle LaTeX source",
                            tint = if (showRawLatex) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Copy Button
                    IconButton(
                        onClick = {
                            val textToCopy = if (showRawLatex) result.latex else result.primaryDisplay
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("CALCX Result", textToCopy))
                            Toast.makeText(context, "Copied $textToCopy", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(32.dp).testTag("copy_math_result")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Share Button
                    IconButton(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "${result.primaryDisplay}\n(Calculated with CALCX)")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Result"))
                        },
                        modifier = Modifier.size(32.dp).testTag("share_math_result")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share result",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Save to Notebook Button
                    if (onSaveToNotebook != null) {
                        IconButton(
                            onClick = {
                                onSaveToNotebook(title, calculationText, result.primaryDisplay)
                                Toast.makeText(context, "Saved to Notebook", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(32.dp).testTag("save_math_notebook")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.BookmarkAdd,
                                contentDescription = "Save to notebook",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // PRIMARY RESULT: ONE single canonical rendered mathematical output
            if (result.isError) {
                Text(
                    text = result.errorMessage ?: "Calculation Error",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Medium
                )
            } else {
                if (showRawLatex) {
                    // Raw LaTeX source display (only when explicitly requested)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = result.latex,
                            modifier = Modifier.padding(10.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    // Clean Rendered Math Output
                    MathView(
                        latex = result.latex,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth().testTag("primary_math_result")
                    )
                }

                // Secondary Representation (Only shown when explicitly different, e.g. Exact mode showing ≈ decimal)
                result.secondaryDisplay?.let { sec ->
                    Text(
                        text = sec,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.testTag("secondary_math_result")
                    )
                }
            }

            // Steps accordion (if available)
            if (result.steps.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showSteps = !showSteps }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Derivation Steps (${result.steps.size})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (showSteps) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                AnimatedVisibility(
                    visible = showSteps,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        result.steps.forEachIndexed { index, step ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                Text(
                                    text = step,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Data class to parse top-level fraction
private data class FractionParts(
    val prefix: String,
    val numerator: String,
    val denominator: String,
    val suffix: String
)

private fun parseTopLevelFraction(latex: String): FractionParts? {
    val fracIdx = latex.indexOf("\\frac{")
    if (fracIdx == -1) return null

    val prefix = latex.substring(0, fracIdx).trim()
    var idx = fracIdx + 6
    var depth = 1
    val numStart = idx
    while (idx < latex.length && depth > 0) {
        if (latex[idx] == '{') depth++
        else if (latex[idx] == '}') depth--
        idx++
    }
    if (depth != 0) return null
    val numerator = latex.substring(numStart, idx - 1)

    // Denominator
    if (idx >= latex.length || latex[idx] != '{') return null
    idx++
    val denStart = idx
    depth = 1
    while (idx < latex.length && depth > 0) {
        if (latex[idx] == '{') depth++
        else if (latex[idx] == '}') depth--
        idx++
    }
    if (depth != 0) return null
    val denominator = latex.substring(denStart, idx - 1)
    val suffix = latex.substring(idx).trim()

    return FractionParts(prefix, numerator, denominator, suffix)
}

/**
 * Normalizes LaTeX tags into clean mathematical Unicode representations
 */
private fun normalizeLatexForDisplay(input: String): String {
    var s = input
        .replace("\\cdot", "·")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\pm", "±")
        .replace("\\mp", "∓")
        .replace("\\leq", "≤")
        .replace("\\le", "≤")
        .replace("\\geq", "≥")
        .replace("\\ge", "≥")
        .replace("\\neq", "≠")
        .replace("\\ne", "≠")
        .replace("\\approx", "≈")
        .replace("\\infty", "∞")
        .replace("\\pi", "π")
        .replace("\\theta", "θ")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\gamma", "γ")
        .replace("\\lambda", "λ")
        .replace("\\mu", "μ")
        .replace("\\sigma", "σ")
        .replace("\\omega", "ω")
        .replace("\\Delta", "Δ")
        .replace("\\int", "∫")
        .replace("\\partial", "∂")
        .replace("\\sum", "∑")
        .replace("\\sqrt{", "√(")
        .replace("\\left(", "(")
        .replace("\\right)", ")")
        .replace("\\left[", "[")
        .replace("\\right]", "]")
        .replace("\\left|", "|")
        .replace("\\right|", "|")
        .replace("\\,", " ")
        .replace("\\ ", " ")

    // Clean up \text{...}
    val textRegex = Regex("\\\\text\\{([^}]*)\\}")
    s = textRegex.replace(s) { it.groupValues[1] }

    // Clean remaining unclosed braces from replaced \sqrt{
    return s
}

/**
 * Builds an AnnotatedString with superscripts and subscripts properly styled
 */
private fun buildFormattedMathString(text: String): androidx.compose.ui.text.AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val ch = text[i]
            if (ch == '^' && i + 1 < text.length) {
                i++
                if (text[i] == '{') {
                    i++
                    val start = i
                    while (i < text.length && text[i] != '}') i++
                    val superText = text.substring(start, i)
                    withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 14.sp)) {
                        append(superText)
                    }
                    if (i < text.length && text[i] == '}') i++
                } else if (text[i] == '(') {
                    i++
                    val start = i
                    while (i < text.length && text[i] != ')') i++
                    val superText = text.substring(start, i)
                    withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 14.sp)) {
                        append(superText)
                    }
                    if (i < text.length && text[i] == ')') i++
                } else {
                    withStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 14.sp)) {
                        append(text[i].toString())
                    }
                    i++
                }
            } else if (ch == '_' && i + 1 < text.length) {
                i++
                if (text[i] == '{') {
                    i++
                    val start = i
                    while (i < text.length && text[i] != '}') i++
                    val subText = text.substring(start, i)
                    withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 14.sp)) {
                        append(subText)
                    }
                    if (i < text.length && text[i] == '}') i++
                } else {
                    withStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 14.sp)) {
                        append(text[i].toString())
                    }
                    i++
                }
            } else {
                append(ch)
                i++
            }
        }
    }
}
