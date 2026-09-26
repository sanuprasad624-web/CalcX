package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import com.example.ui.components.MathView
import com.example.ui.components.RDKitStructureView

enum class OrganicToolTab(val label: String) {
    REACTIONS("Reactions & Predictor"),
    STRUCTURES("2D Molecules (RDKit)"),
    MECHANISMS("Mechanisms"),
    GOC("GOC & Stability"),
    ISOMERISM("Isomerism"),
    NAMED_REACTIONS("Named Reactions"),
    TESTS_REAGENTS("Tests & Reagents"),
    CALCULATIONS("Organic Calc")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganicChemistryScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(OrganicToolTab.REACTIONS) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("JEE Organic Chemistry Toolkit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Offline-First Mechanism & Reaction Engine", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("organic_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("organic_chemistry_screen")
    ) { padding ->
        OrganicChemistryContent(
            onSaveToNotebook = onSaveToNotebook,
            modifier = Modifier.padding(padding)
        )
    }
}

@Composable
fun OrganicChemistryContent(
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(OrganicToolTab.REACTIONS) }

    Column(modifier = modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 16.dp,
            modifier = Modifier.fillMaxWidth().testTag("organic_tab_row")
        ) {
            OrganicToolTab.values().forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label, fontWeight = FontWeight.SemiBold) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            when (selectedTab) {
                OrganicToolTab.REACTIONS -> ReactionPredictorView(onSaveToNotebook)
                OrganicToolTab.STRUCTURES -> RdkitMoleculeViewerSection(onSaveToNotebook)
                OrganicToolTab.MECHANISMS -> MechanismsView(onSaveToNotebook)
                OrganicToolTab.GOC -> GocView(onSaveToNotebook)
                OrganicToolTab.ISOMERISM -> IsomerismView(onSaveToNotebook)
                OrganicToolTab.NAMED_REACTIONS -> NamedReactionsView(onSaveToNotebook)
                OrganicToolTab.TESTS_REAGENTS -> TestsAndReagentsView(onSaveToNotebook)
                OrganicToolTab.CALCULATIONS -> OrganicCalculationsView(onSaveToNotebook)
            }
        }
    }
}

@Composable
private fun ReactionPredictorView(onSaveToNotebook: (String, String, String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredReactions = remember(searchQuery) {
        if (searchQuery.isBlank()) OrganicChemistryEngine.REACTIONS
        else {
            val q = searchQuery.lowercase()
            OrganicChemistryEngine.REACTIONS.filter {
                it.reactant.lowercase().contains(q) ||
                        it.reagent.lowercase().contains(q) ||
                        it.majorProduct.lowercase().contains(q) ||
                        it.reactionName.lowercase().contains(q) ||
                        it.mechanismType.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("reaction_predictor_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().testTag("reaction_search_input"),
                label = { Text("Search reactant, reagent, or reaction name...") },
                placeholder = { Text("e.g. Propene, HBr, Benzene, Alc. KOH") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredReactions) { rxn ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("reaction_card_${rxn.reactionName}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = rxn.reactionName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                onSaveToNotebook(
                                    rxn.reactionName,
                                    "${rxn.reactant} + ${rxn.reagent}",
                                    rxn.majorProduct
                                )
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Notebook", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Chemical Equation:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            MathView(
                                latex = rxn.reactionEquationLatex,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Mechanism: ${rxn.mechanismType}", style = MaterialTheme.typography.labelSmall) }
                        )
                    }

                    Text("Major Product: ${rxn.majorProduct}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    if (rxn.minorProduct.isNotEmpty()) {
                        Text("Minor Product: ${rxn.minorProduct}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Text("Regiochemistry: ${rxn.regiochemistry}", style = MaterialTheme.typography.bodySmall)
                    Text("Stereochemistry: ${rxn.stereochemistry}", style = MaterialTheme.typography.bodySmall)

                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("JEE Exam Trap / Note:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelMedium)
                            Text(rxn.jeeNotes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MechanismsView(onSaveToNotebook: (String, String, String) -> Unit) {
    var expandedIndex by remember { mutableIntStateOf(0) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Detailed Reaction Mechanisms (JEE Syllabus)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Step-by-step electronic movement, intermediates, rate laws, and stereochemistry.", style = MaterialTheme.typography.bodySmall)
        }

        items(OrganicChemistryEngine.MECHANISMS.size) { idx ->
            val mech = OrganicChemistryEngine.MECHANISMS[idx]
            val isExpanded = expandedIndex == idx

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedIndex = if (isExpanded) -1 else idx }
                    .testTag("mechanism_card_${mech.name}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(mech.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                            Text("Category: ${mech.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onSaveToNotebook(mech.name, mech.rateLaw, mech.intermediate) }) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Rate Law:", style = MaterialTheme.typography.labelSmall)
                            MathView(latex = mech.rateLaw, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text("Substrate: ${mech.substrate}", style = MaterialTheme.typography.bodySmall)
                    Text("Intermediate: ${mech.intermediate}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Stereochemistry: ${mech.stereochemistry}", style = MaterialTheme.typography.bodySmall)

                    AnimatedVisibility(visible = isExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                            Text("Solvent Effect: ${mech.solventEffect}", style = MaterialTheme.typography.bodySmall)

                            Text("Step-by-Step Reaction Mechanism:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                            mech.steps.forEachIndexed { sIdx, step ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text("${sIdx + 1}", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                                    }
                                    MathView(latex = step, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                }
                            }

                            Text("Key JEE Highlights:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                            mech.jeeKeyPoints.forEach { pt ->
                                Text("• $pt", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GocView(onSaveToNotebook: (String, String, String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("General Organic Chemistry (GOC) & Electronic Effects", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Master inductive, resonance, hyperconjugation, aromaticity, and stability series for JEE.", style = MaterialTheme.typography.bodySmall)
        }

        items(OrganicChemistryEngine.GOC_CONCEPTS) { goc ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("goc_card_${goc.title}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(goc.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text(goc.summary, style = MaterialTheme.typography.bodySmall)

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Order / Priority Series:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            MathView(
                                latex = goc.orderSeriesLatex,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text("Fundamental Rules:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    goc.rules.forEach { r ->
                        Text("• $r", style = MaterialTheme.typography.bodySmall)
                    }

                    if (goc.jeeExceptions.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("High-Yield JEE Exceptions & Traps:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                                goc.jeeExceptions.forEach { ex ->
                                    Text("• $ex", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun IsomerismView(onSaveToNotebook: (String, String, String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Isomerism Toolkit (Structural & Stereoisomerism)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Tautomerism, E/Z geometrical isomerism, CIP rules, and stereoisomer counting formulas.", style = MaterialTheme.typography.bodySmall)
        }

        items(OrganicChemistryEngine.ISOMERISM_CONCEPTS) { iso ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("isomerism_card_${iso.title}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(iso.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Type: ${iso.type}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    Text(iso.definition, style = MaterialTheme.typography.bodySmall)

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Governing Formula / Equilibrium:", style = MaterialTheme.typography.labelSmall)
                            MathView(latex = iso.formulaLatex, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Text("Conditions & Criteria:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    iso.conditions.forEach { cond ->
                        Text("• $cond", style = MaterialTheme.typography.bodySmall)
                    }

                    Text("High-Yield Examples:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                    iso.examples.forEach { ex ->
                        Text("• $ex", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun NamedReactionsView(onSaveToNotebook: (String, String, String) -> Unit) {
    var search by remember { mutableStateOf("") }
    val filtered = remember(search) {
        if (search.isBlank()) OrganicChemistryEngine.NAMED_REACTIONS
        else {
            val q = search.lowercase()
            OrganicChemistryEngine.NAMED_REACTIONS.filter {
                it.name.lowercase().contains(q) ||
                        it.reactant.lowercase().contains(q) ||
                        it.reagents.lowercase().contains(q) ||
                        it.product.lowercase().contains(q)
            }
        }
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Search named reactions...") },
                placeholder = { Text("e.g. Wurtz, Clemmensen, Gabriel, HVZ") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filtered) { r ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("named_reaction_${r.name}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(r.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        IconButton(onClick = { onSaveToNotebook(r.name, "${r.reactant} + ${r.reagents}", r.product) }) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Save", tint = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Equation:", style = MaterialTheme.typography.labelSmall)
                            MathView(latex = r.equationLatex, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Text("Reactant: ${r.reactant}", style = MaterialTheme.typography.bodySmall)
                    Text("Reagents: ${r.reagents}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Product: ${r.product}", style = MaterialTheme.typography.bodySmall)
                    Text("Intermediate: ${r.keyIntermediates}", style = MaterialTheme.typography.bodySmall)

                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("JEE Traps & Significance:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(r.importantNotes, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TestsAndReagentsView(onSaveToNotebook: (String, String, String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item {
            Text("Functional Group Diagnostic Tests", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Key laboratory identification tests asked regularly in JEE Main & Advanced.", style = MaterialTheme.typography.bodySmall)
        }

        items(OrganicChemistryEngine.FUNCTIONAL_TESTS) { t ->
            Card(
                modifier = Modifier.fillMaxWidth().testTag("functional_test_${t.testName}"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(t.testName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    Text("Target Group: ${t.functionalGroup}", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium)
                    Text("Reagent: ${t.reagentUsed}", style = MaterialTheme.typography.bodySmall)

                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Reaction:", style = MaterialTheme.typography.labelSmall)
                            MathView(latex = t.equationLatex, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Text("Observation: ${t.positiveObservation}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
                    Text("JEE Notes: ${t.notes}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text("Reagent Selectivity & Exception Matrix", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        items(OrganicChemistryEngine.REAGENT_SELECTIVITIES) { r ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(r.reagent, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Role: ${r.role}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    Text("Action: ${r.functionalGroupAction}", style = MaterialTheme.typography.bodySmall)
                    Text("Exceptions & Selectivity: ${r.exceptionsAndSelectivity}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun OrganicCalculationsView(onSaveToNotebook: (String, String, String) -> Unit) {
    var subCalc by remember { mutableStateOf("COMBUSTION") }

    // Combustion state
    var sampleMassStr by remember { mutableStateOf("0.246") }
    var co2MassStr by remember { mutableStateOf("0.198") }
    var h2oMassStr by remember { mutableStateOf("0.1014") }

    // Kjeldahl state
    var kjeldahlSampleStr by remember { mutableStateOf("0.50") }
    var acidVolStr by remember { mutableStateOf("25.0") }
    var acidNormalityStr by remember { mutableStateOf("0.1") }

    // DBE state
    var carbonCount by remember { mutableIntStateOf(6) }
    var hydrogenCount by remember { mutableIntStateOf(6) }
    var nitrogenCount by remember { mutableIntStateOf(0) }
    var halogenCount by remember { mutableIntStateOf(0) }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Quantitative Organic Calculations", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Solve percentage composition, Kjeldahl, Carius, and Degree of Unsaturation (DBE).", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                FilterChip(
                    selected = subCalc == "COMBUSTION",
                    onClick = { subCalc = "COMBUSTION" },
                    label = { Text("Liebig %C, %H") }
                )
                FilterChip(
                    selected = subCalc == "KJELDAHL",
                    onClick = { subCalc = "KJELDAHL" },
                    label = { Text("Kjeldahl %N") }
                )
                FilterChip(
                    selected = subCalc == "DBE",
                    onClick = { subCalc = "DBE" },
                    label = { Text("DBE Index") }
                )
            }
        }

        when (subCalc) {
            "COMBUSTION" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = sampleMassStr,
                            onValueChange = { sampleMassStr = it },
                            label = { Text("Mass of Organic Compound (g)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = co2MassStr,
                                onValueChange = { co2MassStr = it },
                                label = { Text("Mass of CO₂ (g)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = h2oMassStr,
                                onValueChange = { h2oMassStr = it },
                                label = { Text("Mass of H₂O (g)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val sMass = sampleMassStr.toDoubleOrNull() ?: 0.246
                        val co2 = co2MassStr.toDoubleOrNull() ?: 0.198
                        val h2o = h2oMassStr.toDoubleOrNull() ?: 0.1014
                        val result = OrganicChemistryEngine.calculateCombustionAnalysis(sMass, co2, h2o)

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(result.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                MathView(latex = result.formulaLatex, fontSize = 15.sp)

                                result.steps.forEach { step ->
                                    MathView(latex = step, fontSize = 13.sp)
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result.finalAnswer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "KJELDAHL" -> {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = kjeldahlSampleStr,
                            onValueChange = { kjeldahlSampleStr = it },
                            label = { Text("Mass of Organic Compound (g)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = acidVolStr,
                                onValueChange = { acidVolStr = it },
                                label = { Text("Acid Volume Used (mL)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = acidNormalityStr,
                                onValueChange = { acidNormalityStr = it },
                                label = { Text("Normality of Acid (N)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        val sMass = kjeldahlSampleStr.toDoubleOrNull() ?: 0.50
                        val vAcid = acidVolStr.toDoubleOrNull() ?: 25.0
                        val nAcid = acidNormalityStr.toDoubleOrNull() ?: 0.1
                        val result = OrganicChemistryEngine.calculateKjeldahl(sMass, vAcid, nAcid)

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(result.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                MathView(latex = result.formulaLatex, fontSize = 15.sp)

                                result.steps.forEach { step ->
                                    MathView(latex = step, fontSize = 13.sp)
                                }

                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result.finalAnswer,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }
            "DBE" -> {
                item {
                    val dbe = OrganicChemistryEngine.calculateDegreeOfUnsaturation(carbonCount, hydrogenCount, nitrogenCount, halogenCount)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Calculate Degree of Unsaturation (Double Bond Equivalent):", fontWeight = FontWeight.SemiBold)

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = carbonCount.toString(),
                                onValueChange = { carbonCount = it.toIntOrNull() ?: 1 },
                                label = { Text("Carbons (C)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = hydrogenCount.toString(),
                                onValueChange = { hydrogenCount = it.toIntOrNull() ?: 0 },
                                label = { Text("Hydrogens (H)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nitrogenCount.toString(),
                                onValueChange = { nitrogenCount = it.toIntOrNull() ?: 0 },
                                label = { Text("Nitrogens (N)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = halogenCount.toString(),
                                onValueChange = { halogenCount = it.toIntOrNull() ?: 0 },
                                label = { Text("Halogens (X)") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Formula: DBE = C + 1 - (H + X - N) / 2", style = MaterialTheme.typography.labelSmall)
                                Text("Degree of Unsaturation (DBE) = $dbe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                                Text(
                                    when {
                                        dbe == 0 -> "Fully saturated compound (alkane, saturated alcohol, etc.). No rings or π-bonds."
                                        dbe == 1 -> "1 ring OR 1 double bond (alkene, cycloalkane, aldehyde, ketone)."
                                        dbe == 2 -> "2 rings, 2 double bonds, 1 triple bond, OR 1 ring + 1 double bond."
                                        dbe >= 4 -> "High probability of Aromatic Benzene ring (DBE = 4: 1 ring + 3 double bonds)!"
                                        else -> "Contains multiple unsaturations or rings."
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RdkitMoleculeViewerSection(onSaveToNotebook: (String, String, String) -> Unit) {
    var smilesInput by remember { mutableStateOf("c1ccccc1") }
    var selectedPresetName by remember { mutableStateOf("Benzene") }
    var isReactionMode by remember { mutableStateOf(false) }

    val moleculePresets = listOf(
        "Benzene" to "c1ccccc1",
        "Aspirin" to "CC(=O)Oc1ccccc1C(=O)O",
        "Ethanol" to "CCO",
        "Acetone" to "CC(=O)C",
        "Caffeine" to "CN1C=NC2=C1C(=O)N(C(=O)N2C)C",
        "Toluene" to "Cc1ccccc1",
        "Phenol" to "Oc1ccccc1",
        "Nitrobenzene" to "c1ccccc1[N+](=O)[O-]",
        "Aniline" to "Nc1ccccc1",
        "Glucose" to "OC[C@@H]1O[C@H](O)[C@H](O)[C@@H](O)[C@@H]1O"
    )

    val reactionPresets = listOf(
        "Esterification" to "CC(=O)O.OCC>>CC(=O)OCC.O",
        "Nitration of Benzene" to "c1ccccc1.[N+](=O)[O-]>>c1ccccc1[N+](=O)[O-]",
        "Aldol Addition" to "CC=O.CC=O>>CC(O)CC=O"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize().testTag("rdkit_molecule_viewer_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "2D Molecule & Reaction Diagram Engine (RDKit WASM)",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "High-precision chemical structure and reaction scheme rendering powered 100% offline by RDKit WebAssembly.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Mode switch: Single Molecule vs Chemical Reaction
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = !isReactionMode,
                    onClick = {
                        isReactionMode = false
                        smilesInput = "c1ccccc1"
                        selectedPresetName = "Benzene"
                    },
                    label = { Text("Molecule (SMILES)") },
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = isReactionMode,
                    onClick = {
                        isReactionMode = true
                        smilesInput = "CC(=O)O.OCC>>CC(=O)OCC.O"
                        selectedPresetName = "Esterification"
                    },
                    label = { Text("Reaction (A + B → C)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Preset Chips
        item {
            Text("Presets (JEE Syllabus):", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!isReactionMode) {
                    items(moleculePresets.size) { i ->
                        val (name, s) = moleculePresets[i]
                        SuggestionChip(
                            onClick = {
                                smilesInput = s
                                selectedPresetName = name
                            },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                } else {
                    items(reactionPresets.size) { i ->
                        val (name, s) = reactionPresets[i]
                        SuggestionChip(
                            onClick = {
                                smilesInput = s
                                selectedPresetName = name
                            },
                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }

        // SMILES Input Field
        item {
            OutlinedTextField(
                value = smilesInput,
                onValueChange = { smilesInput = it },
                label = { Text(if (isReactionMode) "Reaction SMILES (reactants>>products)" else "Molecular SMILES") },
                placeholder = { Text(if (isReactionMode) "e.g. CC(=O)O.OCC>>CC(=O)OCC.O" else "e.g. c1ccccc1, CCO, CC(=O)O") },
                modifier = Modifier.fillMaxWidth().testTag("smiles_text_field"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (smilesInput.isNotEmpty()) {
                        IconButton(onClick = { smilesInput = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )
        }

        // Primary 2D RDKit Structure Display Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isReactionMode) "Reaction Scheme" else selectedPresetName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        FilledTonalButton(
                            onClick = {
                                onSaveToNotebook(
                                    selectedPresetName,
                                    "SMILES: $smilesInput",
                                    if (isReactionMode) "Reaction Scheme" else "2D Chemical Structure"
                                )
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Save Note", fontSize = 12.sp)
                        }
                    }

                    // 2D Molecular Canvas (RDKit WebAssembly)
                    RDKitStructureView(
                        smiles = smilesInput,
                        isReaction = isReactionMode,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    )

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("SMILES String: $smilesInput", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                            Text(
                                "Generated with true 2D stereochemical projection, bond angles, heteroatom labeling, and aromatic ring detection.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

