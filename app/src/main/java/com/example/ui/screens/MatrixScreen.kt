package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.Matrix

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatrixScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var size by remember { mutableStateOf(2) } // 2 for 2x2, 3 for 3x3

    // Matrix A and B state grids
    var matrixA by remember(size) {
        mutableStateOf(
            if (size == 2) arrayOf(arrayOf("1", "2"), arrayOf("3", "4"))
            else arrayOf(arrayOf("1", "2", "3"), arrayOf("0", "1", "4"), arrayOf("5", "6", "0"))
        )
    }

    var matrixB by remember(size) {
        mutableStateOf(
            if (size == 2) arrayOf(arrayOf("2", "0"), arrayOf("1", "2"))
            else arrayOf(arrayOf("1", "0", "0"), arrayOf("0", "1", "0"), arrayOf("0", "0", "1"))
        )
    }

    var scalarK by remember { mutableStateOf("2") }
    var operationTitle by remember { mutableStateOf<String?>(null) }
    var resultMatrix by remember { mutableStateOf<Matrix?>(null) }
    var scalarResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun parseMatrix(grid: Array<Array<String>>): Matrix {
        val data = Array(size) { r ->
            DoubleArray(size) { c ->
                grid[r][c].toDoubleOrNull() ?: 0.0
            }
        }
        return Matrix(size, size, data)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Matrix Calculator ($size×$size)", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("matrix_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("matrix_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dimension Toggle (2x2 or 3x3)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Matrix Size:", fontWeight = FontWeight.SemiBold)
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = size == 2,
                            onClick = { size = 2; resultMatrix = null; scalarResult = null; errorMessage = null },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                        ) {
                            Text("2 × 2", fontWeight = FontWeight.Bold)
                        }
                        SegmentedButton(
                            selected = size == 3,
                            onClick = { size = 3; resultMatrix = null; scalarResult = null; errorMessage = null },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                        ) {
                            Text("3 × 3", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Matrix A Input Grid
            item {
                MatrixInputCard(
                    title = "Matrix A",
                    size = size,
                    grid = matrixA,
                    onCellChanged = { r, c, v ->
                        val copy = Array(size) { row -> matrixA[row].clone() }
                        copy[r][c] = v
                        matrixA = copy
                    }
                )
            }

            // Operations on Matrix A
            item {
                Text("Operations on Matrix A:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            try {
                                val a = parseMatrix(matrixA)
                                val det = a.determinant()
                                operationTitle = "Determinant |A|"
                                scalarResult = String.format("%.4f", det).trimEnd('0').trimEnd('.')
                                resultMatrix = null
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = e.message
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_det_a")
                    ) {
                        Text("det(A)", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            try {
                                val a = parseMatrix(matrixA)
                                operationTitle = "Inverse A⁻¹"
                                resultMatrix = a.inverse()
                                scalarResult = null
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = e.message ?: "Singular matrix (det=0)"
                                resultMatrix = null
                                scalarResult = null
                            }
                        },
                        modifier = Modifier.weight(1f).testTag("btn_inv_a")
                    ) {
                        Text("A⁻¹", fontWeight = FontWeight.Bold)
                    }

                    FilledTonalButton(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            operationTitle = "Transpose Aᵀ"
                            resultMatrix = a.transpose()
                            scalarResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).testTag("btn_trans_a")
                    ) {
                        Text("Aᵀ", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val tr = a.trace()
                            operationTitle = "Trace tr(A)"
                            scalarResult = String.format("%.4f", tr).trimEnd('0').trimEnd('.')
                            resultMatrix = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("tr(A)")
                    }

                    FilledTonalButton(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val r = a.rank()
                            operationTitle = "Rank(A)"
                            scalarResult = r.toString()
                            resultMatrix = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("rank(A)")
                    }

                    FilledTonalButton(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val k = scalarK.toDoubleOrNull() ?: 1.0
                            operationTitle = "$k × A"
                            resultMatrix = a * k
                            scalarResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("k × A")
                    }
                }
            }

            // Matrix B Input Grid
            item {
                MatrixInputCard(
                    title = "Matrix B",
                    size = size,
                    grid = matrixB,
                    onCellChanged = { r, c, v ->
                        val copy = Array(size) { row -> matrixB[row].clone() }
                        copy[r][c] = v
                        matrixB = copy
                    }
                )
            }

            // Combined Operations (A & B)
            item {
                Text("Binary Operations (A & B):", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val b = parseMatrix(matrixB)
                            operationTitle = "A + B"
                            resultMatrix = a + b
                            scalarResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).testTag("btn_mat_add")
                    ) {
                        Text("A + B", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val b = parseMatrix(matrixB)
                            operationTitle = "A - B"
                            resultMatrix = a - b
                            scalarResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).testTag("btn_mat_sub")
                    ) {
                        Text("A - B", fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val a = parseMatrix(matrixA)
                            val b = parseMatrix(matrixB)
                            operationTitle = "A × B"
                            resultMatrix = a * b
                            scalarResult = null
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f).testTag("btn_mat_mul")
                    ) {
                        Text("A × B", fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Error Display
            errorMessage?.let { err ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = "Error: $err",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Result Display Card
            if (resultMatrix != null || scalarResult != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Result: ${operationTitle ?: ""}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.titleMedium
                            )

                            scalarResult?.let { sRes ->
                                Text(
                                    text = sRes,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            resultMatrix?.let { mat ->
                                MatrixResultGrid(matrix = mat)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = {
                                        val resStr = scalarResult ?: resultMatrix?.let { mat ->
                                            mat.data.joinToString("\n") { row ->
                                                row.joinToString("  ") { String.format("%.4f", it).trimEnd('0').trimEnd('.') }
                                            }
                                        } ?: ""
                                        onSaveToNotebook("Matrix ${operationTitle ?: "Calc"}", resStr, "Size: $size×$size")
                                        Toast.makeText(context, "Saved to Notebook!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Save to Notebook")
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
private fun MatrixInputCard(
    title: String,
    size: Int,
    grid: Array<Array<String>>,
    onCellChanged: (Int, Int, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            for (r in 0 until size) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (c in 0 until size) {
                        OutlinedTextField(
                            value = grid[r][c],
                            onValueChange = { onCellChanged(r, c, it) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixResultGrid(matrix: Matrix) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (r in 0 until matrix.rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (c in 0 until matrix.cols) {
                    val v = matrix.data[r][c]
                    val str = if (Math.abs(v) < 1e-12) "0" else String.format("%.4f", v).trimEnd('0').trimEnd('.')
                    Text(
                        text = str,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
