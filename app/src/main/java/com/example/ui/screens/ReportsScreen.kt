package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicineBatch
import com.example.data.model.Sale
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusChip
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(viewModel: PharmacyViewModel) {
    val context = LocalContext.current
    val language by viewModel.currentLanguage.collectAsState()
    val sales by viewModel.sales.collectAsState()
    val saleItems by viewModel.saleItems.collectAsState()
    val batches by viewModel.batches.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val customers by viewModel.customers.collectAsState()
    val expenses by viewModel.expenses.collectAsState()

    var selectedPeriod by remember { mutableStateOf("TODAY") } // TODAY, WEEK, MONTH, ALL
    var selectedReportTab by remember { mutableStateOf(0) } // 0: Sales & Profit, 1: Inventory Valuation, 2: Expiry Analysis, 3: Due & Payables

    val now = System.currentTimeMillis()
    val dayMillis = TimeUnit.DAYS.toMillis(1)

    // Filter sales by period
    val periodSales = remember(sales, selectedPeriod) {
        when (selectedPeriod) {
            "TODAY" -> sales.filter { now - it.timestamp <= dayMillis }
            "WEEK" -> sales.filter { now - it.timestamp <= (7 * dayMillis) }
            "MONTH" -> sales.filter { now - it.timestamp <= (30 * dayMillis) }
            else -> sales
        }
    }

    val completedPeriodSales = periodSales.filter { it.status == "COMPLETED" }
    val totalRevenue = completedPeriodSales.sumOf { it.grandTotal }
    val totalDiscount = completedPeriodSales.sumOf { it.discountAmount }
    val totalTax = completedPeriodSales.sumOf { it.taxAmount }

    // Sold items profit calculation
    val completedSaleIds = completedPeriodSales.map { it.id }.toSet()
    val relevantSaleItems = saleItems.filter { it.saleId in completedSaleIds }
    val totalCogs = relevantSaleItems.sumOf { it.purchasePrice * it.quantity }
    val grossProfit = totalRevenue - totalCogs

    // Inventory Valuation
    val activeBatches = batches.filter { it.status == "ACTIVE" }
    val totalCostValue = activeBatches.sumOf { it.availableQty * it.purchasePrice }
    val totalMrpValue = activeBatches.sumOf { it.availableQty * it.salePrice }
    val totalInventoryProfit = totalMrpValue - totalCostValue

    // Expiry counts
    val expiring30 = activeBatches.filter { it.expiryTimestampMillis - now in 0..(30 * dayMillis) }
    val expiring60 = activeBatches.filter { it.expiryTimestampMillis - now in (30 * dayMillis)..(60 * dayMillis) }
    val expiring90 = activeBatches.filter { it.expiryTimestampMillis - now in (60 * dayMillis)..(90 * dayMillis) }
    val expiredBatches = batches.filter { it.expiryTimestampMillis - now < 0 || it.status == "EXPIRED" }

    // Dues
    val totalCustomerDue = customers.sumOf { it.currentDue }
    val totalSupplierPayable = suppliers.sumOf { it.payableBalance }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        // Top Period Selector & CSV Export Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("TODAY" to "Today", "WEEK" to "7 Days", "MONTH" to "30 Days", "ALL" to "All").forEach { (code, label) ->
                    val isSel = selectedPeriod == code
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedPeriod = code },
                        label = { Text(label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Button(
                onClick = {
                    exportReportCsv(
                        context = context,
                        period = selectedPeriod,
                        revenue = totalRevenue,
                        profit = grossProfit,
                        costValue = totalCostValue,
                        mrpValue = totalMrpValue,
                        custDue = totalCustomerDue,
                        supPayable = totalSupplierPayable,
                        batches = activeBatches
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Report Type Tabs
        TabRow(
            selectedTabIndex = selectedReportTab,
            containerColor = Color.White,
            contentColor = PharmacyGreenPrimary,
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(selected = selectedReportTab == 0, onClick = { selectedReportTab = 0 }, text = { Text("Sales & Profit", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedReportTab == 1, onClick = { selectedReportTab = 1 }, text = { Text("Stock Valuation", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedReportTab == 2, onClick = { selectedReportTab = 2 }, text = { Text("Expiry Audit", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
            Tab(selected = selectedReportTab == 3, onClick = { selectedReportTab = 3 }, text = { Text("Due & Payables", fontSize = 11.sp, fontWeight = FontWeight.Bold) })
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (selectedReportTab) {
            0 -> {
                // Sales & Profit Report
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Net Revenue",
                                value = AppLocale.formatCurrency(totalRevenue, language),
                                icon = Icons.Default.TrendingUp,
                                accentColor = PharmacyGreenPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Est. Gross Profit",
                                value = AppLocale.formatCurrency(grossProfit, language),
                                icon = Icons.Default.MonetizationOn,
                                accentColor = MedicalTealSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Financial Summary ($selectedPeriod)", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportLine("Total Invoices Count", "${completedPeriodSales.size}")
                                ReportLine("Total Sales Value", AppLocale.formatCurrency(totalRevenue + totalDiscount, language))
                                ReportLine("Discounts Given", "- " + AppLocale.formatCurrency(totalDiscount, language), color = AlertRed)
                                ReportLine("VAT / Tax Collected", AppLocale.formatCurrency(totalTax, language))
                                ReportLine("Cost of Goods Sold (COGS)", AppLocale.formatCurrency(totalCogs, language))
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                ReportLine("Gross Profit Estimate", AppLocale.formatCurrency(grossProfit, language), color = PharmacyGreenPrimary, isBold = true)
                                val margin = if (totalRevenue > 0) (grossProfit / totalRevenue) * 100 else 0.0
                                ReportLine("Gross Margin Rate", String.format("%.1f %%", margin), color = PharmacyGreenPrimary, isBold = true)
                            }
                        }
                    }
                }
            }
            1 -> {
                // Inventory Valuation
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Valuation at Cost",
                                value = AppLocale.formatCurrency(totalCostValue, language),
                                icon = Icons.Default.AccountBalance,
                                accentColor = Color(0xFF6366F1),
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Valuation at MRP",
                                value = AppLocale.formatCurrency(totalMrpValue, language),
                                icon = Icons.Default.Storefront,
                                accentColor = PharmacyGreenPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Stock Valuation Metrics", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportLine("Active Batches Count", "${activeBatches.size}")
                                ReportLine("Total Sellable Units", "${activeBatches.sumOf { it.availableQty }}")
                                ReportLine("Total Investment (Buy Cost)", AppLocale.formatCurrency(totalCostValue, language))
                                ReportLine("Retail Revenue Potential (MRP)", AppLocale.formatCurrency(totalMrpValue, language))
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                ReportLine("Potential Inventory Margin", AppLocale.formatCurrency(totalInventoryProfit, language), color = PharmacyGreenPrimary, isBold = true)
                            }
                        }
                    }
                }
            }
            2 -> {
                // Expiry Audit
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            MetricCard(
                                title = "< 30 Days",
                                value = "${expiring30.size}",
                                icon = Icons.Default.Warning,
                                accentColor = AlertRed,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "< 60 Days",
                                value = "${expiring60.size}",
                                icon = Icons.Default.HourglassTop,
                                accentColor = AlertAmber,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "< 90 Days",
                                value = "${expiring90.size}",
                                icon = Icons.Default.CalendarMonth,
                                accentColor = MedicalTealSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(text = "Near-Expiry Batches (< 90 Days)", fontWeight = FontWeight.Bold)
                    }

                    val nearExpiryList = expiring30 + expiring60 + expiring90
                    items(nearExpiryList) { b ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Batch: ${b.batchNumber}", fontWeight = FontWeight.Bold)
                                    Text(text = "Qty: ${b.availableQty} • Rack: ${b.storageLocation}", fontSize = 12.sp, color = Color.DarkGray)
                                    Text(text = "Supplier: ${b.supplierName}", fontSize = 10.sp, color = Color.Gray)
                                }
                                StatusChip(
                                    text = "Exp: ${b.expiryDate}",
                                    backgroundColor = if (b in expiring30) AlertRedLight else AlertAmberLight,
                                    textColor = if (b in expiring30) AlertRed else AlertAmber
                                )
                            }
                        }
                    }
                }
            }
            3 -> {
                // Dues & Payables
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            MetricCard(
                                title = "Customer Due (Receivable)",
                                value = AppLocale.formatCurrency(totalCustomerDue, language),
                                icon = Icons.Default.People,
                                accentColor = AlertAmber,
                                modifier = Modifier.weight(1f)
                            )
                            MetricCard(
                                title = "Supplier Payable (Dues)",
                                value = AppLocale.formatCurrency(totalSupplierPayable, language),
                                icon = Icons.Default.LocalShipping,
                                accentColor = AlertRed,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "Working Capital Balance", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.height(10.dp))
                                ReportLine("Customers with Outstanding Due", "${customers.count { it.currentDue > 0 }}")
                                ReportLine("Total Customer Receivables", AppLocale.formatCurrency(totalCustomerDue, language), color = AlertAmber)
                                ReportLine("Suppliers with Outstanding Balance", "${suppliers.count { it.payableBalance > 0 }}")
                                ReportLine("Total Supplier Payables", AppLocale.formatCurrency(totalSupplierPayable, language), color = AlertRed)
                                Divider(modifier = Modifier.padding(vertical = 6.dp))
                                val netBalance = totalCustomerDue - totalSupplierPayable
                                ReportLine("Net Due Differential", AppLocale.formatCurrency(netBalance, language), isBold = true)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportLine(label: String, value: String, color: Color = Color.Unspecified, isBold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal))
        Text(text = value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold, color = color))
    }
}

private fun exportReportCsv(
    context: Context,
    period: String,
    revenue: Double,
    profit: Double,
    costValue: Double,
    mrpValue: Double,
    custDue: Double,
    supPayable: Double,
    batches: List<MedicineBatch>
) {
    val sb = StringBuilder()
    sb.appendLine("MAK Pharma BD - Pharmacy Management Report")
    sb.appendLine("Report Period: $period")
    sb.appendLine("Generated At: ${System.currentTimeMillis()}")
    sb.appendLine("----------------------------------------")
    sb.appendLine("Metric,Value (BDT)")
    sb.appendLine("Net Sales Revenue,$revenue")
    sb.appendLine("Gross Profit Estimate,$profit")
    sb.appendLine("Inventory Cost Valuation,$costValue")
    sb.appendLine("Inventory MRP Valuation,$mrpValue")
    sb.appendLine("Customer Receivables,$custDue")
    sb.appendLine("Supplier Payables,$supPayable")
    sb.appendLine("----------------------------------------")
    sb.appendLine("Batch Number,Expiry Date,Available Qty,Buy Price,Sale Price,Supplier")
    for (b in batches) {
        sb.appendLine("${b.batchNumber},${b.expiryDate},${b.availableQty},${b.purchasePrice},${b.salePrice},\"${b.supplierName}\"")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_SUBJECT, "MAK Pharma BD Report - $period.csv")
        type = "text/csv"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Export Report CSV"))
}
