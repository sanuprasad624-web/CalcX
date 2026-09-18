package com.example.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CalcKeypad(
    onKeyInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    isScientificExpanded: Boolean,
    onToggleScientific: () -> Unit,
    hapticEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    val view = LocalView.current
    var isInvMode by remember { mutableStateOf(false) }

    fun performHaptic() {
        if (hapticEnabled) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Memory & Scientific Toggle Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MemoryButton(text = "MC", onClick = { performHaptic(); onMemoryClear() }, modifier = Modifier.weight(1f))
            MemoryButton(text = "MR", onClick = { performHaptic(); onMemoryRecall() }, modifier = Modifier.weight(1f))
            MemoryButton(text = "M+", onClick = { performHaptic(); onMemoryAdd() }, modifier = Modifier.weight(1f))
            MemoryButton(text = "M-", onClick = { performHaptic(); onMemorySubtract() }, modifier = Modifier.weight(1f))
            MemoryButton(text = "MS", onClick = { performHaptic(); onMemoryStore() }, modifier = Modifier.weight(1f))

            // Expand Scientific Toggle
            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { performHaptic(); onToggleScientific() }
                    .testTag("toggle_scientific_button"),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SCI",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Icon(
                        imageVector = if (isScientificExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand scientific keys",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Expandable Scientific Keypad
        AnimatedVisibility(
            visible = isScientificExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Sci Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SciButton(
                        text = if (isInvMode) "INV" else "INV",
                        isHighlighted = isInvMode,
                        onClick = { performHaptic(); isInvMode = !isInvMode },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = if (isInvMode) "sin⁻¹" else "sin",
                        onClick = { performHaptic(); onKeyInput(if (isInvMode) "sin⁻¹(" else "sin(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = if (isInvMode) "cos⁻¹" else "cos",
                        onClick = { performHaptic(); onKeyInput(if (isInvMode) "cos⁻¹(" else "cos(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = if (isInvMode) "tan⁻¹" else "tan",
                        onClick = { performHaptic(); onKeyInput(if (isInvMode) "tan⁻¹(" else "tan(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "π",
                        onClick = { performHaptic(); onKeyInput("π") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sci Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SciButton(
                        text = if (isInvMode) "eˣ" else "ln",
                        onClick = { performHaptic(); onKeyInput(if (isInvMode) "exp(" else "ln(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = if (isInvMode) "10ˣ" else "log",
                        onClick = { performHaptic(); onKeyInput(if (isInvMode) "10^(" else "log(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "√",
                        onClick = { performHaptic(); onKeyInput("sqrt(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "xʸ",
                        onClick = { performHaptic(); onKeyInput("^") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "e",
                        onClick = { performHaptic(); onKeyInput("e") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Sci Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SciButton(
                        text = "(",
                        onClick = { performHaptic(); onKeyInput("(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = ")",
                        onClick = { performHaptic(); onKeyInput(")") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "x!",
                        onClick = { performHaptic(); onKeyInput("!") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "1/x",
                        onClick = { performHaptic(); onKeyInput("1/(") },
                        modifier = Modifier.weight(1f)
                    )
                    SciButton(
                        text = "%",
                        onClick = { performHaptic(); onKeyInput("%") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Standard Primary Numeric Keypad
        // Row 1: C, ( ), %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KeypadButton(
                text = "C",
                textColor = MaterialTheme.colorScheme.error,
                backgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                onClick = { performHaptic(); onClear() },
                modifier = Modifier.weight(1f),
                tag = "btn_clear"
            )
            KeypadButton(
                text = "±",
                textColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { performHaptic(); onToggleSign() },
                modifier = Modifier.weight(1f),
                tag = "btn_sign"
            )
            KeypadButton(
                text = "%",
                textColor = MaterialTheme.colorScheme.primary,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { performHaptic(); onKeyInput("%") },
                modifier = Modifier.weight(1f),
                tag = "btn_percent"
            )
            KeypadButton(
                text = "÷",
                textColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = { performHaptic(); onKeyInput("÷") },
                modifier = Modifier.weight(1f),
                tag = "btn_divide"
            )
        }

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NumberButton(text = "7", onClick = { performHaptic(); onKeyInput("7") }, modifier = Modifier.weight(1f))
            NumberButton(text = "8", onClick = { performHaptic(); onKeyInput("8") }, modifier = Modifier.weight(1f))
            NumberButton(text = "9", onClick = { performHaptic(); onKeyInput("9") }, modifier = Modifier.weight(1f))
            KeypadButton(
                text = "×",
                textColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = { performHaptic(); onKeyInput("×") },
                modifier = Modifier.weight(1f),
                tag = "btn_multiply"
            )
        }

        // Row 3: 4, 5, 6, −
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NumberButton(text = "4", onClick = { performHaptic(); onKeyInput("4") }, modifier = Modifier.weight(1f))
            NumberButton(text = "5", onClick = { performHaptic(); onKeyInput("5") }, modifier = Modifier.weight(1f))
            NumberButton(text = "6", onClick = { performHaptic(); onKeyInput("6") }, modifier = Modifier.weight(1f))
            KeypadButton(
                text = "−",
                textColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = { performHaptic(); onKeyInput("-") },
                modifier = Modifier.weight(1f),
                tag = "btn_minus"
            )
        }

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NumberButton(text = "1", onClick = { performHaptic(); onKeyInput("1") }, modifier = Modifier.weight(1f))
            NumberButton(text = "2", onClick = { performHaptic(); onKeyInput("2") }, modifier = Modifier.weight(1f))
            NumberButton(text = "3", onClick = { performHaptic(); onKeyInput("3") }, modifier = Modifier.weight(1f))
            KeypadButton(
                text = "+",
                textColor = MaterialTheme.colorScheme.onPrimary,
                backgroundColor = MaterialTheme.colorScheme.primary,
                onClick = { performHaptic(); onKeyInput("+") },
                modifier = Modifier.weight(1f),
                tag = "btn_plus"
            )
        }

        // Row 5: 0, ., ⌫, =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            NumberButton(text = "0", onClick = { performHaptic(); onKeyInput("0") }, modifier = Modifier.weight(1f))
            KeypadButton(
                text = ".",
                textColor = MaterialTheme.colorScheme.onSurface,
                backgroundColor = MaterialTheme.colorScheme.surface,
                onClick = { performHaptic(); onKeyInput(".") },
                modifier = Modifier.weight(1f),
                tag = "btn_decimal"
            )
            KeypadButtonWithIcon(
                icon = Icons.AutoMirrored.Filled.Backspace,
                textColor = MaterialTheme.colorScheme.error,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = { performHaptic(); onBackspace() },
                modifier = Modifier.weight(1f),
                tag = "btn_backspace"
            )
            KeypadButton(
                text = "=",
                textColor = MaterialTheme.colorScheme.onTertiary,
                backgroundColor = MaterialTheme.colorScheme.tertiary,
                onClick = { performHaptic(); onEquals() },
                modifier = Modifier.weight(1f),
                tag = "btn_equals"
            )
        }
    }
}

@Composable
private fun NumberButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeypadButton(
        text = text,
        textColor = MaterialTheme.colorScheme.onSurface,
        backgroundColor = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        modifier = modifier,
        tag = "btn_$text"
    )
}

@Composable
private fun SciButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("sci_btn_$text"),
        color = if (isHighlighted) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = if (isHighlighted) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MemoryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(34.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .testTag("mem_btn_$text"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp
                ),
                color = textColor
            )
        }
    }
}

@Composable
private fun KeypadButtonWithIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    textColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tag: String
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = "Backspace",
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
