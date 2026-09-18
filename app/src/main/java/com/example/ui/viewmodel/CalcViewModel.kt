package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.HistoryEntity
import com.example.data.local.entity.NotebookEntity
import com.example.data.repository.CalcRepository
import com.example.math.AngleMode
import com.example.math.ExpressionEvaluator
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.DarkModeSetting
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class CalculatorUiState(
    val expression: String = "",
    val resultPreview: String = "0",
    val exactResult: String = "",
    val isExactMode: Boolean = false,
    val angleMode: AngleMode = AngleMode.DEG,
    val memory: Double = 0.0,
    val isScientificExpanded: Boolean = true,
    val precision: Int = 8,
    val jeeModeEnabled: Boolean = false,
    val hapticEnabled: Boolean = true,
    val appTheme: AppThemeSetting = AppThemeSetting.GRAPHITE,
    val darkMode: DarkModeSetting = DarkModeSetting.DARK,
    val errorMessage: String? = null,
    val previousCalculation: String? = null,
    val isResultJustEvaluated: Boolean = false
)

class CalcViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CalcRepository = CalcRepository(AppDatabase.getDatabase(application))
    private val evaluator = ExpressionEvaluator(AngleMode.DEG)

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    val historyFlow: StateFlow<List<HistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notesFlow: StateFlow<List<NotebookEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onKeyInput(input: String) {
        _uiState.update { current ->
            val isOperator = input in "+-×*÷/^%"
            val newExpr = when {
                current.isResultJustEvaluated -> {
                    if (isOperator) {
                        // Continue calculating with previous result
                        current.expression + input
                    } else {
                        // Replace previous result with new input
                        input
                    }
                }
                current.expression == "0" && !isOperator -> input
                else -> current.expression + input
            }
            current.copy(
                expression = newExpr,
                errorMessage = null,
                isResultJustEvaluated = false
            )
        }
        evaluatePreview()
    }

    fun onBackspace() {
        _uiState.update { current ->
            if (current.isResultJustEvaluated) {
                current.copy(isResultJustEvaluated = false)
            } else if (current.expression.isNotEmpty()) {
                val newExpr = current.expression.dropLast(1)
                current.copy(expression = newExpr, errorMessage = null)
            } else current
        }
        evaluatePreview()
    }

    fun onClear() {
        _uiState.update {
            it.copy(
                expression = "",
                resultPreview = "0",
                exactResult = "",
                errorMessage = null,
                previousCalculation = null,
                isResultJustEvaluated = false
            )
        }
    }

    fun onToggleSign() {
        val expr = _uiState.value.expression
        if (expr.isEmpty()) return
        if (expr.startsWith("-")) {
            _uiState.update { it.copy(expression = expr.removePrefix("-"), isResultJustEvaluated = false) }
        } else {
            _uiState.update { it.copy(expression = "-($expr)", isResultJustEvaluated = false) }
        }
        evaluatePreview()
    }

    fun onToggleAngleMode() {
        _uiState.update { current ->
            val nextMode = when (current.angleMode) {
                AngleMode.DEG -> AngleMode.RAD
                AngleMode.RAD -> AngleMode.GRAD
                AngleMode.GRAD -> AngleMode.DEG
            }
            evaluator.angleMode = nextMode
            current.copy(angleMode = nextMode)
        }
        evaluatePreview()
    }

    fun onToggleExactMode() {
        _uiState.update { it.copy(isExactMode = !it.isExactMode) }
    }

    fun onToggleScientificExpanded() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    fun onEquals() {
        val current = _uiState.value
        val originalExpr = current.expression.trim()
        if (originalExpr.isEmpty()) return

        evaluator.angleMode = current.angleMode
        val res = evaluator.evaluate(originalExpr)

        if (res.isSuccess) {
            val formatted = res.formattedDecimal(current.precision)
            val exact = res.exactText
            val chosenResult = if (current.isExactMode && exact.isNotEmpty()) exact else formatted

            _uiState.update {
                it.copy(
                    expression = chosenResult,
                    resultPreview = chosenResult,
                    exactResult = exact,
                    previousCalculation = originalExpr,
                    isResultJustEvaluated = true,
                    errorMessage = null
                )
            }
            // Persist to Room
            viewModelScope.launch {
                repository.addHistory(
                    expression = originalExpr,
                    result = chosenResult,
                    category = if (current.jeeModeEnabled) "JEE" else "General",
                    angleMode = current.angleMode.name
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    resultPreview = "Error",
                    errorMessage = res.error ?: "Invalid expression"
                )
            }
        }
    }

    private fun evaluatePreview() {
        val expr = _uiState.value.expression.trim()
        if (expr.isEmpty()) {
            _uiState.update { it.copy(resultPreview = "0", exactResult = "", errorMessage = null) }
            return
        }
        evaluator.angleMode = _uiState.value.angleMode
        val res = evaluator.evaluate(expr)
        if (res.isSuccess) {
            val dec = res.formattedDecimal(_uiState.value.precision)
            val exact = res.exactText
            _uiState.update {
                it.copy(
                    resultPreview = dec,
                    exactResult = exact,
                    errorMessage = null
                )
            }
        }
    }

    // Memory Functions: MC, MR, M+, M-, MS
    fun onMemoryClear() = _uiState.update { it.copy(memory = 0.0) }

    fun onMemoryRecall() {
        val mem = _uiState.value.memory
        val str = if (mem % 1.0 == 0.0) mem.toLong().toString() else mem.toString()
        onKeyInput(str)
    }

    fun onMemoryAdd() {
        val currVal = _uiState.value.resultPreview.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(memory = it.memory + currVal) }
    }

    fun onMemorySubtract() {
        val currVal = _uiState.value.resultPreview.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(memory = it.memory - currVal) }
    }

    fun onMemoryStore() {
        val currVal = _uiState.value.resultPreview.toDoubleOrNull() ?: 0.0
        _uiState.update { it.copy(memory = currVal) }
    }

    fun reuseHistory(item: HistoryEntity) {
        _uiState.update {
            it.copy(
                expression = item.expression,
                resultPreview = item.result,
                errorMessage = null
            )
        }
    }

    fun toggleFavoriteHistory(item: HistoryEntity) {
        viewModelScope.launch {
            repository.toggleFavoriteHistory(item)
        }
    }

    fun deleteHistory(item: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteHistory(item)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun addNote(title: String, calculationText: String, result: String, note: String, category: String) {
        viewModelScope.launch {
            repository.addNote(title, calculationText, result, note, category)
        }
    }

    fun deleteNote(note: NotebookEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }

    fun setAppTheme(theme: AppThemeSetting) {
        _uiState.update { it.copy(appTheme = theme) }
    }

    fun setDarkMode(mode: DarkModeSetting) {
        _uiState.update { it.copy(darkMode = mode) }
    }

    fun setPrecision(p: Int) {
        _uiState.update { it.copy(precision = p) }
        evaluatePreview()
    }

    fun toggleJeeMode() {
        _uiState.update { it.copy(jeeModeEnabled = !it.jeeModeEnabled) }
    }

    fun toggleHaptic() {
        _uiState.update { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun searchHistory(query: String): Flow<List<HistoryEntity>> = repository.searchHistory(query)
    fun searchNotes(query: String): Flow<List<NotebookEntity>> = repository.searchNotes(query)
}
