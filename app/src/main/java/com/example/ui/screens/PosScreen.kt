package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Customer
import com.example.data.model.Medicine
import com.example.data.model.PaymentMethod
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.HeldBill
import com.example.ui.viewmodel.PharmacyViewModel
import com.example.ui.viewmodel.PosCartItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PosScreen(viewModel: PharmacyViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val searchQuery by viewModel.posSearchQuery.collectAsState()
    val discountAmount by viewModel.posDiscountAmount.collectAsState()
    val taxPercent by viewModel.posTaxPercent.collectAsState()
    val paidAmount by viewModel.posPaidAmount.collectAsState()
    val paymentMethod by viewModel.posPaymentMethod.collectAsState()
    val mobileProvider by viewModel.posMobileProvider.collectAsState()
    val heldBills by viewModel.heldBills.collectAsState()
    val subtotal by viewModel.cartSubtotal.collectAsState()
    val taxAmount by viewModel.cartTaxAmount.collectAsState()
    val grandTotal by viewModel.cartGrandTotal.collectAsState()

    var showCustomerPicker by remember { mutableStateOf(false) }
    var showNewCustomerDialog by remember { mutableStateOf(false) }
    var showHeldBillsDialog by remember { mutableStateOf(false) }
    var showDiscountDialog by remember { mutableStateOf(false) }
    var barcodeInput by remember { mutableStateOf("") }

    // Batch stock map for instant availability check
    val activeBatches = batches.filter { it.status == "ACTIVE" && it.availableQty > 0 }
    val stockMap = activeBatches.groupBy { it.medicineId }.mapValues { entry -> entry.value.sumOf { it.availableQty } }

    // Filtered medicines by search
    val filteredMedicines = remember(medicines, searchQuery) {
        if (searchQuery.isBlank()) medicines
        else medicines.filter { med ->
            med.name.contains(searchQuery, ignoreCase = true) ||
            med.banglaName.contains(searchQuery, ignoreCase = true) ||
            med.genericName.contains(searchQuery, ignoreCase = true) ||
            med.brand.contains(searchQuery, ignoreCase = true) ||
            med.barcode.contains(searchQuery, ignoreCase = true) ||
            med.sku.contains(searchQuery, ignoreCase = true)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(SoftBackground)) {
        val isWideScreen = maxWidth >= 840.dp

        if (isWideScreen) {
            // Tablet / Desktop Side-by-Side Layout
            Row(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Left Column: Catalog & Search
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .fillMaxHeight()
                        .padding(end = 8.dp)
                ) {
                    PosSearchAndBarcodeBar(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.updatePosSearchQuery(it) },
                        barcodeInput = barcodeInput,
                        onBarcodeChange = { barcodeInput = it },
                        onBarcodeSubmit = { code ->
                            val matched = medicines.firstOrNull { it.barcode == code }
                            if (matched != null) {
                                viewModel.addToCart(matched)
                                barcodeInput = ""
                            }
                        },
                        language = language
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MedicinesCatalogList(
                        medicines = filteredMedicines,
                        stockMap = stockMap,
                        language = language,
                        onAdd = { viewModel.addToCart(it) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Right Column: Cart, Customer, Payments
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    PosCartSection(
                        cartItems = cartItems,
                        selectedCustomer = selectedCustomer,
                        subtotal = subtotal,
                        discountAmount = discountAmount,
                        taxPercent = taxPercent,
                        taxAmount = taxAmount,
                        grandTotal = grandTotal,
                        paidAmount = paidAmount,
                        paymentMethod = paymentMethod,
                        mobileProvider = mobileProvider,
                        language = language,
                        onSelectCustomerClick = { showCustomerPicker = true },
                        onQtyChange = { id, q -> viewModel.updateCartItemQuantity(id, q) },
                        onRemove = { viewModel.removeFromCart(it) },
                        onClear = { viewModel.clearCart() },
                        onHold = { viewModel.holdCurrentCart() },
                        onViewHeldBills = { showHeldBillsDialog = true },
                        onEditDiscount = { showDiscountDialog = true },
                        onPaidChange = { viewModel.setPosPaidAmount(it) },
                        onPaymentMethodChange = { viewModel.setPosPaymentMethod(it) },
                        onMobileProviderChange = { viewModel.setPosMobileProvider(it) },
                        onCompleteSale = { viewModel.completeSale() },
                        heldBillsCount = heldBills.size
                    )
                }
            }
        } else {
            // Handheld Mobile Layout with Tabbed or Vertical Flow
            var activeMobileTab by remember { mutableStateOf(0) } // 0: Catalog, 1: Cart

            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                // Mobile Top Toggle (Catalog vs Cart)
                TabRow(
                    selectedTabIndex = activeMobileTab,
                    containerColor = Color.White,
                    contentColor = PharmacyGreenPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = activeMobileTab == 0,
                        onClick = { activeMobileTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == Language.BANGLA) "ওষুধ তালিকা" else "Medicines",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                    Tab(
                        selected = activeMobileTab == 1,
                        onClick = { activeMobileTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${AppLocale.get("cart", language)} (${cartItems.size})",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (activeMobileTab == 0) {
                    PosSearchAndBarcodeBar(
                        searchQuery = searchQuery,
                        onSearchChange = { viewModel.updatePosSearchQuery(it) },
                        barcodeInput = barcodeInput,
                        onBarcodeChange = { barcodeInput = it },
                        onBarcodeSubmit = { code ->
                            val matched = medicines.firstOrNull { it.barcode == code }
                            if (matched != null) {
                                viewModel.addToCart(matched)
                                barcodeInput = ""
                            }
                        },
                        language = language
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    MedicinesCatalogList(
                        medicines = filteredMedicines,
                        stockMap = stockMap,
                        language = language,
                        onAdd = { viewModel.addToCart(it) },
                        modifier = Modifier.weight(1f)
                    )

                    // Floating Cart summary bar on mobile
                    if (cartItems.isNotEmpty()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clickable { activeMobileTab = 1 },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = PharmacyGreenPrimary)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "${cartItems.sumOf { it.quantity }} items in Cart",
                                        style = MaterialTheme.typography.labelMedium.copy(color = PharmacyGreenLight)
                                    )
                                    Text(
                                        text = AppLocale.formatCurrency(grandTotal, language),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.White)
                                    )
                                }
                                Button(
                                    onClick = { activeMobileTab = 1 },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = PharmacyGreenPrimary)
                                ) {
                                    Text(text = "Checkout >", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        PosCartSection(
                            cartItems = cartItems,
                            selectedCustomer = selectedCustomer,
                            subtotal = subtotal,
                            discountAmount = discountAmount,
                            taxPercent = taxPercent,
                            taxAmount = taxAmount,
                            grandTotal = grandTotal,
                            paidAmount = paidAmount,
                            paymentMethod = paymentMethod,
                            mobileProvider = mobileProvider,
                            language = language,
                            onSelectCustomerClick = { showCustomerPicker = true },
                            onQtyChange = { id, q -> viewModel.updateCartItemQuantity(id, q) },
                            onRemove = { viewModel.removeFromCart(it) },
                            onClear = { viewModel.clearCart() },
                            onHold = { viewModel.holdCurrentCart() },
                            onViewHeldBills = { showHeldBillsDialog = true },
                            onEditDiscount = { showDiscountDialog = true },
                            onPaidChange = { viewModel.setPosPaidAmount(it) },
                            onPaymentMethodChange = { viewModel.setPosPaymentMethod(it) },
                            onMobileProviderChange = { viewModel.setPosMobileProvider(it) },
                            onCompleteSale = { viewModel.completeSale() },
                            heldBillsCount = heldBills.size
                        )
                    }
                }
            }
        }
    }

    // Customer Selection Dialog
    if (showCustomerPicker) {
        CustomerPickerDialog(
            customers = customers,
            language = language,
            onSelect = {
                viewModel.selectCustomer(it)
                showCustomerPicker = false
            },
            onAddNew = {
                showCustomerPicker = false
                showNewCustomerDialog = true
            },
            onDismiss = { showCustomerPicker = false }
        )
    }

    // New Customer Quick Dialog
    if (showNewCustomerDialog) {
        NewCustomerDialog(
            language = language,
            onSave = { newCust ->
                viewModel.saveCustomer(newCust)
                viewModel.selectCustomer(newCust)
                showNewCustomerDialog = false
            },
            onDismiss = { showNewCustomerDialog = false }
        )
    }

    // Held Bills Dialog
    if (showHeldBillsDialog) {
        HeldBillsDialog(
            heldBills = heldBills,
            language = language,
            onRestore = { bill ->
                viewModel.restoreHeldBill(bill)
                showHeldBillsDialog = false
            },
            onDismiss = { showHeldBillsDialog = false }
        )
    }

    // Discount & Tax Dialog
    if (showDiscountDialog) {
        DiscountTaxDialog(
            currentDiscount = discountAmount,
            currentTax = taxPercent,
            language = language,
            onApply = { disc, tax ->
                viewModel.setPosDiscount(disc)
                viewModel.setPosTaxPercent(tax)
                showDiscountDialog = false
            },
            onDismiss = { showDiscountDialog = false }
        )
    }
}

@Composable
private fun PosSearchAndBarcodeBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    barcodeInput: String,
    onBarcodeChange: (String) -> Unit,
    onBarcodeSubmit: (String) -> Unit,
    language: Language
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = {
                    Text(
                        text = AppLocale.get("search_medicine_placeholder", language),
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = PharmacyGreenPrimary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pos_search_input"),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Barcode quick input
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = onBarcodeChange,
                    placeholder = { Text(text = "Scan or type barcode (e.g. 894110010101)", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = "Barcode", modifier = Modifier.size(18.dp))
                    },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("barcode_input"),
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                FilledTonalButton(
                    onClick = { onBarcodeSubmit(barcodeInput.trim()) },
                    enabled = barcodeInput.isNotBlank(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Add", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun MedicinesCatalogList(
    medicines: List<Medicine>,
    stockMap: Map<String, Int>,
    language: Language,
    onAdd: (Medicine) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(medicines) { med ->
            val totalStock = stockMap[med.id] ?: 0
            val isOutOfStock = totalStock == 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isOutOfStock) { onAdd(med) }
                    .testTag("medicine_item_${med.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                            Text(
                                text = med.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            if (med.banglaName.isNotBlank() && language == Language.BANGLA) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${med.banglaName})",
                                    style = MaterialTheme.typography.bodySmall.copy(color = PharmacyGreenPrimary)
                                )
                            }
                        }
                        Text(
                            text = "${med.dosageForm} • ${med.strength} • ${med.genericName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                        )
                        Text(
                            text = "${med.brand} • ${med.category}",
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
                        Spacer(modifier = Modifier.height(2.dp))
                        if (isOutOfStock) {
                            StatusChip(
                                text = "Out of Stock",
                                backgroundColor = AlertRedLight,
                                textColor = AlertRed
                            )
                        } else {
                            StatusChip(
                                text = "Stock: $totalStock ${med.unit}",
                                backgroundColor = if (totalStock <= med.reorderLevel) AlertAmberLight else PharmacyGreenLight,
                                textColor = if (totalStock <= med.reorderLevel) AlertAmber else PharmacyGreenDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { onAdd(med) },
                        enabled = !isOutOfStock,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (!isOutOfStock) PharmacyGreenPrimary else Color.LightGray)
                            .size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PosCartSection(
    cartItems: List<PosCartItem>,
    selectedCustomer: Customer?,
    subtotal: Double,
    discountAmount: Double,
    taxPercent: Double,
    taxAmount: Double,
    grandTotal: Double,
    paidAmount: Double?,
    paymentMethod: PaymentMethod,
    mobileProvider: String,
    language: Language,
    onSelectCustomerClick: () -> Unit,
    onQtyChange: (String, Int) -> Unit,
    onRemove: (String) -> Unit,
    onClear: () -> Unit,
    onHold: () -> Unit,
    onViewHeldBills: () -> Unit,
    onEditDiscount: () -> Unit,
    onPaidChange: (Double?) -> Unit,
    onPaymentMethodChange: (PaymentMethod) -> Unit,
    onMobileProviderChange: (String) -> Unit,
    onCompleteSale: () -> Unit,
    heldBillsCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        // Customer Selector Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF1F5F9))
                .clickable { onSelectCustomerClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = PharmacyGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = selectedCustomer?.name ?: AppLocale.get("walk_in_customer", language),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (selectedCustomer != null) {
                        Text(
                            text = "Due: ${AppLocale.formatCurrency(selectedCustomer.currentDue, language)} | Limit: ${AppLocale.formatCurrency(selectedCustomer.creditLimit, language)}",
                            style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                        )
                    }
                }
            }
            Text(
                text = if (language == Language.BANGLA) "পরিবর্তন" else "Change",
                style = MaterialTheme.typography.labelSmall.copy(color = PharmacyGreenPrimary, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Cart Items List
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppLocale.get("empty_cart", language),
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.medicine.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        StatusChip(
                                            text = "FEFO: ${item.batch.batchNumber}",
                                            backgroundColor = PharmacyGreenLight,
                                            textColor = PharmacyGreenDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Exp: ${item.batch.expiryDate}",
                                            style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                                        )
                                    }
                                }

                                Text(
                                    text = AppLocale.formatCurrency(item.totalPrice, language),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Quantity controls & Remove
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "@ ${item.unitPrice} / ${item.medicine.unit}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(
                                        onClick = { onQtyChange(item.medicine.id, item.quantity - 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", modifier = Modifier.size(18.dp))
                                    }
                                    Text(
                                        text = "${item.quantity}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { onQtyChange(item.medicine.id, item.quantity + 1) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(
                                        onClick = { onRemove(item.medicine.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = AlertRed, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Payment Method Selectors
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PaymentMethod.values().take(4).forEach { method ->
                val isSelected = paymentMethod == method
                FilterChip(
                    selected = isSelected,
                    onClick = { onPaymentMethodChange(method) },
                    label = {
                        Text(
                            text = if (language == Language.BANGLA) method.labelBn else method.labelEn,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (paymentMethod == PaymentMethod.MOBILE_BANKING) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("bKash", "Nagad", "Rocket", "Upay").forEach { provider ->
                    val isSel = mobileProvider == provider
                    OutlinedButton(
                        onClick = { onMobileProviderChange(provider) },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = if (isSel) ButtonDefaults.outlinedButtonColors(containerColor = MedicalTealLight) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Text(text = provider, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Calculations Breakdown
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = AppLocale.get("subtotal", language), style = MaterialTheme.typography.bodySmall)
                Text(text = AppLocale.formatCurrency(subtotal, language), style = MaterialTheme.typography.bodySmall)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEditDiscount() },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${AppLocale.get("discount", language)} (Tap to edit)",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                )
                Text(
                    text = "- ${AppLocale.formatCurrency(discountAmount, language)}",
                    style = MaterialTheme.typography.bodySmall.copy(color = AlertRed)
                )
            }
            if (taxAmount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "VAT ($taxPercent%)", style = MaterialTheme.typography.bodySmall)
                    Text(text = AppLocale.formatCurrency(taxAmount, language), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = AppLocale.get("grand_total", language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = AppLocale.formatCurrency(grandTotal, language),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = PharmacyGreenPrimary
                    )
                )
            }

            // Paid amount input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = AppLocale.get("paid_amount", language), style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = paidAmount?.toString() ?: "",
                    onValueChange = { str -> onPaidChange(str.toDoubleOrNull()) },
                    placeholder = { Text(text = String.format("%.2f", grandTotal), fontSize = 12.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .width(130.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            val actualPaid = paidAmount ?: grandTotal
            if (actualPaid > grandTotal) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = AppLocale.get("change_return", language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PharmacyGreenPrimary))
                    Text(text = AppLocale.formatCurrency(actualPaid - grandTotal, language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PharmacyGreenPrimary))
                }
            } else if (actualPaid < grandTotal) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = AppLocale.get("due_amount", language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AlertAmber))
                    Text(text = AppLocale.formatCurrency(grandTotal - actualPaid, language), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = AlertAmber))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Complete Sale CTA Button
        Button(
            onClick = onCompleteSale,
            enabled = cartItems.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("complete_sale_button"),
            colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${AppLocale.get("complete_sale", language)} (${AppLocale.formatCurrency(grandTotal, language)})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Auxiliary actions: Hold, Held Bills, Clear
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = onHold,
                enabled = cartItems.isNotEmpty(),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(text = AppLocale.get("hold_cart", language), fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                onClick = onViewHeldBills,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(text = "${AppLocale.get("held_bills", language)} ($heldBillsCount)", fontSize = 11.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            OutlinedButton(
                onClick = onClear,
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AlertRed),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                Text(text = AppLocale.get("clear_cart", language), fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CustomerPickerDialog(
    customers: List<Customer>,
    language: Language,
    onSelect: (Customer?) -> Unit,
    onAddNew: () -> Unit,
    onDismiss: () -> Unit
) {
    var search by remember { mutableStateOf("") }
    val filtered = customers.filter {
        it.name.contains(search, ignoreCase = true) || it.phone.contains(search, ignoreCase = true)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = AppLocale.get("select_customer", language), fontWeight = FontWeight.Bold)
                IconButton(onClick = onAddNew) {
                    Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer", tint = PharmacyGreenPrimary)
                }
            }
        },
        text = {
            Column(modifier = Modifier.heightIn(max = 380.dp)) {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = { Text(text = "Search by phone or name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Walk-in default option
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(null) },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Text(
                        text = "👤 ${AppLocale.get("walk_in_customer", language)}",
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(filtered) { cust ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(cust) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = cust.name, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Due: ${AppLocale.formatCurrency(cust.currentDue, language)}",
                                        color = if (cust.currentDue > 0) AlertAmber else Color.Gray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(text = cust.phone, style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun NewCustomerDialog(
    language: Language,
    onSave: (Customer) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var creditLimit by remember { mutableStateOf("2000") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = AppLocale.get("add_customer", language), fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (e.g. 01711-xxxxxx)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = creditLimit,
                    onValueChange = { creditLimit = it },
                    label = { Text("Credit Limit (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
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
                                creditLimit = creditLimit.toDoubleOrNull() ?: 2000.0,
                                currentDue = 0.0
                            )
                        )
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text(text = AppLocale.get("save", language))
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text(text = AppLocale.get("cancel", language)) }
        }
    )
}

@Composable
private fun HeldBillsDialog(
    heldBills: List<HeldBill>,
    language: Language,
    onRestore: (HeldBill) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = AppLocale.get("held_bills", language), fontWeight = FontWeight.Bold) },
        text = {
            if (heldBills.isEmpty()) {
                Text(text = "No held bills currently.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(heldBills) { bill ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onRestore(bill) },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = bill.title, fontWeight = FontWeight.Bold)
                                    Text(text = "${bill.items.size} items", style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray))
                                }
                                Button(
                                    onClick = { onRestore(bill) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
                                ) {
                                    Text("Restore")
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun DiscountTaxDialog(
    currentDiscount: Double,
    currentTax: Double,
    language: Language,
    onApply: (Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var discInput by remember { mutableStateOf(if (currentDiscount > 0) currentDiscount.toString() else "") }
    var taxInput by remember { mutableStateOf(if (currentTax > 0) currentTax.toString() else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Adjust Discount & Tax", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = discInput,
                    onValueChange = { discInput = it },
                    label = { Text("Flat Discount Amount (৳)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = taxInput,
                    onValueChange = { taxInput = it },
                    label = { Text("VAT / Tax Rate (%)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onApply(discInput.toDoubleOrNull() ?: 0.0, taxInput.toDoubleOrNull() ?: 0.0)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
            ) {
                Text(text = "Apply")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
