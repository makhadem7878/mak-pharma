package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.PaymentMethod
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesAndShiftScreen(viewModel: PharmacyViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val activeShift by viewModel.activeShift.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val duePayments by viewModel.duePayments.collectAsState()

    var showAddExpenseDialog by remember { mutableStateOf(false) }

    // Shift Calculations
    val cashSales = sales.filter { it.status == "COMPLETED" && it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.paidAmount }
    val mobileSales = sales.filter { it.status == "COMPLETED" && it.paymentMethod == PaymentMethod.MOBILE_BANKING.name }.sumOf { it.paidAmount }
    val cardSales = sales.filter { it.status == "COMPLETED" && it.paymentMethod == PaymentMethod.CARD.name }.sumOf { it.paidAmount }
    val cashDue = duePayments.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amountPaid }
    val cashExpenses = expenses.filter { it.paymentMethod == PaymentMethod.CASH.name }.sumOf { it.amount }

    val openingFloat = activeShift?.openingCash ?: 5000.0
    val expectedCashInDrawer = openingFloat + cashSales + cashDue - cashExpenses

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Daily Cash Drawer Summary Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.PointOfSale, contentDescription = null, tint = PharmacyGreenPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Cashier Shift & Drawer Balance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        StatusChip(text = "SHIFT: OPEN", backgroundColor = PharmacyGreenLight, textColor = PharmacyGreenDark)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    DrawerRow("Opening Cash Float", AppLocale.formatCurrency(openingFloat, language))
                    DrawerRow("+ Cash Sales", AppLocale.formatCurrency(cashSales, language), color = PharmacyGreenPrimary)
                    DrawerRow("+ Cash Due Collected", AppLocale.formatCurrency(cashDue, language), color = PharmacyGreenPrimary)
                    DrawerRow("- Cash Expenses", "- " + AppLocale.formatCurrency(cashExpenses, language), color = AlertRed)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Expected Cash in Drawer", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(
                            text = AppLocale.formatCurrency(expectedCashInDrawer, language),
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PharmacyGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Digital Breakdown
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "bKash / Mobile", fontSize = 11.sp, color = Color.Gray)
                            Text(text = AppLocale.formatCurrency(mobileSales, language), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Cards", fontSize = 11.sp, color = Color.Gray)
                            Text(text = AppLocale.formatCurrency(cardSales, language), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Expenses Header & Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pharmacy Daily Expenses",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Button(
                    onClick = { showAddExpenseDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Expense")
                }
            }
        }

        // Expenses List
        if (expenses.isEmpty()) {
            item {
                Text("No expenses recorded today.")
            }
        } else {
            items(expenses) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = exp.title, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusChip(text = exp.category, backgroundColor = Color(0xFFF1F5F9), textColor = Color.DarkGray)
                            }
                            Text(
                                text = "${exp.dateTimeStr} • ${exp.paymentMethod} • By: ${exp.recordedBy}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                            if (exp.notes.isNotBlank()) {
                                Text(text = exp.notes, style = MaterialTheme.typography.labelSmall.copy(color = MedicalTealSecondary))
                            }
                        }

                        Text(
                            text = "- ${AppLocale.formatCurrency(exp.amount, language)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = AlertRed)
                        )
                    }
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        AddExpenseDialog(
            language = language,
            onSave = { cat, title, amt, method, notes ->
                viewModel.addExpense(cat, title, amt, method, notes)
                showAddExpenseDialog = false
            },
            onDismiss = { showAddExpenseDialog = false }
        )
    }
}

@Composable
private fun DrawerRow(label: String, value: String, color: Color = Color.Unspecified) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold, color = color))
    }
}

@Composable
private fun AddExpenseDialog(
    language: Language,
    onSave: (String, String, Double, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var category by remember { mutableStateOf("Tea & Snacks") }
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var method by remember { mutableStateOf("CASH") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf("Tea & Snacks", "Utilities", "Staff Salary", "Rent", "Maintenance", "Transportation", "Marketing", "Miscellaneous")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Record Pharmacy Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Category:", style = MaterialTheme.typography.labelMedium)
                LazyColumn(modifier = Modifier.heightIn(max = 120.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(categories) { cat ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (category == cat) PharmacyGreenLight else Color.Transparent)
                                .clickable { category = cat }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = category == cat, onClick = { category = cat })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = cat, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Expense Title (e.g. Electric meter token)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Amount (৳)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CASH", "bKash", "BANK").forEach { m ->
                        FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m, fontSize = 11.sp) })
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (Optional)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    if (title.isNotBlank() && amt != null && amt > 0) {
                        onSave(category, title, amt, method, notes)
                    }
                },
                enabled = title.isNotBlank() && amount.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
