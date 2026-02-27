package com.smartbudge.app.ui.screens.goals

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
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.smartbudge.app.data.local.entity.SavingsGoalEntity
import com.smartbudge.app.ui.components.*
import com.smartbudge.app.ui.screens.home.formatCurrency
import com.smartbudge.app.ui.theme.*

@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel(),
    addGoalViewModel: AddGoalViewModel = hiltViewModel()
) {
    val goals by viewModel.savingsGoals.collectAsState()
    var showAddGoalDialog by remember { mutableStateOf(false) }

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
            Text("Savings Goals", style = MaterialTheme.typography.displayLarge, color = textColor)
            IconButton(
                onClick = { showAddGoalDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .background(PrimaryBlue, CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                // Summary Statistics
                val totalSaved = goals.sumOf { it.saved_amount }
                val totalTarget = goals.sumOf { it.target_amount }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Total Saved Card (Blue Glass)
                    PremiumCard(
                        modifier = Modifier.weight(1f),
                        isGlass = true,
                        bgColor = PrimaryBlue.copy(alpha = 0.6f),
                        cornerRadius = 24.dp,
                        padding = 20.dp
                    ) {
                        Column {
                            Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Total Saved", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.9f))
                            Text(formatCurrency(totalSaved), style = MaterialTheme.typography.titleLarge, color = Color.White)
                        }
                    }

                    // Total Target Card (Standard Glass)
                    PremiumCard(
                        modifier = Modifier.weight(1f),
                        isGlass = true,
                        bgColor = if (isDark) Color.Black.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.6f),
                        cornerRadius = 24.dp,
                        padding = 20.dp
                    ) {
                        Column {
                            Icon(Icons.Default.Flag, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Target", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                            Text(formatCurrency(totalTarget), style = MaterialTheme.typography.titleLarge, color = textColor)
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Active Goals",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor
                )
            }

            if (goals.isEmpty()) {
                item {
                    Text(
                        "No savings goals yet. Start saving for something special!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = mutedTextColor,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(goals) { goal ->
                    GoalCard(
                        goal = goal,
                        surfaceColor = surfaceColor,
                        textColor = textColor,
                        mutedTextColor = mutedTextColor,
                        isDark = isDark
                    )
                }
            }
            
            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            viewModel = addGoalViewModel,
            onDismiss = { showAddGoalDialog = false },
            onSave = {
                addGoalViewModel.saveGoal {
                    showAddGoalDialog = false
                }
            },
            isDark = isDark,
            surfaceColor = surfaceColor,
            textColor = textColor
        )
    }
}

@Composable
fun GoalCard(
    goal: SavingsGoalEntity,
    surfaceColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    isDark: Boolean
) {
    val progress = if (goal.target_amount > 0) (goal.saved_amount / goal.target_amount).toFloat() else 0f
    
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        isGlass = true,
        bgColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Black.copy(alpha = 0.03f),
        cornerRadius = 24.dp,
        padding = 24.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(PrimaryBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = PrimaryBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = goal.title, style = MaterialTheme.typography.titleLarge, color = textColor)
                        Text(
                            text = "Target: ${formatCurrency(goal.target_amount)}",
                            style = MaterialTheme.typography.labelMedium,
                            color = mutedTextColor
                        )
                    }
                }
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.DarkGray.copy(alpha = 0.3f) else iOSGreyBlue)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(CircleShape)
                        .background(PrimaryBlue)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Current Savings", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                    Text(formatCurrency(goal.saved_amount), style = MaterialTheme.typography.titleLarge, color = textColor)
                }
                
                val remaining = (goal.target_amount - goal.saved_amount).coerceAtLeast(0.0)
                if (remaining > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Remaining", style = MaterialTheme.typography.labelMedium, color = mutedTextColor)
                        Text(formatCurrency(remaining), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                    }
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Completed", tint = iOSGreen)
                }
            }
        }
    }
}

@Composable
fun AddGoalDialog(
    viewModel: AddGoalViewModel,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    isDark: Boolean,
    surfaceColor: Color,
    textColor: Color
) {
    val title by viewModel.title.collectAsState()
    val targetAmount by viewModel.targetAmount.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor, RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(text = "New Savings Goal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = textColor)
            Spacer(modifier = Modifier.height(24.dp))
            
            SmartTextField(
                value = title,
                onValueChange = { viewModel.setTitle(it) },
                label = "Goal Name (e.g. New Phone)"
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            SmartTextField(
                value = targetAmount,
                onValueChange = { viewModel.setTargetAmount(it) },
                label = "Target Amount",
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                )
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) { Text("Cancel", color = if (isDark) MutedTextDark else MutedTextLight) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onSave,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Save Goal", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
