package com.smartbudge.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartbudge.app.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun SmartBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val isDark = true
    
    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        bgColor = CardDark,
        isGlass = false,
        cornerRadius = 32.dp,
        padding = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home & Reports
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
                NavItem(icon = Icons.Default.Home, label = "Home", isSelected = currentRoute == "home") { onNavigate("home") }
                NavItem(icon = Icons.Default.BarChart, label = "Reports", isSelected = currentRoute == "reports") { onNavigate("reports") }
            }

            // Centered FAB
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                    .clickable { onNavigate("add_transaction/expense") },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(24.dp))
            }

            // Goals & Profile
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.SpaceAround) {
                NavItem(icon = Icons.Default.Flag, label = "Goals", isSelected = currentRoute == "goals") { onNavigate("goals") }
                NavItem(icon = Icons.Default.Person, label = "Profile", isSelected = currentRoute == "profile") { onNavigate("profile") }
            }
        }
    }
}

@Composable
fun NavItem(icon: ImageVector, label: String, isSelected: Boolean, onClick: () -> Unit) {
    val isDark = true
    val tint by animateColorAsState(
        if (isSelected) Color.White else Color.White.copy(alpha = 0.5f)
    )
    val scale by animateFloatAsState(if (isSelected) 1.2f else 1f)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(24.dp).graphicsLayer(scaleX = scale, scaleY = scale)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.size(4.dp).background(PrimaryBlue, CircleShape))
        }
    }
}
