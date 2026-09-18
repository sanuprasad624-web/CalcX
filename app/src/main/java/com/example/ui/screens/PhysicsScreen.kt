package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.CalculationStepResult
import com.example.engine.PhysicsEngine

enum class PhysicsSubTool(val label: String) {
    PROJECTILE("Projectile"),
    KINEMATICS("Kinematics"),
    GRAVITATION("Gravitation"),
    SHM("Oscillations"),
    ELECTROSTATICS("Coulomb / E")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhysicsScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(PhysicsSubTool.PROJECTILE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Physics Problem Solver", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("physics_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("physics_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTool.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                PhysicsSubTool.values().forEach { tool ->
                    Tab(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        text = { Text(tool.label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTool) {
                    PhysicsSubTool.PROJECTILE -> ProjectileView(onSaveToNotebook)
                    PhysicsSubTool.KINEMATICS -> KinematicsView(onSaveToNotebook)
                    PhysicsSubTool.GRAVITATION -> GravitationView(onSaveToNotebook)
                    PhysicsSubTool.SHM -> ShmView(onSaveToNotebook)
                    PhysicsSubTool.ELECTROSTATICS -> ElectrostaticsView(onSaveToNotebook)
                }
            }
        }
    }
}

@Composable
private fun ProjectileView(onSaveToNotebook: (String, String, String) -> Unit) {
    var uStr by remember { mutableStateOf("25.0") }
    var thetaStr by remember { mutableStateOf("45.0") }
    var gStr by remember { mutableStateOf("9.8") }
    var steps by remember { mutableStateOf<List<CalculationStepResult>>(emptyList()) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Projectile Motion Solver", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Calculates Time of Flight, Maximum Height, and Range with full substitution steps.", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = uStr, onValueChange = { uStr = it }, label = { Text("Velocity u (m/s)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = thetaStr, onValueChange = { thetaStr = it }, label = { Text("Angle θ (°)") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            OutlinedTextField(
                value = gStr,
                onValueChange = { gStr = it },
                label = { Text("Gravity g (m/s², default 9.8)") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Button(
                onClick = {
                    val u = uStr.toDoubleOrNull() ?: 20.0
                    val theta = thetaStr.toDoubleOrNull() ?: 45.0
                    val g = gStr.toDoubleOrNull() ?: 9.8
                    steps = PhysicsEngine.solveProjectile(u, theta, g)
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_solve_projectile")
            ) {
                Text("Calculate Trajectory & Steps", fontWeight = FontWeight.Bold)
            }
        }

        if (steps.isNotEmpty()) {
            items(steps.size) { index ->
                val s = steps[index]
                StepResultCard(s) {
                    onSaveToNotebook("Projectile: ${s.title}", "${s.answer} ${s.unit}", s.toShareableText())
                }
            }
        }
    }
}

@Composable
private fun KinematicsView(onSaveToNotebook: (String, String, String) -> Unit) {
    var uStr by remember { mutableStateOf("0.0") }
    var aStr by remember { mutableStateOf("9.8") }
    var tStr by remember { mutableStateOf("5.0") }
    var steps by remember { mutableStateOf<List<CalculationStepResult>>(emptyList()) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("1D Kinematics (Constant Acceleration)", fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = uStr, onValueChange = { uStr = it }, label = { Text("u (m/s)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = aStr, onValueChange = { aStr = it }, label = { Text("a (m/s²)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tStr, onValueChange = { tStr = it }, label = { Text("t (s)") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Button(
                onClick = {
                    val u = uStr.toDoubleOrNull() ?: 0.0
                    val a = aStr.toDoubleOrNull() ?: 9.8
                    val t = tStr.toDoubleOrNull() ?: 1.0
                    steps = PhysicsEngine.solveKinematics(u, a, t)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Solve v & s", fontWeight = FontWeight.Bold)
            }
        }
        if (steps.isNotEmpty()) {
            items(steps.size) { i ->
                StepResultCard(steps[i]) {
                    onSaveToNotebook("Kinematics: ${steps[i].title}", "${steps[i].answer} ${steps[i].unit}", steps[i].toShareableText())
                }
            }
        }
    }
}

@Composable
private fun GravitationView(onSaveToNotebook: (String, String, String) -> Unit) {
    var m1Str by remember { mutableStateOf("5.972e24") }
    var m2Str by remember { mutableStateOf("70.0") }
    var rStr by remember { mutableStateOf("6.371e6") }
    var result by remember { mutableStateOf<CalculationStepResult?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Universal Law of Gravitation", fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(value = m1Str, onValueChange = { m1Str = it }, label = { Text("Mass m₁ (kg, e.g. Earth 5.972e24)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = m2Str, onValueChange = { m2Str = it }, label = { Text("Mass m₂ (kg)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(value = rStr, onValueChange = { rStr = it }, label = { Text("Separation r (m)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = {
                    val m1 = m1Str.toDoubleOrNull() ?: 1.0
                    val m2 = m2Str.toDoubleOrNull() ?: 1.0
                    val r = rStr.toDoubleOrNull() ?: 1.0
                    result = PhysicsEngine.solveGravitation(m1, m2, r)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Gravitational Force", fontWeight = FontWeight.Bold)
            }
        }
        result?.let {
            item {
                StepResultCard(it) {
                    onSaveToNotebook("Gravitation: ${it.title}", "${it.answer} ${it.unit}", it.toShareableText())
                }
            }
        }
    }
}

@Composable
private fun ShmView(onSaveToNotebook: (String, String, String) -> Unit) {
    var mStr by remember { mutableStateOf("0.5") }
    var kStr by remember { mutableStateOf("200.0") }
    var result by remember { mutableStateOf<CalculationStepResult?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Simple Harmonic Motion (Spring-Mass)", fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = mStr, onValueChange = { mStr = it }, label = { Text("Mass m (kg)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = kStr, onValueChange = { kStr = it }, label = { Text("Spring k (N/m)") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Button(
                onClick = {
                    val m = mStr.toDoubleOrNull() ?: 1.0
                    val k = kStr.toDoubleOrNull() ?: 100.0
                    result = PhysicsEngine.solveSHM(m, k)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Period T", fontWeight = FontWeight.Bold)
            }
        }
        result?.let {
            item {
                StepResultCard(it) {
                    onSaveToNotebook("SHM: ${it.title}", "${it.answer} ${it.unit}", it.toShareableText())
                }
            }
        }
    }
}

@Composable
private fun ElectrostaticsView(onSaveToNotebook: (String, String, String) -> Unit) {
    var q1Str by remember { mutableStateOf("1.0e-6") }
    var q2Str by remember { mutableStateOf("2.0e-6") }
    var rStr by remember { mutableStateOf("0.1") }
    var result by remember { mutableStateOf<CalculationStepResult?>(null) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Coulomb's Law", fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = q1Str, onValueChange = { q1Str = it }, label = { Text("q₁ (Coulombs)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = q2Str, onValueChange = { q2Str = it }, label = { Text("q₂ (Coulombs)") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            OutlinedTextField(value = rStr, onValueChange = { rStr = it }, label = { Text("Distance r (meters)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button(
                onClick = {
                    val q1 = q1Str.toDoubleOrNull() ?: 1e-6
                    val q2 = q2Str.toDoubleOrNull() ?: 1e-6
                    val r = rStr.toDoubleOrNull() ?: 0.1
                    result = PhysicsEngine.solveCoulomb(q1, q2, r)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Force", fontWeight = FontWeight.Bold)
            }
        }
        result?.let {
            item {
                StepResultCard(it) {
                    onSaveToNotebook("Coulomb: ${it.title}", "${it.answer} ${it.unit}", it.toShareableText())
                }
            }
        }
    }
}

@Composable
fun StepResultCard(res: CalculationStepResult, onSave: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(res.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("Formula: ${res.formulaUsed}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text("Substitution: ${res.substitution}", style = MaterialTheme.typography.bodySmall)
            Text("Result = ${res.answer} ${res.unit}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary)

            IconButton(onClick = onSave, modifier = Modifier.align(Alignment.End)) {
                Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Notebook")
            }
        }
    }
}
