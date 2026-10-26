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
import com.example.data.model.Medicine
import com.example.data.model.Purchase
import com.example.data.model.PurchaseItem
import com.example.data.model.Supplier
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesAndSuppliersScreen(
    viewModel: PharmacyViewModel,
    initialTab: Int = 0 // 0: Purchases, 1: Suppliers
) {
    val language by viewModel.currentLanguage.collectAsState()
    val purchases by viewModel.purchases.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val medicines by viewModel.medicines.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var searchQuery by remember { mutableStateOf("") }
    var showNewPurchaseDialog by remember { mutableStateOf(false) }
    var showNewSupplierDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Tab row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PharmacyGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(AppLocale.get("purchases", language), fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(AppLocale.get("suppliers", language), fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search and Add bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(if (selectedTab == 0) "Search purchases..." else "Search suppliers...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyGreenPrimary) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (selectedTab == 0) showNewPurchaseDialog = true
                    else showNewSupplierDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text(if (selectedTab == 0) "New Purchase" else "New Supplier")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedTab == 0) {
            // Purchases List
            val filteredPurchases = purchases.filter {
                searchQuery.isBlank() ||
                        it.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                        it.supplierName.contains(searchQuery, ignoreCase = true)
            }

            if (filteredPurchases.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No purchases recorded yet.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredPurchases) { p ->
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
                                    Column {
                                        Text(text = p.invoiceNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "Supplier: ${p.supplierName}", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                    }
                                    StatusChip(
                                        text = p.status,
                                        backgroundColor = PharmacyGreenLight,
                                        textColor = PharmacyGreenDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Date: ${p.purchaseDate}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    Text(
                                        text = "Total: ${AppLocale.formatCurrency(p.totalAmount, language)}",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PharmacyGreenPrimary)
                                    )
                                }
                                if (p.dueAmount > 0) {
                                    Text(
                                        text = "Due Payable: ${AppLocale.formatCurrency(p.dueAmount, language)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = AlertAmber, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Suppliers List
            val filteredSuppliers = suppliers.filter {
                searchQuery.isBlank() ||
                        it.name.contains(searchQuery, ignoreCase = true) ||
                        it.contactPerson.contains(searchQuery, ignoreCase = true) ||
                        it.phone.contains(searchQuery, ignoreCase = true)
            }

            if (filteredSuppliers.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No suppliers found.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredSuppliers) { sup ->
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
                                    Column {
                                        Text(text = sup.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "Contact: ${sup.contactPerson} (${sup.phone})", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                        Text(text = sup.address, style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = AppLocale.formatCurrency(sup.payableBalance, language),
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (sup.payableBalance > 0) AlertAmber else PharmacyGreenPrimary
                                            )
                                        )
                                        Text(text = "Payable Due", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Supplier Dialog
    if (showNewSupplierDialog) {
        NewSupplierDialog(
            language = language,
            onSave = { sup ->
                viewModel.saveSupplier(sup)
                showNewSupplierDialog = false
            },
            onDismiss = { showNewSupplierDialog = false }
        )
    }

    // New Purchase Dialog
    if (showNewPurchaseDialog) {
        NewPurchaseDialog(
            suppliers = suppliers,
            medicines = medicines,
            language = language,
            onSave = { purchase, items ->
                viewModel.receivePurchase(purchase, items)
                showNewPurchaseDialog = false
            },
            onDismiss = { showNewPurchaseDialog = false }
        )
    }
}

@Composable
private fun NewSupplierDialog(
    language: Language,
    onSave: (Supplier) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Supplier / Company", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company Name (e.g. Beximco)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact Person") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Depot / Address") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSave(
                            Supplier(
                                id = "sup_${System.currentTimeMillis()}",
                                name = name.trim(),
                                contactPerson = contact.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim()
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Save Supplier")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun NewPurchaseDialog(
    suppliers: List<Supplier>,
    medicines: List<Medicine>,
    language: Language,
    onSave: (Purchase, List<PurchaseItem>) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedSupplier by remember { mutableStateOf(suppliers.firstOrNull()) }
    var invoiceNumber by remember { mutableStateOf("PUR-" + (1000..9999).random()) }
    var selectedMedicine by remember { mutableStateOf(medicines.firstOrNull()) }
    var batchNumber by remember { mutableStateOf("B-" + (100..999).random()) }
    var expiryDate by remember { mutableStateOf("15/12/2027") }
    var quantity by remember { mutableStateOf("50") }
    var bonusQty by remember { mutableStateOf("2") }
    var purchasePrice by remember { mutableStateOf("25.0") }
    var mrpPrice by remember { mutableStateOf("30.0") }
    var paidAmount by remember { mutableStateOf("") }

    val qtyInt = quantity.toIntOrNull() ?: 50
    val buyPriceDbl = purchasePrice.toDoubleOrNull() ?: 25.0
    val totalAmount = qtyInt * buyPriceDbl
    val paidDbl = paidAmount.toDoubleOrNull() ?: totalAmount
    val dueAmount = (totalAmount - paidDbl).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Receive New Purchase Stock", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Supplier: ${selectedSupplier?.name ?: "None"}", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = invoiceNumber, onValueChange = { invoiceNumber = it }, label = { Text("Purchase Challan / Invoice #") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Text(text = "Medicine: ${selectedMedicine?.name ?: "None"}", fontWeight = FontWeight.SemiBold)
                OutlinedTextField(value = batchNumber, onValueChange = { batchNumber = it }, label = { Text("Batch Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = expiryDate, onValueChange = { expiryDate = it }, label = { Text("Expiry Date (DD/MM/YYYY)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text("Quantity") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = bonusQty, onValueChange = { bonusQty = it }, label = { Text("Bonus Qty") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = purchasePrice, onValueChange = { purchasePrice = it }, label = { Text("Buy Price (৳)") }, singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = mrpPrice, onValueChange = { mrpPrice = it }, label = { Text("Sale MRP (৳)") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                Text(text = "Calculated Total: ৳ ${String.format("%.2f", totalAmount)}", fontWeight = FontWeight.Bold, color = PharmacyGreenPrimary)
                OutlinedTextField(value = paidAmount, onValueChange = { paidAmount = it }, label = { Text("Paid Amount (Remaining will be Due)") }, placeholder = { Text("৳ $totalAmount") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sup = selectedSupplier ?: return@Button
                    val med = selectedMedicine ?: return@Button
                    val pId = UUID.randomUUID().toString()
                    val p = Purchase(
                        id = pId,
                        invoiceNumber = invoiceNumber.trim(),
                        supplierId = sup.id,
                        supplierName = sup.name,
                        purchaseDate = "26/10/2026",
                        timestamp = System.currentTimeMillis(),
                        totalAmount = totalAmount,
                        paidAmount = paidDbl,
                        dueAmount = dueAmount
                    )
                    val pItem = PurchaseItem(
                        id = UUID.randomUUID().toString(),
                        purchaseId = pId,
                        medicineId = med.id,
                        medicineName = med.name,
                        batchNumber = batchNumber.trim(),
                        expiryDate = expiryDate.trim(),
                        quantity = qtyInt,
                        bonusQuantity = bonusQty.toIntOrNull() ?: 0,
                        purchasePrice = buyPriceDbl,
                        mrp = mrpPrice.toDoubleOrNull() ?: buyPriceDbl,
                        salePrice = mrpPrice.toDoubleOrNull() ?: buyPriceDbl,
                        total = totalAmount
                    )
                    onSave(p, listOf(pItem))
                },
                enabled = selectedSupplier != null && selectedMedicine != null && batchNumber.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Receive Stock")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
