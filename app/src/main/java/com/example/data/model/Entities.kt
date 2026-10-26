package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "medicines",
    indices = [
        Index("barcode"),
        Index("name"),
        Index("genericName"),
        Index("brand")
    ]
)
data class Medicine(
    @PrimaryKey val id: String,
    val name: String,
    val banglaName: String,
    val genericName: String,
    val brand: String,
    val dosageForm: String, // from DosageForm
    val strength: String,
    val unit: String, // Piece, Strip, Bottle, Box
    val packSize: Int = 10,
    val category: String,
    val barcode: String,
    val sku: String,
    val prescriptionRequired: Boolean = false,
    val reorderLevel: Int = 20,
    val defaultSalePrice: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(
    tableName = "medicine_batches",
    indices = [
        Index("medicineId"),
        Index("batchNumber"),
        Index("expiryTimestampMillis"),
        Index("status")
    ]
)
data class MedicineBatch(
    @PrimaryKey val id: String,
    val medicineId: String,
    val batchNumber: String,
    val mfgDate: String, // DD/MM/YYYY
    val expiryDate: String, // DD/MM/YYYY
    val expiryTimestampMillis: Long,
    val purchasePrice: Double,
    val mrp: Double,
    val salePrice: Double,
    val availableQty: Int,
    val supplierId: String,
    val supplierName: String,
    val receivedDate: String,
    val storageLocation: String = "Rack A-1",
    val status: String = "ACTIVE" // ACTIVE, DAMAGED, EXPIRED, QUARANTINE
)

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey val id: String,
    val name: String,
    val contactPerson: String,
    val phone: String,
    val email: String,
    val address: String,
    val tradeLicense: String = "",
    val openingPayable: Double = 0.0,
    val payableBalance: Double = 0.0,
    val isActive: Boolean = true
)

@Entity(
    tableName = "customers",
    indices = [Index("phone")]
)
data class Customer(
    @PrimaryKey val id: String,
    val name: String,
    val phone: String,
    val address: String,
    val creditLimit: Double = 2000.0,
    val currentDue: Double = 0.0,
    val notes: String = "",
    val isActive: Boolean = true
)

@Entity(
    tableName = "sales",
    indices = [Index("invoiceNumber"), Index("timestamp"), Index("customerId")]
)
data class Sale(
    @PrimaryKey val id: String,
    val invoiceNumber: String, // e.g. PCB-20261026-0001
    val timestamp: Long,
    val dateTimeStr: String,
    val customerId: String, // "WALK_IN" or customer id
    val customerName: String,
    val customerPhone: String,
    val subtotal: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val taxRatePercent: Double,
    val rounding: Double = 0.0,
    val grandTotal: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val changeReturn: Double,
    val paymentMethod: String, // CASH, CARD, MOBILE_BANKING, etc.
    val mobileBankingProvider: String = "", // bKash, Nagad, Rocket
    val cashierName: String,
    val cashierRole: String,
    val status: String = "COMPLETED", // COMPLETED, REFUNDED, DRAFT
    val itemsCount: Int = 1,
    val notes: String = ""
)

@Entity(
    tableName = "sale_items",
    indices = [Index("saleId"), Index("medicineId"), Index("batchId")]
)
data class SaleItem(
    @PrimaryKey val id: String,
    val saleId: String,
    val medicineId: String,
    val medicineName: String,
    val dosageForm: String,
    val strength: String,
    val batchId: String,
    val batchNumber: String,
    val expiryDate: String,
    val quantity: Int,
    val unitPrice: Double,
    val purchasePrice: Double, // For gross profit tracking
    val discountPercent: Double = 0.0,
    val totalPrice: Double
)

@Entity(
    tableName = "purchases",
    indices = [Index("invoiceNumber"), Index("supplierId"), Index("timestamp")]
)
data class Purchase(
    @PrimaryKey val id: String,
    val invoiceNumber: String,
    val supplierId: String,
    val supplierName: String,
    val purchaseDate: String,
    val timestamp: Long,
    val totalAmount: Double,
    val paidAmount: Double,
    val dueAmount: Double,
    val status: String = "RECEIVED", // RECEIVED, RETURNED
    val notes: String = ""
)

@Entity(
    tableName = "purchase_items",
    indices = [Index("purchaseId"), Index("medicineId")]
)
data class PurchaseItem(
    @PrimaryKey val id: String,
    val purchaseId: String,
    val medicineId: String,
    val medicineName: String,
    val batchNumber: String,
    val expiryDate: String,
    val quantity: Int,
    val bonusQuantity: Int = 0,
    val purchasePrice: Double,
    val mrp: Double,
    val salePrice: Double,
    val total: Double
)

@Entity(
    tableName = "stock_movements",
    indices = [Index("medicineId"), Index("batchId"), Index("timestamp")]
)
data class StockMovement(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val dateTimeStr: String,
    val medicineId: String,
    val medicineName: String,
    val batchId: String,
    val batchNumber: String,
    val type: String, // SALE, SALE_RETURN, PURCHASE_RECEIVE, ADJUSTMENT_ADD, ADJUSTMENT_SUB, etc.
    val quantityChange: Int, // e.g. -5, +20
    val remainingQty: Int,
    val referenceId: String, // Invoice id or purchase id
    val reason: String,
    val performedBy: String
)

@Entity(
    tableName = "customer_due_payments",
    indices = [Index("customerId"), Index("timestamp")]
)
data class CustomerDuePayment(
    @PrimaryKey val id: String,
    val customerId: String,
    val customerName: String,
    val timestamp: Long,
    val dateTimeStr: String,
    val amountPaid: Double,
    val paymentMethod: String,
    val receiptNumber: String,
    val notes: String,
    val collectedBy: String
)

@Entity(
    tableName = "expenses",
    indices = [Index("timestamp"), Index("category")]
)
data class Expense(
    @PrimaryKey val id: String,
    val category: String,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val dateTimeStr: String,
    val paymentMethod: String,
    val recordedBy: String,
    val notes: String = ""
)

@Entity(tableName = "cashier_shifts")
data class CashierShift(
    @PrimaryKey val id: String,
    val cashierName: String,
    val role: String,
    val openedAt: Long,
    val closedAt: Long? = null,
    val openingCash: Double,
    val cashSales: Double = 0.0,
    val cashDueCollected: Double = 0.0,
    val cashExpenses: Double = 0.0,
    val expectedCash: Double = 0.0,
    val actualCash: Double? = null,
    val status: String = "OPEN" // OPEN, CLOSED
)

@Entity(
    tableName = "audit_logs",
    indices = [Index("timestamp"), Index("actionType")]
)
data class AuditLog(
    @PrimaryKey val id: String,
    val timestamp: Long,
    val dateTimeStr: String,
    val userRole: String,
    val userName: String,
    val actionType: String,
    val entityName: String,
    val entityId: String,
    val details: String
)

@Entity(tableName = "pharmacy_settings")
data class PharmacySettings(
    @PrimaryKey val id: Int = 1,
    val pharmacyName: String = "MAK Pharma BD",
    val pharmacyBanglaName: String = "ম্যাক ফার্মা বিডি",
    val address: String = "House #14, Road #7, Dhanmondi, Dhaka-1205",
    val phone: String = "01700-112233 / 02-9876543",
    val email: String = "info@makpharmabd.com",
    val invoicePrefix: String = "PCB",
    val defaultVatPercent: Double = 0.0,
    val defaultDiscountLimitPercent: Double = 15.0,
    val expiryAlert30Days: Int = 30,
    val expiryAlert60Days: Int = 60,
    val expiryAlert90Days: Int = 90,
    val lowStockThresholdDefault: Int = 20,
    val invoiceFooterNoteEn: String = "Get well soon! Prescribed medicines only. Non-refrigerated medicines returnable within 7 days with invoice.",
    val invoiceFooterNoteBn: String = "আপনার দ্রুত আরোগ্য কামনা করি! প্রেসক্রিপশন ছাড়া ওষুধ বিক্রয় করা হয় না। ৭ দিনের মধ্যে ক্যাশ মেমোসহ ফেরতযোগ্য।"
)

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val role: String, // OWNER_ADMIN, PHARMACIST_MANAGER, CASHIER, STOREKEEPER, VIEWER_ACCOUNTANT
    val phone: String,
    val isActive: Boolean = true
)
