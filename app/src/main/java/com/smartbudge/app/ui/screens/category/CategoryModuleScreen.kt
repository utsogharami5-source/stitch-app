package com.smartbudge.app.ui.screens.category

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.data.local.entity.TransactionEntity
import com.smartbudge.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryModuleScreen(
    categoryName: String,
    viewModel: CategoryModuleViewModel = hiltViewModel(),
    onNavigateToAddTransaction: (String, Int?) -> Unit = { _, _ -> },
    onNavigateBack: () -> Unit = {}
) {
    LaunchedEffect(categoryName) {
        viewModel.setCategory(categoryName)
    }

    val transactions by viewModel.transactions.collectAsState()
    val isDark = LocalThemeIsDark.current
    
    val bgColor = if (isDark) BackgroundDark else BackgroundLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight
    val surfaceColor = if (isDark) CardDark else CardLight

    val accentColor = when (categoryName.lowercase()) {
        "food" -> iOSGreen
        "entertainment" -> iOSPurple
        "utilities" -> iOSBlue
        else -> PrimaryBlue
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        // App Bar
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = categoryName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Summary Card
        val totalSpent = transactions.sumOf { it.amount }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = accentColor.copy(alpha = 0.1f)
            ),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = accentColor.copy(alpha = 0.2f)
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Total Spent",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "৳${String.format("%.2f", totalSpent)}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
            }
        }

        Text(
            "Recent Transactions",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(transactions) { transaction ->
                var showOptionsDialog by remember { mutableStateOf(false) }

                TransactionRow(
                    transaction = transaction,
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    onClick = { showOptionsDialog = true }
                )

                if (showOptionsDialog) {
                    AlertDialog(
                        onDismissRequest = { showOptionsDialog = false },
                        title = { Text("Transaction Options") },
                        text = { Text("What would you like to do with this transaction?") },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showOptionsDialog = false
                                    val typePath = transaction.type.lowercase()
                                    onNavigateToAddTransaction(typePath, transaction.transaction_id)
                                }
                            ) {
                                Text("Edit")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    showOptionsDialog = false
                                    viewModel.deleteTransaction(transaction)
                                }
                            ) {
                                Text("Delete", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    )
                }
            }
            
            if (transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No transactions found for $categoryName",
                            fontSize = 14.sp,
                            color = mutedTextColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: TransactionEntity,
    surfaceColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    onClick: () -> Unit = {}
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(surfaceColor, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(iOSRed.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("৳", color = iOSRed, fontWeight = FontWeight.Bold)
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                transaction.note.ifEmpty { "Transaction" },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor
            )
            Text(
                dateFormat.format(Date(transaction.date)),
                fontSize = 12.sp,
                color = mutedTextColor
            )
        }
        
        Text(
            "-৳${String.format("%.2f", transaction.amount)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = iOSRed
        )
    }
}
