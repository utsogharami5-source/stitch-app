package com.smartbudge.app.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.data.local.entity.CategoryEntity
import com.smartbudge.app.ui.theme.*
import com.smartbudge.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel = hiltViewModel(),
    type: String = "expense",
    transactionId: Int? = null,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(type, transactionId) {
        viewModel.initType(type)
        if (transactionId != null) {
            viewModel.initForEdit(transactionId)
        }
    }

    val amount by viewModel.amount.collectAsState()
    val note by viewModel.note.collectAsState()
    val isIncome by viewModel.isIncome.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val isDark = true
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) CardDark else CardLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight

    GlowingBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding()
        ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isDark) CardDark else iOSGreyBlue, CircleShape)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(if (transactionId != null) "Edit Record" else "Add Record", style = MaterialTheme.typography.titleLarge, color = textColor)
            PremiumButton(
                text = "Save",
                onClick = { viewModel.saveTransaction { onNavigateBack() } },
                modifier = Modifier
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Transaction Type Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isIncome) (if (isDark) PrimaryBlue else Color.White) else Color.Transparent)
                            .clickable { viewModel.setIsIncome(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Income",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isIncome) FontWeight.Bold else FontWeight.Medium,
                            color = if (isIncome) (if (isDark) Color.White else iOSGreen) else mutedTextColor
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (!isIncome) (if (isDark) PrimaryBlue else Color.White) else Color.Transparent)
                            .clickable { viewModel.setIsIncome(false) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Expense",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (!isIncome) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isIncome) (if (isDark) Color.White else iOSRed) else mutedTextColor
                        )
                    }
                }
            }

            item {
                // Amount Input Card
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlass = true,
                    bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                    cornerRadius = 24.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Amount (৳)", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = amount,
                            onValueChange = { viewModel.setAmount(it) },
                            textStyle = MaterialTheme.typography.displayLarge.copy(
                                textAlign = TextAlign.Center,
                                color = if (isIncome) iOSGreen else iOSRed
                            ),
                            placeholder = {
                                Text(
                                    "0.00",
                                    style = MaterialTheme.typography.displayLarge,
                                    textAlign = TextAlign.Center,
                                    color = mutedTextColor.copy(alpha = 0.3f)
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                // Details Section
                Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                    // Category Section
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Category", style = MaterialTheme.typography.titleLarge, color = textColor)
                        
                        if (categories.isEmpty()) {
                            Text("No categories yet", style = MaterialTheme.typography.bodyLarge, color = mutedTextColor)
                        } else {
                            // Simple Flow-like Grid
                            val chunks = categories.chunked(3)
                            chunks.forEach { rowCategories ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowCategories.forEach { category ->
                                        val isSelected = selectedCategory?.category_id == category.category_id
                                        CategoryChip(
                                            category = category,
                                            isSelected = isSelected,
                                            isDark = isDark,
                                            onClick = { viewModel.setCategory(category) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowCategories.size < 3) {
                                        repeat(3 - rowCategories.size) { Spacer(modifier = Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }
                    }

                    // Note Section
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Note", style = MaterialTheme.typography.titleLarge, color = textColor)
                        TextField(
                            value = note,
                            onValueChange = { newNote -> viewModel.setNote(newNote) },
                            placeholder = { Text("What was this for?", style = MaterialTheme.typography.bodyLarge, color = mutedTextColor) },
                            textStyle = MaterialTheme.typography.bodyLarge,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = surfaceColor,
                                unfocusedContainerColor = surfaceColor,
                                focusedIndicatorColor = PrimaryBlue,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }

        // Add Transaction Button at bottom
        Box(modifier = Modifier.padding(24.dp)) {
            PremiumButton(
                text = if (transactionId != null) "Save Changes" else "Add Transaction",
                onClick = { viewModel.saveTransaction { onNavigateBack() } },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            )
        }
    }
    }
}

@Composable
fun CategoryChip(
    category: CategoryEntity,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isSelected) PrimaryBlue else (if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
    val border = if (isSelected) Color.Transparent else (if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f))

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelMedium,
            color = if (isSelected) Color.White else (if (isDark) TextDark else TextLight),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
