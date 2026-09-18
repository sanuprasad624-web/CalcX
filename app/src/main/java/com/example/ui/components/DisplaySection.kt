package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.math.AngleMode

@Composable
fun DisplaySection(
    expression: String,
    resultPreview: String,
    exactResult: String,
    isExactMode: Boolean,
    angleMode: AngleMode,
    errorMessage: String?,
    onToggleAngleMode: () -> Unit,
    onToggleExactMode: () -> Unit,
    onSaveToNotebook: () -> Unit,
    previousCalculation: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exprScrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("display_section"),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Controls Bar: DEG/RAD/GRAD, EXACT/DEC, Save to Notebook, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Mode Badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleAngleMode() }
                        .testTag("angle_mode_toggle"),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = angleMode.name,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Exact / Decimal Toggle Badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleExactMode() }
                        .testTag("exact_mode_toggle"),
                    color = if (isExactMode) MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                    else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (isExactMode) "EXACT" else "DECIMAL",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isExactMode) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action icons: Copy result & Save to Notebook
                IconButton(
                    onClick = {
                        val textToCopy = if (isExactMode && exactResult.isNotEmpty()) exactResult else resultPreview
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("CALCX Result", textToCopy))
                        Toast.makeText(context, "Copied $textToCopy", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(36.dp).testTag("copy_result_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy result",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onSaveToNotebook,
                    modifier = Modifier.size(36.dp).testTag("save_notebook_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BookmarkAdd,
                        contentDescription = "Save to Notebook",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Closed Previous Calculation Breadcrumb (if user just evaluated)
            if (!previousCalculation.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$previousCalculation =",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }

            // Live Expression Text (Scrollable horizontally)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(exprScrollState, reverseScrolling = true),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = if (expression.isEmpty()) "0" else expression,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Default,
                        letterSpacing = 0.5.sp
                    ),
                    color = if (expression.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    modifier = Modifier.testTag("expression_display")
                )
            }

            // Evaluated Result Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End,
                        modifier = Modifier.testTag("error_display")
                    )
                } else {
                    val activeResult = if (isExactMode && exactResult.isNotEmpty()) exactResult else resultPreview
                    val hasLatex = activeResult.contains("\\frac") || activeResult.contains("\\pi") || activeResult.contains("\\sqrt")

                    if (hasLatex) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.testTag("result_display")
                        ) {
                            Text(
                                text = "= ",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            MathView(
                                latex = activeResult,
                                fontSize = 28.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = "= $activeResult",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 32.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.End,
                            modifier = Modifier.testTag("result_display")
                        )
                    }

                    // Secondary exact/decimal preview if available
                    if (exactResult.isNotEmpty() && exactResult != resultPreview) {
                        Text(
                            text = if (isExactMode) "≈ $resultPreview" else "Exact: $exactResult",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
        }
    }
}
