package com.smartbudge.app.ui.screens.reports

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.ui.screens.home.formatCurrency
import com.smartbudge.app.ui.theme.*
import com.smartbudge.app.ui.components.*

@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val categorySpending by viewModel.categorySpending.collectAsState()
    val timeFilter by viewModel.timeFilter.collectAsState()

    val isDark = true
    val backgroundColor = if (isDark) BackgroundDark else BackgroundLight
    val surfaceColor = if (isDark) CardDark else CardLight
    val textColor = if (isDark) TextDark else TextLight
    val mutedTextColor = if (isDark) MutedTextDark else MutedTextLight

    GlowingBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 24.dp)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Financial Reports", style = MaterialTheme.typography.displayLarge, color = textColor)
            IconButton(
                onClick = { },
                modifier = Modifier
                    .size(44.dp)
                    .background(if (isDark) CardDark else iOSGreyBlue, CircleShape)
            ) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Time Filter Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("Weekly", "Yearly").forEach { filter ->
                        val isSelected = timeFilter == filter
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) (if (isDark) PrimaryBlue else Color.White) else Color.Transparent)
                                .clickable { viewModel.setTimeFilter(filter) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = filter,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) (if (isDark) Color.White else TextLight) else mutedTextColor
                            )
                        }
                    }
                }
            }

            item {
                val totalSpent = categorySpending.sumOf { it.totalAmount }
                PremiumCard(
                    modifier = Modifier.fillMaxWidth(),
                    isGlass = true,
                    bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                    cornerRadius = 24.dp
                ) {
                    Column {
                        Text("Total Outgoing", style = MaterialTheme.typography.bodyLarge, color = mutedTextColor)
                        Text(
                            text = formatCurrency(totalSpent),
                            style = MaterialTheme.typography.displayLarge,
                            color = textColor
                        )
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        // Placeholder Bar Chart (Simple clean style)
                        Text("INCOME VS EXPENSE", style = MaterialTheme.typography.labelMedium, color = mutedTextColor, letterSpacing = 1.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.65f, 0.45f, 0.85f, 0.35f, 0.95f, 0.55f, 0.25f)
                            val days = listOf("M", "T", "W", "T", "F", "S", "S")
                            for (i in 0..6) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(0.5f)
                                            .weight(1f)
                                            .background(if (isDark) Color.DarkGray.copy(alpha = 0.2f) else iOSGreyBlue, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .fillMaxHeight(heights[i])
                                                .background(PrimaryBlue, RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(days[i], style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                                }
                            }
                        }
                    }
                }
            }

            if (categorySpending.isEmpty()) {
                item {
                    Text(
                        "No data available for this period.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedTextColor,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            } else {
                item {
                    Text("Spending by Category", style = MaterialTheme.typography.titleLarge, color = textColor)
                }

                item {
                    PremiumCard(
                        modifier = Modifier.fillMaxWidth(),
                        isGlass = true,
                        bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
                        cornerRadius = 24.dp,
                        padding = 24.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            categorySpending.forEach { spending ->
                                CategorySpendingItem(spending, textColor, mutedTextColor)
                            }
                        }
                    }
                }
            }


            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
    }
}

@Composable
fun CategorySpendingItem(spending: CategorySpending, textColor: Color, mutedTextColor: Color) {
    val animatedProgress by animateFloatAsState(
        targetValue = spending.percentage,
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color(android.graphics.Color.parseColor(spending.category.color)), CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = spending.category.name, fontWeight = FontWeight.Medium, fontSize = 14.sp, color = textColor)
                Spacer(modifier = Modifier.height(4.dp))
                // Clean minimal progress bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(4.dp)
                        .background(mutedTextColor.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress)
                            .fillMaxHeight()
                            .background(Color(android.graphics.Color.parseColor(spending.category.color)), RoundedCornerShape(2.dp))
                    )
                }
            }
        }
        Text(
            text = formatCurrency(spending.totalAmount),
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = textColor
        )
    }
}
