package com.example.ui.screens.subject

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.subject.*
import com.example.ui.components.MathView
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCalculatorScreen(
    subject: SubjectType,
    calculators: List<CalculatorDefinition>,
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedChapter by remember { mutableStateOf<String?>(null) }
    var activeCalculator by remember { mutableStateOf(calculators.firstOrNull()) }
    var favoriteIds by remember { mutableStateOf(setOf<String>()) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    val chapters = remember(calculators) {
        listOf("All") + calculators.map { it.chapter }.distinct()
    }

    val filteredCalculators = remember(calculators, searchQuery, selectedChapter, showOnlyFavorites, favoriteIds) {
        calculators.filter { calc ->
            val matchesChapter = selectedChapter == null || selectedChapter == "All" || calc.chapter == selectedChapter
            val matchesFav = !showOnlyFavorites || favoriteIds.contains(calc.id)
            if (!matchesChapter || !matchesFav) return@filter false

            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return@filter true

            calc.name.lowercase().contains(q) ||
                    calc.topic.lowercase().contains(q) ||
                    calc.chapter.lowercase().contains(q) ||
                    calc.description.lowercase().contains(q) ||
                    calc.searchKeywords.any { it.lowercase().contains(q) }
        }
    }

    // Keep active calculator synced
    LaunchedEffect(filteredCalculators) {
        if (activeCalculator == null || !filteredCalculators.contains(activeCalculator)) {
            activeCalculator = filteredCalculators.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${subject.displayName} Calculators", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Text("CALCX • Made by Aman Prasad", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("subject_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    FilledTonalIconToggleButton(
                        checked = showOnlyFavorites,
                        onCheckedChange = { showOnlyFavorites = it },
                        modifier = Modifier.testTag("fav_filter_button")
                    ) {
                        Icon(
                            if (showOnlyFavorites) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorites",
                            tint = if (showOnlyFavorites) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("subject_calc_screen_${subject.name.lowercase()}")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search ${subject.displayName} formulas & topics...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("subject_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Chapter Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            ) {
                items(chapters) { ch ->
                    FilterChip(
                        selected = (selectedChapter == null && ch == "All") || selectedChapter == ch,
                        onClick = { selectedChapter = if (ch == "All") null else ch },
                        label = { Text(ch, maxLines = 1) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Calculator Selector (Horizontal Scroll if multiple)
            if (filteredCalculators.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    items(filteredCalculators) { calc ->
                        val isSelected = activeCalculator?.id == calc.id
                        InputChip(
                            selected = isSelected,
                            onClick = { activeCalculator = calc },
                            label = { Text(calc.name, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = if (favoriteIds.contains(calc.id)) {
                                { Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(14.dp)) }
                            } else null
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Main Active Calculator Form
            val currentCalc = activeCalculator
            if (currentCalc != null) {
                val isFav = favoriteIds.contains(currentCalc.id)
                CalculatorFormView(
                    calculator = currentCalc,
                    isFavorite = isFav,
                    onToggleFavorite = {
                        favoriteIds = if (isFav) favoriteIds - currentCalc.id else favoriteIds + currentCalc.id
                    },
                    onSaveToNotebook = onSaveToNotebook
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Text("No matching calculators found", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.outline)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorFormView(
    calculator: CalculatorDefinition,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    val inputValues = remember(calculator.id) {
        mutableStateMapOf<String, String>().apply {
            calculator.inputs.forEach { put(it.id, it.defaultValue) }
        }
    }

    var resultState by remember(calculator.id) { mutableStateOf<CalculationExecutionResult?>(null) }
    var errorMessage by remember(calculator.id) { mutableStateOf<String?>(null) }
    var showSteps by remember(calculator.id) { mutableStateOf(true) }

    fun runCalculation() {
        errorMessage = null
        val res = calculator.execute(inputValues.toMap(), emptyMap())
        res.onSuccess {
            resultState = it
        }.onFailure {
            errorMessage = it.message ?: "Calculation error occurred"
            resultState = null
        }
    }

    // Auto-calculate initial defaults
    LaunchedEffect(calculator.id) {
        runCalculation()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header & Description Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(calculator.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("${calculator.chapter} • ${calculator.topic}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = onToggleFavorite) {
                            Icon(
                                if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(calculator.description, style = MaterialTheme.typography.bodySmall)

                    // Formula Box
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.padding(12.dp)) {
                            MathView(
                                latex = calculator.formulaLatex,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Input Fields Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Given Inputs:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

                    calculator.inputs.forEach { field ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = inputValues[field.id] ?: "",
                                onValueChange = { inputValues[field.id] = it },
                                label = { Text(field.label) },
                                placeholder = { if (field.placeholder.isNotEmpty()) Text(field.placeholder) },
                                supportingText = if (field.description.isNotEmpty()) {
                                    { Text(field.description) }
                                } else null,
                                trailingIcon = if (field.unit.isNotEmpty()) {
                                    { Text(field.unit, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(end = 12.dp), color = MaterialTheme.colorScheme.primary) }
                                } else null,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("field_${field.id}")
                            )
                        }
                    }

                    Button(
                        onClick = { runCalculation() },
                        modifier = Modifier.fillMaxWidth().testTag("calculate_action_button")
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Calculate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Error Banner
        if (errorMessage != null) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Text(errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }

        // Result Card
        val curResult = resultState
        if (curResult != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("calculation_result_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Calculated Result:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            if (curResult.isVerified) {
                                Surface(
                                    color = Color(0xFF2E7D32).copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                                        Text("Verified", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Exact LaTeX Output
                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                MathView(
                                    latex = curResult.exactResultLatex,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Text(
                            "Numerical Output: ${curResult.numericResult} ${curResult.unit}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (curResult.verificationNote.isNotEmpty()) {
                            Text(
                                "✓ ${curResult.verificationNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        // Action Buttons: Copy LaTeX, Copy Plain, Save to Notebook
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                        ) {
                            FilledTonalButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("LaTeX Result", curResult.exactResultLatex))
                                    Toast.makeText(context, "Copied LaTeX", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy LaTeX", style = MaterialTheme.typography.labelSmall)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Plain Result", "${calculator.name}: ${curResult.numericResult} ${curResult.unit}"))
                                    Toast.makeText(context, "Copied Plain Text", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.TextSnippet, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Plain", style = MaterialTheme.typography.labelSmall)
                            }

                            Button(
                                onClick = {
                                    onSaveToNotebook(calculator.name, curResult.formulaLatex, "${curResult.numericResult} ${curResult.unit}")
                                    Toast.makeText(context, "Saved to Notebook", Toast.LENGTH_SHORT).show()
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Embedded Curve Plotter if available
            val pts = curResult.graphPoints
            if (pts != null && pts.size > 2) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Trajectory / Curve Plot:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                CurveCanvas(points = pts, xLabel = curResult.graphXLabel, yLabel = curResult.graphYLabel)
                            }
                        }
                    }
                }
            }

            // Step by Step Detailed Breakdown
            if (curResult.steps.isNotEmpty()) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { showSteps = !showSteps },
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Step-by-Step Solution Breakdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                                Icon(if (showSteps) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
                            }

                            AnimatedVisibility(visible = showSteps) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    curResult.steps.forEach { step ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(step.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                                                MathView(
                                                    latex = step.latex,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (step.explanation.isNotEmpty()) {
                                                    Text(step.explanation, style = MaterialTheme.typography.bodySmall)
                                                }
                                            }
                                        }
                                    }
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
private fun CurveCanvas(
    points: List<Pair<Double, Double>>,
    xLabel: String,
    yLabel: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val pad = 24f

        val minX = points.minOf { it.first }
        val maxX = points.maxOf { it.first }
        val minY = min(0.0, points.minOf { it.second })
        val maxY = max(1e-6, points.maxOf { it.second })

        val spanX = if (abs(maxX - minX) < 1e-9) 1.0 else (maxX - minX)
        val spanY = if (abs(maxY - minY) < 1e-9) 1.0 else (maxY - minY)

        // Draw grid
        drawLine(gridColor, Offset(pad, h - pad), Offset(w - pad, h - pad), strokeWidth = 2f)
        drawLine(gridColor, Offset(pad, pad), Offset(pad, h - pad), strokeWidth = 2f)

        // Draw curve path
        val path = Path()
        points.forEachIndexed { idx, pt ->
            val screenX = pad + ((pt.first - minX) / spanX).toFloat() * (w - 2 * pad)
            val screenY = (h - pad) - ((pt.second - minY) / spanY).toFloat() * (h - 2 * pad)
            if (idx == 0) {
                path.moveTo(screenX, screenY)
            } else {
                path.lineTo(screenX, screenY)
            }
        }

        drawPath(
            path = path,
            color = primaryColor,
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )
    }
}
