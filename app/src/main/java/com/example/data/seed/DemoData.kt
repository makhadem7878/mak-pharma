package com.example.data.seed

import com.example.data.model.*
import java.util.concurrent.TimeUnit

object DemoData {
    // Current base time: Oct 26, 2026
    private val now = System.currentTimeMillis()
    private val dayMillis = TimeUnit.DAYS.toMillis(1)

    val defaultSettings = PharmacySettings(
        id = 1,
        pharmacyName = "MAK Pharma BD",
        pharmacyBanglaName = "ম্যাক ফার্মা বিডি",
        address = "House #14, Road #7, Dhanmondi, Dhaka-1205",
        phone = "01700-112233 / 02-9876543",
        email = "info@makpharmabd.com",
        invoicePrefix = "PCB",
        defaultVatPercent = 0.0,
        defaultDiscountLimitPercent = 15.0,
        expiryAlert30Days = 30,
        expiryAlert60Days = 60,
        expiryAlert90Days = 90,
        lowStockThresholdDefault = 20
    )

    val users = listOf(
        AppUser("usr_admin", "Khademul Islam", "admin@makpharmabd.com", UserRole.OWNER_ADMIN.name, "01700-112233"),
        AppUser("usr_manager", "Dr. Nahid Hasan (B.Pharm)", "nahid@makpharmabd.com", UserRole.PHARMACIST_MANAGER.name, "01711-223344"),
        AppUser("usr_cashier", "Shuvo Rahman", "cashier1@makpharmabd.com", UserRole.CASHIER.name, "01819-334455"),
        AppUser("usr_store", "Alamgir Hossain", "store@makpharmabd.com", UserRole.STOREKEEPER.name, "01912-445566"),
        AppUser("usr_acct", "Taslima Akter", "finance@makpharmabd.com", UserRole.VIEWER_ACCOUNTANT.name, "01620-556677")
    )

    val suppliers = listOf(
        Supplier("sup_sqr", "Square Pharmaceuticals Ltd", "Rafiqul Islam", "01712-100200", "sqr_dhaka@square.com", "Mohakhali C/A, Dhaka-1212", "TR-DHK-99412", 15000.0, 15000.0),
        Supplier("sup_bex", "Beximco Pharmaceuticals Ltd", "Moniruzzaman", "01819-200300", "bex_sales@beximco.net", "17 Dhanmondi R/A, Dhaka", "TR-DHK-88231", 8500.0, 8500.0),
        Supplier("sup_inc", "Incepta Pharmaceuticals Ltd", "Mizanur Rahman", "01912-300400", "incepta_depot@incepta.com", "40 Shahid Tajuddin Sarani, Tejgaon", "TR-DHK-77124", 12000.0, 12000.0),
        Supplier("sup_ren", "Renata Limited", "Kamrul Hasan", "01755-400500", "renata_dist@renata-ltd.com", "Plot 1, Milk Vita Road, Section 7, Mirpur", "TR-DHK-66512", 4200.0, 4200.0),
        Supplier("sup_acm", "The ACME Laboratories Ltd", "Shafiqul Alam", "01620-500600", "acme_sales@acmeglobal.com", "Court De la ACME, 1/4 Kallayanpur, Dhaka", "TR-DHK-55419", 9600.0, 9600.0)
    )

    val customers = listOf(
        Customer("cust_1", "Md. Karim Ullah", "01711-234567", "House 12, Road 4, Dhanmondi, Dhaka", 5000.0, 450.0, "Regular hypertensive patient"),
        Customer("cust_2", "Begum Rokeya Sultana", "01819-876543", "Flat 4B, Sector 7, Uttara, Dhaka", 4000.0, 0.0, "Monthly diabetic medicines"),
        Customer("cust_3", "Dr. Ahsan Habib", "01912-349812", "Green Road, Panthapath, Dhaka", 10000.0, 1200.0, "Physician clinic customer"),
        Customer("cust_4", "Farhana Yasmin", "01620-554433", "Tajmahal Road, Mohammadpur, Dhaka", 3000.0, 250.0, "Pediatric vitamins buyer"),
        Customer("cust_5", "Kamal Hossain", "01755-998877", "Block D, Lalmatia, Dhaka", 2500.0, 0.0, "")
    )

    val medicines = listOf(
        Medicine("med_1", "Napa Extra", "নাপা এক্সট্রা", "Paracetamol + Caffeine", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "500mg + 65mg", "Strip", 10, "Analgesic & Antipyretic", "894110010101", "MED-NAP-EXT", false, 30, 30.0),
        Medicine("med_2", "Seclo 20", "সেকলো ২০", "Omeprazole", "Square Pharmaceuticals PLC", DosageForm.CAPSULE.name, "20mg", "Strip", 10, "Anti-ulcerant", "894110010102", "MED-SEC-020", false, 25, 70.0),
        Medicine("med_3", "Ace Plus", "এইস প্লাস", "Paracetamol + Caffeine", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "500mg + 65mg", "Strip", 10, "Analgesic & Antipyretic", "894110010103", "MED-ACE-PLS", false, 20, 30.0),
        Medicine("med_4", "Monas 10", "মোনাস ১০", "Montelukast", "The ACME Laboratories Ltd", DosageForm.TABLET.name, "10mg", "Strip", 10, "Respiratory", "894110010104", "MED-MON-010", false, 15, 160.0),
        Medicine("med_5", "Ceevit", "সিভিট", "Vitamin C (Ascorbic Acid)", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "250mg", "Strip", 10, "Vitamins & Minerals", "894110010105", "MED-CEE-VIT", false, 40, 25.0),
        Medicine("med_6", "Azithrocin 500", "অ্যাজিথ্রোসিন ৫০০", "Azithromycin", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "500mg", "Box", 6, "Antibiotic", "894110010106", "MED-AZI-500", true, 10, 210.0),
        Medicine("med_7", "Alatrex 10", "অ্যালাট্রেক্স ১০", "Cetirizine Hydrochloride", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "10mg", "Strip", 10, "Antihistamine", "894110010107", "MED-ALA-010", false, 25, 35.0),
        Medicine("med_8", "Pantonix 20", "প্যান্টোনিক্স ২০", "Pantoprazole", "Incepta Pharmaceuticals Ltd", DosageForm.TABLET.name, "20mg", "Strip", 10, "Anti-ulcerant", "894110010108", "MED-PAN-020", false, 20, 75.0),
        Medicine("med_9", "Maxpro 20", "ম্যাক্সপ্রো ২০", "Esomeprazole", "Renata Limited", DosageForm.CAPSULE.name, "20mg", "Strip", 10, "Anti-ulcerant", "894110010109", "MED-MAX-020", false, 25, 80.0),
        Medicine("med_10", "Entacyd Plus", "এন্টাসিড প্লাস", "Magaldrate + Simethicone", "Square Pharmaceuticals PLC", DosageForm.SUSPENSION.name, "200ml", "Bottle", 1, "Antacid", "894110010110", "MED-ENT-PLS", false, 15, 110.0),
        Medicine("med_11", "Fexo 120", "ফেক্সো ১২০", "Fexofenadine Hydrochloride", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "120mg", "Strip", 10, "Antihistamine", "894110010111", "MED-FEX-120", false, 20, 90.0),
        Medicine("med_12", "Sergel 20", "সারজেল ২০", "Esomeprazole", "Healthcare Pharmaceuticals Ltd", DosageForm.CAPSULE.name, "20mg", "Strip", 10, "Anti-ulcerant", "894110010112", "MED-SER-020", false, 20, 80.0),
        Medicine("med_13", "Bexitrol-F", "বেক্সিট্রোল-এফ", "Salmeterol + Fluticasone", "Beximco Pharmaceuticals Ltd", DosageForm.INHALER.name, "120 Puffs", "Piece", 1, "Respiratory", "894110010113", "MED-BEX-INH", true, 8, 380.0),
        Medicine("med_14", "Betnovate-N", "বেটনােভেট-এন", "Betamethasone + Neomycin", "GlaxoSmithKline BD", DosageForm.CREAM_OINTMENT.name, "15g", "Tube", 1, "Dermatological", "894110010114", "MED-BET-CRM", false, 15, 65.0),
        Medicine("med_15", "Calbo-D", "ক্যালবো-ডি", "Calcium + Vitamin D3", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "500mg+200IU", "Bottle", 30, "Vitamins & Minerals", "894110010115", "MED-CAL-BOD", false, 15, 210.0),
        Medicine("med_16", "Neuro-B", "নিউরো-বি", "Vitamin B1 + B6 + B12", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "100+200+200", "Strip", 10, "Vitamins & Minerals", "894110010116", "MED-NEU-ROB", false, 20, 70.0),
        Medicine("med_17", "Amodis 400", "অ্যামোডিস ৪০০", "Metronidazole", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "400mg", "Strip", 10, "Antiprotozoal", "894110010117", "MED-AMO-400", false, 20, 25.0),
        Medicine("med_18", "D-Bal 20000 IU", "ডি-বল ২০০০০", "Cholecalciferol", "Square Pharmaceuticals PLC", DosageForm.CAPSULE.name, "20,000 IU", "Strip", 4, "Vitamins & Minerals", "894110010118", "MED-DBA-020", false, 12, 120.0),
        Medicine("med_19", "Tofen 1mg", "টোফেন ১ মিগ্রা", "Ketotifen", "Beximco Pharmaceuticals Ltd", DosageForm.TABLET.name, "1mg", "Strip", 10, "Antihistamine", "894110010119", "MED-TOF-001", false, 15, 40.0),
        Medicine("med_20", "Neotack 150", "নিওট্যাক ১৫০", "Ranitidine Hydrochloride", "Square Pharmaceuticals PLC", DosageForm.TABLET.name, "150mg", "Strip", 10, "Anti-ulcerant", "894110010120", "MED-NEO-150", false, 20, 30.0) // Low stock example
    )

    // Medicine batches with FEFO sorting and diverse expiries for alerts testing:
    // - <30 days expiry (e.g. 18 days from now)
    // - <60 days expiry (e.g. 45 days from now)
    // - <90 days expiry (e.g. 78 days from now)
    // - Normal 2027/2028 expiries
    // - Low stock count (e.g. 6 strips remaining)
    // - Out of stock (0 strips remaining)
    val batches = listOf(
        // Napa Extra: Batch 1 (FEFO, expires in 22 days -> <30d alert!), Batch 2 (expires in 18 months)
        MedicineBatch("bat_1", "med_1", "NE-2401", "10/01/2024", "18/11/2026", now + (22 * dayMillis), 23.50, 30.00, 30.00, 35, "sup_sqr", "Square Pharmaceuticals Ltd", "15/01/2024", "Rack A-1", "ACTIVE"),
        MedicineBatch("bat_2", "med_1", "NE-2409", "05/09/2024", "20/08/2027", now + (300 * dayMillis), 23.50, 30.00, 30.00, 120, "sup_sqr", "Square Pharmaceuticals Ltd", "10/09/2024", "Rack A-1", "ACTIVE"),

        // Seclo 20: Batch 1 (expires in 45 days -> <60d alert!), Batch 2 (healthy)
        MedicineBatch("bat_3", "med_2", "SEC-2311", "01/11/2023", "10/12/2026", now + (45 * dayMillis), 56.00, 70.00, 70.00, 28, "sup_sqr", "Square Pharmaceuticals Ltd", "10/11/2023", "Rack A-2", "ACTIVE"),
        MedicineBatch("bat_4", "med_2", "SEC-2405", "15/05/2024", "15/05/2027", now + (560 * dayMillis), 56.00, 70.00, 70.00, 95, "sup_sqr", "Square Pharmaceuticals Ltd", "20/05/2024", "Rack A-2", "ACTIVE"),

        // Ace Plus: healthy stock
        MedicineBatch("bat_5", "med_3", "ACE-2403", "12/03/2024", "15/03/2027", now + (500 * dayMillis), 23.00, 30.00, 30.00, 80, "sup_sqr", "Square Pharmaceuticals Ltd", "18/03/2024", "Rack A-3", "ACTIVE"),

        // Monas 10: Batch 1 (expires in 75 days -> <90d alert!)
        MedicineBatch("bat_6", "med_4", "MON-2401", "10/01/2024", "10/01/2027", now + (75 * dayMillis), 128.00, 160.00, 160.00, 42, "sup_acm", "The ACME Laboratories Ltd", "15/01/2024", "Rack B-1", "ACTIVE"),

        // Ceevit: high volume stock
        MedicineBatch("bat_7", "med_5", "CEE-2404", "01/04/2024", "01/04/2027", now + (520 * dayMillis), 18.50, 25.00, 25.00, 210, "sup_sqr", "Square Pharmaceuticals Ltd", "05/04/2024", "Counter Box 1", "ACTIVE"),

        // Azithrocin 500: Batch 1 (<30 days expiry alert!)
        MedicineBatch("bat_8", "med_6", "AZI-2310", "15/10/2023", "15/11/2026", now + (20 * dayMillis), 168.00, 210.00, 210.00, 14, "sup_sqr", "Square Pharmaceuticals Ltd", "20/10/2023", "Fridge 1", "ACTIVE"),
        MedicineBatch("bat_9", "med_6", "AZI-2408", "01/08/2024", "01/08/2027", now + (640 * dayMillis), 168.00, 210.00, 210.00, 36, "sup_sqr", "Square Pharmaceuticals Ltd", "05/08/2024", "Fridge 1", "ACTIVE"),

        // Alatrex 10:
        MedicineBatch("bat_10", "med_7", "ALA-2402", "14/02/2024", "14/02/2027", now + (470 * dayMillis), 27.00, 35.00, 35.00, 65, "sup_sqr", "Square Pharmaceuticals Ltd", "20/02/2024", "Rack A-4", "ACTIVE"),

        // Pantonix 20:
        MedicineBatch("bat_11", "med_8", "PAN-2406", "10/06/2024", "10/06/2027", now + (590 * dayMillis), 59.00, 75.00, 75.00, 70, "sup_inc", "Incepta Pharmaceuticals Ltd", "15/06/2024", "Rack B-2", "ACTIVE"),

        // Maxpro 20:
        MedicineBatch("bat_12", "med_9", "MAX-2407", "05/07/2024", "05/07/2027", now + (615 * dayMillis), 63.00, 80.00, 80.00, 85, "sup_ren", "Renata Limited", "10/07/2024", "Rack B-3", "ACTIVE"),

        // Entacyd Plus:
        MedicineBatch("bat_13", "med_10", "ENT-2403", "15/03/2024", "15/09/2027", now + (680 * dayMillis), 85.00, 110.00, 110.00, 34, "sup_sqr", "Square Pharmaceuticals Ltd", "20/03/2024", "Rack C-1", "ACTIVE"),

        // Fexo 120: expires in 80 days -> <90d alert
        MedicineBatch("bat_14", "med_11", "FEX-2401", "20/01/2024", "15/01/2027", now + (80 * dayMillis), 71.00, 90.00, 90.00, 22, "sup_sqr", "Square Pharmaceuticals Ltd", "25/01/2024", "Rack A-5", "ACTIVE"),

        // Sergel 20:
        MedicineBatch("bat_15", "med_12", "SER-2405", "10/05/2024", "10/05/2027", now + (560 * dayMillis), 63.00, 80.00, 80.00, 48, "sup_inc", "Incepta Pharmaceuticals Ltd", "15/05/2024", "Rack B-4", "ACTIVE"),

        // Bexitrol-F Inhaler: Low stock (6 units, reorder is 8)
        MedicineBatch("bat_16", "med_13", "BEX-2402", "01/02/2024", "01/02/2027", now + (460 * dayMillis), 305.00, 380.00, 380.00, 6, "sup_bex", "Beximco Pharmaceuticals Ltd", "05/02/2024", "Shelf Spec-1", "ACTIVE"),

        // Betnovate-N Cream:
        MedicineBatch("bat_17", "med_14", "BET-2404", "10/04/2024", "10/10/2027", now + (710 * dayMillis), 51.00, 65.00, 65.00, 38, "sup_sqr", "Square Pharmaceuticals Ltd", "15/04/2024", "Ointment Box", "ACTIVE"),

        // Calbo-D:
        MedicineBatch("bat_18", "med_15", "CAL-2403", "15/03/2024", "15/03/2027", now + (500 * dayMillis), 165.00, 210.00, 210.00, 25, "sup_sqr", "Square Pharmaceuticals Ltd", "20/03/2024", "Rack C-2", "ACTIVE"),

        // Neuro-B:
        MedicineBatch("bat_19", "med_16", "NEU-2406", "10/06/2024", "10/06/2027", now + (590 * dayMillis), 55.00, 70.00, 70.00, 52, "sup_sqr", "Square Pharmaceuticals Ltd", "15/06/2024", "Rack A-6", "ACTIVE"),

        // Amodis 400:
        MedicineBatch("bat_20", "med_17", "AMO-2401", "12/01/2024", "12/01/2027", now + (440 * dayMillis), 19.50, 25.00, 25.00, 75, "sup_sqr", "Square Pharmaceuticals Ltd", "18/01/2024", "Rack A-7", "ACTIVE"),

        // D-Bal 20000 IU:
        MedicineBatch("bat_21", "med_18", "DBA-2405", "20/05/2024", "20/05/2027", now + (570 * dayMillis), 94.00, 120.00, 120.00, 30, "sup_sqr", "Square Pharmaceuticals Ltd", "25/05/2024", "Rack B-5", "ACTIVE"),

        // Tofen 1mg: Low stock alert (5 units, reorder is 15)
        MedicineBatch("bat_22", "med_19", "TOF-2402", "15/02/2024", "15/02/2027", now + (475 * dayMillis), 31.00, 40.00, 40.00, 5, "sup_bex", "Beximco Pharmaceuticals Ltd", "20/02/2024", "Rack A-8", "ACTIVE"),

        // Neotack 150: OUT OF STOCK example (0 units)
        MedicineBatch("bat_23", "med_20", "NEO-2309", "01/09/2023", "01/09/2026", now - (55 * dayMillis), 23.50, 30.00, 30.00, 0, "sup_sqr", "Square Pharmaceuticals Ltd", "05/09/2023", "Rack A-9", "EXPIRED")
    )

    val sampleSales = listOf(
        Sale(
            id = "sale_1",
            invoiceNumber = "PCB-20261026-0001",
            timestamp = now - (3 * 3600 * 1000),
            dateTimeStr = "26/10/2026 09:15 AM",
            customerId = "cust_1",
            customerName = "Md. Karim Ullah",
            customerPhone = "01711-234567",
            subtotal = 340.0,
            discountAmount = 10.0,
            taxAmount = 0.0,
            taxRatePercent = 0.0,
            rounding = 0.0,
            grandTotal = 330.0,
            paidAmount = 330.0,
            dueAmount = 0.0,
            changeReturn = 0.0,
            paymentMethod = PaymentMethod.CASH.name,
            cashierName = "Shuvo Rahman",
            cashierRole = UserRole.CASHIER.name,
            status = "COMPLETED",
            itemsCount = 3,
            notes = "Counter Sale"
        ),
        Sale(
            id = "sale_2",
            invoiceNumber = "PCB-20261026-0002",
            timestamp = now - (2 * 3600 * 1000),
            dateTimeStr = "26/10/2026 10:30 AM",
            customerId = "WALK_IN",
            customerName = "Walk-in Customer",
            customerPhone = "",
            subtotal = 590.0,
            discountAmount = 20.0,
            taxAmount = 0.0,
            taxRatePercent = 0.0,
            rounding = 0.0,
            grandTotal = 570.0,
            paidAmount = 570.0,
            dueAmount = 0.0,
            changeReturn = 0.0,
            paymentMethod = PaymentMethod.MOBILE_BANKING.name,
            mobileBankingProvider = "bKash",
            cashierName = "Shuvo Rahman",
            cashierRole = UserRole.CASHIER.name,
            status = "COMPLETED",
            itemsCount = 2,
            notes = "bKash TrxID: BK9283741"
        ),
        Sale(
            id = "sale_3",
            invoiceNumber = "PCB-20261026-0003",
            timestamp = now - (1 * 3600 * 1000),
            dateTimeStr = "26/10/2026 11:45 AM",
            customerId = "cust_3",
            customerName = "Dr. Ahsan Habib",
            customerPhone = "01912-349812",
            subtotal = 1450.0,
            discountAmount = 50.0,
            taxAmount = 0.0,
            taxRatePercent = 0.0,
            rounding = 0.0,
            grandTotal = 1400.0,
            paidAmount = 1000.0,
            dueAmount = 400.0,
            changeReturn = 0.0,
            paymentMethod = PaymentMethod.DUE_CREDIT.name,
            cashierName = "Dr. Nahid Hasan",
            cashierRole = UserRole.PHARMACIST_MANAGER.name,
            status = "COMPLETED",
            itemsCount = 4,
            notes = "Partial payment, remaining ৳ 400 added to ledger"
        )
    )

    val sampleSaleItems = listOf(
        SaleItem("si_1", "sale_1", "med_1", "Napa Extra", DosageForm.TABLET.name, "500mg+65mg", "bat_1", "NE-2401", "18/11/2026", 2, 30.0, 23.50, 0.0, 60.0),
        SaleItem("si_2", "sale_1", "med_2", "Seclo 20", DosageForm.CAPSULE.name, "20mg", "bat_3", "SEC-2311", "10/12/2026", 2, 70.0, 56.00, 0.0, 140.0),
        SaleItem("si_3", "sale_1", "med_16", "Neuro-B", DosageForm.TABLET.name, "100+200+200", "bat_19", "NEU-2406", "10/06/2027", 2, 70.0, 55.00, 0.0, 140.0),

        SaleItem("si_4", "sale_2", "med_6", "Azithrocin 500", DosageForm.TABLET.name, "500mg", "bat_8", "AZI-2310", "15/11/2026", 1, 210.0, 168.00, 0.0, 210.0),
        SaleItem("si_5", "sale_2", "med_13", "Bexitrol-F", DosageForm.INHALER.name, "120 Puffs", "bat_16", "BEX-2402", "01/02/2027", 1, 380.0, 305.00, 0.0, 380.0),

        SaleItem("si_6", "sale_3", "med_4", "Monas 10", DosageForm.TABLET.name, "10mg", "bat_6", "MON-2401", "10/01/2027", 5, 160.0, 128.00, 0.0, 800.0),
        SaleItem("si_7", "sale_3", "med_15", "Calbo-D", DosageForm.TABLET.name, "500mg+200IU", "bat_18", "CAL-2403", "15/03/2027", 2, 210.0, 165.00, 0.0, 420.0),
        SaleItem("si_8", "sale_3", "med_11", "Fexo 120", DosageForm.TABLET.name, "120mg", "bat_14", "FEX-2401", "15/01/2027", 1, 90.0, 71.00, 0.0, 90.0),
        SaleItem("si_9", "sale_3", "med_7", "Alatrex 10", DosageForm.TABLET.name, "10mg", "bat_10", "ALA-2402", "14/02/2027", 4, 35.0, 27.00, 0.0, 140.0)
    )

    val samplePurchases = listOf(
        Purchase(
            id = "pur_1",
            invoiceNumber = "PUR-20261025-01",
            supplierId = "sup_sqr",
            supplierName = "Square Pharmaceuticals Ltd",
            purchaseDate = "25/10/2026",
            timestamp = now - (24 * 3600 * 1000),
            totalAmount = 14500.0,
            paidAmount = 10000.0,
            dueAmount = 4500.0,
            status = "RECEIVED",
            notes = "Weekly replenishment of Napa & Seclo"
        )
    )

    val samplePurchaseItems = listOf(
        PurchaseItem("pi_1", "pur_1", "med_1", "Napa Extra", "NE-2409", "20/08/2027", 200, 10, 23.50, 30.00, 30.00, 4700.0),
        PurchaseItem("pi_2", "pur_1", "med_2", "Seclo 20", "SEC-2405", "15/05/2027", 150, 5, 56.00, 70.00, 70.00, 8400.0),
        PurchaseItem("pi_3", "pur_1", "med_5", "Ceevit", "CEE-2404", "01/04/2027", 75, 5, 18.50, 25.00, 25.00, 1400.0)
    )

    val sampleStockMovements = listOf(
        StockMovement("sm_1", now - (24 * 3600 * 1000), "25/10/2026 04:30 PM", "med_1", "Napa Extra", "bat_2", "NE-2409", StockMovementType.PURCHASE_RECEIVE.name, 210, 210, "pur_1", "Purchase Order Received", "Alamgir Hossain"),
        StockMovement("sm_2", now - (3 * 3600 * 1000), "26/10/2026 09:15 AM", "med_1", "Napa Extra", "bat_1", "NE-2401", StockMovementType.SALE.name, -2, 35, "sale_1", "POS Sale (FEFO)", "Shuvo Rahman"),
        StockMovement("sm_3", now - (3 * 3600 * 1000), "26/10/2026 09:15 AM", "med_2", "Seclo 20", "bat_3", "SEC-2311", StockMovementType.SALE.name, -2, 28, "sale_1", "POS Sale (FEFO)", "Shuvo Rahman"),
        StockMovement("sm_4", now - (2 * 3600 * 1000), "26/10/2026 10:30 AM", "med_6", "Azithrocin 500", "bat_8", "AZI-2310", StockMovementType.SALE.name, -1, 14, "sale_2", "POS Sale", "Shuvo Rahman")
    )

    val sampleExpenses = listOf(
        Expense("exp_1", "Tea & Snacks", "Afternoon pharmacy staff tea & biscuits", 180.0, now - (4 * 3600 * 1000), "26/10/2026 08:30 AM", PaymentMethod.CASH.name, "Dr. Nahid Hasan"),
        Expense("exp_2", "Utilities", "Electricity bill token for DESCO meter", 2500.0, now - (48 * 3600 * 1000), "24/10/2026 11:00 AM", PaymentMethod.MOBILE_BANKING.name, "Khademul Islam", "bKash bill pay"),
        Expense("exp_3", "Maintenance", "Air conditioner filter cleaning & gas check", 1200.0, now - (72 * 3600 * 1000), "23/10/2026 03:00 PM", PaymentMethod.CASH.name, "Alamgir Hossain")
    )

    val sampleDuePayments = listOf(
        CustomerDuePayment("cdp_1", "cust_1", "Md. Karim Ullah", now - (5 * 3600 * 1000), "26/10/2026 07:45 AM", 500.0, PaymentMethod.CASH.name, "DUE-20261026-01", "Previous month due clearance", "Shuvo Rahman")
    )

    val initialShift = CashierShift(
        id = "shift_today",
        cashierName = "Shuvo Rahman",
        role = UserRole.CASHIER.name,
        openedAt = now - (6 * 3600 * 1000),
        openingCash = 5000.0,
        cashSales = 330.0,
        cashDueCollected = 500.0,
        cashExpenses = 180.0,
        expectedCash = 5650.0,
        status = "OPEN"
    )

    val sampleAuditLogs = listOf(
        AuditLog("log_1", now - (6 * 3600 * 1000), "26/10/2026 06:00 AM", UserRole.CASHIER.name, "Shuvo Rahman", "SHIFT_OPENED", "CashierShift", "shift_today", "Shift opened with ৳ 5,000.00 cash float"),
        AuditLog("log_2", now - (3 * 3600 * 1000), "26/10/2026 09:15 AM", UserRole.CASHIER.name, "Shuvo Rahman", "SALE_CREATED", "Sale", "PCB-20261026-0001", "Sale completed for ৳ 330.00 (Cash) - 3 items"),
        AuditLog("log_3", now - (2 * 3600 * 1000), "26/10/2026 10:30 AM", UserRole.CASHIER.name, "Shuvo Rahman", "SALE_CREATED", "Sale", "PCB-20261026-0002", "Sale completed for ৳ 570.00 (bKash) - 2 items"),
        AuditLog("log_4", now - (1 * 3600 * 1000), "26/10/2026 11:45 AM", UserRole.PHARMACIST_MANAGER.name, "Dr. Nahid Hasan", "SALE_CREATED", "Sale", "PCB-20261026-0003", "Sale completed for ৳ 1,400.00 (৳ 400 due) - 4 items")
    )
}
