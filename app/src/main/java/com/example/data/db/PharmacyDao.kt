package com.example.data.db

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PharmacyDao {
    // --- MEDICINES ---
    @Query("SELECT * FROM medicines WHERE isActive = 1 ORDER BY name ASC")
    fun getAllMedicines(): Flow<List<Medicine>>

    @Query("SELECT * FROM medicines WHERE id = :id")
    suspend fun getMedicineById(id: String): Medicine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicine(medicine: Medicine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedicines(medicines: List<Medicine>)

    @Update
    suspend fun updateMedicine(medicine: Medicine)

    @Query("UPDATE medicines SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteMedicine(id: String)

    // --- BATCHES (FEFO: First Expire First Out) ---
    @Query("SELECT * FROM medicine_batches ORDER BY expiryTimestampMillis ASC")
    fun getAllBatches(): Flow<List<MedicineBatch>>

    @Query("SELECT * FROM medicine_batches WHERE medicineId = :medicineId AND status = 'ACTIVE' AND availableQty > 0 ORDER BY expiryTimestampMillis ASC")
    suspend fun getSellableBatchesFefo(medicineId: String): List<MedicineBatch>

    @Query("SELECT * FROM medicine_batches WHERE medicineId = :medicineId ORDER BY expiryTimestampMillis ASC")
    fun getBatchesForMedicine(medicineId: String): Flow<List<MedicineBatch>>

    @Query("SELECT * FROM medicine_batches WHERE id = :batchId")
    suspend fun getBatchById(batchId: String): MedicineBatch?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: MedicineBatch)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatches(batches: List<MedicineBatch>)

    @Update
    suspend fun updateBatch(batch: MedicineBatch)

    @Query("UPDATE medicine_batches SET availableQty = :newQty WHERE id = :batchId")
    suspend fun updateBatchQty(batchId: String, newQty: Int)

    @Query("UPDATE medicine_batches SET status = :status WHERE id = :batchId")
    suspend fun updateBatchStatus(batchId: String, status: String)

    // --- SUPPLIERS ---
    @Query("SELECT * FROM suppliers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: String): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSuppliers(suppliers: List<Supplier>)

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET payableBalance = payableBalance + :delta WHERE id = :id")
    suspend fun adjustSupplierPayable(id: String, delta: Double)

    // --- CUSTOMERS ---
    @Query("SELECT * FROM customers WHERE isActive = 1 ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: String): Customer?

    @Query("SELECT * FROM customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomers(customers: List<Customer>)

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Query("UPDATE customers SET currentDue = currentDue + :delta WHERE id = :id")
    suspend fun adjustCustomerDue(id: String, delta: Double)

    // --- SALES ---
    @Query("SELECT * FROM sales ORDER BY timestamp DESC")
    fun getAllSales(): Flow<List<Sale>>

    @Query("SELECT * FROM sales WHERE id = :id")
    suspend fun getSaleById(id: String): Sale?

    @Query("SELECT * FROM sales WHERE invoiceNumber = :invoiceNumber LIMIT 1")
    suspend fun getSaleByInvoice(invoiceNumber: String): Sale?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSale(sale: Sale)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSales(sales: List<Sale>)

    @Update
    suspend fun updateSale(sale: Sale)

    @Query("SELECT COUNT(*) FROM sales")
    suspend fun getSalesCount(): Int

    // --- SALE ITEMS ---
    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    suspend fun getSaleItems(saleId: String): List<SaleItem>

    @Query("SELECT * FROM sale_items WHERE saleId = :saleId")
    fun getSaleItemsFlow(saleId: String): Flow<List<SaleItem>>

    @Query("SELECT * FROM sale_items")
    fun getAllSaleItems(): Flow<List<SaleItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleItems(items: List<SaleItem>)

    // --- PURCHASES ---
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseById(id: String): Purchase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<Purchase>)

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Query("SELECT * FROM purchase_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseItems(purchaseId: String): List<PurchaseItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseItems(items: List<PurchaseItem>)

    // --- STOCK MOVEMENTS ---
    @Query("SELECT * FROM stock_movements ORDER BY timestamp DESC LIMIT 200")
    fun getRecentStockMovements(): Flow<List<StockMovement>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovement(movement: StockMovement)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStockMovements(movements: List<StockMovement>)

    // --- CUSTOMER DUE PAYMENTS ---
    @Query("SELECT * FROM customer_due_payments ORDER BY timestamp DESC")
    fun getAllDuePayments(): Flow<List<CustomerDuePayment>>

    @Query("SELECT * FROM customer_due_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getDuePaymentsForCustomer(customerId: String): Flow<List<CustomerDuePayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuePayment(payment: CustomerDuePayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuePayments(payments: List<CustomerDuePayment>)

    // --- EXPENSES ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)

    // --- CASHIER SHIFTS ---
    @Query("SELECT * FROM cashier_shifts ORDER BY openedAt DESC LIMIT 1")
    fun getActiveShift(): Flow<CashierShift?>

    @Query("SELECT * FROM cashier_shifts ORDER BY openedAt DESC")
    fun getAllShifts(): Flow<List<CashierShift>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShift(shift: CashierShift)

    @Update
    suspend fun updateShift(shift: CashierShift)

    // --- AUDIT LOGS ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 300")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLog>)

    // --- PHARMACY SETTINGS ---
    @Query("SELECT * FROM pharmacy_settings WHERE id = 1 LIMIT 1")
    fun getSettingsFlow(): Flow<PharmacySettings?>

    @Query("SELECT * FROM pharmacy_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettings(): PharmacySettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: PharmacySettings)

    // --- APP USERS ---
    @Query("SELECT * FROM app_users WHERE isActive = 1")
    fun getAllUsers(): Flow<List<AppUser>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<AppUser>)

    @Update
    suspend fun updateUser(user: AppUser)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: AppUser)
}
