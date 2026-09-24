package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entity.HistoryEntity
import com.example.ui.screens.*
import com.example.ui.theme.CalcxTheme
import com.example.ui.viewmodel.CalcViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val viewModel: CalcViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val historyList by viewModel.historyFlow.collectAsStateWithLifecycle()
            val notesList by viewModel.notesFlow.collectAsStateWithLifecycle()

            var showSplash by remember { mutableStateOf(true) }
            var currentScreen by remember { mutableStateOf(CalcScreenDestination.CALCULATOR) }
            var graphInitialExpression by remember { mutableStateOf("x^2 - 4") }

            // Handle hardware or gesture back button when in any sub-tool screen
            BackHandler(enabled = !showSplash && currentScreen != CalcScreenDestination.CALCULATOR) {
                currentScreen = CalcScreenDestination.CALCULATOR
            }

            CalcxTheme(
                themeSetting = uiState.appTheme,
                darkModeSetting = uiState.darkMode
            ) {
                Crossfade(
                    targetState = showSplash,
                    label = "SplashScreenTransition"
                ) { isSplash ->
                    if (isSplash) {
                        CalcxSplashScreen(onDismiss = { showSplash = false })
                    } else {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            modifier = Modifier.fillMaxSize(),
                            label = "ScreenNavigation"
                        ) { screen ->
                    when (screen) {
                        CalcScreenDestination.CALCULATOR -> {
                            CalculatorScreen(
                                uiState = uiState,
                                onKeyInput = { viewModel.onKeyInput(it) },
                                onBackspace = { viewModel.onBackspace() },
                                onClear = { viewModel.onClear() },
                                onToggleSign = { viewModel.onToggleSign() },
                                onEquals = { viewModel.onEquals() },
                                onToggleAngleMode = { viewModel.onToggleAngleMode() },
                                onToggleExactMode = { viewModel.onToggleExactMode() },
                                onToggleScientific = { viewModel.onToggleScientificExpanded() },
                                onMemoryClear = { viewModel.onMemoryClear() },
                                onMemoryRecall = { viewModel.onMemoryRecall() },
                                onMemoryAdd = { viewModel.onMemoryAdd() },
                                onMemorySubtract = { viewModel.onMemorySubtract() },
                                onMemoryStore = { viewModel.onMemoryStore() },
                                onNavigate = { dest -> currentScreen = dest },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = if (uiState.jeeModeEnabled) "JEE" else "General"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.GRAPH -> {
                            GraphScreen(
                                initialExpression = graphInitialExpression,
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Graph"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.MATHEMATICS -> {
                            MathematicsScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Mathematics"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.CALCULUS -> {
                            CalculusScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onOpenInGrapher = { expr ->
                                    graphInitialExpression = expr
                                    currentScreen = CalcScreenDestination.GRAPH
                                },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Calculus"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.JEE_TOOLKIT -> {
                            JeeToolkitScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "JEE"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.PHYSICS -> {
                            PhysicsScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Physics"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.CHEMISTRY -> {
                            ChemistryScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Chemistry"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.ORGANIC_CHEM -> {
                            OrganicChemistryScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Organic Chem"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.UNIT_CONVERTER -> {
                            UnitConverterScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Engineering"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.MATRIX -> {
                            MatrixScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Mathematics"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.PROGRAMMER -> {
                            ProgrammerScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
                            )
                        }

                        CalcScreenDestination.STATISTICS -> {
                            StatisticsScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
                            )
                        }

                        CalcScreenDestination.FINANCE -> {
                            FinanceScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Finance"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.DATETIME -> {
                            DateTimeScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
                            )
                        }

                        CalcScreenDestination.FORMULAS -> {
                            FormulaLibraryScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Formula"
                                    )
                                },
                                onUseInCalculator = { formulaText ->
                                    viewModel.onKeyInput(formulaText)
                                    currentScreen = CalcScreenDestination.CALCULATOR
                                }
                            )
                        }

                        CalcScreenDestination.CONSTANTS -> {
                            ConstantsScreen(
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR },
                                onInsertToCalculator = { constantVal ->
                                    viewModel.onKeyInput(constantVal)
                                    currentScreen = CalcScreenDestination.CALCULATOR
                                },
                                onSaveToNotebook = { title, calc, res ->
                                    viewModel.addNote(
                                        title = title,
                                        calculationText = calc,
                                        result = res,
                                        note = "",
                                        category = "Physics"
                                    )
                                }
                            )
                        }

                        CalcScreenDestination.NOTEBOOK -> {
                            NotebookScreen(
                                notes = notesList,
                                onAddNote = { title, calc, res, note, cat ->
                                    viewModel.addNote(title, calc, res, note, cat)
                                },
                                onDeleteNote = { note ->
                                    viewModel.deleteNote(note)
                                },
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
                            )
                        }

                        CalcScreenDestination.HISTORY -> {
                            HistoryScreen(
                                historyList = historyList,
                                onReuse = { item: HistoryEntity ->
                                    viewModel.reuseHistory(item)
                                    currentScreen = CalcScreenDestination.CALCULATOR
                                },
                                onToggleFavorite = { item: HistoryEntity ->
                                    viewModel.toggleFavoriteHistory(item)
                                },
                                onDelete = { item: HistoryEntity ->
                                    viewModel.deleteHistory(item)
                                },
                                onClearAll = {
                                    viewModel.clearAllHistory()
                                },
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
                            )
                        }

                        CalcScreenDestination.SETTINGS -> {
                            SettingsScreen(
                                currentTheme = uiState.appTheme,
                                onThemeChange = { viewModel.setAppTheme(it) },
                                currentDarkMode = uiState.darkMode,
                                onDarkModeChange = { viewModel.setDarkMode(it) },
                                precision = uiState.precision,
                                onPrecisionChange = { viewModel.setPrecision(it) },
                                jeeModeEnabled = uiState.jeeModeEnabled,
                                onToggleJeeMode = { viewModel.toggleJeeMode() },
                                hapticEnabled = uiState.hapticEnabled,
                                onToggleHaptic = { viewModel.toggleHaptic() },
                                onBack = { currentScreen = CalcScreenDestination.CALCULATOR }
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

@Composable
fun CalcxSplashScreen(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) {
        delay(1200)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onDismiss)
            .padding(32.dp)
            .testTag("calcx_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shadowElevation = 6.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "∫dx",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "CALCX",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Calculate Anything.\nUnderstand Everything.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "Made by Aman Prasad",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                )
            }
        }
    }
}
