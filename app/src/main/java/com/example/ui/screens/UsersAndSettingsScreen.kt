package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.model.PharmacySettings
import com.example.data.model.UserRole
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersAndSettingsScreen(
    viewModel: PharmacyViewModel,
    initialTab: Int = 0 // 0: Users & Roles, 1: Settings
) {
    val language by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val users by viewModel.users.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var selectedTab by remember { mutableStateOf(initialTab) }
    var showResetConfirmation by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = PharmacyGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(AppLocale.get("users", language), fontWeight = FontWeight.Bold) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(AppLocale.get("settings", language), fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (selectedTab == 0) {
            // User Management & Role Testing
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PharmacyGreenLight),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = PharmacyGreenDark)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "Active Session: ${currentUser.name}", fontWeight = FontWeight.Bold, color = PharmacyGreenDark)
                                Text(
                                    text = "Role: ${if (language == Language.BANGLA) UserRole.valueOf(currentUser.role).displayNameBn else UserRole.valueOf(currentUser.role).displayNameEn}",
                                    fontSize = 12.sp,
                                    color = PharmacyGreenDark
                                )
                            }
                        }
                    }
                }

                item {
                    Text(text = "Available Staff Accounts & Role Switcher", fontWeight = FontWeight.Bold)
                }

                items(users) { user ->
                    val isCurrent = user.id == currentUser.id
                    val roleEnum = UserRole.valueOf(user.role)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = user.name, fontWeight = FontWeight.Bold)
                                Text(text = user.email + " • " + user.phone, fontSize = 11.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                StatusChip(
                                    text = if (language == Language.BANGLA) roleEnum.displayNameBn else roleEnum.displayNameEn,
                                    backgroundColor = if (isCurrent) PharmacyGreenLight else Color(0xFFF1F5F9),
                                    textColor = if (isCurrent) PharmacyGreenDark else Color.DarkGray
                                )
                            }

                            if (!isCurrent) {
                                Button(
                                    onClick = { viewModel.switchUserRole(roleEnum) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text("Switch Role", fontSize = 11.sp)
                                }
                            } else {
                                Text(text = "Active", fontWeight = FontWeight.Bold, color = PharmacyGreenPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(text = "Role-Based Access Control (RBAC) Matrix", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            RoleMatrixRow("Owner / Admin", "Full System Access, Users, Settings, Reports, Audit Logs")
                            RoleMatrixRow("Pharmacist / Manager", "POS, Inventory, Stock Adjust, Purchases, Refunds, Reports")
                            RoleMatrixRow("Cashier", "POS Billing, Customer Lookup, Own Sales, Thermal Printing")
                            RoleMatrixRow("Storekeeper", "Batch Receiving, Expiry Audit, Stock Adjustments")
                            RoleMatrixRow("Viewer / Accountant", "Financial & Inventory Reports (Read-Only)")
                        }
                    }
                }
            }
        } else {
            // Settings Form
            var name by remember(settings) { mutableStateOf(settings.pharmacyName) }
            var banglaName by remember(settings) { mutableStateOf(settings.pharmacyBanglaName) }
            var address by remember(settings) { mutableStateOf(settings.address) }
            var phone by remember(settings) { mutableStateOf(settings.phone) }
            var email by remember(settings) { mutableStateOf(settings.email) }
            var prefix by remember(settings) { mutableStateOf(settings.invoicePrefix) }
            var vatRate by remember(settings) { mutableStateOf(settings.defaultVatPercent.toString()) }
            var discountLimit by remember(settings) { mutableStateOf(settings.defaultDiscountLimitPercent.toString()) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Pharmacy Store Information", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Pharmacy Name (English)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = banglaName, onValueChange = { banglaName = it }, label = { Text("Pharmacy Name (বাংলা)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Store Address") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Helpline Phone") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Contact Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Billing & Financial Configuration", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            OutlinedTextField(value = prefix, onValueChange = { prefix = it }, label = { Text("Invoice Number Prefix (e.g. PCB)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = vatRate, onValueChange = { vatRate = it }, label = { Text("Default VAT / Tax Rate (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = discountLimit, onValueChange = { discountLimit = it }, label = { Text("Max Discount Limit (%)") }, singleLine = true, modifier = Modifier.fillMaxWidth())

                            Button(
                                onClick = {
                                    viewModel.saveSettings(
                                        settings.copy(
                                            pharmacyName = name.trim(),
                                            pharmacyBanglaName = banglaName.trim(),
                                            address = address.trim(),
                                            phone = phone.trim(),
                                            email = email.trim(),
                                            invoicePrefix = prefix.trim(),
                                            defaultVatPercent = vatRate.toDoubleOrNull() ?: 0.0,
                                            defaultDiscountLimitPercent = discountLimit.toDoubleOrNull() ?: 15.0
                                        )
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
                            ) {
                                Text("Save Settings")
                            }
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = AlertRedLight)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Reset Realistic Demo Data", fontWeight = FontWeight.Bold, color = AlertRed)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Replaces existing records with the complete Bangladeshi pharmacy dataset (Square, Beximco, Incepta medicines, FEFO batches, customer dues, suppliers, and sales).",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { showResetConfirmation = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AlertRed)
                            ) {
                                Icon(Icons.Default.RestartAlt, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppLocale.get("reset_demo", language))
                            }
                        }
                    }
                }
            }
        }
    }

    ConfirmationDialog(
        show = showResetConfirmation,
        title = "Reset Demo Data?",
        message = "This will restore 20+ realistic medicines, FEFO batch allocations, suppliers, customers, and transactions to their fresh demo state.",
        confirmButtonText = "Yes, Reset Data",
        isDestructive = true,
        onConfirm = {
            viewModel.resetDemoData()
            showResetConfirmation = false
        },
        onDismiss = { showResetConfirmation = false }
    )
}

@Composable
private fun RoleMatrixRow(role: String, permissions: String) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = role, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PharmacyGreenPrimary)
        Text(text = permissions, fontSize = 11.sp, color = Color.DarkGray)
        Divider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(top = 4.dp))
    }
}
