package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.*
import com.example.math.CanonicalMathResult
import com.example.math.calculus.CalculusEngine
import com.example.ui.components.MathResultCard
import com.example.ui.components.MathView

enum class JeeSubTool(val label: String) {
    QUADRATIC("Quadratic"),
    CALCULUS_DIFF("Calculus d/dx"),
    CALCULUS_INT("Calculus ∫"),
    SYSTEM_2X2("2x2 System"),
    VECTORS("Vectors"),
    COMPLEX("Complex No."),
    MOMENT_INERTIA("Inertia"),
    DIMENSIONS("Dimensions")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JeeToolkitScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(JeeSubTool.QUADRATIC) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("JEE & Engineering Toolkit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("jee_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("jee_toolkit_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Horizontal tool selector
            ScrollableTabRow(
                selectedTabIndex = selectedTool.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                JeeSubTool.values().forEach { tool ->
                    Tab(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        text = { Text(tool.label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            // Active Tool Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                when (selectedTool) {
                    JeeSubTool.QUADRATIC -> QuadraticToolView(onSaveToNotebook)
                    JeeSubTool.CALCULUS_DIFF -> JeeCalculusDiffView(onSaveToNotebook)
                    JeeSubTool.CALCULUS_INT -> JeeCalculusIntView(onSaveToNotebook)
                    JeeSubTool.SYSTEM_2X2 -> LinearSystem2ToolView(onSaveToNotebook)
                    JeeSubTool.VECTORS -> VectorToolView(onSaveToNotebook)
                    JeeSubTool.COMPLEX -> ComplexToolView(onSaveToNotebook)
                    JeeSubTool.MOMENT_INERTIA -> MomentOfInertiaToolView(onSaveToNotebook)
                    JeeSubTool.DIMENSIONS -> DimensionToolView()
                }
            }
        }
    }
}

@Composable
private fun QuadraticToolView(onSaveToNotebook: (String, String, String) -> Unit) {
    var aStr by remember { mutableStateOf("1") }
    var bStr by remember { mutableStateOf("-5") }
    var cStr by remember { mutableStateOf("6") }
    var result by remember { mutableStateOf<EquationResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "Solve ax² + bx + c = 0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Computes exact discriminant Δ, root nature, radical forms, and decimal approximations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = aStr,
                    onValueChange = { aStr = it },
                    label = { Text("a") },
                    modifier = Modifier.weight(1f).testTag("input_quad_a")
                )
                OutlinedTextField(
                    value = bStr,
                    onValueChange = { bStr = it },
                    label = { Text("b") },
                    modifier = Modifier.weight(1f).testTag("input_quad_b")
                )
                OutlinedTextField(
                    value = cStr,
                    onValueChange = { cStr = it },
                    label = { Text("c") },
                    modifier = Modifier.weight(1f).testTag("input_quad_c")
                )
            }
        }

        item {
            Button(
                onClick = {
                    val a = aStr.toDoubleOrNull() ?: 1.0
                    val b = bStr.toDoubleOrNull() ?: 0.0
                    val c = cStr.toDoubleOrNull() ?: 0.0
                    result = EquationSolverEngine.solveQuadratic(a, b, c)
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_solve_quad")
            ) {
                Text("Solve Quadratic Equation", fontWeight = FontWeight.Bold)
            }
        }

        result?.let { res ->
            item {
                when (res) {
                    is EquationResult.Quadratic -> {
                        val quadLatex = "x_1 = ${res.root1Display}, \\quad x_2 = ${res.root2Display}"
                        MathResultCard(
                            result = CanonicalMathResult(
                                latex = quadLatex,
                                displayString = "${res.root1Display}, ${res.root2Display}",
                                steps = res.steps,
                                isExact = true
                            ),
                            title = "Quadratic Roots: ${res.natureOfRoots}",
                            calculationText = "${aStr}x^2 + ${bStr}x + ${cStr} = 0",
                            formulaLatex = "x = \\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}",
                            substitutionLatex = "\\Delta = (${bStr})^2 - 4(${aStr})(${cStr}) = ${res.discriminant}",
                            onSaveToNotebook = onSaveToNotebook
                        )
                    }
                    is EquationResult.Error -> {
                        Text(res.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun LinearSystem2ToolView(onSaveToNotebook: (String, String, String) -> Unit) {
    var a1 by remember { mutableStateOf("2") }
    var b1 by remember { mutableStateOf("3") }
    var c1 by remember { mutableStateOf("8") }

    var a2 by remember { mutableStateOf("5") }
    var b2 by remember { mutableStateOf("-1") }
    var c2 by remember { mutableStateOf("3") }

    var result by remember { mutableStateOf<EquationResult?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Simultaneous Equations (2 Variables)", fontWeight = FontWeight.Bold)
            MathView(
                latex = "\\begin{cases} a_1 x + b_1 y = c_1 \\\\ a_2 x + b_2 y = c_2 \\end{cases}",
                fontSize = 16.sp
            )
        }

        item {
            Text("Equation 1", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = a1, onValueChange = { a1 = it }, label = { Text("a₁") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = b1, onValueChange = { b1 = it }, label = { Text("b₁") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = c1, onValueChange = { c1 = it }, label = { Text("c₁") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Equation 2", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = a2, onValueChange = { a2 = it }, label = { Text("a₂") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = b2, onValueChange = { b2 = it }, label = { Text("b₂") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = c2, onValueChange = { c2 = it }, label = { Text("c₂") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Button(
                onClick = {
                    val nA1 = a1.toDoubleOrNull() ?: 1.0
                    val nB1 = b1.toDoubleOrNull() ?: 0.0
                    val nC1 = c1.toDoubleOrNull() ?: 0.0
                    val nA2 = a2.toDoubleOrNull() ?: 0.0
                    val nB2 = b2.toDoubleOrNull() ?: 1.0
                    val nC2 = c2.toDoubleOrNull() ?: 0.0
                    result = EquationSolverEngine.solve2x2System(nA1, nB1, nC1, nA2, nB2, nC2)
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_solve_system")
            ) {
                Text("Solve System via Cramer's Rule", fontWeight = FontWeight.Bold)
            }
        }

        result?.let { res ->
            item {
                when (res) {
                    is EquationResult.LinearSystem2 -> {
                        val sysLatex = "x = ${String.format(java.util.Locale.US, "%.4f", res.x)}, \\quad y = ${String.format(java.util.Locale.US, "%.4f", res.y)}"
                        MathResultCard(
                            result = CanonicalMathResult(
                                latex = sysLatex,
                                displayString = "x = ${String.format(java.util.Locale.US, "%.4f", res.x)}, y = ${String.format(java.util.Locale.US, "%.4f", res.y)}",
                                steps = res.steps,
                                isExact = true
                            ),
                            title = "System Solution (Cramer's Rule)",
                            calculationText = "\\begin{cases} ${a1}x + ${b1}y = $c1 \\\\ ${a2}x + ${b2}y = $c2 \\end{cases}",
                            formulaLatex = "x = \\frac{D_x}{D}, \\quad y = \\frac{D_y}{D}",
                            onSaveToNotebook = onSaveToNotebook
                        )
                    }
                    is EquationResult.Error -> {
                        Text(res.message, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    else -> {}
                }
            }
        }
    }
}

@Composable
private fun VectorToolView(onSaveToNotebook: (String, String, String) -> Unit) {
    var v1x by remember { mutableStateOf("1") }
    var v1y by remember { mutableStateOf("2") }
    var v1z by remember { mutableStateOf("3") }

    var v2x by remember { mutableStateOf("4") }
    var v2y by remember { mutableStateOf("5") }
    var v2z by remember { mutableStateOf("6") }

    val vec1 = remember(v1x, v1y, v1z) {
        Vector3D(v1x.toDoubleOrNull() ?: 0.0, v1y.toDoubleOrNull() ?: 0.0, v1z.toDoubleOrNull() ?: 0.0)
    }
    val vec2 = remember(v2x, v2y, v2z) {
        Vector3D(v2x.toDoubleOrNull() ?: 0.0, v2y.toDoubleOrNull() ?: 0.0, v2z.toDoubleOrNull() ?: 0.0)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("3D Vector Operations (A & B)", fontWeight = FontWeight.Bold)
        }

        item {
            Text("Vector A (Ax î + Ay ĵ + Az k̂)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = v1x, onValueChange = { v1x = it }, label = { Text("Ax") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = v1y, onValueChange = { v1y = it }, label = { Text("Ay") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = v1z, onValueChange = { v1z = it }, label = { Text("Az") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Vector B (Bx î + By ĵ + Bz k̂)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = v2x, onValueChange = { v2x = it }, label = { Text("Bx") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = v2y, onValueChange = { v2y = it }, label = { Text("By") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = v2z, onValueChange = { v2z = it }, label = { Text("Bz") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            val dot = vec1 dot vec2
            val cross = vec1 cross vec2
            val angle = try { vec1.angleWith(vec2) } catch (_: Exception) { 0.0 }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Calculated Results:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    MathView(
                        latex = "\\vec{A} = ${v1x}\\hat{i} + ${v1y}\\hat{j} + ${v1z}\\hat{k}, \\quad |\\vec{A}| = ${String.format(java.util.Locale.US, "%.4f", vec1.magnitude)}",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "\\vec{B} = ${v2x}\\hat{i} + ${v2y}\\hat{j} + ${v2z}\\hat{k}, \\quad |\\vec{B}| = ${String.format(java.util.Locale.US, "%.4f", vec2.magnitude)}",
                        fontSize = 16.sp
                    )
                    HorizontalDivider()
                    MathView(
                        latex = "\\vec{A} \\cdot \\vec{B} = ${String.format(java.util.Locale.US, "%.4f", dot)}",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MathView(
                        latex = "\\vec{A} \\times \\vec{B} = ${cross.x}\\hat{i} + ${cross.y}\\hat{j} + ${cross.z}\\hat{k}",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    MathView(
                        latex = "\\theta = ${String.format(java.util.Locale.US, "%.2f", angle)}^\\circ",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "\\vec{A} + \\vec{B} = ${(vec1 + vec2).x}\\hat{i} + ${(vec1 + vec2).y}\\hat{j} + ${(vec1 + vec2).z}\\hat{k}",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "\\vec{A} - \\vec{B} = ${(vec1 - vec2).x}\\hat{i} + ${(vec1 - vec2).y}\\hat{j} + ${(vec1 - vec2).z}\\hat{k}",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ComplexToolView(onSaveToNotebook: (String, String, String) -> Unit) {
    var r1 by remember { mutableStateOf("3") }
    var i1 by remember { mutableStateOf("4") }
    var r2 by remember { mutableStateOf("1") }
    var i2 by remember { mutableStateOf("-2") }

    val c1 = remember(r1, i1) { ComplexNumber(r1.toDoubleOrNull() ?: 0.0, i1.toDoubleOrNull() ?: 0.0) }
    val c2 = remember(r2, i2) { ComplexNumber(r2.toDoubleOrNull() ?: 0.0, i2.toDoubleOrNull() ?: 0.0) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Complex Numbers (Z₁ & Z₂)", fontWeight = FontWeight.Bold)
        }

        item {
            Text("Z₁ = a + bi")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = r1, onValueChange = { r1 = it }, label = { Text("Real a") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = i1, onValueChange = { i1 = it }, label = { Text("Imag b") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Z₂ = c + di")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = r2, onValueChange = { r2 = it }, label = { Text("Real c") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = i2, onValueChange = { i2 = it }, label = { Text("Imag d") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Polar Forms & Conjugates:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    
                    MathView(
                        latex = "Z_1 = ${c1.real} + ${c1.imag}i = ${String.format(java.util.Locale.US, "%.3f", c1.modulus)} e^{i ${String.format(java.util.Locale.US, "%.1f", c1.argumentDeg)}^\\circ}",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "Z_2 = ${c2.real} + ${c2.imag}i = ${String.format(java.util.Locale.US, "%.3f", c2.modulus)} e^{i ${String.format(java.util.Locale.US, "%.1f", c2.argumentDeg)}^\\circ}",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "\\overline{Z_1} = ${c1.real} - ${c1.imag}i",
                        fontSize = 16.sp
                    )
                    HorizontalDivider()
                    Text("Operations:", fontWeight = FontWeight.Bold)
                    MathView(
                        latex = "Z_1 + Z_2 = ${(c1 + c2).real} + ${(c1 + c2).imag}i",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "Z_1 - Z_2 = ${(c1 - c2).real} + ${(c1 - c2).imag}i",
                        fontSize = 16.sp
                    )
                    MathView(
                        latex = "Z_1 \\cdot Z_2 = ${(c1 * c2).real} + ${(c1 * c2).imag}i",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    val divResult = try {
                        val res = c1 / c2
                        "\\frac{Z_1}{Z_2} = ${String.format(java.util.Locale.US, "%.4f", res.real)} + ${String.format(java.util.Locale.US, "%.4f", res.imag)}i"
                    } catch (e: Exception) {
                        "\\text{Division by zero}"
                    }
                    MathView(
                        latex = divResult,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MomentOfInertiaToolView(onSaveToNotebook: (String, String, String) -> Unit) {
    var mass by remember { mutableStateOf("2.0") }
    var dim by remember { mutableStateOf("0.5") }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Moment of Inertia Calculator & Standards", fontWeight = FontWeight.Bold)
            Text("Rotational inertia for standard geometries tested in JEE & Engineering.", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = mass,
                    onValueChange = { mass = it },
                    label = { Text("Mass M (kg)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = dim,
                    onValueChange = { dim = it },
                    label = { Text("Radius R / Length L (m)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        items(PhysicsEngine.momentOfInertiaLibrary) { item ->
            val mVal = mass.toDoubleOrNull() ?: 1.0
            val dVal = dim.toDoubleOrNull() ?: 1.0
            val iVal = item.calculate(mVal, dVal, 0.0)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(item.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Axis: ${item.axis}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                    
                    MathView(
                        latex = item.formulaText,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    MathView(
                        latex = "I = ${String.format(java.util.Locale.US, "%.5f", iVal)}\\text{ kg}\\cdot\\text{m}^2",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(item.explanation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun DimensionToolView() {
    var searchQuery by remember { mutableStateOf("") }
    var massPower by remember { mutableStateOf(1) }
    var lengthPower by remember { mutableStateOf(1) }
    var timePower by remember { mutableStateOf(-2) }
    var currentPower by remember { mutableStateOf(0) }
    var tempPower by remember { mutableStateOf(0) }

    val activeDim = Dimension(massPower, lengthPower, timePower, currentPower, tempPower)
    val matches = DimensionEngine.findMatches(activeDim)
    val searchResults = remember(searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else DimensionEngine.searchQuantities(searchQuery)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("JEE Dimensional Analysis Checker", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Analyze and identify physical quantities from dimensional formulas [Mᵃ Lᵇ Tᶜ Aᵈ Kᵉ].", style = MaterialTheme.typography.bodySmall)
        }

        // Search Bar for Unit Dimensions & Quantities
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dimension_search_input"),
                label = { Text("Search quantity, unit, or dimension...") },
                placeholder = { Text("e.g., Viscosity, N/m², J·s, Force") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Search Results List (if query is active)
        if (searchQuery.isNotBlank()) {
            item {
                Text(
                    text = "Search Results (${searchResults.size}) — Tap to Load:",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (searchResults.isEmpty()) {
                item {
                    Text(
                        text = "No quantities found matching \"$searchQuery\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(searchResults.size) { index ->
                    val q = searchResults[index]
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                massPower = q.dimension.m
                                lengthPower = q.dimension.l
                                timePower = q.dimension.t
                                currentPower = q.dimension.i
                                tempPower = q.dimension.th
                                searchQuery = ""
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(q.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                Text("SI Unit: ${q.siUnit}", style = MaterialTheme.typography.bodySmall)
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                MathView(
                                    latex = q.dimension.toLatexString(),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    MathView(
                        latex = activeDim.toLatexString(),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        item {
            Text("Adjust Dimension Powers:", fontWeight = FontWeight.SemiBold)
            PowerStepper("Mass (M)", massPower) { massPower = it }
            PowerStepper("Length (L)", lengthPower) { lengthPower = it }
            PowerStepper("Time (T)", timePower) { timePower = it }
            PowerStepper("Current (A)", currentPower) { currentPower = it }
            PowerStepper("Temperature (K)", tempPower) { tempPower = it }
        }

        item {
            Text("Matching Physical Quantities:", fontWeight = FontWeight.Bold)
            if (matches.isEmpty()) {
                Text("No common single named quantity matches this exact combination.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                matches.forEach { q ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(q.name, fontWeight = FontWeight.Bold)
                                Text("SI Unit: ${q.siUnit}", style = MaterialTheme.typography.bodySmall)
                            }
                            MathView(
                                latex = q.dimension.toLatexString(),
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PowerStepper(name: String, value: Int, onValueChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(name, fontWeight = FontWeight.Medium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            FilledTonalIconButton(onClick = { onValueChange(value - 1) }, modifier = Modifier.size(36.dp)) {
                Text("-", fontWeight = FontWeight.Bold)
            }
            Text(value.toString(), modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
            FilledTonalIconButton(onClick = { onValueChange(value + 1) }, modifier = Modifier.size(36.dp)) {
                Text("+", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun JeeCalculusDiffView(onSaveToNotebook: (String, String, String) -> Unit) {
    var expr by remember { mutableStateOf("x^3 - 4*x^2 + 5") }
    var order by remember { mutableIntStateOf(1) }
    var pointStr by remember { mutableStateOf("") }
    var result by remember { mutableStateOf<CanonicalMathResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("JEE Calculus: Symbolic Differentiation", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Power rule, product rule, quotient rule, chain rule for JEE calculus.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("Function f(x)") },
                modifier = Modifier.fillMaxWidth().testTag("jee_diff_expr_input")
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2).forEach { o ->
                    FilterChip(
                        selected = order == o,
                        onClick = { order = o },
                        label = { Text("${o}st Order") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = pointStr,
                    onValueChange = { pointStr = it },
                    label = { Text("At x = (opt)") },
                    modifier = Modifier.weight(1.5f)
                )
            }
        }

        item {
            Button(
                onClick = {
                    val pt = pointStr.toDoubleOrNull()
                    val res = CalculusEngine.differentiate(expr, "x", order, pt)
                    if (res.isSuccess) {
                        result = CanonicalMathResult(
                            latex = if (pt != null) "${res.derivativeLatex} \\implies f^{($order)}($pt) = ${res.pointValueExact}" else res.derivativeLatex,
                            displayString = if (pt != null) "${res.derivativeDisplay}  [at x = $pt: ${res.pointValueExact}]" else res.derivativeDisplay,
                            exactString = res.pointValueExact,
                            decimalValue = res.pointValueDecimal,
                            decimalString = res.pointValueDecimal?.let { String.format(java.util.Locale.US, "%.6f", it) },
                            steps = res.steps,
                            isExact = true
                        )
                    } else {
                        result = CanonicalMathResult.error(res.errorMessage ?: "Failed to differentiate")
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_jee_diff")
            ) {
                Text("Differentiate f(x)", fontWeight = FontWeight.Bold)
            }
        }

        result?.let { res ->
            item {
                MathResultCard(
                    result = res,
                    title = "Derivative Result",
                    calculationText = "d/dx ($expr)",
                    onSaveToNotebook = onSaveToNotebook
                )
            }
        }
    }
}

@Composable
private fun JeeCalculusIntView(onSaveToNotebook: (String, String, String) -> Unit) {
    var expr by remember { mutableStateOf("x^2 + sin(x)") }
    var lowerStr by remember { mutableStateOf("0") }
    var upperStr by remember { mutableStateOf("pi") }
    var isDefinite by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<CanonicalMathResult?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("JEE Calculus: Integration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Symbolic antiderivatives & exact/numerical definite integrals.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isDefinite,
                    onClick = { isDefinite = false },
                    label = { Text("Indefinite ∫ f(x) dx") }
                )
                FilterChip(
                    selected = isDefinite,
                    onClick = { isDefinite = true },
                    label = { Text("Definite ∫[a, b]") }
                )
            }
        }

        item {
            OutlinedTextField(
                value = expr,
                onValueChange = { expr = it },
                label = { Text("Integrand f(x)") },
                modifier = Modifier.fillMaxWidth().testTag("jee_int_expr_input")
            )
        }

        if (isDefinite) {
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = lowerStr,
                        onValueChange = { lowerStr = it },
                        label = { Text("Lower limit a") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = upperStr,
                        onValueChange = { upperStr = it },
                        label = { Text("Upper limit b") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (isDefinite) {
                        val a = if (lowerStr.trim() == "pi") Math.PI else (lowerStr.toDoubleOrNull() ?: 0.0)
                        val b = if (upperStr.trim() == "pi") Math.PI else (upperStr.toDoubleOrNull() ?: 1.0)
                        val res = CalculusEngine.integrateDefinite(expr, a, b, "x")
                        if (res.isSuccess) {
                            result = CanonicalMathResult(
                                latex = "\\int_{$lowerStr}^{$upperStr} \\left($expr\\right)\\,dx = ${res.exactResult ?: res.formattedDecimal}",
                                displayString = res.exactResult ?: res.formattedDecimal,
                                decimalValue = res.value,
                                decimalString = res.formattedDecimal,
                                exactString = res.exactResult,
                                steps = res.steps,
                                isExact = res.isExact
                            )
                        } else {
                            result = CanonicalMathResult.error(res.errorMessage ?: "Failed to integrate")
                        }
                    } else {
                        val res = CalculusEngine.integrateIndefinite(expr, "x")
                        if (res.isSuccess) {
                            result = CanonicalMathResult(
                                latex = res.resultLatex,
                                displayString = res.resultDisplay,
                                exactString = res.resultDisplay,
                                steps = res.steps,
                                isExact = res.isSymbolic
                            )
                        } else {
                            result = CanonicalMathResult.error(res.errorMessage ?: "Failed to integrate")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_jee_int")
            ) {
                Text(if (isDefinite) "Evaluate Definite Integral" else "Find Antiderivative", fontWeight = FontWeight.Bold)
            }
        }

        result?.let { res ->
            item {
                MathResultCard(
                    result = res,
                    title = if (isDefinite) "Definite Integral" else "Antiderivative",
                    calculationText = "∫ ($expr) dx",
                    onSaveToNotebook = onSaveToNotebook
                )
            }
        }
    }
}
