package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.RegressionResult
import com.example.engine.StatisticsEngine
import com.example.engine.StatisticsSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics & Data Analyzer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("stats_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("statistics_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Descriptive Stats") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Linear Regression") })
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> DescriptiveStatsView()
                    1 -> LinearRegressionView()
                }
            }
        }
    }
}

@Composable
private fun DescriptiveStatsView() {
    var dataInput by remember { mutableStateOf("12, 15, 14, 10, 18, 20, 15, 22, 17, 19") }
    var summary by remember { mutableStateOf<StatisticsSummary?>(null) }

    LaunchedEffect(Unit) {
        val list = parseNumbers(dataInput)
        summary = StatisticsEngine.compute(list)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Single-Variable Summary Statistics", fontWeight = FontWeight.Bold)
            Text("Enter numbers separated by commas or spaces:", style = MaterialTheme.typography.bodySmall)
        }

        item {
            OutlinedTextField(
                value = dataInput,
                onValueChange = {
                    dataInput = it
                    val list = parseNumbers(it)
                    summary = StatisticsEngine.compute(list)
                },
                modifier = Modifier.fillMaxWidth().testTag("stats_data_input"),
                minLines = 2
            )
        }

        summary?.let { s ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Central Tendency & Dispersion:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        StatRow("Count (n)", s.count.toString())
                        StatRow("Sum (Σx)", "%.4f".format(s.sum))
                        StatRow("Mean (μ / x̄)", "%.4f".format(s.mean))
                        StatRow("Median", "%.4f".format(s.median))
                        StatRow("Mode", if (s.mode.isNotEmpty()) s.mode.joinToString(", ") { "%.2f".format(it) } else "None")
                        Divider()
                        StatRow("Range", "%.4f (Min: %.2f, Max: %.2f)".format(s.range, s.min, s.max))
                        StatRow("Sample Std Dev (s)", "%.4f".format(s.sampleStdDev))
                        StatRow("Population Std Dev (σ)", "%.4f".format(s.populationStdDev))
                        StatRow("Sample Variance (s²)", "%.4f".format(s.sampleVariance))
                        Divider()
                        StatRow("Quartile 1 (Q1)", "%.4f".format(s.q1))
                        StatRow("Quartile 3 (Q3)", "%.4f".format(s.q3))
                        StatRow("Interquartile Range (IQR)", "%.4f".format(s.iqr))
                    }
                }
            }
        }
    }
}

@Composable
private fun LinearRegressionView() {
    var xInput by remember { mutableStateOf("1, 2, 3, 4, 5, 6") }
    var yInput by remember { mutableStateOf("2.2, 3.9, 6.1, 7.8, 10.2, 12.1") }
    var regResult by remember { mutableStateOf<RegressionResult?>(null) }

    LaunchedEffect(Unit) {
        val xs = parseNumbers(xInput)
        val ys = parseNumbers(yInput)
        regResult = StatisticsEngine.computeLinearRegression(xs, ys)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Two-Variable Linear Regression", fontWeight = FontWeight.Bold)
            Text("Fit least-squares line y = mx + c to determine slope, intercept, and correlation coefficient r.", style = MaterialTheme.typography.bodySmall)
        }

        item {
            OutlinedTextField(
                value = xInput,
                onValueChange = {
                    xInput = it
                    regResult = StatisticsEngine.computeLinearRegression(parseNumbers(it), parseNumbers(yInput))
                },
                label = { Text("X Values (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = yInput,
                onValueChange = {
                    yInput = it
                    regResult = StatisticsEngine.computeLinearRegression(parseNumbers(xInput), parseNumbers(it))
                },
                label = { Text("Y Values (comma separated)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        regResult?.let { reg ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Regression Fit Line:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(reg.equation, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Divider()
                        StatRow("Slope (m)", "%.6f".format(reg.slope))
                        StatRow("Intercept (c)", "%.6f".format(reg.intercept))
                        StatRow("Pearson Correlation (r)", "%.4f".format(reg.r))
                        StatRow("Coeff of Determination (R²)", "%.4f".format(reg.rSquared))
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

private fun parseNumbers(input: String): List<Double> {
    return input.split(",", " ", "\n", "\t")
        .mapNotNull { it.trim().toDoubleOrNull() }
}
