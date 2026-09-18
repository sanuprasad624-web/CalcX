package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.engine.ChemistryEngine

enum class ChemSubTool(val label: String) {
    MOLAR_MASS("Molar Mass"),
    ORGANIC_CHEM("Organic Chem"),
    PH_BUFFER("pH & Buffers"),
    NERNST("Nernst Eq"),
    THERMO("Gibbs ΔG")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChemistryScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(ChemSubTool.MOLAR_MASS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chemistry Calculator & Tools", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chemistry_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("chemistry_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTool.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                ChemSubTool.values().forEach { tool ->
                    Tab(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        text = { Text(tool.label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTool) {
                    ChemSubTool.MOLAR_MASS -> MolarMassView(onSaveToNotebook)
                    ChemSubTool.ORGANIC_CHEM -> OrganicChemistryContent(onSaveToNotebook)
                    ChemSubTool.PH_BUFFER -> PhBufferView(onSaveToNotebook)
                    ChemSubTool.NERNST -> NernstView(onSaveToNotebook)
                    ChemSubTool.THERMO -> ThermoView(onSaveToNotebook)
                }
            }
        }
    }
}

@Composable
private fun MolarMassView(onSaveToNotebook: (String, String, String) -> Unit) {
    var formula by remember { mutableStateOf("H2SO4") }
    var result by remember { mutableStateOf<ChemistryEngine.MolarMassResult?>(null) }

    LaunchedEffect(Unit) {
        result = ChemistryEngine.calculateMolarMass(formula)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Molar Mass & Composition Calculator", fontWeight = FontWeight.Bold)
            Text("Enter chemical formulas with brackets: e.g. H2SO4, Ca(OH)2, C6H12O6, Fe2(SO4)3", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = formula,
                    onValueChange = {
                        formula = it
                        result = ChemistryEngine.calculateMolarMass(it)
                    },
                    label = { Text("Chemical Formula") },
                    modifier = Modifier.weight(1f).testTag("input_chem_formula")
                )
            }
        }

        // Quick presets
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("H2O", "H2SO4", "NaCl", "C6H12O6", "KMnO4").forEach { preset ->
                    SuggestionChip(
                        onClick = {
                            formula = preset
                            result = ChemistryEngine.calculateMolarMass(preset)
                        },
                        label = { Text(preset) }
                    )
                }
            }
        }

        result?.let { res ->
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Formula: ${res.formula}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            "Total Molar Mass: ${"%.4f".format(res.totalMolarMass)} g/mol",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Divider()
                        Text("Elemental Mass Breakdown:", fontWeight = FontWeight.SemiBold)
                        res.elementBreakdown.forEach { (elem, pair) ->
                            val count = pair.first
                            val pct = pair.second
                            val atomicMass = ChemistryEngine.atomicMasses[elem] ?: 0.0
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("$elem (x$count, ${atomicMass} u)")
                                Text("${"%.2f".format(pct)} %", fontWeight = FontWeight.Bold)
                            }
                        }

                        IconButton(
                            onClick = {
                                onSaveToNotebook(
                                    "Molar Mass: ${res.formula}",
                                    "${"%.4f".format(res.totalMolarMass)} g/mol",
                                    res.elementBreakdown.map { "${it.key}: ${"%.2f".format(it.value.second)}%" }.joinToString(", ")
                                )
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Save")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PhBufferView(onSaveToNotebook: (String, String, String) -> Unit) {
    var hConc by remember { mutableStateOf("1.0e-3") }
    var phResult by remember { mutableStateOf("3.00") }

    var pKa by remember { mutableStateOf("4.76") }
    var saltM by remember { mutableStateOf("0.1") }
    var acidM by remember { mutableStateOf("0.1") }
    var bufferPh by remember { mutableStateOf("4.76") }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("pH and Acid-Base Equilibrium", fontWeight = FontWeight.Bold)
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("pH from [H⁺] Concentration", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    OutlinedTextField(
                        value = hConc,
                        onValueChange = {
                            hConc = it
                            val c = it.toDoubleOrNull()
                            if (c != null && c > 0) {
                                phResult = "%.4f".format(ChemistryEngine.calculatePhFromH(c))
                            }
                        },
                        label = { Text("[H⁺] in mol/L") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Calculated pH = $phResult", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    val phVal = phResult.toDoubleOrNull() ?: 7.0
                    val pohVal = 14.0 - phVal
                    Text("pOH = ${"%.2f".format(pohVal)}  |  [OH⁻] = ${"%.2e".format(ChemistryEngine.calculateHFromPh(pohVal))} M")
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Buffer pH (Henderson-Hasselbalch)", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = pKa, onValueChange = { pKa = it }, label = { Text("pKₐ") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = saltM, onValueChange = { saltM = it }, label = { Text("[Salt/Base]") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = acidM, onValueChange = { acidM = it }, label = { Text("[Acid]") }, modifier = Modifier.weight(1f))
                    }
                    Button(
                        onClick = {
                            val ka = pKa.toDoubleOrNull() ?: 4.76
                            val s = saltM.toDoubleOrNull() ?: 0.1
                            val a = acidM.toDoubleOrNull() ?: 0.1
                            bufferPh = "%.4f".format(ChemistryEngine.calculateBufferPh(ka, s, a))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calculate Buffer pH")
                    }
                    Text("Buffer pH = $bufferPh", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun NernstView(onSaveToNotebook: (String, String, String) -> Unit) {
    var e0 by remember { mutableStateOf("1.10") }
    var nElectrons by remember { mutableStateOf("2") }
    var qQuotient by remember { mutableStateOf("0.01") }
    var eCell by remember { mutableStateOf("1.159") }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Nernst Equation (at 298.15 K)", fontWeight = FontWeight.Bold)
            Text("E = E° - (0.05916 / n) · log₁₀(Q)", style = MaterialTheme.typography.bodySmall)
        }

        item {
            OutlinedTextField(value = e0, onValueChange = { e0 = it }, label = { Text("Standard EMF E° (Volts)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nElectrons, onValueChange = { nElectrons = it }, label = { Text("Electrons (n)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = qQuotient, onValueChange = { qQuotient = it }, label = { Text("Reaction Quotient Q") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Button(
                onClick = {
                    val ez = e0.toDoubleOrNull() ?: 1.10
                    val n = nElectrons.toIntOrNull() ?: 2
                    val q = qQuotient.toDoubleOrNull() ?: 1.0
                    eCell = "%.4f".format(ChemistryEngine.calculateNernst(ez, n, q))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate Cell Potential E", fontWeight = FontWeight.Bold)
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cell Potential E = $eCell V", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun ThermoView(onSaveToNotebook: (String, String, String) -> Unit) {
    var deltaH by remember { mutableStateOf("-92.2") }
    var deltaS by remember { mutableStateOf("-198.7") }
    var tempK by remember { mutableStateOf("298.15") }
    var deltaG by remember { mutableStateOf("-32.96") }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Gibbs Free Energy & Spontaneity", fontWeight = FontWeight.Bold)
            Text("ΔG = ΔH - T·ΔS", style = MaterialTheme.typography.bodySmall)
        }

        item {
            OutlinedTextField(value = deltaH, onValueChange = { deltaH = it }, label = { Text("Enthalpy ΔH (kJ/mol)") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = deltaS, onValueChange = { deltaS = it }, label = { Text("Entropy ΔS (J/(mol·K))") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tempK, onValueChange = { tempK = it }, label = { Text("Temp T (Kelvin)") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Button(
                onClick = {
                    val h = deltaH.toDoubleOrNull() ?: 0.0
                    val s = deltaS.toDoubleOrNull() ?: 0.0
                    val t = tempK.toDoubleOrNull() ?: 298.15
                    deltaG = "%.2f".format(ChemistryEngine.calculateGibbs(h, s, t))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Calculate ΔG & Spontaneity", fontWeight = FontWeight.Bold)
            }
        }
        item {
            val dg = deltaG.toDoubleOrNull() ?: 0.0
            val isSpontaneous = dg < 0.0
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("ΔG = $deltaG kJ/mol", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        if (isSpontaneous) "Reaction is SPONTANEOUS (ΔG < 0)" else "Reaction is NON-SPONTANEOUS (ΔG ≥ 0)",
                        fontWeight = FontWeight.Bold,
                        color = if (isSpontaneous) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}
