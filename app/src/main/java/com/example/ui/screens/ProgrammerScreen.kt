package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.BitWordSize
import com.example.engine.ProgrammerEngine

enum class NumberBase(val radix: Int, val label: String) {
    HEX(16, "HEX"),
    DEC(10, "DEC"),
    OCT(8, "OCT"),
    BIN(2, "BIN")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgrammerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var activeBase by remember { mutableStateOf(NumberBase.DEC) }
    var wordSize by remember { mutableStateOf(BitWordSize.DWORD) }
    var currentValue by remember { mutableStateOf(0L) }
    var pendingOp by remember { mutableStateOf<String?>(null) }
    var operandA by remember { mutableStateOf(0L) }
    var inputBuffer by remember { mutableStateOf("0") }

    val formattedBases = remember(currentValue, wordSize) {
        ProgrammerEngine.formatAllBases(currentValue, wordSize)
    }

    fun applyKey(char: Char) {
        val newBuf = if (inputBuffer == "0") char.toString() else inputBuffer + char
        val parsed = try {
            java.lang.Long.parseUnsignedLong(newBuf, activeBase.radix) and wordSize.mask
        } catch (_: Exception) {
            currentValue
        }
        inputBuffer = newBuf
        currentValue = parsed
    }

    fun backspace() {
        if (inputBuffer.length > 1) {
            inputBuffer = inputBuffer.dropLast(1)
            currentValue = java.lang.Long.parseUnsignedLong(inputBuffer, activeBase.radix) and wordSize.mask
        } else {
            inputBuffer = "0"
            currentValue = 0L
        }
    }

    fun clear() {
        inputBuffer = "0"
        currentValue = 0L
        pendingOp = null
        operandA = 0L
    }

    fun executeBitwise(op: String) {
        when (op) {
            "NOT" -> {
                currentValue = (currentValue.inv()) and wordSize.mask
                inputBuffer = java.lang.Long.toString(currentValue, activeBase.radix).uppercase()
            }
            "SHL" -> {
                currentValue = (currentValue shl 1) and wordSize.mask
                inputBuffer = java.lang.Long.toString(currentValue, activeBase.radix).uppercase()
            }
            "SHR" -> {
                currentValue = (currentValue ushr 1) and wordSize.mask
                inputBuffer = java.lang.Long.toString(currentValue, activeBase.radix).uppercase()
            }
            else -> {
                operandA = currentValue
                pendingOp = op
                inputBuffer = "0"
            }
        }
    }

    fun calculateEquals() {
        val op = pendingOp ?: return
        val b = currentValue
        val res = when (op) {
            "AND" -> operandA and b
            "OR" -> operandA or b
            "XOR" -> operandA xor b
            else -> b
        } and wordSize.mask

        currentValue = res
        inputBuffer = java.lang.Long.toString(res, activeBase.radix).uppercase()
        pendingOp = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Programmer Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("programmer_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("programmer_screen")
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Word size selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Word Size:", fontWeight = FontWeight.SemiBold)
                Row {
                    BitWordSize.values().forEach { ws ->
                        FilterChip(
                            selected = wordSize == ws,
                            onClick = { wordSize = ws; currentValue = currentValue and ws.mask },
                            label = { Text(ws.name) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }

            // Live Synchronized Base Representations
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    NumberBase.values().forEach { base ->
                        val isSelected = activeBase == base
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    activeBase = base
                                    inputBuffer = java.lang.Long.toString(currentValue, base.radix).uppercase()
                                }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = base.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedBases[base.label] ?: "0",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Bitwise Operation buttons: AND, OR, XOR, NOT, <<, >>
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilledTonalButton(onClick = { executeBitwise("AND") }, modifier = Modifier.weight(1f)) { Text("AND") }
                FilledTonalButton(onClick = { executeBitwise("OR") }, modifier = Modifier.weight(1f)) { Text("OR") }
                FilledTonalButton(onClick = { executeBitwise("XOR") }, modifier = Modifier.weight(1f)) { Text("XOR") }
                FilledTonalButton(onClick = { executeBitwise("NOT") }, modifier = Modifier.weight(1f)) { Text("NOT") }
                FilledTonalButton(onClick = { executeBitwise("SHL") }, modifier = Modifier.weight(1f)) { Text("<<") }
                FilledTonalButton(onClick = { executeBitwise("SHR") }, modifier = Modifier.weight(1f)) { Text(">>") }
            }

            // Hex/Dec Keypad Grid
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Row 1: A, B, C, D, E, F
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf('A', 'B', 'C', 'D', 'E', 'F').forEach { char ->
                        Button(
                            onClick = { applyKey(char) },
                            enabled = activeBase == NumberBase.HEX,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(char.toString())
                        }
                    }
                }

                // Row 2: 7, 8, 9, C
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { applyKey('7') }, enabled = activeBase.radix > 7, modifier = Modifier.weight(1f)) { Text("7") }
                    Button(onClick = { applyKey('8') }, enabled = activeBase.radix > 8, modifier = Modifier.weight(1f)) { Text("8") }
                    Button(onClick = { applyKey('9') }, enabled = activeBase.radix > 9, modifier = Modifier.weight(1f)) { Text("9") }
                    Button(
                        onClick = { clear() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.weight(1f)
                    ) { Text("CLR") }
                }

                // Row 3: 4, 5, 6, ⌫
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { applyKey('4') }, enabled = activeBase.radix > 4, modifier = Modifier.weight(1f)) { Text("4") }
                    Button(onClick = { applyKey('5') }, enabled = activeBase.radix > 5, modifier = Modifier.weight(1f)) { Text("5") }
                    Button(onClick = { applyKey('6') }, enabled = activeBase.radix > 6, modifier = Modifier.weight(1f)) { Text("6") }
                    FilledTonalButton(onClick = { backspace() }, modifier = Modifier.weight(1f)) { Text("⌫") }
                }

                // Row 4: 1, 2, 3, =
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { applyKey('1') }, enabled = activeBase.radix > 1, modifier = Modifier.weight(1f)) { Text("1") }
                    Button(onClick = { applyKey('2') }, enabled = activeBase.radix > 2, modifier = Modifier.weight(1f)) { Text("2") }
                    Button(onClick = { applyKey('3') }, enabled = activeBase.radix > 3, modifier = Modifier.weight(1f)) { Text("3") }
                    Button(
                        onClick = { calculateEquals() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                        modifier = Modifier.weight(1f)
                    ) { Text("=") }
                }

                // Row 5: 0
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = { applyKey('0') }, modifier = Modifier.fillMaxWidth()) { Text("0") }
                }
            }
        }
    }
}
