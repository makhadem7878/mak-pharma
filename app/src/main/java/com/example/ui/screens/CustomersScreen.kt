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
import com.example.data.model.Customer
import com.example.data.model.PaymentMethod
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomersScreen(viewModel: PharmacyViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val duePayments by viewModel.duePayments.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomerForDue by remember { mutableStateOf<Customer?>(null) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Customers & Dues, 1: Due Collection History

    val totalOutstandingDue = customers.sumOf { it.currentDue }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Top KPI Banner
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Total Customer Due (Receivable)",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = AppLocale.formatCurrency(totalOutstandingDue, language),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = AlertAmber
                        )
                    )
                }

                Button(
                    onClick = { showNewCustomerDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(AppLocale.get("add_customer", language))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PharmacyGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(AppLocale.get("customer_list", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Payment History (${duePayments.size})", fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search customer by phone or name...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyGreenPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            val filtered = customers.filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery, ignoreCase = true)
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { cust ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = cust.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(text = "📞 ${cust.phone}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                    Text(text = "📍 ${cust.address}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    if (cust.notes.isNotBlank()) {
                                        Text(text = "Note: ${cust.notes}", style = MaterialTheme.typography.labelSmall.copy(color = MedicalTealSecondary))
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = AppLocale.formatCurrency(cust.currentDue, language),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if (cust.currentDue > 0) AlertAmber else PharmacyGreenPrimary
                                        )
                                    )
                                    Text(
                                        text = "Limit: ${AppLocale.formatCurrency(cust.creditLimit, language)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                    )
                                }
                            }

                            if (cust.currentDue > 0) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Button(
                                        onClick = { selectedCustomerForDue = cust },
                                        colors = ButtonDefaults.buttonColors(containerColor = AlertAmber),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PriceCheck, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(AppLocale.get("record_payment", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Due Payment History
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(duePayments) { dp ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = dp.customerName, fontWeight = FontWeight.Bold)
                                Text(text = "Receipt: ${dp.receiptNumber} • ${dp.dateTimeStr}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                Text(text = "Method: ${dp.paymentMethod} • By: ${dp.collectedBy}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            }
                            Text(
                                text = "+ ${AppLocale.formatCurrency(dp.amountPaid, language)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PharmacyGreenPrimary
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Due Payment Dialog
    selectedCustomerForDue?.let { cust ->
        DueCollectionDialog(
            customer = cust,
            language = language,
            onCollect = { amount, method, notes ->
                viewModel.recordDuePayment(cust.id, amount, method, notes)
                selectedCustomerForDue = null
            },
            onDismiss = { selectedCustomerForDue = null }
        )
    }

    // New Customer Dialog
    if (showNewCustomerDialog) {
        CustomerFormDialog(
            language = language,
            onSave = {
                viewModel.saveCustomer(it)
                showNewCustomerDialog = false
            },
            onDismiss = { showNewCustomerDialog = false }
        )
    }
}

@Composable
private fun DueCollectionDialog(
    customer: Customer,
    language: Language,
    onCollect: (Double, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amountInput by remember { mutableStateOf(customer.currentDue.toString()) }
    var selectedMethod by remember { mutableStateOf(PaymentMethod.CASH.name) }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Collect Due: ${customer.name}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Total Outstanding Due: ৳ ${customer.currentDue}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AlertAmber)
                )

                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("Payment Amount (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Payment Method:", style = MaterialTheme.typography.labelMedium)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("CASH", "bKash", "Nagad", "BANK").forEach { m ->
                        val isSel = selectedMethod == m
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedMethod = m },
                            label = { Text(m, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Reference (Optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull()
                    if (amt != null && amt > 0) {
                        onCollect(amt, selectedMethod, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Confirm Collection")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun CustomerFormDialog(
    language: Language,
    onSave: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("3000") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = AppLocale.get("add_customer", language), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = creditLimit, onValueChange = { creditLimit = it }, label = { Text("Credit Limit (৳)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Medical / Patient Notes") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            Customer(
                                id = "cust_${System.currentTimeMillis()}",
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                creditLimit = creditLimit.toDoubleOrNull() ?: 3000.0,
                                notes = notes.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text(AppLocale.get("save", language))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(AppLocale.get("cancel", language)) }
        }
    )
}
