package com.example.ui.components.graph

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class KeyboardMode {
    MATH,
    ABC
}

@Composable
fun DesmosKeyboard(
    onInsertText: (String) -> Unit,
    onBackspace: () -> Unit,
    onMoveCursorLeft: () -> Unit,
    onMoveCursorRight: () -> Unit,
    onEnter: () -> Unit,
    onAddExpression: () -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onHideKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mode by remember { mutableStateOf(KeyboardMode.MATH) }
    var isShifted by remember { mutableStateOf(false) }
    var showFuncsSheet by remember { mutableStateOf(false) }

    // Colors matching Desmos web/mobile keyboard
    val keyboardBg = Color(0xFFF1F3F5)
    val standardKeyBg = Color(0xFFFFFFFF)
    val numberKeyBg = Color(0xFFE2E6EA)
    val actionKeyBg = Color(0xFFD0D5DD)
    val primaryEnterBg = Color(0xFF2563EB)
    val textColor = Color(0xFF1E293B)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(keyboardBg)
            .border(width = 0.5.dp, color = Color(0xFFCBD5E1))
            .testTag("desmos_custom_keyboard")
    ) {
        // --- TOP TOOLBAR (Add +, Undo ↶, Redo ↷, Settings, Hide ⌄) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onAddExpression,
                    modifier = Modifier.size(34.dp).testTag("kb_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add item", tint = textColor)
                }
                IconButton(
                    onClick = onUndo,
                    modifier = Modifier.size(34.dp).testTag("kb_undo_btn")
                ) {
                    Icon(Icons.Default.Undo, contentDescription = "Undo", tint = textColor)
                }
                IconButton(
                    onClick = onRedo,
                    modifier = Modifier.size(34.dp).testTag("kb_redo_btn")
                ) {
                    Icon(Icons.Default.Redo, contentDescription = "Redo", tint = textColor)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { showFuncsSheet = true },
                    modifier = Modifier.size(34.dp).testTag("kb_settings_btn")
                ) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings / Functions", tint = textColor)
                }
                IconButton(
                    onClick = onHideKeyboard,
                    modifier = Modifier.size(34.dp).testTag("kb_hide_btn")
                ) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Hide Keyboard", tint = textColor)
                }
            }
        }

        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCBD5E1))

        // --- KEYBOARD BODY ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (mode == KeyboardMode.MATH) {
                // ROW 1: x | y | a² | a^b | 7 | 8 | 9 | ÷ | funcs
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MathKey(label = "x", isItalic = true, bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("x") }
                    MathKey(label = "y", isItalic = true, bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("y") }
                    MathKey(label = "a²", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("^2") }
                    MathKey(label = "aᵇ", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("^") }
                    MathKey(label = "7", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("7") }
                    MathKey(label = "8", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("8") }
                    MathKey(label = "9", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("9") }
                    MathKey(label = "÷", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("/") }
                    MathKey(label = "funcs", bg = actionKeyBg, fontSize = 11.sp, modifier = Modifier.weight(1.3f)) { showFuncsSheet = true }
                }

                // ROW 2: ( | ) | < | > | 4 | 5 | 6 | × | ← | →
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MathKey(label = "(", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("(") }
                    MathKey(label = ")", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText(")") }
                    MathKey(label = "<", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("<") }
                    MathKey(label = ">", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText(">") }
                    MathKey(label = "4", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("4") }
                    MathKey(label = "5", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("5") }
                    MathKey(label = "6", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("6") }
                    MathKey(label = "×", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("*") }
                    MathIconKey(icon = Icons.AutoMirrored.Filled.ArrowBack, bg = actionKeyBg, modifier = Modifier.weight(0.65f)) { onMoveCursorLeft() }
                    MathIconKey(icon = Icons.AutoMirrored.Filled.ArrowForward, bg = actionKeyBg, modifier = Modifier.weight(0.65f)) { onMoveCursorRight() }
                }

                // ROW 3: |a| | , | ≤ | ≥ | 1 | 2 | 3 | − | ⌫
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MathKey(label = "|a|", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("abs(") }
                    MathKey(label = ",", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText(",") }
                    MathKey(label = "≤", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("<=") }
                    MathKey(label = "≥", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText(">=") }
                    MathKey(label = "1", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("1") }
                    MathKey(label = "2", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("2") }
                    MathKey(label = "3", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("3") }
                    MathKey(label = "−", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("-") }
                    MathIconKey(icon = Icons.AutoMirrored.Filled.Backspace, bg = actionKeyBg, modifier = Modifier.weight(1.3f)) { onBackspace() }
                }

                // ROW 4: ABC | 🔊 | √ | π | 0 | . | = | + | ↵
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    MathKey(label = "ABC", bg = actionKeyBg, fontSize = 12.sp, modifier = Modifier.weight(1.1f)) { mode = KeyboardMode.ABC }
                    MathIconKey(icon = Icons.Default.VolumeUp, bg = actionKeyBg, modifier = Modifier.weight(0.9f)) { /* audio trace */ }
                    MathKey(label = "√", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("sqrt(") }
                    MathKey(label = "π", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("pi") }
                    MathKey(label = "0", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText("0") }
                    MathKey(label = ".", bg = numberKeyBg, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)) { onInsertText(".") }
                    MathKey(label = "=", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("=") }
                    MathKey(label = "+", bg = standardKeyBg, modifier = Modifier.weight(1f)) { onInsertText("+") }
                    MathIconKey(icon = Icons.Default.KeyboardReturn, bg = primaryEnterBg, tint = Color.White, modifier = Modifier.weight(1.3f)) { onEnter() }
                }
            } else {
                // ABC MODE (Matches Screenshot 2)
                // ROW 1: q w e r t y u i o p
                val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    row1.forEach { ch ->
                        val letter = if (isShifted) ch.uppercase() else ch
                        MathKey(label = letter, isItalic = true, bg = standardKeyBg, modifier = Modifier.weight(1f)) {
                            onInsertText(letter)
                        }
                    }
                }

                // ROW 2: a s d f g h j k l θ
                val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "θ")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    row2.forEach { ch ->
                        val letter = if (isShifted && ch != "θ") ch.uppercase() else ch
                        MathKey(label = letter, isItalic = true, bg = standardKeyBg, modifier = Modifier.weight(1f)) {
                            onInsertText(letter)
                        }
                    }
                }

                // ROW 3: ⇧ | z x c v b n m | ⌫
                val row3 = listOf("z", "x", "c", "v", "b", "n", "m")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    MathIconKey(
                        icon = if (isShifted) Icons.Default.KeyboardCapslock else Icons.Default.North,
                        bg = if (isShifted) primaryEnterBg else actionKeyBg,
                        tint = if (isShifted) Color.White else textColor,
                        modifier = Modifier.weight(1.2f)
                    ) { isShifted = !isShifted }

                    row3.forEach { ch ->
                        val letter = if (isShifted) ch.uppercase() else ch
                        MathKey(label = letter, isItalic = true, bg = standardKeyBg, modifier = Modifier.weight(1f)) {
                            onInsertText(letter)
                        }
                    }

                    MathIconKey(icon = Icons.AutoMirrored.Filled.Backspace, bg = actionKeyBg, modifier = Modifier.weight(1.3f)) { onBackspace() }
                }

                // ROW 4: 1 2 3 | a_b | ! % | [ ] | { } | ~ : | ' | ↵
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    MathKey(label = "1 2 3", bg = actionKeyBg, fontSize = 12.sp, modifier = Modifier.weight(1.3f)) { mode = KeyboardMode.MATH }
                    MathKey(label = "a_b", bg = standardKeyBg, fontSize = 12.sp, modifier = Modifier.weight(1f)) { onInsertText("_") }
                    MathKey(label = "! %", bg = standardKeyBg, fontSize = 11.sp, modifier = Modifier.weight(1f)) { onInsertText("!") }
                    MathKey(label = "[ ]", bg = standardKeyBg, fontSize = 11.sp, modifier = Modifier.weight(1f)) { onInsertText("[]") }
                    MathKey(label = "{ }", bg = standardKeyBg, fontSize = 11.sp, modifier = Modifier.weight(1f)) { onInsertText("{}") }
                    MathKey(label = "~ :", bg = standardKeyBg, fontSize = 11.sp, modifier = Modifier.weight(1f)) { onInsertText("~") }
                    MathKey(label = "'", bg = standardKeyBg, modifier = Modifier.weight(0.8f)) { onInsertText("'") }
                    MathIconKey(icon = Icons.Default.KeyboardReturn, bg = primaryEnterBg, tint = Color.White, modifier = Modifier.weight(1.3f)) { onEnter() }
                }
            }
        }
    }

    // --- FUNCTIONS POPUP DIALOG ---
    if (showFuncsSheet) {
        AlertDialog(
            onDismissRequest = { showFuncsSheet = false },
            title = { Text("Functions (funcs)", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Trigonometry", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("sin", "cos", "tan", "csc", "sec", "cot").forEach { fn ->
                            OutlinedButton(
                                onClick = { onInsertText("$fn("); showFuncsSheet = false },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(fn, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Inverse & Hyperbolic", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("arcsin", "arccos", "arctan", "sinh", "cosh", "tanh").forEach { fn ->
                            OutlinedButton(
                                onClick = { onInsertText("$fn("); showFuncsSheet = false },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(fn, fontSize = 10.sp)
                            }
                        }
                    }

                    Text("Logarithms & Calculus", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ln", "log", "exp", "sqrt", "abs").forEach { fn ->
                            OutlinedButton(
                                onClick = { onInsertText("$fn("); showFuncsSheet = false },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(fn, fontSize = 11.sp)
                            }
                        }
                    }

                    Text("Statistics & Rounding", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("round", "floor", "ceil", "sign").forEach { fn ->
                            OutlinedButton(
                                onClick = { onInsertText("$fn("); showFuncsSheet = false },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                modifier = Modifier.weight(1f).height(36.dp)
                            ) {
                                Text(fn, fontSize = 11.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFuncsSheet = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
private fun MathKey(
    label: String,
    modifier: Modifier = Modifier,
    bg: Color = Color.White,
    textColor: Color = Color(0xFF1E293B),
    fontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
    fontWeight: FontWeight = FontWeight.Medium,
    isItalic: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("key_$label"),
        shape = RoundedCornerShape(6.dp),
        color = bg,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD1D5DB))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label,
                color = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal
            )
        }
    }
}

@Composable
private fun MathIconKey(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    bg: Color = Color.White,
    tint: Color = Color(0xFF1E293B),
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag("key_icon_${icon.name}"),
        shape = RoundedCornerShape(6.dp),
        color = bg,
        shadowElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFD1D5DB))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
