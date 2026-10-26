package com.example.ui.locale

enum class Language(val code: String, val label: String) {
    ENGLISH("en", "English"),
    BANGLA("bn", "বাংলা")
}

object AppLocale {
    fun formatCurrency(amount: Double, language: Language = Language.ENGLISH): String {
        val formatted = String.format("%.2f", amount)
        return if (language == Language.BANGLA) {
            "৳ ${toBanglaDigits(formatted)}"
        } else {
            "৳ $formatted"
        }
    }

    fun toBanglaDigits(input: String): String {
        val banglaDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(banglaDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun get(key: String, language: Language): String {
        val entry = strings[key] ?: return key
        return if (language == Language.BANGLA) entry.second else entry.first
    }

    private val strings = mapOf(
        "app_name" to Pair("MAK Pharma BD", "ম্যাক ফার্মা বিডি"),
        "app_subtitle" to Pair("Retail Pharmacy Management System", "খুচরা ফার্মেসি ব্যবস্থাপনা সিস্টেম"),
        "dashboard" to Pair("Dashboard", "ড্যাশবোর্ড"),
        "pos" to Pair("POS Billing", "কাউন্টার বিক্রয় / POS"),
        "sales" to Pair("Sales History", "বিক্রয় তালিকা"),
        "inventory" to Pair("Inventory & Stock", "ওষুধ ও স্টক"),
        "purchases" to Pair("Purchases", "ক্রয় চালান"),
        "suppliers" to Pair("Suppliers", "সরবরাহকারী"),
        "customers" to Pair("Customers & Due", "গ্রাহক ও বাকি"),
        "expenses" to Pair("Expenses & Shift", "খরচ ও ক্যাশ শিফট"),
        "reports" to Pair("Reports", "রিপোর্ট ও অ্যানালিটিক্স"),
        "users" to Pair("User Roles", "ব্যবহারকারী ও পদবি"),
        "settings" to Pair("Settings", "সেটিংস"),
        "audit_logs" to Pair("Audit Logs", "নিরাপত্তা ও অডিট লগ"),

        // Dashboard Metrics
        "todays_sales" to Pair("Today's Sales", "আজকের মোট বিক্রয়"),
        "profit_estimate" to Pair("Est. Gross Profit", "আনুমানিক মোট লাভ"),
        "total_invoices" to Pair("Invoices Today", "আজকের ইনভয়েস"),
        "purchase_amount" to Pair("Purchases Today", "আজকের ক্রয়"),
        "due_collected" to Pair("Due Collected Today", "আজকের বাকি আদায়"),
        "low_stock" to Pair("Low Stock Items", "কম স্টকের ওষুধ"),
        "out_of_stock" to Pair("Out of Stock", "স্টক শেষ"),
        "expiring_30_days" to Pair("Expiring < 30 Days", "৩০ দিনের মধ্যে মেয়াদ শেষ"),
        "expiring_60_days" to Pair("Expiring < 60 Days", "৬০ দিনের মধ্যে মেয়াদ শেষ"),
        "expiring_90_days" to Pair("Expiring < 90 Days", "৯০ দিনের মধ্যে মেয়াদ শেষ"),
        "recent_sales" to Pair("Recent Sales", "সাম্প্রতিক বিক্রয়"),
        "quick_actions" to Pair("Quick Actions", "দ্রুত অ্যাকশন"),
        "new_sale" to Pair("New Sale (POS)", "নতুন বিক্রয় (POS)"),
        "add_medicine" to Pair("Add Medicine", "নতুন ওষুধ যোগ"),
        "add_purchase" to Pair("New Purchase", "নতুন চালান গ্রহণ"),
        "add_customer" to Pair("Add Customer", "নতুন গ্রাহক যোগ"),
        "collect_due" to Pair("Collect Due", "বাকি টাকা আদায়"),
        "view_all" to Pair("View All", "সব দেখুন"),

        // POS
        "search_medicine_placeholder" to Pair("Search by name, generic, brand, barcode...", "ওষুধের নাম, জেনেরিক, কোম্পানি বা বারকোড খুঁজুন..."),
        "cart" to Pair("Sale Cart", "বিক্রয় কার্ট"),
        "empty_cart" to Pair("No items in cart. Search or scan to add medicines.", "কার্ট খালি। ওষুধ সার্চ করে বা স্ক্যান করে যোগ করুন।"),
        "walk_in_customer" to Pair("Walk-in Customer", "সাধারণ খদ্দের (ওয়াক-ইন)"),
        "select_customer" to Pair("Select Customer", "গ্রাহক নির্বাচন"),
        "fefo_badge" to Pair("FEFO: Earliest Expiry Batch", "FEFO: নিকটতম মেয়াদের ব্যাচ"),
        "subtotal" to Pair("Subtotal", "উপ-মোট"),
        "discount" to Pair("Discount", "ছাড় / ডিসকাউন্ট"),
        "vat_tax" to Pair("VAT / Tax", "ভ্যাট / ট্যাক্স"),
        "grand_total" to Pair("Grand Total", "সর্বমোট টাকা"),
        "paid_amount" to Pair("Paid Amount", "জমা / প্রাপ্ত টাকা"),
        "due_amount" to Pair("Due / Receivable", "বাকি টাকা"),
        "change_return" to Pair("Change Return", "ফেরত টাকা"),
        "complete_sale" to Pair("Complete Sale", "বিক্রয় সম্পন্ন করুন"),
        "hold_cart" to Pair("Hold Bill", "বিল হোল্ড করুন"),
        "held_bills" to Pair("Held Bills", "হোল্ড করা বিল"),
        "clear_cart" to Pair("Clear Cart", "কার্ট পরিষ্কার করুন"),
        "fefo_warning" to Pair("Automatic FEFO batch selected for optimal shelf life.", "উন্নত ব্যবস্থাপনার জন্য নিকটতম মেয়াদের ব্যাচ নির্বাচন করা হয়েছে।"),
        "insufficient_stock" to Pair("Insufficient sellable stock in batch!", "ব্যাচে পর্যাপ্ত বিক্রয়যোগ্য স্টক নেই!"),
        "payment_method" to Pair("Payment Method", "পরিশোধ পদ্ধতি"),

        // Invoice
        "invoice_preview" to Pair("Invoice Receipt", "ক্যাশ মেমো / ইনভয়েস"),
        "print_share" to Pair("Print / Share", "প্রিন্ট / শেয়ার"),
        "download_pdf" to Pair("Download Invoice", "ইনভয়েস ডাউনলোড"),
        "invoice_no" to Pair("Invoice #", "ইনভয়েস নং"),
        "date_time" to Pair("Date & Time", "তারিখ ও সময়"),
        "cashier" to Pair("Cashier", "ক্যাশিয়ার"),
        "customer" to Pair("Customer", "গ্রাহক"),
        "item" to Pair("Item", "বিবরণ"),
        "batch" to Pair("Batch", "ব্যাচ"),
        "exp" to Pair("Exp", "মেয়াদ"),
        "qty" to Pair("Qty", "পরিমাণ"),
        "price" to Pair("Price", "দর"),
        "total" to Pair("Total", "মোট"),
        "status" to Pair("Status", "অবস্থা"),
        "refund_sale" to Pair("Refund / Return Sale", "বিক্রয় ফেরত / রিফান্ড"),

        // Inventory
        "all_medicines" to Pair("All Medicines", "সকল ওষুধ"),
        "batches" to Pair("Batches & Expiry", "ব্যাচ ও মেয়াদ"),
        "stock_movements" to Pair("Stock Movements", "স্টক চলাচল খতিয়ান"),
        "stock_adjustment" to Pair("Adjust Stock", "স্টক সমন্বয়"),
        "reorder_alert" to Pair("Needs Reorder", "পুনরায় অর্ডার প্রয়োজন"),
        "active_stock" to Pair("Active Stock", "মজুদ পরিমাণ"),
        "rack_location" to Pair("Location", "র‍্যাক / শেলফ"),
        "dosage" to Pair("Dosage Form", "ওষুধের ধরন"),
        "manufacturer" to Pair("Manufacturer", "প্রস্তুতকারক"),
        "generic" to Pair("Generic Name", "জেনেরিক নাম"),
        "prescription_req" to Pair("Prescription Required", "প্রেসক্রিপশন আবশ্যক"),

        // Purchases
        "purchase_invoices" to Pair("Purchase Invoices", "ক্রয় চালান সমূহ"),
        "receive_new_purchase" to Pair("Receive Purchase", "নতুন মালামাল গ্রহণ"),
        "supplier" to Pair("Supplier", "সরবরাহকারী"),
        "payable" to Pair("Payable Balance", "মোট দেনা"),

        // Customers
        "customer_list" to Pair("Customer Directory", "গ্রাহক তালিকা"),
        "credit_limit" to Pair("Credit Limit", "বাকির সর্বোচ্চ সীমা"),
        "current_due" to Pair("Current Due", "বর্তমান বাকি"),
        "record_payment" to Pair("Record Due Payment", "বাকি পরিশোধ গ্রহণ"),

        // Common
        "save" to Pair("Save", "সংরক্ষণ"),
        "cancel" to Pair("Cancel", "বাতিল"),
        "close" to Pair("Close", "বন্ধ"),
        "confirm" to Pair("Confirm", "নিশ্চিত করুন"),
        "search" to Pair("Search...", "অনুসন্ধান..."),
        "filter" to Pair("Filter", "ফিল্টার"),
        "export_csv" to Pair("Export CSV", "সিএসভি এক্সপোর্ট"),
        "reset_demo" to Pair("Reset Demo Data", "ডেমো ডাটা রিসেট"),
        "role" to Pair("Current Role", "বর্তমান পদবি"),
        "switch_role" to Pair("Switch Role", "রোল পরিবর্তন করুন")
    )
}
