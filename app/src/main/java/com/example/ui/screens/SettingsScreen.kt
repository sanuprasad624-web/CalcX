package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AppThemeSetting
import com.example.ui.theme.DarkModeSetting

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: AppThemeSetting,
    onThemeChange: (AppThemeSetting) -> Unit,
    currentDarkMode: DarkModeSetting,
    onDarkModeChange: (DarkModeSetting) -> Unit,
    precision: Int,
    onPrecisionChange: (Int) -> Unit,
    jeeModeEnabled: Boolean,
    onToggleJeeMode: () -> Unit,
    hapticEnabled: Boolean,
    onToggleHaptic: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Preferences", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("settings_screen")
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance & Themes
            item {
                Text("Appearance & Color Theme", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Color Scheme:", fontWeight = FontWeight.SemiBold)
                        val themes = listOf(
                            AppThemeSetting.GRAPHITE to "Graphite (Monochrome)",
                            AppThemeSetting.MIDNIGHT to "Midnight (Deep Blue)",
                            AppThemeSetting.OCEAN to "Ocean (Cyan/Teal)",
                            AppThemeSetting.EMERALD to "Emerald (Mint/Forest)",
                            AppThemeSetting.PURPLE to "Purple (Violet/Amber)",
                            AppThemeSetting.SYSTEM to "System Dynamic"
                        )
                        themes.forEach { (theme, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onThemeChange(theme) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = currentTheme == theme,
                                    onClick = { onThemeChange(theme) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(label, fontWeight = if (currentTheme == theme) FontWeight.Bold else FontWeight.Normal)
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text("Mode:", fontWeight = FontWeight.SemiBold)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = currentDarkMode == DarkModeSetting.DARK,
                                onClick = { onDarkModeChange(DarkModeSetting.DARK) },
                                shape = SegmentedButtonDefaults.itemShape(0, 3)
                            ) { Text("Dark") }
                            SegmentedButton(
                                selected = currentDarkMode == DarkModeSetting.LIGHT,
                                onClick = { onDarkModeChange(DarkModeSetting.LIGHT) },
                                shape = SegmentedButtonDefaults.itemShape(1, 3)
                            ) { Text("Light") }
                            SegmentedButton(
                                selected = currentDarkMode == DarkModeSetting.SYSTEM,
                                onClick = { onDarkModeChange(DarkModeSetting.SYSTEM) },
                                shape = SegmentedButtonDefaults.itemShape(2, 3)
                            ) { Text("System") }
                        }
                    }
                }
            }

            // Calculation Preferences
            item {
                Text("Calculation Behavior", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("JEE Fast Mode", fontWeight = FontWeight.Bold)
                                Text("Prioritizes exact fractional & radical formats for physics and math exam preparation.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = jeeModeEnabled, onCheckedChange = { onToggleJeeMode() }, modifier = Modifier.testTag("switch_jee_mode"))
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Haptic Vibration Feedback", fontWeight = FontWeight.Bold)
                                Text("Tactile confirmation when keys and buttons are pressed.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = hapticEnabled, onCheckedChange = { onToggleHaptic() }, modifier = Modifier.testTag("switch_haptic"))
                        }

                        HorizontalDivider()

                        Column {
                            Text("Decimal Precision: $precision decimal places", fontWeight = FontWeight.Bold)
                            Slider(
                                value = precision.toFloat(),
                                onValueChange = { onPrecisionChange(it.toInt()) },
                                valueRange = 2f..12f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth().testTag("slider_precision")
                            )
                        }
                    }
                }
            }

            // About Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("CALCX", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text("Calculate Anything. Understand Everything.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Made by Aman Prasad", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "Version ${com.example.BuildConfig.VERSION_NAME} (Build ${com.example.BuildConfig.VERSION_CODE}) • ${com.example.BuildConfig.BUILD_TYPE.replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Text("• 100% Offline-First Architecture", style = MaterialTheme.typography.bodySmall)
                        Text("• Precision Shunting-Yard Algebraic Engine", style = MaterialTheme.typography.bodySmall)
                        Text("• Integrated JEE Exam & Engineering Toolkit", style = MaterialTheme.typography.bodySmall)
                        Text("• Interactive 2D Graphing with Dynamic Axes", style = MaterialTheme.typography.bodySmall)
                        Text("• Calculus Suite: Differentiation & Definite Integrals", style = MaterialTheme.typography.bodySmall)
                        Text("• Local Room Database for History & Engineering Notes", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
