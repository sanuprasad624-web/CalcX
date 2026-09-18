package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.CanonicalMathResult
import com.example.math.calculus.CalculusEngine
import com.example.ui.components.MathResultCard

enum class CalculusTab {
    DIFFERENTIATION,
    INDEFINITE_INTEGRATION,
    DEFINITE_INTEGRATION
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculusScreen(
    onBack: () -> Unit,
    onOpenInGrapher: (expression: String) -> Unit,
    onSaveToNotebook: ((title: String, calc: String, res: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(CalculusTab.DIFFERENTIATION) }

    // Differentiation State
    var diffExpr by remember { mutableStateOf("x^3 - 3*x^2 + 2") }
    var diffVar by remember { mutableStateOf("x") }
    var diffOrder by remember { mutableIntStateOf(1) }
    var diffPointStr by remember { mutableStateOf("") }
    var diffResult by remember { mutableStateOf<CanonicalMathResult?>(null) }

    // Indefinite Integration State
    var indefExpr by remember { mutableStateOf("x^2 + 2*x + sin(x)") }
    var indefVar by remember { mutableStateOf("x") }
    var indefResult by remember { mutableStateOf<CanonicalMathResult?>(null) }

    // Definite Integration State
    var defExpr by remember { mutableStateOf("x^2") }
    var defVar by remember { mutableStateOf("x") }
    var defLowerStr by remember { mutableStateOf("0") }
    var defUpperStr by remember { mutableStateOf("3") }
    var defResult by remember { mutableStateOf<CanonicalMathResult?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculus & Symbolic Engine", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("calculus_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("calculus_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Mode Tabs
            item {
                TabRow(
                    selectedTabIndex = selectedTab.ordinal,
                    modifier = Modifier.fillMaxWidth().testTag("calculus_tab_row")
                ) {
                    Tab(
                        selected = selectedTab == CalculusTab.DIFFERENTIATION,
                        onClick = { selectedTab = CalculusTab.DIFFERENTIATION },
                        text = { Text("d/dx Derivative", fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == CalculusTab.INDEFINITE_INTEGRATION,
                        onClick = { selectedTab = CalculusTab.INDEFINITE_INTEGRATION },
                        text = { Text("∫ f(x) dx", fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.AllInclusive, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    Tab(
                        selected = selectedTab == CalculusTab.DEFINITE_INTEGRATION,
                        onClick = { selectedTab = CalculusTab.DEFINITE_INTEGRATION },
                        text = { Text("∫[a, b]", fontSize = 13.sp) },
                        icon = { Icon(Icons.Default.ViewAgenda, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // Tab 1: Differentiation
            if (selectedTab == CalculusTab.DIFFERENTIATION) {
                item {
                    Text(
                        "Symbolic Differentiation",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Computes exact nth-order derivatives with product, quotient, and chain rule expansion.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = diffExpr,
                        onValueChange = { diffExpr = it },
                        label = { Text("Function f(x)") },
                        placeholder = { Text("e.g. x^3 - 3*x + sin(x)") },
                        modifier = Modifier.fillMaxWidth().testTag("diff_expr_input"),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = diffVar,
                            onValueChange = { diffVar = it },
                            label = { Text("Variable") },
                            modifier = Modifier.weight(1f).testTag("diff_var_input"),
                            singleLine = true
                        )

                        // Order selector (1st, 2nd, 3rd)
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text("Order", style = MaterialTheme.typography.labelSmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(1, 2, 3).forEach { order ->
                                    FilterChip(
                                        selected = diffOrder == order,
                                        onClick = { diffOrder = order },
                                        label = { Text("${order}º") },
                                        modifier = Modifier.testTag("diff_order_$order")
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = diffPointStr,
                            onValueChange = { diffPointStr = it },
                            label = { Text("At x = (opt)") },
                            modifier = Modifier.weight(1.5f).testTag("diff_point_input"),
                            singleLine = true
                        )
                    }
                }

                // Differentiate Button & Quick Presets
                item {
                    Button(
                        onClick = {
                            val pt = diffPointStr.toDoubleOrNull()
                            val res = CalculusEngine.differentiate(diffExpr, diffVar, diffOrder, pt)
                            if (res.isSuccess) {
                                val latex = if (pt != null) {
                                    "${res.derivativeLatex} \\implies f^{($diffOrder)}($pt) = ${res.pointValueExact}"
                                } else {
                                    res.derivativeLatex
                                }
                                val disp = if (pt != null) {
                                    "${res.derivativeDisplay}  [at $diffVar = $pt: ${res.pointValueExact}]"
                                } else {
                                    res.derivativeDisplay
                                }
                                diffResult = CanonicalMathResult(
                                    latex = latex,
                                    displayString = disp,
                                    decimalValue = res.pointValueDecimal,
                                    decimalString = res.pointValueDecimal?.let { String.format(java.util.Locale.US, "%.6f", it) },
                                    exactString = res.pointValueExact,
                                    steps = res.steps,
                                    isExact = true
                                )
                            } else {
                                diffResult = CanonicalMathResult.error(res.errorMessage ?: "Failed to differentiate")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_differentiate")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Differentiate (d/dx)")
                    }
                }

                // Rendered Result Card (ZERO duplicate results)
                diffResult?.let { result ->
                    item {
                        MathResultCard(
                            result = result,
                            title = "Derivative f^($diffOrder)($diffVar)",
                            calculationText = "d^$diffOrder/d$diffVar^$diffOrder ($diffExpr)",
                            onSaveToNotebook = onSaveToNotebook
                        )
                    }

                    // Plot in Grapher action button
                    item {
                        OutlinedButton(
                            onClick = { onOpenInGrapher(diffExpr) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_plot_derivative_graph")
                        ) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Plot Function & Derivative in 2D Grapher")
                        }
                    }
                }
            }

            // Tab 2: Indefinite Integration
            if (selectedTab == CalculusTab.INDEFINITE_INTEGRATION) {
                item {
                    Text(
                        "Symbolic Indefinite Integration",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Finds exact antiderivative F(x) + C for polynomials, trigonometric, exponential, and rational expressions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = indefExpr,
                        onValueChange = { indefExpr = it },
                        label = { Text("Integrand f(x)") },
                        placeholder = { Text("e.g. x^2 + 2*x + sin(x)") },
                        modifier = Modifier.fillMaxWidth().testTag("indef_expr_input"),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = indefVar,
                        onValueChange = { indefVar = it },
                        label = { Text("Integration Variable (dx)") },
                        modifier = Modifier.fillMaxWidth().testTag("indef_var_input"),
                        singleLine = true
                    )
                }

                item {
                    Button(
                        onClick = {
                            val res = CalculusEngine.integrateIndefinite(indefExpr, indefVar)
                            if (res.isSuccess) {
                                indefResult = CanonicalMathResult(
                                    latex = res.resultLatex,
                                    displayString = res.resultDisplay,
                                    exactString = res.resultDisplay,
                                    steps = res.steps,
                                    isExact = res.isSymbolic
                                )
                            } else {
                                indefResult = CanonicalMathResult.error(res.errorMessage ?: "Failed to integrate")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_integrate_indef")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Integrate ∫ f(x) dx")
                    }
                }

                indefResult?.let { result ->
                    item {
                        MathResultCard(
                            result = result,
                            title = "Antiderivative ∫ f($indefVar) d$indefVar",
                            calculationText = "∫ ($indefExpr) d$indefVar",
                            onSaveToNotebook = onSaveToNotebook
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { onOpenInGrapher(indefExpr) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_plot_indef_graph")
                        ) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Plot in 2D Grapher")
                        }
                    }
                }
            }

            // Tab 3: Definite Integration
            if (selectedTab == CalculusTab.DEFINITE_INTEGRATION) {
                item {
                    Text(
                        "Definite Integration ∫[a, b] f(x) dx",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Evaluates exact area under curve via Fundamental Theorem, or high-precision Adaptive Simpson's Quadrature.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    OutlinedTextField(
                        value = defExpr,
                        onValueChange = { defExpr = it },
                        label = { Text("Integrand f(x)") },
                        placeholder = { Text("e.g. x^2, sin(x), 1/(1+x^2)") },
                        modifier = Modifier.fillMaxWidth().testTag("def_expr_input"),
                        singleLine = true
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = defLowerStr,
                            onValueChange = { defLowerStr = it },
                            label = { Text("Lower limit (a)") },
                            modifier = Modifier.weight(1f).testTag("def_lower_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = defUpperStr,
                            onValueChange = { defUpperStr = it },
                            label = { Text("Upper limit (b)") },
                            modifier = Modifier.weight(1f).testTag("def_upper_input"),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = defVar,
                            onValueChange = { defVar = it },
                            label = { Text("Var") },
                            modifier = Modifier.weight(0.7f).testTag("def_var_input"),
                            singleLine = true
                        )
                    }
                }

                item {
                    Button(
                        onClick = {
                            val a = defLowerStr.toDoubleOrNull() ?: 0.0
                            val b = defUpperStr.toDoubleOrNull() ?: 1.0
                            val res = CalculusEngine.integrateDefinite(defExpr, a, b, defVar)

                            if (res.isSuccess) {
                                val latex = "\\int_{$a}^{$b} \\left($defExpr\\right)\\,d$defVar = ${res.exactResult ?: res.formattedDecimal}"
                                defResult = CanonicalMathResult(
                                    latex = latex,
                                    displayString = res.exactResult ?: res.formattedDecimal,
                                    decimalValue = res.value,
                                    decimalString = res.formattedDecimal,
                                    exactString = res.exactResult,
                                    steps = res.steps,
                                    isExact = res.isExact
                                )
                            } else {
                                defResult = CanonicalMathResult.error(res.errorMessage ?: "Failed to evaluate definite integral")
                            }
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_integrate_definite")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Evaluate Definite Integral")
                    }
                }

                defResult?.let { result ->
                    item {
                        MathResultCard(
                            result = result,
                            title = "Definite Integral ∫_{$defLowerStr}^{$defUpperStr}",
                            calculationText = "∫_{$defLowerStr}^{$defUpperStr} ($defExpr) d$defVar",
                            onSaveToNotebook = onSaveToNotebook
                        )
                    }

                    item {
                        OutlinedButton(
                            onClick = { onOpenInGrapher(defExpr) },
                            modifier = Modifier.fillMaxWidth().testTag("btn_plot_def_graph")
                        ) {
                            Icon(Icons.Default.AutoGraph, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Plot Function & Area in 2D Grapher")
                        }
                    }
                }
            }
        }
    }
}
