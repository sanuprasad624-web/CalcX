package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.DateTimeEngine
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimeScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Date & Time Calculations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("datetime_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("datetime_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Age") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Difference") })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Add Days") })
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTab) {
                    0 -> AgeCalculatorView()
                    1 -> DateDifferenceView()
                    2 -> AddDaysView()
                }
            }
        }
    }
}

@Composable
private fun AgeCalculatorView() {
    var birthYear by remember { mutableStateOf("2000") }
    var birthMonth by remember { mutableStateOf("1") }
    var birthDay by remember { mutableStateOf("15") }

    val y = birthYear.toIntOrNull() ?: 2000
    val m = (birthMonth.toIntOrNull() ?: 1).coerceIn(1, 12)
    val d = (birthDay.toIntOrNull() ?: 1).coerceIn(1, 31)

    val ageResult = remember(y, m, d) {
        DateTimeEngine.calculateAge(y, m, d)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Age & Birthday Countdown", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text("Enter Date of Birth (Year / Month / Day):", style = MaterialTheme.typography.bodySmall)
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = birthYear, onValueChange = { birthYear = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = birthMonth, onValueChange = { birthMonth = it }, label = { Text("Month (1-12)") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = birthDay, onValueChange = { birthDay = it }, label = { Text("Day (1-31)") }, modifier = Modifier.weight(1f))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Exact Age:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${ageResult.years} Years, ${ageResult.months} Months, ${ageResult.days} Days",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider()
                    Text("Total Days Lived: ${"%,d".format(ageResult.totalDays)} days", fontWeight = FontWeight.SemiBold)
                    Text("Next Birthday in: ${ageResult.daysUntilNextBirthday} days", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.tertiary)
                }
            }
        }
    }
}

@Composable
private fun DateDifferenceView() {
    val cal = Calendar.getInstance()
    var y1 by remember { mutableStateOf("2024") }
    var m1 by remember { mutableStateOf("1") }
    var d1 by remember { mutableStateOf("1") }

    var y2 by remember { mutableStateOf((cal.get(Calendar.YEAR)).toString()) }
    var m2 by remember { mutableStateOf((cal.get(Calendar.MONTH) + 1).toString()) }
    var d2 by remember { mutableStateOf((cal.get(Calendar.DAY_OF_MONTH)).toString()) }

    val ny1 = y1.toIntOrNull() ?: 2024
    val nm1 = (m1.toIntOrNull() ?: 1).coerceIn(1, 12)
    val nd1 = (d1.toIntOrNull() ?: 1).coerceIn(1, 31)

    val ny2 = y2.toIntOrNull() ?: 2024
    val nm2 = (m2.toIntOrNull() ?: 1).coerceIn(1, 12)
    val nd2 = (d2.toIntOrNull() ?: 1).coerceIn(1, 31)

    val diff = remember(ny1, nm1, nd1, ny2, nm2, nd2) {
        DateTimeEngine.calculateDifference(ny1, nm1, nd1, ny2, nm2, nd2)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Date Difference & Working Days", fontWeight = FontWeight.Bold)
        }
        item {
            Text("Start Date (YYYY / MM / DD):")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = y1, onValueChange = { y1 = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = m1, onValueChange = { m1 = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = d1, onValueChange = { d1 = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Text("End Date (YYYY / MM / DD):")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = y2, onValueChange = { y2 = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = m2, onValueChange = { m2 = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = d2, onValueChange = { d2 = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Total Days: ${diff.totalDays} days (${diff.fullWeeks} weeks)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Working Days (Mon - Fri): ${diff.workingDays} days", fontWeight = FontWeight.Medium)
                    Text("Weekend Days: ${diff.weekends} days", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun AddDaysView() {
    val cal = Calendar.getInstance()
    var year by remember { mutableStateOf((cal.get(Calendar.YEAR)).toString()) }
    var month by remember { mutableStateOf((cal.get(Calendar.MONTH) + 1).toString()) }
    var day by remember { mutableStateOf((cal.get(Calendar.DAY_OF_MONTH)).toString()) }
    var daysToAddStr by remember { mutableStateOf("45") }

    val y = year.toIntOrNull() ?: 2024
    val m = (month.toIntOrNull() ?: 1).coerceIn(1, 12)
    val d = (day.toIntOrNull() ?: 1).coerceIn(1, 31)
    val add = daysToAddStr.toIntOrNull() ?: 0

    val target = remember(y, m, d, add) {
        DateTimeEngine.addDaysToDate(y, m, d, add)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Add / Subtract Days", fontWeight = FontWeight.Bold)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = year, onValueChange = { year = it }, label = { Text("Year") }, modifier = Modifier.weight(1.2f))
                OutlinedTextField(value = month, onValueChange = { month = it }, label = { Text("Month") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            OutlinedTextField(
                value = daysToAddStr,
                onValueChange = { daysToAddStr = it },
                label = { Text("Days to Add (use negative to subtract)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Resulting Date:", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${target.first}-${target.second.toString().padStart(2, '0')}-${target.third.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
