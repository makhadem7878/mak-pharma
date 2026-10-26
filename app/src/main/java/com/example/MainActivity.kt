package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.data.model.UserRole
import com.example.ui.components.AppDrawerContent
import com.example.ui.components.AppTopBar
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.navigation.AppScreen
import com.example.ui.screens.*
import com.example.ui.theme.MAKPharmaTheme
import com.example.ui.theme.PharmacyGreenDark
import com.example.ui.theme.PharmacyGreenLight
import com.example.ui.theme.PharmacyGreenPrimary
import com.example.ui.viewmodel.PharmacyViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MAKPharmaTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp(viewModel: PharmacyViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()

    val showInvoiceDialog by viewModel.showInvoiceDialog.collectAsState()
    val lastSale by viewModel.lastCompletedSale.collectAsState()
    val lastSaleItems by viewModel.lastCompletedSaleItems.collectAsState()

    var activeViewingSale by remember { mutableStateOf<Sale?>(null) }
    var activeViewingSaleItems by remember { mutableStateOf<List<SaleItem>>(emptyList()) }
    var showRoleSwitchDialog by remember { mutableStateOf(false) }

    // Snackbar notification
    LaunchedEffect(uiMessage) {
        uiMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    // Predictive Back Handling
    BackHandler(enabled = drawerState.isOpen || currentScreen != AppScreen.DASHBOARD || activeViewingSale != null) {
        if (drawerState.isOpen) {
            scope.launch { drawerState.close() }
        } else if (activeViewingSale != null) {
            activeViewingSale = null
        } else if (currentScreen != AppScreen.DASHBOARD) {
            viewModel.navigateTo(AppScreen.DASHBOARD)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentScreen = currentScreen,
                currentLanguage = currentLanguage,
                currentUser = currentUser,
                pharmacyName = if (currentLanguage == Language.BANGLA) settings.pharmacyBanglaName else settings.pharmacyName,
                onSelectScreen = { screen ->
                    viewModel.navigateTo(screen)
                    scope.launch { drawerState.close() }
                },
                onRoleClick = {
                    showRoleSwitchDialog = true
                    scope.launch { drawerState.close() }
                },
                onResetDemoClick = {
                    viewModel.resetDemoData()
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    currentScreen = currentScreen,
                    currentLanguage = currentLanguage,
                    currentUser = currentUser,
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    onRoleClick = { showRoleSwitchDialog = true }
                )
            },
            bottomBar = {
                // Bottom Navigation Bar for primary screens
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    val bottomNavScreens = listOf(
                        AppScreen.DASHBOARD,
                        AppScreen.POS,
                        AppScreen.INVENTORY,
                        AppScreen.SALES,
                        AppScreen.CUSTOMERS
                    )

                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) screen.iconFilled else screen.iconOutlined,
                                    contentDescription = screen.name
                                )
                            },
                            label = {
                                Text(
                                    text = AppLocale.get(screen.titleKey, currentLanguage),
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = PharmacyGreenDark,
                                selectedTextColor = PharmacyGreenPrimary,
                                indicatorColor = PharmacyGreenLight
                            )
                        )
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = viewModel,
                        onNavigate = { viewModel.navigateTo(it) },
                        onViewSaleInvoice = { sale ->
                            activeViewingSale = sale
                            // Load items for this sale
                            scope.launch {
                                val saleItems = viewModel.saleItems.value.filter { it.saleId == sale.id }
                                activeViewingSaleItems = saleItems
                            }
                        }
                    )
                    AppScreen.POS -> PosScreen(viewModel = viewModel)
                    AppScreen.INVENTORY -> InventoryScreen(viewModel = viewModel)
                    AppScreen.SALES -> SalesHistoryScreen(
                        viewModel = viewModel,
                        onViewInvoice = { sale ->
                            activeViewingSale = sale
                            scope.launch {
                                val saleItems = viewModel.saleItems.value.filter { it.saleId == sale.id }
                                activeViewingSaleItems = saleItems
                            }
                        }
                    )
                    AppScreen.CUSTOMERS -> CustomersScreen(viewModel = viewModel)
                    AppScreen.PURCHASES -> PurchasesAndSuppliersScreen(viewModel = viewModel, initialTab = 0)
                    AppScreen.SUPPLIERS -> PurchasesAndSuppliersScreen(viewModel = viewModel, initialTab = 1)
                    AppScreen.EXPENSES -> ExpensesAndShiftScreen(viewModel = viewModel)
                    AppScreen.REPORTS -> ReportsScreen(viewModel = viewModel)
                    AppScreen.USERS -> UsersAndSettingsScreen(viewModel = viewModel, initialTab = 0)
                    AppScreen.SETTINGS -> UsersAndSettingsScreen(viewModel = viewModel, initialTab = 1)
                    AppScreen.AUDIT_LOGS -> AuditLogsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Invoice Dialog after completing a sale
    if (showInvoiceDialog && lastSale != null) {
        InvoiceDialog(
            sale = lastSale!!,
            items = lastSaleItems,
            settings = settings,
            language = currentLanguage,
            onDismiss = { viewModel.dismissInvoiceDialog() }
        )
    }

    // Modal Invoice Dialog when viewing past sale
    if (activeViewingSale != null) {
        InvoiceDialog(
            sale = activeViewingSale!!,
            items = activeViewingSaleItems,
            settings = settings,
            language = currentLanguage,
            onDismiss = { activeViewingSale = null }
        )
    }

    // Role Switch Dialog
    if (showRoleSwitchDialog) {
        RoleSwitchDialog(
            currentRole = UserRole.valueOf(currentUser.role),
            language = currentLanguage,
            onSelectRole = { role ->
                viewModel.switchUserRole(role)
                showRoleSwitchDialog = false
            },
            onDismiss = { showRoleSwitchDialog = false }
        )
    }
}

@Composable
private fun RoleSwitchDialog(
    currentRole: UserRole,
    language: Language,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = AppLocale.get("switch_role", language),
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Select any pharmacy role to test permissions and workflows immediately:",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                UserRole.values().forEach { role ->
                    val isSel = currentRole == role
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRole(role) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSel) PharmacyGreenLight else Color(0xFFF8FAFC)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == Language.BANGLA) role.displayNameBn else role.displayNameEn,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) PharmacyGreenDark else Color.Black
                                )
                                Text(
                                    text = when (role) {
                                        UserRole.OWNER_ADMIN -> "Full privileges, users, settings"
                                        UserRole.PHARMACIST_MANAGER -> "POS, Stock, Purchases, Refunds"
                                        UserRole.CASHIER -> "POS counter, invoice print, sales"
                                        UserRole.STOREKEEPER -> "Purchases, batches, stock adjust"
                                        UserRole.VIEWER_ACCOUNTANT -> "Read-only financial reports"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                            if (isSel) {
                                StatusChip(
                                    text = "ACTIVE",
                                    backgroundColor = PharmacyGreenPrimary,
                                    textColor = Color.White
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(AppLocale.get("close", language)) }
        }
    )
}
