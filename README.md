# MAK Pharma BD — Full-Stack Retail Pharmacy Management System

A production-grade retail pharmacy management system designed specifically for pharmacies in Bangladesh, featuring fast POS billing, First-Expire-First-Out (FEFO) batch management, near-expiry and low-stock alerts, customer credit/due tracking, supplier purchase orders, cashier shifts, gross profit analytics, and a bilingual interface in English and বাংলা (Bangla).

---

## 🌟 Key Features

1. **Dashboard & Clinical KPIs**:
   - Real-time KPIs: Today's sales, Gross profit estimate, Invoices count, Purchases, Due collected.
   - Intelligent Expiry Badges: Batches expiring within 30 days (🚨), 60 days (⏳), 90 days, and expired batches.
   - Low-stock warnings when inventory drops below configured reorder levels.
   - 7-day sales activity bar chart.
   - Quick action shortcuts (New Sale, Add Medicine, Receive Purchase, Add Customer).

2. **POS / Counter Billing**:
   - Search by brand name, generic name, Bangla name, barcode, SKU, or manufacturer.
   - Instant barcode entry simulation and scanner support.
   - **FEFO (First Expire, First Out)**: Automatically selects the earliest non-expired batch for each medicine to minimize pharmacy wastage.
   - Enforces batch stock limits (prevents overselling beyond available batch quantity).
   - Customer selection (Walk-in or registered patient with phone lookup).
   - Payment methods: Cash, Mobile Banking (**bKash**, **Nagad**, **Rocket**, Upay), Debit/Credit Card, Bank Transfer, Customer Due / Credit.
   - Configurable VAT/tax rate and customizable discount amounts.
   - Sequential invoice generation: `PCB-YYYYMMDD-XXXX`.
   - Bill Hold / Retrieve system for pending customers.
   - Beautiful Thermal Receipt & A4 printable preview with one-tap Android Share sheet.

3. **Medicine Master & Batch Inventory**:
   - Comprehensive medicine catalog with dosage forms (Tablet, Capsule, Syrup, Suspension, Inhaler, Drops, Cream/Ointment, etc.).
   - Batch-level tracking: Batch No, Mfg date, Expiry date, Purchase cost, MRP, Sale price, Rack/Storage location (e.g. Rack A-1, Fridge 1).
   - Manual Stock Adjustment with audit trail (Discrepancy count, breakage, expired disposal, sample, return to vendor).
   - Real-time Stock Movement Ledger recording every addition, deduction, sale, and return.

4. **Purchases & Supplier Payables**:
   - Supplier directory (Square, Beximco, Incepta, Renata, ACME) with phone, address, and outstanding payable balances.
   - Receive Purchase Orders with multiple medicine batch lines, bonus quantities, purchase price, MRP, and partial/due payment tracking.
   - Atomically updates batch stock and supplier payable ledger upon receipt.

5. **Customer & Due Management**:
   - Customer directory with address, phone numbers (`017...`, `018...`), credit limits, and current due balance.
   - Fast due collection flow with payment method recording, digital receipt number generation, and ledger updates.

6. **Cashier Shifts & Finance**:
   - Daily Cash Drawer reconciliation: Opening float + Cash sales + Cash due collections - Cash expenses = Expected cash in drawer.
   - Expense tracker with categories (Utilities, DESCO meter, Rent, Staff tea & snacks, Maintenance).

7. **Reports & CSV Export**:
   - Filterable reports: Sales & Gross Profit, Stock Valuation (at Buy Cost vs Retail MRP), Expiry Audit (<30d, <60d, <90d), Customer Receivables, Supplier Payables.
   - One-tap "Export CSV" feature sharing formatted CSV data across apps.

8. **Role-Based Access Control (RBAC)**:
   - **Owner / Admin**: Full system access, staff user management, settings, reports, audit logs.
   - **Pharmacist / Manager**: POS, inventory, stock adjustments, purchase receiving, refunds, reports.
   - **Cashier**: POS counter, customer lookup, thermal invoice print, own sales.
   - **Storekeeper**: Batch receiving, physical stock count discrepancy adjustments, expiry audits.
   - **Viewer / Accountant**: Read-only financial & inventory reports.
   - Includes an instant **Role Switcher** in the top bar to test permissions across all roles immediately.

9. **Bilingual English & বাংলা UI**:
   - Seamless instant language switch toggling all labels, currencies, and invoice footers.
   - Proper Unicode support for Bengali digits and typography.

---

## 🏗️ Architecture & Technology Stack

- **Platform**: Android Native with Jetpack Compose & Material Design 3
- **Language**: Kotlin 2.0+ (Coroutines & StateFlow)
- **Database Engine**: Room SQLite with ACID `@Transaction` support, index optimizations, and reactive `Flow` streams.
- **Dependency Injection**: Clean Constructor Injection & ViewModel Factory.
- **Timezone**: `Asia/Dhaka` (GMT+6)
- **Currency**: Bangladeshi Taka (BDT / ৳)
- **Date Format**: `DD/MM/YYYY`

---

## 🧪 Verification & Test Checklist

| Feature | Test Action | Expected Result |
|---|---|---|
| **POS FEFO Batch Selection** | Add "Napa Extra" or "Seclo 20" to cart in POS | Earliest batch `NE-2401` (Exp: 18/11/2026) is automatically selected. |
| **Batch Stock Guard** | Increase item quantity beyond available batch units | System blocks action and warns with exact available batch balance. |
| **Sale Completion** | Choose Cash / bKash, enter paid amount, tap "Complete Sale" | Stock is deducted atomically, sequential invoice `PCB-20261026-XXXX` generated, thermal receipt preview shown. |
| **Print / Share Invoice** | Tap "Print / Share" on invoice preview dialog | Formatted receipt text opens in Android Share sheet. |
| **Sale Return / Refund** | Open Sales History, tap "Refund" on completed invoice | Stock is restored back to original batches, sale marked as REFUNDED, audit log created. |
| **Stock Adjustment** | Go to Inventory > Batches > Adjust Stock | Stock updated with reason (e.g. Physical count discrepancy) and recorded in Stock Movements ledger. |
| **Due Collection** | Go to Customers, tap "Collect Due" for Md. Karim Ullah | Balance reduced, receipt generated, drawer cash increased. |
| **Role Permissions** | Switch role to "Cashier" via top avatar | Administrative settings & inventory adjustment restricted. |
| **Language Switch** | Tap "বাং / EN" button in top bar | UI immediately switches between English and বাংলা. |
| **Demo Data Reset** | Tap "Reset Demo Data" in Settings or Drawer | Database cleanly re-seeds with 20+ realistic Bangladeshi medicines, suppliers, and batches. |

---

## 🚀 Realistic Demo Accounts

Use the **Role Switcher** avatar at the top right of the app to switch into any of the following pre-configured profiles:
1. **Khademul Islam** (`admin@makpharmabd.com`) — *Owner / Admin*
2. **Dr. Nahid Hasan (B.Pharm)** (`nahid@makpharmabd.com`) — *Pharmacist / Manager*
3. **Shuvo Rahman** (`cashier1@makpharmabd.com`) — *Cashier*
4. **Alamgir Hossain** (`store@makpharmabd.com`) — *Storekeeper*
5. **Taslima Akter** (`finance@makpharmabd.com`) — *Viewer / Accountant*
