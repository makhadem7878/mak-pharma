package com.example.data.model

enum class UserRole(val displayNameEn: String, val displayNameBn: String) {
    OWNER_ADMIN("Owner / Admin", "মালিক / অ্যাডমিন"),
    PHARMACIST_MANAGER("Pharmacist / Manager", "ফার্মাসিস্ট / ম্যানেজার"),
    CASHIER("Cashier", "ক্যাশিয়ার"),
    STOREKEEPER("Storekeeper", "স্টোরকিপার"),
    VIEWER_ACCOUNTANT("Viewer / Accountant", "দর্শক / হিসাবরক্ষক");

    fun canAccessPos(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, CASHIER)
    fun canManageInventory(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, STOREKEEPER)
    fun canManagePurchases(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, STOREKEEPER)
    fun canManageUsers(): Boolean = this == OWNER_ADMIN
    fun canViewAuditLogs(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER)
    fun canViewFinancialReports(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, VIEWER_ACCOUNTANT)
    fun canCollectDue(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, CASHIER)
    fun canAdjustStock(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER, STOREKEEPER)
    fun canRefundSale(): Boolean = this in listOf(OWNER_ADMIN, PHARMACIST_MANAGER)
    fun canChangeSettings(): Boolean = this == OWNER_ADMIN
}

enum class DosageForm(val labelEn: String, val labelBn: String) {
    TABLET("Tablet", "ট্যাবলেট"),
    CAPSULE("Capsule", "ক্যাপসুল"),
    SYRUP("Syrup", "সিরাপ"),
    SUSPENSION("Suspension", "সাসপেনশন"),
    INJECTION("Injection", "ইনজেকশন"),
    CREAM_OINTMENT("Cream/Ointment", "ক্রিম/মলম"),
    DROPS("Eye/Ear Drops", "ড্রপস"),
    INHALER("Inhaler", "ইনহেলার"),
    POWDER_SACHET("Powder/Sachet", "পাউডার/স্যাচে"),
    SUPPOSITORY("Suppository", "সাপোজিটরি")
}

enum class PaymentMethod(val labelEn: String, val labelBn: String) {
    CASH("Cash", "নগদ"),
    MOBILE_BANKING("Mobile Banking", "মোবাইল ব্যাংকিং"),
    CARD("Debit/Credit Card", "কার্ড"),
    BANK_TRANSFER("Bank Transfer", "ব্যাংক ট্রান্সফার"),
    DUE_CREDIT("Due / Credit", "বাকি"),
    MIXED("Mixed Payment", "মিশ্র পেমেন্ট")
}

enum class BatchStatus(val labelEn: String, val labelBn: String) {
    ACTIVE("Active", "সক্রিয়"),
    DAMAGED("Damaged", "ক্ষতিগ্রস্ত"),
    EXPIRED("Expired", "মেয়াদোত্তীর্ণ"),
    QUARANTINE("Quarantine", "কোয়ারেন্টাইন")
}

enum class StockMovementType(val labelEn: String, val labelBn: String) {
    SALE("Sale", "বিক্রয়"),
    SALE_RETURN("Sale Refund / Return", "বিক্রয় ফেরত"),
    PURCHASE_RECEIVE("Purchase Received", "ক্রয় জমা"),
    PURCHASE_RETURN("Purchase Return", "ক্রয় ফেরত"),
    ADJUSTMENT_ADD("Stock Adjustment (+)", "স্টক সমন্বয় (+)"),
    ADJUSTMENT_SUB("Stock Adjustment (-)", "স্টক সমন্বয় (-)"),
    PHYSICAL_COUNT_DISCREPANCY("Count Discrepancy", "গণনা গরমিল")
}
