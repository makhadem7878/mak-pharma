package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatusChip
import com.example.ui.locale.Language
import com.example.ui.theme.*
import com.example.ui.viewmodel.PharmacyViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditLogsScreen(viewModel: PharmacyViewModel) {
    val language by viewModel.currentLanguage.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var actionFilter by remember { mutableStateOf("ALL") }

    val filtered = auditLogs.filter { log ->
        val matchesSearch = searchQuery.isBlank() ||
                log.details.contains(searchQuery, ignoreCase = true) ||
                log.userName.contains(searchQuery, ignoreCase = true) ||
                log.entityId.contains(searchQuery, ignoreCase = true)

        val matchesAction = when (actionFilter) {
            "SALE" -> log.actionType.contains("SALE")
            "STOCK" -> log.actionType.contains("STOCK")
            "PURCHASE" -> log.actionType.contains("PURCHASE")
            "DUE" -> log.actionType.contains("DUE")
            else -> true
        }

        matchesSearch && matchesAction
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftBackground)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Security, contentDescription = null, tint = PharmacyGreenPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "Security Audit Trail & Compliance", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text(text = "Immutable log of sales, cancellations, adjustments & manager actions", style = MaterialTheme.typography.labelSmall.copy(color = Color.Gray))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search logs by keyword, user, or invoice #...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PharmacyGreenPrimary) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL" to "All Logs", "SALE" to "Sales & Refunds", "STOCK" to "Stock Movements", "PURCHASE" to "Purchases", "DUE" to "Due Collections").forEach { (code, label) ->
                val isSel = actionFilter == code
                FilterChip(
                    selected = isSel,
                    onClick = { actionFilter = code },
                    label = { Text(label, fontSize = 11.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No audit log records match current query.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(filtered) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusChip(
                                    text = log.actionType,
                                    backgroundColor = when {
                                        log.actionType.contains("REFUND") -> AlertRedLight
                                        log.actionType.contains("ADJUST") -> AlertAmberLight
                                        else -> PharmacyGreenLight
                                    },
                                    textColor = when {
                                        log.actionType.contains("REFUND") -> AlertRed
                                        log.actionType.contains("ADJUST") -> AlertAmber
                                        else -> PharmacyGreenDark
                                    }
                                )

                                Text(text = log.dateTimeStr, fontSize = 11.sp, color = Color.Gray)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(text = log.details, style = MaterialTheme.typography.bodyMedium)

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "By: ${log.userName} (${log.userRole})",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MedicalTealSecondary, fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Ref: ${log.entityId}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color.DarkGray)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
