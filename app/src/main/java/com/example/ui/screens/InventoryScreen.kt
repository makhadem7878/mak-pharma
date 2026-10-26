package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: PharmacyViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val recentMovements by viewModel.recentStockMovements.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Medicines Master, 1: Batches (FEFO), 2: Stock Movements
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, LOW_STOCK, EXPIRING_30, EXPIRING_90, OUT_OF_STOCK
    var selectedDosageForm by remember { mutableStateOf<String?>(null) }

    var showAddMedicineDialog by remember { mutableStateOf(false) }
    var selectedBatchForAdjustment by remember { mutableStateOf<MedicineBatch?>(null) }

    val now = System.currentTimeMillis()
    val dayMillis = TimeUnit.DAYS.toMillis(1)

    // Stock map
    val activeBatches = batches.filter { it.status == "ACTIVE" }
    val stockMap = activeBatches.groupBy { it.medicineId }.mapValues { entry -> entry.value.sumOf { it.availableQty } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Tab Selector Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PharmacyGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(AppLocale.get("all_medicines", language), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(AppLocale.get("batches", language), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(AppLocale.get("stock_movements", language), fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search name, generic, brand, batch...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyGreenPrimary) },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(10.dp)
            )

            val roleEnum = UserRole.valueOf(currentUser.role)
            if (roleEnum.canManageInventory()) {
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { showAddMedicineDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(52.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (language == Language.BANGLA) "নতুন ওষুধ" else "Add")
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Filter Pills
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Items") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "LOW_STOCK",
                    onClick = { selectedFilter = "LOW_STOCK" },
                    label = { Text("⚠️ Low Stock") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "EXPIRING_30",
                    onClick = { selectedFilter = "EXPIRING_30" },
                    label = { Text("🚨 Expiring < 30d") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "EXPIRING_90",
                    onClick = { selectedFilter = "EXPIRING_90" },
                    label = { Text("⏳ Expiring < 90d") }
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "OUT_OF_STOCK",
                    onClick = { selectedFilter = "OUT_OF_STOCK" },
                    label = { Text("❌ Out of Stock") }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Content
        when (selectedTab) {
            0 -> MedicinesListTab(
                medicines = medicines,
                stockMap = stockMap,
                batches = batches,
                searchQuery = searchQuery,
                filter = selectedFilter,
                language = language,
                now = now,
                dayMillis = dayMillis
            )
            1 -> BatchesListTab(
                batches = batches,
                medicines = medicines,
                searchQuery = searchQuery,
                filter = selectedFilter,
                language = language,
                now = now,
                dayMillis = dayMillis,
                onAdjustClick = { selectedBatchForAdjustment = it }
            )
            2 -> StockMovementsTab(
                movements = recentMovements,
                searchQuery = searchQuery,
                language = language
            )
        }
    }

    // Add Medicine Dialog
    if (showAddMedicineDialog) {
        AddMedicineDialog(
            language = language,
            onSave = { med ->
                viewModel.saveMedicine(med)
                showAddMedicineDialog = false
            },
            onDismiss = { showAddMedicineDialog = false }
        )
    }

    // Stock Adjustment Dialog
    selectedBatchForAdjustment?.let { batch ->
        val med = medicines.firstOrNull { it.id == batch.medicineId }
        StockAdjustmentDialog(
            batch = batch,
            medicineName = med?.name ?: "Medicine",
            language = language,
            onAdjust = { newQty, reason, type ->
                viewModel.adjustBatchStock(batch.id, newQty, reason, type)
                selectedBatchForAdjustment = null
            },
            onDismiss = { selectedBatchForAdjustment = null }
        )
    }
}

@Composable
private fun MedicinesListTab(
    medicines: List<Medicine>,
    stockMap: Map<String, Int>,
    batches: List<MedicineBatch>,
    searchQuery: String,
    filter: String,
    language: Language,
    now: Long,
    dayMillis: Long
) {
    val filtered = medicines.filter { med ->
        val matchesSearch = searchQuery.isBlank() ||
                med.name.contains(searchQuery, ignoreCase = true) ||
                med.genericName.contains(searchQuery, ignoreCase = true) ||
                med.brand.contains(searchQuery, ignoreCase = true)

        val totalStock = stockMap[med.id] ?: 0
        val medBatches = batches.filter { it.medicineId == med.id && it.status == "ACTIVE" }

        val matchesFilter = when (filter) {
            "LOW_STOCK" -> totalStock in 1..med.reorderLevel
            "OUT_OF_STOCK" -> totalStock == 0
            "EXPIRING_30" -> medBatches.any { it.expiryTimestampMillis - now in 0..(30 * dayMillis) }
            "EXPIRING_90" -> medBatches.any { it.expiryTimestampMillis - now in 0..(90 * dayMillis) }
            else -> true
        }

        matchesSearch && matchesFilter
    }

    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No medicines match current search or filters.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { med ->
                val totalStock = stockMap[med.id] ?: 0
                val isLow = totalStock in 1..med.reorderLevel
                val isOut = totalStock == 0

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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = med.name,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (med.prescriptionRequired) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        StatusChip(text = "Rx Required", backgroundColor = Color(0xFFFEE2E2), textColor = AlertRed)
                                    }
                                }
                                if (med.banglaName.isNotBlank()) {
                                    Text(text = med.banglaName, style = MaterialTheme.typography.bodySmall.copy(color = PharmacyGreenPrimary))
                                }
                                Text(
                                    text = "Generic: ${med.genericName} • ${med.strength}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                                )
                                Text(
                                    text = "Company: ${med.brand} • ${med.category}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = AppLocale.formatCurrency(med.defaultSalePrice, language),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = PharmacyGreenPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (isOut) {
                                    StatusChip(text = "Out of Stock", backgroundColor = AlertRedLight, textColor = AlertRed)
                                } else if (isLow) {
                                    StatusChip(text = "Low: $totalStock ${med.unit}", backgroundColor = AlertAmberLight, textColor = AlertAmber)
                                } else {
                                    StatusChip(text = "Stock: $totalStock ${med.unit}", backgroundColor = PharmacyGreenLight, textColor = PharmacyGreenDark)
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF1F5F9))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "SKU: ${med.sku} | Barcode: ${med.barcode}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                            Text(text = "Reorder Level: ${med.reorderLevel} ${med.unit}", style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BatchesListTab(
    batches: List<MedicineBatch>,
    medicines: List<Medicine>,
    searchQuery: String,
    filter: String,
    language: Language,
    now: Long,
    dayMillis: Long,
    onAdjustClick: (MedicineBatch) -> Unit
) {
    val medMap = medicines.associateBy { it.id }

    val filtered = batches.filter { batch ->
        val med = medMap[batch.medicineId]
        val matchesSearch = searchQuery.isBlank() ||
                batch.batchNumber.contains(searchQuery, ignoreCase = true) ||
                (med?.name?.contains(searchQuery, ignoreCase = true) == true) ||
                batch.supplierName.contains(searchQuery, ignoreCase = true)

        val daysToExpiry = (batch.expiryTimestampMillis - now) / dayMillis

        val matchesFilter = when (filter) {
            "LOW_STOCK" -> batch.availableQty <= (med?.reorderLevel ?: 20)
            "OUT_OF_STOCK" -> batch.availableQty == 0
            "EXPIRING_30" -> daysToExpiry in 0..30
            "EXPIRING_90" -> daysToExpiry in 0..90
            else -> true
        }

        matchesSearch && matchesFilter
    }

    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No batches match current filter.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { batch ->
                val med = medMap[batch.medicineId]
                val daysToExpiry = (batch.expiryTimestampMillis - now) / dayMillis
                val isExpiringSoon = daysToExpiry in 0..60
                val isExpired = daysToExpiry < 0

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
                                Text(
                                    text = med?.name ?: "Unknown Medicine",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Batch: ${batch.batchNumber}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusChip(
                                        text = "Rack: ${batch.storageLocation}",
                                        backgroundColor = Color(0xFFF1F5F9),
                                        textColor = Color.DarkGray
                                    )
                                }
                            }

                            // Expiry Status Badge
                            if (isExpired) {
                                StatusChip(text = "EXPIRED", backgroundColor = AlertRedLight, textColor = AlertRed)
                            } else if (daysToExpiry <= 30) {
                                StatusChip(text = "Exp: $daysToExpiry days!", backgroundColor = AlertRedLight, textColor = AlertRed)
                            } else if (daysToExpiry <= 90) {
                                StatusChip(text = "Exp: $daysToExpiry days", backgroundColor = AlertAmberLight, textColor = AlertAmber)
                            } else {
                                StatusChip(text = "Exp: ${batch.expiryDate}", backgroundColor = PharmacyGreenLight, textColor = PharmacyGreenDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Available Qty: ${batch.availableQty}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Buy: ৳${batch.purchasePrice} | MRP: ৳${batch.mrp} | Sale: ৳${batch.salePrice}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                                )
                                Text(
                                    text = "Supplier: ${batch.supplierName}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                                )
                            }

                            OutlinedButton(
                                onClick = { onAdjustClick(batch) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Adjust Stock", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockMovementsTab(
    movements: List<StockMovement>,
    searchQuery: String,
    language: Language
) {
    val filtered = movements.filter {
        searchQuery.isBlank() ||
                it.medicineName.contains(searchQuery, ignoreCase = true) ||
                it.batchNumber.contains(searchQuery, ignoreCase = true) ||
                it.reason.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No stock movements recorded yet.")
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filtered) { sm ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = sm.medicineName, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                StatusChip(
                                    text = sm.type,
                                    backgroundColor = if (sm.quantityChange > 0) PharmacyGreenLight else AlertAmberLight,
                                    textColor = if (sm.quantityChange > 0) PharmacyGreenDark else AlertAmber
                                )
                            }
                            Text(
                                text = "Batch: ${sm.batchNumber} • ${sm.reason}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                            )
                            Text(
                                text = "${sm.dateTimeStr} • By: ${sm.performedBy}",
                                style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (sm.quantityChange > 0) "+${sm.quantityChange}" else "${sm.quantityChange}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (sm.quantityChange > 0) PharmacyGreenPrimary else AlertRed
                                )
                            )
                            Text(text = "Bal: ${sm.remainingQty}", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockAdjustmentDialog(
    batch: MedicineBatch,
    medicineName: String,
    language: Language,
    onAdjust: (Int, String, StockMovementType) -> Unit,
    onDismiss: () -> Unit
) {
    var newQtyInput by remember { mutableStateOf(batch.availableQty.toString()) }
    var reasonInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(StockMovementType.PHYSICAL_COUNT_DISCREPANCY) }

    val presetReasons = listOf(
        "Physical Count Discrepancy",
        "Damage / Breakage",
        "Expired Stock Discard",
        "Sample / Physician Promo",
        "Return to Distributor"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Adjust Stock: $medicineName (Batch ${batch.batchNumber})", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Current Stock: ${batch.availableQty} units",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                OutlinedTextField(
                    value = newQtyInput,
                    onValueChange = { newQtyInput = it },
                    label = { Text("New Adjusted Quantity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(text = "Adjustment Reason:", style = MaterialTheme.typography.labelMedium)
                presetReasons.forEach { preset ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                reasonInput = preset
                                selectedType = if (preset.contains("Count")) StockMovementType.PHYSICAL_COUNT_DISCREPANCY
                                else StockMovementType.ADJUSTMENT_SUB
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = reasonInput == preset,
                            onClick = {
                                reasonInput = preset
                                selectedType = if (preset.contains("Count")) StockMovementType.PHYSICAL_COUNT_DISCREPANCY
                                else StockMovementType.ADJUSTMENT_SUB
                            }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = preset, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = reasonInput,
                    onValueChange = { reasonInput = it },
                    label = { Text("Custom Reason / Audit Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val q = newQtyInput.toIntOrNull()
                    if (q != null && reasonInput.isNotBlank()) {
                        onAdjust(q, reasonInput, selectedType)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Confirm Adjustment")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddMedicineDialog(
    language: Language,
    onSave: (Medicine) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var banglaName by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("Square Pharmaceuticals PLC") }
    var dosageForm by remember { mutableStateOf(DosageForm.TABLET.name) }
    var strength by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("Strip") }
    var category by remember { mutableStateOf("General") }
    var barcode by remember { mutableStateOf("8941" + (10000000..99999999).random()) }
    var sku by remember { mutableStateOf("MED-" + (100..999).random()) }
    var defaultPrice by remember { mutableStateOf("50.0") }
    var reorderLevel by remember { mutableStateOf("20") }
    var prescriptionRequired by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = AppLocale.get("add_medicine", language), fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medicine Name (e.g. Napa Extra)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = banglaName, onValueChange = { banglaName = it }, label = { Text("Bangla Name (ঐচ্ছিক)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = genericName, onValueChange = { genericName = it }, label = { Text("Generic Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = strength, onValueChange = { strength = it }, label = { Text("Strength (e.g. 500mg)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Company / Brand") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = defaultPrice, onValueChange = { defaultPrice = it }, label = { Text("Default Sale Price (৳)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = reorderLevel, onValueChange = { reorderLevel = it }, label = { Text("Reorder Warning Level") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = prescriptionRequired, onCheckedChange = { prescriptionRequired = it })
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Prescription Required (Rx)", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            Medicine(
                                id = "med_${System.currentTimeMillis()}",
                                name = name.trim(),
                                banglaName = banglaName.trim(),
                                genericName = genericName.trim(),
                                brand = brand.trim(),
                                dosageForm = dosageForm,
                                strength = strength.trim(),
                                unit = unit,
                                category = category,
                                barcode = barcode,
                                sku = sku,
                                prescriptionRequired = prescriptionRequired,
                                reorderLevel = reorderLevel.toIntOrNull() ?: 20,
                                defaultSalePrice = defaultPrice.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text("Save Medicine")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
