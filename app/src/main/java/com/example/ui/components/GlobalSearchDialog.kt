package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.engine.ConstantsLibrary
import com.example.engine.FormulaLibrary

data class SearchResultEntry(
    val title: String,
    val subtitle: String,
    val category: String,
    val action: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchDialog(
    onDismiss: () -> Unit,
    onNavigateToTool: (String) -> Unit,
    onInsertText: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val results = remember(query) {
        val q = query.trim().lowercase()
        val list = mutableListOf<SearchResultEntry>()

        val toolList = listOf(
            "JEE Toolkit" to "jee",
            "Physics Solver" to "physics",
            "Chemistry Tools" to "chemistry",
            "Unit Converter" to "units",
            "Matrix Calculator" to "matrix",
            "Programmer Calculator" to "programmer",
            "Statistics & Regression" to "stats",
            "Finance & EMI" to "finance",
            "Date & Time" to "datetime",
            "Physical Constants" to "constants",
            "Formula Library" to "formulas",
            "Calculation Notebook" to "notebook"
        )

        toolList.forEach { (name, id) ->
            if (q.isEmpty() || name.lowercase().contains(q)) {
                list.add(
                    SearchResultEntry(
                        title = name,
                        subtitle = "Open calculator tool",
                        category = "TOOL",
                        action = { onNavigateToTool(id); onDismiss() }
                    )
                )
            }
        }

        if (q.isNotEmpty()) {
            FormulaLibrary.search(q).take(5).forEach { f ->
                list.add(
                    SearchResultEntry(
                        title = f.name,
                        subtitle = f.formulaDisplay,
                        category = "FORMULA",
                        action = { onNavigateToTool("formulas"); onDismiss() }
                    )
                )
            }

            ConstantsLibrary.search(q).take(5).forEach { c ->
                list.add(
                    SearchResultEntry(
                        title = "${c.name} (${c.symbol})",
                        subtitle = "${c.formattedString} - Tap to insert into calculator",
                        category = "CONSTANT",
                        action = { onInsertText(c.value.toString()); onDismiss() }
                    )
                )
            }
        }

        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.80f)
                .testTag("global_search_dialog"),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quick Navigation & Search", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    placeholder = { Text("Search tools, formulas, constants...") },
                    modifier = Modifier.fillMaxWidth().testTag("global_search_input")
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(results) { item ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { item.action() },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.title, fontWeight = FontWeight.Bold)
                                    Text(item.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                    Text(
                                        text = item.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
