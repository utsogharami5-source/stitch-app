package com.smartbudge.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.ui.theme.*
import com.smartbudge.app.ui.components.*
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToAddTransaction: (String, Int?) -> Unit,
    onOpenDrawer: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val balance by viewModel.totalBalance.collectAsState()
    val totalIncome by viewModel.totalIncome.collectAsState()
    val totalExpense by viewModel.totalExpense.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    
    val isDark = true
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) CardDark else CardLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight

    GlowingBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                AppHeader(
                    name = user?.name?.ifEmpty { "User" } ?: "User",
                    isDark = isDark,
                    onMenuClick = onOpenDrawer,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor
                )
            }
            item {
                TotalBalanceCard(
                    balance = balance,
                    totalIncome = totalIncome,
                    totalExpense = totalExpense,
                    surfaceColor = surfaceColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    isDark = isDark
                )
            }
            
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recent Transactions", style = MaterialTheme.typography.titleLarge, color = textColor)
                    Text("See All", style = MaterialTheme.typography.labelMedium, color = PrimaryBlue, modifier = Modifier.clickable { /* TODO */ })
                }
            }
            
            if (recentTransactions.isEmpty()) {
                item {
                    Text(
                        "No transactions yet.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedTextColor,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(recentTransactions) { transaction ->
                    var showOptionsDialog by remember { mutableStateOf(false) }

                    TransactionItem(
                        transaction = transaction,
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        isDark = isDark,
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
            }
            // Padding for bottom nav overlap
            item { Spacer(modifier = Modifier.height(111.dp)) }
        }
    }
}

@Composable
fun AppHeader(name: String, isDark: Boolean, onMenuClick: () -> Unit, textColor: Color, mutedTextColor: Color) {
    val headerBg = if (isDark) BackgroundDark else BackgroundLight
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Good Morning,",
                style = MaterialTheme.typography.bodyLarge,
                color = mutedTextColor
            )
            Text(
                text = name,
                style = MaterialTheme.typography.displayLarge,
                color = textColor
            )
        }
        
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(44.dp)
                .background(if (isDark) CardDark else iOSGreyBlue, RoundedCornerShape(22.dp))
        ) {
            Icon(Icons.Default.Person, contentDescription = "Profile", tint = Color.White)
        }
    }
}

@Composable
fun TotalBalanceCard(balance: Double, totalIncome: Double, totalExpense: Double, surfaceColor: Color, textColor: Color, mutedTextColor: Color, isDark: Boolean) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        bgColor = surfaceColor,
        isGlass = true,
        cornerRadius = 32.dp,
        padding = 28.dp
    ) {
        val cardContentColor = if (isDark) textColor else Color.White
        val cardMutedColor = if (isDark) mutedTextColor else Color.White.copy(alpha = 0.7f)

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Total Balance",
                        style = MaterialTheme.typography.bodyLarge,
                        color = cardMutedColor
                    )
                    Text(
                        text = formatCurrency(balance),
                        style = MaterialTheme.typography.displayLarge,
                        color = cardContentColor
                    )
                }
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Wallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(), 
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Mini-Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = if (isDark) iOSGreen else Color.White, modifier = Modifier.size(16.dp))
                        Text("INCOME", style = MaterialTheme.typography.labelMedium, color = cardMutedColor)
                    }
                    Text(formatCurrency(totalIncome), style = MaterialTheme.typography.titleLarge, color = cardContentColor)
                }
                
                // Expense Mini-Card
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.TrendingDown, contentDescription = null, tint = if (isDark) iOSRed else Color.White, modifier = Modifier.size(16.dp))
                        Text("EXPENSE", style = MaterialTheme.typography.labelMedium, color = cardMutedColor)
                    }
                    Text(formatCurrency(totalExpense), style = MaterialTheme.typography.titleLarge, color = cardContentColor)
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: com.smartbudge.app.data.local.entity.TransactionEntity,
    surfaceColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    isDark: Boolean,
    onClick: () -> Unit = {}
) {
    val isIncome = transaction.type == "Income"
    val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val iconColor = if (isIncome) iOSGreen else iOSRed
    val iconBg = iconColor.copy(alpha = 0.1f)

    PremiumCard(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        isGlass = true,
        bgColor = surfaceColor,
        padding = 12.dp,
        cornerRadius = 24.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = iconColor, 
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note.ifEmpty { "Transaction" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                Text(
                    text = sdf.format(java.util.Date(transaction.date)),
                    style = MaterialTheme.typography.labelMedium,
                    color = mutedTextColor
                )
            }
            Text(
                text = "${if (isIncome) "+" else "-"}${formatCurrency(transaction.amount)}",
                style = MaterialTheme.typography.titleLarge,
                color = if (isIncome) iOSGreen else iOSRed
            )
        }
    }
}

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.getDefault())
    format.currency = Currency.getInstance("BDT")
    return format.format(amount)
}
