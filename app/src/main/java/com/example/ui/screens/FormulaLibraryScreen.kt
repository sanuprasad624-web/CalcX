package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.formula.CanonicalFormula
import com.example.math.formula.FormulaSubject
import com.example.math.formula.JeeFormulaDatabase
import com.example.math.formula.JeeLevel
import com.example.ui.components.MathView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormulaLibraryScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    onUseInCalculator: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectTab by remember { mutableStateOf("All") }
    var selectedChapter by remember { mutableStateOf<String?>(null) }
    var selectedJeeLevel by remember { mutableStateOf<JeeLevel?>(null) }
    var isRevisionMode by remember { mutableStateOf(false) }
    var favoriteFormulaIds by remember { mutableStateOf(setOf<String>()) }

    val subjectTabs = listOf("All", "Mathematics", "Physics", "Chemistry", "Favorites")

    // Filtered list
    val subjectEnum = when (selectedSubjectTab) {
        "Mathematics" -> FormulaSubject.MATHEMATICS
        "Physics" -> FormulaSubject.PHYSICS
        "Chemistry" -> FormulaSubject.CHEMISTRY
        else -> null
    }

    val baseFormulas = remember(selectedSubjectTab, favoriteFormulaIds) {
        if (selectedSubjectTab == "Favorites") {
            JeeFormulaDatabase.allFormulas.filter { favoriteFormulaIds.contains(it.id) }
        } else {
            JeeFormulaDatabase.allFormulas
        }
    }

    val availableChapters = remember(selectedSubjectTab, baseFormulas) {
        val list = if (subjectEnum != null) {
            baseFormulas.filter { it.subject == subjectEnum }
        } else {
            baseFormulas
        }
        list.map { it.chapter }.distinct()
    }

    val filteredFormulas = remember(searchQuery, selectedSubjectTab, selectedChapter, selectedJeeLevel, favoriteFormulaIds) {
        baseFormulas.filter { formula ->
            val matchesSubject = (subjectEnum == null || formula.subject == subjectEnum)
            val matchesChapter = (selectedChapter == null || formula.chapter == selectedChapter)
            val matchesJee = (selectedJeeLevel == null || formula.jeeLevel == selectedJeeLevel || formula.jeeLevel == JeeLevel.BOTH)

            if (!matchesSubject || !matchesChapter || !matchesJee) return@filter false

            val q = searchQuery.trim().lowercase()
            if (q.isEmpty()) return@filter true

            formula.name.lowercase().contains(q) ||
                    formula.chapter.lowercase().contains(q) ||
                    formula.topic.lowercase().contains(q) ||
                    formula.latex.lowercase().contains(q) ||
                    formula.searchKeywords.any { it.lowercase().contains(q) } ||
                    formula.tags.any { it.lowercase().contains(q) } ||
                    formula.variables.any { it.symbol.lowercase().contains(q) || it.name.lowercase().contains(q) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("JEE Formula Handbook", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${filteredFormulas.size} formulas • Class 11 & 12 Complete",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("formula_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Quick Revision Mode Toggle
                    FilledTonalIconToggleButton(
                        checked = isRevisionMode,
                        onCheckedChange = { isRevisionMode = it },
                        modifier = Modifier.testTag("revision_mode_toggle")
                    ) {
                        Icon(
                            if (isRevisionMode) Icons.Default.FlashOn else Icons.Default.MenuBook,
                            contentDescription = "Quick Revision Mode"
                        )
                    }
                }
            )
        },
        modifier = modifier.testTag("formula_library_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                placeholder = { Text("Search formulas, variables, topics, constants...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("formula_search_input")
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Navigation Tabs
            ScrollableTabRow(
                selectedTabIndex = subjectTabs.indexOf(selectedSubjectTab),
                edgePadding = 0.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                subjectTabs.forEach { tabName ->
                    Tab(
                        selected = selectedSubjectTab == tabName,
                        onClick = {
                            selectedSubjectTab = tabName
                            selectedChapter = null
                        },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (tabName == "Favorites") {
                                    Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                                Text(tabName, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips (JEE Level & Chapters)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                // Level Filter
                item {
                    FilterChip(
                        selected = selectedJeeLevel == null,
                        onClick = { selectedJeeLevel = null },
                        label = { Text("All Levels") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedJeeLevel == JeeLevel.MAIN,
                        onClick = { selectedJeeLevel = if (selectedJeeLevel == JeeLevel.MAIN) null else JeeLevel.MAIN },
                        label = { Text("JEE Main") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedJeeLevel == JeeLevel.ADVANCED,
                        onClick = { selectedJeeLevel = if (selectedJeeLevel == JeeLevel.ADVANCED) null else JeeLevel.ADVANCED },
                        label = { Text("JEE Advanced") }
                    )
                }

                // Chapter filter chips
                if (availableChapters.size > 1) {
                    item {
                        VerticalDivider(modifier = Modifier.height(24.dp).padding(horizontal = 4.dp))
                    }
                    items(availableChapters) { chapter ->
                        FilterChip(
                            selected = selectedChapter == chapter,
                            onClick = { selectedChapter = if (selectedChapter == chapter) null else chapter },
                            label = { Text(chapter, maxLines = 1) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Revision mode banner
            if (isRevisionMode) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            "Quick Revision Mode: Concise formula flashcards for rapid exam recall",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Formula List
            if (filteredFormulas.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                        Text(
                            if (selectedSubjectTab == "Favorites") "No favorite formulas saved yet" else "No matching formulas found",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(filteredFormulas, key = { it.id }) { formula ->
                        val isFav = favoriteFormulaIds.contains(formula.id)
                        CanonicalFormulaCard(
                            formula = formula,
                            isFavorite = isFav,
                            isRevisionMode = isRevisionMode,
                            onToggleFavorite = {
                                favoriteFormulaIds = if (isFav) {
                                    favoriteFormulaIds - formula.id
                                } else {
                                    favoriteFormulaIds + formula.id
                                }
                            },
                            onCopy = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Formula", "${formula.name}: ${formula.latex}"))
                                Toast.makeText(context, "Copied LaTeX to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onSave = {
                                onSaveToNotebook(formula.name, formula.latex, formula.explanation)
                                Toast.makeText(context, "Saved to Notebook", Toast.LENGTH_SHORT).show()
                            },
                            onUseInCalculator = {
                                onUseInCalculator?.invoke(formula.latex)
                                Toast.makeText(context, "Formula sent to Calculator", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CanonicalFormulaCard(
    formula: CanonicalFormula,
    isFavorite: Boolean,
    isRevisionMode: Boolean,
    onToggleFavorite: () -> Unit,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onUseInCalculator: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(!isRevisionMode) }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("formula_card_${formula.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(formula.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                "${formula.subject.displayName} • ${formula.chapter}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                formula.jeeLevel.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(36.dp)) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (isFavorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy formula", modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onSave, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save to Notebook", modifier = Modifier.size(20.dp))
                    }
                }
            }

            // MathView Display Canvas
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().clickable { isExpanded = !isExpanded }
            ) {
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                    MathView(
                        latex = formula.latex,
                        fontSize = if (isRevisionMode) 17.sp else 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Collapsible Details Section
            AnimatedVisibility(visible = isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                    // Variables list
                    if (formula.variables.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Variables & Parameters:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            formula.variables.forEach { v ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("•", style = MaterialTheme.typography.bodySmall)
                                    MathView(
                                        latex = v.symbol,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        ": ${v.name}${if (v.unit.isNotEmpty()) " [${v.unit}]" else ""}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    // Conditions / Limitations
                    if (formula.conditions.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Conditions: ", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formula.conditions, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Common Mistake Alert
                    if (formula.commonMistake.isNotEmpty()) {
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                                )
                                Column {
                                    Text("Common JEE Mistake:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                    Text(formula.commonMistake, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                                }
                            }
                        }
                    }

                    // Explanation & Example
                    if (formula.explanation.isNotEmpty()) {
                        Text(formula.explanation, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Interactive Actions Footer
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = onUseInCalculator,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Use in Calculator", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
