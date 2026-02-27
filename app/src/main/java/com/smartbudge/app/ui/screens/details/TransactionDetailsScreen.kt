package com.smartbudge.app.ui.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.ui.components.PremiumCard
import com.smartbudge.app.ui.screens.home.formatCurrency
import com.smartbudge.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String, Int) -> Unit,
    viewModel: TransactionDetailsViewModel = hiltViewModel()
) {
    val transaction by viewModel.transaction.collectAsState()
    val categoryName by viewModel.categoryName.collectAsState()
    
    val isDark = LocalThemeIsDark.current
    val bgColor = if (isDark) BackgroundDark else BackgroundLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight
    val surfaceColor = if (isDark) CardDark else CardLight

    var showDeleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        TopAppBar(
            title = { Text("Transaction Details", color = textColor, fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (transaction != null) {
            val t = transaction!!
            val isIncome = t.type == "Income"
            val iconColor = if (isIncome) iOSGreen else iOSRed
            val iconBg = iconColor.copy(alpha = 0.1f)
            val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Main Info Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlass = true,
                    bgColor = surfaceColor,
                    cornerRadius = 32.dp,
                    padding = 32.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(iconBg, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(32.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "${if (isIncome) "+" else "-"}${formatCurrency(t.amount)}",
                            style = MaterialTheme.typography.displayLarge,
                            color = textColor
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = t.note.ifEmpty { "No Description" },
                            style = MaterialTheme.typography.titleLarge,
                            color = mutedTextColor,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        HorizontalDivider(color = mutedTextColor.copy(alpha = 0.2f))
                        
                        Spacer(modifier = Modifier.height(24.dp))

                        DetailRow("Type", t.type, textColor, mutedTextColor)
                        DetailRow("Category", categoryName, textColor, mutedTextColor)
                        val sdf = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
                        DetailRow("Date", sdf.format(Date(t.date)), textColor, mutedTextColor)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlass = true,
                    bgColor = surfaceColor,
                    cornerRadius = 24.dp,
                    padding = 16.dp
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { onNavigateToEdit(t.type.lowercase(), t.transaction_id) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Edit Transaction", color = Color.White, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = iOSRed),
                            border = androidx.compose.foundation.BorderStroke(1.dp, iOSRed.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = iOSRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Transaction", color = iOSRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Delete Transaction", color = textColor) },
                    text = { Text("Are you sure you want to delete this transaction? This action cannot be undone.", color = mutedTextColor) },
                    confirmButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            viewModel.deleteTransaction(t) {
                                onNavigateBack()
                            }
                        }) {
                            Text("Delete", color = iOSRed)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog = false }) {
                            Text("Cancel", color = textColor)
                        }
                    },
                    containerColor = surfaceColor
                )
            }
        } else {
            // Loading or not found state
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, textColor: Color, mutedTextColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = mutedTextColor)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = textColor, fontWeight = FontWeight.Bold)
    }
}
