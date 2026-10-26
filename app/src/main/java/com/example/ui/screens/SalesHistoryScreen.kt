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
import com.example.data.model.Sale
import com.example.data.model.UserRole
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesHistoryScreen(
    viewModel: PharmacyViewModel,
    onViewInvoice: (Sale) -> Unit
) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val sales by viewModel.sales.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, COMPLETED, REFUNDED
    var selectedSaleForRefund by remember { mutableStateOf<Sale?>(null) }

    val filtered = sales.filter { s ->
        val matchesSearch = searchQuery.isBlank() ||
                s.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                s.customerName.contains(searchQuery, ignoreCase = true) ||
                s.customerPhone.contains(searchQuery, ignoreCase = true)

        val matchesStatus = when (selectedStatusFilter) {
            "COMPLETED" -> s.status == "COMPLETED"
            "REFUNDED" -> s.status == "REFUNDED"
            else -> true
        }

        matchesSearch && matchesStatus
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by invoice # or customer...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyGreenPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All Sales", "COMPLETED" to "Completed", "REFUNDED" to "Refunded").forEach { (code, label) ->
                val isSel = selectedStatusFilter == code
                FilterChip(
                    selected = isSel,
                    onClick = { selectedStatusFilter = code },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No sales records found.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { sale ->
                    val isRefunded = sale.status == "REFUNDED"
                    val canRefund = UserRole.valueOf(currentUser.role).canRefundSale() && !isRefunded

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onViewInvoice(sale) },
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
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = sale.invoiceNumber, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusChip(
                                            text = sale.status,
                                            backgroundColor = if (isRefunded) AlertRedLight else PharmacyGreenLight,
                                            textColor = if (isRefunded) AlertRed else PharmacyGreenDark
                                        )
                                    }
                                    Text(text = "${sale.customerName} • ${sale.dateTimeStr}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                }

                                Text(
                                    text = AppLocale.formatCurrency(sale.grandTotal, language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isRefunded) Color.Gray else PharmacyGreenPrimary
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Payment: ${sale.paymentMethod} ${sale.mobileBankingProvider} • Cashier: ${sale.cashierName}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )

                                Row {
                                    OutlinedButton(
                                        onClick = { onViewInvoice(sale) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Invoice", fontSize = 10.sp)
                                    }

                                    if (canRefund) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedButton(
                                            onClick = { selectedSaleForRefund = sale },
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Refund", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Refund Confirmation Dialog
    selectedSaleForRefund?.let { sale ->
        RefundDialog(
            sale = sale,
            language = language,
            onConfirmRefund = { reason ->
                viewModel.refundSale(sale.id, reason)
                selectedSaleForRefund = null
            },
            onDismiss = { selectedSaleForRefund = null }
        )
    }
}

@Composable
private fun RefundDialog(
    sale: Sale,
    language: Language,
    onConfirmRefund: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var reason by remember { mutableStateOf("Patient returned unopened blister pack") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Refund Sale: ${sale.invoiceNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Total refund amount: ${AppLocale.formatCurrency(sale.grandTotal, language)}. All items will be restored to their original batches in inventory.",
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Reason for Return / Refund") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (reason.isNotBlank()) onConfirmRefund(reason)
                },
                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
            ) {
                Text("Confirm Refund & Restock")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
