package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.math.graph.GraphFunction
import com.example.math.graph.GraphSlider
import com.example.math.graph.GraphViewport
import com.example.ui.components.MathQuillController
import com.example.ui.components.graph.DesmosExpressionRow
import com.example.ui.components.graph.DesmosKeyboard
import com.example.ui.components.graph.GraphCanvas
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    initialExpression: String = "x^2 + y^2 = 5",
    onBack: () -> Unit,
    onSaveToNotebook: ((String, String, String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Viewport: Desmos default standard view [-10, 10] x [-10, 10]
    var viewport by remember { mutableStateOf(GraphViewport()) }

    // Color counter so every new function graph must show by another color
    var colorCounter by remember { mutableIntStateOf(1) }

    // Functions list: row 1 initialized to "x^2 + y^2 = 5" with Desmos Red
    var functions by remember {
        mutableStateOf(
            listOf(
                GraphFunction.createFromLatex("f1", initialExpression, 0)
            )
        )
    }

    // Active focused row index
    var focusedRowIndex by remember { mutableIntStateOf(0) }

    // Map of MathQuillControllers for each expression row
    val controllers = remember { mutableStateMapOf<String, MathQuillController>() }

    fun getOrCreateController(id: String): MathQuillController {
        return controllers.getOrPut(id) { MathQuillController() }
    }

    // Undo / Redo history
    val history = remember { mutableStateListOf<List<GraphFunction>>(functions) }
    var historyIndex by remember { mutableIntStateOf(0) }

    fun recordHistory(newFuncs: List<GraphFunction>) {
        while (history.size > historyIndex + 1) {
            history.removeAt(history.size - 1)
        }
        history.add(newFuncs)
        historyIndex = history.size - 1
        functions = newFuncs
    }

    // Keyboard visibility toggle
    var isKeyboardVisible by remember { mutableStateOf(true) }

    // Settings & Dialogs
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }
    var showAxes by remember { mutableStateOf(true) }
    var showTableDialog by remember { mutableStateOf(false) }
    var angleMode by remember { mutableStateOf("RAD") }

    // Sliders / Parameters (e.g. a, b)
    var sliders by remember {
        mutableStateOf(
            listOf(
                GraphSlider("a", 1.0f, -5f, 5f, 0.1f),
                GraphSlider("b", 0.0f, -5f, 5f, 0.1f)
            )
        )
    }
    val paramMap = remember(sliders) {
        sliders.associate { it.name to it.value.toDouble() }
    }

    val listState = rememberLazyListState()

    // Add expression helper: picks next distinct color from PALETTE
    val addNewExpression: () -> Unit = {
        val nextColorIndex = colorCounter
        colorCounter++
        val newId = "f_${System.currentTimeMillis()}"
        val newFunc = GraphFunction.createFromLatex(newId, "", nextColorIndex)
        val newFuncs = functions + newFunc
        recordHistory(newFuncs)
        focusedRowIndex = newFuncs.size - 1
        isKeyboardVisible = true
        getOrCreateController(newId).focus()
    }

    // Active focused controller helper
    fun getActiveController(): MathQuillController? {
        if (functions.isEmpty()) return null
        val safeIdx = focusedRowIndex.coerceIn(0, functions.size - 1)
        return getOrCreateController(functions[safeIdx].id)
    }

    // Keyboard Actions routed to MathQuill API
    val handleInsertText: (String) -> Unit = { insertText ->
        if (functions.isEmpty()) {
            addNewExpression()
        }
        getActiveController()?.insertTypedText(insertText)
    }

    val handleInsertFunction: (String) -> Unit = { fnName ->
        if (functions.isEmpty()) {
            addNewExpression()
        }
        getActiveController()?.insertFunction(fnName)
    }

    val handleBackspace: () -> Unit = {
        if (functions.isNotEmpty()) {
            val safeIndex = focusedRowIndex.coerceIn(0, functions.size - 1)
            val activeFunc = functions[safeIndex]
            if (activeFunc.expressionText.isEmpty() && functions.size > 1) {
                val updatedList = functions.toMutableList()
                updatedList.removeAt(safeIndex)
                recordHistory(updatedList)
                focusedRowIndex = (safeIndex - 1).coerceAtLeast(0)
                getOrCreateController(functions[focusedRowIndex].id).focus()
            } else {
                getActiveController()?.handleBackspace()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Folder,
                            contentDescription = "Graph",
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Untitled Graph",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("graph_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    // Save Button
                    Button(
                        onClick = {
                            Toast.makeText(context, "Graph saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp).testTag("graph_save_btn")
                    ) {
                        Text("Save", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Reset Viewport (Center)
                    IconButton(
                        onClick = { viewport = viewport.reset() },
                        modifier = Modifier.testTag("graph_reset_view_btn")
                    ) {
                        Icon(Icons.Default.CenterFocusStrong, contentDescription = "Reset View", tint = Color(0xFFE2E8F0))
                    }

                    // Table of Values
                    IconButton(
                        onClick = { showTableDialog = true },
                        modifier = Modifier.testTag("graph_table_btn")
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = "Table of Values", tint = Color(0xFFE2E8F0))
                    }

                    // Wrench / Graph Settings
                    IconButton(
                        onClick = { showSettingsSheet = true },
                        modifier = Modifier.testTag("graph_settings_btn")
                    ) {
                        Icon(Icons.Default.Build, contentDescription = "Graph Settings", tint = Color(0xFFE2E8F0))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color(0xFFE2E8F0)
                )
            )
        },
        modifier = modifier.testTag("graph_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            // ==========================================
            // TOP SECTION: GRAPH CANVAS & FLOATING TOOLS
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (isKeyboardVisible) 1.15f else 1.85f)
            ) {
                // 1. Interactive 2D Graph Canvas with Desmos Grid & Numbers on Axes
                GraphCanvas(
                    viewport = viewport,
                    functions = functions,
                    parameters = paramMap,
                    showGrid = showGrid,
                    showAxes = showAxes,
                    onViewportChange = { viewport = it },
                    modifier = Modifier.fillMaxSize()
                )

                // 2. Floating Canvas Controls (Wrench on top-right, Zoom on bottom-right)
                IconButton(
                    onClick = { showSettingsSheet = true },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        Icons.Default.Build,
                        contentDescription = "Graph Settings",
                        tint = Color(0xFF475569),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Zoom In / Out Buttons
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewport = viewport.zoom(0.8) },
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color(0xFF334155))
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewport = viewport.zoom(1.25) },
                        color = Color.White.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color(0xFF334155))
                        }
                    }
                }
            }

            HorizontalDivider(thickness = 1.dp, color = Color(0xFFE2E8F0))

            // ==========================================
            // MIDDLE SECTION: DESMOS EQUATION ROWS LIST
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp, max = if (isKeyboardVisible) 140.dp else 240.dp)
                    .background(Color.White)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(functions, key = { _, fn -> fn.id }) { idx, fn ->
                        val rowController = getOrCreateController(fn.id)
                        DesmosExpressionRow(
                            index = idx + 1,
                            function = fn,
                            isFocused = (focusedRowIndex == idx),
                            controller = rowController,
                            onRowClick = {
                                focusedRowIndex = idx
                                isKeyboardVisible = true
                                rowController.focus()
                            },
                            onToggleVisibility = {
                                val updated = functions.toMutableList()
                                updated[idx] = fn.copy(isVisible = !fn.isVisible)
                                recordHistory(updated)
                            },
                            onDelete = {
                                if (functions.size > 1) {
                                    val updated = functions.toMutableList()
                                    updated.removeAt(idx)
                                    recordHistory(updated)
                                    focusedRowIndex = (idx - 1).coerceAtLeast(0)
                                    getOrCreateController(functions[focusedRowIndex].id).focus()
                                } else {
                                    // Clear current row
                                    rowController.clear()
                                    val updated = functions.toMutableList()
                                    updated[0] = GraphFunction.createFromLatex(fn.id, "", fn.colorIndexOrDefault())
                                    recordHistory(updated)
                                }
                            },
                            onUpdateLatex = { newLatex ->
                                val updated = functions.toMutableList()
                                val newFn = GraphFunction.createFromLatex(fn.id, newLatex, fn.colorIndexOrDefault())
                                    .copy(
                                        isVisible = fn.isVisible,
                                        showDerivative = fn.showDerivative,
                                        showTangent = fn.showTangent,
                                        showIntegralArea = fn.showIntegralArea
                                    )
                                updated[idx] = newFn
                                recordHistory(updated)
                            },
                            onUpdateFunction = { updatedFn ->
                                val updated = functions.toMutableList()
                                updated[idx] = updatedFn
                                recordHistory(updated)
                            },
                            onEnter = addNewExpression
                        )
                    }

                    // Next empty prompt row indicator
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { addNewExpression() }
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${functions.size + 1}",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+ add item (expression)",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // ==========================================
            // BOTTOM SECTION: DESMOS CUSTOM KEYBOARD
            // ==========================================
            AnimatedVisibility(
                visible = isKeyboardVisible,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                DesmosKeyboard(
                    onInsertText = handleInsertText,
                    onInsertFunction = handleInsertFunction,
                    onBackspace = handleBackspace,
                    onMoveCursorLeft = { getActiveController()?.moveCursorLeft() },
                    onMoveCursorRight = { getActiveController()?.moveCursorRight() },
                    onNextSlot = { getActiveController()?.moveToNextSlot() },
                    onEnter = addNewExpression,
                    onAddExpression = addNewExpression,
                    onUndo = {
                        if (historyIndex > 0) {
                            historyIndex--
                            functions = history[historyIndex]
                            focusedRowIndex = focusedRowIndex.coerceIn(0, functions.size - 1)
                            getOrCreateController(functions[focusedRowIndex].id).setLatex(functions[focusedRowIndex].expressionText)
                        }
                    },
                    onRedo = {
                        if (historyIndex < history.size - 1) {
                            historyIndex++
                            functions = history[historyIndex]
                            focusedRowIndex = focusedRowIndex.coerceIn(0, functions.size - 1)
                            getOrCreateController(functions[focusedRowIndex].id).setLatex(functions[focusedRowIndex].expressionText)
                        }
                    },
                    onHideKeyboard = { isKeyboardVisible = false },
                    angleMode = angleMode,
                    onToggleAngleMode = {
                        angleMode = if (angleMode == "RAD") "DEG" else "RAD"
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Floating "Show Keyboard" button if keyboard is hidden
            if (!isKeyboardVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    FloatingActionButton(
                        onClick = { isKeyboardVisible = true },
                        modifier = Modifier.size(44.dp).testTag("show_keyboard_fab"),
                        containerColor = Color(0xFF2563EB),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Keyboard, contentDescription = "Show Desmos Keyboard")
                    }
                }
            }
        }
    }

    // --- GRAPH SETTINGS BOTTOM SHEET ---
    if (showSettingsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSettingsSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Graph Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Grid Lines")
                    Switch(checked = showGrid, onCheckedChange = { showGrid = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Coordinate Axes")
                    Switch(checked = showAxes, onCheckedChange = { showAxes = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Angles")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = angleMode == "RAD",
                            onClick = { angleMode = "RAD" },
                            label = { Text("Radians") }
                        )
                        FilterChip(
                            selected = angleMode == "DEG",
                            onClick = { angleMode = "DEG" },
                            label = { Text("Degrees") }
                        )
                    }
                }

                HorizontalDivider()

                Text("Default Zoom Intervals", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewport = GraphViewport(-10.0, 10.0, -10.0, 10.0)
                            showSettingsSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Standard [-10, 10]")
                    }
                    OutlinedButton(
                        onClick = {
                            viewport = GraphViewport(-2 * Math.PI, 2 * Math.PI, -2.0, 2.0)
                            showSettingsSheet = false
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Trig [-2π, 2π]")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // --- TABLE OF VALUES DIALOG ---
    if (showTableDialog) {
        val activeFunc = functions.getOrNull(focusedRowIndex.coerceIn(0, functions.size - 1))
        AlertDialog(
            onDismissRequest = { showTableDialog = false },
            title = {
                Text(
                    "Table of Values: ${activeFunc?.expressionText ?: "f(x)"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                val tablePoints = remember(activeFunc, paramMap) {
                    val pts = mutableListOf<Pair<Double, Double>>()
                    if (activeFunc?.parsedExpr != null) {
                        val vars = paramMap.toMutableMap()
                        for (i in -5..5) {
                            val x = i.toDouble()
                            vars["x"] = x
                            try {
                                val y = activeFunc.parsedExpr.eval(vars)
                                pts.add(Pair(x, y))
                            } catch (_: Exception) {}
                        }
                    }
                    pts
                }

                Column(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF1F5F9)).padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("x", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("y = f(x)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    }
                    HorizontalDivider()
                    LazyColumn {
                        items(tablePoints.size) { i ->
                            val (x, y) = tablePoints[i]
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    String.format(Locale.US, "%.1f", x),
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    if (y.isNaN()) "Undefined" else String.format(Locale.US, "%.4f", y),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTableDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun GraphFunction.colorIndexOrDefault(): Int {
    val idx = GraphFunction.PALETTE.indexOf(this.color)
    return if (idx >= 0) idx else 0
}
