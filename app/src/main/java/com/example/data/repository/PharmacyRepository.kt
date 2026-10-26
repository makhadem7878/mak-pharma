package com.example.data.repository

import com.example.data.db.PharmacyDao
import com.example.data.model.*
import com.example.data.seed.DemoData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class PharmacyRepository(private val dao: PharmacyDao) {

    val allMedicines: Flow<List<Medicine>> = dao.getAllMedicines()
    val allBatches: Flow<List<MedicineBatch>> = dao.getAllBatches()
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val allSales: Flow<List<Sale>> = dao.getAllSales()
    val allSaleItems: Flow<List<SaleItem>> = dao.getAllSaleItems()
    val allPurchases: Flow<List<Purchase>> = dao.getAllPurchases()
    val recentStockMovements: Flow<List<StockMovement>> = dao.getRecentStockMovements()
    val allDuePayments: Flow<List<CustomerDuePayment>> = dao.getAllDuePayments()
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    val activeShift: Flow<CashierShift?> = dao.getActiveShift()
    val allShifts: Flow<List<CashierShift>> = dao.getAllShifts()
    val allAuditLogs: Flow<List<AuditLog>> = dao.getAllAuditLogs()
    val settingsFlow: Flow<PharmacySettings?> = dao.getSettingsFlow()
    val allUsers: Flow<List<AppUser>> = dao.getAllUsers()

    private val dhakaTimeZone = TimeZone.getTimeZone("Asia/Dhaka")

    fun getDhakaDateTimeString(timeMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.US)
        sdf.timeZone = dhakaTimeZone
        return sdf.format(Date(timeMillis))
    }

    private fun getDhakaDateCode(timeMillis: Long = System.currentTimeMillis()): String {
        val sdf = SimpleDateFormat("yyyyMMdd", java.util.Locale.US)
        sdf.timeZone = dhakaTimeZone
        return sdf.format(Date(timeMillis))
    }

    suspend fun checkAndPrepopulate() = withContext(Dispatchers.IO) {
        val existingSettings = dao.getSettings()
        if (existingSettings == null) {
            populateDefaults()
        }
    }

    suspend fun resetDemoData() = withContext(Dispatchers.IO) {
        populateDefaults()
    }

    private suspend fun populateDefaults() {
        dao.insertSettings(DemoData.defaultSettings)
        dao.insertUsers(DemoData.users)
        dao.insertSuppliers(DemoData.suppliers)
        dao.insertCustomers(DemoData.customers)
        dao.insertMedicines(DemoData.medicines)
        dao.insertBatches(DemoData.batches)
        dao.insertSales(DemoData.sampleSales)
        dao.insertSaleItems(DemoData.sampleSaleItems)
        dao.insertPurchases(DemoData.samplePurchases)
        dao.insertPurchaseItems(DemoData.samplePurchaseItems)
        dao.insertStockMovements(DemoData.sampleStockMovements)
        dao.insertExpenses(DemoData.sampleExpenses)
        dao.insertDuePayments(DemoData.sampleDuePayments)
        dao.insertShift(DemoData.initialShift)
        dao.insertAuditLogs(DemoData.sampleAuditLogs)
    }

    // --- POS & FEFO ALLOCATION ---
    data class FefoBatchAllocation(
        val batch: MedicineBatch,
        val allocatedQty: Int
    )

    suspend fun allocateFefoBatch(medicineId: String, requestedQty: Int): Result<MedicineBatch> = withContext(Dispatchers.IO) {
        val sellableBatches = dao.getSellableBatchesFefo(medicineId)
        if (sellableBatches.isEmpty()) {
            return@withContext Result.failure(Exception("No sellable batch available for this medicine"))
        }

        // FEFO: Take the earliest non-expired batch with enough stock
        val candidate = sellableBatches.firstOrNull { it.availableQty >= requestedQty }
            ?: sellableBatches.first()

        if (candidate.availableQty < requestedQty) {
            return@withContext Result.failure(Exception("Only ${candidate.availableQty} available in earliest batch ${candidate.batchNumber}"))
        }

        Result.success(candidate)
    }

    suspend fun completeSale(
        sale: Sale,
        items: List<SaleItem>,
        currentUser: AppUser
    ): Result<Sale> = withContext(Dispatchers.IO) {
        try {
            // Validate and deduct stock for each batch
            for (item in items) {
                val batch = dao.getBatchById(item.batchId)
                    ?: return@withContext Result.failure(Exception("Batch ${item.batchNumber} not found"))
                if (batch.availableQty < item.quantity) {
                    return@withContext Result.failure(Exception("Batch ${batch.batchNumber} has only ${batch.availableQty} units"))
                }
                val newQty = batch.availableQty - item.quantity
                dao.updateBatchQty(batch.id, newQty)

                // Log movement
                val movement = StockMovement(
                    id = UUID.randomUUID().toString(),
                    timestamp = sale.timestamp,
                    dateTimeStr = sale.dateTimeStr,
                    medicineId = item.medicineId,
                    medicineName = item.medicineName,
                    batchId = batch.id,
                    batchNumber = batch.batchNumber,
                    type = StockMovementType.SALE.name,
                    quantityChange = -item.quantity,
                    remainingQty = newQty,
                    referenceId = sale.invoiceNumber,
                    reason = "POS Sale (FEFO Batch ${batch.batchNumber})",
                    performedBy = currentUser.name
                )
                dao.insertStockMovement(movement)
            }

            // Customer due update
            if (sale.customerId != "WALK_IN" && sale.dueAmount > 0) {
                dao.adjustCustomerDue(sale.customerId, sale.dueAmount)
            }

            // Insert sale and items
            dao.insertSale(sale)
            dao.insertSaleItems(items)

            // Audit log
            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = sale.timestamp,
                    dateTimeStr = sale.dateTimeStr,
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "SALE_CREATED",
                    entityName = "Sale",
                    entityId = sale.invoiceNumber,
                    details = "Sale completed for ৳ ${sale.grandTotal} (${sale.paymentMethod}). Total items: ${items.size}"
                )
            )

            Result.success(sale)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refundSale(
        saleId: String,
        reason: String,
        currentUser: AppUser
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val sale = dao.getSaleById(saleId) ?: return@withContext Result.failure(Exception("Sale not found"))
            if (sale.status == "REFUNDED") {
                return@withContext Result.failure(Exception("Sale is already refunded"))
            }

            val items = dao.getSaleItems(saleId)
            val nowTime = System.currentTimeMillis()
            val dateStr = getDhakaDateTimeString(nowTime)

            // Restore batch quantities
            for (item in items) {
                val batch = dao.getBatchById(item.batchId)
                if (batch != null) {
                    val restoredQty = batch.availableQty + item.quantity
                    dao.updateBatchQty(batch.id, restoredQty)

                    dao.insertStockMovement(
                        StockMovement(
                            id = UUID.randomUUID().toString(),
                            timestamp = nowTime,
                            dateTimeStr = dateStr,
                            medicineId = item.medicineId,
                            medicineName = item.medicineName,
                            batchId = batch.id,
                            batchNumber = batch.batchNumber,
                            type = StockMovementType.SALE_RETURN.name,
                            quantityChange = item.quantity,
                            remainingQty = restoredQty,
                            referenceId = sale.invoiceNumber,
                            reason = "Sale Refund: $reason",
                            performedBy = currentUser.name
                        )
                    )
                }
            }

            // Revert customer due if applicable
            if (sale.customerId != "WALK_IN" && sale.dueAmount > 0) {
                dao.adjustCustomerDue(sale.customerId, -sale.dueAmount)
            }

            // Update sale status
            dao.updateSale(sale.copy(status = "REFUNDED", notes = "${sale.notes} [REFUNDED: $reason]"))

            // Audit log
            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "SALE_REFUNDED",
                    entityName = "Sale",
                    entityId = sale.invoiceNumber,
                    details = "Sale ${sale.invoiceNumber} refunded for ৳ ${sale.grandTotal}. Reason: $reason"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateNextInvoiceNumber(): String = withContext(Dispatchers.IO) {
        val settings = dao.getSettings() ?: DemoData.defaultSettings
        val prefix = settings.invoicePrefix
        val dateCode = getDhakaDateCode()
        val count = dao.getSalesCount() + 1
        String.format("%s-%s-%04d", prefix, dateCode, count)
    }

    // --- STOCK ADJUSTMENT ---
    suspend fun adjustBatchStock(
        batchId: String,
        newQuantity: Int,
        reason: String,
        adjustmentType: StockMovementType,
        currentUser: AppUser
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val batch = dao.getBatchById(batchId) ?: return@withContext Result.failure(Exception("Batch not found"))
            val med = dao.getMedicineById(batch.medicineId)
            val diff = newQuantity - batch.availableQty
            val nowTime = System.currentTimeMillis()
            val dateStr = getDhakaDateTimeString(nowTime)

            dao.updateBatchQty(batchId, newQuantity)

            dao.insertStockMovement(
                StockMovement(
                    id = UUID.randomUUID().toString(),
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    medicineId = batch.medicineId,
                    medicineName = med?.name ?: "Medicine",
                    batchId = batch.id,
                    batchNumber = batch.batchNumber,
                    type = adjustmentType.name,
                    quantityChange = diff,
                    remainingQty = newQuantity,
                    referenceId = "ADJ-${batch.batchNumber}",
                    reason = reason,
                    performedBy = currentUser.name
                )
            )

            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "STOCK_ADJUSTED",
                    entityName = "MedicineBatch",
                    entityId = batch.batchNumber,
                    details = "Stock adjusted from ${batch.availableQty} to $newQuantity (diff: $diff). Reason: $reason"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- PURCHASE RECEIVING ---
    suspend fun receivePurchase(
        purchase: Purchase,
        items: List<PurchaseItem>,
        currentUser: AppUser
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            dao.insertPurchase(purchase)
            dao.insertPurchaseItems(items)

            for (item in items) {
                val totalQty = item.quantity + item.bonusQuantity
                val batchId = "bat_${UUID.randomUUID().toString().take(8)}"
                val sdf = SimpleDateFormat("dd/MM/yyyy", java.util.Locale.US)
                val expDate = try { sdf.parse(item.expiryDate)?.time ?: (System.currentTimeMillis() + 365L * 86400000L) } catch (_: Exception) { System.currentTimeMillis() + 365L * 86400000L }

                val batch = MedicineBatch(
                    id = batchId,
                    medicineId = item.medicineId,
                    batchNumber = item.batchNumber,
                    mfgDate = getDhakaDateTimeString().split(" ").first(),
                    expiryDate = item.expiryDate,
                    expiryTimestampMillis = expDate,
                    purchasePrice = item.purchasePrice,
                    mrp = item.mrp,
                    salePrice = item.salePrice,
                    availableQty = totalQty,
                    supplierId = purchase.supplierId,
                    supplierName = purchase.supplierName,
                    receivedDate = purchase.purchaseDate
                )
                dao.insertBatch(batch)

                dao.insertStockMovement(
                    StockMovement(
                        id = UUID.randomUUID().toString(),
                        timestamp = purchase.timestamp,
                        dateTimeStr = getDhakaDateTimeString(purchase.timestamp),
                        medicineId = item.medicineId,
                        medicineName = item.medicineName,
                        batchId = batchId,
                        batchNumber = item.batchNumber,
                        type = StockMovementType.PURCHASE_RECEIVE.name,
                        quantityChange = totalQty,
                        remainingQty = totalQty,
                        referenceId = purchase.invoiceNumber,
                        reason = "Purchase Invoice ${purchase.invoiceNumber}",
                        performedBy = currentUser.name
                    )
                )
            }

            if (purchase.dueAmount > 0) {
                dao.adjustSupplierPayable(purchase.supplierId, purchase.dueAmount)
            }

            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = purchase.timestamp,
                    dateTimeStr = getDhakaDateTimeString(purchase.timestamp),
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "PURCHASE_RECEIVED",
                    entityName = "Purchase",
                    entityId = purchase.invoiceNumber,
                    details = "Purchase received: ${purchase.invoiceNumber} from ${purchase.supplierName} for ৳ ${purchase.totalAmount}"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DUE COLLECTION ---
    suspend fun recordDuePayment(
        customerId: String,
        amount: Double,
        paymentMethod: String,
        notes: String,
        currentUser: AppUser
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val customer = dao.getCustomerById(customerId) ?: return@withContext Result.failure(Exception("Customer not found"))
            val nowTime = System.currentTimeMillis()
            val dateStr = getDhakaDateTimeString(nowTime)
            val receiptNo = "DUE-${getDhakaDateCode()}-${UUID.randomUUID().toString().take(4).uppercase()}"

            dao.adjustCustomerDue(customerId, -amount)

            dao.insertDuePayment(
                CustomerDuePayment(
                    id = UUID.randomUUID().toString(),
                    customerId = customerId,
                    customerName = customer.name,
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    amountPaid = amount,
                    paymentMethod = paymentMethod,
                    receiptNumber = receiptNo,
                    notes = notes,
                    collectedBy = currentUser.name
                )
            )

            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "DUE_COLLECTED",
                    entityName = "CustomerDuePayment",
                    entityId = receiptNo,
                    details = "Due payment of ৳ $amount collected from ${customer.name} via $paymentMethod"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- EXPENSES ---
    suspend fun addExpense(
        category: String,
        title: String,
        amount: Double,
        paymentMethod: String,
        notes: String,
        currentUser: AppUser
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val nowTime = System.currentTimeMillis()
            val dateStr = getDhakaDateTimeString(nowTime)

            dao.insertExpense(
                Expense(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    title = title,
                    amount = amount,
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    paymentMethod = paymentMethod,
                    recordedBy = currentUser.name,
                    notes = notes
                )
            )

            dao.insertAuditLog(
                AuditLog(
                    id = UUID.randomUUID().toString(),
                    timestamp = nowTime,
                    dateTimeStr = dateStr,
                    userRole = currentUser.role,
                    userName = currentUser.name,
                    actionType = "EXPENSE_ADDED",
                    entityName = "Expense",
                    entityId = title,
                    details = "Expense of ৳ $amount added under $category"
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- CRUD OPERATIONS ---
    suspend fun saveMedicine(medicine: Medicine) = withContext(Dispatchers.IO) { dao.insertMedicine(medicine) }
    suspend fun saveSupplier(supplier: Supplier) = withContext(Dispatchers.IO) { dao.insertSupplier(supplier) }
    suspend fun saveCustomer(customer: Customer) = withContext(Dispatchers.IO) { dao.insertCustomer(customer) }
    suspend fun saveSettings(settings: PharmacySettings) = withContext(Dispatchers.IO) { dao.insertSettings(settings) }
    suspend fun getSaleItems(saleId: String) = withContext(Dispatchers.IO) { dao.getSaleItems(saleId) }
}
