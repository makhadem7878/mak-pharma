package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppScreen(
    val route: String,
    val titleKey: String,
    val iconFilled: ImageVector,
    val iconOutlined: ImageVector,
    val showInBottomNav: Boolean = false,
    val showInDrawer: Boolean = true
) {
    DASHBOARD("dashboard", "dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, true, true),
    POS("pos", "pos", Icons.Filled.PointOfSale, Icons.Outlined.PointOfSale, true, true),
    INVENTORY("inventory", "inventory", Icons.Filled.Medication, Icons.Outlined.Medication, true, true),
    SALES("sales", "sales", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, true, true),
    CUSTOMERS("customers", "customers", Icons.Filled.People, Icons.Outlined.People, true, true),
    PURCHASES("purchases", "purchases", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, false, true),
    SUPPLIERS("suppliers", "suppliers", Icons.Filled.LocalShipping, Icons.Outlined.LocalShipping, false, true),
    EXPENSES("expenses", "expenses", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, false, true),
    REPORTS("reports", "reports", Icons.Filled.BarChart, Icons.Outlined.BarChart, false, true),
    USERS("users", "users", Icons.Filled.Badge, Icons.Outlined.Badge, false, true),
    AUDIT_LOGS("audit_logs", "audit_logs", Icons.Filled.Security, Icons.Outlined.Security, false, true),
    SETTINGS("settings", "settings", Icons.Filled.Settings, Icons.Outlined.Settings, false, true)
}
