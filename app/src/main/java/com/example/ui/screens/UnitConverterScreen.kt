package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.UnitConversionEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val categories = UnitConversionEngine.categories
    var selectedCategoryIndex by remember { mutableStateOf(0) }
    val currentCategory = categories[selectedCategoryIndex]

    var inputValueStr by remember { mutableStateOf("1.0") }
    var fromUnit by remember(selectedCategoryIndex) { mutableStateOf(currentCategory.units[0]) }
    var toUnit by remember(selectedCategoryIndex) {
        mutableStateOf(if (currentCategory.units.size > 1) currentCategory.units[1] else currentCategory.units[0])
    }

    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    val inputVal = inputValueStr.toDoubleOrNull() ?: 0.0
    val convertedVal = UnitConversionEngine.convert(inputVal, fromUnit, toUnit)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("unit_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("unit_converter_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedCategoryIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEachIndexed { index, cat ->
                    Tab(
                        selected = selectedCategoryIndex == index,
                        onClick = {
                            selectedCategoryIndex = index
                            fromUnit = cat.units[0]
                            toUnit = if (cat.units.size > 1) cat.units[1] else cat.units[0]
                        },
                        text = { Text(cat.name, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = inputValueStr,
                                onValueChange = { inputValueStr = it },
                                label = { Text("Value to Convert") },
                                modifier = Modifier.fillMaxWidth().testTag("input_unit_value")
                            )

                            // From unit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("From:", fontWeight = FontWeight.SemiBold)
                                Box {
                                    FilledTonalButton(onClick = { fromExpanded = true }) {
                                        Text("${fromUnit.name} (${fromUnit.symbol})")
                                    }
                                    DropdownMenu(
                                        expanded = fromExpanded,
                                        onDismissRequest = { fromExpanded = false }
                                    ) {
                                        currentCategory.units.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text("${unit.name} (${unit.symbol})") },
                                                onClick = {
                                                    fromUnit = unit
                                                    fromExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            // Swap
                            IconButton(
                                onClick = {
                                    val temp = fromUnit
                                    fromUnit = toUnit
                                    toUnit = temp
                                },
                                modifier = Modifier.align(Alignment.CenterHorizontally).testTag("btn_swap_units")
                            ) {
                                Icon(Icons.Default.SwapVert, contentDescription = "Swap")
                            }

                            // To unit
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("To:", fontWeight = FontWeight.SemiBold)
                                Box {
                                    FilledTonalButton(onClick = { toExpanded = true }) {
                                        Text("${toUnit.name} (${toUnit.symbol})")
                                    }
                                    DropdownMenu(
                                        expanded = toExpanded,
                                        onDismissRequest = { toExpanded = false }
                                    ) {
                                        currentCategory.units.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text("${unit.name} (${unit.symbol})") },
                                                onClick = {
                                                    toUnit = unit
                                                    toExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Divider()

                            // Converted Output
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${"%.6g".format(convertedVal)} ${toUnit.symbol}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$inputValueStr ${fromUnit.symbol} = ${"%.6g".format(convertedVal)} ${toUnit.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = {
                                    val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clip.setPrimaryClip(ClipData.newPlainText("Unit Conversion", "$inputValueStr ${fromUnit.symbol} = ${"%.6g".format(convertedVal)} ${toUnit.symbol}"))
                                    Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                IconButton(onClick = {
                                    onSaveToNotebook(
                                        "${currentCategory.name} Conversion",
                                        "$inputValueStr ${fromUnit.symbol} -> ${toUnit.symbol}",
                                        "${"%.6g".format(convertedVal)} ${toUnit.symbol}"
                                    )
                                }) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Notebook")
                                }
                            }
                        }
                    }
                }

                // All units in category
                item {
                    Text(
                        "All Units in ${currentCategory.name}:",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                items(currentCategory.units) { unit ->
                    val allVal = UnitConversionEngine.convert(inputVal, fromUnit, unit)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(unit.name, fontWeight = FontWeight.Medium)
                            Text(
                                "${"%.6g".format(allVal)} ${unit.symbol}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
