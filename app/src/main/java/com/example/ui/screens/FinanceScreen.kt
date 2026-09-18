package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.FinanceEngine

enum class FinanceSubTool(val label: String) {
    EMI("Loan EMI"),
    INTEREST("Interest"),
    GST("GST / Tax"),
    TIP_SPLIT("Tip Split"),
    INFLATION("Inflation")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(
    onBack: () -> Unit,
    onSaveToNotebook: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTool by remember { mutableStateOf(FinanceSubTool.EMI) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Toolkit", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("finance_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier.testTag("finance_screen")
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = selectedTool.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                FinanceSubTool.values().forEach { tool ->
                    Tab(
                        selected = selectedTool == tool,
                        onClick = { selectedTool = tool },
                        text = { Text(tool.label, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                when (selectedTool) {
                    FinanceSubTool.EMI -> EmiView(onSaveToNotebook)
                    FinanceSubTool.INTEREST -> InterestView(onSaveToNotebook)
                    FinanceSubTool.GST -> GstView(onSaveToNotebook)
                    FinanceSubTool.TIP_SPLIT -> TipSplitView(onSaveToNotebook)
                    FinanceSubTool.INFLATION -> InflationView(onSaveToNotebook)
                }
            }
        }
    }
}

@Composable
private fun EmiView(onSaveToNotebook: (String, String, String) -> Unit) {
    var principalStr by remember { mutableStateOf("500000") }
    var rateStr by remember { mutableStateOf("8.5") }
    var tenureYearsStr by remember { mutableStateOf("5") }

    val principal = principalStr.toDoubleOrNull() ?: 0.0
    val rate = rateStr.toDoubleOrNull() ?: 0.0
    val tenureYears = tenureYearsStr.toIntOrNull() ?: 1
    val tenureMonths = tenureYears * 12

    val emiRes = remember(principal, rate, tenureMonths) {
        FinanceEngine.calculateLoanEmi(principal, rate, tenureMonths)
    }

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Loan EMI Calculator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }
        item {
            OutlinedTextField(
                value = principalStr,
                onValueChange = { principalStr = it },
                label = { Text("Loan Amount (Principal)") },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = rateStr,
                    onValueChange = { rateStr = it },
                    label = { Text("Annual Rate (%)") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tenureYearsStr,
                    onValueChange = { tenureYearsStr = it },
                    label = { Text("Tenure (Years)") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Monthly EMI", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "₹ ${"%,.2f".format(emiRes.monthlyEmi)}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Interest Payable:")
                        Text("₹ ${"%,.2f".format(emiRes.totalInterest)}", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Repayment (P + I):")
                        Text("₹ ${"%,.2f".format(emiRes.totalPayment)}", fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            onSaveToNotebook(
                                "Loan EMI: ₹$principalStr at $rateStr% for $tenureYearsStr yrs",
                                "₹${"%,.2f".format(emiRes.monthlyEmi)}/mo",
                                "Total: ₹${"%,.2f".format(emiRes.totalPayment)}, Interest: ₹${"%,.2f".format(emiRes.totalInterest)}"
                            )
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = "Save")
                    }
                }
            }
        }
    }
}

@Composable
private fun InterestView(onSaveToNotebook: (String, String, String) -> Unit) {
    var pStr by remember { mutableStateOf("100000") }
    var rStr by remember { mutableStateOf("7.0") }
    var tStr by remember { mutableStateOf("3.0") }
    var isCompound by remember { mutableStateOf(true) }

    val p = pStr.toDoubleOrNull() ?: 0.0
    val r = rStr.toDoubleOrNull() ?: 0.0
    val t = tStr.toDoubleOrNull() ?: 0.0

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Interest Calculator", fontWeight = FontWeight.Bold)
                Row {
                    FilterChip(selected = !isCompound, onClick = { isCompound = false }, label = { Text("Simple") })
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(selected = isCompound, onClick = { isCompound = true }, label = { Text("Compound") })
                }
            }
        }
        item {
            OutlinedTextField(value = pStr, onValueChange = { pStr = it }, label = { Text("Principal Amount") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = rStr, onValueChange = { rStr = it }, label = { Text("Rate % p.a.") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = tStr, onValueChange = { tStr = it }, label = { Text("Time (Years)") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isCompound) {
                        val cRes = FinanceEngine.calculateCompoundInterest(p, r, t)
                        Text("Compound Interest (Monthly Compounding):", fontWeight = FontWeight.SemiBold)
                        Text("Final Amount: ₹ ${"%,.2f".format(cRes.finalAmount)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Interest Earned: ₹ ${"%,.2f".format(cRes.totalInterest)}", fontWeight = FontWeight.Medium)
                    } else {
                        val sInterest = FinanceEngine.calculateSimpleInterest(p, r, t)
                        val total = p + sInterest
                        Text("Simple Interest:", fontWeight = FontWeight.SemiBold)
                        Text("Final Amount: ₹ ${"%,.2f".format(total)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Interest Earned: ₹ ${"%,.2f".format(sInterest)}", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun GstView(onSaveToNotebook: (String, String, String) -> Unit) {
    var amountStr by remember { mutableStateOf("1000") }
    var gstRate by remember { mutableStateOf(18.0) }
    var isInclusive by remember { mutableStateOf(false) }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val (gstAmount, totalAmount) = FinanceEngine.calculateGst(amount, gstRate, isInclusive)

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Goods & Services Tax (GST) Calculator", fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Base / Total Amount") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Text("GST Rate:")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(5.0, 12.0, 18.0, 28.0).forEach { rate ->
                    FilterChip(
                        selected = gstRate == rate,
                        onClick = { gstRate = rate },
                        label = { Text("${rate.toInt()}%") }
                    )
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = isInclusive, onCheckedChange = { isInclusive = it })
                Text("Price is already Inclusive of GST")
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("GST Tax Amount: ₹ ${"%,.2f".format(gstAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Total Final Price: ₹ ${"%,.2f".format(totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (isInclusive) "Net pre-tax amount: ₹ ${"%,.2f".format(totalAmount - gstAmount)}" else "Net base price: ₹ ${"%,.2f".format(amount)}")
                }
            }
        }
    }
}

@Composable
private fun TipSplitView(onSaveToNotebook: (String, String, String) -> Unit) {
    var billStr by remember { mutableStateOf("2400") }
    var tipPercent by remember { mutableStateOf(10.0) }
    var peopleCount by remember { mutableStateOf(4) }

    val bill = billStr.toDoubleOrNull() ?: 0.0
    val map = FinanceEngine.calculateTipSplit(bill, tipPercent, peopleCount)

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Bill Split & Tip Calculator", fontWeight = FontWeight.Bold)
        }
        item {
            OutlinedTextField(value = billStr, onValueChange = { billStr = it }, label = { Text("Total Bill Amount") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.0, 5.0, 10.0, 15.0, 20.0).forEach { tip ->
                    FilterChip(selected = tipPercent == tip, onClick = { tipPercent = tip }, label = { Text("${tip.toInt()}%") })
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Split between people: $peopleCount", fontWeight = FontWeight.Medium)
                Row {
                    FilledTonalIconButton(onClick = { if (peopleCount > 1) peopleCount-- }) { Text("-") }
                    Spacer(modifier = Modifier.width(8.dp))
                    FilledTonalIconButton(onClick = { peopleCount++ }) { Text("+") }
                }
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Per Person Pays:", style = MaterialTheme.typography.bodyMedium)
                    Text("₹ ${"%,.2f".format(map["perPerson"])}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Divider()
                    Text("Total Tip: ₹ ${"%,.2f".format(map["tipAmount"])}")
                    Text("Total Amount with Tip: ₹ ${"%,.2f".format(map["totalBill"])}")
                }
            }
        }
    }
}

@Composable
private fun InflationView(onSaveToNotebook: (String, String, String) -> Unit) {
    var amountStr by remember { mutableStateOf("100000") }
    var inflationRateStr by remember { mutableStateOf("6.0") }
    var yearsStr by remember { mutableStateOf("10") }

    val amount = amountStr.toDoubleOrNull() ?: 0.0
    val rate = inflationRateStr.toDoubleOrNull() ?: 6.0
    val yrs = yearsStr.toDoubleOrNull() ?: 10.0

    val fv = FinanceEngine.calculateInflation(amount, rate, yrs)

    LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Text("Inflation Impact Calculator", fontWeight = FontWeight.Bold)
            Text("Calculates the equivalent future cost needed due to inflation erosion.", style = MaterialTheme.typography.bodySmall)
        }
        item {
            OutlinedTextField(value = amountStr, onValueChange = { amountStr = it }, label = { Text("Current Amount") }, modifier = Modifier.fillMaxWidth())
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = inflationRateStr, onValueChange = { inflationRateStr = it }, label = { Text("Inflation Rate %") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = yearsStr, onValueChange = { yearsStr = it }, label = { Text("Years") }, modifier = Modifier.weight(1f))
            }
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Future Cost Equivalent:", style = MaterialTheme.typography.bodyMedium)
                    Text("₹ ${"%,.2f".format(fv)}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("What costs ₹$amountStr today will cost ₹${"%,.2f".format(fv)} in ${yearsStr} years at $inflationRateStr% annual inflation.")
                }
            }
        }
    }
}
