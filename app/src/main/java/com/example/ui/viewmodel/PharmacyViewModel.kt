package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.PharmacyRepository
import com.example.data.seed.DemoData
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.navigation.AppScreen
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

data class PosCartItem(
    val medicine: Medicine,
    val batch: MedicineBatch,
    val quantity: Int = 1,
    val unitPrice: Double = batch.salePrice,
    val discountPercent: Double = 0.0
) {
    val subtotal: Double get() = quantity * unitPrice
    val discountAmount: Double get() = subtotal * (discountPercent / 100.0)
    val totalPrice: Double get() = subtotal - discountAmount
}

data class HeldBill(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val items: List<PosCartItem>,
    val customer: Customer?
)

class PharmacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PharmacyRepository

    init {
        val dao = AppDatabase.getInstance(application).pharmacyDao()
        repository = PharmacyRepository(dao)
        viewModelScope.launch {
            repository.checkAndPrepopulate()
        }
    }

    // --- GLOBAL APP STATE ---
    private val _currentLanguage = MutableStateFlow(Language.ENGLISH)
    val currentLanguage: StateFlow<Language> = _currentLanguage.asStateFlow()

    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _currentUser = MutableStateFlow(DemoData.users[1]) // Default: Dr. Nahid Hasan (Pharmacist/Manager)
    val currentUser: StateFlow<AppUser> = _currentUser.asStateFlow()

    val settings: StateFlow<PharmacySettings> = repository.settingsFlow
        .map { it ?: DemoData.defaultSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoData.defaultSettings)

    val users: StateFlow<List<AppUser>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DemoData.users)

    val medicines: StateFlow<List<Medicine>> = repository.allMedicines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val batches: StateFlow<List<MedicineBatch>> = repository.allBatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sales: StateFlow<List<Sale>> = repository.allSales
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val saleItems: StateFlow<List<SaleItem>> = repository.allSaleItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchases: StateFlow<List<Purchase>> = repository.allPurchases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duePayments: StateFlow<List<CustomerDuePayment>> = repository.allDuePayments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeShift: StateFlow<CashierShift?> = repository.activeShift
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val shifts: StateFlow<List<CashierShift>> = repository.allShifts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentStockMovements: StateFlow<List<StockMovement>> = repository.recentStockMovements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- POS STATE ---
    private val _cartItems = MutableStateFlow<List<PosCartItem>>(emptyList())
    val cartItems: StateFlow<List<PosCartItem>> = _cartItems.asStateFlow()

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer.asStateFlow()

    private val _posSearchQuery = MutableStateFlow("")
    val posSearchQuery: StateFlow<String> = _posSearchQuery.asStateFlow()

    private val _posDiscountAmount = MutableStateFlow(0.0)
    val posDiscountAmount: StateFlow<Double> = _posDiscountAmount.asStateFlow()

    private val _posTaxPercent = MutableStateFlow(0.0)
    val posTaxPercent: StateFlow<Double> = _posTaxPercent.asStateFlow()

    private val _posPaidAmount = MutableStateFlow<Double?>(null)
    val posPaidAmount: StateFlow<Double?> = _posPaidAmount.asStateFlow()

    private val _posPaymentMethod = MutableStateFlow(PaymentMethod.CASH)
    val posPaymentMethod: StateFlow<PaymentMethod> = _posPaymentMethod.asStateFlow()

    private val _posMobileProvider = MutableStateFlow("bKash")
    val posMobileProvider: StateFlow<String> = _posMobileProvider.asStateFlow()

    private val _heldBills = MutableStateFlow<List<HeldBill>>(emptyList())
    val heldBills: StateFlow<List<HeldBill>> = _heldBills.asStateFlow()

    private val _lastCompletedSale = MutableStateFlow<Sale?>(null)
    val lastCompletedSale: StateFlow<Sale?> = _lastCompletedSale.asStateFlow()

    private val _lastCompletedSaleItems = MutableStateFlow<List<SaleItem>>(emptyList())
    val lastCompletedSaleItems: StateFlow<List<SaleItem>> = _lastCompletedSaleItems.asStateFlow()

    private val _showInvoiceDialog = MutableStateFlow(false)
    val showInvoiceDialog: StateFlow<Boolean> = _showInvoiceDialog.asStateFlow()

    private val _uiMessage = MutableStateFlow<String?>(null)
    val uiMessage: StateFlow<String?> = _uiMessage.asStateFlow()

    // --- COMPUTED CART TOTALS ---
    val cartSubtotal: StateFlow<Double> = _cartItems.map { items ->
        items.sumOf { it.totalPrice }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartTaxAmount: StateFlow<Double> = combine(cartSubtotal, _posTaxPercent) { subtotal, taxPct ->
        subtotal * (taxPct / 100.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartGrandTotal: StateFlow<Double> = combine(cartSubtotal, _posDiscountAmount, cartTaxAmount) { subtotal, disc, tax ->
        (subtotal - disc + tax).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- NAVIGATION & ROLE SWITCHING ---
    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun toggleLanguage() {
        _currentLanguage.value = if (_currentLanguage.value == Language.ENGLISH) Language.BANGLA else Language.ENGLISH
    }

    fun switchUserRole(role: UserRole) {
        val foundUser = users.value.firstOrNull { it.role == role.name }
        if (foundUser != null) {
            _currentUser.value = foundUser
        } else {
            _currentUser.value = _currentUser.value.copy(role = role.name)
        }
    }

    fun clearMessage() {
        _uiMessage.value = null
    }

    // --- POS ACTIONS ---
    fun updatePosSearchQuery(query: String) {
        _posSearchQuery.value = query
    }

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setPosDiscount(discount: Double) {
        _posDiscountAmount.value = discount
    }

    fun setPosTaxPercent(taxPct: Double) {
        _posTaxPercent.value = taxPct
    }

    fun setPosPaidAmount(amount: Double?) {
        _posPaidAmount.value = amount
    }

    fun setPosPaymentMethod(method: PaymentMethod) {
        _posPaymentMethod.value = method
    }

    fun setPosMobileProvider(provider: String) {
        _posMobileProvider.value = provider
    }

    fun addToCart(medicine: Medicine) {
        viewModelScope.launch {
            val existingItem = _cartItems.value.firstOrNull { it.medicine.id == medicine.id }
            val requestedQty = (existingItem?.quantity ?: 0) + 1

            // FEFO: Allocate from the earliest expiring sellable batch!
            val allocationResult = repository.allocateFefoBatch(medicine.id, requestedQty)
            if (allocationResult.isFailure) {
                _uiMessage.value = allocationResult.exceptionOrNull()?.message ?: "Insufficient stock"
                return@launch
            }

            val allocatedBatch = allocationResult.getOrThrow()

            if (existingItem != null) {
                _cartItems.value = _cartItems.value.map { item ->
                    if (item.medicine.id == medicine.id) {
                        item.copy(quantity = requestedQty, batch = allocatedBatch)
                    } else item
                }
            } else {
                _cartItems.value = _cartItems.value + PosCartItem(
                    medicine = medicine,
                    batch = allocatedBatch,
                    quantity = 1,
                    unitPrice = allocatedBatch.salePrice
                )
            }

            _uiMessage.value = "FEFO: Batch ${allocatedBatch.batchNumber} (Exp: ${allocatedBatch.expiryDate})"
        }
    }

    fun updateCartItemQuantity(medicineId: String, newQty: Int) {
        if (newQty <= 0) {
            removeFromCart(medicineId)
            return
        }

        viewModelScope.launch {
            val item = _cartItems.value.firstOrNull { it.medicine.id == medicineId } ?: return@launch
            val allocationResult = repository.allocateFefoBatch(medicineId, newQty)
            if (allocationResult.isFailure) {
                _uiMessage.value = allocationResult.exceptionOrNull()?.message ?: "Batch quantity exceeded"
                return@launch
            }
            val batch = allocationResult.getOrThrow()
            _cartItems.value = _cartItems.value.map {
                if (it.medicine.id == medicineId) it.copy(quantity = newQty, batch = batch) else it
            }
        }
    }

    fun removeFromCart(medicineId: String) {
        _cartItems.value = _cartItems.value.filterNot { it.medicine.id == medicineId }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _posDiscountAmount.value = 0.0
        _posPaidAmount.value = null
        _selectedCustomer.value = null
    }

    fun holdCurrentCart() {
        if (_cartItems.value.isEmpty()) return
        val title = "Hold #${_heldBills.value.size + 1} (${_selectedCustomer.value?.name ?: "Walk-in"})"
        val bill = HeldBill(
            title = title,
            items = _cartItems.value,
            customer = _selectedCustomer.value
        )
        _heldBills.value = _heldBills.value + bill
        clearCart()
        _uiMessage.value = "Bill held successfully ($title)"
    }

    fun restoreHeldBill(heldBill: HeldBill) {
        _cartItems.value = heldBill.items
        _selectedCustomer.value = heldBill.customer
        _heldBills.value = _heldBills.value.filterNot { it.id == heldBill.id }
        _uiMessage.value = "Restored: ${heldBill.title}"
    }

    fun completeSale() {
        val currentItems = _cartItems.value
        if (currentItems.isEmpty()) {
            _uiMessage.value = "Cart is empty"
            return
        }

        viewModelScope.launch {
            val user = _currentUser.value
            val invoiceNo = repository.generateNextInvoiceNumber()
            val nowTime = System.currentTimeMillis()
            val dateStr = repository.getDhakaDateTimeString(nowTime)
            val sub = cartSubtotal.value
            val disc = _posDiscountAmount.value
            val tax = cartTaxAmount.value
            val grand = cartGrandTotal.value

            val paid = when (_posPaymentMethod.value) {
                PaymentMethod.DUE_CREDIT -> _posPaidAmount.value ?: 0.0
                else -> _posPaidAmount.value ?: grand
            }
            val due = (grand - paid).coerceAtLeast(0.0)
            val change = (paid - grand).coerceAtLeast(0.0)

            val saleId = UUID.randomUUID().toString()
            val sale = Sale(
                id = saleId,
                invoiceNumber = invoiceNo,
                timestamp = nowTime,
                dateTimeStr = dateStr,
                customerId = _selectedCustomer.value?.id ?: "WALK_IN",
                customerName = _selectedCustomer.value?.name ?: "Walk-in Customer",
                customerPhone = _selectedCustomer.value?.phone ?: "",
                subtotal = sub,
                discountAmount = disc,
                taxAmount = tax,
                taxRatePercent = _posTaxPercent.value,
                grandTotal = grand,
                paidAmount = paid,
                dueAmount = due,
                changeReturn = change,
                paymentMethod = _posPaymentMethod.value.name,
                mobileBankingProvider = if (_posPaymentMethod.value == PaymentMethod.MOBILE_BANKING) _posMobileProvider.value else "",
                cashierName = user.name,
                cashierRole = user.role,
                status = "COMPLETED",
                itemsCount = currentItems.size,
                notes = if (due > 0) "৳ $due marked as customer due" else "Payment received"
            )

            val saleItemEntities = currentItems.map { ci ->
                SaleItem(
                    id = UUID.randomUUID().toString(),
                    saleId = saleId,
                    medicineId = ci.medicine.id,
                    medicineName = ci.medicine.name,
                    dosageForm = ci.medicine.dosageForm,
                    strength = ci.medicine.strength,
                    batchId = ci.batch.id,
                    batchNumber = ci.batch.batchNumber,
                    expiryDate = ci.batch.expiryDate,
                    quantity = ci.quantity,
                    unitPrice = ci.unitPrice,
                    purchasePrice = ci.batch.purchasePrice,
                    discountPercent = ci.discountPercent,
                    totalPrice = ci.totalPrice
                )
            }

            val result = repository.completeSale(sale, saleItemEntities, user)
            if (result.isSuccess) {
                _lastCompletedSale.value = sale
                _lastCompletedSaleItems.value = saleItemEntities
                _showInvoiceDialog.value = true
                clearCart()
                _uiMessage.value = "Sale $invoiceNo completed!"
            } else {
                _uiMessage.value = "Sale failed: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun dismissInvoiceDialog() {
        _showInvoiceDialog.value = false
    }

    fun refundSale(saleId: String, reason: String) {
        viewModelScope.launch {
            val res = repository.refundSale(saleId, reason, _currentUser.value)
            if (res.isSuccess) {
                _uiMessage.value = "Sale refunded and stock restored to batches"
            } else {
                _uiMessage.value = "Refund error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    // --- STOCK ADJUSTMENT ---
    fun adjustBatchStock(batchId: String, newQty: Int, reason: String, type: StockMovementType) {
        viewModelScope.launch {
            val res = repository.adjustBatchStock(batchId, newQty, reason, type, _currentUser.value)
            if (res.isSuccess) {
                _uiMessage.value = "Stock adjusted successfully"
            } else {
                _uiMessage.value = "Adjustment error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    // --- PURCHASE RECEIVING ---
    fun receivePurchase(purchase: Purchase, items: List<PurchaseItem>) {
        viewModelScope.launch {
            val res = repository.receivePurchase(purchase, items, _currentUser.value)
            if (res.isSuccess) {
                _uiMessage.value = "Purchase ${purchase.invoiceNumber} received into batch stock!"
            } else {
                _uiMessage.value = "Error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    // --- DUE COLLECTION ---
    fun recordDuePayment(customerId: String, amount: Double, method: String, notes: String) {
        viewModelScope.launch {
            val res = repository.recordDuePayment(customerId, amount, method, notes, _currentUser.value)
            if (res.isSuccess) {
                _uiMessage.value = "Due payment recorded and customer balance updated"
            } else {
                _uiMessage.value = "Error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    // --- EXPENSES ---
    fun addExpense(category: String, title: String, amount: Double, method: String, notes: String) {
        viewModelScope.launch {
            val res = repository.addExpense(category, title, amount, method, notes, _currentUser.value)
            if (res.isSuccess) {
                _uiMessage.value = "Expense recorded"
            } else {
                _uiMessage.value = "Error: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    // --- ENTITY SAVING ---
    fun saveMedicine(medicine: Medicine) {
        viewModelScope.launch {
            repository.saveMedicine(medicine)
            _uiMessage.value = "Medicine ${medicine.name} saved"
        }
    }

    fun saveSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.saveSupplier(supplier)
            _uiMessage.value = "Supplier ${supplier.name} saved"
        }
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _uiMessage.value = "Customer ${customer.name} saved"
        }
    }

    fun saveSettings(settings: PharmacySettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            _uiMessage.value = "Settings updated"
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoData()
            clearCart()
            _uiMessage.value = "Demo data re-seeded to default!"
        }
    }
}
