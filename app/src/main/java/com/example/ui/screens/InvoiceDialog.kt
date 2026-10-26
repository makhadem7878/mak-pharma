package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.PharmacySettings
import com.example.data.model.Sale
import com.example.data.model.SaleItem
import com.example.ui.locale.AppLocale
import com.example.ui.locale.Language
import com.example.ui.theme.PharmacyGreenPrimary
import com.example.ui.theme.PharmacyGreenPrimaryDark
import com.example.ui.theme.TextPrimary

@Composable
fun InvoiceDialog(
    sale: Sale,
    items: List<SaleItem>,
    settings: PharmacySettings,
    language: Language,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppLocale.get("invoice_preview", language),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row {
                        IconButton(onClick = { shareInvoiceText(context, sale, items, settings, language) }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Thermal Receipt View
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                        .padding(16.dp)
                ) {
                    // Pharmacy Header
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = PharmacyGreenPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == Language.BANGLA) settings.pharmacyBanglaName else settings.pharmacyName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = PharmacyGreenPrimary
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = settings.address,
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Hotline: ${settings.phone}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "=== CASH MEMO / RETAIL INVOICE ===",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                color = Color.Gray
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Meta Info
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Invoice: ${sale.invoiceNumber}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Date: ${sale.dateTimeStr}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Customer: ${sale.customerName}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (sale.customerPhone.isNotBlank()) {
                                Text(
                                    text = "Phone: ${sale.customerPhone}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                text = "Cashier: ${sale.cashierName}",
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(8.dp))

                    // Items Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF1F5F9))
                            .padding(vertical = 4.dp, horizontal = 6.dp)
                    ) {
                        Text(
                            text = AppLocale.get("item", language),
                            modifier = Modifier.weight(2.5f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppLocale.get("batch", language),
                            modifier = Modifier.weight(1.5f),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppLocale.get("qty", language),
                            modifier = Modifier.weight(0.8f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppLocale.get("price", language),
                            modifier = Modifier.weight(1.2f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = AppLocale.get("total", language),
                            modifier = Modifier.weight(1.3f),
                            textAlign = TextAlign.End,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Items rows
                    items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(2.5f)) {
                                Text(
                                    text = item.medicineName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${item.dosageForm} ${item.strength}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 10.sp)
                                )
                            }
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = item.batchNumber,
                                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                                )
                                Text(
                                    text = "E:${item.expiryDate}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray, fontSize = 9.sp)
                                )
                            }
                            Text(
                                text = "${item.quantity}",
                                modifier = Modifier.weight(0.8f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = String.format("%.2f", item.unitPrice),
                                modifier = Modifier.weight(1.2f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = String.format("%.2f", item.totalPrice),
                                modifier = Modifier.weight(1.3f),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Divider(color = Color(0xFFF1F5F9))
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Totals Section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        InvoiceRow("Subtotal", AppLocale.formatCurrency(sale.subtotal, language))
                        if (sale.discountAmount > 0) {
                            InvoiceRow("Discount", "- ${AppLocale.formatCurrency(sale.discountAmount, language)}", color = Color(0xFFDC2626))
                        }
                        if (sale.taxAmount > 0) {
                            InvoiceRow("VAT / Tax", AppLocale.formatCurrency(sale.taxAmount, language))
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        InvoiceRow("Grand Total", AppLocale.formatCurrency(sale.grandTotal, language), isBold = true, fontSize = 16.sp)
                        InvoiceRow("Paid Amount", AppLocale.formatCurrency(sale.paidAmount, language))
                        if (sale.dueAmount > 0) {
                            InvoiceRow("Due Amount", AppLocale.formatCurrency(sale.dueAmount, language), color = Color(0xFFD97706), isBold = true)
                        }
                        if (sale.changeReturn > 0) {
                            InvoiceRow("Change Return", AppLocale.formatCurrency(sale.changeReturn, language), color = PharmacyGreenPrimary)
                        }
                        InvoiceRow("Payment Method", "${sale.paymentMethod} ${sale.mobileBankingProvider}".trim())
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Footer notes
                    Text(
                        text = if (language == Language.BANGLA) settings.invoiceFooterNoteBn else settings.invoiceFooterNoteEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = { shareInvoiceText(context, sale, items, settings, language) },
                        colors = ButtonDefaults.buttonColors(containerColor = PharmacyGreenPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = AppLocale.get("print_share", language))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onDismiss) {
                        Text(text = AppLocale.get("close", language))
                    }
                }
            }
        }
    }
}

@Composable
private fun InvoiceRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified,
    isBold: Boolean = false,
    fontSize: androidx.compose.ui.unit.TextUnit = 13.sp
) {
    Row(
        modifier = Modifier
            .widthIn(min = 220.dp)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                fontSize = fontSize
            )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                color = color,
                fontSize = fontSize
            )
        )
    }
}

private fun shareInvoiceText(
    context: Context,
    sale: Sale,
    items: List<SaleItem>,
    settings: PharmacySettings,
    language: Language
) {
    val sb = StringBuilder()
    sb.appendLine("=========================================")
    sb.appendLine("          ${settings.pharmacyName}          ")
    sb.appendLine("       ${settings.pharmacyBanglaName}       ")
    sb.appendLine("   ${settings.address}   ")
    sb.appendLine("   Tel: ${settings.phone}   ")
    sb.appendLine("=========================================")
    sb.appendLine("Invoice No : ${sale.invoiceNumber}")
    sb.appendLine("Date/Time  : ${sale.dateTimeStr}")
    sb.appendLine("Customer   : ${sale.customerName} ${sale.customerPhone}")
    sb.appendLine("Cashier    : ${sale.cashierName}")
    sb.appendLine("-----------------------------------------")
    sb.appendLine(String.format("%-18s %-8s %3s %8s", "ITEM", "BATCH", "QTY", "TOTAL"))
    sb.appendLine("-----------------------------------------")
    for (item in items) {
        val name = if (item.medicineName.length > 17) item.medicineName.take(16) + "." else item.medicineName
        sb.appendLine(String.format("%-18s %-8s %3d %8.2f", name, item.batchNumber.take(8), item.quantity, item.totalPrice))
    }
    sb.appendLine("-----------------------------------------")
    sb.appendLine(String.format("Subtotal       : %10.2f", sale.subtotal))
    if (sale.discountAmount > 0) sb.appendLine(String.format("Discount       : -%9.2f", sale.discountAmount))
    if (sale.taxAmount > 0) sb.appendLine(String.format("VAT/Tax        : %10.2f", sale.taxAmount))
    sb.appendLine(String.format("Grand Total    : ৳%9.2f", sale.grandTotal))
    sb.appendLine(String.format("Paid Amount    : ৳%9.2f", sale.paidAmount))
    if (sale.dueAmount > 0) sb.appendLine(String.format("Due Amount     : ৳%9.2f", sale.dueAmount))
    if (sale.changeReturn > 0) sb.appendLine(String.format("Change Return  : ৳%9.2f", sale.changeReturn))
    sb.appendLine("Payment Method : ${sale.paymentMethod} ${sale.mobileBankingProvider}")
    sb.appendLine("=========================================")
    sb.appendLine(settings.invoiceFooterNoteEn)
    sb.appendLine(settings.invoiceFooterNoteBn)
    sb.appendLine("=========================================")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_SUBJECT, "Invoice ${sale.invoiceNumber} - ${settings.pharmacyName}")
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "Share Invoice"))
}
