package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.CalcKeypad
import com.example.ui.components.DisplaySection
import com.example.ui.viewmodel.CalculatorUiState

enum class CalcScreenDestination {
    CALCULATOR,
    GRAPH,
    MATHEMATICS,
    CALCULUS,
    JEE_TOOLKIT,
    PHYSICS,
    CHEMISTRY,
    ORGANIC_CHEM,
    UNIT_CONVERTER,
    MATRIX,
    PROGRAMMER,
    STATISTICS,
    FINANCE,
    DATETIME,
    FORMULAS,
    CONSTANTS,
    NOTEBOOK,
    HISTORY,
    SETTINGS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onKeyInput: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onToggleSign: () -> Unit,
    onEquals: () -> Unit,
    onToggleAngleMode: () -> Unit,
    onToggleExactMode: () -> Unit,
    onToggleScientific: () -> Unit,
    onMemoryClear: () -> Unit,
    onMemoryRecall: () -> Unit,
    onMemoryAdd: () -> Unit,
    onMemorySubtract: () -> Unit,
    onMemoryStore: () -> Unit,
    onNavigate: (CalcScreenDestination) -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showSaveDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("CALCX", fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleLarge)
                        if (uiState.jeeModeEnabled) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    "JEE MODE",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigate(CalcScreenDestination.HISTORY) },
                        modifier = Modifier.testTag("topbar_history_btn")
                    ) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(
                        onClick = { onNavigate(CalcScreenDestination.NOTEBOOK) },
                        modifier = Modifier.testTag("topbar_notebook_btn")
                    ) {
                        Icon(Icons.Default.EditNote, contentDescription = "Notebook")
                    }
                    IconButton(
                        onClick = { onNavigate(CalcScreenDestination.SETTINGS) },
                        modifier = Modifier.testTag("topbar_settings_btn")
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        modifier = modifier.testTag("calculator_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Quick Tool Nav Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("quick_tools_bar")
            ) {
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.GRAPH) },
                        label = { Text("2D Graph") },
                        leadingIcon = { Icon(Icons.Default.AutoGraph, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("chip_graph")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.CALCULUS) },
                        label = { Text("Calculus") },
                        leadingIcon = { Icon(Icons.Default.Functions, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        modifier = Modifier.testTag("chip_calculus")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.JEE_TOOLKIT) },
                        label = { Text("JEE & Eng") },
                        leadingIcon = { Icon(Icons.Default.Engineering, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_jee")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.MATHEMATICS) },
                        label = { Text("Mathematics") },
                        leadingIcon = { Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f),
                            labelColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        modifier = Modifier.testTag("chip_mathematics")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.PHYSICS) },
                        label = { Text("Physics") },
                        leadingIcon = { Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_physics")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.CHEMISTRY) },
                        label = { Text("Chemistry") },
                        leadingIcon = { Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_chemistry")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.ORGANIC_CHEM) },
                        label = { Text("Organic Chem") },
                        leadingIcon = { Icon(Icons.Default.Biotech, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_organic_chemistry")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.UNIT_CONVERTER) },
                        label = { Text("Converter") },
                        leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_converter")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.MATRIX) },
                        label = { Text("Matrices") },
                        leadingIcon = { Icon(Icons.Default.GridOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_matrix")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.PROGRAMMER) },
                        label = { Text("Programmer") },
                        leadingIcon = { Icon(Icons.Default.Terminal, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_programmer")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.STATISTICS) },
                        label = { Text("Statistics") },
                        leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_stats")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.FINANCE) },
                        label = { Text("Finance & EMI") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_finance")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.DATETIME) },
                        label = { Text("Date & Time") },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_datetime")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.FORMULAS) },
                        label = { Text("Formulas") },
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_formulas")
                    )
                }
                item {
                    AssistChip(
                        onClick = { onNavigate(CalcScreenDestination.CONSTANTS) },
                        label = { Text("Constants") },
                        leadingIcon = { Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("chip_constants")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Display Section
            DisplaySection(
                expression = uiState.expression,
                resultPreview = uiState.resultPreview,
                exactResult = uiState.exactResult,
                isExactMode = uiState.isExactMode,
                angleMode = uiState.angleMode,
                errorMessage = uiState.errorMessage,
                onToggleAngleMode = onToggleAngleMode,
                onToggleExactMode = onToggleExactMode,
                onSaveToNotebook = { showSaveDialog = true },
                previousCalculation = uiState.previousCalculation,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Scientific & Numeric Keypad
            CalcKeypad(
                onKeyInput = onKeyInput,
                onBackspace = onBackspace,
                onClear = onClear,
                onToggleSign = onToggleSign,
                onEquals = onEquals,
                onMemoryClear = onMemoryClear,
                onMemoryRecall = onMemoryRecall,
                onMemoryAdd = onMemoryAdd,
                onMemorySubtract = onMemorySubtract,
                onMemoryStore = onMemoryStore,
                isScientificExpanded = uiState.isScientificExpanded,
                onToggleScientific = onToggleScientific,
                hapticEnabled = uiState.hapticEnabled,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (showSaveDialog) {
        var title by remember { mutableStateOf(if (uiState.jeeModeEnabled) "JEE Calculation" else "Calculation Note") }
        var notes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save to Calculation Notebook") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Expression: ${uiState.expression}", fontWeight = FontWeight.SemiBold)
                    Text("Result: = ${uiState.resultPreview}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Note / Observations (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSaveToNotebook(title, uiState.expression, uiState.resultPreview)
                    showSaveDialog = false
                    Toast.makeText(context, "Saved to Notebook", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
